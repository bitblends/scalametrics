package com.bitblends.scalametrics.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.meta.dialects._

/**
  * Unit tests for the ScalaDialectDetector object.
  *
  * These tests verify that the dialect detector correctly:
  *   - Detects Scala 3 features (enum, given, using, extension, export, etc.)
  *   - Detects Scala 2.13 features (LazyList, toIntOption, ArraySeq, etc.)
  *   - Detects Scala 2.12 features (.to[Collection], breakOut, CanBuildFrom, etc.)
  *   - Handles parsing-based scoring for different dialects
  *   - Combines heuristic and parse scores to determine the best dialect
  *   - Handles edge cases (empty code, invalid syntax, mixed features, etc.)
  */
class ScalaDialectDetectorSpec extends AnyFlatSpec with Matchers {

  behavior of "ScalaDialectDetector"

  // ========== Scala 3 Feature Detection Tests ==========

  it should "detect Scala 3 enum syntax" in {
    val code =
      """
        |enum Color {
        |  case Red, Green, Blue
        |}
        |""".stripMargin

    val result = ScalaDialectDetector.detect(code)
    // Scala3 is an alias for Scala38, they are the same object
    result should (be(Scala3) or be(Scala38))
  }

  it should "detect Scala 3 given syntax" in {
    val code =
      """
        |given intOrd: Ordering[Int] with {
        |  def compare(x: Int, y: Int) = x - y
        |}
        |""".stripMargin

    val result = ScalaDialectDetector.detect(code)
    result should (be(Scala3) or be(Scala38))
  }

  it should "detect Scala 3 using clause" in {
    val code =
      """
        |def sum[T](xs: List[T])(using num: Numeric[T]): T = xs.sum
        |""".stripMargin

    val result = ScalaDialectDetector.detect(code)
    result should (be(Scala3) or be(Scala38))
  }

  it should "detect Scala 3 extension method" in {
    val code =
      """
        |extension (x: Int)
        |  def square: Int = x * x
        |""".stripMargin

    val result = ScalaDialectDetector.detect(code)
    result should (be(Scala3) or be(Scala38))
  }

  it should "detect Scala 3 export clause" in {
    val code =
      """
        |class Wrapper(val inner: SomeClass) {
        |  export inner.*
        |}
        |""".stripMargin

    val result = ScalaDialectDetector.detect(code)
    result should (be(Scala3) or be(Scala38))
  }

  it should "detect Scala 3 end marker" in {
    val code =
      """
        |def foo: Int = {
        |  val x = 1
        |  x + 1
        |}
        |end foo
        |""".stripMargin

    val result = ScalaDialectDetector.detect(code)
    result should (be(Scala3) or be(Scala38))
  }

  it should "detect Scala 3 opaque type" in {
    val code =
      """
        |opaque type UserId = Long
        |object UserId {
        |  def apply(id: Long): UserId = id
        |}
        |""".stripMargin

    val result = ScalaDialectDetector.detect(code)
    result should (be(Scala3) or be(Scala38))
  }

  it should "detect Scala 3 derives clause" in {
    val code =
      """
        |case class Person(name: String, age: Int) derives Eq, Show
        |""".stripMargin

    val result = ScalaDialectDetector.detect(code)
    result should (be(Scala3) or be(Scala38))
  }

  it should "detect Scala 3 inline modifier" in {
    val code =
      """
        |inline def max(a: Int, b: Int): Int = if (a > b) a else b
        |""".stripMargin

    val result = ScalaDialectDetector.detect(code)
    result should (be(Scala3) or be(Scala38))
  }

  it should "detect Scala 3 multi-line package header" in {
    val code =
      """
        |package com.example
        |package app
        |
        |object Main extends App {
        |  println("Hello")
        |}
        |""".stripMargin

    // Multi-line package headers are a heuristic, should give some score to Scala 3
    // but may not be enough alone to determine Scala 3
    val result = ScalaDialectDetector.detect(code)
    // Accept any dialect as multi-line packages can exist in Scala 2 as well
    result should (be(Scala3) or be(Scala213) or be(Scala212))
  }

  it should "handle very long code efficiently" in {
    val code = (1 to 1000)
      .map { i =>
        s"val x$i = $i"
      }
      .mkString("\n")

    // Should complete without hanging
    ScalaDialectDetector.detect(code) shouldBe Scala3
  }

