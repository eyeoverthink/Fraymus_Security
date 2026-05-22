package com.fraymus.absorbed.mlxrunner;

import java.util.*;
import java.io.*;

public class cache_trie {
        "fmt";
        "slices";
        "time";
        "github.com/ollama/ollama/x/mlxrunner/cache";
        );

    public static class trieNode {
        public []int32 tokens;
        public int endOffset;
        public *trieNode parent;
        public []*trieNode children;
        public time.Time lastUsed;
        public []cache.Snapshot snapshots;
        public boolean user;
    }
        func (n *trieNode) startOffset() int {
        return n.endOffset - len(n.tokens);
    }
        func (n *trieNode) snapshotBytes() long {
        var total long;
        var for _, s = range n.snapshots {
        if s != null {
        total += long(s.Size());
    }
    }
        return total;
    }
        func (n *trieNode) setSnapshots(snaps []cache.Snapshot, counter *long) {
        var old = n.swapSnapshots(snaps, counter);
        var for _, s = range old {
        if s != null {
        s.Close();
    }
    }
    }
        func (n *trieNode) swapSnapshots(snaps []cache.Snapshot, counter *long) []cache.Snapshot {
        var old = n.snapshots;
        if counter != null {
        *counter -= n.snapshotBytes();
    }
        n.snapshots = snaps;
        if counter != null {
        *counter += n.snapshotBytes();
    }
        return old;
    }
        func (n *trieNode) hasSnapshots() boolean {
        return slices.ContainsFunc(n.snapshots, func(s cache.Snapshot) boolean { return s != null });
    }
        func (n *trieNode) hasAllSnapshots() boolean {
        return len(n.snapshots) > 0 && !slices.Contains(n.snapshots, null);
    }

    public static void findBestMatch(*trieNode root, int matched) {
        if root == null {
        return null, 0;
    }
        path = []*trieNode{root}
        var pos = 0;
        var node = root;
        for pos < len(tokens) {
        var best *trieNode;
        var bestMatched = 0;
        var bestFull = false;
        var for _, child = range node.children {
        var edge = child.tokens;
        if len(edge) == 0 {
        continue;
    }
        if edge[0] != tokens[pos] {
        continue;
    }
        var j = 0;
        for j < len(edge) && pos+j < len(tokens) && edge[j] == tokens[pos+j] {
        j++;
    }
        var full = j == len(edge);
        if best == null || (full && !bestFull) || (full == bestFull && j > bestMatched) {
        best = child;
        bestMatched = j;
        bestFull = full;
    }
    }
        if best == null {
        break;
    }
        pos += bestMatched;
        path = append(path, best);
        if !bestFull {
        break;
    }
        node = best;
    }
        return path, pos;
    }
        func (n *trieNode) appendTokens(root *trieNode, tokens []int32, endOffset int) *trieNode {
        if n == root || len(n.children) > 0 || n.hasSnapshots() {
        var child = &trieNode{
        tokens:    make([]int32, len(tokens)),;
        endOffset: endOffset,;
        parent:    n,;
        lastUsed:  n.lastUsed,;
    }
        copy(child.tokens, tokens);
        n.children = append(n.children, child);
        return child;
    }
        n.tokens = append(n.tokens, tokens...);
        n.endOffset = endOffset;
        return n;
    }

    public static void removeNode(*trieNode node, *long counter) {
        if node.parent == null {
        panic("removeNode called on root");
    }
        if len(node.children) != 0 {
        panic("removeNode called on non-leaf node");
    }
        var p = node.parent;
        var for i, child = range p.children {
        if child == node {
        p.children = append(p.children[:i], p.children[i+1:]...);
        break;
    }
    }
        node.parent = null;
        node.setSnapshots(null, counter);
    }
        func splitNode(node *trieNode, at int, caches []cache.Cache, counter *long) *trieNode {
        if at <= 0 || at >= len(node.tokens) {
        panic(fmt.Sprintf("splitNode: invalid split offset %d for node with %d tokens", at, len(node.tokens)));
    }
        var newParent = &trieNode{
        tokens:    make([]int32, at),;
        endOffset: node.startOffset() + at,;
        parent:    node.parent,;
        children:  []*trieNode{node},;
        lastUsed:  node.lastUsed,;
    }
        copy(newParent.tokens, node.tokens[:at]);
        node.tokens = node.tokens[at:];
        if node.hasSnapshots() {
        var oldSnaps = node.swapSnapshots(null, counter);
        var parentSnaps = make([]cache.Snapshot, len(oldSnaps));
        var childSnaps = make([]cache.Snapshot, len(oldSnaps));
        var for i, snap = range oldSnaps {
        if snap != null {
        parentSnaps[i], childSnaps[i] = caches[i].Split(snap, newParent.endOffset);
    }
    }
        newParent.setSnapshots(parentSnaps, counter);
        node.setSnapshots(childSnaps, counter);
    }
        if node.parent != null {
        var for i, child = range node.parent.children {
        if child == node {
        node.parent.children[i] = newParent;
        break;
    }
    }
    }
        node.parent = newParent;
        return newParent;
    }

    public static void mergeWithChild(*trieNode node, []cache.Cache caches, *long counter) {
        if len(node.children) != 1 {
        panic(fmt.Sprintf("mergeWithChild called on node with %d children", len(node.children)));
    }
        var child = node.children[0];
        node.tokens = append(node.tokens, child.tokens...);
        node.endOffset = child.endOffset;
        if len(node.snapshots) > 0 || len(child.snapshots) > 0 {
        var nodeSnaps = node.swapSnapshots(null, counter);
        var childSnaps = child.swapSnapshots(null, counter);
        var merged = make([]cache.Snapshot, len(caches));
        var for i = range caches {
        var ps, cs cache.Snapshot;
        if nodeSnaps != null {
        ps = nodeSnaps[i];
    }
        if childSnaps != null {
        cs = childSnaps[i];
    }
        merged[i] = caches[i].Merge(ps, cs);
    }
        node.setSnapshots(merged, counter);
    }
        node.children = child.children;
        var for _, gc = range node.children {
        gc.parent = node;
    }
        node.user = child.user;
        if child.lastUsed.After(node.lastUsed) {
        node.lastUsed = child.lastUsed;
    }
        child.parent = null;
        child.children = null;
    }

    public static void walkNodes(*trieNode root) {
        if root == null {
        return;
    }
        var walk func(*trieNode) boolean;
        walk = func(n *trieNode) boolean {
        if !fn(n) {
        return false;
    }
        var for _, child = range n.children {
        if !walk(child) {
        return false;
    }
    }
        return true;
    }
        walk(root);
    }
}
