# EitherT is not covariant
I just stumbled about the issue that EitherT is not covariant.
I started out by converting our errors from `String` to a nicer Error type (we did String matching before, to check the type of the error):

```
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

# Other
- Is the `Any` type dangerous? Seems like bad practice to use it - ever

- Varags are done with `*`
  Example: 
  ```scala
  // calculate the sum of all the numbers passed to the method
  def sum(args: Int*): Int = args.fold(0)(_+_)
  ```

- `AnyRef` represents reference types. All non-value types are defined as reference types. Every user-defined type in Scala is a subtype of `AnyRef`. If Scala is used in the context of a Java runtime environment, `AnyRef` corresponds to `java.lang.Object`.

- `require` throws `IllegalArgumentException` and `assert` throws `AssertionError`.

- Currying  
_Methods may define multiple parameter lists. When a method is called with a fewer number of parameter lists, then this will yield a function taking the missing parameter lists as its arguments._

- 1. A Functor is a structure with a map function.
  2. A Monad is a structure with a flatMap function.


- `case object`  
  is a singleton, no constructor, can be used for pattern matching

https://youtu.be/m40YOZr1nxQ

# Selective Functions
Figure out which effects a program will have via the type system.
https://youtu.be/gs7MNm6YMX4