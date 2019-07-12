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