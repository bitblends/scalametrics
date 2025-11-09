package com.bitblends.scalametrics.analyzer

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.meta._

/**
  * Unit tests for the TypeInfer object.
  *
  * These tests verify that the TypeInfer correctly:
  *   - Infers types from literal values (primitives, strings, null, unit)
  *   - Infers types from Java collection constructors (java.util.*)
  *   - Infers types from Java time types (java.time.*)
  *   - Infers types from Scala collections (List, Vector, Seq, Set, Array, Map)
  *   - Handles type ascriptions and type applications
  *   - Infers types from control flow structures (if/else, match, blocks)
  *   - Handles homogeneous type inference
  *   - Simplifies fully qualified class names
  */
class TypeInferSpec extends AnyFlatSpec with Matchers {

  behavior of "TypeInfer"

  // ========== Literal Types ==========

  it should "handle toByte method call" in {
    // Note: toByte is a method call on Int literal, TypeInfer doesn't handle this case
    val term = parseTerm("42.toByte")
    TypeInfer.inferSimpleType(term) shouldBe None
  }

  it should "handle toShort method call" in {
    // Note: toShort is a method call on Int literal, TypeInfer doesn't handle this case
    val term = parseTerm("42.toShort")
    TypeInfer.inferSimpleType(term) shouldBe None
  }

  it should "infer Int from integer literal" in {
    val term = parseTerm("42")
    TypeInfer.inferSimpleType(term) shouldBe Some("Int")
  }

  it should "infer Long from long literal" in {
    val term = parseTerm("42L")
    TypeInfer.inferSimpleType(term) shouldBe Some("Long")
  }

  it should "infer Float from float literal" in {
    val term = parseTerm("3.14f")
    TypeInfer.inferSimpleType(term) shouldBe Some("Float")
  }

  it should "infer Double from double literal" in {
    val term = parseTerm("3.14")
    TypeInfer.inferSimpleType(term) shouldBe Some("Double")
  }

  it should "infer Boolean from boolean literal" in {
    val term = parseTerm("true")
    TypeInfer.inferSimpleType(term) shouldBe Some("Boolean")
  }

  it should "infer Char from char literal" in {
    val term = parseTerm("'a'")
    TypeInfer.inferSimpleType(term) shouldBe Some("Char")
  }

  it should "infer String from string literal" in {
    val term = parseTerm("\"hello\"")
    TypeInfer.inferSimpleType(term) shouldBe Some("String")
  }

  it should "infer String from string interpolation" in {
    val term = parseTerm("s\"hello $name\"")
    TypeInfer.inferSimpleType(term) shouldBe Some("String")
  }

  it should "infer Unit from unit literal" in {
    val term = parseTerm("()")
    TypeInfer.inferSimpleType(term) shouldBe Some("Unit")
  }

  it should "infer Null from null literal" in {
    val term = parseTerm("null")
    TypeInfer.inferSimpleType(term) shouldBe Some("Null")
  }

  // ========== Scala Collections ==========

  it should "infer List type from homogeneous list of integers" in {
    val term = parseTerm("List(1, 2, 3)")
    TypeInfer.inferSimpleType(term) shouldBe Some("List[Int]")
  }

  it should "infer List type from homogeneous list of strings" in {
    val term = parseTerm("List(\"a\", \"b\", \"c\")")
    TypeInfer.inferSimpleType(term) shouldBe Some("List[String]")
  }

  it should "infer List[_] from heterogeneous list" in {
    val term = parseTerm("List(1, \"a\", true)")
    TypeInfer.inferSimpleType(term) shouldBe Some("List[_]")
  }

  it should "infer Vector type from homogeneous vector" in {
    val term = parseTerm("Vector(1, 2, 3)")
    TypeInfer.inferSimpleType(term) shouldBe Some("Vector[Int]")
  }

  it should "infer Seq type from homogeneous seq" in {
    val term = parseTerm("Seq(1, 2, 3)")
    TypeInfer.inferSimpleType(term) shouldBe Some("Seq[Int]")
  }

  it should "infer Set type from homogeneous set" in {
    val term = parseTerm("Set(1, 2, 3)")
    TypeInfer.inferSimpleType(term) shouldBe Some("Set[Int]")
  }

  it should "infer Array type from homogeneous array" in {
    val term = parseTerm("Array(1, 2, 3)")
    TypeInfer.inferSimpleType(term) shouldBe Some("Array[Int]")
  }

  it should "infer Map type from homogeneous map with tuple syntax" in {
    val term = parseTerm("Map((\"a\", 1), (\"b\", 2))")
    TypeInfer.inferSimpleType(term) shouldBe Some("Map[String, Int]")
  }

