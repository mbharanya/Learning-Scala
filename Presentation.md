---
marp: true
title: Findings, Interesting Topics About FP & Scala with Cats
theme: default
class:
  - invert
paginate: true
size: 4K
auto-scaling: code
---
# Findings, Interesting Topics About FP & Scala with :cat2:
---

# Content
- Type Classes
- Monads
- Monad Transformers
- Applicatives
- Monoids
- Kleislis
- ADTs
---

# Terminology
- Pure
  - `A => B`
- Effectful == monadic
  - `F[A] => F[B]`
  - Option models the effect of optionality
  - Future models latency as an effect
  - Try abstracts the effect of failures (manages exceptions as effects)
- https://alvinalexander.com/scala/what-effects-effectful-mean-in-functional-programming
  <!--  _when a function returns an A, that A has already been fully evaluated; but if that function returns F[A] instead, that result has not already been fully evaluated, the A is still inside F[A] waiting to be evaluated_ -->
---

# Type Classes
```scala
trait Showable[A] {
  def show(a: A): String
}
```
- Provides methods for a Type `A`
## Advantages:
- ad hoc polymorphism
- Compile-time safety:
```scala
java.util.UUID.randomUUID().show
> could not find implicit value for parameter showable: Showable[java.util.UUID]
```

---

