object Folding extends App {
  List(1, 2, 3).foldLeft(List.empty[Int])((a, i) => i :: a)
  List(1, 2, 3).foldRight(List.empty[Int])((i, a) => i :: a)
}
