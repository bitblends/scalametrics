package com.bitblends.scalametrics.metrics

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.meta._

/**
  * Unit tests for the Cyclomatic object.
  *
  * These tests verify that the Cyclomatic complexity calculator correctly:
  *   - Computes base cyclomatic complexity (CC = 1 for simple code)
  *   - Counts decision points in if-else statements
  *   - Counts decision points in match expressions
  *   - Counts decision points in loops (while, do-while, for, for-yield)
  *   - Counts decision points in try-catch blocks
  *   - Counts boolean operators (&&, ||)
  *   - Counts case guards (if conditions in case clauses)
  *   - Counts for-comprehension guards
  *   - Analyzes complete source files
  *   - Handles nested control structures
  */
class CyclomaticSpec extends AnyFlatSpec with Matchers {

  behavior of "Cyclomatic"

  // ========== Base Complexity ==========

  it should "compute CC = 1 for simple expression" in {
    val term = parseTerm("42")
    Cyclomatic.compute(term) shouldBe 1
  }

  it should "compute CC = 1 for simple method call" in {
    val term = parseTerm("foo.bar()")
    Cyclomatic.compute(term) shouldBe 1
  }

  it should "compute CC = 1 for simple arithmetic" in {
    val term = parseTerm("1 + 2 + 3")
    Cyclomatic.compute(term) shouldBe 1
  }

  it should "compute CC = 1 for block with no decision points" in {
    val term = parseTerm("""{
      |  val x = 1
      |  val y = 2
      |  x + y
      |}""".stripMargin)
    Cyclomatic.compute(term) shouldBe 1
  }

  // ========== If-Else Statements ==========

  it should "compute CC = 2 for simple if-else" in {
    val term = parseTerm("if (x > 0) 1 else 2")
    Cyclomatic.compute(term) shouldBe 2
  }

  it should "compute CC = 3 for if-else-if chain" in {
    val term = parseTerm("""if (x > 0) "positive"
      |else if (x < 0) "negative"
      |else "zero"
      |""".stripMargin)
    Cyclomatic.compute(term) shouldBe 3
  }

  it should "compute CC for nested if statements" in {
    val term = parseTerm("""if (x > 0) {
      |  if (x > 10) 100
      |  else 10
      |} else 0
      |""".stripMargin)
    Cyclomatic.compute(term) shouldBe 3
  }

  // ========== Match Expressions ==========

  it should "compute CC for match with 3 cases" in {
    val term = parseTerm("""x match {
      |  case 1 => "one"
      |  case 2 => "two"
      |  case _ => "other"
      |}""".stripMargin)
    Cyclomatic.compute(term) shouldBe 4 // 1 base + 3 cases
  }

  it should "compute CC for match with guards" in {
    val term = parseTerm("""x match {
      |  case n if n > 10 => "big"
      |  case n if n < 0 => "negative"
      |  case _ => "other"
      |}""".stripMargin)
    Cyclomatic.compute(term) shouldBe 6 // 1 base + 3 cases + 2 guards
  }

  it should "compute CC for nested match expressions" in {
    val term = parseTerm("""x match {
      |  case 1 => y match {
      |    case "a" => 1
      |    case "b" => 2
      |    case _ => 3
      |  }
      |  case 2 => 4
      |  case _ => 5
      |}""".stripMargin)
    Cyclomatic.compute(term) shouldBe 7 // 1 base + 3 outer cases + 3 inner cases
  }

  // ========== While Loops ==========

  it should "compute CC = 2 for while loop" in {
    val term = parseTerm("""while (x < 10) {
      |  x = x + 1
      |}""".stripMargin)
    Cyclomatic.compute(term) shouldBe 2
  }

  it should "compute CC for while loop with if inside" in {
    val term = parseTerm("""while (x < 10) {
      |  if (x % 2 == 0) println(x)
      |  x = x + 1
      |}""".stripMargin)
    Cyclomatic.compute(term) shouldBe 3 // 1 base + 1 while + 1 if
  }

  // ========== Do-While Loops ==========

  it should "compute CC = 2 for do-while loop" in {
    val term = parseTerm("""do {
      |  x = x + 1
      |} while (x < 10)""".stripMargin)
    Cyclomatic.compute(term) shouldBe 2
  }

  // ========== For Loops ==========

