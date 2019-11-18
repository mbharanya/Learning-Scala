# Kleisli
Like a monad Transformer for function composition.
```scala
val f: Int => Task[String] = ???
val fKleisli: Kleisli[Task, Int, String] = ???
```
The advantage is you can do it withing the context of a Future / Task, etc.

`andThenK` does automatic lifting.

Call `run` to extract again