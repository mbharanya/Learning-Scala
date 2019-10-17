# Type Classes
# Readers & Writers
# Monads
## Identity Monad
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

# Semigroupal and Applicative
|+| |@|

## Monoids
# Functors
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
