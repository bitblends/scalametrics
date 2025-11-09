package com.bitblends.scalametrics.metrics

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.meta._

/**
  * Unit tests for the ReturnTypeExplicitness object.
  *
  * These tests verify that the return type explicitness analyzer correctly:
  *   - Detects explicit return types on defs, vals, and vars
  *   - Detects inferred return types
  *   - Identifies public vs private/protected members
  *   - Handles method declarations (abstract methods)
  *   - Summarizes return type statistics
  *   - Attempts to infer simple types when not explicitly declared
  */
class ReturnTypeExplicitnessSpec extends AnyFlatSpec with Matchers {

  behavior of "ReturnTypeExplicitness"

  // ========== Def with Explicit Return Type ==========

  it should "detect explicit return type on def" in {
    val defn = parseDefn("def foo(x: Int): Int = x + 1")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.name shouldBe "foo"
    item.kind shouldBe "def"
    item.isPublic shouldBe true
    item.hasExplicitReturnType shouldBe true
    item.inferredReturnType shouldBe None
  }

  it should "detect explicit String return type on def" in {
    val defn = parseDefn("def greet(name: String): String = s\"Hello, $name\"")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.hasExplicitReturnType shouldBe true
    item.inferredReturnType shouldBe None
  }

  it should "detect explicit Unit return type on def" in {
    val defn = parseDefn("def print(x: Int): Unit = println(x)")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.hasExplicitReturnType shouldBe true
  }

  it should "detect explicit complex return type on def" in {
    val defn = parseDefn("def getList: List[String] = List(\"a\", \"b\")")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.hasExplicitReturnType shouldBe true
  }

  // ========== Def with Inferred Return Type ==========

  it should "detect inferred return type on def" in {
    val defn = parseDefn("def foo(x: Int) = x + 1")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.hasExplicitReturnType shouldBe false
    // TypeInfer should be able to infer Int from the literal
    item.inferredReturnType shouldBe defined
  }

  it should "detect inferred String return type on def" in {
    val defn = parseDefn("def greet = \"Hello\"")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.hasExplicitReturnType shouldBe false
    item.inferredReturnType shouldBe Some("String")
  }

  it should "attempt to infer return type from literal" in {
    val defn = parseDefn("def getNumber = 42")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.hasExplicitReturnType shouldBe false
    item.inferredReturnType shouldBe Some("Int")
  }

  it should "attempt to infer List type" in {
    val defn = parseDefn("def getList = List(1, 2, 3)")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.hasExplicitReturnType shouldBe false
    item.inferredReturnType shouldBe Some("List[Int]")
  }

  it should "handle def with complex body and no explicit type" in {
    val defn = parseDefn("def compute(x: Int) = if (x > 0) x else -x")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.hasExplicitReturnType shouldBe false
    item.inferredReturnType shouldBe Some("Int")
  }

  // ========== Val with Explicit Type ==========

  it should "detect explicit type on val" in {
    val valDefn = parseVal("val x: Int = 42")
    val item = ReturnTypeExplicitness.forVal(valDefn)

    item.name shouldBe "x"
    item.kind shouldBe "val"
    item.isPublic shouldBe true
    item.hasExplicitReturnType shouldBe true
    item.inferredReturnType shouldBe None
  }

  it should "detect explicit String type on val" in {
    val valDefn = parseVal("val name: String = \"Alice\"")
    val item = ReturnTypeExplicitness.forVal(valDefn)

    item.hasExplicitReturnType shouldBe true
  }

  it should "detect explicit complex type on val" in {
    val valDefn = parseVal("val list: List[Int] = List(1, 2, 3)")
    val item = ReturnTypeExplicitness.forVal(valDefn)

    item.hasExplicitReturnType shouldBe true
  }

  // ========== Val with Inferred Type ==========

  it should "detect inferred type on val" in {
    val valDefn = parseVal("val x = 42")
    val item = ReturnTypeExplicitness.forVal(valDefn)

    item.hasExplicitReturnType shouldBe false
    item.inferredReturnType shouldBe Some("Int")
  }

