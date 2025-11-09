package com.bitblends.scalametrics.metrics

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.meta._

/**
  * Unit tests for the ExpressionBranchDensity object.
  *
  * These tests verify that the branch density calculator correctly:
  *   - Counts if-else statements
  *   - Counts match expression cases
  *   - Counts loops (while, do-while, for, for-yield)
  *   - Counts try-catch cases
  *   - Counts boolean operators (&&, ||)
  *   - Computes total branches
  *   - Computes lines of code (LOC)
  *   - Analyzes complete source files
  */
class ExpressionBranchDensitySpec extends AnyFlatSpec with Matchers {

  behavior of "ExpressionBranchDensity"

  // ========== Basic Metrics ==========

  it should "compute zero branches for simple expression" in {
    val term = parseTerm("42")
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.branches shouldBe 0
    metrics.ifCount shouldBe 0
    metrics.caseCount shouldBe 0
    metrics.loopCount shouldBe 0
    metrics.catchCaseCount shouldBe 0
    metrics.boolOpsCount shouldBe 0
  }

  it should "compute zero branches for simple method call" in {
    val term = parseTerm("foo.bar()")
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.branches shouldBe 0
  }

  // ========== If-Else Statements ==========

  it should "count single if-else as 1 branch" in {
    val term = parseTerm("if (x > 0) 1 else 2")
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.ifCount shouldBe 1
    metrics.branches shouldBe 1
  }

