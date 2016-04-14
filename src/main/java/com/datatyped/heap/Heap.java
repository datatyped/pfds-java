package com.datatyped.heap;

public interface Heap<A, T> {
    T empty();
    boolean isEmpty(T h);

    T insert(A x, T h);
    T merge(T h1, T h2);

    A findMin(T h);
    T deleteMin(T h);
}