import cats.Applicative

object Ch8TestingAsyncCode extends App{
  import scala.language.higherKinds
  import cats.Id
  import scala.concurrent.Future
  import cats.instances.future._  // for Applicative
  import cats.instances.list._ // for Traverse
  import cats.syntax.traverse._ // for traverse
  import scala.concurrent.ExecutionContext.Implicits.global
  import cats.syntax.functor._ // for map
  import cats.Applicative


  trait UptimeClient[F[_]] {
    def getUptime(hostname: String): F[Int]
  }

  trait RealUptimeClient extends UptimeClient[Future]{
    def getUptime(hostname: String): Future[Int]
  }

  class UptimeService[F[_]](client: UptimeClient[F])(implicit a: Applicative[F]) {
    def getTotalUptime(hostnames: List[String]): F[Int] =
          hostnames.traverse(client.getUptime).map(_.sum) }

  class TestUptimeClient(hosts: Map[String, Int]) extends UptimeClient[Id] {
    def getUptime(hostname: String): Int =
      hosts.getOrElse(hostname, 0)
  }

  def testTotalUptime() = {
    val hosts = Map("host1" -> 10, "host2" -> 6)
    val client = new TestUptimeClient(hosts)
    val service = new UptimeService(client)
    val actual = service.getTotalUptime(hosts.keys.toList)
    val expected = hosts.values.sum
    assert(actual == expected)
  }
  testTotalUptime()

}


