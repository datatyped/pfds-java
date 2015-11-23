package com.datatyped;

import fj.F0;
import fj.F3;
import fj.P;
import fj.P2;
import org.derive4j.Data;
import org.derive4j.Flavour;

import static com.datatyped.SplayHeaps.*;

@Data(flavour = Flavour.FJ)
public abstract class SplayHeap<A extends Comparable<A>> implements Heap<A, SplayHeap<A>> {
    public abstract <X> X match(
        F0<X> E,
        F3<SplayHeap<A>, A, SplayHeap<A>, X> T
    );

    private P2<SplayHeap<A>, SplayHeap<A>> partition(A pivot) {
        return match(
            () -> P.p(E(), E()),
            (a, x, b) -> {
                if (x.compareTo(pivot) <= 0) {
                    return b.match(
                        () ->  P.p(this, E()),
                        (b1, y, b2) -> {
                            if (y.compareTo(pivot) <= 0) {
                                P2<SplayHeap<A>, SplayHeap<A>> p = b2.partition(pivot);
                                return P.p(T(T(a, x, b1), y, p._1()), p._2());
                            } else {
                                P2<SplayHeap<A>, SplayHeap<A>> p = b1.partition(pivot);
                                return P.p(T(a, x, p._1()), T(p._2(), y, b2));
                            }
                        }
                    );
                } else {
                    return a.match(
                        () -> P.p(E(), this),
                        (a1, y, a2) -> {
                            if (y.compareTo(pivot) <= 0) {
                                P2<SplayHeap<A>, SplayHeap<A>> p = a2.partition(pivot);
                                return P.p(T(a1, y, p._1()), T(p._2(), x, b));
                            } else {
                                P2<SplayHeap<A>, SplayHeap<A>> p = a1.partition(pivot);
                                return P.p(p._1(), T(p._2(), y, T(a2, x, b)));
                            }
                        }
                    );
                }
            }
        );
    }

    static <A extends Comparable<A>> SplayHeap<A> empty() {
        return E();
    }

    @Override
    public boolean isEmpty() {
        return match(
            () -> true,
            (a, x, b) -> false
        );
    }

    @Override
    public SplayHeap<A> insert(A x) {
        P2<SplayHeap<A>, SplayHeap<A>> p = partition(x);
        return T(p._1(), x, p._2());
    }

    @Override
    public SplayHeap<A> merge(SplayHeap<A> t) {
        return match(
            () -> t,
            (a, x, b) -> {
                P2<SplayHeap<A>, SplayHeap<A>> p = t.partition(x);
                return T(a.merge(p._1()), x, b.merge(p._2()));
            }
        );
    }

    @Override
    public A findMin() {
        return match(
            () -> { throw new IllegalArgumentException("SplayHeap.findMin: empty heap"); },
            (a, x, b) -> a.isEmpty() ? x : a.findMin()
        );
    }

    @Override
    public SplayHeap<A> deleteMin() {
        return match(
            () -> { throw new IllegalArgumentException("SplayHeap.deleteMin: empty heap"); },
            (a, x, b) -> a.match(
                () -> b,
                (a1, y, a2) -> a1.isEmpty() ? T(a2, x, b) : T(a1.deleteMin(), y, T(a2, x, b))
            )
        );
    }
}