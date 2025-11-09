package com.bitblends.scalametrics.metrics

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.meta._

/**
  * Unit tests for the InlineAndImplicits object.
  *
  * These tests verify that the inline and implicits analyzer correctly:
  *   - Detects implicit modifiers on defs, vals, and vars
  *   - Detects inline modifiers (Scala 3) and @inline annotations (Scala 2)
  *   - Identifies implicit conversions
  *   - Handles method declarations (abstract defs)
  *   - Distinguishes between implicit parameters and implicit definitions
  */
class InlineAndImplicitsSpec extends AnyFlatSpec with Matchers {

  behavior of "InlineAndImplicits"

  // ========== Implicit Defs ==========

  it should "detect implicit def" in {
    val defn = parseDefn("implicit def ordering: Ordering[String] = ???")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe false // not a conversion (no params)
    metrics.isInlineMethod shouldBe false
  }

  it should "detect implicit conversion (single param, non-Unit return)" in {
    val defn = parseDefn("implicit def intToString(x: Int): String = x.toString")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe true
    metrics.isInlineMethod shouldBe false
  }

  it should "not detect implicit conversion when param is implicit" in {
    val defn = parseDefn("implicit def foo(implicit x: Int): String = x.toString")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe false // param is implicit
  }

  it should "not detect implicit conversion when return type is Unit" in {
    val defn = parseDefn("implicit def foo(x: Int): Unit = println(x)")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe false // return type is Unit
  }

  it should "not detect implicit conversion when multiple params" in {
    val defn = parseDefn("implicit def foo(x: Int, y: String): String = ???")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe false // multiple params
  }

  it should "detect non-implicit def" in {
    val defn = parseDefn("def add(x: Int, y: Int): Int = x + y")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe false
    metrics.hasImplicitConversion shouldBe false
    metrics.isInlineMethod shouldBe false
  }

  // ========== Inline Defs ==========

  it should "detect Scala 3 inline modifier on def" in {
    implicit val dialect: Dialect = dialects.Scala3
    val code = "inline def squared(x: Int): Int = x * x"
    val defn = code.parse[Stat].get.asInstanceOf[Defn.Def]
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.isInlineMethod shouldBe true
    metrics.hasImplicitMod shouldBe false
  }

  it should "detect Scala 2 @inline annotation on def" in {
    val defn = parseDefn("@inline def squared(x: Int): Int = x * x")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.isInlineMethod shouldBe true
    metrics.hasImplicitMod shouldBe false
  }

  it should "handle def with both implicit and inline" in {
    implicit val dialect: Dialect = dialects.Scala3
    val code = "implicit inline def foo(x: Int): String = x.toString"
    val defn = code.parse[Stat].get.asInstanceOf[Defn.Def]
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.isInlineMethod shouldBe true
    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe true
  }

  // ========== Implicit Vals ==========

  it should "detect implicit val" in {
    val valDefn = parseVal("implicit val timeout: Int = 30")
    val metrics = InlineAndImplicits.forVal(valDefn.mods)

    metrics.hasImplicitMod shouldBe true
  }

  it should "detect non-implicit val" in {
    val valDefn = parseVal("val x: Int = 42")
    val metrics = InlineAndImplicits.forVal(valDefn.mods)

    metrics.hasImplicitMod shouldBe false
  }

  // ========== Implicit Vars ==========

  it should "detect implicit var" in {
    val varDefn = parseVar("implicit var counter: Int = 0")
    val metrics = InlineAndImplicits.forVar(varDefn)

    metrics.hasImplicitMod shouldBe true
  }

  it should "detect non-implicit var" in {
    val varDefn = parseVar("var x: Int = 42")
    val metrics = InlineAndImplicits.forVar(varDefn)

    metrics.hasImplicitMod shouldBe false
  }

  // ========== Abstract Method Declarations ==========