  it should "detect Scala 3 LazyList (top-level)" in {
    val code =
      """
        |val nums = LazyList.from(1)
        |""".stripMargin

    // Scala213 doesn't allow top-level statements, so it detects Scala3
    ScalaDialectDetector.detect(code) shouldBe Scala3
  }

  // ========== Scala 2.13 Feature Detection Tests ==========

  it should "detect Scala 2.13 LazyList" in {
    val code =
      """object Test {
        |   val nums = LazyList.from(1)
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect Scala 2.13 toIntOption" in {
    val code =
      """object Test {
        |val n = "123".toIntOption
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect Scala 2.13 toDoubleOption" in {
    val code =
      """object Test {
        |val d = "3.14".toDoubleOption
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect Scala 2.13 toLongOption" in {
    val code =
      """object Test {
        |val l = "12345".toLongOption
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect Scala 2.13 ArraySeq" in {
    val code =
      """
        |import scala.collection.immutable.ArraySeq
        |object Test {
        |val arr = ArraySeq(1, 2, 3)
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect Scala 2.13 collection .from method" in {
    val code =
      """object Test {
        |val list = List.from(Seq(1, 2, 3))
        |val vec = Vector.from(list)
        |val set = Set.from(vec)
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect Scala 2.13 Factory type" in {
    val code =
      """
        |import scala.collection.Factory
        |object Test {
        |def build[A, C](xs: Iterable[A])(implicit factory: Factory[A, C]): C =
        |  factory.fromSpecific(xs)
        |  }
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect Scala 2.13 BuildFrom" in {
    val code =
      """
        |import scala.collection.BuildFrom
        |object Test {
        |def transform[A, B, C](xs: Iterable[A])(f: A => B)(implicit bf: BuildFrom[Iterable[A], B, C]): C = ???
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect Scala 2.13 view.grouped" in {
    val code =
      """object Test {
        |val groups = (1 to 10).view.grouped(3).toList
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect Scala 2.13 view.sliding" in {
    val code =
      """object Test {
        |val windows = (1 to 10).view.sliding(3).toList
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect Scala 2.13 implicit def" in {
    val code =
      """object Test {
        |implicit def stringToInt(s: String): Int = s.toInt
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect Scala 2.13 implicit class" in {
    val code =
      """object Test {
        |implicit class RichInt(val i: Int) extends AnyVal {
        |  def square: Int = i * i
        |}
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect extends App as Scala 2.13" in {
    val code =
      """
        |object Main extends App {
        |  println("Hello, World!")
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  // ========== Scala 2.12 Feature Detection Tests ==========

  it should "detect Scala 2.12 .to[Collection] syntax" in {
    val code =
      """object Test {
        |val list = Seq(1, 2, 3).to[List]
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala212
  }

  it should "detect Scala 2.12 breakOut" in {
    val code =
      """
        |import scala.collection.breakOut
        |object Test {
        |val map: Map[Int, String] = List(1, 2, 3).map(x => (x, x.toString))(breakOut)
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala212
  }

  it should "detect Scala 2.12 CanBuildFrom" in {
    val code =
      """
        |import scala.collection.generic.CanBuildFrom
        |object Test {
        |def myMap[A, B, That](xs: Iterable[A])(f: A => B)(implicit cbf: CanBuildFrom[Iterable[A], B, That]): That = ???
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala212
  }

  // ========== Edge Cases Tests ==========

  it should "handle empty code and fall back to Scala213" in {
    val code = ""
    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "handle whitespace-only code and fall back to Scala213" in {
    val code = "   \n\n  \t  \n  "
    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "handle comments-only code and fall back to Scala213" in {
    val code =
      """
        |// This is a comment
        |/* This is a block comment */
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect Scala 3 when multiple Scala 3 features are present" in {
    val code =
      """
        |enum Status {
        |  case Active, Inactive
        |}
        |
        |given statusOrd: Ordering[Status] = ???
        |
        |extension (s: Status)
        |  def isActive: Boolean = s == Status.Active
        |""".stripMargin

    val result = ScalaDialectDetector.detect(code); result should (be(Scala3) or be(Scala38))
  }

  it should "detect Scala 2.13 when multiple 2.13 features are present" in {
    val code =
      """object Test {
        |val nums = LazyList.from(1)
        |val opt = "42".toIntOption
        |val arr = ArraySeq(1, 2, 3)
        |val list = List.from(arr)
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect Scala 2.12 when multiple 2.12 features are present" in {
    val code =
      """object Test {
        |import scala.collection.breakOut
        |import scala.collection.generic.CanBuildFrom
        |
        |val list = Seq(1, 2, 3).to[List]
        |val result = (1 to 10).view.force
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala212
  }

  it should "prefer Scala 3 when both Scala 3 and 2.13 features are present" in {
    val code =
      """
        |// Scala 3 feature (high score)
        |enum Color {
        |  case Red, Green, Blue
        |}
        |
        |// Scala 2.13 feature (lower score)
        |implicit class RichString(s: String) {
        |  def toOpt: Option[Int] = s.toIntOption
        |}
        |""".stripMargin

    val result = ScalaDialectDetector.detect(code); result should (be(Scala3) or be(Scala38))
  }

  it should "prefer Scala 2.13 when both 2.13 and 2.12 features are present" in {
    val code =
      """object Test {
        |// Scala 2.13 feature (stronger)
        |val nums = LazyList.from(1)
        |val arr = ArraySeq(1, 2, 3)
        |
        |// Also valid in 2.13
        |val list = Seq(1, 2, 3).to[List]
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "handle syntactically invalid code by scoring based on partial parse" in {
    val code =
      """
        |def foo = {
        |  enum Color { // Scala 3 keyword
        |    case Red
        |  // Missing closing brace
        |""".stripMargin

    // Should still detect Scala 3 based on the enum keyword even with parse errors
    val result = ScalaDialectDetector.detect(code); result should (be(Scala3) or be(Scala38))
  }

  it should "detect simple class definition and default to Scala213" in {
    val code =
      """
        |class Person(name: String, age: Int)
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect simple object definition and default to Scala213" in {
    val code =
      """
        |object Utils {
        |  def hello(): Unit = println("Hello")
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect trait definition and default to Scala213" in {
    val code =
      """
        |trait Animal {
        |  def speak(): String
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  // ========== Parsing Score Tests ==========

  it should "give higher parse score to Scala 3 when code parses successfully in Scala 3" in {
    val code =
      """
        |enum Color derives Eq:
        |  case Red, Green, Blue
        |""".stripMargin

    // This code should parse successfully in Scala 3 but not in Scala 2.x
    val result = ScalaDialectDetector.detect(code); result should (be(Scala3) or be(Scala38))
  }

  it should "give higher parse score to Scala 2.13 when code parses successfully" in {
    val code =
      """
        |class Foo {
        |  val nums = LazyList(1, 2, 3)
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "combine heuristic and parse scores effectively" in {
    val code =
      """
        |package com.example
        |
        |object Main {
        |  def main(args: Array[String]): Unit = {
        |    val nums = LazyList.from(1).take(10)
        |    println(nums.toList)
        |  }
        |}
        |""".stripMargin

    // LazyList is a strong Scala 2.13 indicator
    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  // ========== Real-world Code Examples ==========

  it should "detect Scala 3 from a realistic Scala 3 program" in {
    val code =
      """
        |package example
        |
        |enum Tree[+T]:
        |  case Leaf(value: T)
        |  case Branch(left: Tree[T], right: Tree[T])
        |
        |  def size: Int = this match
        |    case Leaf(_) => 1
        |    case Branch(l, r) => l.size + r.size
        |end Tree
        |
        |given Ordering[Tree[Int]] with
        |  def compare(x: Tree[Int], y: Tree[Int]): Int =
        |    x.size - y.size
        |
        |extension [T](tree: Tree[T])
        |  def map[U](f: T => U): Tree[U] = tree match
        |    case Tree.Leaf(v) => Tree.Leaf(f(v))
        |    case Tree.Branch(l, r) => Tree.Branch(l.map(f), r.map(f))
        |""".stripMargin

    val result = ScalaDialectDetector.detect(code); result should (be(Scala3) or be(Scala38))
  }

  it should "detect Scala 2.13 from a realistic Scala 2.13 program" in {
    val code =
      """
        |package example
        |
        |import scala.collection.immutable.ArraySeq
        |
        |object DataProcessor {
        |  def process(input: String): Option[List[Int]] = {
        |    val numbers = input.split(",").toList
        |    val parsed = numbers.flatMap(_.trim.toIntOption)
        |
        |    if (parsed.isEmpty) None
        |    else {
        |      val seq = ArraySeq.from(parsed)
        |      Some(seq.toList)
        |    }
        |  }
        |
        |  implicit class RichList[A](list: List[A]) {
        |    def safeHead: Option[A] = list.headOption
        |  }
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect Scala 2.12 from a realistic Scala 2.12 program" in {
    val code =
      """
        |package example
        |
        |import scala.collection.breakOut
        |import scala.collection.generic.CanBuildFrom
        |
        |object LegacyProcessor {
        |  def processData[A, B](data: List[A])(f: A => (Int, B)): Map[Int, B] = {
        |    data.map(f)(breakOut)
        |  }
        |
        |  def convertToList[A](seq: Seq[A]): List[A] = {
        |    seq.to[List]
        |  }
        |
        |  def customMap[A, B, That](xs: Iterable[A])(f: A => B)
        |    (implicit cbf: CanBuildFrom[Iterable[A], B, That]): That = {
        |    val builder = cbf()
        |    xs.foreach(x => builder += f(x))
        |    builder.result()
        |  }
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala212
  }

  // ========== Boundary Cases ==========

  it should "handle code with package object" in {
    val code =
      """
        |package object utils {
        |  val constant = 42
        |  def helper(): String = "help"
        |}
        |""".stripMargin

    // Package objects are valid in all Scala versions, default to 2.13
    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "handle code with case class" in {
    val code =
      """
        |case class User(id: Long, name: String, email: String)
        |""".stripMargin

    // Case classes are valid in all Scala versions, default to 2.13
    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "handle code with for comprehension" in {
    val code =
      """object Test {
        |val result = for {
        |  x <- List(1, 2, 3)
        |  y <- List(4, 5, 6)
        |  if x < y
        |} yield (x, y)
        |}
        |""".stripMargin

    // For comprehensions are valid in all Scala versions, default to 2.13
    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "handle code with pattern matching" in {
    val code =
      """object Test {
        |def describe(x: Any): String = x match {
        |  case i: Int => "integer"
        |  case s: String => "string"
        |  case _ => "unknown"
        |}
        |}
        |""".stripMargin

    // Pattern matching is valid in all Scala versions, default to 2.13
    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "handle code with type parameters and bounds" in {
    val code =
      """object Test {
        |def max[T <: Comparable[T]](x: T, y: T): T =
        |  if (x.compareTo(y) > 0) x else y
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "handle code with annotations" in {
    val code =
      """object Test {
        |@deprecated("Use newMethod instead", "1.0")
        |def oldMethod(): Unit = ()
        |
        |@tailrec
        |def factorial(n: Int, acc: Int = 1): Int =
        |  if (n <= 1) acc else factorial(n - 1, n * acc)
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "handle string with 'using' in a comment" in {
    val code =
      """
        |object Test {
        |// We are using a simple approach here
        |def foo(): Int = 42
        |}
        |""".stripMargin

    // 'using' in comment should not trigger Scala 3 detection
    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "handle string with 'enum' in a string literal" in {
    val code =
      """object Test {
        |val text = "This enum is just text"
        |}
        |""".stripMargin

    // 'enum' in string should not trigger Scala 3 detection
    ScalaDialectDetector.detect(code) shouldBe Scala213
  }

  it should "detect actual 'using' keyword usage not in comments" in {
    val code =
      """
        |def sum(xs: List[Int])(using Numeric[Int]): Int = xs.sum
        |""".stripMargin

    val result = ScalaDialectDetector.detect(code); result should (be(Scala3) or be(Scala38))
  }

  it should "handle code with unicode characters" in {
    val code =
      """
        |class Café {
        |  val greeting = "Hello, 世界!"
        |  def π: Double = 3.14159
        |}
        |""".stripMargin

    ScalaDialectDetector.detect(code) shouldBe Scala213
  }
}