  it should "detect inferred String type on val" in {
    val valDefn = parseVal("val name = \"Alice\"")
    val item = ReturnTypeExplicitness.forVal(valDefn)

    item.hasExplicitReturnType shouldBe false
    item.inferredReturnType shouldBe Some("String")
  }

  it should "attempt to infer List type on val" in {
    val valDefn = parseVal("val list = List(1, 2, 3)")
    val item = ReturnTypeExplicitness.forVal(valDefn)

    item.hasExplicitReturnType shouldBe false
    item.inferredReturnType shouldBe Some("List[Int]")
  }

  it should "handle val with tuple pattern and explicit type" in {
    val valDefn = parseVal("val (a, b): (Int, String) = (1, \"hello\")")
    val item = ReturnTypeExplicitness.forVal(valDefn)

    item.hasExplicitReturnType shouldBe true
  }

  it should "handle val with tuple pattern and inferred type" in {
    val valDefn = parseVal("val (a, b) = (1, \"hello\")")
    val item = ReturnTypeExplicitness.forVal(valDefn)

    item.hasExplicitReturnType shouldBe false
  }

  // ========== Var with Explicit Type ==========

  it should "detect explicit type on var" in {
    val varDefn = parseVar("var x: Int = 42")
    val item = ReturnTypeExplicitness.forVar(varDefn)

    item.name shouldBe "x"
    item.kind shouldBe "var"
    item.isPublic shouldBe true
    item.hasExplicitReturnType shouldBe true
    item.inferredReturnType shouldBe None
  }

  it should "detect explicit String type on var" in {
    val varDefn = parseVar("var name: String = \"Alice\"")
    val item = ReturnTypeExplicitness.forVar(varDefn)

    item.hasExplicitReturnType shouldBe true
  }

  // ========== Var with Inferred Type ==========

  it should "detect inferred type on var" in {
    val varDefn = parseVar("var x = 42")
    val item = ReturnTypeExplicitness.forVar(varDefn)

    item.hasExplicitReturnType shouldBe false
    item.inferredReturnType shouldBe Some("Int")
  }

  it should "detect inferred String type on var" in {
    val varDefn = parseVar("var name = \"Alice\"")
    val item = ReturnTypeExplicitness.forVar(varDefn)

    item.hasExplicitReturnType shouldBe false
    item.inferredReturnType shouldBe Some("String")
  }

  // ========== Access Modifiers ==========

  it should "detect public def" in {
    val defn = parseDefn("def foo: Int = 42")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.isPublic shouldBe true
  }

  it should "detect private def" in {
    val defn = parseDefn("private def foo: Int = 42")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.isPublic shouldBe false
  }

  it should "detect protected def" in {
    val defn = parseDefn("protected def foo: Int = 42")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.isPublic shouldBe false
  }

  it should "detect private val" in {
    val valDefn = parseVal("private val x: Int = 42")
    val item = ReturnTypeExplicitness.forVal(valDefn)

    item.isPublic shouldBe false
  }

  it should "detect protected var" in {
    val varDefn = parseVar("protected var x: Int = 42")
    val item = ReturnTypeExplicitness.forVar(varDefn)

    item.isPublic shouldBe false
  }

  it should "detect private[this] def" in {
    val defn = parseDefn("private[this] def foo: Int = 42")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.isPublic shouldBe false
  }

  // ========== Abstract Method Declarations ==========

  it should "handle abstract method declaration (always explicit)" in {
    val declDefn = parseDeclDef("def foo(x: Int): String")
    val item = ReturnTypeExplicitness.forDecl(declDefn)

    item.name shouldBe "foo"
    item.kind shouldBe "decl-def"
    item.isPublic shouldBe true
    item.hasExplicitReturnType shouldBe true
    item.inferredReturnType shouldBe None
  }

  it should "handle private abstract method declaration" in {
    val declDefn = parseDeclDef("private def foo: Int")
    val item = ReturnTypeExplicitness.forDecl(declDefn)

    item.isPublic shouldBe false
    item.hasExplicitReturnType shouldBe true
  }

  it should "handle protected abstract method declaration" in {
    val declDefn = parseDeclDef("protected def bar: String")
    val item = ReturnTypeExplicitness.forDecl(declDefn)

    item.isPublic shouldBe false
    item.hasExplicitReturnType shouldBe true
  }

