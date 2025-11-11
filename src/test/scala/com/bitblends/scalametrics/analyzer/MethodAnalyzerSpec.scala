package com.bitblends.scalametrics.analyzer

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Files
import scala.meta._

/**
  * Unit tests for the MethodAnalyzer object.
  *
  * These tests verify that the MethodAnalyzer correctly:
  *   - Analyzes concrete method definitions (Defn.Def)
  *   - Analyzes abstract method declarations (Decl.Def)
  *   - Computes cyclomatic complexity and nesting depth
  *   - Extracts parameter metrics (implicit, using, default, vararg, by-name)
  *   - Detects access modifiers and documentation
  *   - Computes pattern matching metrics
  *   - Computes branch density metrics
  *   - Handles inline and implicit conversion modifiers
  *   - Infers return types when not explicitly declared
  *   - Processes nested methods and scopes
  */
class MethodAnalyzerSpec extends AnyFlatSpec with Matchers {

  behavior of "MethodAnalyzer"

  it should "return the correct analyzer name" in {
    MethodAnalyzer.name shouldBe "method"
  }

  it should "analyze a simple method definition" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def simple(): Int = 42
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.metadata.name shouldBe "com.example.MyObject.simple"
    method.metadata.signature shouldBe "simple(): Int"
    method.inlineAndImplicitMetrics.isAbstract shouldBe false
    method.metadata.accessModifier shouldBe "public"
    method.parameterMetrics.totalParams shouldBe 0
    method.parameterMetrics.paramLists shouldBe 1
  }

  it should "analyze a method with parameters" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def add(x: Int, y: Int): Int = x + y
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.metadata.name shouldBe "com.example.MyObject.add"
    method.metadata.signature should include("x: Int")
    method.metadata.signature should include("y: Int")
    method.parameterMetrics.totalParams shouldBe 2
    method.parameterMetrics.paramLists shouldBe 1
  }

  it should "analyze a method with multiple parameter lists" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def curried(x: Int)(y: Int): Int = x + y
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.parameterMetrics.totalParams shouldBe 2
    method.parameterMetrics.paramLists shouldBe 2
  }

  it should "detect implicit parameters" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def withImplicit(x: Int)(implicit ctx: String): Int = x
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.parameterMetrics.implicitParams shouldBe 1
    method.parameterMetrics.implicitParamLists shouldBe 1
  }

  it should "detect default parameters" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def withDefaults(x: Int = 10, y: String = "hello"): Int = x
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.parameterMetrics.defaultedParams shouldBe 2
  }

  it should "detect vararg parameters" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def varargs(items: Int*): Int = items.sum
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.parameterMetrics.varargParams shouldBe 1
  }

  it should "detect by-name parameters" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def byName(x: => Int): Int = x
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.parameterMetrics.byNameParams shouldBe 1
  }

  it should "detect private access modifier" in {
    val ctx = createContext(
      """package com.example
        |
        |class MyClass {
        |  private def secret(): Int = 42
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.metadata.accessModifier shouldBe "private"
  }

  it should "detect protected access modifier" in {
    val ctx = createContext(
      """package com.example
        |
        |class MyClass {
        |  protected def internal(): Int = 42
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.metadata.accessModifier shouldBe "protected"
  }

  it should "detect Scaladoc comments" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  /** This method is documented */
        |  def documented(): Int = 42
        |
        |  def notDocumented(): Int = 100
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 2
    val documented = result.methods.find(_.metadata.name.contains("documented"))
    documented shouldBe defined
    documented.get.hasScaladoc shouldBe true

    val notDocumented = result.methods.find(_.metadata.name.contains("notDocumented"))
    notDocumented shouldBe defined
    notDocumented.get.hasScaladoc shouldBe false
  }

  it should "detect deprecated annotation" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  @deprecated
        |  def oldMethod(): Int = 42
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.metadata.isDeprecated shouldBe true
  }

  it should "compute cyclomatic complexity for method with conditionals" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def complex(x: Int): String = {
        |    if (x > 0) "positive"
        |    else if (x < 0) "negative"
        |    else "zero"
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.cComplexity should be > 1
  }

  it should "compute nesting depth for nested structures" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def nested(x: Int): Int = {
        |    if (x > 0) {
        |      if (x > 10) {
        |        if (x > 100) {
        |          1000
        |        } else 100
        |      } else 10
        |    } else 0
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.nestingDepth should be > 2
  }

  it should "compute pattern matching metrics" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def matchTest(x: Int): String = x match {
        |    case 1 => "one"
        |    case 2 => "two"
        |    case n if n > 10 => "big"
        |    case _ => "other"
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.pmMetrics.matches shouldBe 1
    method.pmMetrics.cases shouldBe 4
    method.pmMetrics.guards should be > 0
    method.pmMetrics.guards should be > 0
  }

  it should "compute branch density metrics" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def branches(x: Int): Int = {
        |    if (x > 0) 1
        |    else if (x < 0) -1
        |    else 0
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.bdMetrics.branches should be > 0
    method.bdMetrics.ifCount should be > 0
  }

  it should "detect explicit return type" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def explicit(): Int = 42
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.inlineAndImplicitMetrics.hasExplicitReturnType shouldBe true
  }

  it should "detect inferred return type" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def inferred() = 42
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.inlineAndImplicitMetrics.hasExplicitReturnType shouldBe false
    method.inlineAndImplicitMetrics.inferredReturnType shouldBe defined
  }

  it should "analyze abstract method declarations" in {
    val ctx = createContext(
      """package com.example
        |
        |trait MyTrait {
        |  def abstractMethod(x: Int, y: String): Boolean
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.metadata.name shouldBe "com.example.MyTrait.abstractMethod"
    method.inlineAndImplicitMetrics.isAbstract shouldBe true
    method.inlineAndImplicitMetrics.hasExplicitReturnType shouldBe true
    method.parameterMetrics.totalParams shouldBe 2
    method.cComplexity shouldBe 0
    method.nestingDepth shouldBe 0
  }

  it should "handle methods with type parameters" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def generic[T](value: T): T = value
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.metadata.signature should include("[T]")
    method.metadata.signature should include("value: T")
  }

  it should "handle methods with complex type parameters" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def complex[T <: Comparable[T], U](t: T, u: U): T = t
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.metadata.signature should include("[T")
    method.metadata.signature should include("U")
  }

  it should "analyze nested methods" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def outer(): Int = {
        |    def inner(): Int = 42
        |    inner()
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 2
    val outer = result.methods.find(_.metadata.name.contains("outer"))
    outer shouldBe defined
    outer.get.metadata.isNested shouldBe false

    val inner = result.methods.find(_.metadata.name.contains("inner"))
    inner shouldBe defined
    inner.get.metadata.isNested shouldBe true
  }

  it should "analyze methods in nested classes" in {
    val ctx = createContext(
      """package com.example
        |
        |class Outer {
        |  class Inner {
        |    def method(): Int = 42
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.metadata.name should include("Outer")
    method.metadata.name should include("Inner")
    method.metadata.name should include("method")
  }

  it should "count lines of code for methods" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def multiline(): Int = {
        |    val a = 1
        |    val b = 2
        |    val c = 3
        |    a + b + c
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.metadata.linesOfCode should be > 1
  }

  it should "handle methods with pattern matching in parameters" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def destructure(tuple: (Int, String)): Int = tuple match {
        |    case (n, _) => n
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.pmMetrics.matches should be > 0
  }

  it should "handle methods with loop constructs" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def loop(n: Int): Int = {
        |    var i = 0
        |    while (i < n) {
        |      i = i + 1
        |    }
        |    i
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.bdMetrics.loopCount should be > 0
  }

  it should "handle methods with try-catch blocks" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def tryCatch(): Int = {
        |    try {
        |      42
        |    } catch {
        |      case _: Exception => 0
        |    }
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.bdMetrics.catchCaseCount should be > 0
  }

  it should "handle methods with boolean operators" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def boolOps(x: Int, y: Int): Boolean = {
        |    x > 0 && y > 0 || x < 0 && y < 0
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.bdMetrics.boolOpsCount should be > 0
  }

  it should "detect implicit conversion methods" in {
    val ctx = createContext(
      """package com.example
        |
        |object Conversions {
        |  implicit def intToString(x: Int): String = x.toString
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.inlineAndImplicitMetrics.isImplicitConversion shouldBe true
  }

  it should "handle methods in companion objects" in {
    val ctx = createContext(
      """package com.example
        |
        |class MyClass
        |
        |object MyClass {
        |  def apply(): MyClass = new MyClass
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.metadata.name should include("MyClass")
    method.metadata.name should include("apply")
  }

  it should "handle methods with lambda functions in body" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def withLambda(): Int = {
        |    val f = (x: Int) => x + 1
        |    f(41)
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    // Should have the outer method
    val outerMethod = result.methods.find(_.metadata.name.contains("withLambda"))
    outerMethod shouldBe defined
  }

  it should "handle methods in default package" in {
    val ctx = createContext(
      """object MyObject {
        |  def method(): Int = 42
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.metadata.name shouldBe "MyObject.method"
  }

  it should "detect parent member for nested methods" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  val x = {
        |    def nested(): Int = 42
        |    nested()
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    val nestedMethod = result.methods.find(_.metadata.name.contains("nested"))
    nestedMethod shouldBe defined
    nestedMethod.get.metadata.parentMember shouldBe defined
    nestedMethod.get.metadata.parentMember.get should include("val")
  }

  it should "compute average cases per match" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def test(x: Int): String = {
        |    val a = x match {
        |      case 1 => "one"
        |      case 2 => "two"
        |    }
        |    val b = x match {
        |      case 3 => "three"
        |      case 4 => "four"
        |      case 5 => "five"
        |      case _ => "other"
        |    }
        |    a + b
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.pmMetrics.matches shouldBe 2
    method.pmMetrics.avgCasesPerMatch should be > 0.0
  }

  it should "handle methods with for comprehensions" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def forComp(): List[Int] = {
        |    for {
        |      i <- 1 to 10
        |      if i % 2 == 0
        |    } yield i
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    // For comprehensions contribute to cyclomatic complexity
    method.cComplexity should be > 1
  }

  it should "compute branch density per 100 lines" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def density(x: Int): Int = {
        |    if (x > 0) 1 else -1
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.bdMetrics.densityPer100 should be >= 0.0
  }

  it should "require fileId to be set in context" in {
    val testFile = createTempFile(
      """package com.example
        |
        |object Test {
        |  def method(): Int = 42
        |}
        |""".stripMargin
    )

    val source = parseFile(testFile)
    val ctx = AnalysisCtx(testFile, source, Some("test-project")) // No fileId set

    an[Exception] should be thrownBy {
      MethodAnalyzer.run(ctx)
    }

    testFile.delete()
  }

  it should "handle methods with nested match expressions" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def nested(x: Int, y: Int): String = x match {
        |    case 1 => y match {
        |      case 1 => "one-one"
        |      case 2 => "one-two"
        |      case _ => "one-other"
        |    }
        |    case 2 => "two"
        |    case _ => "other"
        |  }
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.pmMetrics.nestedMatches should be > 0
    method.pmMetrics.maxNesting should be > 1
  }

  it should "handle empty method body" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def empty(): Unit = ()
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.cComplexity shouldBe 1
    method.nestingDepth shouldBe 0
  }

  it should "handle method with mixed parameter types" in {
    val ctx = createContext(
      """package com.example
        |
        |object MyObject {
        |  def mixed(x: Int, y: => String = "default", z: Int*)(implicit ctx: String): Int = x
        |}
        |""".stripMargin
    )

    val result = MethodAnalyzer.run(ctx)

    result.methods should have size 1
    val method = result.methods.head
    method.parameterMetrics.totalParams shouldBe 4
    method.parameterMetrics.defaultedParams shouldBe 1
    method.parameterMetrics.byNameParams shouldBe 1
    method.parameterMetrics.varargParams shouldBe 1
    method.parameterMetrics.implicitParams shouldBe 1
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
    *   an AnalysisCtx ready for MethodAnalyzer
    */
  private def createContext(code: String, dialect: Dialect = dialects.Scala213): AnalysisCtx = {
    val testFile = createTempFile(code)
    val source = parseFile(testFile, dialect)
    val ctx = AnalysisCtx(testFile, source, Some("test-project"))

    // Run FileAnalyzer first to set fileId (required by MethodAnalyzer)
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
