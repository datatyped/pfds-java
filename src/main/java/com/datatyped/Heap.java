package com.datatyped;

import java.util.Iterator;

public interface Heap<A extends Comparable<A>, T extends Heap<A, T>> extends Iterable<A> {
    boolean isEmpty();

    T insert(A x);
    T merge(T t);

    A findMin();
    T deleteMin();

    default Iterator<A> iterator() {
        return new Iterator<A>() {
            private Heap<A, T> t = Heap.this;

            @Override
            public boolean hasNext() {
                return t.isEmpty();
            }

            @Override
            public A next() {
                A x = t.findMin();
                t = t.deleteMin();
                return x;
            }
        };
    }
}
