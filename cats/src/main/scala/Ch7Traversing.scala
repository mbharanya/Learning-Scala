object Ch7Traversing extends App {

  import scala.concurrent._
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global

  val hostnames = List(
    "alpha.example.com",
    "beta.example.com",
    "gamma.demo.com"
  )

  def getUptime(hostname: String): Future[Int] =
    Future(hostname.length * 60) // just for demonstration


  val allUptimes1: Future[List[Int]] = hostnames.foldLeft(Future(List.empty[Int])) {
    (accum, host) =>
      val uptime: Future[Int] = getUptime(host)
      for {
        accum: List[Int] <- accum
        uptime: Int <- uptime
      } yield accum :+ uptime
  }
  Await.result(allUptimes1, 1.second)


  val allUptimes2: Future[List[Int]] =
    Future.traverse(hostnames)(getUptime)
  Await.result(allUptimes2, 1.second)

}
