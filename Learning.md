# Pattern matching
It's just like switch case!

_This means you always have to make sure that all cases are covered, even if it means adding a default case where there’s nothing to do_

seems error prone to me, you could forget it

Deep matches - Very cool.

Example:
```scala
// Simplistic address type. Using all strings is questionable, too.
case class Address(street: String, city: String, country: String)
case class Person(name: String, age: Int, address: Address)

val alice   = Person("Alice",   25, Address("1 Scala Lane", "Chicago", "USA"))
val bob     = Person("Bob",     29, Address("2 Java Ave.",  "Miami",   "USA"))
val charlie = Person("Charlie", 32, Address("3 Python Ct.", "Boston",  "USA"))

for (person <- Seq(alice, bob, charlie)) {
  person match {
    case Person("Alice", 25, Address(_, "Chicago", _)) => println("Hi Alice!")
    case Person("Bob", 29, Address("2 Java Ave.", "Miami", "USA")) =>
      println("Hi Bob!")
    case Person(name, age, _) =>
      println(s"Who are you, $age year-old person named $name?")
  }
}
```

Variable binding
https://alvinalexander.com/scala/how-to-use-pattern-matching-scala-match-case-expressions#adding-variables-to-patterns

```scala
// doesn't work
case list: List(1, _*) => s"thanks for the List: $list"

// works
case list @ List(1, _*) => s"$list"
```

_One other generalization is worth noting: a sequence of cases gives you a partial function. If you apply such a function on a value it does not support, it will generate a run-time exception. For example, here is a partial function that returns the second element of a list of integers:_
```scala
val second: List[Int] => Int = {
    case x :: y :: _ => y
}
```
_I don't really understand which part is a partial function._

Not all cases are covered by this pattern (for example `Nil`), so it will return a `PartialFunction` it contains an `isDefinedAt` which checks for the valid cases.

# Lists
`A :: B :: C` is interpreted as `A :: (B :: C)`. `head` and `tail` make much more sense to me now. There are implicit parentheses around each of the elements, because it's a recursive structure.

Using pattern matching you must be exact:
```scala
scala> val List(a, b, c) = fruit

a: String = apple
b: String = pear
c: String = banana

scala> val List(a, b) = fruit

scala.MatchError: List(apple, pear, banana) (of class scala.collection.immutable.$colon$colon)
  ... 24 elided

// this works
scala> val a :: b :: rest = fruit
a: String = apple
b: String = pear
rest: List[String] = List(banana)
```
Concatenate Lists
```scala
xs ::: ys ::: zs
```
is interpreted like this:
```scala
  xs ::: (ys ::: zs)
```

_It’s a good idea to organize your data so that most accesses are at the head of a list, rather than the last element._

Reverse it if necessary

## Methods on Lists
```scala
val abcde = List('a', 'b', 'c', 'd', 'e')
```
| Function      | Example                                        |
| ------------- |------------------------------------------------|
| `.init`       | abcde.init == `List(a, b, c, d)`               |
| `.last`       | abcde.last == `'e'`                            |
| `.take`       | abcde take 2 == `List(a, b)`                   |
| `.drop`       | abcde drop 2 == `List(c, d, e)`                |
| `.splitAt`    | abcde splitAt 2 == `(List(a, b),List(c, d, e))`|
| `.apply`      | abcde appy 2 == `'c'`                          |
| `.indices`    | abcde.indices == `Range(0, 1, 2, 3, 4)`        |

`zip`
```scala
val zipped = abcde zip List(1, 2, 3)
// List((a,1), (b,2), (c,3))
zipped.unzip
// (List(a, b, c),List(1, 2, 3))
```

### Folding
foldLeft `/:`  
structure: `(startValue /: list) (binaryOperation)`  
`(z /: List(a, b, c)) (op)` equals `op(op(op(z, a), b), c)`
```
       op
      /  \
     op   c
    / \
   op   b
  / \  
 z   a
```
That's why `/:` is used

Examples:
```scala
 ("" /: words) (_ +" "+ _)
 // => the quick brown fox
 (words.head /: words.tail)  (_ +" "+ _)
  // => the quick brown fox
```

foldRight `:\`  
_It involves the same three operands as fold left, but the first two appear in reversed order: The first operand is the list to fold, the second is the start value._

https://alvinalexander.com/scala/how-to-walk-scala-collections-reduceleft-foldright-cookbook

_The foldLeft method works just like reduceLeft, but it lets you set a seed value to be used for the first element_
```scala
scala> val a = Array(1, 2, 3)
a: Array[Int] = Array(1, 2, 3)

scala> a.reduceLeft(_ + _)
res0: Int = 6

scala> a.foldLeft(20)(_ + _)
res1: Int = 26

scala> a.foldLeft(100)(_ + _)
res2: Int = 106

List(1, 3, 8).foldLeft(100)(_ - _) == ((100 - 1) - 3) - 8 == 88
List(1, 3, 8).foldRight(100)(_ - _) == 1 - (3 - (8 - 100)) == -94
```

Sorting
```scala
scala> List(1, -3, 4, 2, 6) sortWith (_ < _)
res51: List[Int] = List(-3, 1, 2, 4, 6)
```

Methods on the `scala.List` companion object:
`List.tabulate` 
```scala
scala> val squares = List.tabulate(5)(n => n * n)
        squares: List[Int] = List(0, 1, 4, 9, 16)
```

# Misc
- [`lazy val`](https://stackoverflow.com/questions/7484928/what-does-a-lazy-val-do)
Interesting can see how it works, use cases not very clear to me.

- Is the `Any` type dangerous? Seems like bad practice to use it - ever

- Varags are done with `*`
  Example: 
  ```scala
  // calculate the sum of all the numbers passed to the method
  def sum(args: Int*): Int = args.fold(0)(_+_)
  ```