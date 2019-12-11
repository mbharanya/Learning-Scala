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
- Composition is associative
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