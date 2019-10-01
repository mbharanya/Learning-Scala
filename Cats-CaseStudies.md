# Case Studies
## Testing Asynchronous Code
[Exercise using the Id type](cats/src/Ch8TestingAsyncCode.scala)

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

## Case Study: Map-Reduce
### Implementing foldMap
```scala
  def parallelFoldMap[A, B: Monoid]
  (values: Vector[A])
  (func: A => B): Future[B] = {
    val numCores = Runtime.getRuntime.availableProcessors
    val groupSize = (1.0 * values.size / numCores).ceil.toInt
    val batches: Seq[Vector[A]] = values.grouped(groupSize).toVector
    val futures: Vector[Future[B]] = batches.map(b => Future(foldMap(b)(func))).toVector
    foldMap(futures)(func)
  }
```
This was my first try. I wasn't able to figure out what the last step should be after getting a `Vector[Future[B]]`, as I need some way to apply `A => B` to `Vector[Future[B]]`.
The book suggest this:
```scala
 Future.sequence(futures) map { iterable =>
    iterable.foldLeft(Monoid[B].empty)(_ |+| _)
}
```
[Exercise](cats/src/Ch9FoldMap.scala)

## Case Study: Data Validation
