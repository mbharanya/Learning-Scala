object Ch6ProductOfMonad extends App{
  import cats.Monad
  import cats.syntax.flatMap._ // for flatMap
  import cats.syntax.functor._ // for map
  import cats.instances.list._ // for Semigroupal

  def product[M[_]: Monad, A, B](x: M[A], y: M[B]): M[(A, B)] = for{
    a <- x
    b <- y
  } yield (a,b)

  println(product(List(1, 2), List(3, 4)))
}
