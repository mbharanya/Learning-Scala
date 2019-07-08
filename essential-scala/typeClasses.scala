// 7.3.4.1 Equality

case class Person(name: String, email: String)

trait Equal[A]{
    def equals(a: A, b: A): Boolean
}

object PersonEquality extends Equal[Person] {
    def equals(person1: Person, person2: Person) = person1.email == person2.email
}