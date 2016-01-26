package com.datatyped;

import fj.*;
import fj.data.List;
import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.FieldNames;

import static com.datatyped.BootstrapHeaps.*;
import static fj.Ord.ord;

public final class BootstrapHeap<A> implements Heap<A, BootstrapHeap.Heap<A>> {
    private final Ord<A> ord;
    private final SkewBinomialHeap<Heap<A>> primH;

    public BootstrapHeap(Ord<A> ord) {
        this.ord = ord;
        this.primH = new SkewBinomialHeap<>(ord(h1 -> h2 ->
            h1.match(
                () -> h2.match(
                    () -> Ordering.EQ,
                    (y, p2) -> Ordering.LT),
                (x, p1) -> h2.match(
                    () -> Ordering.GT,
                    (y, p2) -> ord.compare(x, y))
            )
        ));
    }

    @Data(@Derive(inClass = "BootstrapHeaps"))
    public interface Heap<A> {
        <X> X match(
            F0<X> E,
            @FieldNames({"elem", "primH"})
            F2<A, List<SkewBinomialHeap.Tree<Heap<A>>>, X> H
        );
    }

    @Override
    public Heap<A> empty() {
        return E();
    }

    @Override
    public boolean isEmpty(Heap<A> h) {
        return h.match(
            () -> true,
            (x, p) -> false
        );
    }

    @Override
    public Heap<A> insert(A x, Heap<A> h) {
        return merge(H(x, primH.empty()), h);
    }

    @Override
    public Heap<A> merge(Heap<A> h1, Heap<A> h2) {
        return h1.match(
            () -> h2,
            (x, p1) -> h2.match(
                () -> h1,
                (y, p2) -> {
                    if (ord.isGreaterThan(x, y)) return H(y, primH.insert(h1, p2));
                    else return H(x, primH.insert(h2, p1));
                }
            )
        );
    }

    @Override
    public A findMin(Heap<A> h) {
        return h.match(
            () -> { throw new IllegalArgumentException("BootstrapHeap.findMin: empty heap"); },
            (x, p) -> x
        );
    }

    @Override
    public Heap<A> deleteMin(Heap<A> h) {
        return h.match(
            () -> { throw new IllegalArgumentException("BootstrapHeap.deleteMin: empty heap"); },
            (x, p) -> {
                if (primH.isEmpty(p)) {
                    return E();
                } else {
                    return primH.findMin(p).match(
                        () -> { throw new IllegalArgumentException("BootstrapHeap.deleteMin: empty heap"); },
                        (y, p1) -> H(y, primH.merge(p1, primH.deleteMin(p)))
                    );
                }
            }
        );
    }
}
