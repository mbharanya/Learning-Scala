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

_## Aside: Higher Kinds and Type Constructors_

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