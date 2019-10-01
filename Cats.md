# Scala with Cats🐈

# Anatomy of a Type Class
This is used to provide
`.print` on any object that we have a Printable with that type.

```scala 
object PrintableSyntax{
  implicit class PrintableOps[A](value: A){
    def format(implicit p: Printable[A]) = p.format(value)
    def print(implicit p: Printable[A]) = println(p.format(value))
  }
}
```

# Monoids and Semigroups
Adding `int`s is closed. `(a: Int, b: Int) => Int`. 
The identity of add is 0, as 0 + a = a

If the order doesn't matter it's called associativity: 
```scala
(1 + 2) + 3
// res3: Int = 6
1 + (2 + 3)
// res4: Int = 6
```


_Formally, a monoid for a type `A` is_:
- an operation combine with type `(A,A)=>A` 
- an element empty of type `A` 
```scala
trait Monoid[A] {
  def combine(x: A, y: A): A
  def empty: A
}
```
_For all values x, y, and z, in A, combine must be associative and empty must be an identity element:_ 
```scala
def associativeLaw[A](x: A, y: A, z: A)
      (implicit m: Monoid[A]): Boolean = {
  m.combine(x, m.combine(y, z)) ==
    m.combine(m.combine(x, y), z)
}
def identityLaw[A](x: A)
      (implicit m: Monoid[A]): Boolean = {
  (m.combine(x, m.empty) == x) &&
    (m.combine(m.empty, x) == x)
}
```

_Integer subtraction, for example, is not a monoid because subtraction is not associative:_
```scala
(1 - 2) - 3
// res15: Int = -4
1 - (2 - 3)
// res16: Int = 2
```
Examples of Monoids:
- Ints, with the zero being 0 and the operator being +.
- Ints, with the zero being 1 and the operator being *.
- Lists, with the zero being Nil and the operator being ++.
- Strings, with the zero being "" and the operator being +.

## Definition of a Semigroup
_A semigroup is just the combine part of a monoid._
```scala
trait Semigroup[A] {
  def combine(x: A, y: A): A
}
trait Monoid[A] extends Semigroup[A] {
  def empty: A
}
```

Monoids in Cats: 
```scala
import cats.Monoid
import cats.instances.string._ // for Monoid
Monoid[String].combine("Hi ", "there")
// res0: String = Hi there
Monoid[String].empty
// res1: String = ""
```

The combine method is also accessible as `|+|`
```scala
import cats.instances.string._ // for Monoid
import cats.syntax.semigroup._ // for |+|
val stringResult = "Hi " |+| "there" |+| Monoid[String].empty 
// stringResult: String = Hi there
import cats.instances.int._ // for Monoid
val intResult = 1 |+| 2 |+| Monoid[Int].empty
// intResult: Int = 3
```

# Functors
_Informally, a functor is anything with a map method._
single argument functions are also functors
function composition is sequencing
```scala
val func =
  ((x: Int) => x.toDouble).
    map(x => x + 1).
    map(x => x * 2).
    map(x => x + "!")
func(123)
// res10: String = 248.0!
```
Calling `map` doesn't actually execute the code, it just chains the operations together. Only after supplying an argument to the generated function it gets executed.
_We can think of this as lazily queueing up operations similar to Future_

_Formally, a functor is a type `F[A]` with an operation `map` with type `(A => B) => F[B]`_

Functors in Cats: 
```scala
package cats
import scala.language.higherKinds
trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}
```

Functors Laws:
Identity: calling map with the identity function is the same as doing nothing:
```scala
 fa.map(a => a) == fa
```
Composition: mapping with two functions f and g is the same as mapping with f and then mapping with g:
```scala
 fa.map(g(f(_))) == fa.map(f).map(g)
```

## Aside: Higher Kinds and Type Constructors

_Kinds are like types for types. They describe the number of "holes" in a type  
For example, List is a type constructor with one hole. We fill that hole by specifying a parameter to produce a regular type like List[Int] or List[A]. The trick is not to confuse type constructors with generic types. List is a type constructor, List[A] is a type:_

