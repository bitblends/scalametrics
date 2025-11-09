package com.bitblends.scalametrics.metrics

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.meta._

/**
  * Unit tests for the Parameter object.
  *
  * These tests verify that the parameter analyzer correctly:
  *   - Counts total parameters and parameter lists
  *   - Detects implicit parameters and parameter lists
  *   - Detects using parameters and parameter lists (Scala 3)
  *   - Detects default parameters
  *   - Detects by-name parameters
  *   - Detects vararg (repeated) parameters
  *   - Detects inline parameters (Scala 3)
  *   - Handles multiple parameter lists
  *   - Analyzes constructors (primary and secondary)
  *   - Analyzes method declarations (abstract methods)
  */
class ParameterSpec extends AnyFlatSpec with Matchers {

  behavior of "Parameter"

  // ========== Basic Parameter Counting ==========

  it should "count zero parameters for method with no params" in {
    val defn = parseDefn("def foo: Int = 42")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 0
    arity.paramLists shouldBe 0
  }

  it should "count zero parameters for method with empty param list" in {
    val defn = parseDefn("def foo(): Int = 42")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 0
    arity.paramLists shouldBe 1
  }

  it should "count single parameter" in {
    val defn = parseDefn("def foo(x: Int): Int = x")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 1
    arity.paramLists shouldBe 1
  }

  it should "count multiple parameters in single list" in {
    val defn = parseDefn("def add(x: Int, y: Int): Int = x + y")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 2
    arity.paramLists shouldBe 1
  }

  it should "count parameters in multiple parameter lists" in {
    val defn = parseDefn("def foo(x: Int)(y: String): String = y * x")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 2
    arity.paramLists shouldBe 2
  }

  it should "count parameters in three parameter lists" in {
    val defn = parseDefn("def foo(a: Int)(b: String)(c: Boolean): String = ???")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 3
    arity.paramLists shouldBe 3
  }

  // ========== Implicit Parameters ==========

  it should "detect implicit parameter" in {
    val defn = parseDefn("def foo(implicit x: Int): Int = x")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 1
    arity.implicitParams shouldBe 1
    arity.implicitParamLists shouldBe 1
  }

  it should "detect multiple implicit parameters in same list" in {
    val defn = parseDefn("def foo(implicit x: Int, y: String): String = y * x")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 2
    arity.implicitParams shouldBe 2
    arity.implicitParamLists shouldBe 1
  }

  it should "detect implicit parameter list after regular list" in {
    val defn = parseDefn("def foo(x: Int)(implicit y: String): String = y * x")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 2
    arity.paramLists shouldBe 2
    arity.implicitParams shouldBe 1
    arity.implicitParamLists shouldBe 1
  }

  it should "not count regular parameters as implicit" in {
    val defn = parseDefn("def foo(x: Int, y: String): String = y * x")
    val arity = Parameter.forDef(defn)

    arity.implicitParams shouldBe 0
    arity.implicitParamLists shouldBe 0
  }

  // ========== Using Parameters (Scala 3) ==========

  it should "detect using parameter in Scala 3" in {
    implicit val dialect: Dialect = dialects.Scala3
    val code = "def foo(using x: Int): Int = x"
    val defn = code.parse[Stat].get.asInstanceOf[Defn.Def]
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 1
    arity.usingParams shouldBe 1
    arity.usingParamLists shouldBe 1
  }

  it should "detect multiple using parameters in Scala 3" in {
    implicit val dialect: Dialect = dialects.Scala3
    val code = "def foo(using x: Int, y: String): String = y * x"
    val defn = code.parse[Stat].get.asInstanceOf[Defn.Def]
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 2
    arity.usingParams shouldBe 2
    arity.usingParamLists shouldBe 1
  }

  it should "detect using parameter list after regular list in Scala 3" in {
    implicit val dialect: Dialect = dialects.Scala3
    val code = "def foo(x: Int)(using y: String): String = y * x"
    val defn = code.parse[Stat].get.asInstanceOf[Defn.Def]
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 2
    arity.paramLists shouldBe 2
    arity.usingParams shouldBe 1
    arity.usingParamLists shouldBe 1
  }

  // ========== Default Parameters ==========

  it should "detect default parameter" in {
    val defn = parseDefn("def foo(x: Int = 42): Int = x")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 1
    arity.defaultedParams shouldBe 1
  }

  it should "detect multiple default parameters" in {
    val defn = parseDefn("def foo(x: Int = 1, y: String = \"default\"): String = y * x")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 2
    arity.defaultedParams shouldBe 2
  }

