package com.bitblends.scalametrics.metrics

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.meta._

/**
  * Unit tests for the NestingDepth object.
  *
  * These tests verify that the nesting depth calculator correctly:
  *   - Computes depth for blocks
  *   - Computes depth for if-else statements
  *   - Computes depth for match expressions
  *   - Computes depth for loops (while, do-while, for, for-yield)
  *   - Computes depth for try-catch-finally blocks
  *   - Computes depth for lambdas and partial functions
  *   - Handles nested constructs correctly
  *   - Analyzes complete source files
  */
class NestingDepthSpec extends AnyFlatSpec with Matchers {

  behavior of "NestingDepth"

  // ========== Basic Depth ==========

  it should "compute depth 0 for simple expression" in {
    val term = parseTerm("42")
    NestingDepth.compute(term) shouldBe 0
  }

  it should "compute depth 0 for simple method call" in {
    val term = parseTerm("foo.bar()")
    NestingDepth.compute(term) shouldBe 0
  }

  it should "compute depth 0 for simple arithmetic" in {
    val term = parseTerm("1 + 2 + 3")
    NestingDepth.compute(term) shouldBe 0
  }

  // ========== Blocks ==========

  it should "compute depth 1 for simple block" in {
    val term = parseTerm("""{
      |  val x = 1
      |  val y = 2
      |  x + y
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 1
  }

  it should "compute depth 2 for nested blocks" in {
    val term = parseTerm("""{
      |  val x = 1
      |  {
      |    val y = 2
      |    x + y
      |  }
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  it should "compute depth 3 for triple nested blocks" in {
    val term = parseTerm("""{
      |  val a = 1
      |  {
      |    val b = 2
      |    {
      |      val c = 3
      |      a + b + c
      |    }
      |  }
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 3
  }

  // ========== If-Else Statements ==========

  it should "compute depth 1 for simple if-else" in {
    val term = parseTerm("if (x > 0) 1 else 2")
    NestingDepth.compute(term) shouldBe 1
  }

  it should "compute depth 2 for if-else with block" in {
    val term = parseTerm("""if (x > 0) {
      |  val y = x * 2
      |  y + 1
      |} else 0
      |""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  it should "compute depth 2 for nested if statements" in {
    val term = parseTerm("""if (x > 0) {
      |  if (x > 10) 100
      |  else 10
      |} else 0
      |""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  it should "compute depth 3 for triple nested if statements" in {
    val term = parseTerm("""if (a) {
      |  if (b) {
      |    if (c) 1 else 2
      |  } else 3
      |} else 4
      |""".stripMargin)
    NestingDepth.compute(term) shouldBe 3
  }

  it should "compute depth for if-else-if chain" in {
    val term = parseTerm("""if (x > 0) "positive"
      |else if (x < 0) "negative"
      |else "zero"
      |""".stripMargin)
    NestingDepth.compute(term) shouldBe 1 // each if/else is depth 1
  }

  // ========== Match Expressions ==========

  it should "compute depth 1 for simple match" in {
    val term = parseTerm("""x match {
      |  case 1 => "one"
      |  case 2 => "two"
      |  case _ => "other"
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 1
  }

  it should "compute depth 2 for match with block in case body" in {
    val term = parseTerm("""x match {
      |  case 1 => {
      |    val y = 100
      |    y + 1
      |  }
      |  case _ => 0
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  it should "compute depth 2 for nested match expressions" in {
    val term = parseTerm("""x match {
      |  case 1 => y match {
      |    case "a" => 1
      |    case "b" => 2
      |    case _ => 3
      |  }
      |  case 2 => 4
      |  case _ => 5
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  it should "compute depth for match with guards" in {
    val term = parseTerm("""x match {
      |  case n if n > 10 => "big"
      |  case n if n < 0 => "negative"
      |  case _ => "other"
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 1
  }

  // ========== While Loops ==========

  it should "compute depth 1 for simple while loop" in {
    val term = parseTerm("""while (x < 10) {
      |  x = x + 1
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 1
  }

  it should "compute depth 2 for while loop with block" in {
    val term = parseTerm("""while (x < 10) {
      |  {
      |    val y = x * 2
      |    println(y)
      |  }
      |  x = x + 1
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  it should "compute depth 2 for while loop with if inside" in {
    val term = parseTerm("""while (x < 10) {
      |  if (x % 2 == 0) println(x)
      |  x = x + 1
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  it should "compute depth 2 for nested while loops" in {
    val term = parseTerm("""while (x < 10) {
      |  while (y < 5) {
      |    y = y + 1
      |  }
      |  x = x + 1
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  // ========== Do-While Loops ==========

  it should "compute depth 1 for simple do-while loop" in {
    val term = parseTerm("""do {
      |  x = x + 1
      |} while (x < 10)""".stripMargin)
    NestingDepth.compute(term) shouldBe 1
  }

  it should "compute depth 2 for do-while with nested block" in {
    val term = parseTerm("""do {
      |  {
      |    val y = x * 2
      |    println(y)
      |  }
      |  x = x + 1
      |} while (x < 10)""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  // ========== For Loops ==========

  it should "compute depth 1 for simple for loop" in {
    val term = parseTerm("""for (i <- 1 to 10) {
      |  println(i)
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 1
  }

  it should "compute depth 2 for for loop with nested block" in {
    val term = parseTerm("""for (i <- 1 to 10) {
      |  {
      |    val sq = i * i
      |    println(sq)
      |  }
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  it should "compute depth 2 for nested for loops" in {
    val term = parseTerm("""for (i <- 1 to 10) {
      |  for (j <- 1 to i) {
      |    println((i, j))
      |  }
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  it should "compute depth for for loop with guard" in {
    val term = parseTerm("""for {
      |  i <- 1 to 10
      |  if i % 2 == 0
      |} println(i)""".stripMargin)
    NestingDepth.compute(term) shouldBe 1
  }

  // ========== For-Yield Comprehensions ==========

  it should "compute depth 1 for simple for-yield" in {
    val term = parseTerm("""for (i <- 1 to 10) yield i * 2""")
    NestingDepth.compute(term) shouldBe 1
  }

  it should "compute depth for for-yield with guard" in {
    val term = parseTerm("""for {
      |  i <- 1 to 10
      |  if i % 2 == 0
      |} yield i""".stripMargin)
    NestingDepth.compute(term) shouldBe 1
  }

  it should "compute depth 2 for for-yield with nested expression" in {
    val term = parseTerm("""for (i <- 1 to 10) yield {
      |  val sq = i * i
      |  sq + 1
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  // ========== Try-Catch-Finally Blocks ==========

  it should "compute depth 1 for simple try-catch" in {
    val term = parseTerm("""try {
      |  riskyOperation()
      |} catch {
      |  case _: Exception => handleError()
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 1
  }

  it should "compute depth 2 for try-catch with nested block in try" in {
    val term = parseTerm("""try {
      |  {
      |    val x = compute()
      |    process(x)
      |  }
      |} catch {
      |  case _: Exception => handleError()
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  it should "compute depth 2 for try-catch with nested block in catch" in {
    val term = parseTerm("""try {
      |  riskyOperation()
      |} catch {
      |  case _: Exception => {
      |    val msg = "Error occurred"
      |    log(msg)
      |  }
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  it should "compute depth for try-catch-finally" in {
    val term = parseTerm("""try {
      |  riskyOperation()
      |} catch {
      |  case _: Exception => handleError()
      |} finally {
      |  cleanup()
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 1
  }

  it should "compute depth 2 for try-catch-finally with nested finally block" in {
    val term = parseTerm("""try {
      |  riskyOperation()
      |} catch {
      |  case _: Exception => handleError()
      |} finally {
      |  {
      |    val res = cleanup()
      |    log(res)
      |  }
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  // ========== Lambdas and Functions ==========

  it should "compute depth 1 for simple lambda" in {
    val term = parseTerm("(x: Int) => x * 2")
    NestingDepth.compute(term) shouldBe 1
  }

  it should "compute depth 2 for lambda with block body" in {
    val term = parseTerm("""(x: Int) => {
      |  val sq = x * x
      |  sq + 1
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  it should "compute depth 2 for nested lambdas" in {
    val term = parseTerm("(x: Int) => (y: Int) => x + y")
    NestingDepth.compute(term) shouldBe 2
  }

  it should "compute depth for lambda in higher-order function" in {
    val term = parseTerm("List(1, 2, 3).map(x => x * 2)")
    NestingDepth.compute(term) shouldBe 1
  }

  // ========== Partial Functions ==========

  it should "compute depth 1 for simple partial function" in {
    val term = parseTerm("""{ case 1 => "one"; case 2 => "two" }""")
    NestingDepth.compute(term) shouldBe 1
  }

  it should "compute depth 2 for partial function with block in case body" in {
    val term = parseTerm("""{
      |  case 1 => {
      |    val msg = "one"
      |    msg.toUpperCase
      |  }
      |  case 2 => "two"
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 2
  }

  // ========== Complex Nested Structures ==========

  it should "compute depth for complex nested structure" in {
    val term = parseTerm("""if (x > 0) {
      |  for (i <- 1 to x) {
      |    if (i % 2 == 0) {
      |      println(i)
      |    }
      |  }
      |} else {
      |  while (y < 10) {
      |    y = y + 1
      |  }
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 3
  }

  it should "compute depth for deeply nested blocks" in {
    val term = parseTerm("""{
      |  val a = 1
      |  {
      |    val b = 2
      |    {
      |      val c = 3
      |      {
      |        val d = 4
      |        a + b + c + d
      |      }
      |    }
      |  }
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 4
  }

  it should "compute depth for match inside for inside if" in {
    val term = parseTerm("""if (condition) {
      |  for (item <- items) {
      |    item match {
      |      case Some(x) => process(x)
      |      case None => skip()
      |    }
      |  }
      |}""".stripMargin)
    NestingDepth.compute(term) shouldBe 3
  }

  // ========== Source Analysis ==========

  it should "analyze source with single def" in {
    val code = """object Test {
      |  def simple(): Int = 42
      |}""".stripMargin
    val results = NestingDepth.analyzeSource(code)

    results should have size 1
    results.head.name shouldBe "simple"
    results.head.kind shouldBe "def"
    results.head.depth shouldBe 0
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

    val results = NestingDepth.analyzeSource(code)

    results should have size 3
    results.find(_.name == "add").get.depth shouldBe 0
    results.find(_.name == "isPositive").get.depth shouldBe 1
    results.find(_.name == "factorial").get.depth shouldBe 2 // block + if
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

    val results = NestingDepth.analyzeSource(code)

    results should have size 3
    results.find(_.name == "simple").get.depth shouldBe 0
    results.find(_.name == "withIf").get.depth shouldBe 1
    results.find(_.name == "withMatch").get.depth shouldBe 1
  }

  it should "use forDef to compute depth for method definition" in {
    val defn = parseDefn("def test(x: Int): Int = if (x > 0) 1 else -1")
    val depth = NestingDepth.forDef(defn)

    depth shouldBe Some(1)
  }

  it should "use forVal to compute depth for val definition" in {
    val valDefn = parseVal("val result = x match { case 1 => \"one\"; case _ => \"other\" }")
    val depth = NestingDepth.forVal(valDefn)

    depth shouldBe Some(1)
  }

  it should "handle val with tuple pattern" in {
    val code = """object Test {
      |  val (a, b) = (1, 2)
      |}""".stripMargin
    val results = NestingDepth.analyzeSource(code)

    results should have size 1
    results.head.name shouldBe "<val>"
    results.head.depth shouldBe 0
  }

  it should "compute depth for realistic method" in {
    val term = parseTerm("""{
      |  if (input == null) throw new IllegalArgumentException()
      |
      |  val result = input match {
      |    case x if x > 100 => {
      |      val msg = "large"
      |      msg.toUpperCase
      |    }
      |    case x if x > 50 => "medium"
      |    case x if x > 0 => "small"
      |    case _ => "invalid"
      |  }
      |
      |  for {
      |    i <- 1 to 10
      |    if i % 2 == 0
      |  } {
      |    println(i)
      |  }
      |
      |  result
      |}""".stripMargin)

    // block(1) + if(1) + match(1) + case body block(1) + for body(1) = max 3
    val depth = NestingDepth.compute(term)
    depth should be >= 2
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
