import cats.kernel.Monoid
import cats.implicits._

object SuperAdder extends App {
  def add[A: Monoid](items: List[A]): A =
    items.foldLeft(Monoid[A].empty)(_ |+| _)

  println(add(List(1, 2, 3)))
}


