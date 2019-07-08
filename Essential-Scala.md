# Essential Scala

# Case Objects
Case class without constructor -> case object
```scala
case object Citizen {
  def firstName = "John"
  def lastName  = "Doe"
  def name = firstName + " " + lastName
}
```

internally it will create this:
```scala
class Citizen { /* ... */ }
object Citizen extends Citizen { /* ... */ }
```

# Traits
_When we mark a trait as sealed we must define all of its subtypes in the same file._
_If all the subtypes of a trait are known, seal the trait_
```scala
scala> def missingCase(v: Visitor) =
          v match {
            case User(_, _, _) => "Got a user"
          }
<console>:21: warning: match may not be exhaustive. It would fail on the following input: Anonymous(_, _)
v match {
               ^
missingCase: (v: Visitor)String
```
It will provide compile time errors, because it knows all the possibilities.

# Recursive data
```scala
final case class Broken(broken: Broken)
```
this will never be possible to instantiate

```scala
sealed trait IntList
final case object End extends IntList
final case class Pair(head: Int, tail: IntList) extends IntList

// construct with this:
Pair(1, Pair(2, Pair(3, End)))

// same thing
val d = End()
val c = Pair(3, d)
val b = Pair(2, c)
val a = Pair(1, b)
```
Because we defined an End object it is possible to instantiate it.

## Tail recursion
_You may be concerned that recursive calls will consume excessive stack space. Scala can apply an optimisation, called tail recursion, to many recursive functions to stop them consuming stack space._

```scala
def method1: Int =
  1
def tailCall: Int =
  method1
```
This is a tail call because it immediately returns the value.
```scala
 def notATailCall: Int =
  method1 + 2
```
This instead needs to first execute method1 and then add something to it.
_Scala only optimises tail calls where the caller calls itself_
You can manually add `@tailrec` to a method to optimize it.

## Placeholder syntax
```scala
_+_         // expands to `(a,b)=>a+b` 
foo(_)      // expands to `(a) => foo(a)`
foo(_, b)   // expands to `(a) => foo(a, b)`
_(foo)      // expands to `(a) => a(foo)` 
// and so on...
```

convert method calls to functions 
```scala
object Sum {
  def sum(x: Int, y: Int) = x + y
}
Sum.sum
// <console>:9: error: missing arguments for method sum in object Sum;
// follow this method with `_' if you want to treat it as a partially applied function // Sum.sum
// ^
(Sum.sum _)
// res: (Int, Int) => Int = <function2>
```

# Collections
## Ranges
```scala
10 until 1 by -1
```
Like a for loop in java

# Type Class Instances
```scala
implicit val ordering = Ordering.fromLessThan[Int](_ < _) scala> List(2, 4, 3).sorted
// res: List[Int] = List(2, 3, 4)
List(1, 7 ,5).sorted
// res: List[Int] = List(1, 5, 7)
```
Ordering is implicitly passed to the sorted method.

## Type Class Pattern
_A type class is a trait with at least one type variable. The type variables specify the concrete types the type class instances are defined for. Methods in the trait usually use the type variables._
```scala
trait ExampleTypeClass[A] {
  def doSomething(in: A): Foo
}
```

## Implicit Parameter Lists
```scala
object HtmlUtil {
def htmlify[A](data: A)(implicit writer: HtmlWriter[A]): String = {
    writer.write(data)
  }
}
```
This takes the data to convert to HTML, as well as an implicit parameter of the writer to use. 
```scala
HtmlUtil.htmlify(Person("John", "john@example.com"))(PersonWriter)
// res: String = <span>John &lt;john@example.com&gt;</span>
```

If you define an implicit value it will find it: 
```scala
implicit object ApproximationWriter extends HtmlWriter[Int] { def write(in: Int): String =
s"It's definitely less than ${((in / 10) + 1) * 10}" }


// result
HtmlUtil.htmlify(2)
// res: String = It's definitely less than 10
```

Even better: 
```scala
object HtmlWriter {
  def apply[A](implicit writer: HtmlWriter[A]): HtmlWriter[A] =
    writer 
}
// used like this:
                // hidden `.apply`
HtmlWriter[Person].write(Person("Noel", "noel@example.org"))
```