  // ========== Source Analysis ==========

  it should "analyze source with single def" in {
    val code = """object Test {
      |  def foo(x: Int): Int = x + 1
      |}""".stripMargin
    val results = ReturnTypeExplicitness.analyzeSource(code)

    results should have size 1
    results.head.name shouldBe "foo"
    results.head.kind shouldBe "def"
    results.head.hasExplicitReturnType shouldBe true
  }

  it should "analyze source with mixed explicit and inferred types" in {
    val code = """object Test {
      |  def explicit(x: Int): Int = x + 1
      |  def inferred(x: Int) = x + 1
      |  val explicitVal: String = "hello"
      |  val inferredVal = "hello"
      |}""".stripMargin

    val results = ReturnTypeExplicitness.analyzeSource(code)

    results should have size 4
    results.count(_.hasExplicitReturnType) shouldBe 2
    results.count(!_.hasExplicitReturnType) shouldBe 2
  }

  it should "analyze source with public and private members" in {
    val code = """class Foo {
      |  def public: Int = 42
      |  private def privateMethod: Int = 0
      |  protected val protectedVal: String = "secret"
      |}""".stripMargin

    val results = ReturnTypeExplicitness.analyzeSource(code)

    results should have size 3
    results.count(_.isPublic) shouldBe 1
    results.count(!_.isPublic) shouldBe 2
  }

  it should "analyze source with abstract methods" in {
    val code = """trait Foo {
      |  def abstractMethod(x: Int): String
      |  def concreteMethod(x: Int): String = x.toString
      |}""".stripMargin

    val results = ReturnTypeExplicitness.analyzeSource(code)

    results should have size 2
    results.find(_.name == "abstractMethod").get.kind shouldBe "decl-def"
    results.find(_.name == "concreteMethod").get.kind shouldBe "def"
    // Both should have explicit return types
    results.forall(_.hasExplicitReturnType) shouldBe true
  }

  it should "analyze source with vars" in {
    val code = """class Counter {
      |  var count: Int = 0
      |  var name = "counter"
      |}""".stripMargin

    val results = ReturnTypeExplicitness.analyzeSource(code)

    results should have size 2
    results.forall(_.kind == "var") shouldBe true
    results.count(_.hasExplicitReturnType) shouldBe 1
    results.count(!_.hasExplicitReturnType) shouldBe 1
  }

  // ========== Summary Statistics ==========

  it should "summarize empty list" in {
    val summary = ReturnTypeExplicitness.summarize(List.empty)

    summary.totalDefs shouldBe 0
    summary.totalPublicDefs shouldBe 0
    summary.explicitDefs shouldBe 0
    summary.explicitPublicDefs shouldBe 0
  }

  it should "summarize single explicit def" in {
    val defn = parseDefn("def foo: Int = 42")
    val item = ReturnTypeExplicitness.forDef(defn)
    val summary = ReturnTypeExplicitness.summarize(List(item))

    summary.totalDefs shouldBe 1
    summary.totalPublicDefs shouldBe 1
    summary.explicitDefs shouldBe 1
    summary.explicitPublicDefs shouldBe 1
  }

  it should "summarize mixed explicit and inferred" in {
    val code = """object Test {
      |  def explicit: Int = 42
      |  def inferred = 42
      |  val explicitVal: String = "hello"
      |  val inferredVal = "hello"
      |}""".stripMargin
    val results = ReturnTypeExplicitness.analyzeSource(code)
    val summary = ReturnTypeExplicitness.summarize(results)

    summary.totalDefs shouldBe 4
    summary.explicitDefs shouldBe 2
  }

  it should "summarize public vs private" in {
    val code = """class Foo {
      |  def public: Int = 42
      |  private def privateExplicit: Int = 0
      |  private def privateInferred = 1
      |  def publicInferred = 2
      |}""".stripMargin
    val results = ReturnTypeExplicitness.analyzeSource(code)
    val summary = ReturnTypeExplicitness.summarize(results)

    summary.totalDefs shouldBe 4
    summary.totalPublicDefs shouldBe 2
    summary.explicitDefs shouldBe 2
    summary.explicitPublicDefs shouldBe 1
  }

