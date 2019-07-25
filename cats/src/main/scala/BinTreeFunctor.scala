import cats.Functor
import cats.implicits._

object BinTreeFunctor extends App {

  sealed trait Tree[+A]

  final case class Branch[A](left: Tree[A], right: Tree[A])
    extends Tree[A]

  final case class Leaf[A](value: A) extends Tree[A]

  object Tree {
    def branch[A](left: Tree[A], right: Tree[A]): Tree[A] =
      Branch(left, right)

    def leaf[A](value: A): Tree[A] =
      Leaf(value)
  }

  implicit val treeFunctor: Functor[Tree] =
    new Functor[Tree] {
      def map[A, B](tree: Tree[A])(func: A => B): Tree[B] =
        tree match {
          case Branch(left, right) =>
            Branch(map(left)(func), map(right)(func))
          case Leaf(value) =>
            Leaf(func(value))
        }
    }
  val tree = Tree.leaf(100)

  Tree.leaf(100).map(_ * 2)

}
