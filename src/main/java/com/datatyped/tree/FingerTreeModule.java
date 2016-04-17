package com.datatyped.tree;

import javaslang.*;
import javaslang.algebra.Monoid;
import javaslang.collection.List;
import org.derive4j.Data;
import org.derive4j.FieldNames;
import org.derive4j.Flavour;

import java.util.NoSuchElementException;

import static com.datatyped.tree.Nodes.*;
import static com.datatyped.tree.Digits.*;
import static com.datatyped.tree.FingerTrees.*;
import static com.datatyped.tree.Views.*;

public class FingerTreeModule<A, M> {
    private final Monoid<M> monoid;
    private final Function1<A, M> measure;

    @Data(flavour = Flavour.Javaslang)
    public interface Node<A, M> {
        <X> X match(
            @FieldNames({"v", "a", "b"})
            Function3<M, A, A, X> node2,
            @FieldNames({"v", "a", "b", "c"})
            Function4<M, A, A, A, X> node3
        );

        default <T> T foldRight(Function2<T, A, T> f, T acc) {
            return match(
                (v, a, b) -> f.apply(f.apply(acc, b), a),
                (v, a, b, c) -> f.apply(f.apply(f.apply(acc, c), b), a)
            );
        }

        default <T> T foldLeft(Function2<T, A, T> f, T acc) {
            return match(
                (v, a, b) -> f.apply(f.apply(acc, a), b),
                (v, a, b, c) -> f.apply(f.apply(f.apply(acc, a), b), c)
            );
        }

        static <A, M>  M measure(Node<A, M> node) {
            return node.match(
                (v, a, b) -> v,
                (v, a, b, c) -> v
            );
        }

        static <A, M> Digit<A, M> toDigit(Node<A, M> node) {
            return node.match(
                (v, a, b) -> two(v, a, b),
                (v, a, b, c) -> three(v, a, b, c)
            );
        }

        static <A, M> Node<A, M> create(Monoid<M> monoid, Function1<A, M> measure, A a, A b) {
            return node2(monoid.combine(measure.apply(a), measure.apply(b)), a, b);
        }

        static <A, M> Node<A, M> create(Monoid<M> monoid, Function1<A, M> measure, A a, A b, A c) {
            return node3(monoid.combine(measure.apply(a), monoid.combine(measure.apply(b), measure.apply(c))), a, b, c);
        }
    }

    @Data(flavour = Flavour.Javaslang)
    public interface Digit<A, M> {
        <X> X match(
            @FieldNames({"v", "a"})
            Function2<M, A, X> one,
            @FieldNames({"v", "a", "b"})
            Function3<M, A, A, X> two,
            @FieldNames({"v", "a", "b", "c"})
            Function4<M, A, A, A, X> three,
            @FieldNames({"v", "a", "b", "c", "d"})
            Function5<M, A, A, A, A, X> four
        );

        default <T> T foldRight(Function2<T, A, T> f, T acc) {
            return match(
                (v, a) -> f.apply(acc, a),
                (v, a, b) -> f.apply(f.apply(acc, b), a),
                (v, a, b, c) -> f.apply(f.apply(f.apply(acc, c), b), a),
                (v, a, b, c, d) -> f.apply(f.apply(f.apply(f.apply(acc, d), c), b), a)
            );
        }

        default <T> T foldLeft(Function2<T, A, T> f, T acc) {
            return match(
                (v, a) -> f.apply(acc, a),
                (v, a, b) -> f.apply(f.apply(acc, a), b),
                (v, a, b, c) -> f.apply(f.apply(f.apply(acc, a), b), c),
                (v, a, b, c, d) -> f.apply(f.apply(f.apply(f.apply(acc, a), b), c), d)
            );
        }

        static <A, M> M measure(Digit<A, M> digit) {
            return digit.match(
                (v, a) -> v,
                (v, a, b) -> v,
                (v, a, b, c) -> v,
                (v, a, b, c, d) -> v
            );
        }

        static <A, M> A head(Digit<A, M> digit) {
            return digit.match(
                (v, a) -> a,
                (v, a, b) -> a,
                (v, a, b, c) -> a,
                (v, a, b, c, d) -> a
            );
        }

