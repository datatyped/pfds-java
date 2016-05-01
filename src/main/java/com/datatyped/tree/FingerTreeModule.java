package com.datatyped.tree;

import javaslang.*;
import javaslang.algebra.Monoid;
import javaslang.collection.Foldable;
import javaslang.collection.List;
import javaslang.control.Option;
import org.derive4j.Data;
import org.derive4j.FieldNames;
import org.derive4j.Flavour;

import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.datatyped.tree.Nodes.*;
import static com.datatyped.tree.Digits.*;
import static com.datatyped.tree.FingerTrees.*;

public class FingerTreeModule<A, M> {
    private final Monoid<M> monoid;
    private final Function1<A, M> measure;

    @Data(flavour = Flavour.Javaslang)
    public interface Node<A, M> extends Foldable<A> {
        <X> X match(
            @FieldNames({"v", "a", "b"})
            Function3<M, A, A, X> node2,
            @FieldNames({"v", "a", "b", "c"})
            Function4<M, A, A, A, X> node3
        );

        default <U> U foldLeft(U zero, BiFunction<? super U, ? super A, ? extends U> combine) {
            return match(
                (v, a, b) -> combine.apply(combine.apply(zero, a), b),
                (v, a, b, c) -> combine.apply(combine.apply(combine.apply(zero, a), b), c)
            );
        }

        default <U> U foldRight(U zero, BiFunction<? super A, ? super U, ? extends U> combine) {
            return match(
                (v, a, b) -> combine.apply(a, combine.apply(b, zero)),
                (v, a, b, c) -> combine.apply(a, combine.apply(b, combine.apply(c, zero)))
            );
        }

        default A reduceLeft(BiFunction<? super A, ? super A, ? extends A> op) {
            return match(
                (v, a, b) -> op.apply(a, b),
                (v, a, b, c) -> op.apply(op.apply(a, b), c)
            );
        }

        default Option<A> reduceLeftOption(BiFunction<? super A, ? super A, ? extends A> op) {
            return Option.some(reduceLeft(op));
        }

        default A reduceRight(BiFunction<? super A, ? super A, ? extends A> op) {
            return match(
                (v, a, b) -> op.apply(a, b),
                (v, a, b, c) -> op.apply(a, op.apply(b, c))
            );
        }

