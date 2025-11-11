package com.bitblends.scalametrics.metrics

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.meta._

/**
  * Unit tests for the PatternMatching object.
  *
  * These tests verify that the pattern matching analyzer correctly:
  *   - Counts match expressions
  *   - Counts match cases
  *   - Counts guards (if conditions in cases)
  *   - Counts wildcard patterns
  *   - Computes maximum match nesting depth
  *   - Counts nested matches
  *   - Analyzes complete source files
  */
class PatternMatchingSpec extends AnyFlatSpec with Matchers {

  behavior of "PatternMatching"

  // ========== Basic Pattern Matching ==========

  it should "compute zero matches for simple expression" in {
    val term = parseTerm("42")
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 0
    metrics.cases shouldBe 0
    metrics.guards shouldBe 0
    metrics.wildcards shouldBe 0
    metrics.maxNesting shouldBe 0
    metrics.nestedMatches shouldBe 0
  }

  it should "count single match expression" in {
    val term = parseTerm("""x match {
      |  case 1 => "one"
      |  case 2 => "two"
      |  case _ => "other"
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 1
    metrics.cases shouldBe 3
    metrics.wildcards shouldBe 1
    metrics.maxNesting shouldBe 1
  }

  it should "count match with two cases" in {
    val term = parseTerm("""x match {
      |  case 1 => "one"
      |  case _ => "other"
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 1
    metrics.cases shouldBe 2
    metrics.wildcards shouldBe 1
  }

  it should "count match with many cases" in {
    val term = parseTerm("""x match {
      |  case 1 => "one"
      |  case 2 => "two"
      |  case 3 => "three"
      |  case 4 => "four"
      |  case 5 => "five"
      |  case _ => "other"
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 1
    metrics.cases shouldBe 6
    metrics.wildcards shouldBe 1
  }

  // ========== Guards ==========

  it should "count guards in match cases" in {
    val term = parseTerm("""x match {
      |  case n if n > 10 => "big"
      |  case n if n < 0 => "negative"
      |  case _ => "other"
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 1
    metrics.cases shouldBe 3
    metrics.guards shouldBe 2
    metrics.wildcards shouldBe 1
  }

  it should "count single guard" in {
    val term = parseTerm("""x match {
      |  case n if n > 0 => "positive"
      |  case _ => "non-positive"
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.guards shouldBe 1
  }

  it should "count guards mixed with non-guarded cases" in {
    val term = parseTerm("""x match {
      |  case 1 => "one"
      |  case n if n > 10 => "big"
      |  case n if n < 0 => "negative"
      |  case _ => "other"
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 1
    metrics.cases shouldBe 4
    metrics.guards shouldBe 2
  }

  it should "handle match with no guards" in {
    val term = parseTerm("""x match {
      |  case 1 => "one"
      |  case 2 => "two"
      |  case _ => "other"
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.guards shouldBe 0
  }

  // ========== Wildcards ==========

  it should "count wildcard pattern" in {
    val term = parseTerm("""x match {
      |  case 1 => "one"
      |  case _ => "other"
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.wildcards shouldBe 1
  }

  it should "count zero wildcards when none present" in {
    val term = parseTerm("""x match {
      |  case 1 => "one"
      |  case 2 => "two"
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.wildcards shouldBe 0
  }

  it should "count multiple wildcard patterns" in {
    val term = parseTerm("""{
      |  val a = x match {
      |    case 1 => "one"
      |    case _ => "other"
      |  }
      |  val b = y match {
      |    case "a" => 1
      |    case _ => 0
      |  }
      |  (a, b)
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 2
    metrics.wildcards shouldBe 2
  }

  // ========== Nested Matches ==========

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
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 2
    metrics.cases shouldBe 6 // 3 outer + 3 inner
    metrics.maxNesting shouldBe 2
    metrics.nestedMatches shouldBe 1 // 1 inner match
    metrics.wildcards shouldBe 2
  }

  it should "count triple nested matches" in {
    val term = parseTerm("""x match {
      |  case 1 => y match {
      |    case "a" => z match {
      |      case true => 1
      |      case false => 2
      |    }
      |    case "b" => 3
      |  }
      |  case 2 => 4
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 3
    metrics.maxNesting shouldBe 3
    metrics.nestedMatches shouldBe 2 // 2 inner matches (at depths 2 and 3)
  }

  it should "count sibling matches (not nested)" in {
    val term = parseTerm("""{
      |  val a = x match {
      |    case 1 => "one"
      |    case _ => "other"
      |  }
      |  val b = y match {
      |    case "a" => 1
      |    case "b" => 2
      |    case _ => 3
      |  }
      |  (a, b)
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 2
    metrics.cases shouldBe 5 // 2 + 3
    metrics.maxNesting shouldBe 1 // not nested within each other
    metrics.nestedMatches shouldBe 0 // siblings, not nested
  }

  // ========== Complex Pattern Matching ==========

  it should "handle match with tuple patterns" in {
    val term = parseTerm("""pair match {
      |  case (1, 2) => "one-two"
      |  case (x, y) => s"$x-$y"
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 1
    metrics.cases shouldBe 2
  }

  it should "handle match with constructor patterns" in {
    val term = parseTerm("""opt match {
      |  case Some(x) => x
      |  case None => 0
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 1
    metrics.cases shouldBe 2
  }

  it should "handle match with list patterns" in {
    val term = parseTerm("""list match {
      |  case Nil => "empty"
      |  case head :: tail => s"head: $head"
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 1
    metrics.cases shouldBe 2
  }

  it should "handle match with typed patterns" in {
    val term = parseTerm("""obj match {
      |  case s: String => s.length
      |  case i: Int => i
      |  case _ => 0
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 1
    metrics.cases shouldBe 3
    metrics.wildcards shouldBe 1
  }

  it should "handle match with variable binding" in {
    val term = parseTerm("""opt match {
      |  case Some(x @ Point(_, _)) => x
      |  case None => Point(0, 0)
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 1
    metrics.cases shouldBe 2
  }

  it should "handle match with guard on complex condition" in {
    val term = parseTerm("""x match {
      |  case n if n > 0 && n < 10 => "single digit"
      |  case n if n >= 10 => "multiple digits"
      |  case _ => "non-positive"
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.guards shouldBe 2
    metrics.wildcards shouldBe 1
  }

  // ========== Source Analysis ==========

  it should "analyze source with single def" in {
    val code = """object Test {
      |  def classify(x: Int): String = x match {
      |    case 1 => "one"
      |    case 2 => "two"
      |    case _ => "other"
      |  }
      |}""".stripMargin
    val results = PatternMatching.analyzeSource(code)

    results should have size 1
    results.head.name shouldBe "classify"
    results.head.kind shouldBe "def"
    results.head.metrics.matches shouldBe 1
    results.head.metrics.cases shouldBe 3
    results.head.metrics.wildcards shouldBe 1
  }

  it should "analyze source with multiple defs" in {
    val code = """object Test {
      |  def foo(x: Int): String = x match {
      |    case 1 => "one"
      |    case _ => "other"
      |  }
      |
      |  def bar(x: Int): String = x match {
      |    case n if n > 0 => "positive"
      |    case n if n < 0 => "negative"
      |    case _ => "zero"
      |  }
      |}""".stripMargin

    val results = PatternMatching.analyzeSource(code)

    results should have size 2
    results.find(_.name == "foo").get.metrics.matches shouldBe 1
    results.find(_.name == "foo").get.metrics.guards shouldBe 0
    results.find(_.name == "bar").get.metrics.matches shouldBe 1
    results.find(_.name == "bar").get.metrics.guards shouldBe 2
  }

  it should "analyze source with val definitions" in {
    val code = """object Test {
      |  val simple = 42
      |  val withMatch = 1 match {
      |    case 1 => "one"
      |    case 2 => "two"
      |    case _ => "other"
      |  }
      |}""".stripMargin

    val results = PatternMatching.analyzeSource(code)

    results should have size 2
    results.find(_.name == "simple").get.metrics.matches shouldBe 0
    results.find(_.name == "withMatch").get.metrics.matches shouldBe 1
    results.find(_.name == "withMatch").get.metrics.cases shouldBe 3
  }

  it should "use forDef to compute metrics for method definition" in {
    val defn = parseDefn("""def test(x: Int): String = x match {
      |  case 1 => "one"
      |  case _ => "other"
      |}""".stripMargin)
    val metrics = PatternMatching.forDef(defn)

    metrics shouldBe defined
    metrics.get.matches shouldBe 1
    metrics.get.cases shouldBe 2
  }

  it should "use forVal to compute metrics for val definition" in {
    val valDefn = parseVal("""val result = x match {
      |  case 1 => "one"
      |  case _ => "other"
      |}""".stripMargin)
    val metrics = PatternMatching.forVal(valDefn)

    metrics shouldBe defined
    metrics.get.matches shouldBe 1
    metrics.get.cases shouldBe 2
  }

  it should "handle val with tuple pattern" in {
    val code = """object Test {
      |  val (a, b) = (1, 2)
      |}""".stripMargin
    val results = PatternMatching.analyzeSource(code)

    results should have size 1
    results.head.name shouldBe "<val>"
    results.head.metrics.matches shouldBe 0
  }

  it should "analyze realistic method with pattern matching" in {
    val code = """object Test {
      |  def process(input: Any): String = input match {
      |    case s: String if s.nonEmpty => s.toUpperCase
      |    case i: Int if i > 0 => i.toString
      |    case list: List[_] => list match {
      |      case Nil => "empty list"
      |      case head :: _ => s"list starting with $head"
      |    }
      |    case _ => "unknown"
      |  }
      |}""".stripMargin

    val results = PatternMatching.analyzeSource(code)

    results should have size 1
    val metrics = results.head.metrics
    metrics.matches shouldBe 2 // outer + inner
    metrics.cases shouldBe 6 // 4 outer + 2 inner
    metrics.guards shouldBe 2 // 2 guards in outer match
    metrics.maxNesting shouldBe 2
    metrics.nestedMatches shouldBe 1
    metrics.wildcards shouldBe 1
  }

  it should "handle method with no pattern matching" in {
    val defn = parseDefn("def add(x: Int, y: Int): Int = x + y")
    val metrics = PatternMatching.forDef(defn)

    metrics shouldBe defined
    metrics.get.matches shouldBe 0
    metrics.get.cases shouldBe 0
    metrics.get.guards shouldBe 0
    metrics.get.wildcards shouldBe 0
  }

  it should "handle pattern matching in for comprehension (collect)" in {
    val term = parseTerm("""list.collect {
      |  case Some(x) => x
      |  case None => 0
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    // collect uses a partial function, which is pattern matching
    metrics.matches shouldBe 0 // partial functions are not Term.Match
  }

  // ========== Edge Cases ==========

  it should "handle empty block" in {
    val term = parseTerm("{}")
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 0
  }

  it should "handle match with single case" in {
    val term = parseTerm("""x match {
      |  case _ => "always"
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 1
    metrics.cases shouldBe 1
    metrics.wildcards shouldBe 1
  }

  it should "handle match with no wildcard in exhaustive match" in {
    val term = parseTerm("""bool match {
      |  case true => 1
      |  case false => 0
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 1
    metrics.cases shouldBe 2
    metrics.wildcards shouldBe 0
  }

  it should "handle match nested in if-else" in {
    val term = parseTerm("""if (condition) {
      |  x match {
      |    case 1 => "one"
      |    case _ => "other"
      |  }
      |} else "default"
      |""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 1
    metrics.cases shouldBe 2
    metrics.maxNesting shouldBe 1
  }

  it should "handle match nested in loop" in {
    val term = parseTerm("""for (item <- items) {
      |  item match {
      |    case Some(x) => process(x)
      |    case None => skip()
      |  }
      |}""".stripMargin)
    val metrics = PatternMatching.compute(term)

    metrics.matches shouldBe 1
    metrics.cases shouldBe 2
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
