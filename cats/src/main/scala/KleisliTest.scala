import cats.data._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cats.instances.future._


object KleisliTest extends App {
  val getNumberFromDb: Unit => Future[Int] = _ => Future.successful(2)
  val processNumber: Int => Future[Int] = num => Future.successful(num * 2)
  val persistToDb: Int => Future[Boolean] = _ => Future.successful(true)


  val kleisliCombo: Kleisli[Future, Unit, Boolean] = Kleisli(getNumberFromDb) andThen processNumber andThen persistToDb

  val unpacked: Unit => Future[Boolean] = kleisliCombo.run

  unpacked().map(println)
}
