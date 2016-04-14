package com.datatyped.tree;

import javaslang.*;
import javaslang.algebra.Monoid;
import org.derive4j.Data;
import org.derive4j.FieldNames;
import org.derive4j.Flavour;

import static com.datatyped.tree.Nodes.*;
import static com.datatyped.tree.Digits.*;
import static com.datatyped.tree.Fgs.*;
import static com.datatyped.tree.Nodes.node2;

public class FingerTree<A, M> {
    private final Monoid<M> monoid;

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

        default M measure() {
            return match(
                (v, a, b) -> v,
                (v, a, b, c) -> v
            );
        }

        static <A, M> Node<A, M> create(Monoid<M> monoid, Function1<A, M> measure, A a, A b) {
            return node2(monoid.combine(measure.apply(a), measure.apply(b)), a, b);
        }

        static <A, M> Node<Node<A, M>, M> create(Monoid<M> monoid, Node<A, M> a, Node<A, M> b) {
            return create(monoid, Node::measure, a, b);
        }

        static <A, M> Node<A, M> create(Monoid<M> monoid, Function1<A, M> measure, A a, A b, A c) {
            return node3(monoid.combine(measure.apply(a), monoid.combine(measure.apply(b), measure.apply(c))), a, b, c);
        }

        static <A, M> Node<Node<A, M>, M> create(Monoid<M> monoid, Node<A, M> a, Node<A, M> b, Node<A, M> c) {
            return create(monoid, Node::measure, a, b, c);
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

        default M measure() {
            return match(
                (v, a) -> v,
                (v, a, b) -> v,
                (v, a, b, c) -> v,
                (v, a, b, c, d) -> v
            );
        }

        static <A, M> Digit<A, M> create(Function1<A, M> measure, A a) {
            return one(measure.apply(a), a);
        }

        static <A, M> Digit<Node<A, M>, M> create(Node<A, M> a) {
            return create(Node::measure, a);
        }

        static <A, M> Digit<A, M> create(Monoid<M> monoid, Function1<A, M> measure, A a, A b) {
            return two(monoid.combine(measure.apply(a), measure.apply(b)), a, b);
        }

        static <A, M> Digit<Node<A, M>, M> create(Monoid<M> monoid, Node<A, M> a, Node<A, M> b) {
            return create(monoid, Node::measure, a, b);
        }

        static <A, M> Digit<A, M> create(Monoid<M> monoid, Function1<A, M> measure, A a, A b, A c) {
            return three(monoid.combine(monoid.combine(measure.apply(a), measure.apply(b)), measure.apply(c)), a, b, c);
        }

        static <A, M> Digit<Node<A, M>, M> create(Monoid<M> monoid, Node<A, M> a, Node<A, M> b, Node<A, M> c) {
            return create(monoid, Node::measure, a, b, c);
        }

        static <A, M> Digit<A, M> create(Monoid<M> monoid, Function1<A, M> measure, A a, A b, A c, A d) {
            return four(monoid.combine(monoid.combine(measure.apply(a), measure.apply(b)), monoid.combine(measure.apply(c), measure.apply(d))), a, b, c, d);
        }

        static <A, M> Digit<Node<A, M>, M> create(Monoid<M> monoid, Node<A, M> a, Node<A, M> b, Node<A, M> c, Node<A, M> d) {
            return create(monoid, Node::measure, a, b, c, d);
        }
    }

    @Data(flavour = Flavour.Javaslang)
    public interface Fg<A, M> {
        <X> X match(
            Function0<X> nil,
            @FieldNames({"a"})
            Function1<A, X> single,
            @FieldNames({"v", "pr", "m", "sf"})
            Function4<M, Digit<A, M>, Fg<Node<A, M>, M>, Digit<A, M>, X> deep
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

        default M measure(Monoid<M> monoid, Function1<A, M> measure) {
            return match(
                monoid::zero,
                measure,
                (v, pr, m, sf) -> v
            );
        }

        static <A, M> Fg<A, M> create(Monoid<M> monoid, Digit<A, M> pr, Fg<Node<A, M>, M> m, Digit<A, M> sf) {
            return deep(monoid.combine(monoid.combine(pr.measure(), m.measure(monoid, Node::measure)), sf.measure()), pr, m, sf);
        }
    }

    private FingerTree(Monoid<M> monoid) {
        this.monoid = monoid;
    }

    public static <A, M> FingerTree<A, M> create(Monoid<M> monoid) {
        return new FingerTree<>(monoid);
    }

    public Fg<A, M> empty() {
        return nil();
    }

    public Fg<A, M> singleton(A a) {
        return single(a);
    }

    public boolean isEmpty(Fg<A, M> t) {
        return t.match(
            () -> true,
            a -> false,
            (v, pr, m, sf) -> false
        );
    }

    /*---------------------------------*/
    /*          cons / snoc            */
    /*---------------------------------*/
    private static <A, M> Digit<A, M> cons(Monoid<M> monoid, Function1<A, M> measure, Digit<A, M> digit, A x) {
        return digit.match(
            (v, a) -> two(monoid.combine(measure.apply(x), v), x, a),
            (v, a, b) -> three(monoid.combine(measure.apply(x), v), x, a, b),
            (v, a, b, c) -> four(monoid.combine(measure.apply(x), v), x, a, b, c),
            (v, a, b, c, d) -> { throw new IllegalArgumentException(); }
        );
    }

    private static <A, M> Digit<Node<A, M>, M> cons(Monoid<M> monoid, Digit<Node<A, M>, M> digit, Node<A, M> x) {
        return cons(monoid, Node::measure, digit, x);
    }

    private static <A, M> Fg<Node<A, M>, M> cons(Monoid<M> monoid, Fg<Node<A, M>, M> t, Node<A, M> a) {
        return t.match(
            () -> single(a),
            (b) -> Fg.create(monoid, Digit.create(a), nil(), Digit.create(b)),
            (v, pr, m, sf) -> pr.match(
                (v1, b) -> deep(monoid.combine(a.measure(), v1), cons(monoid, pr, a), m, sf),
                (v1, b, c) -> deep(monoid.combine(a.measure(), v1), cons(monoid, pr, a), m, sf),
                (v1, b, c, d) -> deep(monoid.combine(a.measure(), v1), cons(monoid, pr, a), m, sf),
                (v1, b, c, d, e) -> Fg.create(monoid, Digit.create(monoid, a, b), cons(monoid, m, Node.create(monoid, c, d, e)), sf)
            )
        );
    }

    public Fg<A, M> cons(Monoid<M> monoid, Function1<A, M> measure, Fg<A, M> t, A a) {
        return t.match(
            () -> single(a),
            (b) -> Fg.create(monoid, Digit.create(measure, a), nil(), Digit.create(measure, b)),
            (v, pr, m, sf) -> pr.match(
                (v1, b) -> deep(monoid.combine(measure.apply(a), v1), cons(monoid, measure, pr, a), m, sf),
                (v1, b, c) -> deep(monoid.combine(measure.apply(a), v1), cons(monoid, measure, pr, a), m, sf),
                (v1, b, c, d) -> deep(monoid.combine(measure.apply(a), v1), cons(monoid, measure, pr, a), m, sf),
                (v1, b, c, d, e) -> Fg.create(monoid, Digit.create(monoid, measure, a, b), cons(monoid, m, Node.create(monoid, measure, c, d, e)), sf)
            )
        );
    }
}