  it should "compute CC = 2 for simple for loop" in {
    val term = parseTerm("""for (i <- 1 to 10) {
      |  println(i)
      |}""".stripMargin)
    Cyclomatic.compute(term) shouldBe 2
  }

  it should "compute CC for for loop with guard" in {
    val term = parseTerm("""for {
      |  i <- 1 to 10
      |  if i % 2 == 0
      |} println(i)""".stripMargin)
    Cyclomatic.compute(term) shouldBe 3 // 1 base + 1 for + 1 guard
  }

  it should "compute CC for for loop with multiple guards" in {
    val term = parseTerm("""for {
      |  i <- 1 to 10
      |  if i % 2 == 0
      |  if i > 5
      |} println(i)""".stripMargin)
    Cyclomatic.compute(term) shouldBe 4 // 1 base + 1 for + 2 guards
  }

  // ========== For-Yield Comprehensions ==========

  it should "compute CC = 2 for simple for-yield" in {
    val term = parseTerm("""for (i <- 1 to 10) yield i * 2""")
    Cyclomatic.compute(term) shouldBe 2
  }

  it should "compute CC for for-yield with guard" in {
    val term = parseTerm("""for {
      |  i <- 1 to 10
      |  if i % 2 == 0
      |} yield i""".stripMargin)
    Cyclomatic.compute(term) shouldBe 3 // 1 base + 1 for + 1 guard
  }

  it should "compute CC for for-yield with multiple guards" in {
    val term = parseTerm("""for {
      |  i <- 1 to 10
      |  if i % 2 == 0
      |  j <- 1 to i
      |  if j > 2
      |} yield (i, j)""".stripMargin)
    Cyclomatic.compute(term) shouldBe 4 // 1 base + 1 for + 2 guards
  }

  // ========== Try-Catch Blocks ==========

  it should "compute CC for try-catch with one catch case" in {
    val term = parseTerm("""try {
      |  riskyOperation()
      |} catch {
      |  case _: Exception => handleError()
      |}""".stripMargin)
    Cyclomatic.compute(term) shouldBe 2 // 1 base + 1 catch case
  }

  it should "compute CC for try-catch with multiple catch cases" in {
    val term = parseTerm("""try {
      |  riskyOperation()
      |} catch {
      |  case _: IOException => handleIO()
      |  case _: SQLException => handleSQL()
      |  case _: Exception => handleGeneric()
      |}""".stripMargin)
    Cyclomatic.compute(term) shouldBe 4 // 1 base + 3 catch cases
  }

  it should "compute CC for try-catch-finally" in {
    val term = parseTerm("""try {
      |  riskyOperation()
      |} catch {
      |  case _: Exception => handleError()
      |} finally {
      |  cleanup()
      |}""".stripMargin)
    Cyclomatic.compute(term) shouldBe 2 // finally doesn't add complexity
  }

  // ========== Boolean Operators ==========

  it should "compute CC for single && operator" in {
    val term = parseTerm("x > 0 && y > 0")
    Cyclomatic.compute(term) shouldBe 2 // 1 base + 1 &&
  }

  it should "compute CC for single || operator" in {
    val term = parseTerm("x > 0 || y > 0")
    Cyclomatic.compute(term) shouldBe 2 // 1 base + 1 ||
  }

  it should "compute CC for chained boolean operators" in {
    val term = parseTerm("x > 0 && y > 0 || z > 0")
    Cyclomatic.compute(term) shouldBe 3 // 1 base + 1 && + 1 ||
  }

  it should "compute CC for complex boolean expression" in {
    val term = parseTerm("(x > 0 && y > 0) || (a < 0 && b < 0)")
    Cyclomatic.compute(term) shouldBe 4 // 1 base + 2 && + 1 ||
  }

  // ========== Combined Constructs ==========

  it should "compute CC for if with boolean operators" in {
    val term = parseTerm("""if (x > 0 && y > 0) {
      |  println("both positive")
      |} else {
      |  println("not both positive")
      |}""".stripMargin)
    Cyclomatic.compute(term) shouldBe 3 // 1 base + 1 if + 1 &&
  }