![bg fit](https://static.existentialcomics.com/comics/other/monadsForDinner.jpg)

---
# Monads
- anything with a constructor and a `flatMap` method
- mechanism for sequencing computations
![](http://adit.io/imgs/functors/monad_nothing.png)
---
## Benefits of Monadic Design
- Avoid boilerplate code
  - `Option`
- Seperation of concern
  - `Option` handles 'coping mechanism' 

- Every monad is also a functor 
  - rely on both `flatMap` and `map` to sequence computations
- `for` comprehensions

--- 
```scala
trait Monad[F[_]] {
  def pure[A](a: A): F[A] // wrap it

  def flatMap[A, B](value: F[A])(func: A => F[B]): F[B] // from monadic with a non-monadic value

  def map[A, B](value: F[A])(func: A => B): F[B] =
    flatMap(value)(a => pure(func(a))) // from monadid
}
```
![50% center](https://1.bp.blogspot.com/-f5T38j9evCk/VZ54cIBwUeI/AAAAAAAABLY/3bvMZaQ4HCY/s640/nzfiq.jpg)

---

## Cool Monads from scalaz
- `State`
  - TODO more research
- `Undo`
  - [supporting undo , redo and hput to push the last state on history](http://hackage.haskell.org/package/Hedi-0.1.1/docs/Undo.html)
- `Id`
  - Write functions that combine Monadic & Non-Monadic values
  - Switch between async / sync for testing
      - Prod: `Future[String]`
      - Test: `Id[String]`
---
# Monad Transformers
- Avoid nested for comprehensions
- Compose `Option` `Future`
- Confusion on how `liftM` works
  - TODO example here
---

# Functor
- anything with a `.map` method
- single argument functions are also functors  
---
- Can be composed (first do this, then that)
```scala
val listOption = List(Some(1), None, Some(2))
// listOption: List[Option[Int]] = List(Some(1), None, Some(2))

// Through Functor#compose
Functor[List].compose[Option].map(listOption)(_ + 1)
// res1: List[Option[Int]] = List(Some(2), None, Some(3))
```
---
- Laws:
  - Composition: `fa.map(f).map(g)` is `fa.map(f.andThen(g))`
  - Identity: Mapping with the identity function is a no-op
    `fa.map(x => x) = fa`
- Allow lifting pure to effectful function
  ```scala
   def lift[A, B](f: A => B): F[A] => F[B] = fa => map(fa)(f)
  ```

---

```scala
// Example implementation for Option
implicit val functorForOption: Functor[Option] = new Functor[Option] {
  def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa match {
    case None    => None
    case Some(a) => Some(f(a))
  }
}
```
---

# Applicatives
- Applicative extends `Functor` with an `ap` and `pure` method.
- Wrap functions in Contexts!
- mapN to apply for different airities
- avoid making unnecessary claims about dependencies between computations
```scala
import cats.implicits._

val f: (Int, Char) => Double = (i, c) => (i + c).toDouble

val int: Option[Int] = Some(5)
val char: Option[Char] = Some('a')
int.map(i => (c: Char) => f(i, c)) // what now?
```
- `ap` unpacks both Monads, and then applies the function to the value:
![](https://files.slack.com/files-pri/T028396QP-FQVF69EQY/applicative_just.png)


---

# Monoids
```scala
trait Monoid[A] {
  def combine(x: A, y: A): A
  def empty: A
}
```
Examples of Monoids:
- Ints, with the zero being 0 and the operator being +.
- Ints, with the zero being 1 and the operator being *.
- Lists, with the zero being Nil and the operator being ++.
- Strings, with the zero being "" and the operator being +.

## Semigroup
- Just the combine part of the Monoid
---

# Kleisli
```scala
val f: Int => Task[String] = ???
val fKleisli: Kleisli[Task, Int, String] = ???
```
Like a monad Transformer for function composition.
--> The advantage is you can do it within the context of a Future / Task, etc.

`andThenK` does automatic lifting.

Call `run` to extract again.

---

```scala
import cats.data._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cats.instances.future._

object KleisliTest extends App {
  val getNumberFromDb: Unit => Future[Int]  = _ => Future.successful(2)
  val processNumber: Int => Future[Int]     = num => Future.successful(num * 2)
  val persistToDb: Int => Future[Boolean]   = _ => Future.successful(true)

  val kleisliCombo: Kleisli[Future, Unit, Boolean] = 
    Kleisli(getNumberFromDb)
    andThen processNumber
    andThen persistToDb

  val unpacked: Unit => Future[Boolean] = kleisliCombo.run

  unpacked().map(println)
}
```
---
# Algebraic Data Types
- A sum type consisting of various subtypes of other sum and product types.
- Analyze values of algebraic data with pattern matching.
---
## Product Type
```scala
case class Student(enrolled: Boolean, age: Byte)
// Students consists of a Boolean AND Byte
// 2 * 256 = 258
```
--- 
## Sum Type
```scala
sealed trait Color 
case object Red extends Color
case object Blue extends Color
case object Green extends Color
// Color can be Red OR Blue OR Green
// 1 + 1 + 1 = 3 distinct possible values
```
--- 
# Example
![bg 70% right](https://upload.wikimedia.org/wikipedia/commons/f/f4/Florida_Box_Turtle_Digon3_re-edited.jpg)
- Either
  - move forward X meters
  - rotate Y degrees
--- 
## Naive implemenation:
```scala
final case class Command(label: String, meters: Option[Int], degrees: Option[Int])
```
What are the issues?

---

## Hybrid type
![bg opacity:0.6](https://www.toyota.com/imgix/responsive/images/mlp/colorizer/2020/prius/8X7/1.png?bg=fff&fm=webp)
```scala
sealed trait Option[+A]  
case object None extends Option[Nothing] 
case class Some[A](a: A) extends Option[A]
type TwoBooleansOrNone = Option[(Boolean, Boolean)]
// BooleanOrNone can be None OR Some(Boolean AND Boolean)
// 1 + (2 * 2) = 5 distinct possible values
```
---
## Illegal states
```scala
Command("foo", None, None)
Command("bar", Some(1), Some(2))
```
--- 
## Reworked
```scala
sealed abstract class Command extends Product with Serializable

object Command {
  final case class Move(meters: Int) extends Command
  final case class Rotate(degrees: Int) extends Command
}
```
---
## Bonus: Pattern Matching
```scala
def print(cmd: Command) = cmd match {
  case Command.Move(dist)    => println(s"Moving by ${dist}m")
  case Command.Rotate(angle) => println(s"Rotating by ${angle}°")
}
```