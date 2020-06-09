import zio._

object PrintSequence extends App {
  import zio.console._

  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    // does nothing like this
    putStrLn("Hello")
    putStrLn("World!")

    // zip two effects, get tuple from results
    putStrLn("Hello").zipLeft(putStrLn("World!"))

    // zip right
    putStrLn("Hello") *> putStrLn("World!") *> ZIO.succeed(ExitCode(0))
  }
}

object ErrorRecovery extends App {
  val StdInputFailed = 1

  import zio.console._

  val failed =
    putStrLn("About to fail...") *>
      ZIO.fail("Uh oh!") *>
      putStrLn("This will NEVER be printed!")


  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    (failed as ExitCode(0)) orElse ZIO.succeed(ExitCode(1))
}