        default Option<A> reduceRightOption(BiFunction<? super A, ? super A, ? extends A> op) {
            return Option.some(reduceRight(op));
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
    public interface Digit<A, M> extends Foldable<A> {
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

        default <U> U foldLeft(U zero, BiFunction<? super U, ? super A, ? extends U> combine) {
            return match(
                (v, a) -> combine.apply(zero, a),
                (v, a, b) -> combine.apply(combine.apply(zero, a), b),
                (v, a, b, c) -> combine.apply(combine.apply(combine.apply(zero, a), b), c),
                (v, a, b, c, d) -> combine.apply(combine.apply(combine.apply(combine.apply(zero, a), b), c), d)
            );
        }

        default <U> U foldRight(U zero, BiFunction<? super A, ? super U, ? extends U> combine) {
            return match(
                (v, a) -> combine.apply(a, zero),
                (v, a, b) -> combine.apply(a, combine.apply(b, zero)),
                (v, a, b, c) -> combine.apply(a, combine.apply(b, combine.apply(c, zero))),
                (v, a, b, c, d) -> combine.apply(a, combine.apply(b, combine.apply(c, combine.apply(d, zero))))
            );
        }

        default A reduceLeft(BiFunction<? super A, ? super A, ? extends A> op) {
            return match(
                (v, a) -> a,
                (v, a, b) -> op.apply(a, b),
                (v, a, b, c) -> op.apply(op.apply(a, b), c),
                (v, a, b, c, d) -> op.apply(op.apply(op.apply(a, b), c), d)
            );
        }

        default Option<A> reduceLeftOption(BiFunction<? super A, ? super A, ? extends A> op) {
            return Option.some(reduceLeft(op));
        }

        default A reduceRight(BiFunction<? super A, ? super A, ? extends A> op) {
            return match(
                (v, a) -> a,
                (v, a, b) -> op.apply(a, b),
                (v, a, b, c) -> op.apply(a, op.apply(b, c)),
                (v, a, b, c, d) -> op.apply(a, op.apply(b, op.apply(c, d)))
            );
        }

        default Option<A> reduceRightOption(BiFunction<? super A, ? super A, ? extends A> op) {
            return Option.some(reduceRight(op));
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
                (v, a, b) -> deep(v, create(measure, a), nil(), create(measure, b)),
                (v, a, b, c) -> deep(v, create(monoid, measure, a, b), nil(), create(measure, c)),
                (v, a, b, c, d) -> deep(v, create(monoid, measure, a, b, c), nil(), create(measure, d))
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
    public interface FingerTree<A, M> extends Foldable<A> {
        <X> X match(
            Function0<X> nil,
            @FieldNames({"a"})
            Function1<A, X> single,
            @FieldNames({"v", "pr", "m", "sf"})
            Function4<M, Digit<A, M>, FingerTree<Node<A, M>, M>, Digit<A, M>, X> deep
        );

        default <U> U foldLeft(U zero, BiFunction<? super U, ? super A, ? extends U> combine) {
            return match(
                () -> zero,
                (a) -> combine.apply(zero, a),
                (v, pr, m, sf) -> sf.foldLeft(m.foldLeft(pr.foldLeft(zero, combine), (t, a) -> a.foldLeft(t, combine)), combine)
            );
        }

        default <U> U foldRight(U zero, BiFunction<? super A, ? super U, ? extends U> combine) {
            return match(
                () -> zero,
                (a) -> combine.apply(a, zero),
                (v, pr, m, sf) -> pr.foldRight(m.foldRight(sf.foldRight(zero, combine), (a, t) -> a.foldRight(t, combine)), combine)
            );
        }

        default A reduceLeft(BiFunction<? super A, ? super A, ? extends A> op) {
            return match(
                () -> { throw new NoSuchElementException(); },
                (a) -> a,
                (v, pr, m, sf) -> sf.foldLeft(m.foldLeft(pr.reduceLeft(op), (t, a) -> a.foldLeft(t, op)), op)
            );
        }

        default Option<A> reduceLeftOption(BiFunction<? super A, ? super A, ? extends A> op) {
            return match(
                Option::none,
                Option::some,
                (v, pr, m, sf) -> Option.some(sf.foldLeft(m.foldLeft(pr.reduceLeft(op), (t, a) -> a.foldLeft(t, op)), op))
            );
        }

        default A reduceRight(BiFunction<? super A, ? super A, ? extends A> op) {
            return match(
                () -> { throw new NoSuchElementException(); },
                (a) -> a,
                (v, pr, m, sf) -> pr.foldRight(m.foldRight(sf.reduceRight(op), (a, t) -> a.foldRight(t, op)), op)
            );
        }

        default Option<A> reduceRightOption(BiFunction<? super A, ? super A, ? extends A> op) {
            return match(
                Option::none,
                Option::some,
                (v, pr, m, sf) -> Option.some(pr.foldRight(m.foldRight(sf.reduceRight(op), (a, t) -> a.foldRight(t, op)), op))
            );
        }

        static <A, M> M measure(Monoid<M> monoid, FingerTree<Node<A, M>, M> t) {
            return measure(monoid, Node::measure, t);
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
            Function0<X> nil,
            @FieldNames({"a", "t"})
            Function2<A, T, X> cons
        );

        static <A, M> View<Node<A, M>, FingerTree<Node<A, M>, M>> left(Monoid<M> monoid, FingerTree<Node<A, M>, M> t) {
            return left(monoid, Node::measure, t);
        }

        static <A, M> View<A, FingerTree<A, M>> left(Monoid<M> monoid, Function1<A, M> measure, FingerTree<A, M> t) {
            return t.match(
                () -> Views.nil(),
                (a) -> Views.cons(a, nil()),
                (v, pr, m, sf) -> pr.match(
                    (v1, a) -> Views.cons(a, left(monoid, m).match(
                        () -> Digit.toTree(monoid, measure, sf),
                        (a1, t1) -> FingerTree.create(monoid, Node.toDigit(a1), t1, sf)
                    )),
                    (v1, a, b) -> Views.cons(Digit.head(pr), FingerTree.create(monoid, Digit.tail(monoid, measure, pr), m, sf)),
                    (v1, a, b, c) -> Views.cons(Digit.head(pr), FingerTree.create(monoid, Digit.tail(monoid, measure, pr), m, sf)),
                    (v1, a, b, c, d) -> Views.cons(Digit.head(pr), FingerTree.create(monoid, Digit.tail(monoid, measure, pr), m, sf))
                )
            );
        }

        static <A, M> View<Node<A, M>, FingerTree<Node<A, M>, M>> right(Monoid<M> monoid, FingerTree<Node<A, M>, M> t) {
            return right(monoid, Node::measure, t);
        }

        static <A, M> View<A, FingerTree<A, M>> right(Monoid<M> monoid, Function1<A, M> measure, FingerTree<A, M> t) {
            return t.match(
                () -> Views.nil(),
                (a) -> Views.cons(a, nil()),
                (v, pr, m, sf) -> pr.match(
                    (v1, a) -> Views.cons(a, right(monoid, m).match(
                        () -> Digit.toTree(monoid, measure, pr),
                        (a1, t1) -> FingerTree.create(monoid, pr, t1, Node.toDigit(a1))
                    )),
                    (v1, a, b) -> Views.cons(Digit.last(sf), FingerTree.create(monoid, pr, m, Digit.init(monoid, measure, sf))),
                    (v1, a, b, c) -> Views.cons(Digit.last(sf), FingerTree.create(monoid, pr, m, Digit.init(monoid, measure, sf))),
                    (v1, a, b, c, d) -> Views.cons(Digit.last(sf), FingerTree.create(monoid, pr, m, Digit.init(monoid, measure, sf)))
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

    /*---------------------------------*/
    /*            append               */
    /*---------------------------------*/
    private static <A, M> List<Node<A, M>> nodes(Monoid<M> monoid, Function1<A, M> measure, List<A> ts, Digit<A, M> sf2) {
        if (ts.isEmpty()) {
            return sf2.match(
                (v, a) -> { throw new IllegalArgumentException(); },
                (v, a, b) -> List.of(Node.create(monoid, measure, a, b)),
                (v, a, b, c) -> List.of(Node.create(monoid, measure, a, b, c)),
                (v, a, b, c, d) -> List.of(Node.create(monoid, measure, a, b), Node.create(monoid, measure, c, d))
            );
        } else {
            A a = ts.head();
            List<A> ts2 = ts.tail();
            if (ts2.isEmpty()) {
                return sf2.match(
                    (v, b) -> List.of(Node.create(monoid, measure, a, b)),
                    (v, b, c) -> List.of(Node.create(monoid, measure, a, b, c)),
                    (v, b, c, d) -> List.of(Node.create(monoid, measure, a, b), Node.create(monoid, measure, c, d)),
                    (v, b, c, d, e) -> List.of(Node.create(monoid, measure, a, b, c), Node.create(monoid, measure, d, e))
                );
            } else {
                A b = ts2.head();
                List<A> ts3 = ts2.tail();
                if (ts3.isEmpty()) {
                    return sf2.match(
                        (v, c) -> List.of(Node.create(monoid, measure, a, b, c)),
                        (v, c, d) -> List.of(Node.create(monoid, measure, a, b), Node.create(monoid, measure, c, d)),
                        (v, c, d, e) -> List.of(Node.create(monoid, measure, a, b, c), Node.create(monoid, measure, d, e)),
                        (v, c, d, e, f) -> List.of(Node.create(monoid, measure, a, b, c), Node.create(monoid, measure, d, e, f))
                    );
                } else {
                    A c = ts3.head();
                    List<A> ts4 = ts3.tail();
                    if (ts4.isEmpty()) {
                        return sf2.match(
                            (v, d) -> List.of(Node.create(monoid, measure, a, b), Node.create(monoid, measure, c, d)),
                            (v, d, e) -> List.of(Node.create(monoid, measure, a, b, c), Node.create(monoid, measure, d, e)),
                            (v, d, e, f) -> List.of(Node.create(monoid, measure, a, b, c), Node.create(monoid, measure, d, e, f)),
                            (v, d, e, f, g) -> List.of(Node.create(monoid, measure, a, b, c), Node.create(monoid, measure, d, e), Node.create(monoid, measure, f, g))
                        );
                    } else {
                        return nodes(monoid, measure, ts4, sf2).prepend(Node.create(monoid, measure, a, b, c));
                    }
                }
            }
        }
    }

    private static <A, M> FingerTree<Node<A, M>, M> append(Monoid<M> monoid, FingerTree<Node<A, M>, M> t1, List<Node<A, M>> elts, FingerTree<Node<A, M>, M> t2) {
        return append(monoid, Node::measure, t1, elts, t2);
    }

    private static <A, M> FingerTree<A, M> append(Monoid<M> monoid, Function1<A, M> measure, FingerTree<A, M> t1, List<A> elts, FingerTree<A, M> t2) {
        return t1.match(
            () -> elts.foldRight(t2, (elt, acc) -> FingerTree.cons(monoid, measure, acc, elt)),
            (x1) -> t2.match(
                () -> elts.foldLeft(t1, (acc, elt) -> FingerTree.snoc(monoid, measure, acc, elt)),
                (x2) -> FingerTree.cons(monoid, measure, elts.foldRight(t2, (elt, acc) -> FingerTree.cons(monoid, measure, acc, elt)), x1),
                (v2, pr2, m2, sf2) -> FingerTree.cons(monoid, measure, elts.foldRight(t2, (elt, acc) -> FingerTree.cons(monoid, measure, acc, elt)), x1)
            ),
            (v1, pr1, m1, sf1) -> t2.match(
                () -> elts.foldLeft(t1, (acc, elt) -> FingerTree.snoc(monoid, measure, acc, elt)),
                (x2) -> FingerTree.snoc(monoid, measure, elts.foldLeft(t1, (acc, elt) -> FingerTree.snoc(monoid, measure, acc, elt)), x2),
                (v2, pr2, m2, sf2) -> {
                    List<Node<A, M>> nodes = nodes(monoid, measure, sf1.foldRight(elts, (x, xs) -> xs.prepend(x)), pr2);
                    return FingerTree.create(monoid, pr1, append(monoid, m1, nodes, m2), sf2);
                }
            )
        );
    }

    public FingerTree<A, M> append(FingerTree<A, M> t1, FingerTree<A, M> t2) {
        return append(monoid, measure, t1, List.empty(), t2);
    }

    /*---------------------------------*/
    /*             split               */
    /*---------------------------------*/
    @Data(flavour = Flavour.Javaslang)
    public interface Split<A, T> {
        <X> X match(
            @FieldNames({"l", "a", "r"})
            Function3<T, A, T, X> split
        );

        static <A, M> Split<A, List<A>> splitDigit(Monoid<M> monoid, Function1<A, M> measure, Predicate<M> p, M i, Digit<A, M> digit) {
            return digit.match(
                (v, a) -> Splits.split(List.empty(), a, List.empty()),
                (v, a, b) -> {
                    M i1 = monoid.combine(i, (measure.apply(a)));
                    if (p.test(i1)) return Splits.split(List.empty(), a, List.of(b));
                    else return Splits.split(List.of(a), b, List.empty());
                },
                (v, a, b, c) -> {
                    M i1 = monoid.combine(i, (measure.apply(a)));
                    if (p.test(i1)) return Splits.split(List.empty(), a, List.of(b, c));
                    else {
                        M i2 = monoid.combine(i1, (measure.apply(b)));
                        if (p.test(i2)) return Splits.split(List.of(a), b, List.of(c));
                        else return Splits.split(List.of(a, b), c, List.empty());
                    }
                },
                (v, a, b, c, d) -> {
                    M i1 = monoid.combine(i, (measure.apply(a)));
                    if (p.test(i1)) return Splits.split(List.empty(), a, List.of(b, c, d));
                    else {
                        M i2 = monoid.combine(i1, (measure.apply(b)));
                        if (p.test(i2)) return Splits.split(List.of(a), b, List.of(c, d));
                        else {
                            M i3 = monoid.combine(i2, (measure.apply(c)));
                            if (p.test(i3)) return Splits.split(List.of(a, b), c, List.of(d));
                            else return Splits.split(List.of(a, b, c), d, List.empty());
                        }
                    }
                }
            );
        }

        static <A, M> FingerTree<A, M> deepLeft(Monoid<M> monoid, Function1<A, M> measure, List<A> pr, FingerTree<Node<A, M>, M> m, Digit<A, M> sf) {
            if (pr.isEmpty()) {
                return View.left(monoid, Node::measure, m).match(
                    () -> Digit.toTree(monoid, measure, sf),
                    (a, m1) -> FingerTree.create(monoid, Node.toDigit(a), m1, sf)
                );
            } else {
                return FingerTree.create(monoid, Digit.create(monoid, measure, pr), m, sf);
            }
        }

        static <A, M> FingerTree<A, M> deepRight(Monoid<M> monoid, Function1<A, M> measure, Digit<A, M> pr, FingerTree<Node<A, M>, M> m, List<A> sf) {
            if (sf.isEmpty()) {
                return View.right(monoid, Node::measure, m).match(
                    () -> Digit.toTree(monoid, measure, pr),
                    (a, m1) -> FingerTree.create(monoid, pr, m1, Node.toDigit(a))
                );
            } else {
                return FingerTree.create(monoid, pr, m, Digit.create(monoid, measure, sf));
            }
        }

        static <A, M> Split<Node<A, M>, FingerTree<Node<A, M>, M>> splitTree(Monoid<M> monoid, Predicate<M> p, M i, FingerTree<Node<A, M>, M> t) {
            return splitTree(monoid, Node::measure, p, i, t);
        }

        static <A, M> Split<A, FingerTree<A, M>> splitTree(Monoid<M> monoid, Function1<A, M> measure, Predicate<M> p, M i, FingerTree<A, M> t) {
            return t.match(
                () -> { throw new NoSuchElementException(); },
                (a) -> Splits.split (nil(), a, nil()),
                (v, pr, m, sf) -> {
                    M vpr = monoid.combine(i, Digit.measure(pr));
                    if (p.test(vpr)) {
                        return splitDigit(monoid, measure, p, i, pr).match(
                            (l, x, r) -> Splits.split(FingerTree.create(monoid, measure, l), x, deepLeft(monoid, measure, r, m, sf))
                        );
                    } else {
                        M vm = monoid.combine(vpr, FingerTree.measure(monoid, m));
                        if (p.test(vm)) {
                            return splitTree(monoid, p, vpr, m).match(
                                (ml, xs, mr) -> splitDigit(monoid, measure, p, monoid.combine(vpr, FingerTree.measure(monoid, ml)), Node.toDigit(xs)).match(
                                    (l, x, r) -> Splits.split(deepRight(monoid, measure, pr, ml, l), x, deepLeft(monoid, measure, r, mr, sf))
                                )
                            );
                        } else {
                            return splitDigit(monoid, measure, p, vm, sf).match(
                                (l, x, r) -> Splits.split(deepRight(monoid, measure, pr, m, l), x, FingerTree.create(monoid, measure, r))
                            );
                        }
                    }
                }
            );
        }
    }

    public Tuple2<FingerTree<A, M>, FingerTree<A, M>> split(Predicate<M> p, FingerTree<A, M> t) {
        if (isEmpty(t)) {
            return Tuple.of(nil(), nil());
        } else if (p.test(FingerTree.measure(monoid, measure, t))) {
            return Split.splitTree(monoid, measure, p, monoid.zero(), t).match(
                (l, x, r) -> Tuple.of(l, cons(r, x))
            );
        } else {
            return Tuple.of(t, nil());
        }
    }


}
