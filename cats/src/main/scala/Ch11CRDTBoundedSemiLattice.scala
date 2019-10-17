object Ch11CRDTBoundedSemiLattice extends App {

  import cats.Monoid
  import cats.instances.list._ // for Monoid
  import cats.instances.map._ // for Monoid
  import cats.syntax.semigroup._ // for |+|
  import cats.syntax.foldable._ // for combineAll

  trait BoundedSemiLattice[A] extends Monoid[A] {
    def combine(a1: A, a2: A): A

    def empty: A
  }

  object BoundedSemiLattice {
    val boundedSemiLatticeInt: BoundedSemiLattice[Int] = new BoundedSemiLattice[Int] {
      def combine(a1: Int, a2: Int) = a1 max a2

      def empty() = 0
    }

    implicit def boundedSemiLatticeSet[T]: BoundedSemiLattice[Set[T]] = new BoundedSemiLattice[Set[T]] {
      def combine(a1: Set[T], a2: Set[T]) = a1 ++ a2

      def empty() = Set.empty[T]
    }

  }


  final case class GCounter[T](counters: Map[String, T]) {
    def increment(machine: String, amount: T)(implicit m: Monoid[T]) = {
      val value = amount |+| counters.getOrElse(machine, m.empty)
      GCounter(counters + (machine -> value))
    }

    def merge(that: GCounter[T])(implicit b: BoundedSemiLattice[T]): GCounter[T] =
      GCounter(this.counters combine that.counters)


    def total(implicit m: Monoid[T]): T =
      this.counters.values.toList.combineAll
  }


}
