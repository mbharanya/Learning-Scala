import cats.data.EitherT

import scala.concurrent.ExecutionContext.Implicits.global
import cats.instances.future._
import scala.concurrent.duration._

import scala.concurrent.{Await, Future}

object MonadsTransformAndRollOut extends App {
  type Response[A] = EitherT[Future, String, A]

  // defined type alias Response
  def getPowerLevel(autobot: String): Response[Int] = {
    val powerLevels = Map(
      "Jazz" -> 6,
      "Bumblebee" -> 8,
      "Hot Rod" -> 10
    )
    val level = powerLevels.get(autobot).toRight("Unreachable")
    EitherT(Future(level))
  }

  def canSpecialMove(ally1: String, ally2: String): Response[Boolean] = {
    for {
      powerLevel1 <- getPowerLevel(ally1)
      powerLevel2 <- getPowerLevel(ally2)
    } yield (powerLevel1 + powerLevel2) > 15
  }

  def tacticalReport(ally1: String, ally2: String): String = {
    val value: Either[String, Boolean] = Await.result(canSpecialMove(ally1, ally2).value, 1.second)
    value match {
      case Right(true) => "can do the move"
      case Right(false) => "cannot do the moves"
      case _ => "Dude not found"
    }
  }

  println(getPowerLevel("Jazz").value)
  println(getPowerLevel("Not found").value)

  println(s"Has power move ${Await.result(canSpecialMove("Hot Rod", "Jazz").value, 5.seconds)}")

  println(s"Has NO power move ${Await.result(canSpecialMove("Bumblebee", "Jazz").value, 5.seconds)}")

  println(s"Does not find dude ${Await.result(canSpecialMove("Not found", "Jazz").value, 5.seconds)}")


  println(s"Tactical ${tacticalReport("Jazz", "Hot Rod")}")

}