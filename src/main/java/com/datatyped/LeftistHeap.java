package com.datatyped;

import fj.F0;
import fj.F4;
import org.derive4j.Data;
import org.derive4j.Flavour;

import static com.datatyped.LeftistHeaps.*;

@Data(flavour = Flavour.FJ)
public abstract class LeftistHeap<A extends Comparable<A>> implements Heap<A, LeftistHeap<A>> {
    public abstract <X> X match(
        F0<X> E,
        F4<Integer, A, LeftistHeap<A>, LeftistHeap<A>, X> T
    );

    private Integer rank() {
        return match(
            () -> 0,
            (r, x, a, b) -> r
        );
    }

    private LeftistHeap<A> makeT(A x, LeftistHeap<A> t) {
        if (rank() >= t.rank()) return T(t.rank() + 1, x, this, t);
        else return T(rank() + 1, x, t, this);
    }

    public static <A extends Comparable<A>> LeftistHeap<A> empty() {
        return E();
    }

    @Override
    public boolean isEmpty() {
        return match(
            () -> true,
            (r, x, a, b) -> false
        );
    }

    @Override
    public LeftistHeap<A> insert(A x) {
        return merge(T(1, x, E(), E()));
    }

    @Override
    public LeftistHeap<A> merge(LeftistHeap<A> t) {
        return match(
            () -> t,
            (r1, x, a1, b1) -> t.match(
                () -> this,
                (r2, y, a2, b2) -> {
                    if (x.compareTo(y) <= 0) return a1.makeT(x, t.merge(b1));
                    else return a2.makeT(y, merge(b2));
                }
            )
        );
    }

    @Override
    public A findMin() {
        return match(
            () -> { throw new IllegalArgumentException("LeftistHeap.findMin: empty heap"); },
            (r, x, a, b) -> x
        );
    }

    @Override
    public LeftistHeap<A> deleteMin() {
        return match(
            () -> { throw new IllegalArgumentException("LeftistHeap.deleteMin: empty heap"); },
            (r, x, a, b) -> a.merge(b)
        );
    }
}