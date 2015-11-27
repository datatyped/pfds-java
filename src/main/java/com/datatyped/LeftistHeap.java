package com.datatyped;

import fj.F0;
import fj.F4;
import fj.Ord;
import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.Flavour;

import static com.datatyped.LeftistHeaps.*;

public final class LeftistHeap<A> implements Heap<A, LeftistHeap.Heap<A>> {
    private final Ord<A> ord;

    public LeftistHeap(final Ord<A> ord) {
        this.ord = ord;
    }

    @Data(value = @Derive(inClass = "LeftistHeaps"), flavour = Flavour.FJ)
    public interface Heap<A> {
        <X> X match(
            F0<X> E,
            F4<Integer, A, Heap<A>, Heap<A>, X> T
        );
    }

    private Integer rank(Heap<A> h) {
        return h.match(
            () -> 0,
            (r, x, a, b) -> r
        );
    }

    private Heap<A> makeT(A x, Heap<A> a, Heap<A> b) {
        if (rank(a) >= rank(b)) return T(rank(b) + 1, x, a, b);
        else return T(rank(a) + 1, x, b, a);
    }

    @Override
    public Heap<A> empty() {
        return E();
    }

    @Override
    public boolean isEmpty(Heap<A> h) {
        return h.match(
            () -> true,
            (r, x, a, b) -> false
        );
    }

    @Override
    public Heap<A> insert(A x, Heap<A> h) {
        return merge(T(1, x, E(), E()), h);
    }

    @Override
    public Heap<A> merge(Heap<A> h1, Heap<A> h2) {
        return h1.match(
            () -> h2,
            (r1, x, a1, b1) -> h2.match(
                () -> h1,
                (r2, y, a2, b2) -> {
                    if (ord.isGreaterThan(x, y)) return makeT(y, a2, merge(h1, b2));
                    else return makeT(x, a1, merge(b1, h2));
                }
            )
        );
    }

    @Override
    public A findMin(Heap<A> h) {
        return h.match(
            () -> { throw new IllegalArgumentException("LeftistHeap.findMin: empty heap"); },
            (r, x, a, b) -> x
        );
    }

    @Override
    public Heap<A> deleteMin(Heap<A> h) {
        return h.match(
            () -> { throw new IllegalArgumentException("LeftistHeap.deleteMin: empty heap"); },
            (r, x, a, b) -> merge(a, b)
        );
    }
}