package com.datatyped.heap;

import javaslang.Function4;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.List;
import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.FieldNames;
import org.derive4j.Flavour;

import java.util.Comparator;

import static com.datatyped.heap.SkewBinomialHeaps.*;

public final class SkewBinomialHeap<A> implements Heap<A, List<SkewBinomialHeap.Tree<A>>> {
    private final Comparator<A> comparator;

    @Data(value = @Derive(inClass = "SkewBinomialHeaps"), flavour = Flavour.Javaslang)
    public interface Tree<A> {
        <X> X match(
            @FieldNames({"rank", "root", "elements", "children"})
            Function4<Integer, A, List<A>, List<Tree<A>>, X> node
        );
    }

    private SkewBinomialHeap(final Comparator<A> comparator) {
        this.comparator = comparator;
    }

    public static <A> SkewBinomialHeap<A> create(Comparator<A> comparator) {
        return new SkewBinomialHeap<A>(comparator);
    }

    public static <A extends Comparable<A>> SkewBinomialHeap<A> create() {
        return new SkewBinomialHeap<A>(Comparator.naturalOrder());
    }

    private Tree<A> link(Tree<A> t1, Tree<A> t2) {
        return t1.match((r1, x1, xs1, c1) ->
            t2.match((r2, x2, xs2, c2) -> {
                if (comparator.compare(x1, x2) > 0) return node(r1 + 1, x1, xs1, c1.prepend(t2));
                else return node(r1 + 1, x2, xs2, c2.prepend(t1));
            })
        );
    }

    private Tree<A> skewLink(A x, Tree<A> t1, Tree<A> t2) {
        return link(t1, t2).match((r, y, ys, c) -> {
            if (comparator.compare(x, y) > 0) return node(r, x, ys.prepend(y), c);
            else return node(r, y, ys.prepend(x), c);
        });
    }

    private List<Tree<A>> insTree(Tree<A> t1, List<Tree<A>> ts) {
        if (ts.isEmpty()) {
            return List.of(t1);
        } else {
            Tree<A> t2 = ts.head();
            if (getRank(t1) < getRank(t2)) return ts.prepend(t1);
            else return insTree(link(t1, t2), ts.tail());
        }
    }

    private List<Tree<A>> mergeTrees(List<Tree<A>> ts1, List<Tree<A>> ts2) {
        if (ts1.isEmpty()) {
            return ts2;
        } else if (ts2.isEmpty()) {
            return ts1;
        } else {
            Tree<A> t1 = ts1.head();
            Tree<A> t2 = ts2.head();
            if (getRank(t1) < getRank(t2)) return mergeTrees(ts1.tail(), ts2).prepend(t1);
            else if (getRank(t2) < getRank(t1)) return mergeTrees(ts1, ts2.tail()).prepend(t2);
            else return insTree(link(t1, t2), mergeTrees(ts1.tail(), ts2.tail()));
        }
    }

    private List<Tree<A>> normalize(List<Tree<A>> ts) {
        if (ts.isEmpty()) return List.empty();
        else return insTree(ts.head(), ts.tail());
    }

    @Override
    public List<Tree<A>> empty() {
        return List.empty();
    }

    @Override
    public boolean isEmpty(List<Tree<A>> ts) {
        return ts.isEmpty();
    }

    @Override
    public List<Tree<A>> insert(A x, List<Tree<A>> ts) {
        if (ts.isEmpty() || ts.tail().isEmpty()) {
            return ts.prepend(node(0, x, List.empty(), List.empty()));
        } else {
            Tree<A> t1 = ts.head();
            Tree<A> t2 = ts.tail().head();
            List<Tree<A>> rest = ts.tail().tail();
            if (getRank(t1) < getRank(t2)) return rest.prepend(skewLink(x, t1, t2));
            else return ts.prepend(node(0, x, List.empty(), List.empty()));
        }
    }

    @Override
    public List<Tree<A>> merge(List<Tree<A>> ts1, List<Tree<A>> ts2) {
        return mergeTrees(normalize(ts1), normalize(ts2));
    }

    private Tuple2<Tree<A>, List<Tree<A>>> removeMinTree(List<Tree<A>> ts) {
        if (ts.isEmpty()) {
            throw new IllegalArgumentException("SkewBinomialHeap.removeMinTree: empty heap");
        } else if (ts.tail().isEmpty()) {
            return Tuple.of(ts.head(), empty());
        } else {
            Tree<A> t = ts.head();
            Tuple2<Tree<A>, List<Tree<A>>> p = removeMinTree(ts.tail());
            if (comparator.compare(getRoot(t), getRoot(p._1())) > 0) return Tuple.of(p._1(), p._2().prepend(t));
            else return Tuple.of(t, ts);
        }
    }

    @Override
    public A findMin(List<Tree<A>> ts) {
        return getRoot(removeMinTree(ts)._1());
    }

    @Override
    public List<Tree<A>> deleteMin(List<Tree<A>> ts) {
        Tuple2<Tree<A>, List<Tree<A>>> p = removeMinTree(ts);
        return p._1().match((r, x, xs, ts1) -> {
            List<Tree<A>> ts2 = mergeTrees(ts1.reverse(), normalize(p._2()));
            return xs.foldRight(ts2, this::insert);
        });
    }
}