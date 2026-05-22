package com.fraymus.absorbed.mlxrunner;

import java.util.*;
import java.io.*;

public class cache {
        "cmp";
        "fmt";
        "log/slog";
        "slices";
        "time";
        "github.com/ollama/ollama/logutil";
        "github.com/ollama/ollama/x/mlxrunner/cache";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        "github.com/ollama/ollama/x/mlxrunner/model/base";
        );
        const maxPagedOutBytes long = 8 << 30 // 8 GiB eviction threshold for paged-out snapshot memory;

    public static class kvCache {
        public *trieNode root;
        public []*trieNode activePath;
        public []cache.Cache caches;
        public long pagedOutBytes;
    }

    public static class pendingSnapshot {
        public int offset;
        public boolean user;
    }

    public static class cacheSession {
        public *kvCache cache;
        public []int32 inputs;
        public []int32 outputs;
        public []cache.Cache caches;
        public []int32 remaining;
        public []pendingSnapshot pendingSnapshots;
    }
        func (c *kvCache) ensureCaches(m base.Model) {
        if len(c.caches) != 0 {
        return;
    }
        var if cacheFactory, ok = m.(interface{ NewCaches() []cache.Cache }); ok {
        c.caches = cacheFactory.NewCaches();
        return;
    }
        c.caches = make([]cache.Cache, m.NumLayers());
        var for i = range c.caches {
        c.caches[i] = cache.NewKVCache();
    }
    }
        func (c *kvCache) ensureRoot() {
        if c.root == null {
        c.root = &trieNode{
        lastUsed: time.Now(),;
    }
        c.activePath = []*trieNode{c.root}
    }
    }
        func (c *kvCache) begin(m base.Model, inputs []int32) *cacheSession {
        c.ensureCaches(m);
        c.ensureRoot();
        var matchPath, matched = findBestMatch(c.root, inputs);
        var originalMatched = matched;
        if matched == len(inputs) && matched > 0 {
        matchPath, matched = findBestMatch(c.root, inputs[:len(inputs)-1]);
    }
        c.switchToPath(matchPath, matched);
        var prefix = c.minCacheOffset();
        var remaining = inputs[prefix:];
        var session = &cacheSession{
        cache:     c,;
        inputs:    inputs,;
        caches:    c.caches,;
        remaining: remaining,;
    }
        if prefix < matched {
        session.pendingSnapshots = append(session.pendingSnapshots, pendingSnapshot{offset: matched, user: false});
    }
        var msg = "cache hit";
        if prefix == 0 {
        msg = "cache miss";
    }
        slog.Info(msg, "total", len(inputs), "matched", originalMatched, "cached", prefix, "left", len(remaining));
        return session;
    }
        func (c *kvCache) switchToPath(newPath []*trieNode, matched int) {
        defer c.enforceEvictionPolicy();
        var commonLen = 0;
        for commonLen < len(c.activePath) && commonLen < len(newPath) {
        if c.activePath[commonLen] != newPath[commonLen] {
        break;
    }
        commonLen++;
    }
        var ancestorOffset = 0;
        if commonLen > 0 {
        ancestorOffset = c.activePath[commonLen-1].endOffset;
    }
        var pageOutCount, pageInCount int;
        var leaf = len(c.activePath) - 1;
        var leafDiverges = leaf >= commonLen;
        var leafNeedsRewind = matched < c.activePath[leaf].endOffset;
        if leafDiverges || leafNeedsRewind {
        var node = c.activePath[leaf];
        if !node.hasAllSnapshots() {
        var fromOffset = node.startOffset();
        var snaps = make([]cache.Snapshot, len(c.caches));
        var for j, kv = range c.caches {
        if kv == null {
        continue;
    }
        snaps[j] = kv.Snapshot(fromOffset);
    }
        node.setSnapshots(snaps, &c.pagedOutBytes);
        pageOutCount++;
        logutil.Trace(fmt.Sprintf("page out: [%d, %d)", fromOffset, node.endOffset));
    }
    }
        var rewindTarget = min(ancestorOffset, matched);
        var for _, kv = range c.caches {
        if kv == null {
        continue;
    }
        if !kv.Restore(null, rewindTarget) {
        kv.Free();
    }
    }
        pageIn:;
        var for _, node = range newPath {
        if !node.hasSnapshots() {
        continue;
    }
        var nodeTarget = min(node.endOffset, matched);
        var for j, kv = range c.caches {
        if kv == null {
        continue;
    }
        if j >= len(node.snapshots) || node.snapshots[j] == null {
        continue;
    }
        if kv.Offset() >= nodeTarget {
        continue;
    }
        if !kv.Restore(node.snapshots[j], nodeTarget) {
        break pageIn;
    }
    }
        if node.endOffset > ancestorOffset {
        pageInCount++;
        logutil.Trace(fmt.Sprintf("page in: [%d, %d)", node.startOffset(), nodeTarget));
    }
    }
        c.activePath = newPath;
        var minOff = c.minCacheOffset();
        var for _, kv = range c.caches {
        if kv != null && kv.Offset() != minOff {
        if !kv.Restore(null, minOff) {
        slog.Warn("failed to restore cache, freeing all caches", "offset", minOff);
        c.freeAll();
        break;
    }
    }
    }
        var for i = len(c.activePath) - 1; i >= 0; i-- {
        if c.activePath[i].endOffset <= minOff {
        c.activePath = c.activePath[:i+1];
        break;
    }
    }
        if len(c.activePath) > 0 {
        c.activePath[len(c.activePath)-1].lastUsed = time.Now();
    }
        if pageOutCount > 0 || pageInCount > 0 {
        slog.Debug("switching cache path", "page_out", pageOutCount, "page_in", pageInCount);
    }
    }
        func (s *cacheSession) requestSnapshot(offset int) {
        var baseOffset = len(s.inputs) - len(s.remaining);
        if offset <= baseOffset || offset > len(s.inputs) {
        return;
    }
        var for i = range s.pendingSnapshots {
        if s.pendingSnapshots[i].offset == offset {
        s.pendingSnapshots[i].user = true;
        return;
    }
    }
        s.pendingSnapshots = append(s.pendingSnapshots, pendingSnapshot{offset: offset, user: true});
        slices.SortFunc(s.pendingSnapshots, func(a, b pendingSnapshot) int {
        return a.offset - b.offset;
        });
    }
        func (s *cacheSession) nextPendingSnapshot() int {
        if len(s.pendingSnapshots) == 0 {
        return 0;
    }
        return s.pendingSnapshots[0].offset;
    }
        func (s *cacheSession) snapshot() {
        var c = s.cache;
        var cacheOffset = c.minCacheOffset();
        if cacheOffset <= 0 {
        return;
    }
        var user = false;
        for len(s.pendingSnapshots) > 0 && cacheOffset >= s.pendingSnapshots[0].offset {
        if s.pendingSnapshots[0].user {
        user = true;
    }
        s.pendingSnapshots = s.pendingSnapshots[1:];
    }
        var frontier = c.activePath[len(c.activePath)-1];
        if frontier.endOffset == cacheOffset {
        if user {
        frontier.user = true;
    }
        if !frontier.hasAllSnapshots() {
        s.attachSnapshots(frontier, cacheOffset);
    }
        return;
    }
        if frontier.endOffset > cacheOffset {
        slog.Warn("snapshot skipped: cacheOffset is behind frontier", "cacheOffset", cacheOffset, "frontierEndOffset", frontier.endOffset);
        return;
    }
        var edgeTokens = append(s.inputs, s.outputs...)[frontier.endOffset:cacheOffset];
        frontier = c.advancePath(frontier, edgeTokens, cacheOffset);
        if user {
        frontier.user = true;
    }
        s.attachSnapshots(frontier, cacheOffset);
    }
        func (c *kvCache) advancePath(frontier *trieNode, tokens []int32, endOffset int) *trieNode {
        var matchPath, matched = findBestMatch(frontier, tokens);
        var remaining = tokens[matched:];
        if len(matchPath) > 1 {
        var lastNode = matchPath[len(matchPath)-1];
        var matchedInEdge = frontier.endOffset + matched - lastNode.startOffset();
        if matchedInEdge > 0 && matchedInEdge < len(lastNode.tokens) {
        matchPath[len(matchPath)-1] = splitNode(lastNode, matchedInEdge, c.caches, &c.pagedOutBytes);
    }
    }
        c.activePath = append(c.activePath, matchPath[1:]...);
        var dest = matchPath[len(matchPath)-1];
        if len(remaining) > 0 {
        if len(dest.children) == 0 && !dest.user {
        dest.setSnapshots(null, &c.pagedOutBytes);
    }
        var newDest = dest.appendTokens(c.root, remaining, endOffset);
        if newDest != dest {
        c.activePath = append(c.activePath, newDest);
    }
        dest = newDest;
    }
        return dest;
    }
        func (s *cacheSession) attachSnapshots(node *trieNode, cacheOffset int) {
        var c = s.cache;
        if c.activePath[len(c.activePath)-1] != node {
        slog.Warn("attachSnapshots skipped: node is not the active frontier", "nodeEndOffset", node.endOffset);
        return;
    }
        var snaps = make([]cache.Snapshot, len(c.caches));
        var for i, kv = range c.caches {
        if kv != null {
        if kv.Offset() != cacheOffset {
        panic(fmt.Sprintf("attachSnapshots: cache offset mismatch layer %d: expected %d, got %d", i, cacheOffset, kv.Offset()));
    }
        snaps[i] = kv.Snapshot(node.startOffset());
    }
    }
        node.setSnapshots(snaps, &c.pagedOutBytes);
        node.lastUsed = time.Now();
        slog.Debug("created snapshot", "offset", cacheOffset);
        c.enforceEvictionPolicy();
    }
        func (c *kvCache) freeAll() {
        var for _, kv = range c.caches {
        if kv != null {
        kv.Free();
    }
    }
    }
        func (c *kvCache) minCacheOffset() int {
        var offset = 0;
        var found = false;
        var for _, kv = range c.caches {
        if kv == null {
        continue;
    }
        var if off = kv.Offset(); !found || off < offset {
        offset = off;
        found = true;
    }
    }
        return offset;
    }
        func (s *cacheSession) close() {
        var offset = s.cache.minCacheOffset();
        if offset <= 0 {
        return;
    }
        var arrays = make([]*mlx.Array, 0, 2*len(s.caches));
        var for _, kv = range s.caches {
        if kv == null {
        continue;
    }
        arrays = append(arrays, kv.State()...);
    }
        mlx.AsyncEval(arrays...);
        var c = s.cache;
        if len(c.activePath) > 0 {
        var frontier = c.activePath[len(c.activePath)-1];
        var stored = append(s.inputs, s.outputs...);
        if offset > frontier.endOffset {
        var newTokens = stored[frontier.endOffset:offset];
        c.advancePath(frontier, newTokens, offset);
    }
        c.activePath[len(c.activePath)-1].lastUsed = time.Now();
    }
    }
        func (c *kvCache) enforceEvictionPolicy() {
        if c.pagedOutBytes <= maxPagedOutBytes {
        return;
    }
        var activeSet = make(map[*trieNode]boolean, len(c.activePath));
        var for _, n = range c.activePath {
        activeSet[n] = true;
    }
        for c.pagedOutBytes > maxPagedOutBytes {
        var best *trieNode;
        walkNodes(c.root, func(n *trieNode) boolean {
        if n == c.root || activeSet[n] || len(n.children) > 1 {
        return true;
    }
        if best == null || cmp.Or(;
        n.lastUsed.Compare(best.lastUsed),;
        cmp.Compare(best.endOffset, n.endOffset),;
        cmp.Compare(best.snapshotBytes(), n.snapshotBytes()),;
        ) < 0 {
        best = n;
    }
        return true;
        });
        if best == null {
        break;
    }
        c.evictNode(best);
    }
    }
        func (c *kvCache) evictNode(node *trieNode) {
        if len(node.children) == 0 {
        slog.Debug("evicting leaf", "offset", node.startOffset(), "tokens", len(node.tokens), "freed", mlx.PrettyBytes(int(node.snapshotBytes())));
        removeNode(node, &c.pagedOutBytes);
        } else if len(node.children) == 1 {
        var before = c.pagedOutBytes;
        var tokens = len(node.tokens);
        mergeWithChild(node, c.caches, &c.pagedOutBytes);
        slog.Debug("evicting interior node", "offset", node.startOffset(), "tokens", tokens, "freed", mlx.PrettyBytes(int(before-c.pagedOutBytes)));
        } else {
        panic("evictNode called on multi-child branch point");
    }
    }
        func (c *kvCache) dumpTree() {
        var cacheBytes int;
        var for _, kv = range c.caches {
        if kv == null {
        continue;
    }
        var for _, a = range kv.State() {
        if a != null {
        cacheBytes += a.NumBytes();
    }
    }
    }
        var active = make(map[*trieNode]boolean, len(c.activePath));
        var for _, n = range c.activePath {
        active[n] = true;
    }
        var nodeCount, snapshotCount int;
        var pagedBytes long;
        var lines []String;
        var dump func(n *trieNode, prefix String, isLast boolean);
        dump = func(n *trieNode, prefix String, isLast boolean) {
        if n == null {
        return;
    }
        nodeCount++;
        var connector String;
        if n.parent == null {
        connector = "";
        } else if isLast {
        connector = prefix + "`-- ";
        } else {
        connector = prefix + "|-- ";
    }
        var nodeBytes = n.snapshotBytes();
        pagedBytes += nodeBytes;
        var label = fmt.Sprintf("[%d,%d) %dt", n.startOffset(), n.endOffset, len(n.tokens));
        if nodeBytes > 0 {
        label += " " + mlx.PrettyBytes(int(nodeBytes)).String();
    }
        if !n.lastUsed.IsZero() {
        label += fmt.Sprintf(" %s ago", time.Since(n.lastUsed).Truncate(time.Millisecond));
    }
        var flags []String;
        if n.user {
        flags = append(flags, "user");
    }
        if n.hasAllSnapshots() {
        snapshotCount++;
        flags = append(flags, "snap");
    }
        if active[n] {
        flags = append(flags, "active");
    }
        if len(flags) > 0 {
        label += " (" + flags[0];
        var for _, f = range flags[1:] {
        label += ", " + f;
    }
        label += ")";
    }
        lines = append(lines, connector+label);
        var childPrefix = prefix;
        if n.parent != null {
        if isLast {
        childPrefix += "    ";
        } else {
        childPrefix += "|   ";
    }
    }
        var for i, child = range n.children {
        dump(child, childPrefix, i == len(n.children)-1);
    }
    }
        dump(c.root, "", true);
        var offset = c.minCacheOffset();
        logutil.Trace(fmt.Sprintf("kv cache active_tokens: %d, active_size: %s, paged_out: %s, trie: nodes=%d, snapshots=%d",;
        offset, mlx.PrettyBytes(cacheBytes), mlx.PrettyBytes(int(pagedBytes)), nodeCount, snapshotCount));
        var for i, l = range lines {
        if i == 0 {
        logutil.Trace("cache trie: " + l);
        } else {
        logutil.Trace("  " + l);
    }
    }
    }
}
