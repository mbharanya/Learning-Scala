# Category: The Essence of Composition
A category consists of objects and arrows that go between them.
_If you have an arrow from object 𝐴 to object 𝐵, and another arrow from object 𝐵 to object 𝐶, then there must be an arrow — their composition — that goes from 𝐴 to 𝐶._

`->` = morphism
```scala
val f: A => B
val g: B => C
g compose f
```

`::` in Haskell means 'has the type of'
## Composition
Composition is associative
```scala
val f : A =>B 
val g: B =>C 
val h: C =>D
h compose (g compose f) === (h compose g) compose f === h compose g compose f
```
Identity:
```
𝑓 ∘ id𝐴 = 𝑓
id𝐵 ∘ 𝑓 = 𝑓
```
```scala
def identity[A](a: A): A = a
f compose identity[A] == f
identity[B] _ compose f == f
```
_A category consists of objects and arrows (morphisms). Arrows can be composed, and the composition is associative. Every object has an identity arrow that serves as a unit under composition._
_An object in category theory is an abstract nebulous entity. All you can ever know about it is how it relates to other objects —    how it connects with them using arrows._
# Types
```scala
val x: BigInt
```
`BigInt` is an infinite set, of which x is a part of.

There is a type called bottom ( _|_, or Unicode ⊥), which signifies that a function may not terminate.
Functions that may return bottom, are called partial (as opposed to total functions).

```scala
val fact = (n: Int) => (1 to n).toList.product
```
vs C
```C
int fact(int n) {
    int i;
    int result = 1;
    for (i = 2; i <= n; ++i)
        result *= i;
    return result;
}
```
Much closer to the math definition!

_A mathematical function is just a mapping of values to values._
_functions that always produce the same result given the same input and have no side effects are called pure functions._

You can even build functions that take an empty set (Set as discussed before):
```scala
def absurd[A]: Nothing => A
```
This will never be able to be called though, as you would need to provide something with the type Nothing as an argument.
This different to taking no arguments or `()` / `Unit`

```scala
val f44: Unit => BigInt = _ => 44
```

```scala
val fInt: BigInt => Unit = _ => ()
```
Notice that this function doesn't depend on the input, not even it's type.
_Functions that can be implemented with the same formula for any type are called parametrically polymorphic._

```scala
sealed trait Bool
final case object True extends Bool 
final case object False extends Bool
```
This is an ADT, Bool can be either True or False.
_Functions to Bool are called predicates_

# Categories Great and Small
## Detour - what is a graph?
A graph is the set of values input->output of a function.
```
y = x * 2
```
Graph: 
|Input|Output|
|-|-|
|0|0|
|1|2|
|2|4|
This can also be plotted to a graph, therefore the name.
