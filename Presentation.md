---
marp: true
title: Findings, Interesting Topics About FP & Scala with Cats
theme: default
class:
  - lead
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
  - TODO
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
# Semigroups & Applicatives

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
