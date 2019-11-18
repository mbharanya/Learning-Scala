# Index
- Type Classes
- Monads
- Monad Transformers
- Applicatives
- Monoids
- Kleislis
- ADTs



# Type Classes
## Type Class Pattern
_A type class is a trait with at least one type variable. The type variables specify the concrete types the type class instances are defined for. Methods in the trait usually use the type variables._
```scala
trait ExampleTypeClass[A] {
  def doSomething(in: A): Foo
}
```
# Monads
## Readers & Writers
## Identity Monad
_Identity Monad allows us to write functions that work with monadic and non-monadic values and it is very powerful because we can wrap values into "effect" or "computational context" in production and remove them from "effect" or "computational context" for test using Identity Monad. For example, we can run code asynchronously in the production using the Future and synchronously in the test using the Identity Monad and easily switch between asynchronous and synchronous world._

# Monad Transformers
I just stumbled about the issue that EitherT is not covariant.
I started out by converting our errors from `String` to a nicer Error type (we did String matching before, to check the type of the error):

```scala
  def authenticate(email: String, plainPassword: String): EitherFT[User] =
    for {
      user <- EitherT(
        userRepository
          .findFull(email)
          .map(
            _.flatMap {
              case (user, hashed) =>
                Try(if (plainPassword.equalsHashed(hashed)) user.some else None).getOrElse(None)
            }.toRightDisjunction("Invalid email or password.")
          )
      )
    ... 
    } yield user
```
I introduced some types:
```
sealed trait EmailError { val message: String }
case class InvalidEmailOrPasswordError(override val message: String)                    extends EmailError
...
```
So `EitherFT[User]` must become `EitherT[Future, EmailError, User]`
And changed the disjunction to
```
.toRightDisjunction(InvalidEmailOrPasswordError("Invalid email or password."))
```
Now we have a proper type. Just one problem, EitherT is not actually covariant (reasons follow), so `InvalidEmailOrPasswordError` is not allowed to be interpreted as an `EmailError`

To solve this, you can use the method `.widen[EmailError, User]`, which will basically make EitherT covariant for all intents and purposes.

I haven't found any usages of `.widen` in our code, so that's why I'm sharing this here.
Background: https://typelevel.org/blog/2018/09/29/monad-transformer-variance.html

# Applicative Functors
Writing applicative code allows you to avoid making unnecessary claims about dependencies between computations—claims that similar monadic code would commit you to. A sufficiently smart library or compiler could in principle take advantage of this fact.
```scala
case class Foo(s: Symbol, n: Int)

val maybeFoo = for {
  s <- maybeComputeS(whatever)
  n <- maybeComputeN(whatever)
} yield Foo(s, n)
```
desugared:
```scala
val maybeFoo = maybeComputeS(whatever).flatMap(
  s => maybeComputeN(whatever).map(n => Foo(s, n))
)
```

The applicative version (using Scalaz) looks like this:
```scala
val maybeFoo = (maybeComputeS(whatever) |@| maybeComputeN(whatever))(Foo(_, _))
```

Example vox.buy.worker.controller.delete.DeleteResourcesMessageHandler
```scala
  override def handle(entity: DeleteResourcesMessage, request: SqsdRequest): Future[Boolean] = {
    val keysToDelete: List[S3Key] = entity.urls.flatMap { urlStr =>
      Try {
        val url = new URL(urlStr)
        (S3Bucket(url) |@| S3Key(url)) { (bucket, key) =>
          if (bucket == store.bucket) {
            key.some
          } else {
            warn(s"can not delete a resource $url from unknown bucket: $bucket")
            none
          }
        }.flatten.toList
      }.getOrElse(Nil)
    }
  }
```

```scala
  final def |@|[B](fb: F[B]) = new ApplicativeBuilder[F, A, B] {
    val a: F[A] = self
    val b: F[B] = fb
  }
```

## Monoids
# Functors
# ADTs
Let’s imagine a very simple language in which you can only give the following instructions:  

- move forward X meters
- rotate Y degrees
A naïve implementation could be:

```scala
final case class Command(label: String, meters: Option[Int], degrees: Option[Int])
```
This is problematic, however, since it allows so many illegal states to be represented. For example:
```scala
Command("foo", None, None)
Command("bar", Some(1), Some(2))
```
By reworking our type to a slightly more involved ADT, we get rid of these:
```scala
sealed abstract class Command extends Product with Serializable

object Command {
  final case class Move(meters: Int) extends Command
  final case class Rotate(degrees: Int) extends Command
}
```
It’s now impossible to create a value that makes no sense - either you move forward by X meters, or you rotate by Y degrees, nothing else.

This type also has the advantage of being very pattern match friendly:
```scala
def print(cmd: Command) = cmd match {
  case Command.Move(dist)    => println(s"Moving by ${dist}m")
  case Command.Rotate(angle) => println(s"Rotating by ${angle}°")
}
```
https://nrinaudo.github.io/scala-best-practices/definitions/adt.html
## Kleisli
The abstract concept of composing functions of type A => F[B] has a name: a Kleisli.
Kleisli is just another name for ReaderT

Kleisli enables composition of functions that return a monadic value.
One of the best properties of functions is that they compose:  
given a function `A => B` and a function `B => C`, we can combine them to create a new function `A => C`
```scala
val twice: Int => Int =
  x => x * 2

val countCats: Int => String =
  x => if (x == 1) "1 cat" else s"$x cats"

val twiceAsManyCats: Int => String =
  twice andThen countCats // equivalent to: countCats compose twice
```

Sometimes, our functions will need to return monadic values. For instance, consider the following set of functions.
```scala
val parse: String => Option[Int] =
  s => if (s.matches("-?[0-9]+")) Some(s.toInt) else None

val reciprocal: Int => Option[Double] =
  i => if (i != 0) Some(1.0 / i) else None
```
Here we can't use `compose` or `andThen`, so we need to use Kleisli:
`Kleisli[F[_], A, B]` is just a wrapper around the function `A => F[B]`
```scala
import cats.FlatMap
import cats.implicits._

final case class Kleisli[F[_], A, B](run: A => F[B]) {
  def compose[Z](k: Kleisli[F, Z, A])(implicit F: FlatMap[F]): Kleisli[F, Z, B] =
    Kleisli[F, Z, B](z => k.run(z).flatMap(run))
}

// Bring in cats.FlatMap[Option] instance
import cats.implicits._

val parse: Kleisli[Option,String,Int] =
  Kleisli((s: String) => if (s.matches("-?[0-9]+")) Some(s.toInt) else None)

val reciprocal: Kleisli[Option,Int,Double] =
  Kleisli((i: Int) => if (i != 0) Some(1.0 / i) else None)

val parseAndReciprocal: Kleisli[Option,String,Double] =
  reciprocal.compose(parse)
```
# Trampolining and stack safety in Scala
```scala
def even[A](lst: List[A]): Boolean = {
  lst match {
    case Nil => true
    case x :: xs => odd(xs)
  }
}

def odd[A](lst: List[A]): Boolean = {
  lst match {
    case Nil => false
    case x :: xs => even(xs)
  }
}

even((0 to 1000000).toList) // blows the stack
```

The Scala compiler is able to optimize a specific kind of tail call known as a self-recursive call:
```scala
def gcd(a: Int, b: Int): Int =
  if (b == 0) a else gcd(b, a % b)
```