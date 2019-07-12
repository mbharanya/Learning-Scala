import cats._
import cats.implicits._

object ShowExample extends App{

  val showInt: Show[Int] = Show.apply[Int]
  val showString: Show[String] = Show.apply[String]

  123.show
}

