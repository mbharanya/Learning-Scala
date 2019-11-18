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

# Monads
---
## Identity Monad
- Write functions that combine Monadic & Non-Monadic values
- Switch between async / sync for testing
    - Prod: `Future[String]`
    - Test: `Id[String]`
---

# Monad Transformers

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