  it should "detect implicit in abstract method declaration" in {
    val declDefn = parseDeclDef("implicit def ordering: Ordering[String]")
    val metrics = InlineAndImplicits.forDecl(declDefn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe false
    metrics.isInlineMethod shouldBe false
  }

  it should "detect implicit conversion in abstract method declaration" in {
    val declDefn = parseDeclDef("implicit def intToString(x: Int): String")
    val metrics = InlineAndImplicits.forDecl(declDefn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe true
  }

  it should "detect inline in abstract method declaration" in {
    implicit val dialect: Dialect = dialects.Scala3
    val code = "inline def squared(x: Int): Int"
    val declDefn = code.parse[Stat].get.asInstanceOf[Decl.Def]
    val metrics = InlineAndImplicits.forDecl(declDefn)

    metrics.isInlineMethod shouldBe true
    metrics.hasImplicitMod shouldBe false
  }

  it should "detect non-implicit abstract method" in {
    val declDefn = parseDeclDef("def add(x: Int, y: Int): Int")
    val metrics = InlineAndImplicits.forDecl(declDefn)

    metrics.hasImplicitMod shouldBe false
    metrics.hasImplicitConversion shouldBe false
    metrics.isInlineMethod shouldBe false
  }

  // ========== Edge Cases ==========

  it should "handle implicit conversion with inferred return type" in {
    val defn = parseDefn("implicit def intToString(x: Int) = x.toString")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    // Inferred return type is assumed non-Unit, so this should be a conversion
    metrics.hasImplicitConversion shouldBe true
  }

  it should "handle method with no parameters" in {
    val defn = parseDefn("implicit def default: String = \"default\"")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe false // no params
  }

  it should "handle method with empty parameter list" in {
    val defn = parseDefn("implicit def default(): String = \"default\"")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe false // empty param list
  }

  it should "handle method with multiple parameter lists" in {
    val defn = parseDefn("implicit def foo(x: Int)(y: String): String = ???")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    // First param list has exactly one param, so this looks like a conversion
    metrics.hasImplicitConversion shouldBe true
  }

  it should "handle curried implicit conversion" in {
    val defn = parseDefn("implicit def convert(x: Int): String = x.toString")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe true
  }

  it should "handle implicit def with explicit Unit return type" in {
    val defn = parseDefn("implicit def sideEffect(x: Int): Unit = println(x)")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe false // Unit return type
  }

  it should "handle regular method with annotations" in {
    val defn = parseDefn("@deprecated def old(): Int = 42")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe false
    metrics.isInlineMethod shouldBe false
  }

  it should "detect @inline annotation among other annotations" in {
    val defn = parseDefn("@deprecated @inline def old(): Int = 42")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.isInlineMethod shouldBe true
  }

  it should "handle private implicit def" in {
    val defn = parseDefn("private implicit def convert(x: Int): String = x.toString")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe true
  }

  it should "handle protected implicit def" in {
    val defn = parseDefn("protected implicit def convert(x: Int): String = x.toString")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe true
  }

  // ========== Complex Scenarios ==========

  it should "handle implicit class (via apply method)" in {
    val defn = parseDefn("implicit def apply(x: Int): RichInt = new RichInt(x)")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe true
  }

  it should "handle generic implicit conversion" in {
    val defn = parseDefn("implicit def listToOption[A](list: List[A]): Option[A] = list.headOption")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe true
  }

  it should "handle implicit conversion with type bounds" in {
    val defn = parseDefn("implicit def convert[A <: Number](x: A): String = x.toString")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    metrics.hasImplicitConversion shouldBe true
  }

  it should "handle implicit conversion with context bounds" in {
    val defn = parseDefn("implicit def convert[A: Ordering](x: A): String = x.toString")
    val metrics = InlineAndImplicits.forDef(defn)

    metrics.hasImplicitMod shouldBe true
    // First param clause still has exactly one non-implicit param
    metrics.hasImplicitConversion shouldBe true
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