        static <A, M> A last(Digit<A, M> digit) {
            return digit.match(
                (v, a) -> a,
                (v, a, b) -> b,
                (v, a, b, c) -> c,
                (v, a, b, c, d) -> d
            );
        }

        static <A, M> Digit<A, M> tail(Monoid<M> monoid, Function1<A, M> measure, Digit<A, M> digit) {
            return digit.match(
                (v, a) -> { throw new IllegalArgumentException(); },
                (v, a, b) -> create(measure, b),
                (v, a, b, c) -> create(monoid, measure, b, c),
                (v, a, b, c, d) -> create(monoid, measure, b, c, d)
            );
        }

        static <A, M> Digit<A, M> init(Monoid<M> monoid, Function1<A, M> measure, Digit<A, M> digit) {
            return digit.match(
                (v, a) -> { throw new IllegalArgumentException(); },
                (v, a, b) -> create(measure, a),
                (v, a, b, c) -> create(monoid, measure, a, b),
                (v, a, b, c, d) -> create(monoid, measure, a, b, c)
            );
        }

        static <A, M> Digit<A, M> cons(Monoid<M> monoid, Function1<A, M> measure, A x, Digit<A, M> digit) {
            return digit.match(
                (v, a) -> two(monoid.combine(measure.apply(x), v), x, a),
                (v, a, b) -> three(monoid.combine(measure.apply(x), v), x, a, b),
                (v, a, b, c) -> four(monoid.combine(measure.apply(x), v), x, a, b, c),
                (v, a, b, c, d) -> { throw new IllegalArgumentException(); }
            );
        }

        static <A, M> Digit<A, M> snoc(Monoid<M> monoid, Function1<A, M> measure, A x, Digit<A, M> digit) {
            return digit.match(
                (v, a) -> two(monoid.combine(v, measure.apply(x)), a, x),
                (v, a, b) -> three(monoid.combine(v, measure.apply(x)), a, b, x),
                (v, a, b, c) -> four(monoid.combine(v, measure.apply(x)), a, b, c, x),
                (v, a, b, c, d) -> { throw new IllegalArgumentException(); }
            );
        }

        static <A, M> FingerTree<A, M> toTree(Monoid<M> monoid, Function1<A, M> measure, Digit<A, M> digit) {
            return digit.match(
                (v, a) -> single(a),
                (v, a, b) -> deep(v, Digit.create(measure, a), nil(), Digit.create(measure, b)),
                (v, a, b, c) -> deep(v, Digit.create(monoid, measure, a, b), nil(), Digit.create(measure, c)),
                (v, a, b, c, d) -> deep(v, Digit.create(monoid, measure, a, b, c), nil(), Digit.create(measure, d))
            );
        }

        static <A, M> Digit<A, M> create(Function1<A, M> measure, A a) {
            return one(measure.apply(a), a);
        }

        static <A, M> Digit<A, M> create(Monoid<M> monoid, Function1<A, M> measure, A a, A b) {
            return two(monoid.combine(measure.apply(a), measure.apply(b)), a, b);
        }

        static <A, M> Digit<A, M> create(Monoid<M> monoid, Function1<A, M> measure, A a, A b, A c) {
            return three(monoid.combine(monoid.combine(measure.apply(a), measure.apply(b)), measure.apply(c)), a, b, c);
        }

        static <A, M> Digit<A, M> create(Monoid<M> monoid, Function1<A, M> measure, A a, A b, A c, A d) {
            return four(monoid.combine(monoid.combine(measure.apply(a), measure.apply(b)), monoid.combine(measure.apply(c), measure.apply(d))), a, b, c, d);
        }

        static <A, M> Digit<A, M> create(Monoid<M> monoid, Function1<A, M> measure, List<A> list) {
            if (list.isEmpty()) {
                throw new IllegalArgumentException();
            }
            return list.tail().foldRight(create(measure, list.head()), (a, d) -> cons(monoid, measure, a, d));
        }
    }

    @Data(flavour = Flavour.Javaslang)
    public interface FingerTree<A, M> {
        <X> X match(
            Function0<X> nil,
            @FieldNames({"a"})
            Function1<A, X> single,
            @FieldNames({"v", "pr", "m", "sf"})
            Function4<M, Digit<A, M>, FingerTree<Node<A, M>, M>, Digit<A, M>, X> deep
        );

