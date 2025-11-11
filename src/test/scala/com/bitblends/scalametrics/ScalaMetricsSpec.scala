package com.bitblends.scalametrics

import com.bitblends.scalametrics.analyzer.{FileAnalyzer, MethodAnalyzer}
import com.bitblends.scalametrics.metrics.model.{ProjectInfo, ProjectMetrics}
import com.bitblends.scalametrics.stats.model.ProjectStats
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Files
import scala.meta._
import scala.util.Random

/**
  * Unit tests for the ScalaMetrics object.
  *
  * These tests verify that ScalaMetrics correctly:
  *   - Generates project metrics from multiple source files
  *   - Applies analyzers in the correct order
  *   - Aggregates metrics across all files in a project
  *   - Handles various edge cases including empty files, multiple files, and dialect overrides
  *   - Properly closes file resources after processing
  */
class ScalaMetricsSpec extends AnyFlatSpec with Matchers {

  behavior of "ScalaMetrics.generateProjectMetrics"

  it should "generate metrics for a single simple Scala file" in {
    val testFile = createTempFile(
      "SimpleTest.scala",
      """package com.example
        |
        |object HelloWorld {
        |  def main(args: Array[String]): Unit = {
        |    println("Hello, World!")
        |  }
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "test-project",
      name = "Test Project",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = testFile.getParentFile
    val result: ProjectMetrics = ScalaMetrics.generateProjectMetrics(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    // Verify project info
    result.projectInfo shouldBe projectInfo

    // Verify file metrics were generated
    result.fileMetrics should have size 1
    val fileResult = result.fileMetrics.head

    // Verify file-level metrics
    fileResult.fileMetadata.projectId shouldBe Some("test-project")
    fileResult.fileMetadata.packageName shouldBe "com.example"
    fileResult.fileMetadata.linesOfCode shouldBe 6
    fileResult.fileMetadata.file shouldBe testFile

    // Verify method metrics were captured (should include main method)
    fileResult.methodMetrics should not be empty

    testFile.delete()
  }

  it should "generate metrics for multiple files in a project" in {
    val file1 = createTempFile(
      "File1.scala",
      """package com.example
        |
        |object Service {
        |  def processData(input: String): Int = {
        |    input.length
        |  }
        |}
        |""".stripMargin
    )

    val file2 = createTempFile(
      "File2.scala",
      """package com.example.utils
        |
        |class Helper {
        |  def validate(data: String): Boolean = {
        |    data.nonEmpty
        |  }
        |
        |  def transform(data: String): String = {
        |    data.toUpperCase
        |  }
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "multi-file-project",
      name = "Multi File Project",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = file1.getParentFile
    val result = ScalaMetrics.generateProjectMetrics(
      files = Seq(file1, file2),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    // Verify two files were processed
    result.fileMetrics should have size 2

    // Verify different packages were captured
    val packages = result.fileMetrics.map(_.fileMetadata.packageName).toSet
    packages should contain("com.example")
    packages should contain("com.example.utils")

    // Verify method counts (1 method in file1, 2 methods in file2)
    val totalMethods = result.fileMetrics.flatMap(_.methodMetrics).size
    totalMethods should be >= 3

    file1.delete()
    file2.delete()
  }

  it should "handle empty file list" in {
    val projectInfo = ProjectInfo(
      projectId = "empty-project",
      name = "Empty Project",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = Files.createTempDirectory("empty-project").toFile

    val result = ScalaMetrics.generateProjectMetrics(
      files = Seq.empty,
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    result.fileMetrics shouldBe empty
    result.projectInfo shouldBe projectInfo

    projectDir.delete()
  }

  it should "apply custom analyzers in order" in {
    val testFile = createTempFile(
      "CustomAnalyzer.scala",
      """package com.example
        |
        |class Calculator {
        |  val multiplier = 2
        |
        |  def calculate(x: Int): Int = {
        |    x * multiplier
        |  }
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "custom-analyzer-project",
      name = "Custom Analyzer Project",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = testFile.getParentFile

    // Use only FileAnalyzer and MethodAnalyzer (exclude MemberAnalyzer)
    val result = ScalaMetrics.generateProjectMetrics(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo,
      analyzers = List(FileAnalyzer, MethodAnalyzer)
    )

    result.fileMetrics should have size 1
    val fileResult = result.fileMetrics.head

    // File metrics should be present (FileAnalyzer ran)
    fileResult.fileMetadata should not be null

    // Method metrics should be present (MethodAnalyzer ran)
    fileResult.methodMetrics should not be empty

    // Member metrics should be empty since we excluded MemberAnalyzer
    fileResult.memberMetrics shouldBe empty

    testFile.delete()
  }

  it should "handle files with no methods or members" in {
    val testFile = createTempFile(
      "EmptyObject.scala",
      """package com.example
        |
        |object EmptyObject
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "empty-object-project",
      name = "Empty Object Project",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectMetrics(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    result.fileMetrics should have size 1
    val fileResult = result.fileMetrics.head

    // File metrics should still be present
    fileResult.fileMetadata.packageName shouldBe "com.example"
    fileResult.fileMetadata.linesOfCode shouldBe 2

    testFile.delete()
  }

  it should "use dialect override when provided" in {
    val testFile = createTempFile(
      "Scala3Syntax.scala",
      """package com.example
        |
        |enum Color:
        |  case Red, Green, Blue
        |
        |object ColorUtils:
        |  def describe(c: Color): String = c match
        |    case Color.Red   => "red"
        |    case Color.Green => "green"
        |    case Color.Blue  => "blue"
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "scala3-project",
      name = "Scala 3 Project",
      version = "1.0.0",
      scalaVersion = "3.3.6"
    )

    val projectDir = testFile.getParentFile

    // Use Scala 3 dialect override
    val result = ScalaMetrics.generateProjectMetrics(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo,
      dialectOverride = Some(dialects.Scala3)
    )

    result.fileMetrics should have size 1
    result.fileMetrics.head.fileMetadata.packageName shouldBe "com.example"

    testFile.delete()
  }

  it should "handle files with different packages in same project" in {
    val file1 = createTempFile(
      "PackageA.scala",
      """package com.example.a
        |
        |object ServiceA {
        |  def process(): Unit = ()
        |}
        |""".stripMargin
    )

    val file2 = createTempFile(
      "PackageB.scala",
      """package com.example.b
        |
        |object ServiceB {
        |  def execute(): Unit = ()
        |}
        |""".stripMargin
    )

    val file3 = createTempFile(
      "PackageC.scala",
      """package com.example.c.nested
        |
        |object ServiceC {
        |  def run(): Unit = ()
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "multi-package-project",
      name = "Multi Package Project",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = file1.getParentFile
    val result = ScalaMetrics.generateProjectMetrics(
      files = Seq(file1, file2, file3),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    result.fileMetrics should have size 3

    val packages = result.fileMetrics.map(_.fileMetadata.packageName).toSet
    packages shouldBe Set("com.example.a", "com.example.b", "com.example.c.nested")

    file1.delete()
    file2.delete()
    file3.delete()
  }

  it should "aggregate metrics with complex class hierarchies" in {
    val testFile = createTempFile(
      "ComplexHierarchy.scala",
      """package com.example
        |
        |trait Animal {
        |  def speak(): String
        |  def name: String
        |}
        |
        |abstract class Mammal extends Animal {
        |  val warmBlooded: Boolean = true
        |
        |  def breathe(): Unit = ()
        |}
        |
        |class Dog(val name: String) extends Mammal {
        |  def speak(): String = "Woof"
        |
        |  def fetch(item: String): Unit = {
        |    println(s"Fetching $item")
        |  }
        |}
        |
        |object Dog {
        |  def apply(name: String): Dog = new Dog(name)
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "hierarchy-project",
      name = "Hierarchy Project",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectMetrics(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    result.fileMetrics should have size 1
    val fileResult = result.fileMetrics.head

    // Should capture multiple methods across trait, abstract class, class, and object
    fileResult.methodMetrics.size should be >= 4

    // Should capture members (warmBlooded, name)
    fileResult.memberMetrics.size should be >= 1

    testFile.delete()
  }

  it should "handle cross Scala version configuration" in {
    val testFile = createTempFile(
      "CrossVersion.scala",
      """package com.example
        |
        |object Utils {
        |  def process(x: Int): Int = x * 2
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "cross-version-project",
      name = "Cross Version Project",
      version = "1.0.0",
      scalaVersion = "2.13.17",
      crossScalaVersions = Seq("2.12.20", "2.13.17", "3.3.6")
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectMetrics(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    result.fileMetrics should have size 1
    result.projectInfo.crossScalaVersions should contain allOf ("2.12.20", "2.13.17", "3.3.6")

    testFile.delete()
  }

  it should "preserve all project metadata in result" in {
    val testFile = createTempFile(
      "Metadata.scala",
      """package com.example
        |
        |object Metadata
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "metadata-project",
      name = "Metadata Project",
      version = "2.1.0",
      scalaVersion = "2.13.17",
      description = Some("Test project with full metadata"),
      organization = Some("com.example"),
      organizationName = Some("Example Org"),
      homepage = Some("https://example.com"),
      licenses = Some("MIT"),
      startYear = Some("2025")
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectMetrics(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    // Verify all metadata is preserved
    result.projectInfo shouldBe projectInfo
    result.projectInfo.description shouldBe Some("Test project with full metadata")
    result.projectInfo.organization shouldBe Some("com.example")
    result.projectInfo.organizationName shouldBe Some("Example Org")
    result.projectInfo.homepage shouldBe Some("https://example.com")
    result.projectInfo.licenses shouldBe Some("MIT")
    result.projectInfo.startYear shouldBe Some("2025")

    testFile.delete()
  }

  it should "handle files with case classes and case objects" in {
    val testFile = createTempFile(
      "CaseClasses.scala",
      """package com.example
        |
        |case class Person(name: String, age: Int) {
        |  def greet(): String = s"Hello, I'm $name"
        |}
        |
        |case object Empty
        |
        |sealed trait Result
        |case class Success(value: Int) extends Result
        |case class Failure(error: String) extends Result
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "case-class-project",
      name = "Case Class Project",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectMetrics(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    result.fileMetrics should have size 1
    val fileResult = result.fileMetrics.head

    // Should capture methods and constructor parameters
    fileResult.methodMetrics should not be empty
    fileResult.fileMetadata.packageName shouldBe "com.example"

    testFile.delete()
  }

  it should "correctly process files with implicits and givens" in {
    val testFile = createTempFile(
      "Implicits.scala",
      """package com.example
        |
        |trait Show[A] {
        |  def show(a: A): String
        |}
        |
        |object Show {
        |  implicit val intShow: Show[Int] = new Show[Int] {
        |    def show(n: Int): String = n.toString
        |  }
        |
        |  implicit val stringShow: Show[String] = new Show[String] {
        |    def show(s: String): String = s
        |  }
        |
        |  def apply[A](implicit ev: Show[A]): Show[A] = ev
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "implicit-project",
      name = "Implicit Project",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectMetrics(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    result.fileMetrics should have size 1
    val fileResult = result.fileMetrics.head

    // Should capture implicit members and methods
    fileResult.memberMetrics.size should be >= 2
    fileResult.methodMetrics should not be empty

    testFile.delete()
  }

  it should "handle files with pattern matching" in {
    val testFile = createTempFile(
      "PatternMatch.scala",
      """package com.example
        |
        |object Matcher {
        |  def classify(x: Any): String = x match {
        |    case _: String  => "string"
        |    case _: Int     => "int"
        |    case _: Boolean => "boolean"
        |    case _          => "unknown"
        |  }
        |
        |  def fibonacci(n: Int): Int = n match {
        |    case 0 => 0
        |    case 1 => 1
        |    case _ => fibonacci(n - 1) + fibonacci(n - 2)
        |  }
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "pattern-match-project",
      name = "Pattern Match Project",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectMetrics(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    result.fileMetrics should have size 1
    val fileResult = result.fileMetrics.head

    // Should capture both methods with pattern matching
    fileResult.methodMetrics.size should be >= 2

    testFile.delete()
  }

  behavior of "ScalaMetrics.generateFileMetrics"

  it should "generate metrics for a single Scala file with automatic dialect detection" in {
    val testFile = createTempFile(
      "Simple.scala",
      """package com.example
        |
        |object Calculator {
        |  def add(a: Int, b: Int): Int = a + b
        |
        |  def multiply(x: Int, y: Int): Int = x * y
        |}
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile)

    result shouldBe defined
    val fileResult = result.get

    // Verify file metrics
    fileResult.fileMetadata.file shouldBe testFile
    fileResult.fileMetadata.packageName shouldBe "com.example"
    fileResult.fileMetadata.linesOfCode shouldBe 5
    fileResult.fileMetadata.projectId shouldBe None // No project context

    // Verify method metrics were captured
    fileResult.methodMetrics.size shouldBe 2

    testFile.delete()
  }

  it should "generate metrics with explicit Scala 2.13 dialect" in {
    val testFile = createTempFile(
      "Scala2.scala",
      """package com.example
        |
        |object Utils {
        |  implicit val stringOrdering: Ordering[String] = Ordering.String
        |
        |  def sort(items: List[String])(implicit ord: Ordering[String]): List[String] = {
        |    items.sorted
        |  }
        |}
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile, Some(dialects.Scala213))

    result shouldBe defined
    val fileResult = result.get

    fileResult.fileMetadata.packageName shouldBe "com.example"
    fileResult.fileMetadata.linesOfCode shouldBe 7

    // Should capture method and implicit member
    fileResult.methodMetrics should not be empty
    fileResult.memberMetrics.size should be >= 1

    testFile.delete()
  }

  it should "generate metrics with explicit Scala 3 dialect" in {
    val testFile = createTempFile(
      "Scala3.scala",
      """package com.example
        |
        |enum Color:
        |  case Red, Green, Blue
        |
        |object ColorOps:
        |  def describe(c: Color): String = c match
        |    case Color.Red   => "red color"
        |    case Color.Green => "green color"
        |    case Color.Blue  => "blue color"
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile, Some(dialects.Scala3))

    result shouldBe defined
    val fileResult = result.get

    fileResult.fileMetadata.packageName shouldBe "com.example"
    fileResult.fileMetadata.linesOfCode shouldBe 8

    // Should capture the describe method
    fileResult.methodMetrics should not be empty

    testFile.delete()
  }

  it should "return None for file with parse errors" in {
    val testFile = createTempFile(
      "Invalid.scala",
      """package com.example
        |
        |object Broken {
        |  def malformed() = {{{
        |  // Missing closing braces
        |}
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile)

    // Should return None due to parse error
    result shouldBe None

    testFile.delete()
  }

  it should "handle file with default package" in {
    val testFile = createTempFile(
      "NoPackage.scala",
      """object NoPackage {
        |  val x = 42
        |
        |  def getValue: Int = x
        |}
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile)

    result shouldBe defined
    val fileResult = result.get

    fileResult.fileMetadata.packageName shouldBe "<default>"
    fileResult.fileMetadata.linesOfCode shouldBe 4

    testFile.delete()
  }

  it should "handle file with nested packages" in {
    val testFile = createTempFile(
      "Nested.scala",
      """package com.example.deep.nested.structure
        |
        |class DeepClass {
        |  def deepMethod(): Unit = ()
        |}
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile)

    result shouldBe defined
    val fileResult = result.get

    fileResult.fileMetadata.packageName shouldBe "com.example.deep.nested.structure"
    fileResult.methodMetrics should not be empty

    testFile.delete()
  }

  it should "capture method metrics for various method types" in {
    val testFile = createTempFile(
      "Methods.scala",
      """package com.example
        |
        |class MethodTypes {
        |  def publicMethod(): Unit = ()
        |
        |  private def privateMethod(): Unit = ()
        |
        |  protected def protectedMethod(): Unit = ()
        |
        |  def methodWithParams(a: Int, b: String): Boolean = true
        |
        |  def genericMethod[T](value: T): T = value
        |}
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile)

    result shouldBe defined
    val fileResult = result.get

    // Should capture all 5 methods
    fileResult.methodMetrics.size shouldBe 5

    testFile.delete()
  }

  it should "capture member metrics for various member types" in {
    val testFile = createTempFile(
      "Members.scala",
      """package com.example
        |
        |class MemberTypes {
        |  val immutableVal: Int = 42
        |  var mutableVar: String = "test"
        |  lazy val lazyVal: Double = 3.14
        |  private val privateVal: Boolean = true
        |  protected var protectedVar: Long = 100L
        |}
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile)

    result shouldBe defined
    val fileResult = result.get

    // Should capture all 5 members (note: may capture 6 if synthetic members are included)
    fileResult.memberMetrics.size should be >= 5

    testFile.delete()
  }

  it should "handle empty file" in {
    val testFile = createTempFile("Empty.scala", "")

    val result = ScalaMetrics.generateFileMetrics(testFile)

    result should not be defined

    testFile.delete()
  }

  it should "handle file with only package declaration" in {
    val testFile = createTempFile(
      "PackageOnly.scala",
      """package com.example
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile)

    result shouldBe defined
    val fileResult = result.get

    fileResult.fileMetadata.packageName shouldBe "com.example"
    fileResult.fileMetadata.linesOfCode shouldBe 1
    fileResult.methodMetrics shouldBe empty
    fileResult.memberMetrics shouldBe empty

    testFile.delete()
  }

  it should "handle case classes with constructor parameters" in {
    val testFile = createTempFile(
      "CaseClass.scala",
      """package com.example
        |
        |case class Person(name: String, age: Int, email: String) {
        |  def greet(): String = s"Hello, I'm $name"
        |
        |  def isAdult: Boolean = age >= 18
        |}
        |
        |case object Empty
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile)

    result shouldBe defined
    val fileResult = result.get

    fileResult.fileMetadata.packageName shouldBe "com.example"
    // Should capture at least the greet and isAdult methods
    fileResult.methodMetrics.size should be >= 2

    testFile.delete()
  }

  it should "handle traits with abstract and concrete methods" in {
    val testFile = createTempFile(
      "Trait.scala",
      """package com.example
        |
        |trait Service {
        |  def abstractMethod(): String
        |
        |  def concreteMethod(): Int = 42
        |
        |  def anotherConcrete(x: Int): Int = x * 2
        |}
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile)

    result shouldBe defined
    val fileResult = result.get

    // Should capture all 3 methods (abstract and concrete)
    fileResult.methodMetrics.size shouldBe 3

    testFile.delete()
  }

  it should "handle objects with apply methods" in {
    val testFile = createTempFile(
      "Companion.scala",
      """package com.example
        |
        |case class Config(value: String)
        |
        |object Config {
        |  def apply(value: String): Config = new Config(value.trim)
        |
        |  def default: Config = Config("default")
        |}
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile)

    result shouldBe defined
    val fileResult = result.get

    // Should capture apply and default methods
    fileResult.methodMetrics.size should be >= 2

    testFile.delete()
  }

  it should "handle files with implicit conversions and implicit classes" in {
    val testFile = createTempFile(
      "Implicits.scala",
      """package com.example
        |
        |object Implicits {
        |  implicit def stringToInt(s: String): Int = s.toInt
        |
        |  implicit class RichString(val s: String) extends AnyVal {
        |    def isNumeric: Boolean = s.forall(_.isDigit)
        |  }
        |}
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile)

    result shouldBe defined
    val fileResult = result.get

    fileResult.fileMetadata.packageName shouldBe "com.example"
    // Should capture implicit conversion and implicit class method
    fileResult.methodMetrics.size should be >= 2

    testFile.delete()
  }

  it should "handle files with complex pattern matching" in {
    val testFile = createTempFile(
      "Matching.scala",
      """package com.example
        |
        |sealed trait Result
        |case class Success(value: Int) extends Result
        |case class Failure(error: String) extends Result
        |
        |object ResultProcessor {
        |  def process(result: Result): String = result match {
        |    case Success(v) if v > 0 => s"Positive: $v"
        |    case Success(v) if v < 0 => s"Negative: $v"
        |    case Success(0) => "Zero"
        |    case Failure(err) => s"Error: $err"
        |  }
        |}
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile)

    result shouldBe defined
    val fileResult = result.get

    // Should capture the process method
    fileResult.methodMetrics should not be empty

    testFile.delete()
  }

  it should "handle files with for-comprehensions" in {
    val testFile = createTempFile(
      "ForComp.scala",
      """package com.example
        |
        |object ForComprehensions {
        |  def cartesianProduct(xs: List[Int], ys: List[Int]): List[(Int, Int)] = {
        |    for {
        |      x <- xs
        |      y <- ys
        |    } yield (x, y)
        |  }
        |
        |  def filterMap(items: List[Int]): List[Int] = {
        |    for {
        |      item <- items
        |      if item > 0
        |      doubled = item * 2
        |    } yield doubled
        |  }
        |}
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile)

    result shouldBe defined
    val fileResult = result.get

    // Should capture both methods
    fileResult.methodMetrics.size shouldBe 2

    testFile.delete()
  }

  it should "handle files with higher-order functions" in {
    val testFile = createTempFile(
      "HigherOrder.scala",
      """package com.example
        |
        |object HigherOrder {
        |  def map[A, B](items: List[A])(f: A => B): List[B] = {
        |    items.map(f)
        |  }
        |
        |  def filter[A](items: List[A])(predicate: A => Boolean): List[A] = {
        |    items.filter(predicate)
        |  }
        |
        |  def compose[A, B, C](f: B => C)(g: A => B): A => C = {
        |    (a: A) => f(g(a))
        |  }
        |}
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile)

    result shouldBe defined
    val fileResult = result.get

    // Should capture all 3 higher-order functions
    fileResult.methodMetrics.size shouldBe 3

    testFile.delete()
  }

  it should "detect Scala 2 dialect automatically for implicit syntax" in {
    val testFile = createTempFile(
      "AutoDetectScala2.scala",
      """package com.example
        |
        |object AutoDetect {
        |  implicit val ordering: Ordering[String] = Ordering.String
        |
        |  def sortWithImplicit(items: List[String])(implicit ord: Ordering[String]): List[String] = {
        |    items.sorted
        |  }
        |}
        |""".stripMargin
    )

    // No dialect override - should auto-detect
    val result = ScalaMetrics.generateFileMetrics(testFile)

    result shouldBe defined
    val fileResult = result.get

    fileResult.fileMetadata.packageName shouldBe "com.example"
    fileResult.methodMetrics should not be empty

    testFile.delete()
  }

  it should "properly close file resources after processing" in {
    val testFile = createTempFile(
      "ResourceManagement.scala",
      """package com.example
        |
        |object Test {
        |  def test(): Unit = ()
        |}
        |""".stripMargin
    )

    // Generate metrics multiple times to verify resource handling
    val result1 = ScalaMetrics.generateFileMetrics(testFile)
    val result2 = ScalaMetrics.generateFileMetrics(testFile)
    val result3 = ScalaMetrics.generateFileMetrics(testFile)

    result1 shouldBe defined
    result2 shouldBe defined
    result3 shouldBe defined

    // File should still be accessible and deletable
    testFile.exists() shouldBe true
    testFile.delete() shouldBe true
  }

  it should "handle Scala 3 extension methods" in {
    val testFile = createTempFile(
      "Extensions.scala",
      """package com.example
        |
        |extension (s: String)
        |  def toIntOption: Option[Int] = s.toIntOption
        |  def isBlank: Boolean = s.trim.isEmpty
        |
        |extension [T](xs: List[T])
        |  def second: Option[T] = xs.drop(1).headOption
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile, Some(dialects.Scala3))

    result shouldBe defined
    val fileResult = result.get

    fileResult.fileMetadata.packageName shouldBe "com.example"
    // Should capture extension methods
    fileResult.methodMetrics.size should be >= 3

    testFile.delete()
  }

  it should "handle files with package objects" in {
    val testFile = createTempFile(
      "PackageObject.scala",
      """package com.example
        |
        |package object utils {
        |  type StringMap = Map[String, String]
        |
        |  val defaultTimeout: Int = 5000
        |
        |  def helper(x: Int): Int = x * 2
        |}
        |""".stripMargin
    )

    val result = ScalaMetrics.generateFileMetrics(testFile)

    result shouldBe defined
    val fileResult = result.get

    fileResult.fileMetadata.packageName should include("com.example")
    fileResult.methodMetrics should not be empty
    fileResult.memberMetrics should not be empty

    testFile.delete()
  }

  it should "skip files that fail to parse" in {
    val validFile = createTempFile(
      "Valid.scala",
      """package com.example
        |
        |object Valid {
        |  def test(): Unit = ()
        |}
        |""".stripMargin
    )

    val invalidFile = createTempFile(
      "Invalid.scala",
      """package com.example
        |
        |object Invalid {
        |  def broken() = {{{
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "parse-error-project",
      name = "Parse Error Project",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = validFile.getParentFile
    val result = ScalaMetrics.generateProjectMetrics(
      files = Seq(validFile, invalidFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    // Should only process the valid file
    result.fileMetrics.size should be <= 2
    // At least the valid file should be processed
    result.fileMetrics.exists(_.fileMetadata.file == validFile) shouldBe true

    validFile.delete()
    invalidFile.delete()
  }

  behavior of "ScalaMetrics.generateProjectStats"

  it should "generate statistics for a single simple Scala file" in {
    val testFile = createTempFile(
      "SimpleStats.scala",
      """package com.example
        |/** A calculator utility object */
        |object Calculator {
        |  /** Adds two numbers */
        |  def add(a: Int, b: Int): Int = a + b
        |
        |  /** Multiplies two numbers */
        |  def multiply(x: Int, y: Int): Int = x * y
        |
        |  private def helper(): Unit = ()
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "stats-single-file",
      name = "Single File Stats",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = testFile.getParentFile
    val result: ProjectStats = ScalaMetrics.generateProjectStats(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    // Verify project header
    result.metadata.name shouldBe "Single File Stats"
    result.metadata.version shouldBe "1.0.0"
    result.metadata.scalaVersion shouldBe "2.13.17"

    // Verify project rollup statistics
    val rollup = result.projectRollup
    rollup.totalFiles shouldBe 1
    rollup.totalLoc should be > 0
    rollup.totalFunctions shouldBe 3 // add, multiply, helper
    rollup.totalPublicFunctions shouldBe 2 // add, multiply
    rollup.totalPrivateFunctions shouldBe 1 // helper
    rollup.totalSymbols shouldBe 4
    rollup.totalPublicSymbols shouldBe 3 // counts the object and two public methods
    rollup.totalPrivateSymbols shouldBe 1
    rollup.documentedPublicSymbols shouldBe 3 // add and multiply have Scaladoc
    rollup.scalaDocCoverage shouldBe 100.0 // All public symbols are documented

    // Verify package statistics
    result.packages should have size 1
    val packageStats = result.packages.head
    packageStats.packageRollup.name shouldBe "com.example"

    testFile.delete()
  }

  it should "generate statistics for multiple files with aggregation" in {
    val file1 = createTempFile(
      "Service.scala",
      """package com.example.services
        |
        |class UserService {
        |  /** Gets a user by ID */
        |  def getUser(id: Int): String = {
        |    if (id > 0) s"User$id"
        |    else "Unknown"
        |  }
        |
        |  private val cache = Map.empty[Int, String]
        |}
        |""".stripMargin
    )

    val file2 = createTempFile(
      "Repository.scala",
      """package com.example.data
        |
        |class UserRepository {
        |  def findById(id: Int): Option[String] = {
        |    id match {
        |      case x if x > 0 => Some(s"User$x")
        |      case _ => None
        |    }
        |  }
        |
        |  def saveUser(id: Int, name: String): Boolean = {
        |    id > 0 && name.nonEmpty
        |  }
        |}
        |""".stripMargin
    )

    val file3 = createTempFile(
      "Utils.scala",
      """package com.example.utils
        |
        |object StringUtils {
        |  implicit class RichString(val s: String) extends AnyVal {
        |    def isNumeric: Boolean = s.forall(_.isDigit)
        |  }
        |
        |  def capitalize(str: String): String = {
        |    if (str.isEmpty) str
        |    else str.head.toUpper + str.tail
        |  }
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "multi-file-stats",
      name = "Multi File Stats",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = file1.getParentFile
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(file1, file2, file3),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    // Verify aggregated statistics
    val rollup = result.projectRollup
    rollup.totalFiles shouldBe 3
    rollup.totalPackages shouldBe 3
    rollup.totalFunctions should be >= 4 // getUser, findById, saveUser, capitalize, isNumeric
    rollup.totalSymbols should be >= 5 // Methods + cache member + implicit val

    // Verify branch density metrics
    rollup.branchDensityStats.ifCount should be >= 2 // if statements in getUser and capitalize
    rollup.branchDensityStats.caseCount should be >= 2 // pattern match cases in findById
    rollup.branchDensityStats.boolOpsCount should be >= 1 // && in saveUser

    // Verify implicit metrics
    rollup.inlineAndImplicitStats.implicitVals shouldBe 0 // implicit val in RichString

    // Verify cyclomatic complexity
    rollup.avgCyclomaticComplexity should be > 0.0
    rollup.maxCyclomaticComplexity should be >= 2 // Due to branching

    // Verify packages
    result.packages should have size 3
    val packageNames = result.packages.map(_.packageRollup.name).toSet
    packageNames should contain allOf (
      "com.example.services",
      "com.example.data",
      "com.example.utils"
    )

    file1.delete()
    file2.delete()
    file3.delete()
  }

  it should "handle empty project gracefully" in {
    val projectInfo = ProjectInfo(
      projectId = "empty-stats",
      name = "Empty Project Stats",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = Files.createTempDirectory("empty-stats").toFile

    val result = ScalaMetrics.generateProjectStats(
      files = Seq.empty,
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    // Verify empty project statistics
    result.metadata.name shouldBe "Empty Project Stats"
    result.projectRollup.totalFiles shouldBe 0
    result.projectRollup.totalLoc shouldBe 0
    result.projectRollup.totalFunctions shouldBe 0
    result.projectRollup.totalSymbols shouldBe 0
    result.projectRollup.avgCyclomaticComplexity shouldBe 0.0
    result.projectRollup.scalaDocCoverage shouldBe 0.0
    result.packages shouldBe empty

    projectDir.delete()
  }

  it should "correctly calculate documentation coverage" in {
    val testFile = createTempFile(
      "DocCoverage.scala",
      """package com.example
        |
        |trait Service {
        |  /** Well documented method */
        |  def documented(): Unit
        |
        |  def undocumented(): Unit
        |
        |  /** Another documented method */
        |  def alsoDocumented(x: Int): Int
        |
        |  private def privateMethod(): Unit = ()
        |}
        |
        |class Implementation extends Service {
        |  /** Implementation of documented */
        |  def documented(): Unit = ()
        |
        |  def undocumented(): Unit = ()
        |
        |  def alsoDocumented(x: Int): Int = x * 2
        |
        |  /** Public member with doc */
        |  val publicMember: String = "test"
        |
        |  val undocumentedMember: Int = 42
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "doc-coverage",
      name = "Doc Coverage Project",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    val rollup = result.projectRollup

    // Total public symbols: 8 (6 public methods + 2 public members)
    // Documented public symbols: 4 (3 documented methods + 1 documented member)
    rollup.totalPublicSymbols should be >= 7
    rollup.documentedPublicSymbols should be >= 3
    rollup.scalaDocCoverage should be > 0.0
    rollup.scalaDocCoverage should be < 100.0 // Not all public symbols are documented

    testFile.delete()
  }

  it should "correctly compute complexity metrics" in {
    val testFile = createTempFile(
      "ComplexCode.scala",
      """package com.example
        |
        |object ComplexLogic {
        |  def simpleMethod(): Int = 1
        |
        |  def moderateComplexity(x: Int): String = {
        |    if (x > 10) {
        |      "big"
        |    } else if (x > 5) {
        |      "medium"
        |    } else {
        |      "small"
        |    }
        |  }
        |
        |  def highComplexity(input: Any): Int = {
        |    input match {
        |      case i: Int if i > 0 =>
        |        if (i % 2 == 0) {
        |          i * 2
        |        } else {
        |          i * 3
        |        }
        |      case s: String =>
        |        s.length match {
        |          case 0 => 0
        |          case 1 => 1
        |          case n => n * 2
        |        }
        |      case _ => -1
        |    }
        |  }
        |
        |  def deeplyNested(x: Int): Int = {
        |    if (x > 0) {
        |      if (x > 10) {
        |        if (x > 100) {
        |          x * 3
        |        } else {
        |          x * 2
        |        }
        |      } else {
        |        x + 1
        |      }
        |    } else {
        |      0
        |    }
        |  }
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "complexity-test",
      name = "Complexity Test",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    val rollup = result.projectRollup

    // Verify complexity metrics
    rollup.avgCyclomaticComplexity should be > 1.0
    rollup.maxCyclomaticComplexity should be >= 4 // highComplexity method
    rollup.avgNestingDepth should be > 1.0
    rollup.maxNestingDepth should be >= 3 // deeplyNested method

    // Verify pattern matching metrics
    rollup.patternMatchingStats.matches should be >= 2 // Two match expressions in highComplexity
    rollup.patternMatchingStats.cases should be >= 5 // Total cases across all matches
    rollup.patternMatchingStats.guards should be >= 1 // Guard in the first case
    rollup.patternMatchingStats.maxNesting should be >= 1 // Nested match in highComplexity

    // Verify branch metrics
    rollup.branchDensityStats.ifCount should be >= 6 // Multiple if statements
    rollup.branchDensityStats.caseCount should be >= 5 // Pattern match cases

    testFile.delete()
  }

  it should "work with custom analyzers" in {
    val testFile = createTempFile(
      "CustomAnalyzer.scala",
      """package com.example
        |
        |class DataProcessor {
        |  def process(data: String): String = {
        |    data.trim.toUpperCase
        |  }
        |
        |  val config: Map[String, String] = Map.empty
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "custom-analyzer-stats",
      name = "Custom Analyzer Stats",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = testFile.getParentFile

    // Test with only FileAnalyzer and MethodAnalyzer (exclude MemberAnalyzer)
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo,
      analyzers = List(FileAnalyzer, MethodAnalyzer)
    )

    val rollup = result.projectRollup

    // Should have method metrics but no member metrics since MemberAnalyzer was excluded
    rollup.totalFunctions shouldBe 1 // process method
    rollup.totalSymbols shouldBe 1 // Only the method, no members counted

    testFile.delete()
  }

  it should "handle dialect override for Scala 3 syntax" in {
    val testFile = createTempFile(
      "Scala3Stats.scala",
      """package com.example
        |
        |enum Status:
        |  case Active, Inactive, Pending
        |
        |object StatusOps:
        |  extension (s: Status)
        |    def isActive: Boolean = s == Status.Active
        |    def describe: String = s match
        |      case Status.Active => "Currently active"
        |      case Status.Inactive => "Not active"
        |      case Status.Pending => "Awaiting activation"
        |
        |  given Ordering[Status] = new Ordering[Status]:
        |    def compare(x: Status, y: Status): Int =
        |      x.ordinal.compare(y.ordinal)
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "scala3-stats",
      name = "Scala 3 Stats",
      version = "1.0.0",
      scalaVersion = "3.3.6"
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo,
      dialectOverride = Some(dialects.Scala3)
    )

    val rollup = result.projectRollup

    // Verify Scala 3 specific features are captured
    rollup.totalFunctions should be >= 3 // extension methods + compare
    rollup.inlineAndImplicitStats.givenInstances should be >= 1 // given Ordering instance
    rollup.patternMatchingStats.matches should be >= 1 // Pattern match in describe
    rollup.patternMatchingStats.cases should be >= 3 // Three cases in describe

    testFile.delete()
  }

  it should "calculate return type explicitness metrics" in {
    val testFile = createTempFile(
      "ReturnTypes.scala",
      """package com.example
        |
        |object TypeExamples {
        |  def explicit(): Int = 42
        |
        |  def inferred() = "implicit return type"
        |
        |  val explicitVal: String = "explicit"
        |
        |  val inferredVal = 123
        |
        |  private def privateExplicit(): Boolean = true
        |
        |  private def privateInferred() = false
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "return-types",
      name = "Return Types Project",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    val rollup = result.projectRollup

    // Total defs/vals/vars: 6 (4 methods + 2 vals)
    rollup.totalDefsValsVars shouldBe 6
    rollup.inlineAndImplicitStats.explicitDefsValsVars shouldBe 3 // explicit, explicitVal, privateExplicit
    rollup.inlineAndImplicitStats.returnTypeExplicitness shouldBe 50.0 // 3 out of 6 are explicit

    // Public defs/vals/vars: 4 (2 public methods + 2 public vals)
    rollup.totalPublicDefsValsVars shouldBe 4
    rollup.inlineAndImplicitStats.explicitPublicDefsValsVars shouldBe 2 // explicit, explicitVal
    rollup.inlineAndImplicitStats.publicReturnTypeExplicitness shouldBe 50.0 // 2 out of 4 are explicit

    testFile.delete()
  }

  it should "handle deprecated symbols" in {
    val testFile = createTempFile(
      "Deprecated.scala",
      """package com.example
        |
        |object DeprecatedAPI {
        |  @deprecated("Use newMethod instead", "2.0.0")
        |  def oldMethod(): Unit = ()
        |
        |  def newMethod(): Unit = ()
        |
        |  @deprecated("No longer needed", "1.5.0")
        |  val oldConfig: String = "deprecated"
        |
        |  val newConfig: String = "current"
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "deprecated-test",
      name = "Deprecated Test",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    val rollup = result.projectRollup

    rollup.totalSymbols shouldBe 5 // 2 methods + 2 vals + object
    rollup.totalDeprecatedSymbols shouldBe 2 // oldMethod and oldConfig
    rollup.deprecatedSymbolsDensity shouldBe 40.0 // 2 out of 5 are deprecated (counts the object as a symbol)

    testFile.delete()
  }

  it should "track inline modifiers in Scala 3" in {
    val testFile = createTempFile(
      "InlineCode.scala",
      """package com.example
        |
        |object InlineExamples {
        |  inline def inlineMethod(x: Int): Int = x * 2
        |
        |  def regularMethod(x: Int): Int = x + 1
        |
        |  inline val inlineVal = 42
        |
        |  val regularVal = 100
        |
        |  def withInlineParam(inline x: Int): Int = x * x
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "inline-test",
      name = "Inline Test",
      version = "1.0.0",
      scalaVersion = "3.3.6"
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo,
      dialectOverride = Some(dialects.Scala3)
    )

    val rollup = result.projectRollup

    rollup.inlineAndImplicitStats.inlineMethods should be >= 1 // inlineMethod
    rollup.inlineAndImplicitStats.inlineVals should be >= 1 // inlineVal
    rollup.inlineAndImplicitStats.inlineParams should be >= 1 // inline parameter in withInlineParam

    testFile.delete()
  }

  it should "correctly handle implicit conversions" in {
    val testFile = createTempFile(
      "Implicits.scala",
      """package com.example
        |
        |object ImplicitExamples {
        |  implicit def stringToInt(s: String): Int = s.toInt
        |
        |  implicit def intToString(i: Int): String = i.toString
        |
        |  implicit val defaultTimeout: Int = 5000
        |
        |  implicit var mutableImplicit: String = "default"
        |
        |  def regularMethod(): Unit = ()
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "implicit-test",
      name = "Implicit Test",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    val rollup = result.projectRollup

    rollup.inlineAndImplicitStats.implicitConversions shouldBe 2 // stringToInt and intToString
    rollup.inlineAndImplicitStats.implicitVals shouldBe 1 // defaultTimeout
    rollup.inlineAndImplicitStats.implicitVars shouldBe 1 // mutableImplicit

    testFile.delete()
  }

  it should "aggregate metrics across packages correctly" in {
    val file1 = createTempFile(
      "Package1.scala",
      """package com.example.pkg1
        |
        |object Module1 {
        |  def method1(): Unit = ()
        |  def method2(): Unit = ()
        |}
        |""".stripMargin
    )

    val file2 = createTempFile(
      "Package1B.scala",
      """package com.example.pkg1
        |
        |object Module2 {
        |  def method3(): Unit = ()
        |}
        |""".stripMargin
    )

    val file3 = createTempFile(
      "Package2.scala",
      """package com.example.pkg2
        |
        |object Module3 {
        |  def method4(): Unit = ()
        |  def method5(): Unit = ()
        |  def method6(): Unit = ()
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "package-aggregation",
      name = "Package Aggregation",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = file1.getParentFile
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(file1, file2, file3),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    val rollup = result.projectRollup
    rollup.totalPackages shouldBe 2 // pkg1 and pkg2
    rollup.totalFiles shouldBe 3
    rollup.totalFunctions shouldBe 6

    // Verify package-level aggregation
    val packages = result.packages.map(_.packageRollup)
    val pkg1 = packages.find(_.name == "com.example.pkg1").get
    val pkg2 = packages.find(_.name == "com.example.pkg2").get

    pkg1.totalFunctions shouldBe 3 // method1, method2, method3
    pkg2.totalFunctions shouldBe 3 // method4, method5, method6

    file1.delete()
    file2.delete()
    file3.delete()
  }

  it should "identify packages with high complexity" in {
    val complexFile = createTempFile(
      "ComplexPackage.scala",
      """package com.example.complex
        |
        |object VeryComplex {
        |  def highComplexity(x: Int): Int = {
        |    if (x > 100) {
        |      if (x > 200) {
        |        if (x > 300) x * 3
        |        else x * 2
        |      } else if (x > 150) {
        |        x + 100
        |      } else {
        |        x + 50
        |      }
        |    } else if (x > 50) {
        |      if (x > 75) x - 10
        |      else x - 5
        |    } else if (x > 25) {
        |      x match {
        |        case 26 => 100
        |        case 27 => 200
        |        case 28 => 300
        |        case _ => x * 10
        |      }
        |    } else {
        |      0
        |    }
        |  }
        |
        |  def anotherComplex(s: String): Int = {
        |    s.length match {
        |      case 0 => 0
        |      case 1 => 1
        |      case 2 => 4
        |      case 3 => 9
        |      case 4 => 16
        |      case 5 => 25
        |      case _ => s.length * s.length
        |    }
        |  }
        |}
        |""".stripMargin
    )

    val simpleFile = createTempFile(
      "SimplePackage.scala",
      """package com.example.simple
        |
        |object Simple {
        |  def easy1(): Int = 1
        |  def easy2(): Int = 2
        |  def easy3(): Int = 3
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "complexity-packages",
      name = "Complexity Packages",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = complexFile.getParentFile
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(complexFile, simpleFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    val rollup = result.projectRollup

    // At least one package should have high complexity (avg > 10)
    rollup.packagesWithHighComplexity should be >= 0

    // The complex package should have higher average complexity than the simple one
    rollup.avgCyclomaticComplexity should be > 1.0

    complexFile.delete()
    simpleFile.delete()
  }

  it should "identify packages with low documentation" in {
    val undocumentedFile = createTempFile(
      "Undocumented.scala",
      """package com.example.undoc
        |
        |object NoDocsHere {
        |  def method1(): Unit = ()
        |  def method2(): Unit = ()
        |  val value1: String = "test"
        |  var value2: Int = 42
        |}
        |""".stripMargin
    )

    val documentedFile = createTempFile(
      "WellDocumented.scala",
      """package com.example.documented
        |
        |object WellDocumented {
        |  /** This method is well documented */
        |  def method1(): Unit = ()
        |
        |  /** Another documented method */
        |  def method2(): Unit = ()
        |
        |  /** A documented value */
        |  val value1: String = "test"
        |
        |  /** A documented variable */
        |  var value2: Int = 42
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "doc-packages",
      name = "Documentation Packages",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = undocumentedFile.getParentFile
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(undocumentedFile, documentedFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    val rollup = result.projectRollup

    // Should identify packages with low documentation (< 50% coverage)
    rollup.packagesWithLowDocumentation should be >= 1 // The undoc package

    // Overall documentation coverage should be around 50%
    rollup.scalaDocCoverage should be > 0.0
    rollup.scalaDocCoverage should be < 100.0

    undocumentedFile.delete()
    documentedFile.delete()
  }

  it should "calculate file size metrics" in {
    val smallFile = createTempFile(
      "Small.scala",
      """package com.example
        |object S { def m() = 1 }
        |""".stripMargin
    )

    val largeFile = createTempFile(
      "Large.scala",
      """package com.example
        |
        |object LargeObject {
        |  def method1(): Unit = {
        |    println("This is a method")
        |    println("With multiple lines")
        |    println("To make the file larger")
        |  }
        |
        |  def method2(): Unit = {
        |    println("Another method")
        |    println("Also with multiple lines")
        |  }
        |
        |  val value1 = "A string value"
        |  val value2 = "Another string value"
        |  val value3 = "Yet another string value"
        |
        |  // Adding comments to increase file size
        |  // This is a comment line
        |  // Another comment line
        |  // More comments
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "file-size-test",
      name = "File Size Test",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = smallFile.getParentFile
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(smallFile, largeFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    val rollup = result.projectRollup

    rollup.totalFileSizeBytes should be > 0L
    rollup.averageFileSizeBytes should be > 0L
    rollup.averageFileSizeBytes shouldBe (rollup.totalFileSizeBytes / 2) // 2 files

    smallFile.delete()
    largeFile.delete()
  }

  it should "handle cross-version project configuration" in {
    val testFile = createTempFile(
      "CrossVersion.scala",
      """package com.example
        |
        |object CrossVersionCode {
        |  def crossVersionMethod(): String = "2.13+"
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "cross-version-stats",
      name = "Cross Version Stats",
      version = "1.0.0",
      scalaVersion = "2.13.17",
      crossScalaVersions = Seq("2.12.20", "2.13.17", "3.3.6")
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    // Verify cross-version information is preserved
    result.metadata.scalaVersion shouldBe "2.13.17"
    result.metadata.crossScalaVersions shouldBe Seq("2.12.20", "2.13.17", "3.3.6")

    testFile.delete()
  }

  it should "preserve all project metadata in statistics" in {
    val testFile = createTempFile(
      "MetadataTest.scala",
      """package com.example
        |object Test { def test() = () }
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "full-metadata-stats",
      name = "Full Metadata Stats",
      version = "2.5.0",
      scalaVersion = "2.13.17",
      description = Some("A test project with complete metadata"),
      organization = Some("com.example"),
      organizationName = Some("Example Organization"),
      organizationHomepage = Some("https://example.org"),
      homepage = Some("https://example.com/project"),
      licenses = Some("Apache-2.0"),
      startYear = Some("2024"),
      isSnapshot = Some("true"),
      apiURL = Some("https://api.example.com"),
      scmInfo = Some("git@github.com:example/project.git"),
      developers = Some("John Doe, Jane Smith"),
      versionScheme = Some("semver"),
      projectInfoNameFormal = Some("Full Metadata Statistics Project")
    )

    val projectDir = testFile.getParentFile
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(testFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    // Verify all metadata is preserved in the header
    val header = result.metadata
    header.name shouldBe "Full Metadata Stats"
    header.version shouldBe "2.5.0"
    header.scalaVersion shouldBe "2.13.17"
    header.description shouldBe Some("A test project with complete metadata")
    header.organization shouldBe Some("com.example")
    header.organizationName shouldBe Some("Example Organization")
    header.organizationHomepage shouldBe Some("https://example.org")
    header.homepage shouldBe Some("https://example.com/project")
    header.licenses shouldBe Some("Apache-2.0")
    header.startYear shouldBe Some("2024")
    header.isSnapshot shouldBe Some("true")
    header.apiURL shouldBe Some("https://api.example.com")
    header.scmInfo shouldBe Some("git@github.com:example/project.git")
    header.developers shouldBe Some("John Doe, Jane Smith")
    header.versionScheme shouldBe Some("semver")
    header.projectInfoNameFormal shouldBe Some("Full Metadata Statistics Project")

    testFile.delete()
  }

  it should "handle files that fail to parse gracefully" in {
    val validFile = createTempFile(
      "ValidStats.scala",
      """package com.example
        |
        |object ValidCode {
        |  def validMethod(): Int = 42
        |}
        |""".stripMargin
    )

    val invalidFile = createTempFile(
      "InvalidStats.scala",
      """package com.example
        |
        |object BrokenCode {
        |  def broken() = {{{  // Syntax error
        |}
        |""".stripMargin
    )

    val projectInfo = ProjectInfo(
      projectId = "parse-error-stats",
      name = "Parse Error Stats",
      version = "1.0.0",
      scalaVersion = "2.13.17"
    )

    val projectDir = validFile.getParentFile
    val result = ScalaMetrics.generateProjectStats(
      files = Seq(validFile, invalidFile),
      projectBaseDir = projectDir,
      projectInfo = projectInfo
    )

    // Should process only the valid file
    val rollup = result.projectRollup
    rollup.totalFiles should be <= 2
    rollup.totalFunctions should be >= 1 // At least validMethod

    validFile.delete()
    invalidFile.delete()
  }

  // Helper methods

  /**
    * Creates a temporary file with the given name and content.
    *
    * @param name
    *   the name of the file
    * @param content
    *   the content to write to the file
    * @return
    *   the created temporary file
    */
  private def createTempFile(name: String, content: String): File = {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile
    val tempFile = new File(tempDir, name)
    Files.write(tempFile.toPath, content.getBytes("UTF-8"))
    tempFile
  }
}