  it should "detect mix of default and non-default parameters" in {
    val defn = parseDefn("def foo(x: Int, y: String = \"default\", z: Int = 0): String = y * x")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 3
    arity.defaultedParams shouldBe 2
  }

  it should "not count non-default parameters as defaulted" in {
    val defn = parseDefn("def foo(x: Int, y: String): String = y * x")
    val arity = Parameter.forDef(defn)

    arity.defaultedParams shouldBe 0
  }

  // ========== By-Name Parameters ==========

  it should "detect by-name parameter" in {
    val defn = parseDefn("def foo(x: => Int): Int = x")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 1
    arity.byNameParams shouldBe 1
  }

  it should "detect multiple by-name parameters" in {
    val defn = parseDefn("def foo(x: => Int, y: => String): String = y + x.toString")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 2
    arity.byNameParams shouldBe 2
  }

  it should "detect mix of by-name and regular parameters" in {
    val defn = parseDefn("def foo(x: Int, y: => String): String = y * x")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 2
    arity.byNameParams shouldBe 1
  }

  it should "not count regular parameters as by-name" in {
    val defn = parseDefn("def foo(x: Int, y: String): String = y * x")
    val arity = Parameter.forDef(defn)

    arity.byNameParams shouldBe 0
  }

  // ========== Vararg Parameters ==========

  it should "detect vararg parameter" in {
    val defn = parseDefn("def foo(xs: Int*): Int = xs.sum")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 1
    arity.varargParams shouldBe 1
  }

  it should "detect vararg after regular parameters" in {
    val defn = parseDefn("def foo(x: Int, y: String, zs: Int*): Int = zs.sum + x")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 3
    arity.varargParams shouldBe 1
  }

  it should "not count regular parameters as vararg" in {
    val defn = parseDefn("def foo(x: Int, y: String): String = y * x")
    val arity = Parameter.forDef(defn)

    arity.varargParams shouldBe 0
  }

  // ========== Inline Parameters (Scala 3) ==========

  it should "detect inline parameter in Scala 3" in {
    implicit val dialect: Dialect = dialects.Scala3
    val code = "def foo(inline x: Int): Int = x"
    val defn = code.parse[Stat].get.asInstanceOf[Defn.Def]
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 1
    arity.inlineParams shouldBe 1
  }

  it should "detect multiple inline parameters in Scala 3" in {
    implicit val dialect: Dialect = dialects.Scala3
    val code = "def foo(inline x: Int, inline y: String): String = y * x"
    val defn = code.parse[Stat].get.asInstanceOf[Defn.Def]
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 2
    arity.inlineParams shouldBe 2
  }

  // ========== Combined Parameter Types ==========

  it should "detect implicit parameter with default value" in {
    val defn = parseDefn("def foo(implicit x: Int = 42): Int = x")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 1
    arity.implicitParams shouldBe 1
    arity.defaultedParams shouldBe 1
  }

  it should "detect implicit by-name parameter" in {
    val defn = parseDefn("def foo(implicit x: => Int): Int = x")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 1
    arity.implicitParams shouldBe 1
    arity.byNameParams shouldBe 1
  }

  it should "handle complex parameter combinations" in {
    val defn =
      parseDefn("def foo(a: Int, b: => String = \"default\", c: Int*)(implicit d: Int, e: String): String = ???")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 5
    arity.paramLists shouldBe 2
    arity.defaultedParams shouldBe 1
    arity.byNameParams shouldBe 1
    arity.varargParams shouldBe 1
    arity.implicitParams shouldBe 2
    arity.implicitParamLists shouldBe 1
  }

  // ========== Method Declarations (Abstract Methods) ==========

  it should "analyze abstract method declaration" in {
    val declDefn = parseDeclDef("def foo(x: Int, y: String): String")
    val arity = Parameter.forDecl(declDefn)

    arity.totalParams shouldBe 2
    arity.paramLists shouldBe 1
  }

  it should "analyze abstract method with implicit parameters" in {
    val declDefn = parseDeclDef("def foo(x: Int)(implicit y: String): String")
    val arity = Parameter.forDecl(declDefn)

    arity.totalParams shouldBe 2
    arity.paramLists shouldBe 2
    arity.implicitParams shouldBe 1
    arity.implicitParamLists shouldBe 1
  }

  it should "analyze abstract method with default parameters" in {
    val declDefn = parseDeclDef("def foo(x: Int = 42): Int")
    val arity = Parameter.forDecl(declDefn)

    arity.totalParams shouldBe 1
    arity.defaultedParams shouldBe 1
  }

  it should "analyze abstract method with by-name parameter" in {
    val declDefn = parseDeclDef("def foo(x: => Int): Int")
    val arity = Parameter.forDecl(declDefn)

    arity.totalParams shouldBe 1
    arity.byNameParams shouldBe 1
  }