        default <T> T foldRight(Function2<T, A, T> f, T acc) {
            return match(
                () -> acc,
                (a) -> f.apply(acc, a),
                (v, pr, m, sf) -> pr.foldRight(f, m.foldRight((t, elt) -> elt.foldRight(f, t), sf.foldRight(f, acc)))
            );
        }

        default <T> T foldLeft(Function2<T, A, T> f, T acc) {
            return match(
                () -> acc,
                (a) -> f.apply(acc, a),
                (v, pr, m, sf) -> sf.foldLeft(f, m.foldLeft((t, elt) -> elt.foldLeft(f, t), pr.foldLeft(f, acc)))
            );
        }

        static <A, M> M measure(Monoid<M> monoid, Function1<A, M> measure, FingerTree<A, M> t) {
            return t.match(
                monoid::zero,
                measure,
                (v, pr, m, sf) -> v
            );
        }

        static <A, M> FingerTree<Node<A, M>, M> cons(Monoid<M> monoid, FingerTree<Node<A, M>, M> t, Node<A, M> a) {
            return cons(monoid, Node::measure, t, a);
        }

        static <A, M> FingerTree<A, M> cons(Monoid<M> monoid, Function1<A, M> measure, FingerTree<A, M> t, A a) {
            return t.match(
                () -> single(a),
                (b) -> create(monoid, Digit.create(measure, a), nil(), Digit.create(measure, b)),
                (v, pr, m, sf) -> pr.match(
                    (v1, b) -> deep(monoid.combine(measure.apply(a), v1), Digit.cons(monoid, measure, a, pr), m, sf),
                    (v1, b, c) -> deep(monoid.combine(measure.apply(a), v1), Digit.cons(monoid, measure, a, pr), m, sf),
                    (v1, b, c, d) -> deep(monoid.combine(measure.apply(a), v1), Digit.cons(monoid, measure, a, pr), m, sf),
                    (v1, b, c, d, e) -> create(monoid, Digit.create(monoid, measure, a, b), cons(monoid, m, Node.create(monoid, measure, c, d, e)), sf)
                )
            );
        }

        static <A, M> FingerTree<Node<A, M>, M> snoc(Monoid<M> monoid, FingerTree<Node<A, M>, M> t, Node<A, M> a) {
            return snoc(monoid, Node::measure, t, a);
        }

        static <A, M> FingerTree<A, M> snoc(Monoid<M> monoid, Function1<A, M> measure, FingerTree<A, M> t, A a) {
            return t.match(
                () -> single(a),
                (b) -> create(monoid, Digit.create(measure, b), nil(), Digit.create(measure, a)),
                (v, pr, m, sf) -> sf.match(
                    (v1, b) -> deep(monoid.combine(v1, measure.apply(a)), pr, m, Digit.snoc(monoid, measure, a, sf)),
                    (v1, b, c) -> deep(monoid.combine(v1, measure.apply(a)), pr, m,  Digit.snoc(monoid, measure, a, sf)),
                    (v1, b, c, d) -> deep(monoid.combine(v1, measure.apply(a)), pr, m,  Digit.snoc(monoid, measure, a, sf)),
                    (v1, b, c, d, e) -> create(monoid, pr, snoc(monoid, m,  Node.create(monoid, measure, b, c, d)), Digit.create(monoid, measure, e, a))
                )
            );
        }

        static <A, M> FingerTree<A, M> create(Monoid<M> monoid, Digit<A, M> pr, FingerTree<Node<A, M>, M> m, Digit<A, M> sf) {
            return deep(monoid.combine(monoid.combine(Digit.measure(pr), measure(monoid, Node::measure, m)), Digit.measure(sf)), pr, m, sf);
        }

        static <A, M> FingerTree<A, M> create(Monoid<M> monoid, Function1<A, M> measure, List<A> list) {
            return list.foldRight(nil(), (a, t) -> cons(monoid, measure, t, a));
        }
    }

    private FingerTreeModule(Monoid<M> monoid, Function1<A, M> measure) {
        this.monoid = monoid;
        this.measure = measure;
    }

    public static <A, M> FingerTreeModule<A, M> create(Monoid<M> monoid, Function1<A, M> measure) {
        return new FingerTreeModule<>(monoid, measure);
    }

    public FingerTree<A, M> empty() {
        return nil();
    }

