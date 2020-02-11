object Roman {
  val definiton = Map(
    "M" -> 1000,
    "CM" -> 900,
    "D" -> 500,
    "CD" -> 400,
    "C" -> 100,
    "XC" -> 90,
    "L" -> 50,
    "XL" -> 40,
    "X" -> 10,
    "IX" -> 9,
    "V" -> 5,
    "IV" -> 4,
    "I" -> 1,
  )

  def decode(roman: String): Int = {
    roman.foldLeft { (acc: Int, char: char) => {
      if (chars._1 == chars._2) {
        acc + 2 * definiton(chars._1)
      } else if (definiton(chars._1) < definiton(chars._2)) {
        acc
      }
    }
    }
  }


}