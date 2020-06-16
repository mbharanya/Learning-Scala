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


```scala
object ErrorRecovery extends App {
  val StdInputFailed = 1

  import zio.console._

  val failed =
    putStrLn("About to fail...") *>
      ZIO.fail("Uh oh!") *> 
      putStrLn("This will NEVER be printed!")


  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    // We have type checking for Error handling here, the compiler will not allow a non-handled Error to go through, as the error Type here is Nothing, so we need to use orElse
    (failed as ExitCode(0)) orElse ZIO.succeed(ExitCode(1))

    //fold to handle, fail, success
    failed.fold(_ => ExitCode(1), _ => ExitCode(2))


    // Cause is a datatype to preserve stacktrace and error info. in this case Cause[String], because it fails with a String error
    (failed as ExitCode(0)).catchAllCause(cause => putStrLn(cause.prettyPrint) as ExitCode(1))
}
```

```scala
object Looping extends App {
  import zio.console._

  // would not be possible with futures, is only possible because ZIO is completely lazy
  def repeat[R, E, A](n: Int)(effect: ZIO[R, E, A]): ZIO[R, E, A] =
    if (n <= 1) effect
    else effect *> repeat(n - 1)(effect)

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    repeat(3)(putStrLn("All work and no play makes Jack a dull boy")) as 0
}
```