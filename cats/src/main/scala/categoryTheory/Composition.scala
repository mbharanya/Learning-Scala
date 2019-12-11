package categoryTheory

object Composition extends App {
  def identity[A](a: A) = a

  def compose[A, B, C](f1: A => B, f2: B => C) = f1.andThen(f2)

  assert(identity(5) == 5)

  def intToString(int: Int) = int.toString
  def stringToDouble(str: String) = str.toDouble
  val composed = compose(
    intToString,
    stringToDouble
  )
  assert(composed(5) == 5d)
}

