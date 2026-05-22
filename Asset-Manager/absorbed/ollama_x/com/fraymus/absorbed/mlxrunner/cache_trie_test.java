package com.fraymus.absorbed.mlxrunner;

import java.util.*;
import java.io.*;

public class cache_trie_test {
        "slices";
        "testing";
        "time";
        "github.com/ollama/ollama/x/mlxrunner/cache";
        );
        func newTestTrie(tokens []int32) *trieNode {
        var root = &trieNode{lastUsed: time.Now()}
        if len(tokens) > 0 {
        var child = &trieNode{
        tokens:    slices.Clone(tokens),;
        endOffset: len(tokens),;
        parent:    root,;
        lastUsed:  time.Now(),;
    }
        root.children = []*trieNode{child}
    }
        return root;
    }

    public static void TestFindBestMatchMultipleBranches(*testing.T t) {
        var root = &trieNode{lastUsed: time.Now()}
        var branch1 = &trieNode{
        tokens:    []int32{1, 2, 3},;
        endOffset: 3,;
        parent:    root,;
        lastUsed:  time.Now(),;
    }
        var branch2 = &trieNode{
        tokens:    []int32{4, 5, 6},;
        endOffset: 3,;
        parent:    root,;
        lastUsed:  time.Now(),;
    }
        root.children = []*trieNode{branch1, branch2}
        var path, matched = findBestMatch(root, []int32{1, 2, 3, 7});
        if matched != 3 {
        t.Fatalf("expected 3 matched, got %d", matched);
    }
        if len(path) != 2 || path[1] != branch1 {
        t.Fatal("expected to match branch1");
    }
        path, matched = findBestMatch(root, []int32{4, 5, 6, 8});
        if matched != 3 {
        t.Fatalf("expected 3 matched, got %d", matched);
    }
        if len(path) != 2 || path[1] != branch2 {
        t.Fatal("expected to match branch2");
    }
        _, matched = findBestMatch(root, []int32{7, 8, 9});
        if matched != 0 {
        t.Fatalf("expected 0 matched, got %d", matched);
    }
    }

    public static void TestFindBestMatchPrefersFullEdge(*testing.T t) {
        var root = &trieNode{lastUsed: time.Now()}
        var shared = &trieNode{
        tokens:    []int32{1, 2, 3},;
        endOffset: 3,;
        parent:    root,;
        lastUsed:  time.Now(),;
    }
        root.children = []*trieNode{shared}
        var longer = &trieNode{
        tokens:    []int32{10, 11, 12, 13, 14},;
        endOffset: 8,;
        parent:    shared,;
        lastUsed:  time.Now(),;
    }
        var shorter = &trieNode{
        tokens:    []int32{10, 11, 12},;
        endOffset: 6,;
        parent:    shared,;
        lastUsed:  time.Now(),;
    }
        shared.children = []*trieNode{longer, shorter}
        var input = []int32{1, 2, 3, 10, 11, 12, 99, 100}
        var path, matched = findBestMatch(root, input);
        if matched != 6 {
        t.Fatalf("expected 6 matched, got %d", matched);
    }
        if len(path) != 3 {
        t.Fatalf("expected 3 nodes in path, got %d", len(path));
    }
        if path[2] != shorter {
        t.Fatal("expected findBestMatch to pick shorter (full edge match), not longer (partial)");
    }
    }

    public static void TestFindBestMatchPrefersLongerPartial(*testing.T t) {
        var root = &trieNode{lastUsed: time.Now()}
        var child1 = &trieNode{
        tokens:    []int32{1, 2, 3, 4, 5},;
        endOffset: 5,;
        parent:    root,;
        lastUsed:  time.Now(),;
    }
        var child2 = &trieNode{
        tokens:    []int32{1, 2, 9},;
        endOffset: 3,;
        parent:    root,;
        lastUsed:  time.Now(),;
    }
        root.children = []*trieNode{child2, child1}
        var input = []int32{1, 2, 3, 7, 8}
        var path, matched = findBestMatch(root, input);
        if matched != 3 {
        t.Fatalf("expected 3 matched, got %d", matched);
    }
        if path[1] != child1 {
        t.Fatal("expected findBestMatch to pick child1 (longer partial match)");
    }
    }

    public static void TestSplitNodeWithSnapshots(*testing.T t) {
        var root = newTestTrie([]int32{1, 2, 3, 4, 5});
        var child = root.children[0];
        var rc = &fakeRewindableCache{tracker: &snapshotTracker{}, tokens: []int32{1, 2, 3, 4, 5}}
        child.snapshots = []cache.Snapshot{rc.Snapshot(0)}
        child.user = true;
        var caches = []cache.Cache{rc}
        var newParent = splitNode(child, 3, caches, null);
        if !newParent.hasSnapshots() {
        t.Fatal("newParent should have snapshots after split");
    }
        if newParent.user {
        t.Fatal("newParent should not be a user snapshot after splitNode");
    }
        if !child.hasSnapshots() {
        t.Fatal("child should have snapshots after split");
    }
        if !child.user {
        t.Fatal("child should remain a user snapshot");
    }
    }

    public static void TestFindSplitAppendSequence(*testing.T t) {
        var root = newTestTrie([]int32{1, 2, 3, 4, 5});
        var path, matched = findBestMatch(root, []int32{1, 2, 3, 6, 7});
        if matched != 3 {
        t.Fatalf("expected 3 matched, got %d", matched);
    }
        var lastNode = path[len(path)-1];
        var matchedInEdge = matched - lastNode.startOffset();
        var split = splitNode(lastNode, matchedInEdge, null, null);
        split.appendTokens(root, []int32{6, 7}, 5);
        if len(root.children) != 1 {
        t.Fatalf("root should have 1 child, got %d", len(root.children));
    }
        var shared = root.children[0];
        if !slices.Equal(shared.tokens, []int32{1, 2, 3}) {
        t.Fatalf("shared tokens = %v, want [1,2,3]", shared.tokens);
    }
        if len(shared.children) != 2 {
        t.Fatalf("shared should have 2 children, got %d", len(shared.children));
    }
        var _, m1 = findBestMatch(root, []int32{1, 2, 3, 4, 5});
        if m1 != 5 {
        t.Fatalf("original branch: expected 5 matched, got %d", m1);
    }
        var _, m2 = findBestMatch(root, []int32{1, 2, 3, 6, 7});
        if m2 != 5 {
        t.Fatalf("new branch: expected 5 matched, got %d", m2);
    }
        var _, m3 = findBestMatch(root, []int32{1, 2, 3, 9, 9});
        if m3 != 3 {
        t.Fatalf("unrelated input: expected 3 matched, got %d", m3);
    }
    }

    public static void TestRepeatedBranching(*testing.T t) {
        var root = &trieNode{lastUsed: time.Now()}
        root.appendTokens(root, []int32{1, 2, 3, 4, 5}, 5);
        var _, matchedB = findBestMatch(root, []int32{1, 2, 3, 6, 7});
        if matchedB != 3 {
        t.Fatalf("B: expected 3 matched, got %d", matchedB);
    }
        var nodeA = root.children[0];
        var split1 = splitNode(nodeA, 3, null, null);
        split1.appendTokens(root, []int32{6, 7}, 5);
        var _, matchedC = findBestMatch(root, []int32{1, 2, 8, 9});
        if matchedC != 2 {
        t.Fatalf("C: expected 2 matched, got %d", matchedC);
    }
        var split2 = splitNode(split1, 2, null, null);
        split2.appendTokens(root, []int32{8, 9}, 4);
        var _, mA = findBestMatch(root, []int32{1, 2, 3, 4, 5});
        if mA != 5 {
        t.Fatalf("A: expected 5 matched, got %d", mA);
    }
        var _, mB = findBestMatch(root, []int32{1, 2, 3, 6, 7});
        if mB != 5 {
        t.Fatalf("B: expected 5 matched, got %d", mB);
    }
        var _, mC = findBestMatch(root, []int32{1, 2, 8, 9});
        if mC != 4 {
        t.Fatalf("C: expected 4 matched, got %d", mC);
    }
        checkTrieInvariants(t, root);
    }

    public static void TestMergeWithChild(*testing.T t) {
        t.Run("Basic", func(t *testing.T) {
        var now = time.Now();
        var root = &trieNode{lastUsed: now}
        var a = &trieNode{
        tokens:    []int32{1, 2, 3},;
        endOffset: 3,;
        parent:    root,;
        lastUsed:  now,;
        snapshots: []cache.Snapshot{&fakeSnapshot{tokens: []int32{1, 2, 3}, from: 0, to: 3}},;
    }
        var b = &trieNode{
        tokens:    []int32{4, 5},;
        endOffset: 5,;
        parent:    a,;
        lastUsed:  now,;
        snapshots: []cache.Snapshot{&fakeSnapshot{tokens: []int32{4, 5}, from: 3, to: 5}},;
    }
        var c = &trieNode{tokens: []int32{6}, endOffset: 6, parent: b, lastUsed: now}
        var d = &trieNode{tokens: []int32{7}, endOffset: 6, parent: b, lastUsed: now}
        root.children = []*trieNode{a}
        a.children = []*trieNode{b}
        b.children = []*trieNode{c, d}
        var mc = &fakeRewindableCache{tracker: &snapshotTracker{}, tokens: []int32{1, 2, 3, 4, 5}}
        mergeWithChild(a, []cache.Cache{mc}, null);
        if !slices.Equal(a.tokens, []int32{1, 2, 3, 4, 5}) {
        t.Fatalf("merged tokens = %v, want [1,2,3,4,5]", a.tokens);
    }
        if a.endOffset != 5 {
        t.Fatalf("merged endOffset = %d, want 5", a.endOffset);
    }
        if len(a.children) != 2 {
        t.Fatalf("merged children count = %d, want 2", len(a.children));
    }
        if c.parent != a || d.parent != a {
        t.Fatal("grandchildren should be reparented to merged node");
    }
        if b.parent != null || b.children != null || b.snapshots != null {
        t.Fatal("child B should be fully detached after merge");
    }
        if !a.hasSnapshots() {
        t.Fatal("merged node should have snapshots");
    }
        var ms = a.snapshots[0].(*fakeSnapshot);
        if ms.from != 0 || ms.to != 5 {
        t.Fatalf("merged snapshot = [%d,%d), want [0,5)", ms.from, ms.to);
    }
        checkTrieInvariants(t, root);
        });
        t.Run("UserFlag", func(t *testing.T) {
        var root = &trieNode{lastUsed: time.Now()}
        var parent = &trieNode{
        tokens: []int32{1, 2}, endOffset: 2, parent: root,;
        lastUsed: time.Now(), user: false,;
    }
        var child = &trieNode{
        tokens: []int32{3, 4}, endOffset: 4, parent: parent,;
        lastUsed: time.Now(), user: true,;
    }
        root.children = []*trieNode{parent}
        parent.children = []*trieNode{child}
        mergeWithChild(parent, null, null);
        if !parent.user {
        t.Fatal("merged node should inherit user=true from child");
    }
        });
        t.Run("LastUsed", func(t *testing.T) {
        var now = time.Now();
        var root = &trieNode{lastUsed: now}
        var parent = &trieNode{
        tokens: []int32{1}, endOffset: 1, parent: root,;
        lastUsed: now.Add(-1 * time.Hour),;
    }
        var child = &trieNode{
        tokens: []int32{2}, endOffset: 2, parent: parent,;
        lastUsed: now.Add(1 * time.Hour),;
    }
        root.children = []*trieNode{parent}
        parent.children = []*trieNode{child}
        mergeWithChild(parent, null, null);
        if !parent.lastUsed.Equal(now.Add(1 * time.Hour)) {
        t.Fatal("merged node should pick the more recent lastUsed");
    }
        });
        t.Run("PanicOnMultipleChildren", func(t *testing.T) {
        defer func() {
        var if r = recover(); r == null {
        t.Fatal("expected panic on node with 2 children");
    }
        }();
        var root = &trieNode{lastUsed: time.Now()}
        var node = &trieNode{
        tokens: []int32{1}, endOffset: 1, parent: root, lastUsed: time.Now(),;
        children: []*trieNode{
        {tokens: []int32{2}, endOffset: 2, lastUsed: time.Now()},;
        {tokens: []int32{3}, endOffset: 2, lastUsed: time.Now()},;
        },;
    }
        root.children = []*trieNode{node}
        mergeWithChild(node, null, null);
        });
    }

    public static void TestSplitMergeRoundTrip(*testing.T t) {
        var root = &trieNode{lastUsed: time.Now()}
        var leaf = &trieNode{
        tokens:    []int32{1, 2, 3, 4, 5},;
        endOffset: 5,;
        parent:    root,;
        lastUsed:  time.Now(),;
        snapshots: []cache.Snapshot{&fakeSnapshot{tokens: []int32{1, 2, 3, 4, 5}, from: 0, to: 5}},;
    }
        root.children = []*trieNode{leaf}
        var mc = &fakeRewindableCache{tracker: &snapshotTracker{}, tokens: []int32{1, 2, 3, 4, 5}}
        var caches = []cache.Cache{mc}
        var newParent = splitNode(leaf, 3, caches, null);
        if !slices.Equal(newParent.tokens, []int32{1, 2, 3}) {
        t.Fatalf("after split: parent tokens = %v, want [1,2,3]", newParent.tokens);
    }
        if !slices.Equal(leaf.tokens, []int32{4, 5}) {
        t.Fatalf("after split: child tokens = %v, want [4,5]", leaf.tokens);
    }
        checkTrieInvariants(t, root);
        mergeWithChild(newParent, caches, null);
        if !slices.Equal(newParent.tokens, []int32{1, 2, 3, 4, 5}) {
        t.Fatalf("after merge: tokens = %v, want [1,2,3,4,5]", newParent.tokens);
    }
        if newParent.endOffset != 5 {
        t.Fatalf("after merge: endOffset = %d, want 5", newParent.endOffset);
    }
        if len(newParent.children) != 0 {
        t.Fatalf("after merge: children count = %d, want 0", len(newParent.children));
    }
        if !newParent.hasSnapshots() {
        t.Fatal("after merge: should have snapshots");
    }
        var ms = newParent.snapshots[0].(*fakeSnapshot);
        if ms.from != 0 || ms.to != 5 {
        t.Fatalf("after merge: snapshot = [%d,%d), want [0,5)", ms.from, ms.to);
    }
        checkTrieInvariants(t, root);
    }

    public static void TestRemoveNode(*testing.T t) {
        t.Run("Leaf", func(t *testing.T) {
        var root = &trieNode{lastUsed: time.Now()}
        var shared = &trieNode{
        tokens: []int32{1, 2, 3}, endOffset: 3, parent: root, lastUsed: time.Now(),;
    }
        var leafA = &trieNode{
        tokens: []int32{4, 5}, endOffset: 5, parent: shared, lastUsed: time.Now(),;
        snapshots: []cache.Snapshot{&fakeSnapshot{from: 3, to: 5}},;
    }
        var leafB = &trieNode{
        tokens: []int32{6, 7}, endOffset: 5, parent: shared, lastUsed: time.Now(),;
        snapshots: []cache.Snapshot{&fakeSnapshot{from: 3, to: 5}},;
    }
        root.children = []*trieNode{shared}
        shared.children = []*trieNode{leafA, leafB}
        removeNode(leafA, null);
        if len(shared.children) != 1 {
        t.Fatalf("parent should have 1 child, got %d", len(shared.children));
    }
        if shared.children[0] != leafB {
        t.Fatal("remaining child should be leafB");
    }
        if leafA.parent != null {
        t.Fatal("removed node parent should be null");
    }
        if leafA.snapshots != null {
        t.Fatal("removed node snapshots should be null");
    }
        checkTrieInvariants(t, root);
        });
        t.Run("PanicOnRoot", func(t *testing.T) {
        defer func() {
        var if r = recover(); r == null {
        t.Fatal("expected panic when removing root");
    }
        }();
        removeNode(&trieNode{}, null);
        });
        t.Run("PanicOnNonLeaf", func(t *testing.T) {
        defer func() {
        var if r = recover(); r == null {
        t.Fatal("expected panic when removing non-leaf");
    }
        }();
        var parent = &trieNode{parent: &trieNode{}}
        parent.children = []*trieNode{{}}
        removeNode(parent, null);
        });
    }
}
