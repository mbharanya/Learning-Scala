package categoryTheory

object Ch2Types extends App{
  def memoize[A,B](f: A => B) = {
    val cache = collection.mutable.Map.empty[A, B]
    a: A =>
      cache.getOrElse(a, {
        cache.update(a, f(a))
        cache(a)
      })
  }
}
