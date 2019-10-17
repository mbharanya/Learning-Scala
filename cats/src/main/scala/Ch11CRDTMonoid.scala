import Ch11CRDTBoundedSemiLattice.BoundedSemiLattice
import cats.Monoid
import cats.instances.list._
import cats.instances.map._
import cats.syntax.semigroup._
import cats.syntax.foldable._ // for combineAll

object Ch11CRDTMonoid extends App {

  trait BoundedSemiLattice[A] extends Monoid[A] {
    def combine(a1: A, a2: A): A

    def empty: A
  }

  implicit val boundedSemiLatticeInt: BoundedSemiLattice[Int] = new BoundedSemiLattice[Int] {
    def combine(a1: Int, a2: Int) = a1 max a2

    def empty() = 0
  }


  trait GCounter[F[_, _], K, V] {
    def increment(f: F[K, V])(k: K, v: V)
                 (implicit m: Monoid[V]): F[K, V]

    def merge(f1: F[K, V], f2: F[K, V])
             (implicit b: BoundedSemiLattice[V]): F[K, V]

    def total(f: F[K, V])
             (implicit m: Monoid[V]): V
  }

  object GCounter {
    def apply[F[_, _], K, V](implicit counter: GCounter[F, K, V]) = counter

    implicit def gcCounterMap[K, V] = new GCounter[Map, K, V] {
      override def increment(map: Map[K, V])(k: K, v: V)(implicit m: Monoid[V]): Map[K, V] = {
        val total = map.getOrElse(k, m.empty) |+| v
        map + (k -> total)
      }

      override def merge(map1: Map[K, V], map2: Map[K, V])(implicit b: BoundedSemiLattice[V]): Map[K, V] = {
        map1 |+| map2
      }

      override def total(map: Map[K, V])(implicit m: Monoid[V]): V = map.values.toList.combineAll
    }
  }


  val g1 = Map("a" -> 7, "b" -> 3)
  val g2 = Map("a" -> 2, "b" -> 5)

  val counter = GCounter[Map, String, Int]
  val merged = counter.merge(g1, g2)
  println(merged)
  // merged: Map[String,Int] = Map(a -> 7, b -> 5)
  val total = counter.total(merged)
  println(total)
  // total: Int = 12

}
