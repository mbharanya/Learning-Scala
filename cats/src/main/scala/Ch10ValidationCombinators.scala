import cats.{FlatMap, Semigroup}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._

object Ch10ValidationCombinators extends App {

  sealed trait Predicate[E, A] {
    def and(that: Predicate[E, A]): Predicate[E, A] =
      And(this, that)

    def apply(a: A)(implicit s: Semigroup[E]): Validated[E, A] = this match {
      case Pure(func) =>
        func(a)
      case And(left, right) => {
        val tuple = (left(a), right(a))
        tuple.mapN((_, _) => a)
      }
      case Or(left, right) => {
        left(a) match {
          case Valid(a) => Valid(a)
          case Invalid(b) => right(a) match {
            case Valid(a) => Valid(a)
            case Invalid(c) => Invalid(b |+| c)
          }

        }
      }
    }

  }

  sealed trait Check[E, A, B] {
    def apply(in: A)(implicit s: Semigroup[E]): Validated[E, B]

    //    def map[C](f: B => C): Check[E, A, C] =
    //      Map[E, A, B, C](this, f)

    def flatMap[C](f: B => Check[E, A, C]) =
      FlatMap[E, A, B, C](this, f)

    def andThen[C](that: Check[E, B, C]): Check[E, A, C] =
      AndThen[E, A, B, C](this, that)
  }


  final case class AndThen[E, A, B, C](
                                        check1: Check[E, A, B],
                                        check2: Check[E, B, C]) extends Check[E, A, C] {
    def apply(a: A)(implicit s: Semigroup[E]): Validated[E, C] = check1(a).withEither(_.flatMap(b => check2(b).toEither))
  }

  final case class FlatMap[E, A, B, C](check: Check[E, A, B], func: B => Check[E, A, C]) extends Check[E, A, C] {
    def apply(a: A)(implicit s: Semigroup[E]): Validated[E, C] = check(a).withEither(_.flatMap(b => func(b)(a).toEither))
  }

  final case class Map[E, A, B, C](check: Check[E, A, B], func: B => C) extends Check[E, A, C] {
    def apply(in: A)(implicit s: Semigroup[E]): Validated[E, C] = check(in).map(func)
  }

  final case class And[E, A](
                              left: Predicate[E, A],
                              right: Predicate[E, A]) extends Predicate[E, A]

  final case class Or[E, A](left: Predicate[E, A], right: Predicate[E, A]) extends Predicate[E, A]

  final case class Pure[E, A](func: A => Validated[E, A]) extends Predicate[E, A]

}
