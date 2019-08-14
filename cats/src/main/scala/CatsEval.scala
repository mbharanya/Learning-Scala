import cats.Eval

object CatsEval extends App {
  val now = Eval.now(math.random + 1000)
  // now: cats.Eval[Double] = Now(1000.6884369117727)
  val later = Eval.later(math.random + 2000)
  // later: cats.Eval[Double] = cats.Later@71175ee9
  val always = Eval.always(math.random + 3000)
  // always: cats.Eval[Double] = cats.Always@462e2fea


  //  println(later.value)
  //  println(later.value)
  //  println(later.value)
  //
  //  println(always.value)
  //  println(always.value)
  //  println(always.value)


  def foldRightEval[A, B](as: List[A], acc: Eval[B])
                         (fn: (A, Eval[B]) => Eval[B]): Eval[B] =
    as match {
      case head :: tail =>
        Eval.defer(fn(head, foldRightEval(tail, acc)(fn)))
      case Nil =>
        acc
    }

  def foldRight[A, B](as: List[A], acc: B)(fn: (A, B) => B): B = foldRightEval(as, Eval.now(acc)) { (a, b) =>
    b.map(fn(a, _))
  }.value


  foldRight((1 to 100000).toList, 0L)((_ + _))


}
