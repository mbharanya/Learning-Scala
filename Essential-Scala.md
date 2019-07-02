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
