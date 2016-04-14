package com.datatyped.heap;

import javaslang.collection.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HeapTest {
    LeftistHeap<Integer> leftistHeap = LeftistHeap.create();
    SplayHeap<Integer> splayHeap = SplayHeap.create();
    SkewBinomialHeap<Integer> skewBinomialHeap = SkewBinomialHeap.create();
    BootstrapHeap<Integer> bootstrapHeap = BootstrapHeap.create();

    @Test
    public void leftistHeapTest() {
        LeftistHeap.Heap<Integer> h = leftistHeap.empty();
        assertThat(leftistHeap.isEmpty(h)).isTrue();
        h = leftistHeap.insert(10, h);
        assertThat(leftistHeap.isEmpty(h)).isFalse();
        h = leftistHeap.insert(1, h);
        assertThat(leftistHeap.findMin(h)).isEqualTo(1);
    }

    @Test
    public void splayHeapTest() {
        SplayHeap.Heap<Integer> h = splayHeap.empty();
        assertThat(splayHeap.isEmpty(h)).isTrue();
        h = splayHeap.insert(10, h);
        assertThat(splayHeap.isEmpty(h)).isFalse();
        h = splayHeap.insert(1, h);
        assertThat(splayHeap.findMin(h)).isEqualTo(1);
    }

    @Test
    public void skewBinomialHeapTest() {
        List<SkewBinomialHeap.Tree<Integer>> h = skewBinomialHeap.empty();
        assertThat(skewBinomialHeap.isEmpty(h)).isTrue();
        h = skewBinomialHeap.insert(10, h);
        assertThat(skewBinomialHeap.isEmpty(h)).isFalse();
        h = skewBinomialHeap.insert(1, h);
        assertThat(skewBinomialHeap.findMin(h)).isEqualTo(1);
    }

    @Test
    public void bootstrapHeapTest() {
        BootstrapHeap.Heap<Integer> h = bootstrapHeap.empty();
        assertThat(bootstrapHeap.isEmpty(h)).isTrue();
        h = bootstrapHeap.insert(10, h);
        assertThat(bootstrapHeap.isEmpty(h)).isFalse();
        h = bootstrapHeap.insert(1, h);
        assertThat(bootstrapHeap.findMin(h)).isEqualTo(1);
    }
}