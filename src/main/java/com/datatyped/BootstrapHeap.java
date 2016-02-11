package com.datatyped;

import javaslang.Function0;
import javaslang.Function2;
import javaslang.collection.List;
import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.FieldNames;
import org.derive4j.Flavour;

import java.util.Comparator;

import static com.datatyped.BootstrapHeaps.*;

public final class BootstrapHeap<A> implements Heap<A, BootstrapHeap.Heap<A>> {
    private final Comparator<A> comparator;
    private final SkewBinomialHeap<Heap<A>> primH;

    @Data(value = @Derive(inClass = "BootstrapHeaps"), flavour = Flavour.Javaslang)
    public interface Heap<A> {
        <X> X match(
            Function0<X> E,
            @FieldNames({"elem", "primH"})
            Function2<A, List<SkewBinomialHeap.Tree<Heap<A>>>, X> H
        );
    }

    private BootstrapHeap(Comparator<A> comparator) {
        this.comparator = comparator;
        this.primH = SkewBinomialHeap.create((h1, h2) ->
            h1.match(
                () -> h2.match(
                    () -> 0,
                    (y, p2) -> -1),
                (x, p1) -> h2.match(
                    () -> 1,
                    (y, p2) -> comparator.compare(x, y))
            )
        );
    }

    public static <A> BootstrapHeap<A> create(Comparator<A> comparator) {
        return new BootstrapHeap<A>(comparator);
    }

    public static <A extends Comparable<A>> BootstrapHeap<A> create() {
        return new BootstrapHeap<A>(Comparator.naturalOrder());
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
                    if (comparator.compare(x, y) > 1) return H(y, primH.insert(h1, p2));
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
