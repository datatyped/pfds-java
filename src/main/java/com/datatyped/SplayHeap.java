package com.datatyped;

import javaslang.Function0;
import javaslang.Function3;
import javaslang.Tuple;
import javaslang.Tuple2;
import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.Flavour;

import java.util.Comparator;

import static com.datatyped.SplayHeaps.*;

public final class SplayHeap<A>  implements Heap<A, SplayHeap.Heap<A>> {
    private final Comparator<A> comparator;

    @Data(value = @Derive(inClass = "SplayHeaps"), flavour = Flavour.Javaslang)
    public interface Heap<A> {
        <X> X match(
            Function0<X> E,
            Function3<Heap<A>, A, Heap<A>, X> T
        );
    }

    private SplayHeap(final Comparator<A> comparator) {
        this.comparator = comparator;
    }

    public static <A> SplayHeap<A> create(Comparator<A> comparator) {
        return new SplayHeap<A>(comparator);
    }

    public static <A extends Comparable<A>> SplayHeap<A> create() {
        return new SplayHeap<A>(Comparator.naturalOrder());
    }

    private Tuple2<Heap<A>, Heap<A>> partition(A pivot, Heap<A> h) {
        return h.match(
            () -> Tuple.of(E(), E()),
            (a, x, b) -> {
                if (comparator.compare(x, pivot) > 0) {
                    return a.match(
                        () -> Tuple.of(E(), h),
                        (a1, y, a2) -> {
                            if (comparator.compare(y, pivot) > 0) {
                                Tuple2<Heap<A>, Heap<A>> p = partition(pivot, a1);
                                return Tuple.of(p._1(), T(p._2(), y, T(a2, x, b)));
                            } else {
                                Tuple2<Heap<A>, Heap<A>> p = partition(pivot, a2);
                                return Tuple.of(T(a1, y, p._1()), T(p._2(), x, b));
                            }
                        }
                    );
                } else {
                    return b.match(
                        () -> Tuple.of(h, E()),
                        (b1, y, b2) -> {
                            if (comparator.compare(y, pivot) > 0) {
                                Tuple2<Heap<A>, Heap<A>> p = partition(pivot, b1);
                                return Tuple.of(T(a, x, p._1()), T(p._2(), y, b2));
                            } else {
                                Tuple2<Heap<A>, Heap<A>> p = partition(pivot, b2);
                                return Tuple.of(T(T(a, x, b1), y, p._1()), p._2());
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
        Tuple2<Heap<A>, Heap<A>> p = partition(x, h);
        return T(p._1(), x, p._2());
    }

    @Override
    public Heap<A> merge(Heap<A> h1, Heap<A> h2) {
        return h1.match(
            () -> h2,
            (a, x, b) -> {
                Tuple2<Heap<A>, Heap<A>> p = partition(x, h2);
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