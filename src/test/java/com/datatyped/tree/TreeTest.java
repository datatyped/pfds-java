package com.datatyped.tree;

import javaslang.algebra.Monoid;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TreeTest {
    FingerTreeModule<Integer, Integer> module = FingerTreeModule.create(Monoid.of(0, (a1, a2) -> a1 + a2), a -> 1);

    @Test
    public void fingerTreeTest() {
        FingerTreeModule.FingerTree<Integer, Integer> t = module.empty();
        assertThat(module.isEmpty(t)).isTrue();
        for (int i = 1; i <= 100; i++) {
            t = module.cons(t,i);
        }
        assertThat(module.isEmpty(t)).isFalse();
        assertThat(module.head(t)).isEqualTo(100);
        assertThat(module.last(t)).isEqualTo(1);
    }
}