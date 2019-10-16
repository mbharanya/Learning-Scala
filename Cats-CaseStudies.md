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
[Exercise](cats/src/main/scala/Ch9FoldMap.scala)

## Case Study: Data Validation
## Commutative Replicated Data Types (CRDTs)
_Commutative Replicated Data Types (CRDTs), a family of data structures that can be used to reconcile eventually consistent data._
An eventually consistent system means that at any particular point in time machines are allowed to have differing views of data. However, if all machines can communicate and there are no further updates they will eventually all have the same view of data.

A `GCounter` is a distributed increment-only counter.
For the example of counting the Visitors of a website, given Machine `A` and Machine `B`.
Each machine can only increment it's own counter, but will have the state of the other counters as well. 
At the time of reconcilitation, it will take the max from the other counters.

[See implementation](cats/src/main/scala/Ch11CRDTInt.scala)

Written more compactly, we have:
|Method| Identity| Commutative| Associative| Idempotent|
|------|---------|------------|------------|-----------|
|increment| Y| N| Y| N| 
|merge| Y| Y| Y| Y| 
|total| Y| Y| Y| N|
   
For int:  
- identity: 0 + a == a + 0 == a
- associative: (a + b) + c == a + (b + c)
- commutativity: machine A merging with machine B yields the same result as machine B merging with machine A
- idempotent: a max a = a

From this we can see that
- increment requires a monoid;
- total requires a commutative monoid; and
- merge required an idempotent commutative monoid, also called a
bounded semilatice.