import cats.{Foldable, Monoid}
import cats.instances.int._
import cats.syntax.semigroup._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cats.implicits._
import cats.instances._ // for Monoid

import scala.collection.immutable
import scala.concurrent.Future // for |+|

object Ch9FoldMap extends App {
  def foldMap[A, B: Monoid](seq: Vector[A])(func: A => B): B = seq.map(func).foldLeft(Monoid[B].empty)(_ combine _)


  foldMap(Vector(1, 2, 3))(identity)
  // res2: Int = 6


  import cats.instances.string._ // for Monoid
  // Mapping to a String uses the concatenation monoid:
  foldMap(Vector(1, 2, 3))(_.toString + "! ")
  // res4: String = "1! 2! 3! "

  // Mapping over a String to produce a String:
  foldMap("Hello world!".toVector)(_.toString.toUpperCase) // res6: String = HELLO WORLD!


  def parallelFoldMap[A, B: Monoid]
  (values: Vector[A])
  (func: A => B): Future[B] = {
    val numCores = Runtime.getRuntime.availableProcessors
    val groupSize = (1.0 * values.size / numCores).ceil.toInt
    val batches: Seq[Vector[A]] = values.grouped(groupSize).toVector
    val futures: Vector[Future[B]] = batches.map(b => Future(foldMap(b)(func))).toVector
    Future.sequence(futures) map { iterable =>
      iterable.foldLeft(Monoid[B].empty)(_ |+| _)
    }
  }

  def catsParallelFoldMap[A, B: Monoid]
  (values: Vector[A])
  (func: A => B): Future[B] = {
    val numCores = Runtime.getRuntime.availableProcessors
    val groupSize = (1.0 * values.size / numCores).ceil.toInt
    values
      .grouped(groupSize)
      .toVector
      .traverse(group => Future(group.foldMap(func)))
      .map(_.combineAll)
  }

}