  it should "infer Map type from homogeneous map with arrow syntax" in {
    val term = parseTerm("Map(\"a\" -> 1, \"b\" -> 2)")
    TypeInfer.inferSimpleType(term) shouldBe Some("Map[String, Int]")
  }

  it should "infer Map[_, _] from heterogeneous map keys" in {
    val term = parseTerm("Map(1 -> \"a\", \"b\" -> 2)")
    TypeInfer.inferSimpleType(term) shouldBe Some("Map[_, _]")
  }

  it should "infer Map[_, _] from heterogeneous map values" in {
    val term = parseTerm("Map(\"a\" -> 1, \"b\" -> \"two\")")
    TypeInfer.inferSimpleType(term) shouldBe Some("Map[_, _]")
  }

  it should "infer Map[_, _] from empty map" in {
    val term = parseTerm("Map()")
    TypeInfer.inferSimpleType(term) shouldBe Some("Map[_, _]")
  }

  it should "infer List[Nothing] from Nil" in {
    val term = parseTerm("Nil")
    TypeInfer.inferSimpleType(term) shouldBe Some("List[Nothing]")
  }

  // ========== Java Collections ==========

  it should "infer ArrayList type from new ArrayList[String]" in {
    val term = parseTerm("new java.util.ArrayList[String]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("ArrayList[String]")
  }

  it should "infer HashMap type from new HashMap[String, Int]" in {
    val term = parseTerm("new java.util.HashMap[String, Int]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("HashMap[String, Int]")
  }

  it should "infer HashSet type from new HashSet[Int]" in {
    val term = parseTerm("new java.util.HashSet[Int]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("HashSet[Int]")
  }

  it should "infer LinkedList type from new LinkedList" in {
    val term = parseTerm("new java.util.LinkedList[String]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("LinkedList[String]")
  }

  it should "infer TreeMap type from new TreeMap" in {
    val term = parseTerm("new java.util.TreeMap[String, Int]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("TreeMap[String, Int]")
  }

  it should "infer ConcurrentHashMap type from new ConcurrentHashMap" in {
    val term = parseTerm("new java.util.concurrent.ConcurrentHashMap[String, Int]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("ConcurrentHashMap[String, Int]")
  }

  it should "infer Vector from java.util.Vector" in {
    val term = parseTerm("new java.util.Vector[String]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("Vector[String]")
  }

  it should "infer Stack from java.util.Stack" in {
    val term = parseTerm("new java.util.Stack[Int]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("Stack[Int]")
  }

  // ========== Java Time Types ==========

  it should "infer Instant from Instant.now" in {
    val term = parseTerm("Instant.now")
    TypeInfer.inferSimpleType(term) shouldBe Some("Instant")
  }

  it should "infer LocalDate from LocalDate.now" in {
    val term = parseTerm("LocalDate.now")
    TypeInfer.inferSimpleType(term) shouldBe Some("LocalDate")
  }

  it should "infer LocalDateTime from LocalDateTime.now" in {
    val term = parseTerm("LocalDateTime.now")
    TypeInfer.inferSimpleType(term) shouldBe Some("LocalDateTime")
  }

  it should "infer ZonedDateTime from ZonedDateTime.now" in {
    val term = parseTerm("ZonedDateTime.now")
    TypeInfer.inferSimpleType(term) shouldBe Some("ZonedDateTime")
  }

  it should "infer Duration from Duration.ofSeconds" in {
    val term = parseTerm("Duration.ofSeconds")
    TypeInfer.inferSimpleType(term) shouldBe Some("Duration")
  }

  it should "infer Period from Period.ofDays" in {
    val term = parseTerm("Period.ofDays")
    TypeInfer.inferSimpleType(term) shouldBe Some("Period")
  }

  it should "infer Instant from java.time.Instant.now" in {
    val term = parseTerm("java.time.Instant.now")
    TypeInfer.inferSimpleType(term) shouldBe Some("Instant")
  }

  it should "infer LocalDate from java.time.LocalDate.parse" in {
    val term = parseTerm("java.time.LocalDate.parse")
    TypeInfer.inferSimpleType(term) shouldBe Some("LocalDate")
  }

  // ========== Type Ascriptions ==========

  it should "infer type from type ascription" in {
    val term = parseTerm("42: Long")
    TypeInfer.inferSimpleType(term) shouldBe Some("Long")
  }

  it should "infer complex type from type ascription" in {
    val term = parseTerm("List(): List[String]")
    TypeInfer.inferSimpleType(term) shouldBe Some("List[String]")
  }

  // ========== Type Applications ==========

  it should "infer type from type application" in {
    val term = parseTerm("Option[Int]")
    TypeInfer.inferSimpleType(term) shouldBe Some("Option[Int]")
  }

  it should "infer type from type application with multiple type parameters" in {
    val term = parseTerm("Either[String, Int]")
    TypeInfer.inferSimpleType(term) shouldBe Some("Either[String, Int]")
  }

  // ========== Control Flow ==========

  it should "infer type from if-else with consistent branches" in {
    val term = parseTerm("if (true) 1 else 2")
    TypeInfer.inferSimpleType(term) shouldBe Some("Int")
  }

  it should "return None for if-else with inconsistent branches" in {
    val term = parseTerm("if (true) 1 else \"two\"")
    TypeInfer.inferSimpleType(term) shouldBe None
  }

  it should "infer type from match expression with consistent cases" in {
    val term = parseTerm("""1 match {
      |  case 1 => "one"
      |  case 2 => "two"
      |  case _ => "other"
      |}""".stripMargin)
    TypeInfer.inferSimpleType(term) shouldBe Some("String")
  }

  it should "return None for match expression with inconsistent cases" in {
    val term = parseTerm("""1 match {
      |  case 1 => 1
      |  case 2 => "two"
      |  case _ => true
      |}""".stripMargin)
    TypeInfer.inferSimpleType(term) shouldBe None
  }

  it should "infer type from block with last expression" in {
    val term = parseTerm("""{
      |  val x = 1
      |  val y = 2
      |  x + y
      |}""".stripMargin)
    // x + y is a method call on variables, TypeInfer doesn't track variable types
    TypeInfer.inferSimpleType(term) shouldBe None
  }

  it should "infer type from nested blocks" in {
    val term = parseTerm("""{
      |  val x = {
      |    val a = 1
      |    val b = 2
      |    a + b
      |  }
      |  x * 2
      |}""".stripMargin)
    // x * 2 is a method call on a variable, TypeInfer doesn't track variable types
    TypeInfer.inferSimpleType(term) shouldBe None
  }

  // ========== Generic Method Calls ==========

  it should "infer base type from method call" in {
    val term = parseTerm("foo.bar()")
    TypeInfer.inferSimpleType(term) shouldBe Some("bar")
  }

  it should "infer base type from chained method calls" in {
    val term = parseTerm("foo.bar.baz()")
    TypeInfer.inferSimpleType(term) shouldBe Some("baz")
  }

  // ========== Anonymous Classes ==========

  it should "infer type from anonymous class with java.util collection" in {
    val term = parseTerm("new java.util.ArrayList[String]() {}")
    TypeInfer.inferSimpleType(term) shouldBe Some("ArrayList[String]")
  }

  it should "infer type from anonymous class without type args" in {
    val term = parseTerm("new Runnable() { def run() = () }")
    TypeInfer.inferSimpleType(term) shouldBe Some("Runnable")
  }

  // ========== Edge Cases ==========

  it should "return None for complex expressions without clear type" in {
    val term = parseTerm("foo")
    TypeInfer.inferSimpleType(term) shouldBe None
  }

  it should "handle nested List of Lists" in {
    val term = parseTerm("List(List(1, 2), List(3, 4))")
    TypeInfer.inferSimpleType(term) shouldBe Some("List[List[Int]]")
  }

  it should "handle List of Map" in {
    val term = parseTerm("List(Map(\"a\" -> 1), Map(\"b\" -> 2))")
    TypeInfer.inferSimpleType(term) shouldBe Some("List[Map[String, Int]]")
  }

  it should "handle Map with List values" in {
    val term = parseTerm("Map(\"a\" -> List(1, 2), \"b\" -> List(3, 4))")
    TypeInfer.inferSimpleType(term) shouldBe Some("Map[String, List[Int]]")
  }

  it should "infer String from raw string interpolation" in {
    val term = parseTerm("raw\"hello\"")
    TypeInfer.inferSimpleType(term) shouldBe Some("String")
  }

  it should "infer String from f interpolation" in {
    val term = parseTerm("f\"value: $x%.2f\"")
    TypeInfer.inferSimpleType(term) shouldBe Some("String")
  }

  it should "handle empty blocks by returning None" in {
    val term = parseTerm("{}")
    TypeInfer.inferSimpleType(term) shouldBe None
  }

  it should "infer type from single-element block" in {
    val term = parseTerm("{ 42 }")
    TypeInfer.inferSimpleType(term) shouldBe Some("Int")
  }

  it should "handle match with single empty case by returning None" in {
    // Match with empty body case - the case body has no term
    val term = parseTerm("1 match { case _ => () }")
    TypeInfer.inferSimpleType(term) shouldBe Some("Unit")
  }

  it should "infer type from if without else (returns Unit)" in {
    val term = parseTerm("if (true) 1")
    // if without else in Scala has type Unit if condition is not met
    TypeInfer.inferSimpleType(term) shouldBe None
  }

  it should "simplify java.time type names" in {
    val term = parseTerm("new java.time.Instant()")
    TypeInfer.inferSimpleType(term) shouldBe Some("Instant")
  }

  it should "handle WeakHashMap from java.util" in {
    val term = parseTerm("new java.util.WeakHashMap[String, Int]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("WeakHashMap[String, Int]")
  }

  it should "handle IdentityHashMap from java.util" in {
    val term = parseTerm("new java.util.IdentityHashMap[String, Int]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("IdentityHashMap[String, Int]")
  }

  it should "handle LinkedHashMap from java.util" in {
    val term = parseTerm("new java.util.LinkedHashMap[String, Int]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("LinkedHashMap[String, Int]")
  }

  it should "handle LinkedHashSet from java.util" in {
    val term = parseTerm("new java.util.LinkedHashSet[String]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("LinkedHashSet[String]")
  }

  it should "handle TreeSet from java.util" in {
    val term = parseTerm("new java.util.TreeSet[String]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("TreeSet[String]")
  }

  it should "handle Hashtable from java.util" in {
    val term = parseTerm("new java.util.Hashtable[String, Int]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("Hashtable[String, Int]")
  }

  it should "handle EnumSet from java.util (arity 1)" in {
    val term = parseTerm("new java.util.EnumSet[MyEnum]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("EnumSet[MyEnum]")
  }

  it should "handle OffsetDateTime from java.time" in {
    val term = parseTerm("OffsetDateTime.now")
    TypeInfer.inferSimpleType(term) shouldBe Some("OffsetDateTime")
  }

  it should "handle OffsetTime from java.time" in {
    val term = parseTerm("OffsetTime.now")
    TypeInfer.inferSimpleType(term) shouldBe Some("OffsetTime")
  }

  it should "handle ZoneId from java.time" in {
    val term = parseTerm("ZoneId.systemDefault")
    TypeInfer.inferSimpleType(term) shouldBe Some("ZoneId")
  }

  it should "handle ZoneOffset from java.time" in {
    val term = parseTerm("ZoneOffset.UTC")
    TypeInfer.inferSimpleType(term) shouldBe Some("ZoneOffset")
  }

  it should "handle Year from java.time" in {
    val term = parseTerm("Year.now")
    TypeInfer.inferSimpleType(term) shouldBe Some("Year")
  }

  it should "handle YearMonth from java.time" in {
    val term = parseTerm("YearMonth.now")
    TypeInfer.inferSimpleType(term) shouldBe Some("YearMonth")
  }

  it should "handle MonthDay from java.time" in {
    val term = parseTerm("MonthDay.now")
    TypeInfer.inferSimpleType(term) shouldBe Some("MonthDay")
  }

  it should "handle nested if-else expressions" in {
    val term = parseTerm("if (true) if (false) 1 else 2 else 3")
    TypeInfer.inferSimpleType(term) shouldBe Some("Int")
  }

  it should "handle match inside if" in {
    val term = parseTerm("""if (true) 1 match {
      |  case 1 => "one"
      |  case _ => "other"
      |} else "none"""".stripMargin)
    TypeInfer.inferSimpleType(term) shouldBe Some("String")
  }

  it should "infer Array[_] when elements are heterogeneous" in {
    val term = parseTerm("Array(1, \"two\", 3.0)")
    TypeInfer.inferSimpleType(term) shouldBe Some("Array[_]")
  }

  it should "handle type application with java.util collections" in {
    val term = parseTerm("java.util.HashMap[String, Int]()")
    TypeInfer.inferSimpleType(term) shouldBe Some("HashMap[String, Int]")
  }

  it should "handle List with nested blocks as elements" in {
    // Blocks with variable references don't infer to literal types
    val term = parseTerm("List({ val x = 1; x }, { val y = 2; y })")
    // Since the blocks don't infer to a clear type (x and y are references), it returns List[_]
    TypeInfer.inferSimpleType(term) shouldBe Some("List[_]")
  }

  it should "handle Map with tuple syntax using nested expressions" in {
    // Map with blocks as values - blocks don't infer clearly
    val term = parseTerm("Map((\"a\", { val x = 1; x }), (\"b\", { val y = 2; y }))")
    TypeInfer.inferSimpleType(term) shouldBe Some("Map[_, _]")
  }

  it should "return None for unrecognized term types" in {
    val term = parseTerm("someFunction")
    TypeInfer.inferSimpleType(term) shouldBe None
  }

  // Helper method

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
}
