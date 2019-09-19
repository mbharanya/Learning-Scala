# Type Classes
# Readers & Writers
# Monads
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