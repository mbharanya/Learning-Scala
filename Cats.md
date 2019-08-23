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
_We can think of this as lazily queueing up operatins similar to Future_

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