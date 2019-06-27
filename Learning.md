# Martin Odersky, Lex Spoon, Bill Venners Programming in Scala A Comprehensive Step-by-Step Guide, 2nd Edition 2011
My notes for the "official" book about scala. Peppered with comments and some resources from [the scala cookbook](http://scalacookbook.com/)

# Pattern matching
It's just like switch case (but much much more powerful)!

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
| `.apply`      | abcde apply 2 == `'c'`                          |
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

# Collections
ListBuffer for appending to lists.  
_You append elements with the += operator, and prepend them with the +=: operator._

Sorted sets and maps  
Objects must implement `Ordered` trait
```scala
 scala> val ts = TreeSet(9, 3, 1, 8, 0, 2, 7, 4, 6, 5)
  ts: scala.collection.immutable.TreeSet[Int]
    = TreeSet(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
scala> var tm = TreeMap(3 -> 'x', 1 -> 'x', 4 -> 'x')
  tm: scala.collection.immutable.TreeMap[Int,Char]
    = Map(1 -> x, 3 -> x, 4 -> x)
```

# Stateful Objects
Even though immutable sets and maps do not support a true += method, Scala gives a useful alternate interpretation to +=. Whenever you write a += b, and a does not support a method named +=, Scala will try interpreting it as a = a + b.

```scala
scala> val people = Set("Nancy", "Jane")
  people: scala.collection.immutable.Set[java.lang.String] =
    Set(Nancy, Jane)
scala> people += "Bob"
<console>:11: error: reassignment to val
        people += "Bob"
```

Setters are called with `_=`.  
_The getter of a var x is just named “x”, while its setter is named “x_=”._

--> every var gets these setters by default

# Type Parameterization

Example of a purely functional Queue. It never mutates any state and instead returns a new Queue everytime.  
To achieve constant time for all 3 operations (appending to a list must first traverse all elements because `A :: B :: C == A :: (B :: C)` , this `mirror` method is created.

```scala
class Queue[T](
    private val leading: List[T],
    private val trailing: List[T]
){
private def mirror =
      if (leading.isEmpty)
        new Queue(trailing.reverse, Nil)
else this
    def head = mirror.leading.head
    def tail = {
      val q = mirror
      new Queue(q.leading.tail, q.trailing)
}
    def enqueue(x: T) =
      new Queue(leading, x :: trailing)
}
```

## Information hiding
Private constructors: 
```scala
class Queue[T] private (
    private val leading: List[T],
    private val trailing: List[T]
)
```

## Variance
generic traits are traits that take type parameters  

```scala
class Foo[+A] // A covariant class
class Bar[-A] // A contravariant class
class Baz[A]  // An invariant class
```

_you can demand covariant (flexible) subtyping of queues by chang- ing the first line of the definition of class Queue like this:_
```scala
trait Queue[+T] { ... }
```

_Besides +, there is also a prefix -, which indicates contravariant subtyping. If Queue were defined like this:_
```scala
trait Queue[-T] { ... }
```
_Scala treats arrays as nonvariant (rigid), so an Array[String] is not considered to conform to an Array[Any]_

Example:
```scala
abstract class Animal {
  def name: String
}
case class Cat(name: String) extends Animal
case class Dog(name: String) extends Animal
```

`sealed abstract class List[+A]` class, where the type parameter `A` is covariant. This means that a `List[Cat]` is a `List[Animal]` and a `List[Dog]` is also a `List[Animal]`.

```scala
List[Cat] == List[Animal]
List[Dog] == List[Animal]
```
```scala
object CovarianceTest extends App {
  def printAnimalNames(animals: List[Animal]): Unit = {
    animals.foreach { animal =>
      println(animal.name)
    }
  }

  val cats: List[Cat] = List(Cat("Whiskers"), Cat("Tom"))
  val dogs: List[Dog] = List(Dog("Fido"), Dog("Rex"))

  printAnimalNames(cats)
  // Whiskers
  // Tom

  printAnimalNames(dogs)
  // Fido
  // Rex
}
```
### Contravariance
`class Writer[-A], //A contravariant`  
Writer[B] is a subtype of Writer[A]

to remember: [-A] == [something extends A]

```scala
abstract class Printer[-A] {
  def print(value: A): Unit
}

class AnimalPrinter extends Printer[Animal] {
  def print(animal: Animal): Unit =
    println("The animal's name is: " + animal.name)
}

class CatPrinter extends Printer[Cat] {
  def print(cat: Cat): Unit =
    println("The cat's name is: " + cat.name)
}

object ContravarianceTest extends App {
  val myCat: Cat = Cat("Boots")

  def printMyCat(printer: Printer[Cat]): Unit = {
    printer.print(myCat)
  }

  val catPrinter: Printer[Cat] = new CatPrinter
  val animalPrinter: Printer[Animal] = new AnimalPrinter

  printMyCat(catPrinter)
  printMyCat(animalPrinter)
}
// output: 
// The cat's name is: Boots
// The animal's name is: Boots
```

### Invariance
_Generic classes in Scala are invariant by default_

## Lower bounds
```scala
def enqueue[U >: T](x: U) =
```
_defines T as the lower bound for default_ --> T extends U

```scala
// object private
private[this]
```

Note: Reviewing Variance might be useful, when needed

# Abstract Members
```scala
trait Abstract {
  type T
  def transform(x: T): T
  val initial: T
  var current: T
}

class Concrete extends Abstract {
  type T = String
  def transform(x: String) = x + x
  val initial = "hi"
  var current = initial
}
```

```scala
// Abstract val
val initial: String
// Abstract method
def initial: String
```

It's ok to override a `def` with a `val`, but not the other way around
Why? It's because a `val` will always return the same value (constant), this is not the same for a `def`

A trait with abstract vals is a bit analogous to the an abstract class constructor.

```scala
trait RationalTrait {
    val numerArg: Int
    val denomArg: Int
}

// anonymous class that mixes in the trait
new RationalTrait {
    val numerArg = 1
    val denomArg = 2
}
```
For traits the value is initalized after it's instantiation - compared to classes where the constructor is executed at the time of the creation.
You can pre-initalize it:
```scala
scala> new {
          val numerArg = 1 * x
          val denomArg = 2 * x
        } with RationalTrait
res1: java.lang.Object with RationalTrait = 1/2

object twoThirds extends {
    val numerArg = 2
    val denomArg = 3
} with RationalTrait
```
_Because pre-initialized fields are initialized before the superclass constructor is called, their initializers cannot refer to the object that’s being constructed. Consequently, if such an initializer refers to this, the reference goes to the object containing the class or object that’s being constructed, not the constructed object itself_

```scala
scala> object Demo {
           val x = { println("initializing x"); "done" }
}
  defined module Demo

scala> Demo
  initializing x
  res3: Demo.type = Demo$@17469af
scala> Demo.x
  res4: java.lang.String = done
```
the computation is done during instantiation in this example

[`lazy val`](https://stackoverflow.com/questions/7484928/what-does-a-lazy-val-do) only computed after first accessed - then stored in memory

```scala
class Food
abstract class Animal {
  def eat(food: Food)
}
class Grass extends Food
class Cow extends Animal {
  override def eat(food: Grass) {} // This won’t compile,
}                                  // but if it did,...
class Fish extends Food
val bessy: Animal = new Cow
bessy eat (new Fish)     // ...you could feed fish to cows.

// instead declare an upper bound Type
// <: -> Upper bound
class Food
abstract class Animal {
  type SuitableFood <: Food
  def eat(food: SuitableFood)
}

// Declare what a suitable food is for the Cow Class (path-dependent type)
class Grass extends Food
class Cow extends Animal {
  type SuitableFood = Grass
  override def eat(food: Grass) {}
}
```

```scala
scala> class Fish extends Food
defined class Fish

scala> val bessy: Animal = new Cow
bessy: Animal = Cow@2e3919

scala> bessy eat (new Fish)
<console>:12: error: type mismatch;
  found   : Fish
  required: bessy.SuitableFood
        bessy eat (new Fish)
                  ˆ
```

## Structural subtyping
class inherits from another -> _nominal_ subtype  
class has the same methods as another -> _structual_ (_refinement_ in scala) subtype

nominal are preferred to structurals usually. 

```scala
 class Pasture {
    // specify member type definition of inside this animal
    var animals: List[Animal { type SuitableFood = Grass }] = Nil
    // ...
}

def using[T, S](obj: T)(operation: T => S) = {
    val result = operation(obj)
    obj.close()  // type error!
    result
}

// define an upper bound for T to have a close() method 
def using[T <: { def close(): Unit }, S](obj: T)
      (operation: T => S) = {
    val result = operation(obj)
    obj.close()
    result
}
```

## Enums
```scala
object Color extends Enumeration {
  val Red = Value
  val Green = Value
  val Blue = Value
}

// or 

object Color extends Enumeration {
  val Red, Green, Blue = Value
}
```
`Enumeration` exposes the type `Value`  
_Value is the type of all enumeration values defined in object Color_

```scala
// adding values
object Direction extends Enumeration {
  val North = Value("North")
  val East = Value("East")
  val South = Value("South")
  val West = Value("West")
}
// iterating
scala> for (d <- Direction.values) print(d +" ")
        North East South West

// it also has an id (start from 0)
scala> Direction.East.id
        res14: Int = 1

scala> Direction(1)
        res15: Direction.Value = East
```

# Implicit Conversions and Parameters
Swing example:
```scala
// java way
val button = new JButton
  button.addActionListener(
    new ActionListener {
      def actionPerformed(event: ActionEvent) {
        println("pressed!")
      }
} )

// scala way by passing a function
button.addActionListener( // Type mismatch!
    (_: ActionEvent) => println("pressed!")
)

// --> this needs to be defined - so that it works
// takes a function returns ActionListener
implicit def function2ActionListener(f: ActionEvent => Unit) =
  new ActionListener {
    def actionPerformed(event: ActionEvent) = f(event)
  }

// can be called directly like this:
button.addActionListener(
    function2ActionListener(
      (_: ActionEvent) => println("pressed!")
    )
)

// Because it is implicit it nows to call the method:
button.addActionListener(
    (_: ActionEvent) => println("pressed!")
)
```

Rules:
- The compiler searches for implicit methods if a type error would occur
- The implicit must be in scope as a single identifier, or be associated with the source or target type of the conversion
- For sanity’s sake, the compiler does not insert further implicit conversions when it is already in the middle of trying another implicit.

Implicits are great for DSLs, the compiler will search matching methods in the implicits even if they're not defined on the type you're accessing

They are everywhere. Example from `scala.Predef`:
```scala
package scala
  object Predef {
    class ArrowAssoc[A](x: A) {
      def -> [B](y: B): Tuple2[A, B] = Tuple2(x, y)
    }
    implicit def any2ArrowAssoc[A](x: A): ArrowAssoc[A] =
      new ArrowAssoc(x)
    ...
}
// enables:
Map(1 -> "one", 2 -> "two", 3 -> "three")

//  -> is a rich wrapper
```
Parameter lists can also be satisfied by Implicits, just the types need to match

_Note that when you use implicit on a parameter, then not only will the compiler try to supply that parameter with an implicit value, but the compiler will also use that parameter as an available implicit in the body of the method_ 

# More `for`
```scala
// find all Mothers with their children
val lara = Person("Lara", false)
val bob = Person("Bob", true)
val julie = Person("Julie", false, lara, bob)
val persons = List(lara, bob, julie)

persons withFilter (p => !p.isMale) flatMap (p =>
             (p.children map (c => (p.name, c.name))))
// res1: List[(String, String)] = List((Julie,Lara),
//      (Julie,Bob))

// with a `for`
for (p <- persons; if !p.isMale; c <- p.children)
         yield (p.name, c.name)
// res2: List[(String, String)] = List((Julie,Lara),
//      (Julie,Bob))
```

`for ( seq ) yield expr`
_Here, seq is a sequence of generators, definitions, and filters, with semi- colons between successive element_
```scala
for {
    p <- persons // a generator
    n = p.name // a definition
    if (n startsWith "To") // a filter
} yield n
```

# Concurrency
Scala's alternative to the locking concurrency of Java is called actors, which is share-nothing, message-passing based.
The actors library is deprecated in favor of akka.

# Extractors
_An extractor in Scala is an object that has a method called unapply as one of
its members. The purpose of that unapply method is to match a value and take it apart_  

_the unapply takes an object and tries to give back the arguments_ 

```scala
// inherit from scala function type
object EMail extends ((String, String) => String) {
  // The injection method (optional)
  def apply(user: String, domain: String) = user +"@"+ domain
  // The extraction method (mandatory)
  def unapply(str: String): Option[(String, String)] = {
    val parts = str split "@"
    if (parts.length == 2) Some(parts(0), parts(1)) else None
  } 
}
```

```scala
selectorString match { case EMail(user, domain) => ... }
// would lead to the call:
EMail.unapply(selectorString)
```

# Misc

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
