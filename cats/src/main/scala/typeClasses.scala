// 1.3 Exercise: Printable Library

trait Printable[A]{
    def format(a: A): String
}

object PrintableInstances {
  implicit val stringPrintable = new Printable[String] {
    def format(input: String) = input
  }
  implicit val intPrintable = new Printable[Int] {
    def format(input: Int) = input.toString
  } 
}

object Printable{
    def format[A](a: A)(implicit printer: Printable[A]): String = {
        printer.format(a)
    }
    def print[A](a: A)(implicit printer: Printable[A]): Unit = {
        println(format(a))
    }
}


final case class Cat(name: String, age: Int, color: String)

 object Wrapper {
    implicit class PrintableOps[A](value: A){
        def format(implicit p: Printable[A]) = p.format(value)
        def printNew(implicit p: Printable[A]) = println(p.format(value))
    }
 }


object test extends App {
    import PrintableInstances._
    import Wrapper._

    implicit val catPrintable = new Printable[Cat]{
        def format(cat: Cat) = {
            val name  = Printable.format(cat.name)
            val age   = Printable.format(cat.age)
            val color = Printable.format(cat.color)
            s"$name is a $age year-old $color cat.adasd "
        }
    }

    val cat = Cat("New test", 16, "Red")
    cat.printNew
}