    public FingerTree<A, M> singleton(A a) {
        return single(a);
    }

    public boolean isEmpty(FingerTree<A, M> t) {
        return t.match(
            () -> true,
            (a) -> false,
            (v, pr, m, sf) -> false
        );
    }

    /*---------------------------------*/
    /*          cons / snoc            */
    /*---------------------------------*/

    public FingerTree<A, M> cons(FingerTree<A, M> t, A a) {
        return FingerTree.cons(monoid, measure, t, a);
    }

    public FingerTree<A, M> snoc(FingerTree<A, M> t, A a) {
        return FingerTree.snoc(monoid, measure, t, a);
    }

    /*---------------------------------*/
    /*      head / tail / etc.         */
    /*---------------------------------*/
    @Data(flavour = Flavour.Javaslang)
    public interface View<A, T> {
        <X> X match(
            Function0<X> vnil,
            @FieldNames({"a", "t"})
            Function2<A, T, X> vcons
        );

        static <A, M> View<Node<A, M>, FingerTree<Node<A, M>, M>> left(Monoid<M> monoid, FingerTree<Node<A, M>, M> t) {
            return left(monoid, Node::measure, t);
        }

        static <A, M> View<A, FingerTree<A, M>> left(Monoid<M> monoid, Function1<A, M> measure, FingerTree<A, M> t) {
            return t.match(
                () -> vnil(),
                (a) -> vcons(a, nil()),
                (v, pr, m, sf) -> pr.match(
                    (v1, a) -> vcons(a, left(monoid, m).match(
                        () -> Digit.toTree(monoid, measure, sf),
                        (a1, t1) -> FingerTree.create(monoid, Node.toDigit(a1), t1, sf)
                    )),
                    (v1, a, b) -> vcons(Digit.head(pr), FingerTree.create(monoid, Digit.tail(monoid, measure, pr), m, sf)),
                    (v1, a, b, c) -> vcons(Digit.head(pr), FingerTree.create(monoid, Digit.tail(monoid, measure, pr), m, sf)),
                    (v1, a, b, c, d) -> vcons(Digit.head(pr), FingerTree.create(monoid, Digit.tail(monoid, measure, pr), m, sf))
                )
            );
        }

        static <A, M> View<Node<A, M>, FingerTree<Node<A, M>, M>> right(Monoid<M> monoid, FingerTree<Node<A, M>, M> t) {
            return right(monoid, Node::measure, t);
        }

        static <A, M> View<A, FingerTree<A, M>> right(Monoid<M> monoid, Function1<A, M> measure, FingerTree<A, M> t) {
            return t.match(
                () -> vnil(),
                (a) -> vcons(a, nil()),
                (v, pr, m, sf) -> pr.match(
                    (v1, a) -> vcons(a, right(monoid, m).match(
                        () -> Digit.toTree(monoid, measure, pr),
                        (a1, t1) -> FingerTree.create(monoid, pr, t1, Node.toDigit(a1))
                    )),
                    (v1, a, b) -> vcons(Digit.last(sf), FingerTree.create(monoid, pr, m, Digit.init(monoid, measure, sf))),
                    (v1, a, b, c) -> vcons(Digit.last(sf), FingerTree.create(monoid, pr, m, Digit.init(monoid, measure, sf))),
                    (v1, a, b, c, d) -> vcons(Digit.last(sf), FingerTree.create(monoid, pr, m, Digit.init(monoid, measure, sf)))
                )
            );
        }
    }

    public A head(FingerTree<A, M> t) {
        return t.match(
            () -> { throw new NoSuchElementException(); },
            (a) -> a,
            (v, pr, m, sf) -> Digit.head(pr)
        );
    }

    public A last(FingerTree<A, M> t) {
        return t.match(
            () -> { throw new NoSuchElementException(); },
            (a) -> a,
            (v, pr, m, sf) -> Digit.last(sf)
        );
    }

    public FingerTree<A, M> tail(FingerTree<A, M> t) {
        return View.left(monoid, measure, t).match(
            () -> { throw new NoSuchElementException(); },
            (a, t1) -> t1
        );
    }

    public FingerTree<A, M> init(FingerTree<A, M> t) {
        return View.right(monoid, measure, t).match(
            () -> { throw new NoSuchElementException(); },
            (a, t1) -> t1
        );
    }
}
