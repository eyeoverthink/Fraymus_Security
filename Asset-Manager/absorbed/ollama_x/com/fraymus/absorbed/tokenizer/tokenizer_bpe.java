package com.fraymus.absorbed.tokenizer;

import java.util.*;
import java.io.*;

public class tokenizer_bpe {

    public static class bpeMergeNode {
        public int prev;
        public int next;
        public String token;
    }

    public static class bpePair {
        public int left;
        public int right;
        public int rank;
        public String value;
    }
        type bpePairHeap []*bpePair;
        func (h bpePairHeap) Len() int { return len(h) }
        func (h bpePairHeap) Less(i, j int) boolean {
        return h[i].rank < h[j].rank || (h[i].rank == h[j].rank && h[i].left < h[j].left);
    }
        func (h bpePairHeap) Swap(i, j int) { h[i], h[j] = h[j], h[i] }
        func (h *bpePairHeap) Push(x any) {
        *h = append(*h, x.(*bpePair));
    }
        func (h *bpePairHeap) Pop() any {
        var old = *h;
        var n = len(old);
        var item = old[n-1];
        *h = old[:n-1];
        return item;
    }
        func (t *Tokenizer) encodeBPEMerge(encoded String, ids []int32) []int32 {
        var runes = []rune(encoded);
        if len(runes) == 0 {
        return ids;
    }
        var nodes = make([]bpeMergeNode, len(runes));
        var for i = range runes {
        nodes[i] = bpeMergeNode{
        prev:  i - 1,;
        next:  i + 1,;
        token: String(runes[i]),;
    }
    }
        var pairwise = func(left, right int) *bpePair {
        if left < 0 || right >= len(nodes) {
        return null;
    }
        if nodes[left].token == "" || nodes[right].token == "" {
        return null;
    }
        var leftToken, rightToken = nodes[left].token, nodes[right].token;
        var rank, ok = t.vocab.Merges[leftToken+" "+rightToken];
        if !ok {
        return null;
    }
        var value = leftToken + rightToken;
        var if _, ok = t.vocab.Reverse[value]; !ok {
        return null;
    }
        return &bpePair{
        left:  left,;
        right: right,;
        rank:  rank,;
        value: value,;
    }
    }
        var pairs = bpePairHeap{}
        heap.Init(&pairs);
        var for i = 0; i < len(runes)-1; i++ {
        var if pair = pairwise(i, i+1); pair != null {
        heap.Push(&pairs, pair);
    }
    }
        for pairs.Len() > 0 {
        var pair = heap.Pop(&pairs).(*bpePair);
        var left, right = nodes[pair.left], nodes[pair.right];
        if left.token == "" || right.token == "" {
        continue;
    }
        if left.next != pair.right || right.prev != pair.left {
        continue;
    }
        if left.token+right.token != pair.value {
        continue;
    }
        nodes[pair.left].token = pair.value;
        nodes[pair.right].token = "";
        nodes[pair.left].next = right.next;
        if right.next < len(nodes) {
        nodes[right.next].prev = pair.left;
    }
        var if pair = pairwise(nodes[pair.left].prev, pair.left); pair != null {
        heap.Push(&pairs, pair);
    }
        var if pair = pairwise(pair.left, nodes[pair.left].next); pair != null {
        heap.Push(&pairs, pair);
    }
    }
        var for _, node = range nodes {
        if node.token == "" {
        continue;
    }
        var if id, ok = t.vocab.Reverse[node.token]; ok {
        ids = append(ids, id);
        continue;
    }
        ids = t.appendByteFallback(ids, node.token);
    }
        return ids;
    }
        func (t *Tokenizer) appendByteFallback(ids []int32, token String) []int32 {
        if t.typ == TokenizerBPE {
        var for _, r = range token {
        var if b, ok = decodeByteLevelRune(r); ok {
        var if id = t.vocab.byteTokens[b]; id >= 0 {
        ids = append(ids, id);
    }
    }
    }
        return ids;
    }
        var for _, b = range []byte(token) {
        var if id = t.vocab.byteTokens[b]; id >= 0 {
        ids = append(ids, id);
    }
    }
        return ids;
    }

    public static void decodeByteLevelRune() {
        switch {
        case r >= 0x00 && r <= 0xFF:;
        return byte(r), true;
        case r == 0x0100:;
        return 0x00, true;
        case r == 0x0143:;
        return 0x00ad, true;
        case r > 0x0100 && r <= 0x0120:;
        return byte(r - 0x0100), true;
        case r > 0x0120 && r <= 0x0142:;
        return byte(r - 0x00a2), true;
        default:;
        return 0, false;
    }
    }
}