  it should "analyze abstract method with vararg parameter" in {
    val declDefn = parseDeclDef("def foo(xs: Int*): Int")
    val arity = Parameter.forDecl(declDefn)

    arity.totalParams shouldBe 1
    arity.varargParams shouldBe 1
  }

  // ========== Source Analysis ==========

  it should "analyze source with single def" in {
    val code = """object Test {
      |  def simple(x: Int): Int = x + 1
      |}""".stripMargin
    val results = Parameter.analyzeSource(code)

    results should have size 1
    results.head.name shouldBe "simple"
    results.head.kind shouldBe "def"
    results.head.arity.totalParams shouldBe 1
  }

  it should "analyze source with multiple defs" in {
    val code = """object Test {
      |  def add(x: Int, y: Int): Int = x + y
      |
      |  def greet(name: String = "World"): String = s"Hello, $name"
      |
      |  def compute(x: Int)(implicit factor: Int): Int = x * factor
      |}""".stripMargin

    val results = Parameter.analyzeSource(code)

    results should have size 3
    results.find(_.name == "add").get.arity.totalParams shouldBe 2
    results.find(_.name == "greet").get.arity.defaultedParams shouldBe 1
    results.find(_.name == "compute").get.arity.implicitParams shouldBe 1
  }

  it should "analyze class with primary constructor" in {
    val code = """class Person(name: String, age: Int)"""
    val results = Parameter.analyzeSource(code)

    results should have size 1
    results.head.kind shouldBe "ctor-primary"
    results.head.arity.totalParams shouldBe 2
  }

  it should "analyze class with primary constructor with default params" in {
    val code = """class Person(name: String, age: Int = 0)"""
    val results = Parameter.analyzeSource(code)

    results should have size 1
    results.head.arity.totalParams shouldBe 2
    results.head.arity.defaultedParams shouldBe 1
  }

  it should "analyze class with secondary constructor" in {
    val code = """class Person(name: String) {
      |  def this(name: String, age: Int) = this(name)
      |}""".stripMargin
    val results = Parameter.analyzeSource(code)

    results should have size 2
    results.count(_.kind == "ctor-primary") shouldBe 1
    results.count(_.kind == "ctor-secondary") shouldBe 1
  }

  it should "analyze case class constructor" in {
    val code = """case class Point(x: Int, y: Int)"""
    val results = Parameter.analyzeSource(code)

    // Case class has primary constructor
    val ctorResults = results.filter(_.kind == "ctor-primary")
    ctorResults should have size 1
    ctorResults.head.arity.totalParams shouldBe 2
  }

  // ========== Edge Cases ==========

  it should "handle method with type parameters" in {
    val defn = parseDefn("def foo[A, B](x: A, y: B): (A, B) = (x, y)")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 2
    arity.paramLists shouldBe 1
  }

  it should "handle method with context bounds" in {
    val defn =
      parseDefn("def foo[A: Ordering](x: A, y: A): A = if (implicitly[Ordering[A]].compare(x, y) > 0) x else y")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 2
  }

  it should "handle method with multiple parameter lists of different types" in {
    val defn = parseDefn("def foo(x: Int)(y: => String)(implicit z: Boolean): String = if (z) y * x else \"\"")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 3
    arity.paramLists shouldBe 3
    arity.byNameParams shouldBe 1
    arity.implicitParams shouldBe 1
    arity.implicitParamLists shouldBe 1
  }

  it should "handle constructor with implicit parameters" in {
    val code = """class Foo(x: Int)(implicit y: String)"""
    val results = Parameter.analyzeSource(code)

    results should have size 1
    results.head.arity.totalParams shouldBe 2
    results.head.arity.implicitParams shouldBe 1
    results.head.arity.paramLists shouldBe 2
  }

  it should "handle method with vararg and implicit parameters" in {
    val defn = parseDefn("def foo(xs: Int*)(implicit ordering: Ordering[Int]): List[Int] = xs.sorted.toList")
    val arity = Parameter.forDef(defn)

    arity.totalParams shouldBe 2
    arity.varargParams shouldBe 1
    arity.implicitParams shouldBe 1
  }

  // Helper methods

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

  /**
    * Parses a Scala def declaration (abstract method).
    *
    * @param code
    *   the Scala code to parse
    * @return
    *   the parsed Decl.Def AST
    */
  private def parseDeclDef(code: String): Decl.Def = {
    implicit val dialect: Dialect = dialects.Scala213
    code.parse[Stat].get.asInstanceOf[Decl.Def]
  }
}