  it should "count if-else-if chain correctly" in {
    val term = parseTerm("""if (x > 0) "positive"
      |else if (x < 0) "negative"
      |else "zero"
      |""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.ifCount shouldBe 2 // outer if + inner if in else
    metrics.branches shouldBe 2
  }

  it should "count nested if statements" in {
    val term = parseTerm("""if (x > 0) {
      |  if (x > 10) 100
      |  else 10
      |} else 0
      |""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.ifCount shouldBe 2
    metrics.branches shouldBe 2
  }

  // ========== Match Expressions ==========

  it should "count match cases correctly" in {
    val term = parseTerm("""x match {
      |  case 1 => "one"
      |  case 2 => "two"
      |  case _ => "other"
      |}""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.caseCount shouldBe 3
    metrics.branches shouldBe 3
  }

  it should "count match with guards (guards don't add to branch count)" in {
    val term = parseTerm("""x match {
      |  case n if n > 10 => "big"
      |  case n if n < 0 => "negative"
      |  case _ => "other"
      |}""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.caseCount shouldBe 3
    metrics.branches shouldBe 3 // guards are not counted separately in branch density
  }

  it should "count nested match expressions" in {
    val term = parseTerm("""x match {
      |  case 1 => y match {
      |    case "a" => 1
      |    case "b" => 2
      |    case _ => 3
      |  }
      |  case 2 => 4
      |  case _ => 5
      |}""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.caseCount shouldBe 6 // 3 outer + 3 inner
    metrics.branches shouldBe 6
  }

  // ========== Loops ==========

  it should "count while loop as 1 branch" in {
    val term = parseTerm("""while (x < 10) {
      |  x = x + 1
      |}""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.loopCount shouldBe 1
    metrics.branches shouldBe 1
  }

  it should "count do-while loop as 1 branch" in {
    val term = parseTerm("""do {
      |  x = x + 1
      |} while (x < 10)""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.loopCount shouldBe 1
    metrics.branches shouldBe 1
  }

  it should "count for loop as 1 branch" in {
    val term = parseTerm("""for (i <- 1 to 10) {
      |  println(i)
      |}""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.loopCount shouldBe 1
    metrics.branches shouldBe 1
  }

  it should "count for-yield as 1 branch" in {
    val term = parseTerm("""for (i <- 1 to 10) yield i * 2""")
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.loopCount shouldBe 1
    metrics.branches shouldBe 1
  }

  it should "count while loop with if inside" in {
    val term = parseTerm("""while (x < 10) {
      |  if (x % 2 == 0) println(x)
      |  x = x + 1
      |}""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.loopCount shouldBe 1
    metrics.ifCount shouldBe 1
    metrics.branches shouldBe 2
  }

  // ========== Try-Catch Blocks ==========

  it should "count try-catch with one catch case" in {
    val term = parseTerm("""try {
      |  riskyOperation()
      |} catch {
      |  case _: Exception => handleError()
      |}""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.catchCaseCount shouldBe 1
    metrics.branches shouldBe 1
  }

  it should "count try-catch with multiple catch cases" in {
    val term = parseTerm("""try {
      |  riskyOperation()
      |} catch {
      |  case _: IOException => handleIO()
      |  case _: SQLException => handleSQL()
      |  case _: Exception => handleGeneric()
      |}""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.catchCaseCount shouldBe 3
    metrics.branches shouldBe 3
  }

  it should "count try-catch-finally (finally doesn't add branches)" in {
    val term = parseTerm("""try {
      |  riskyOperation()
      |} catch {
      |  case _: Exception => handleError()
      |} finally {
      |  cleanup()
      |}""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.catchCaseCount shouldBe 1
    metrics.branches shouldBe 1
  }

  // ========== Boolean Operators ==========

  it should "count single && operator" in {
    val term = parseTerm("x > 0 && y > 0")
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.boolOpsCount shouldBe 1
    metrics.branches shouldBe 1
  }

  it should "count single || operator" in {
    val term = parseTerm("x > 0 || y > 0")
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.boolOpsCount shouldBe 1
    metrics.branches shouldBe 1
  }

  it should "count chained boolean operators" in {
    val term = parseTerm("x > 0 && y > 0 || z > 0")
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.boolOpsCount shouldBe 2 // 1 && + 1 ||
    metrics.branches shouldBe 2
  }

  it should "count complex boolean expression" in {
    val term = parseTerm("(x > 0 && y > 0) || (a < 0 && b < 0)")
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.boolOpsCount shouldBe 3 // 2 && + 1 ||
    metrics.branches shouldBe 3
  }

  it should "not count boolean equality operators" in {
    val term = parseTerm("x == y")
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.boolOpsCount shouldBe 0
    metrics.branches shouldBe 0
  }

  it should "not count bitwise operators" in {
    val term = parseTerm("x & y")
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.boolOpsCount shouldBe 0
    metrics.branches shouldBe 0
  }

  // ========== Combined Constructs ==========

  it should "count if with boolean operators" in {
    val term = parseTerm("""if (x > 0 && y > 0) {
      |  println("both positive")
      |} else {
      |  println("not both positive")
      |}""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.ifCount shouldBe 1
    metrics.boolOpsCount shouldBe 1
    metrics.branches shouldBe 2
  }

  it should "count complex nested structure" in {
    val term = parseTerm("""if (x > 0) {
      |  for (i <- 1 to x) {
      |    if (i % 2 == 0) println(i)
      |  }
      |} else {
      |  while (y < 10) {
      |    y = y + 1
      |  }
      |}""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.ifCount shouldBe 2
    metrics.loopCount shouldBe 2
    metrics.branches shouldBe 4
  }

  it should "count method with multiple decision points" in {
    val term = parseTerm("""{
      |  if (x < 0) return -x
      |  if (x == 0) return 0
      |  var result = 1
      |  for (i <- 1 to x) {
      |    result = result * i
      |  }
      |  result
      |}""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.ifCount shouldBe 2
    metrics.loopCount shouldBe 1
    metrics.branches shouldBe 3
  }

  // ========== Lines of Code (LOC) ==========

  it should "compute LOC for single-line expression" in {
    val term = parseTerm("42")
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.loc shouldBe 1
  }

  it should "compute LOC for multi-line block" in {
    val term = parseTerm("""{
      |  val x = 1
      |  val y = 2
      |  x + y
      |}""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.loc shouldBe 5 // 5 lines from { to }
  }

  it should "compute LOC for if-else statement" in {
    val term = parseTerm("""if (x > 0) {
      |  println("positive")
      |} else {
      |  println("non-positive")
      |}""".stripMargin)
    val metrics = ExpressionBranchDensity.compute(term)

    metrics.loc shouldBe 5
  }

  // ========== Source Analysis ==========

  it should "analyze source with single def" in {
    val code = """object Test {
      |  def simple(): Int = 42
      |}""".stripMargin
    val results = ExpressionBranchDensity.analyzeSource(code)

    results should have size 1
    results.head.name shouldBe "simple"
    results.head.kind shouldBe "def"
    results.head.m.branches shouldBe 0
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

    val results = ExpressionBranchDensity.analyzeSource(code)

    results should have size 3
    results.find(_.name == "add").get.m.branches shouldBe 0
    results.find(_.name == "isPositive").get.m.branches shouldBe 1
    results.find(_.name == "factorial").get.m.branches shouldBe 1
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

    val results = ExpressionBranchDensity.analyzeSource(code)

    results should have size 3
    results.find(_.name == "simple").get.m.branches shouldBe 0
    results.find(_.name == "withIf").get.m.branches shouldBe 1
    results.find(_.name == "withMatch").get.m.branches shouldBe 3
  }

  it should "use forDef to compute metrics for method definition" in {
    val defn = parseDefn("def test(x: Int): Int = if (x > 0) 1 else -1")
    val metrics = ExpressionBranchDensity.forDef(defn)

    metrics shouldBe defined
    metrics.get.ifCount shouldBe 1
    metrics.get.branches shouldBe 1
  }

  it should "use forVal to compute metrics for val definition" in {
    val valDefn = parseVal("val result = x match { case 1 => \"one\"; case _ => \"other\" }")
    val metrics = ExpressionBranchDensity.forVal(valDefn)

    metrics shouldBe defined
    metrics.get.caseCount shouldBe 2
    metrics.get.branches shouldBe 2
  }

  it should "handle val with tuple pattern" in {
    val code = """object Test {
      |  val (a, b) = (1, 2)
      |}""".stripMargin
    val results = ExpressionBranchDensity.analyzeSource(code)

    results should have size 1
    results.head.name shouldBe "<val>"
    results.head.m.branches shouldBe 0
  }

  it should "compute realistic method branch density" in {
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

    val metrics = ExpressionBranchDensity.compute(term)

    // 1 if + 4 match cases + 1 for = 6 branches
    metrics.branches shouldBe 6
    metrics.ifCount shouldBe 1
    metrics.caseCount shouldBe 4
    metrics.loopCount shouldBe 1
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