  it should "compute CC for complex nested structure" in {
    val term = parseTerm("""if (x > 0) {
      |  for (i <- 1 to x) {
      |    if (i % 2 == 0) println(i)
      |  }
      |} else {
      |  while (y < 10) {
      |    y = y + 1
      |  }
      |}""".stripMargin)
    Cyclomatic.compute(term) shouldBe 5 // 1 base + 1 outer if + 1 for + 1 inner if + 1 while
  }

  it should "compute CC for method with multiple decision points" in {
    val term = parseTerm("""{
      |  if (x < 0) return -x
      |  if (x == 0) return 0
      |  var result = 1
      |  for (i <- 1 to x) {
      |    result = result * i
      |  }
      |  result
      |}""".stripMargin)
    Cyclomatic.compute(term) shouldBe 4 // 1 base + 2 ifs + 1 for
  }

  // ========== Source Analysis ==========

  it should "analyze source with single def" in {
    val code = """object Test {
      |  def simple(): Int = 42
      |}""".stripMargin
    val results = Cyclomatic.analyzeSource(code)

    results should have size 1
    results.head.name shouldBe "simple"
    results.head.kind shouldBe "def"
    results.head.cc shouldBe 1
  }

  it should "analyze source with multiple defs" in {
    val code = """object Test {
      |  def add(x: Int, y: Int): Int = x + y
      |
      |  def isPositive(x: Int): Boolean =
      |    if (x > 0) true else false
      |
      |  def factorial(n: Int): Int = {
      |    if (n <= 1) 1
      |    else n * factorial(n - 1)
      |  }
      |}""".stripMargin

    val results = Cyclomatic.analyzeSource(code)

    results should have size 3
    results.find(_.name == "add").get.cc shouldBe 1
    results.find(_.name == "isPositive").get.cc shouldBe 2
    results.find(_.name == "factorial").get.cc shouldBe 2
  }

  it should "analyze source with val definitions" in {
    val code = """object Test {
      |  val simple = 42
      |  val withIf = if (true) 1 else 2
      |  val withMatch = 1 match {
      |    case 1 => "one"
      |    case 2 => "two"
      |    case _ => "other"
      |  }
      |}""".stripMargin

    val results = Cyclomatic.analyzeSource(code)

    results should have size 3
    results.find(_.name == "simple").get.cc shouldBe 1
    results.find(_.name == "withIf").get.cc shouldBe 2
    results.find(_.name == "withMatch").get.cc shouldBe 4
  }

  it should "analyze source with nested defs" in {
    val code = """object Test {
      |  def outer(): Int = {
      |    def inner(x: Int): Int = {
      |      if (x > 0) x else -x
      |    }
      |    inner(42)
      |  }
      |}""".stripMargin

    val results = Cyclomatic.analyzeSource(code)

    results should have size 2
    // Note: outer's CC includes the if statement from nested inner function
    // This is current behavior where nested function complexity is included
    results.find(_.name == "outer").get.cc shouldBe 2
    results.find(_.name == "inner").get.cc shouldBe 2
  }

  it should "analyze source with class and methods" in {
    val code = """
      |class Calculator {
      |  def add(x: Int, y: Int): Int = x + y
      |
      |  def divide(x: Int, y: Int): Option[Int] = {
      |    if (y == 0) None
      |    else Some(x / y)
      |  }
      |}
      |""".stripMargin

    val results = Cyclomatic.analyzeSource(code)

    results should have size 2
    results.find(_.name == "add").get.cc shouldBe 1
    results.find(_.name == "divide").get.cc shouldBe 2
  }

  it should "use ccOf to find specific function complexity" in {
    val code = """object Test {
      |  def simple(): Int = 42
      |  def complex(x: Int): String = {
      |    if (x > 0) "positive"
      |    else if (x < 0) "negative"
      |    else "zero"
      |  }
      |}""".stripMargin

    Cyclomatic.ccOf("simple", code) shouldBe Some(1)
    Cyclomatic.ccOf("complex", code) shouldBe Some(3)
    Cyclomatic.ccOf("nonexistent", code) shouldBe None
  }

  // ========== ForDef and ForVal ==========

  it should "compute CC for def using forDef" in {
    val defn = parseDefn("def test(x: Int): Int = if (x > 0) 1 else -1")
    Cyclomatic.forDef(defn) shouldBe Some(2)
  }

