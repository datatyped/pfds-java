package com.datatyped;

import fj.F4;
import fj.Ord;
import fj.P;
import fj.P2;
import fj.data.List;
import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.FieldNames;

import static com.datatyped.SkewBinomialHeaps.*;
import static fj.data.List.*;

public final class SkewBinomialHeap<A> implements Heap<A, List<SkewBinomialHeap.Tree<A>>> {
    private final Ord<A> ord;

    public SkewBinomialHeap(final Ord<A> ord) {
        this.ord = ord;
    }

    @Data(@Derive(inClass = "SkewBinomialHeaps"))
    public interface Tree<A> {
        <X> X match(
            @FieldNames({"rank", "root", "elements", "children"})
            F4<Integer, A, List<A>, List<Tree<A>>, X> node
        );
    }

    private Tree<A> link(Tree<A> t1, Tree<A> t2) {
        return t1.match((r1, x1, xs1, c1) ->
            t2.match((r2, x2, xs2, c2) -> {
                if (ord.isGreaterThan(x1, x2)) return node(r1 + 1, x1, xs1, cons(t2, c1));
                else return node(r1 + 1, x2, xs2, cons(t1, c2));
            })
        );
    }

    private Tree<A> skewLink(A x, Tree<A> t1, Tree<A> t2) {
        return link(t1, t2).match((r, y, ys, c) -> {
            if (ord.isGreaterThan(x, y)) return node(r, x, cons(y, ys), c);
            else return node(r, y, cons(x, ys), c);
        });
    }

    private List<Tree<A>> insTree(Tree<A> t1, List<Tree<A>> ts) {
        if (ts.isEmpty()) {
            return single(t1);
        } else {
            Tree<A> t2 = ts.head();
            if (getRank(t1) < getRank(t2)) return cons(t1,ts);
            else return insTree(link(t1, t2), ts.tail());
        }
    }

    private List<Tree<A>> mergeTrees(List<Tree<A>> ts1, List<Tree<A>> ts2) {
        return ts1.list(ts2, t1 -> ts1p ->
            ts1.list(ts1, t2 -> ts2p -> {
                if (getRank(t1) < getRank(t2)) return cons(t1, mergeTrees(ts1p, ts2));
                else if (getRank(t2) < getRank(t1)) return cons(t2, mergeTrees(ts1, ts2p));
                else return insTree(link(t1, t2), mergeTrees(ts1p, ts2p));
            })
        );
    }

    private List<Tree<A>> normalize(List<Tree<A>> ts) {
        if (ts.isEmpty()) return nil();
        else return insTree(ts.head(), ts.tail());
    }

    @Override
    public List<Tree<A>> empty() {
        return nil();
    }

    @Override
    public boolean isEmpty(List<Tree<A>> ts) {
        return ts.isEmpty();
    }

    @Override
    public List<Tree<A>> insert(A x, List<Tree<A>> ts) {
        if (ts.isEmpty() || ts.isSingle()) {
            return cons(node(0, x, nil(), nil()), ts);
        } else {
            Tree<A> t1 = ts.head();
            Tree<A> t2 = ts.tail().head();
            List<Tree<A>> rest = ts.tail().tail();
            if (getRank(t1) < getRank(t2)) return cons(skewLink(x, t1, t2), rest);
            else return cons(node(0, x, nil(), nil()), ts);
        }
    }

    @Override
    public List<Tree<A>> merge(List<Tree<A>> ts1, List<Tree<A>> ts2) {
        return mergeTrees(normalize(ts1), normalize(ts2));
    }

    private P2<Tree<A>, List<Tree<A>>> removeMinTree(List<Tree<A>> ts) {
        if (ts.isEmpty()) {
            throw new IllegalArgumentException("SkewBinomialHeap.removeMinTree: empty heap");
        } else if (ts.isSingle()) {
            return P.p(ts.head(), empty());
        } else {
            Tree<A> t = ts.head();
            P2<Tree<A>, List<Tree<A>>> p = removeMinTree(ts.tail());
            if (ord.isGreaterThan(getRoot(t), getRoot(p._1()))) return P.p(p._1(), cons(t, p._2()));
            else return P.p(t, ts);
        }
    }

    @Override
    public A findMin(List<Tree<A>> ts) {
        return getRoot(removeMinTree(ts)._1());
    }

    @Override
    public List<Tree<A>> deleteMin(List<Tree<A>> ts) {
        P2<Tree<A>, List<Tree<A>>> p = removeMinTree(ts);
        return p._1().match((r, x, xs, ts1) -> {
            List<Tree<A>> ts2 = mergeTrees(ts1.reverse(), normalize(p._2()));
            return xs.foldRight(this::insert, ts2);
        });
    }
}