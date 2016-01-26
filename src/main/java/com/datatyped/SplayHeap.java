package com.datatyped;

import fj.*;
import org.derive4j.Data;
import org.derive4j.Derive;

import static com.datatyped.SplayHeaps.*;

public final class SplayHeap<A>  implements Heap<A, SplayHeap.Heap<A>> {
    private final Ord<A> ord;

    public SplayHeap(final Ord<A> ord) {
        this.ord = ord;
    }

    @Data(@Derive(inClass = "SplayHeaps"))
    public interface Heap<A> {
        <X> X match(
            F0<X> E,
            F3<Heap<A>, A, Heap<A>, X> T
        );
    }

    private P2<Heap<A>, Heap<A>> partition(A pivot, Heap<A> h) {
        return h.match(
            () -> P.p(E(), E()),
            (a, x, b) -> {
                if (ord.isGreaterThan(x, pivot)) {
                    return a.match(
                        () -> P.p(E(), h),
                        (a1, y, a2) -> {
                            if (ord.isGreaterThan(y, pivot)) {
                                P2<Heap<A>, Heap<A>> p = partition(pivot, a1);
                                return P.p(p._1(), T(p._2(), y, T(a2, x, b)));
                            } else {
                                P2<Heap<A>, Heap<A>> p = partition(pivot, a2);
                                return P.p(T(a1, y, p._1()), T(p._2(), x, b));
                            }
                        }
                    );
                } else {
                    return b.match(
                        () ->  P.p(h, E()),
                        (b1, y, b2) -> {
                            if (ord.isGreaterThan(y, pivot)) {
                                P2<Heap<A>, Heap<A>> p = partition(pivot, b1);
                                return P.p(T(a, x, p._1()), T(p._2(), y, b2));
                            } else {
                                P2<Heap<A>, Heap<A>> p = partition(pivot, b2);
                                return P.p(T(T(a, x, b1), y, p._1()), p._2());
                            }
                        }
                    );
                }
            }
        );
    }

    @Override
    public Heap<A> empty() {
        return E();
    }

    @Override
    public boolean isEmpty(Heap<A> t) {
        return t. match(
            () -> true,
            (a, x, b) -> false
        );
    }

    @Override
    public Heap<A> insert(A x, Heap<A> h) {
        P2<Heap<A>, Heap<A>> p = partition(x, h);
        return T(p._1(), x, p._2());
    }

    @Override
    public Heap<A> merge(Heap<A> h1, Heap<A> h2) {
        return h1.match(
            () -> h2,
            (a, x, b) -> {
                P2<Heap<A>, Heap<A>> p = partition(x, h2);
                return T(merge(p._1(), a), x, merge(p._2(), b));
            }
        );
    }

    @Override
    public A findMin(Heap<A> h) {
        return h.match(
            () -> { throw new IllegalArgumentException("SplayHeap.findMin: empty heap"); },
            (a, x, b) -> isEmpty(a) ? x : findMin(a)
        );
    }

    @Override
    public Heap<A> deleteMin(Heap<A> h) {
        return h.match(
            () -> { throw new IllegalArgumentException("SplayHeap.deleteMin: empty heap"); },
            (a, x, b) -> a.match(
                () -> b,
                (a1, y, a2) -> isEmpty(a1) ? T(a2, x, b) : T(deleteMin(a1), y, T(a2, x, b))
            )
        );
    }
}