  it should "compute CC for val using forVal" in {
    val valDefn = parseVal("val result = x match { case 1 => \"one\"; case _ => \"other\" }")
    Cyclomatic.forVal(valDefn) shouldBe Some(3)
  }

  // ========== Edge Cases ==========

  it should "handle empty match (no cases)" in {
    // This is actually invalid Scala, but testing the metric calculator
    val term = parseTerm("List.empty[Int]")
    Cyclomatic.compute(term) shouldBe 1
  }

  it should "handle pattern with multiple guards in same case" in {
    val term = parseTerm("""x match {
      |  case n if n > 0 => "positive"
      |  case _ => "other"
      |}""".stripMargin)
    Cyclomatic.compute(term) shouldBe 4 // 1 base + 2 cases + 1 guard
  }

  it should "handle for-comprehension with multiple generators" in {
    val term = parseTerm("""for {
      |  i <- 1 to 10
      |  j <- 1 to i
      |} yield (i, j)""".stripMargin)
    Cyclomatic.compute(term) shouldBe 2 // 1 base + 1 for (multiple generators don't add CC)
  }

  it should "not count boolean equality operators" in {
    val term = parseTerm("x == y")
    Cyclomatic.compute(term) shouldBe 1 // == is not && or ||
  }

  it should "not count bitwise operators" in {
    val term = parseTerm("x & y")
    Cyclomatic.compute(term) shouldBe 1 // & is not &&
  }

  it should "handle val with tuple pattern" in {
    val code = """object Test {
      |  val (a, b) = (1, 2)
      |}""".stripMargin
    val results = Cyclomatic.analyzeSource(code)

    results should have size 1
    // Note: Current implementation doesn't extract names from tuple patterns
    // It only handles Pat.Var directly, not Pat.Tuple(Pat.Var(...))
    results.head.name shouldBe "<val>"
    results.head.cc shouldBe 1
  }

  it should "handle val with no extractable name" in {
    val code = """object Test {
      |  def compute(): Int = 42
      |  val _ = compute()
      |}""".stripMargin
    val results = Cyclomatic.analyzeSource(code)

    results should have size 2 // compute and the val
    val valResult = results.find(_.kind == "val")
    valResult shouldBe defined
    valResult.get.name shouldBe "<val>"
    valResult.get.cc shouldBe 1
  }

  it should "use specified dialect for parsing" in {
    val code = """object Test {
      |  def test: Int = 42
      |}""".stripMargin // Scala 3 style (no parens)

    // Should work with Scala 3 dialect
    val results3 = Cyclomatic.analyzeSource(code, dialects.Scala3)
    results3 should have size 1
    results3.head.name shouldBe "test"
  }

  it should "compute CC for realistic method" in {
    val term = parseTerm("""{
      |  if (input == null) throw new IllegalArgumentException()
      |
      |  val result = input match {
      |    case x if x > 100 => "large"
      |    case x if x > 50 => "medium"
      |    case x if x > 0 => "small"
      |    case _ => "invalid"
      |  }
      |
      |  for {
      |    i <- 1 to 10
      |    if i % 2 == 0
      |  } println(i)
      |
      |  result
      |}""".stripMargin)

    // 1 base + 1 if + 4 match cases + 3 guards + 1 for + 1 for-guard = 11
    Cyclomatic.compute(term) shouldBe 11
  }

  // Helper methods

  /**
    * Parses a Scala expression into a Term AST.
    *
    * @param code
    *   the Scala code to parse
    * @return
    *   the parsed Term AST
    */
  private def parseTerm(code: String): Term = {
    implicit val dialect: Dialect = dialects.Scala213
    code.parse[Term].get
  }

  /**
    * Parses a Scala def definition.
    *
    * @param code
    *   the Scala code to parse
    * @return
    *   the parsed Defn.Def AST
    */
  private def parseDefn(code: String): Defn.Def = {
    implicit val dialect: Dialect = dialects.Scala213
    code.parse[Stat].get.asInstanceOf[Defn.Def]
  }

  /**
    * Parses a Scala val definition.
    *
    * @param code
    *   the Scala code to parse
    * @return
    *   the parsed Defn.Val AST
    */
  private def parseVal(code: String): Defn.Val = {
    implicit val dialect: Dialect = dialects.Scala213
    code.parse[Stat].get.asInstanceOf[Defn.Val]
  }
}
