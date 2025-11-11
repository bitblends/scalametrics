package com.bitblends.scalametrics.analyzer

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Files
import scala.meta._

/**
  * Unit tests for the MemberAnalyzer object.
  *
  * These tests verify that the MemberAnalyzer correctly:
  *   - Analyzes various member types (classes, objects, traits, vals, vars, types, givens)
  *   - Computes metrics like cyclomatic complexity, nesting depth, and branch density
  *   - Handles access modifiers and documentation
  *   - Processes nested scopes and hierarchies
  *   - Extracts constructor field metrics
  */
class MemberAnalyzerSpec extends AnyFlatSpec with Matchers {

  behavior of "MemberAnalyzer"

  it should "return the correct analyzer name" in {
    MemberAnalyzer.name shouldBe "member"
  }

  it should "analyze a simple class definition" in {
    val ctx = createContext(
      """package com.example
        |
        |class MyClass {
        |  val x = 42
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    result.members should have size 2
    val classMetric = result.members.find(_.metadata.declarationType == "class")
    classMetric shouldBe defined
    classMetric.get.metadata.name shouldBe "com.example.MyClass"
    classMetric.get.metadata.accessModifier shouldBe "public"

    val valMetric = result.members.find(_.metadata.declarationType == "val")
    valMetric shouldBe defined
    valMetric.get.metadata.name shouldBe "com.example.MyClass.x"
  }

  it should "analyze a simple object definition" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  val constant = 100
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    result.members should have size 2
    val objectMetric = result.members.find(_.metadata.declarationType == "object")
    objectMetric shouldBe defined
    objectMetric.get.metadata.name shouldBe "com.example.MyObject"
  }

  it should "analyze a trait definition" in {
    val ctx = createContext(
      """package com.example
        |
        |trait MyTrait {
        |  def doSomething(): Unit
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    result.members should have size 1
    val traitMetric = result.members.head
    traitMetric.metadata.declarationType shouldBe "trait"
    traitMetric.metadata.name shouldBe "com.example.MyTrait"
  }

  it should "analyze val and var members" in {
    val ctx = createContext(
      """package com.example
        |
        |object Test {
        |  val immutable = 42
        |  var mutable = "hello"
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    val valMetric = result.members.find(_.metadata.declarationType == "val")
    valMetric shouldBe defined
    valMetric.get.metadata.name shouldBe "com.example.Test.immutable"

    val varMetric = result.members.find(_.metadata.declarationType == "var")
    varMetric shouldBe defined
    varMetric.get.metadata.name shouldBe "com.example.Test.mutable"
  }

  it should "analyze type definitions" in {
    val ctx = createContext(
      """package com.example
        |
        |object Types {
        |  type StringList = List[String]
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    val typeMetric = result.members.find(_.metadata.declarationType == "type")
    typeMetric shouldBe defined
    typeMetric.get.metadata.name shouldBe "com.example.Types.StringList"
    typeMetric.get.metadata.signature should include("type StringList")
  }

  it should "detect private access modifiers" in {
    val ctx = createContext(
      """package com.example
        |
        |class MyClass {
        |  private val secret = 42
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    val valMetric = result.members.find(_.metadata.declarationType == "val")
    valMetric shouldBe defined
    valMetric.get.metadata.accessModifier shouldBe "private"
  }

  it should "detect protected access modifiers" in {
    val ctx = createContext(
      """package com.example
        |
        |class MyClass {
        |  protected val internal = 42
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    val valMetric = result.members.find(_.metadata.declarationType == "val")
    valMetric shouldBe defined
    valMetric.get.metadata.accessModifier shouldBe "protected"
  }

  it should "detect Scaladoc comments" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  /** This is documented */
        |  val documented = 42
        |
        |  val notDocumented = 100
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    val documentedMetric =
      result.members.find(m => m.metadata.declarationType == "val" && m.metadata.name.contains("documented"))
    documentedMetric shouldBe defined
    documentedMetric.get.hasScaladoc shouldBe true

    val notDocumentedMetric =
      result.members.find(m => m.metadata.declarationType == "val" && m.metadata.name.contains("notDocumented"))
    notDocumentedMetric shouldBe defined
    notDocumentedMetric.get.hasScaladoc shouldBe false
  }

  it should "detect deprecated annotations" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  @deprecated
        |  val oldValue = 42
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    val valMetric = result.members.find(_.metadata.declarationType == "val")
    valMetric shouldBe defined
    valMetric.get.metadata.isDeprecated shouldBe true
  }

  it should "detect implicit modifiers" in {
    val ctx = createContext(
      """package com.example
        |
        |object Implicits {
        |  implicit val implicitValue: Int = 42
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    val valMetric = result.members.find(_.metadata.declarationType == "val")
    valMetric shouldBe defined
    valMetric.get.inlineAndImplicitMetrics.isImplicit shouldBe true
  }

  it should "compute cyclomatic complexity for val with complex RHS" in {
    val ctx = createContext(
      """package com.example
        |
        |object Test {
        |  val result = if (true) 1 else 2
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    val valMetric = result.members.find(_.metadata.declarationType == "val")
    valMetric shouldBe defined
    valMetric.get.cComplexity should be > 1
  }

  it should "compute branch density metrics" in {
    val ctx = createContext(
      """package com.example
        |
        |object Test {
        |  val result = {
        |    if (true) 1
        |    else if (false) 2
        |    else 3
        |  }
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    val valMetric = result.members.find(_.metadata.declarationType == "val")
    valMetric shouldBe defined
    valMetric.get.bdMetrics.branches should be > 0
    valMetric.get.bdMetrics.ifCount should be > 0
  }

  it should "analyze nested classes" in {
    val ctx = createContext(
      """package com.example
        |
        |class Outer {
        |  class Inner {
        |    val x = 42
        |  }
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    result.members.size should be >= 3
    val outerMetric =
      result.members.find(m => m.metadata.declarationType == "class" && m.metadata.name.endsWith("Outer"))
    outerMetric shouldBe defined

    val innerMetric =
      result.members.find(m => m.metadata.declarationType == "class" && m.metadata.name.contains("Inner"))
    innerMetric shouldBe defined
    innerMetric.get.metadata.name should include("Outer.Inner")
  }

  it should "extract constructor field metrics" in {
    val ctx = createContext(
      """package com.example
        |
        |class Person(val name: String, var age: Int, private val id: Long)
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    // Should have: class + 3 constructor fields
    result.members.size should be >= 3

    val nameField = result.members.find(m => m.metadata.declarationType == "val" && m.metadata.name.contains("name"))
    nameField shouldBe defined
    nameField.get.metadata.accessModifier shouldBe "public"

    val ageField = result.members.find(m => m.metadata.declarationType == "var" && m.metadata.name.contains("age"))
    ageField shouldBe defined

    val idField = result.members.find(m => m.metadata.name.contains("id"))
    idField shouldBe defined
    idField.get.metadata.accessModifier shouldBe "private"
  }

  it should "handle objects within classes" in {
    val ctx = createContext(
      """package com.example
        |
        |class MyClass {
        |  object CompanionLike {
        |    val x = 1
        |  }
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    val objectMetric = result.members.find(_.metadata.declarationType == "object")
    objectMetric shouldBe defined
    objectMetric.get.metadata.name should include("CompanionLike")
  }

  it should "compute pattern matching metrics" in {
    val ctx = createContext(
      """package com.example
        |
        |object Test {
        |  val result = 1 match {
        |    case 1 => "one"
        |    case 2 => "two"
        |    case _ => "other"
        |  }
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    val valMetric = result.members.find(_.metadata.declarationType == "val")
    valMetric shouldBe defined
    valMetric.get.pmMetrics.matches should be > 0
    valMetric.get.pmMetrics.cases should be > 0
    valMetric.get.pmMetrics.wildcards should be > 0
  }

  it should "handle multiple patterns in val declarations" in {
    val ctx = createContext(
      """package com.example
        |
        |object Test {
        |  val (a, b) = (1, 2)
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    val valMetric = result.members.find(_.metadata.declarationType == "val")
    valMetric shouldBe defined
    valMetric.get.metadata.signature should include("a")
  }

  it should "handle default package" in {
    val ctx = createContext(
      """class MyClass {
        |  val x = 42
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    val classMetric = result.members.find(_.metadata.declarationType == "class")
    classMetric shouldBe defined
    classMetric.get.metadata.name shouldBe "MyClass"

    val valMetric = result.members.find(_.metadata.declarationType == "val")
    valMetric shouldBe defined
    valMetric.get.metadata.name shouldBe "MyClass.x"
  }

  it should "handle Scala 3 given definitions" in {
    val ctx = createContext(
      """package com.example
        |
        |trait Show[A]:
        |  def show(a: A): String
        |
        |given Show[Int] with
        |  def show(n: Int) = n.toString
        |""".stripMargin,
      dialects.Scala3
    )

    val result = MemberAnalyzer.run(ctx)

    val givenMetric = result.members.find(_.metadata.declarationType == "given")
    givenMetric shouldBe defined
    givenMetric.get.inlineAndImplicitMetrics.isGivenInstance shouldBe Some(true)
  }

  it should "detect lines of code for members" in {
    val ctx = createContext(
      """package com.example
        |
        |class MyClass {
        |  val x = {
        |    val a = 1
        |    val b = 2
        |    a + b
        |  }
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    val valMetric = result.members.find(m => m.metadata.declarationType == "val" && m.metadata.name.contains("x"))
    valMetric shouldBe defined
    valMetric.get.metadata.linesOfCode should be > 1
  }

  it should "not analyze def methods (handled by MethodAnalyzer)" in {
    val ctx = createContext(
      """package com.example
        |
        |class MyClass {
        |  def myMethod(): Int = 42
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    // Should only have the class, not the method
    result.members.exists(_.metadata.declarationType == "def") shouldBe false
  }

  it should "handle complex nesting with multiple scopes" in {
    val ctx = createContext(
      """package com.example
        |
        |class Outer {
        |  object MiddleObject {
        |    class Inner {
        |      val deepValue = 42
        |    }
        |  }
        |}
        |""".stripMargin
    )

    val result = MemberAnalyzer.run(ctx)

    val deepValueMetric =
      result.members.find(m => m.metadata.declarationType == "val" && m.metadata.name.contains("deepValue"))
    deepValueMetric shouldBe defined
    deepValueMetric.get.metadata.name should include("Outer")
    deepValueMetric.get.metadata.name should include("MiddleObject")
    deepValueMetric.get.metadata.name should include("Inner")
  }

  it should "require fileId to be set in context" in {
    val testFile = createTempFile(
      """package com.example
        |
        |class Test
        |""".stripMargin
    )

    val source = parseFile(testFile)
    val ctx = AnalysisCtx(testFile, source, Some("test-project")) // No fileId set

    an[Exception] should be thrownBy {
      MemberAnalyzer.run(ctx)
    }

    testFile.delete()
  }

  // Helper methods

  /**
    * Creates a test context with the given code and runs FileAnalyzer first to set fileId.
    *
    * @param code
    *   the Scala code to analyze
    * @param dialect
    *   the Scala dialect to use
    * @return
    *   an AnalysisCtx ready for MemberAnalyzer
    */
  private def createContext(code: String, dialect: Dialect = dialects.Scala213): AnalysisCtx = {
    val testFile = createTempFile(code)
    val source = parseFile(testFile, dialect)
    val ctx = AnalysisCtx(testFile, source, Some("test-project"))

    // Run FileAnalyzer first to set fileId (required by MemberAnalyzer)
    val ctxWithFileId = FileAnalyzer.run(ctx)

    testFile.delete()
    ctxWithFileId
  }

  /**
    * Creates a temporary file with the given content.
    *
    * @param content
    *   the content to write to the file
    * @return
    *   the created temporary file
    */
  private def createTempFile(content: String): File = {
    val tempFile = Files.createTempFile("test-scala-", ".scala").toFile
    Files.write(tempFile.toPath, content.getBytes("UTF-8"))
    tempFile
  }

  /**
    * Parses a Scala file into a Source AST.
    *
    * @param file
    *   the file to parse
    * @param dialect
    *   the Scala dialect to use (defaults to Scala 2.13)
    * @return
    *   the parsed Source AST
    */
  private def parseFile(file: File, dialect: Dialect = dialects.Scala213): Source = {
    val content = new String(Files.readAllBytes(file.toPath), "UTF-8")
    implicit val d: Dialect = dialect
    content.parse[Source].get
  }
}