```scala
// Declare F using underscores:
def myMethod[F[_]] = {
  // Reference F without underscores:
  val functor = Functor.apply[F]
  // ...
}
```
From [stackoverflow](https://stackoverflow.com/questions/6246719/what-is-a-higher-kinded-type-in-scala)  
_A type constructor is a type that you can apply to type arguments to "construct" a type._  
_A value constructor is a value that you can apply to value arguments to "construct" a value._

```scala
                   proper    first-order           higher-order

values             10        (x: Int) => x         (f: (Int => Int)) => f(10)
types (classes)    String    List                  Functor
types              String    ({type λ[x] = x})#λ   ({type λ[F[x]] = F[String]})#λ
```

![Relationship between values, types and kinds](https://i.stack.imgur.com/K0dwL.jpg)

## Continue Functors

To define a new Functor you just need to define it's map method:
```scala
implicit val optionFunctor: Functor[Option] =
  new Functor[Option] {
    def map[A, B](value: Option[A])(func: A => B): Option[B] =
      value.map(func)
  }
```

See [Binary tree functor example](cats/src/main/scala/BinTreeFunctor.scala) for real-world example.  
Important here is that `toFunctorOps` from `cats.implicits._` needs to be imported for it to work, otherwise it can't find the implicitly defined functor.

## Contravariant and Invariant Functors
_The contravariant functor, provides an operation called contramap that represents "prepending" an operation to a chain._

Example here, note self alias to distinguish between the 2 format methods.
```scala
object Contramap extends App {

  trait Printable[A] {
    self => def format(value: A): String

    def contramap[B](func: B => A): Printable[B] =
      new Printable[B] {
        def format(value: B): String = self.format(func(value))
      }
  }

  def format[A](value: A)(implicit p: Printable[A]): String = p.format(value)
}
```

This uses the contramap to more efficiently create a printable for the `Box[A]` type. This is more convenient instead of creating a new `Printable[Box[A]]` instance.
```scala
 final case class Box[A](value: A)
 implicit def boxPrintable[A](implicit p: Printable[A]) = p.contramap[Box[A]](_.value)

  // this should work
  format(Box("hello world"))
  // res5: String = "hello world"
  format(Box(true))
  // res6: String = yes
 ```

### Detour: self types
https://docs.scala-lang.org/tour/self-types.html
```scala
trait User {
  def username: String
}

trait Tweeter {
  this: User =>  // reassign this
  def tweet(tweetText: String) = println(s"$username: $tweetText")
}

class VerifiedTweeter(val username_ : String) extends Tweeter with User {  // We mixin User because Tweeter required it
	def username = s"real $username_"
}

val realBeyoncé = new VerifiedTweeter("Beyoncé")
realBeyoncé.tweet("Just spilled my glass of lemonade")  // prints "real Beyoncé: Just spilled my glass of lemonade"
```
In this case in the trait Tweeter `this` is assigned to the trait `User`, so it must be mixed in during mixin. Also because it is called `this` the usage is inferred (you can omit `this.username`).

### Invariant functors
Invariant functors implement a function called `imap`.
It's like a combination of `map` and `contramap`.
Map generates new type class instances by appending a function to a chain, `contramap` generates them by prepending it to the chain.
imap generates them via a pair of bidirectional transformations.

This is usually used for encoding and decoding a data type. 
```scala
object imap extends App {

  trait Codec[A] {
    def encode(value: A): String

    def decode(value: String): A

    def imap[B](dec: A => B, enc: B => A): Codec[B] = {
      val self = this
      new Codec[B] {
        def encode(value: B): String =
          self.encode(enc(value))

        def decode(value: String): B =
          dec(self.decode(value))
      }
    }
  }
}
```

Example (string to string is a no-op): 
```scala
implicit val stringCodec: Codec[String] =
  new Codec[String] {
    def encode(value: String): String = value
    def decode(value: String): String = value
  }

implicit val intCodec: Codec[Int] =
  stringCodec.imap(_.toInt, _.toString)
implicit val booleanCodec: Codec[Boolean] =
  stringCodec.imap(_.toBoolean, _.toString)
```

# Monads
_a monad is anything with a constructor and a flatMap method._  
_A monad is a mechanism for sequencing computations._ 
_Every monad is also a functor (see below for proof), so we can rely on both flatMap and map to sequence computations that do and don’t introduce a new monad. Plus, if we have both flatMap and map we can use for comprehensions to clarify the sequencing behaviour:_
```scala
def stringDivideBy(aStr: String, bStr: String): Option[Int] = for {
    aNum <- parseInt(aStr)
    bNum <- parseInt(bStr)
    ans  <- divide(aNum, bNum)
} yield ans
```

Simplified version of Monad in Cats:  
```scala
import scala.language.higherKinds
trait Monad[F[_]] {
  def pure[A](value: A): F[A]
  def flatMap[A, B](value: F[A])(func: A => F[B]): F[B]
}
```

Laws that must be obeyed:
_Left identity: calling pure and transforming the result with func is the same as calling func:_
```scala
pure(a).flatMap(func) == func(a)
```
_Right identity: passing pure to flatMap is the same as doing nothing:_
```scala
m.flatMap(pure) == m
```
_Associativity: flatMapping over two functions f and g is the same as flatMapping over f and then flatMapping over g:_
```scala
m.flatMap(f).flatMap(g) == m.flatMap(x => f(x).flatMap(g))
```
Implementing a `map` function for a monad:  
```scala
trait Monad[F[_]] {
    def pure[A](a: A): F[A]

    def flatMap[A, B](value: F[A])(func: A => F[B]): F[B]

    def map[A, B](value: F[A])(func: A => B): F[B] =
      flatMap(value)(a => pure(func(a)))
  }
```

From https://itnext.io/benefits-of-identity-monad-in-scala-cats-a2cb0baef639:  

In Monad[F[_]], F represents an “effect” or “computational context” like Future , Either or Option that allows applying a function (A => B or A => F[B]) to a single effectful value (A) without needing to “leave” that “effect” or “computational context” (F) and convert that value to desire effectful value (B).

## The Identity Monad
This only works on Options and Lists.
```scala
import scala.language.higherKinds
import cats.Monad
import cats.syntax.functor._ // for map
import cats.syntax.flatMap._ // for flatMap
def sumSquare[F[_]: Monad](a: F[Int], b: F[Int]): F[Int] = 
  for {
    x <- a
    y <- b
  } yield x*x + y*y
```
To use plain values `cats.Id` can be used:
```scala
import cats.Id
sumSquare(3 : Id[Int], 4 : Id[Int])
// res2: cats.Id[Int] = 25
```
This implements `map`, `flatMap` and `pure` for Id.
Interesting here is that map & flatMap are actually the same, also all type info can be ignored, as `A` will be cast to `Id[A]` automatically
```scala
object MonadicId extends App {

  import cats.Id

  def pure[A](value: A): Id[A] = value

  def map[A, B](initial: Id[A])(func: A => B): Id[B] =
    func(initial)

  def flatMap[A, B](initial: Id[A])(func: A => Id[B]): Id[B] = func(initial)
  

  println(map(123)(_*2))

  println(flatMap(123)(_*2))
}
```
_Identity Monad allows us to write functions that work with monadic and non-monadic values and it is very powerful because we can wrap values into “effect” or “computational context” in production and remove them from “effect” or “computational context” for test using Identity Monad. For example, we can run code asynchronously in the production using the Future and synchronously in the test using the Identity Monad and easily switch between asynchronous and synchronous world._

## Either
Since scala 2.12, `Either` is right-biased. flatMap will point to Right.  
There are some convenience methods in Cats:
```scala
import cats.syntax.either._ // for asRight
val a = 3.asRight[String]
// a: Either[String,Int] = Right(3)
val b = 4.asRight[String]
// b: Either[String,Int] = Right(4)
for {
x <- a
y <- b
} yield x*x + y*y
// res4: scala.util.Either[String,Int] = Right(25)
```
### Error handling
```scala
for {
  a <- 1.asRight[String]
  b <- 0.asRight[String]
  c <- if(b == 0) "DIV0".asLeft[Int]
       else (a / b).asRight[String]
} yield c * 100
// res21: scala.util.Either[String,Int] = Left(DIV0)
```

_Cats provides an additional type class called MonadError that abstracts over Either-like data types that are used for error handling. MonadError provides extra operations for raising and handling errors._  
Simple version: 
```scala
package cats
trait MonadError[F[_], E] extends Monad[F] {
  // Lift an error into the `F` context:
  def raiseError[A](e: E): F[A]
  // Handle an error, potentially recovering from it:
  def handleError[A](fa: F[A])(f: E => A): F[A]
  // Test an instance of `F`,
  // failing if the predicate is not satisfied:
  def ensure[A](fa: F[A])(e: E)(f: A => Boolean): F[A]
}
```

## Eval Monad
_`cats.Eval` is a monad that allows us to abstract over different models of evaluation. We typically hear of two such models: eager and lazy. `Eval` throws in a further distinction of whether or not a result is memoized._  

| Scala | Type | When? |
| ----- | ----- | ------ |
| Eager| immediately|
| Lazy| on access|
| Memoized| run once on first access, after that results are cached |

`val`: eager, memoized

```scala
val x = {
  println("Computing X")
  math.random
}
// Computing X
// x: Double = 0.32119158749503807
x // first access
// res0: Double = 0.32119158749503807
x // second access
// res1: Double = 0.32119158749503807
```
`def`: lazy, not memoized
```scala
def y = {
  println("Computing Y")
  math.random
}
// y: Double
y // first access
// Computing Y
// res2: Double = 0.5179245763430056
y // second access
// Computing Y
// res3: Double = 0.8657077812314633
```
`lazy val`: lazy, memoized
```scala
lazy val z = {
  println("Computing Z")
  math.random
}
// z: Double = <lazy>
z // first access
// Computing Z
// res4: Double = 0.027165389120539563
z // second access
// res5: Double = 0.027165389120539563
```
### `Eval` in cats
```scala
import cats.Eval
val now = Eval.now(math.random + 1000)
// now: cats.Eval[Double] = Now(1000.6884369117727)
val later = Eval.later(math.random + 2000)
// later: cats.Eval[Double] = cats.Later@71175ee9
val always = Eval.always(math.random + 3000)
// always: cats.Eval[Double] = cats.Always@462e2fea
```
Extract the value with `now.value`
As Eval is a Monad it can be chained, and steps are cached:
```scala
val saying = Eval.
  always { println("Step 1"); "The cat" }.
  map { str => println("Step 2"); s"$str sat on" }.
  memoize.
  map { str => println("Step 3"); s"$str the mat" }
// saying: cats.Eval[String] = cats.Eval$$anon$8@7a0389b5
saying.value // first access
// Step 1
// Step 2
// Step 3
// res18: String = The cat sat on the mat
saying.value // second access
// Step 3
// res19: String = The cat sat on the mat
```
_One useful property of Eval is that its map and flatMap methods are trampolined. This means we can nest calls to map and flatMap arbitrarily without consuming stack frames. We call this property “stack safety”._ 
Example:
```scala
def factorial(n: BigInt): BigInt =
  if(n == 1) n else n * factorial(n - 1)
```
`factorial(50000)` will stackoverflow
```scala
  def factorial(n: BigInt): Eval[BigInt] =
   if(n == 1) {
     Eval.now(n)
   } else {
     Eval.defer(factorial(n - 1).map(_ * n))
   }
 factorial(50000).value
```
_ we must bear in mind that trampolining is not free. It avoids consuming stack by creating a chain of function objects on the heap. There are still limits on how deeply we can nest computations, but they are bounded by the size of the heap rather than the stack._
## The Reader Monad
Used to sequence operations that depend on some input. 
```scala
import cats.data.Reader
case class Cat(name: String, favoriteFood: String)
// defined class Cat
val catName: Reader[Cat, String] =
  Reader(cat => cat.name)
// catName: cats.data.Reader[Cat,String] = Kleisli(<function1>)

catName.run(Cat("Garfield", "lasagne"))
// res0: cats.Id[String] = Garfield
```

Composition:

map just extends the computation, by passing the result through a function
```scala
val greetKitty: Reader[Cat, String] =
  catName.map(name => s"Hello ${name}")
greetKitty.run(Cat("Heathcliff", "junk food")) // res1: cats.Id[String] = Hello Heathcliff
```

flatmap (using for comprehension)
```scala
val feedKitty: Reader[Cat, String] =
Reader(cat => s"Have a nice bowl of ${cat.favoriteFood}")
val greetAndFeed: Reader[Cat, String] =
  for {
    greet <- greetKitty
    feed  <- feedKitty
  } yield s"$greet. $feed."
greetAndFeed(Cat("Garfield", "lasagne"))
// res3: cats.Id[String] = Hello Garfield. Have a nice bowl of lasagne
.
greetAndFeed(Cat("Heathcliff", "junk food"))
// res4: cats.Id[String] = Hello Heathcliff. Have a nice bowl of junk
food.
```

Reader example with chaining:
[Readers](cats/src/main/scala/Readers.scala)
```scala
case class Db(
                usernames: Map[Int, String],
                passwords: Map[String, String]
              )

type DbReader[A] = Reader[Db, A]

def findUsername(userId: Int): DbReader[Option[String]] = Reader(db => db.usernames.get(userId))

def checkPassword(username: String, password: String): DbReader[Boolean] =
  Reader(db => db.passwords.get(username).map(_ == password).getOrElse(false))

def checkLogin(userId: Int, password: String): DbReader[Boolean] = for {
  username <- findUsername(userId)
  valid <- username.map(checkPassword(_, password)).getOrElse(false.pure[DbReader])
} yield valid
```
Unfortunately I got this error while compiling, somehow there must be a clash between 2 implicits for the `false.pure`
```
Error:(26, 19) ambiguous implicit values:
 both method catsDataMonadForKleisliId in class KleisliInstances of type [A]=> cats.CommutativeMonad[[γ$15$]cats.data.Kleisli[[A]A,A,γ$15$]]
 and method catsApplicativeForArrow in object Applicative of type [F[_, _], A](implicit F: cats.arrow.Arrow[F])cats.Applicative[[β$0$]F[A,β$0$]]
 match expected type cats.Applicative[Readers.DbReader]
        false.pure[DbReader]
```

Readers are most useful in situations where:
- we are constructing a batch program that can easily be represented by
a function;
- we need to defer injection of a known parameter or set of parameters;
- we want to be able to test parts of the program in isolation.

## The State Monad
instances of State[S, A] represent functions of type S => (S, A).Sis the type of the state and A is the type of the result.
```scala
import cats.data.State
val a = State[Int, String] { state =>
  (state, s"The state is $state")
}
// a: cats.data.State[Int,String] = cats.data.IndexedStateT@70142af6


// Get the state and the result:
val (state, result) = a.run(10).value
// state: Int = 10
// result: String = The state is 10
// Get the state, ignore the result:
val state = a.runS(10).value
// state: Int = 10
// Get the result, ignore the state:
val result = a.runA(10).value
// result: String = The state is 10
```

The map and flatMap methods thread the state from one instance to another. Each individual instance represents an atomic state transformation, and their combination represents a complete sequence of changes

## Defining Custom Monads
Example for `Option`
```scala
import cats.Monad
import scala.annotation.tailrec
val optionMonad = new Monad[Option] {
  def flatMap[A, B](opt: Option[A])
      (fn: A => Option[B]): Option[B] =
    opt flatMap fn
  def pure[A](opt: A): Option[A] =
    Some(opt)
  @tailrec
  def tailRecM[A, B](a: A)
      (fn: A => Option[Either[A, B]]): Option[B] =
    fn(a) match {
      case None           => None
      case Some(Left(a1)) => tailRecM(a1)(fn)
      case Some(Right(b)) => Some(b)
  } 
}
```
tailRecM is used to minimize stack space used

# Monad Transformers
It quickly can become cumbersome to work with a lot of nested Monads:
```scala
def lookupUserName(id: Long): Either[Error, Option[String]] = 
for {
    optUser <- lookupUser(id)
  } yield {
    for { user <- optUser } yield user.name
}
```

```scala
import cats.Monad
import cats.syntax.applicative._ // for pure
import cats.syntax.flatMap._     // for flatMap
import scala.language.higherKinds
// Hypothetical example. This won't actually compile:
def compose[M1[_]: Monad, M2[_]: Monad] = {
  type Composed[A] = M1[M2[A]]
  new Monad[Composed] {
    def pure[A](a: A): Composed[A] =
      a.pure[M2].pure[M1]
    def flatMap[A, B](fa: Composed[A])
        (f: A => Composed[B]): Composed[B] =
      // Problem! How do we write flatMap?
    ??? 
  }
}
```
_It is impossible to write a general definition of flatMap without knowing something about M1 or M2_

If we do know something about one of the types we can complete the code.  
For Option:
```scala
def flatMap[A, B](fa: Composed[A])
    (f: A => Composed[B]): Composed[B] =
  fa.flatMap(_.fold(None.pure[M])(f))
```
_Cats provides transformers for many monads, each named with a T suffix: EitherT composes Either with other monads, OptionT composes Option, and so on._

```scala
import cats.data.OptionT
type ListOption[A] = OptionT[List, A]
```
```scala
import cats.Monad
import cats.instances.list._     // for Monad
import cats.syntax.applicative._ // for pure
val result1: ListOption[Int] = OptionT(List(Option(10))) // result1: ListOption[Int] = OptionT(List(Some(10)))
val result2: ListOption[Int] = 32.pure[ListOption] // result2: ListOption[Int] = OptionT(List(Some(32)))

result1.flatMap { (x: Int) =>
  result2.map { (y: Int) =>
    x+y 
  }
}
// res1: cats.data.OptionT[List,Int] = OptionT(List(Some(42)))
```

_The transformer itself represents the inner monad in a stack, while the first type parameter specifies the outer monad. The remaining type parameters are the types we’ve used to form the corresponding monads._
_For example, our ListOption type above is an alias for OptionT[List, A] but the result is effectively a List[Option[A]]. In other words, we build monad stacks from the inside out:_

_For example, let’s create a Future of an Either of Option. Once again we build this from the inside out with an OptionT of an EitherT of Future. However, we can’t define this in one line because EitherT has three type parameters:_
```scala
case class EitherT[F[_], E, A](stack: F[Either[E, A]]) {
    // etc...
}
```
The three type parameters are as follows:
- F[_] is the outer monad in the stack (Either is the inner);
- E is the error type for the Either;
- A is the result type for the Either.

```scala
import scala.concurrent.Future
import cats.data.{EitherT, OptionT}

type FutureEither[A] = EitherT[Future, String, A]
type FutureEitherOption[A] = OptionT[FutureEither, A]

import cats.instances.future._ // for Monad
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global import scala.concurrent.duration._

val futureEitherOr: FutureEitherOption[Int] =
  for {
    a <- 10.pure[FutureEitherOption]
    b <- 32.pure[FutureEitherOption]
  } yield a + b
```

Unpacking MonadTranformers:
```scala
// Create using apply:
val errorStack1 = OptionT[ErrorOr, Int](Right(Some(10)))
// errorStack1: cats.data.OptionT[ErrorOr,Int] = OptionT(Right(Some(10)))

// Extracting the untransformed monad stack:
errorStack1.value
// res11: ErrorOr[Option[Int]] = Right(Some(10))
// Mapping over the Either in the stack:
errorStack2.value.map(_.getOrElse(-1))
// res13: scala.util.Either[String,Int] = Right(32)
```

## Usage patterns
Creating "super stacks" is pretty common. Example for request handlers:
```scala
sealed abstract class HttpError
final case class NotFound(item: String) extends HttpError final case class BadRequest(msg: String) extends HttpError // etc...
type FutureEither[A] = EitherT[Future, HttpError, A]
```
We use something similar in our codebase:

```scala
  // vox.buy.auth.controller
  type EitherST[F[_], A] = EitherT[F, String, A]
  type EitherFT[A]       = EitherST[Future, A]
```
[Excercise](cats/src/main/scala/MonadsTransformAndRollOut.scala)

# Semigroupal and Applicative
Problems with Monads:
- Can't run in parallel
- For validation can't capture all errors, breaks after first fail

This is because it is assumed that computation in flatMap are dependend on the result of the last one.
```scala
 // context2 is dependent on value1:
context1.flatMap(value1 => context2)
```
- _Semigroupal encompasses the notion of composing pairs of contexts. Cats provides a cats.syntax.apply module that makes use of Semigroupal and Functor to allow users to sequence functions with multiple arguments._

- _Applicative extends Semigroupal and Functor. It provides a way of applying functions to parameters within a context. Applicative is the source of the pure method_

From [Stackoverflow](https://stackoverflow.com/questions/19880207/when-and-why-should-one-use-applicative-functors-in-scala):

_Writing applicative code allows you to avoid making unnecessary claims about dependencies between computations—claims that similar monadic code would commit you to. A sufficiently smart library or compiler could in principle take advantage of this fact._

Monadic code example:
```scala
case class Foo(s: Symbol, n: Int)

val maybeFoo = for {
  s <- maybeComputeS(whatever)
  n <- maybeComputeN(whatever)
} yield Foo(s, n)
```
_We know that maybeComputeN(whatever) doesn't depend on s (assuming these are well-behaved methods that aren't changing some mutable state behind the scenes), but the compiler doesn't—from its perspective it needs to know s before it can start computing n._

_The applicative version (using Scalaz) looks like this:_
```scala
val maybeFoo = (maybeComputeS(whatever) |@| maybeComputeN(whatever))(Foo(_, _))
```
This means that there is no dependency between the two computations

## Semigroupal
_If we have two objects of type `F[A]` and `F[B]`, a `Semigroupal[F]` allows us to
combine them to form an `F[(A, B)]`_
Definition in Cats:  
```scala
trait Semigroupal[F[_]] {
  def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
}
```

```scala
import cats.Semigroupal
import cats.instances.option._ // for Semigroupal
Semigroupal[Option].product(Some(123), Some("abc")) // res0: Option[(Int, String)] = Some((123,abc)
```
if both are `Some` it will return a tuple, if either of them is None it will create None:
```scala
Semigroupal[Option].product(None, Some("abc")) // res1: Option[(Nothing, String)] = None
Semigroupal[Option].product(Some(123), None)
// res2: Option[(Int, Nothing)] = None
```
Short version:
```scala
import cats.instances.option._ // for Semigroupal
import cats.syntax.apply._ // for tupled and mapN

 (Option(123), Option("abc")).tupled
// res7: Option[(Int, String)] = Some((123,abc))
```
`mapN` supports an implicit Functor and a function that matches the arity(number of parameters) to combine the values:
```scala
case class Cat(name: String, born: Int, color: String)
(
  Option("Garfield"),
  Option(1978),
  Option("Orange & black")
).mapN(Cat.apply)
// res9: Option[Cat] = Some(Cat(Garfield,1978,Orange & black)
```
N can also be substituted by the number of parameters (map3)

### Future
_The semantics for Future provide parallel as opposed to sequential execution:_
```scala
import cats.Semigroupal
import cats.instances.future._ // for Semigroupal
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global 
import scala.language.higherKinds

val futurePair = Semigroupal[Future].
  product(Future("Hello"), Future(123))

Await.result(futurePair, 1.second)
// res1: (String, Int) = (Hello,123)
```

### List
_Combining Lists with Semigroupal produces some potentially unexpected results. We might expect code like the following to zip the lists, but we actually get the cartesian product of their elements:_
```scala
import cats.Semigroupal
import cats.instances.list._ // for Semigroupal
Semigroupal[List].product(List(1, 2), List(3, 4))
// res5: List[(Int, Int)] = List((1,3), (1,4), (2,3), (2,4))
```

### Either
_We might expect product applied to Either to accumulate errors instead of fail fast. Again, perhaps surprisingly, we find that product implements the same fail-fast behaviour as flatMap:_
```scala
import cats.instances.either._ // for Semigroupal
type ErrorOr[A] = Either[Vector[String], A]
Semigroupal[ErrorOr].product(
  Left(Vector("Error 1")),
  Left(Vector("Error 2"))
)
// res7: ErrorOr[(Nothing, Nothing)] = Left(Vector(Error 1))
```
[Excercise product with flatmap](cats/src/main/scala/Ch6ProductOfMonad.scala)

_We choose our semantics by choosing our data structures. If we choose a monad, we get strict sequencing. If we choose an applicative, we lose the ability to flatMap. This is the trade-off enforced by the consistency laws. So choose your types carefully!_

# Foldable and Traverse
- Foldable abstracts the familiar foldLeft and foldRight operations
- Traverse is a higher-level abstraction that uses Applicatives to iterate with less pain than folding.

## Foldable
Folding needs a accumulator value & a binary function to combine each item in the sequence:
```scala
def show[A](list: List[A]): String = 
  list.foldLeft("nil")((accum, item) => s"$item then $accum")
show(Nil)
// res0: String = nil
show(List(1, 2, 3))
// res1: String = 3 then 2 then 1 then nil
```
[Exercise: Reflecting on Folds](cats/src/main/scala/Folding.scala)

Foldable contains a bunch of useful methods to safely (stack-safe) fold over collections
```scala
import cats.instances.int._ // for Monoid
Foldable[List].combineAll(List(1, 2, 3))
// res12: Int = 6
```
```scala
import cats.instances.vector._ // for Monoid
val ints = List(Vector(1, 2, 3), Vector(4, 5, 6))
(Foldable[List] compose Foldable[Vector]).combineAll(ints)
// res15: Int = 21
```

## Traverse
```scala
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
val hostnames = List(
  "alpha.example.com",
  "beta.example.com",
  "gamma.demo.com"
)
def getUptime(hostname: String): Future[Int] =
  Future(hostname.length * 60) // just for demonstration
```
We want to poll all of the hosts and collect all of their uptimes. `map` would create a `List[Future[Int]]`, we want a single `Future` though.
Done manually using `.fold`:  
```scala
val allUptimes: Future[List[Int]] = hostnames.foldLeft(Future(List.empty[Int])) {
    (accum, host) =>
      val uptime = getUptime(host)
      for {
        accum  <- accum
        uptime <- uptime
      } yield accum :+ uptime
}
Await.result(allUptimes, 1.second)
// res2: List[Int] = List(1020, 960, 840)
```

Much cleaner is:  
```scala
val allUptimes: Future[List[Int]] =
  Future.traverse(hostnames)(getUptime)
Await.result(allUptimes, 1.second)
// res3: List[Int] = List(1020, 960, 840)
```
The definition is this:
```scala
def traverse[A, B](values: List[A])
    (func: A => Future[B]): Future[List[B]] =
values.foldLeft(Future(List.empty[A])) { (accum, host) => val item = func(host)
for {
      accum <- accum
      item  <- item
    } yield accum :+ item
}
```
- start with a List[A];
- provide a function A=>Future[B]; 
- end up with a Future[List[B]].

Future sequence is even easier:
```scala
object Future {
def sequence[B](futures: List[Future[B]]): Future[List[B]] =
    traverse(futures)(identity)
// etc...
}
```
Starts with a List[A] ends with a Future[List[A]]

_Cats’ Traverse type class generalises these patterns to work with any type of Applicative: Future, Option, Validated, and so on._

```scala
Future(List.empty[Int])
// is the same as
import cats.Applicative
import cats.instances.future._
List.empty[Int].pure[Future]
```
Our combine function is now the same as:  
```scala
import cats.syntax.apply._ // for mapN
// Combining accumulator and hostname using an Applicative:
def newCombine(accum: Future[List[Int]], host: String): Future[List[Int]] =
  (accum, getUptime(host)).mapN(_ :+ _)
```

## Kleisli
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