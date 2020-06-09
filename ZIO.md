# ZIO
Fiber-based
100% non-blocking

Everything is just a description, you need to run it, to actually cause an effect
```scala
ZIO[-R, +E, +A] ~ R => Either[E,A]
```
Basically a function from `R` to Failure `E` or success `A`


```scala
type Task[+A] = ZIO[Any, Throwable, A] //does not take anything
type UIO[+A] = ZIO[Any, Nothing, A] //can not fail
type RIO[-R, +A] = ZIO[R, Throwable, A] //can fail, takes parameter
type IO[+E, +A] = ZIO[Any, E, A] // can fail with type `E` or succeed with type `A`
type URIO[-R, +A] = ZIO[R, Nothing, A] // takes parameter, does not fail, returns A
```

```scala
object HelloWorld extends App { //App is also provided by ZIO
  import zio.console._
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    // we need to map to 0, because the return type is `Int`
    putStrLn("Hello World!").map(_ => 0)

    // the same
    putStrLn("Hello World!").as(0)
}
```


```scala
object PrintSequence extends App {
  import zio.console._

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    // does nothing like this
    putStrLn("Hello")
    putStrLn("World!")

    // zip two effects, get tuple from results
    putStrLn("Hello").zipLeft(putStrLn("World!"))

    // zip right
    putStrLn("Hello") *> putStrLn("World!") *> ZIO.succeed(0)
  }
}
```