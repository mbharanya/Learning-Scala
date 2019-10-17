import cats.Monoid
import cats.instances.list._
import cats.instances.map._
import cats.syntax.foldable._
import cats.syntax.semigroup._ // for combineAll

object Ch11CRDTAbstractKeyValue extends App {

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

  trait KeyValueStore[F[_, _]] {
    def put[K, V](f: F[K, V])(k: K, v: V): F[K, V]

    def get[K, V](f: F[K, V])(k: K): Option[V]

    def getOrElse[K, V](f: F[K, V])(k: K, default: V): V =
      get(f)(k).getOrElse(default)

    def values[K, V](f: F[K, V]): List[V]
  }

  object KeyValueStore {
    implicit def mapKeyValueStore[K, V] = new KeyValueStore[Map] {
      override def get[K, V](f: Map[K, V])(k: K): Option[V] = f.get(k)

      override def put[K, V](f: Map[K, V])(k: K, v: V): Map[K, V] = f + (k -> v)

      override def getOrElse[K, V](f: Map[K, V])(k: K, default: V): V = f.getOrElse(k, default)

      override def values[K, V](f: Map[K, V]): List[V] = f.values.toList
    }

  }

  implicit class KvsOps[F[_, _], K, V](f: F[K, V]) {
    def put(key: K, value: V)
           (implicit kvs: KeyValueStore[F]): F[K, V] =
      kvs.put(f)(key, value)

    def get(key: K)(implicit kvs: KeyValueStore[F]): Option[V] = kvs.get(f)(key)

    def getOrElse(key: K, default: V)
                 (implicit kvs: KeyValueStore[F]): V =
      kvs.getOrElse(f)(key, default)

    def values(implicit kvs: KeyValueStore[F]): List[V] = kvs.values(f)
  }

  implicit def gcounterInstance[F[_, _], K, V]
  (implicit kvs: KeyValueStore[F], km: Monoid[F[K, V]]) =
    new GCounter[F, K, V] {
      def increment(f: F[K, V])(key: K, value: V)
                   (implicit m: Monoid[V]): F[K, V] = {
        val total = f.getOrElse(key, m.empty) |+| value
        f.put(key, total)
      }

      def merge(f1: F[K, V], f2: F[K, V])
               (implicit b: BoundedSemiLattice[V]): F[K, V] =
        f1 |+| f2

      def total(f: F[K, V])(implicit m: Monoid[V]): V =
        f.values.combineAll
    }


}
