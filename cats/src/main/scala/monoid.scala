//trait Semigroup[A] {
//  def combine(x: A, y: A): A
//}
//
//trait Monoid[A] extends Semigroup[A] {
//  def empty: A
//}
//
//object Monoid {
//  def apply[A](implicit monoid: Monoid[A]) =
//    monoid
//}

//implicit val booleanAndMonoid: Monoid[Boolean] = new Monoid[Boolean] {
//  def combine(a: Boolean, b: Boolean) = a && b
//
//  def empty = true
//}