  it should "summarize realistic codebase" in {
    val code = """class Example {
      |  // Public with explicit types (best practice)
      |  def publicExplicit1(x: Int): Int = x + 1
      |  def publicExplicit2: String = "hello"
      |  val publicValExplicit: List[Int] = List(1, 2, 3)
      |
      |  // Public with inferred types (should be avoided for APIs)
      |  def publicInferred1(x: Int) = x + 1
      |  def publicInferred2 = "hello"
      |
      |  // Private with explicit types (optional but clear)
      |  private def privateExplicit: Int = 42
      |
      |  // Private with inferred types (acceptable)
      |  private def privateInferred = 42
      |  private val privateVal = "secret"
      |}""".stripMargin
    val results = ReturnTypeExplicitness.analyzeSource(code)
    val summary = ReturnTypeExplicitness.summarize(results)

    summary.totalDefs shouldBe 8
    summary.totalPublicDefs shouldBe 5
    summary.explicitDefs shouldBe 4
    summary.explicitPublicDefs shouldBe 3
  }

  // ========== Edge Cases ==========

  it should "handle def with no parameters and explicit type" in {
    val defn = parseDefn("def getValue: Int = 42")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.hasExplicitReturnType shouldBe true
  }

  it should "handle def with empty parameter list and inferred type" in {
    val defn = parseDefn("def getValue() = 42")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.hasExplicitReturnType shouldBe false
    item.inferredReturnType shouldBe Some("Int")
  }

  it should "handle val with pattern matching and explicit type" in {
    val valDefn = parseVal("val result: String = x match { case 1 => \"one\"; case _ => \"other\" }")
    val item = ReturnTypeExplicitness.forVal(valDefn)

    item.hasExplicitReturnType shouldBe true
  }

  it should "handle val with pattern matching and inferred type" in {
    val valDefn = parseVal("val result = x match { case 1 => \"one\"; case _ => \"other\" }")
    val item = ReturnTypeExplicitness.forVal(valDefn)

    item.hasExplicitReturnType shouldBe false
    item.inferredReturnType shouldBe Some("String")
  }

  it should "handle def with generic type parameter" in {
    val defn = parseDefn("def identity[A](x: A): A = x")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.hasExplicitReturnType shouldBe true
  }

  it should "handle def with complex generic return type" in {
    val defn = parseDefn("def getMap[K, V](k: K, v: V): Map[K, V] = Map(k -> v)")
    val item = ReturnTypeExplicitness.forDef(defn)

    item.hasExplicitReturnType shouldBe true
  }

  it should "handle var with Option type" in {
    val varDefn = parseVar("var maybeValue: Option[Int] = None")
    val item = ReturnTypeExplicitness.forVar(varDefn)

    item.hasExplicitReturnType shouldBe true
  }

  it should "handle val with function type" in {
    val valDefn = parseVal("val f: Int => String = _.toString")
    val item = ReturnTypeExplicitness.forVal(valDefn)

    item.hasExplicitReturnType shouldBe true
  }

  it should "handle val with inferred function type" in {
    val valDefn = parseVal("val f = (x: Int) => x.toString")
    val item = ReturnTypeExplicitness.forVal(valDefn)

    item.hasExplicitReturnType shouldBe false
  }

  it should "handle multiple vals in same definition with explicit type" in {
    val valDefn = parseVal("val (a, b): (Int, Int) = (1, 2)")
    val item = ReturnTypeExplicitness.forVal(valDefn)

    item.name shouldBe "a,b"
    item.hasExplicitReturnType shouldBe true
  }

  it should "handle multiple vals in same definition with inferred type" in {
    val valDefn = parseVal("val (a, b) = (1, 2)")
    val item = ReturnTypeExplicitness.forVal(valDefn)

    item.name shouldBe "a,b"
    item.hasExplicitReturnType shouldBe false
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
    * Parses a Scala var definition.
    *
    * @param code
    *   the Scala code to parse
    * @return
    *   the parsed Defn.Var AST
    */
  private def parseVar(code: String): Defn.Var = {
    implicit val dialect: Dialect = dialects.Scala213
    code.parse[Stat].get.asInstanceOf[Defn.Var]
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
