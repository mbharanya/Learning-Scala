# Scala with Cats

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
_Kinds are like types for types. They describe the number of “holes” in a type  
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