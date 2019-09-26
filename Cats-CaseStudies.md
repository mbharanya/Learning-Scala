# Case Studies
## Testing Asynchronous Code
(cats/src/Ch8TestingAsyncCode.scala)[Exercise using the Id type]

I first made an error defining the type twice:
```scala
trait RealUptimeClient[Future] extends UptimeClient[Future]{
    def getUptime(hostname: String): Future[Int]
}
trait TestUptimeClient extends UptimeClient[Id]{
    def getUptime(hostname: String): Int
}
```
This is not necessary and caused weird compiler errors.  
Interesting to note here is that Id is `Id[A] => A`, so `Id[Int] == Int`.