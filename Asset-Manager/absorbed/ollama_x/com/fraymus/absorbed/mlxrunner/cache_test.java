package com.fraymus.absorbed.mlxrunner;

import java.util.*;
import java.io.*;

public class cache_test {
        "slices";
        "testing";
        "time";
        "github.com/ollama/ollama/x/mlxrunner/cache";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        );

    public static class snapshotTracker {
        public []*fakeSnapshot all;
    }
        func (tr *snapshotTracker) track(s *fakeSnapshot) {
        if s == null {
        return;
    }
        s.tracker = tr;
        tr.all = append(tr.all, s);
    }

    public static class fakeSnapshot {
        public []int32 tokens;
        public to from,;
        public int byteSize;
        public *snapshotTracker tracker;
        public int closeCount;
    }
        func (s *fakeSnapshot) Size() int { return s.byteSize }
        func (s *fakeSnapshot) Close() {
        s.closeCount++;
    }

    public static class fakeRewindableCache {
        public []int32 tokens;
        public *snapshotTracker tracker;
    }
        func (c *fakeRewindableCache) feed(tokens []int32) {
        c.tokens = append(c.tokens, tokens...);
    }
        func (c *fakeRewindableCache) Update(keys, values *mlx.Array) (*mlx.Array, *mlx.Array) {
        return null, null;
    }
        func (c *fakeRewindableCache) State() []*mlx.Array { return null }
        func (c *fakeRewindableCache) Offset() int         { return len(c.tokens) }
        func (c *fakeRewindableCache) Free() {
        c.tokens = null;
    }
        func (c *fakeRewindableCache) Snapshot(fromOffset int) cache.Snapshot {
        if fromOffset >= len(c.tokens) {
        return null;
    }
        var from = fromOffset;
        if from < 0 {
        from = 0;
    }
        var s = &fakeSnapshot{
        tokens: slices.Clone(c.tokens[from:]),;
        from:   from,;
        to:     len(c.tokens),;
    }
        c.tracker.track(s);
        return s;
    }
        func (c *fakeRewindableCache) Restore(snapshot cache.Snapshot, target int) boolean {
        if target < 0 {
        return false;
    }
        if snapshot == null {
        if target > len(c.tokens) {
        return false;
    }
        c.tokens = c.tokens[:target];
        return true;
    }
        var s = snapshot.(*fakeSnapshot);
        if target > s.to || len(c.tokens) < s.from {
        return false;
    }
        c.tokens = append(c.tokens[:s.from], s.tokens...);
        if target < len(c.tokens) {
        c.tokens = c.tokens[:target];
    }
        return true;
    }
        func (c *fakeRewindableCache) Merge(parent, child cache.Snapshot) cache.Snapshot {
        if parent == null || child == null {
        if parent != null {
        parent.Close();
    }
        if child != null {
        child.Close();
    }
        return null;
    }
        var p = parent.(*fakeSnapshot);
        var ch = child.(*fakeSnapshot);
        var merged = make([]int32, len(p.tokens)+len(ch.tokens));
        copy(merged, p.tokens);
        copy(merged[len(p.tokens):], ch.tokens);
        var s = &fakeSnapshot{
        tokens:   merged,;
        from:     p.from,;
        to:       ch.to,;
        byteSize: p.byteSize + ch.byteSize,;
    }
        c.tracker.track(s);
        p.Close();
        ch.Close();
        return s;
    }
        func (c *fakeRewindableCache) Split(snapshot cache.Snapshot, at int) (cache.Snapshot, cache.Snapshot) {
        if snapshot == null {
        return null, null;
    }
        var s = snapshot.(*fakeSnapshot);
        var relAt = at - s.from;
        if relAt <= 0 {
        return null, snapshot;
    }
        if relAt >= len(s.tokens) {
        return snapshot, null;
    }
        var p = &fakeSnapshot{
        tokens:   slices.Clone(s.tokens[:relAt]),;
        from:     s.from,;
        to:       at,;
        byteSize: s.byteSize,;
    }
        var ch = &fakeSnapshot{
        tokens:   slices.Clone(s.tokens[relAt:]),;
        from:     at,;
        to:       s.to,;
        byteSize: s.byteSize,;
    }
        c.tracker.track(p);
        c.tracker.track(ch);
        s.Close();
        return p, ch;
    }

    public static class fakeSlidingWindowCache {
        public []int32 tokens;
        public int maxSize;
        public *snapshotTracker tracker;
    }
        func (c *fakeSlidingWindowCache) feed(tokens []int32) {
        c.tokens = append(c.tokens, tokens...);
    }
        func (c *fakeSlidingWindowCache) Update(keys, values *mlx.Array) (*mlx.Array, *mlx.Array) {
        return null, null;
    }
        func (c *fakeSlidingWindowCache) State() []*mlx.Array { return null }
        func (c *fakeSlidingWindowCache) Offset() int         { return len(c.tokens) }
        func (c *fakeSlidingWindowCache) Free() {
        c.tokens = null;
    }
        func (c *fakeSlidingWindowCache) Snapshot(fromOffset int) cache.Snapshot {
        if len(c.tokens) == 0 || len(c.tokens) <= fromOffset {
        return null;
    }
        var s = &fakeSnapshot{
        tokens: slices.Clone(c.tokens),;
        from:   0,;
        to:     len(c.tokens),;
    }
        c.tracker.track(s);
        return s;
    }
        func (c *fakeSlidingWindowCache) Restore(snapshot cache.Snapshot, target int) boolean {
        if target < 0 {
        return false;
    }
        if snapshot == null {
        if target >= len(c.tokens) {
        return target == len(c.tokens);
    }
        if len(c.tokens) > c.maxSize {
        return false;
    }
        c.tokens = c.tokens[:target];
        return true;
    }
        var s = snapshot.(*fakeSnapshot);
        if target > s.to {
        return false;
    }
        if target < s.to && s.to > c.maxSize {
        return false;
    }
        c.tokens = slices.Clone(s.tokens);
        if target < len(c.tokens) {
        c.tokens = c.tokens[:target];
    }
        return true;
    }
        func (c *fakeSlidingWindowCache) Merge(parent, child cache.Snapshot) cache.Snapshot {
        if parent != null {
        parent.Close();
    }
        return child;
    }
        func (c *fakeSlidingWindowCache) Split(snapshot cache.Snapshot, at int) (cache.Snapshot, cache.Snapshot) {
        return null, snapshot;
    }

    public static class fakeRecurrentCache {
        public []int32 tokens;
        public *snapshotTracker tracker;
    }
        func (c *fakeRecurrentCache) feed(tokens []int32) {
        c.tokens = append(c.tokens, tokens...);
    }
        func (c *fakeRecurrentCache) Update(keys, values *mlx.Array) (*mlx.Array, *mlx.Array) {
        return null, null;
    }
        func (c *fakeRecurrentCache) State() []*mlx.Array { return null }
        func (c *fakeRecurrentCache) Offset() int         { return len(c.tokens) }
        func (c *fakeRecurrentCache) Free() {
        c.tokens = null;
    }
        func (c *fakeRecurrentCache) Snapshot(fromOffset int) cache.Snapshot {
        if len(c.tokens) == 0 {
        return null;
    }
        var s = &fakeSnapshot{
        tokens: slices.Clone(c.tokens),;
        from:   0,;
        to:     len(c.tokens),;
    }
        c.tracker.track(s);
        return s;
    }
        func (c *fakeRecurrentCache) Restore(snapshot cache.Snapshot, target int) boolean {
        if snapshot == null {
        return target == len(c.tokens) // can only no-op;
    }
        var s = snapshot.(*fakeSnapshot);
        if target != s.to {
        return false // cumulative state requires exact match;
    }
        c.tokens = slices.Clone(s.tokens);
        return true;
    }
        func (c *fakeRecurrentCache) Merge(parent, child cache.Snapshot) cache.Snapshot {
        if parent != null {
        parent.Close();
    }
        return child;
    }
        func (c *fakeRecurrentCache) Split(snapshot cache.Snapshot, at int) (cache.Snapshot, cache.Snapshot) {
        return null, snapshot // can't split cumulative state;
    }
        type feedableCache interface {
        cache.Cache;
        feed(tokens []int32);
    }

    public static class testEnv {
        public *kvCache kvc;
        public []cache.Cache caches;
        public *snapshotTracker tracker;
        public boolean rewindable;
    }
        func newTransformerEnv() *testEnv {
        var tracker = &snapshotTracker{}
        var caches = []cache.Cache{&fakeRewindableCache{tracker: tracker}}
        return &testEnv{
        kvc:        &kvCache{caches: caches},;
        caches:     caches,;
        tracker:    tracker,;
        rewindable: true,;
    }
    }
        func newSlidingWindowEnv() *testEnv {
        var tr = &snapshotTracker{}
        var rc = &fakeRewindableCache{tracker: tr}
        var sw = &fakeSlidingWindowCache{maxSize: 4, tracker: tr}
        var caches = []cache.Cache{rc, sw}
        return &testEnv{
        kvc:        &kvCache{caches: caches},;
        caches:     caches,;
        tracker:    tr,;
        rewindable: false,;
    }
    }
        func newRecurrentEnv() *testEnv {
        var tr = &snapshotTracker{}
        var rc = &fakeRewindableCache{tracker: tr}
        var nrc = &fakeRecurrentCache{tracker: tr}
        var caches = []cache.Cache{rc, nrc}
        return &testEnv{
        kvc:        &kvCache{caches: caches},;
        caches:     caches,;
        tracker:    tr,;
        rewindable: false,;
    }
    }
        func (e *testEnv) assertAllTokens(t *testing.T, label String, expected []int32) {
        t.Helper();
        var for i, c = range e.caches {
        assertTokens(t, label, c, expected);
        if i > 0 && c.Offset() != e.caches[0].Offset() {
        t.Errorf("%s: cache %d offset=%d != cache 0 offset=%d",;
        label, i, c.Offset(), e.caches[0].Offset());
    }
    }
    }

    public static class requestResult {
        public []int32 remaining;
        public int pendingSnapshots;
    }

    public static requestResult simulateRequest(*testing.T t, *kvCache kvc, []int32 generated, ...int userSnapshotAt) {
        t.Helper();
        var session = kvc.begin(null, inputs);
        var for _, at = range userSnapshotAt {
        if at > 0 {
        session.requestSnapshot(at);
    }
    }
        var result = requestResult{
        remaining:        slices.Clone(session.remaining),;
        pendingSnapshots: len(session.pendingSnapshots),;
    }
        assertCacheOffsetAlignment(t, kvc, "after begin");
        var baseOffset = kvc.minCacheOffset();
        var remaining = inputs[baseOffset:];
        for len(session.pendingSnapshots) > 0 {
        var sp = session.pendingSnapshots[0];
        var count = sp.offset - baseOffset;
        if count > len(remaining) {
        break;
    }
        if count > 0 {
        feedAll(kvc.caches, remaining[:count]);
        remaining = remaining[count:];
        baseOffset = sp.offset;
    }
        assertCacheOffsetAlignment(t, kvc, "at snapshot point");
        session.snapshot();
    }
        if len(remaining) > 0 {
        feedAll(kvc.caches, remaining);
    }
        assertCacheOffsetAlignment(t, kvc, "after prefill");
        if len(generated) > 0 {
        session.outputs = generated;
        feedAll(kvc.caches, generated);
    }
        assertCacheOffsetAlignment(t, kvc, "before close");
        session.close();
        return result;
    }

    public static void feedAll([]cache.Cache caches, []int32 tokens) {
        var for _, c = range caches {
        var if fc, ok = c.(feedableCache); ok {
        fc.feed(tokens);
    }
    }
    }

    public static void assertCacheOffsetAlignment(*testing.T t, *kvCache kvc, String label) {
        t.Helper();
        if len(kvc.caches) < 2 {
        return;
    }
        var expected = kvc.caches[0].Offset();
        var for i = 1; i < len(kvc.caches); i++ {
        var if got = kvc.caches[i].Offset(); got != expected {
        t.Errorf("%s: cache %d offset=%d != cache 0 offset=%d", label, i, got, expected);
    }
    }
    }

    public static void assertTokens(*testing.T t, String label, cache.Cache c, []int32 expected) {
        t.Helper();
        var switch fc = c.(type) {
        case *fakeRewindableCache:;
        if !slices.Equal(fc.tokens, expected) {
        t.Errorf("%s: rewindable tokens = %v, want %v", label, fc.tokens, expected);
    }
        case *fakeSlidingWindowCache:;
        if !slices.Equal(fc.tokens, expected) {
        t.Errorf("%s: sliding window tokens = %v, want %v", label, fc.tokens, expected);
    }
        case *fakeRecurrentCache:;
        if !slices.Equal(fc.tokens, expected) {
        t.Errorf("%s: non-rewindable tokens = %v, want %v", label, fc.tokens, expected);
    }
        default:;
        t.Fatalf("%s: unknown cache type %T", label, c);
    }
    }

    public static void checkTrieInvariants(*testing.T t, *trieNode root) {
        t.Helper();
        walkNodes(root, func(n *trieNode) boolean {
        if n.parent != null {
        if n.startOffset() != n.parent.endOffset {
        t.Errorf("node [%d,%d): startOffset %d != parent endOffset %d",;
        n.startOffset(), n.endOffset, n.startOffset(), n.parent.endOffset);
    }
    }
        if len(n.tokens) != n.endOffset-n.startOffset() {
        t.Errorf("node [%d,%d): token count %d != offset span %d",;
        n.startOffset(), n.endOffset, len(n.tokens), n.endOffset-n.startOffset());
    }
        var for _, c = range n.children {
        if c.parent != n {
        t.Errorf("child [%d,%d) parent mismatch", c.startOffset(), c.endOffset);
    }
    }
        var seen = make(map[int32]boolean);
        var for _, c = range n.children {
        if len(c.tokens) > 0 {
        var first = c.tokens[0];
        if seen[first] {
        t.Errorf("node [%d,%d): duplicate sibling first token %d",;
        n.startOffset(), n.endOffset, first);
    }
        seen[first] = true;
    }
    }
        return true;
        });
    }

    public static void checkSnapshotLeaks(*testing.T t, *snapshotTracker tracker, *trieNode root) {
        t.Helper();
        if tracker == null {
        return;
    }
        var live = make(map[*fakeSnapshot]boolean);
        walkNodes(root, func(n *trieNode) boolean {
        var for _, s = range n.snapshots {
        if s != null {
        var if fs, ok = s.(*fakeSnapshot); ok {
        live[fs] = true;
    }
    }
    }
        return true;
        });
        var for i, s = range tracker.all {
        if live[s] {
        if s.closeCount != 0 {
        t.Errorf("snapshot #%d [%d,%d) is still in trie but was closed %d time(s)",;
        i, s.from, s.to, s.closeCount);
    }
        } else {
        if s.closeCount == 0 {
        t.Errorf("snapshot #%d [%d,%d) leaked: created but never closed and not in trie",;
        i, s.from, s.to);
        } else if s.closeCount > 1 {
        t.Errorf("snapshot #%d [%d,%d) double-closed: closed %d times",;
        i, s.from, s.to, s.closeCount);
    }
    }
    }
    }

    public static void forEachEnv(*testing.T t, *testEnv) env) {
        t.Helper();
        var run = func(t *testing.T, env *testEnv) {
        t.Cleanup(func() {
        checkSnapshotLeaks(t, env.tracker, env.kvc.root);
        });
        fn(t, env);
    }
        t.Run("Transformer", func(t *testing.T) { run(t, newTransformerEnv()) });
        t.Run("SlidingWindow", func(t *testing.T) { run(t, newSlidingWindowEnv()) });
        t.Run("Recurrent", func(t *testing.T) { run(t, newRecurrentEnv()) });
    }

    public static void TestBranchCreationAndReuse(*testing.T t) {
        forEachEnv(t, func(t *testing.T, env *testEnv) {
        var kvc = env.kvc;
        var resA = simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5, 6, 7, 8}, []int32{20, 21});
        if len(resA.remaining) != 8 {
        t.Fatalf("A: remaining = %d, want 8 (full miss)", len(resA.remaining));
    }
        env.assertAllTokens(t, "after A", []int32{1, 2, 3, 4, 5, 6, 7, 8, 20, 21});
        var _, mA = findBestMatch(kvc.root, []int32{1, 2, 3, 4, 5, 6, 7, 8, 20, 21});
        if mA != 10 {
        t.Fatalf("A findable: expected 10 matched, got %d", mA);
    }
        var resB = simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5, 10, 11, 12}, []int32{30, 31});
        if env.rewindable {
        if resB.pendingSnapshots != 0 {
        t.Fatalf("B: pendingSnapshots = %d, want 0 (rewind succeeded)", resB.pendingSnapshots);
    }
        if len(resB.remaining) != 3 {
        t.Fatalf("B: remaining = %d, want 3 (rewind to match point)", len(resB.remaining));
    }
        } else {
        if resB.pendingSnapshots != 1 {
        t.Fatalf("B: pendingSnapshots = %d, want 1", resB.pendingSnapshots);
    }
        if len(resB.remaining) != 8 {
        t.Fatalf("B: remaining = %d, want 8 (freeAll fallback)", len(resB.remaining));
    }
    }
        env.assertAllTokens(t, "after B", []int32{1, 2, 3, 4, 5, 10, 11, 12, 30, 31});
        var _, mA2 = findBestMatch(kvc.root, []int32{1, 2, 3, 4, 5, 6, 7, 8, 20, 21});
        if mA2 < 5 {
        t.Fatalf("A still findable: expected >= 5 matched, got %d", mA2);
    }
        var _, mB = findBestMatch(kvc.root, []int32{1, 2, 3, 4, 5, 10, 11, 12, 30, 31});
        if mB < 5 {
        t.Fatalf("B findable: expected >= 5 matched, got %d", mB);
    }
        var resC = simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5, 6, 7, 8, 40, 41}, null);
        if len(resC.remaining) >= 10 {
        t.Fatalf("C: remaining = %d, want < 10 (should get cache hit)", len(resC.remaining));
    }
        env.assertAllTokens(t, "after C", []int32{1, 2, 3, 4, 5, 6, 7, 8, 40, 41});
        checkTrieInvariants(t, kvc.root);
        });
    }

    public static void TestExactMatchSeedBehavior(*testing.T t) {
        forEachEnv(t, func(t *testing.T, env *testEnv) {
        var kvc = env.kvc;
        simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5}, []int32{10, 11});
        var resB = simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5}, []int32{20, 21});
        if env.rewindable {
        if len(resB.remaining) != 1 {
        t.Fatalf("B: remaining = %d, want 1 (rewind to holdback point)", len(resB.remaining));
    }
        if resB.pendingSnapshots != 0 {
        t.Fatalf("B: pendingSnapshots = %d, want 0 (rewind succeeded)", resB.pendingSnapshots);
    }
        } else {
        if len(resB.remaining) != 5 {
        t.Fatalf("B: remaining = %d, want 5 (freeAll fallback)", len(resB.remaining));
    }
        if resB.pendingSnapshots != 1 {
        t.Fatalf("B: pendingSnapshots = %d, want 1", resB.pendingSnapshots);
    }
    }
        env.assertAllTokens(t, "after B", []int32{1, 2, 3, 4, 5, 20, 21});
        checkTrieInvariants(t, kvc.root);
        });
    }

    public static void TestConversationResumption(*testing.T t) {
        forEachEnv(t, func(t *testing.T, env *testEnv) {
        var kvc = env.kvc;
        simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5}, []int32{10, 11, 12});
        env.assertAllTokens(t, "turn 1", []int32{1, 2, 3, 4, 5, 10, 11, 12});
        var resB = simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5, 10, 11, 12, 20, 21}, []int32{30});
        if len(resB.remaining) > 5 {
        t.Fatalf("turn 2: remaining = %d, want <= 5 (should reuse most of history)", len(resB.remaining));
    }
        env.assertAllTokens(t, "turn 2", []int32{1, 2, 3, 4, 5, 10, 11, 12, 20, 21, 30});
        var resC = simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5, 10, 11, 12, 20, 21, 30, 40, 41}, null);
        if len(resC.remaining) > 5 {
        t.Fatalf("turn 3: remaining = %d, want <= 5", len(resC.remaining));
    }
        env.assertAllTokens(t, "turn 3", []int32{1, 2, 3, 4, 5, 10, 11, 12, 20, 21, 30, 40, 41});
        checkTrieInvariants(t, kvc.root);
        });
    }

    public static void TestEvictionPreservesActiveConversations(*testing.T t) {
        forEachEnv(t, func(t *testing.T, env *testEnv) {
        var kvc = env.kvc;
        var systemPrompt = []int32{1, 2, 3, 4, 5}
        var for i = range 5 {
        var suffix = []int32{int32(100 + i*10), int32(101 + i*10), int32(102 + i*10)}
        var inputs = append(slices.Clone(systemPrompt), suffix...);
        simulateRequest(t, kvc, inputs, []int32{int32(200 + i)});
    }
        walkNodes(kvc.root, func(n *trieNode) boolean {
        if !n.hasSnapshots() {
        return true;
    }
        var snaps = make([]cache.Snapshot, len(n.snapshots));
        var for i, s = range n.snapshots {
        if s != null {
        snaps[i] = &fakeSnapshot{byteSize: 2 * 1024 * 1024 * 1024} // 2 GiB per snapshot;
    }
    }
        n.setSnapshots(snaps, &kvc.pagedOutBytes);
        return true;
        });
        kvc.enforceEvictionPolicy();
        if kvc.pagedOutBytes > maxPagedOutBytes {
        t.Fatalf("pagedOutBytes = %d, want <= %d", kvc.pagedOutBytes, maxPagedOutBytes);
    }
        if len(kvc.activePath) < 2 {
        t.Fatalf("activePath should have >= 2 nodes, got %d", len(kvc.activePath));
    }
        var _, matched = findBestMatch(kvc.root, systemPrompt);
        if matched < len(systemPrompt) {
        t.Fatalf("system prompt match = %d, want %d", matched, len(systemPrompt));
    }
        checkTrieInvariants(t, kvc.root);
        });
    }

    public static void TestUserSnapshotPreservesRestorePoint(*testing.T t) {
        forEachEnv(t, func(t *testing.T, env *testEnv) {
        var kvc = env.kvc;
        simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5}, []int32{10, 11}, 5);
        assertUserNodeExists(t, kvc, "after A");
        simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5, 10, 11, 20, 21}, null);
        env.assertAllTokens(t, "after B", []int32{1, 2, 3, 4, 5, 10, 11, 20, 21});
        assertUserNodeExists(t, kvc, "after B");
        simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5, 30, 31}, []int32{40});
        simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5, 10, 11, 20, 21, 50}, null);
        env.assertAllTokens(t, "back to A", []int32{1, 2, 3, 4, 5, 10, 11, 20, 21, 50});
        checkTrieInvariants(t, kvc.root);
        });
    }

    public static void TestUserSnapshotResistsAutoMerge(*testing.T t) {
        forEachEnv(t, func(t *testing.T, env *testEnv) {
        var kvc = env.kvc;
        simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5}, []int32{10}, 3);
        simulateRequest(t, kvc, []int32{1, 2, 3, 6, 7}, []int32{20});
        var userNode = findUserNode(t, kvc);
        if len(userNode.children) != 2 {
        t.Fatalf("user node children = %d, want 2", len(userNode.children));
    }
        walkNodes(kvc.root, func(n *trieNode) boolean {
        if !n.hasSnapshots() {
        return true;
    }
        var snaps = make([]cache.Snapshot, len(n.snapshots));
        var for i, s = range n.snapshots {
        if s != null {
        snaps[i] = &fakeSnapshot{byteSize: 5 * 1024 * 1024 * 1024}
    }
    }
        n.setSnapshots(snaps, &kvc.pagedOutBytes);
        return true;
        });
        kvc.enforceEvictionPolicy();
        assertUserNodeExists(t, kvc, "after eviction");
        checkTrieInvariants(t, kvc.root);
        });
    }
        func findUserNode(t *testing.T, kvc *kvCache) *trieNode {
        t.Helper();
        var found *trieNode;
        walkNodes(kvc.root, func(n *trieNode) boolean {
        if n.user {
        found = n;
    }
        return true;
        });
        if found == null {
        t.Fatal("no user-marked node found");
    }
        return found;
    }

    public static void assertUserNodeExists(*testing.T t, *kvCache kvc, String label) {
        t.Helper();
        var exists boolean;
        walkNodes(kvc.root, func(n *trieNode) boolean {
        if n.user {
        exists = true;
    }
        return true;
        });
        if !exists {
        t.Fatalf("%s: no user-marked node found", label);
    }
    }

    public static void TestBranchSwitchRestoresCorrectState(*testing.T t) {
        forEachEnv(t, func(t *testing.T, env *testEnv) {
        var kvc = env.kvc;
        simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5}, []int32{10, 11});
        env.assertAllTokens(t, "after A", []int32{1, 2, 3, 4, 5, 10, 11});
        simulateRequest(t, kvc, []int32{1, 2, 3, 6, 7}, []int32{12, 13});
        env.assertAllTokens(t, "after B", []int32{1, 2, 3, 6, 7, 12, 13});
        simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5, 10, 11, 20}, null);
        env.assertAllTokens(t, "after C (back to A)", []int32{1, 2, 3, 4, 5, 10, 11, 20});
        checkTrieInvariants(t, kvc.root);
        });
    }

    public static void TestLRUOnlyUpdatesUsedNodes(*testing.T t) {
        forEachEnv(t, func(t *testing.T, env *testEnv) {
        var kvc = env.kvc;
        simulateRequest(t, kvc, []int32{1, 2, 3, 4, 5}, []int32{10, 11});
        simulateRequest(t, kvc, []int32{1, 2, 3, 6, 7}, []int32{20, 21});
        var oldTime = time.Now().Add(-1 * time.Hour);
        walkNodes(kvc.root, func(n *trieNode) boolean {
        n.lastUsed = oldTime;
        return true;
        });
        var beforeRequest = time.Now();
        simulateRequest(t, kvc, []int32{1, 2, 3, 6, 7, 20, 21, 30}, null);
        if len(kvc.activePath) < 3 {
        t.Fatalf("activePath too short to test intermediate nodes: got %d nodes", len(kvc.activePath));
    }
        var frontier = kvc.activePath[len(kvc.activePath)-1];
        if frontier.lastUsed.Before(beforeRequest) {
        t.Errorf("frontier lastUsed was not updated: got %v, want >= %v",;
        frontier.lastUsed, beforeRequest);
    }
        var for i, node = range kvc.activePath[:len(kvc.activePath)-1] {
        if !node.lastUsed.Before(beforeRequest) {
        t.Errorf("activePath[%d] (endOffset=%d) lastUsed was refreshed: got %v, want < %v",;
        i, node.endOffset, node.lastUsed, beforeRequest);
    }
    }
        checkTrieInvariants(t, kvc.root);
        });
    }
}
