package com.bitblends.scalametrics.analyzer

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Files
import scala.meta._

/**
  * Unit tests for the FileAnalyzer object.
  *
  * These tests verify that the FileAnalyzer correctly:
  *   - Extracts file-level metrics such as package name, lines of code, and file size
  *   - Updates the analysis context with the appropriate FileMetrics
  *   - Handles various edge cases including empty files, default packages, and nested packages
  */
class FileAnalyzerSpec extends AnyFlatSpec with Matchers {

  behavior of "FileAnalyzer"

  it should "return the correct analyzer name" in {
    FileAnalyzer.name shouldBe "file"
  }

  it should "extract basic file metrics from a simple Scala file" in {
    // Create a temporary test file
    val testFile = createTempFile(
      """package com.example
        |
        |object HelloWorld {
        |  def main(args: Array[String]): Unit = {
        |    println("Hello, World!")
        |  }
        |}
        |""".stripMargin
    )

    // Parse the file
    val source = parseFile(testFile)

    // Create initial context
    val ctx = AnalysisCtx(
      projectId = Some("test-project"),
      file = testFile,
      tree = source
    )

    // Run the analyzer
    val result = FileAnalyzer.run(ctx)

    // Verify the results
    result.fileMetrics shouldBe defined
    val metrics = result.fileMetrics.get

    metrics.projectId shouldBe Some("test-project")
    metrics.file shouldBe testFile
    metrics.packageName shouldBe "com.example"
    metrics.linesOfCode shouldBe 6
    metrics.fileSizeBytes shouldBe testFile.length()
    metrics.fileId should not be empty

    // Verify fileId is set in the context
    result.fileId shouldBe Some(metrics.fileId)

    // Clean up
    testFile.delete()
  }

  it should "handle files with default package" in {
    val testFile = createTempFile(
      """object HelloWorld {
        |  def main(args: Array[String]): Unit = {
        |    println("Hello, World!")
        |  }
        |}
        |""".stripMargin
    )

    val source = parseFile(testFile)
    val ctx = AnalysisCtx(testFile, source, Some("test-project"))
    val result = FileAnalyzer.run(ctx)

    result.fileMetrics shouldBe defined
    result.fileMetrics.get.packageName shouldBe "<default>"

    testFile.delete()
  }

  it should "handle files with nested packages" in {
    val testFile = createTempFile(
      """package com.example.nested.deep
        |
        |class MyClass {
        |  val x = 42
        |}
        |""".stripMargin
    )

    val source = parseFile(testFile)
    val ctx = AnalysisCtx(testFile, source, Some("test-project"))
    val result = FileAnalyzer.run(ctx)

    result.fileMetrics shouldBe defined
    result.fileMetrics.get.packageName shouldBe "com.example.nested.deep"

    testFile.delete()
  }

  it should "correctly count lines of code for empty file" in {
    val testFile = createTempFile("")

    val source = parseFile(testFile)
    val ctx = AnalysisCtx(testFile, source, Some("test-project"))
    val result = FileAnalyzer.run(ctx)

    result.fileMetrics shouldBe defined
    result.fileMetrics.get.linesOfCode shouldBe 0

    testFile.delete()
  }

  it should "correctly count lines of code for multi-line file" in {
    val testFile = createTempFile(
      """package com.example
        |
        |// This is a comment
        |object MyObject {
        |  val a = 1
        |  val b = 2
        |
        |  def sum(): Int = a + b
        |}
        |""".stripMargin
    )

    val source = parseFile(testFile)
    val ctx = AnalysisCtx(testFile, source, Some("test-project"))
    val result = FileAnalyzer.run(ctx)

    result.fileMetrics shouldBe defined
    result.fileMetrics.get.linesOfCode shouldBe 7

    testFile.delete()
  }

  it should "preserve file size in bytes" in {
    val content = "package com.example\n\nobject Test { val x = 1 }\n"
    val testFile = createTempFile(content)

    val source = parseFile(testFile)
    val ctx = AnalysisCtx(testFile, source, Some("test-project"))
    val result = FileAnalyzer.run(ctx)

    result.fileMetrics shouldBe defined
    result.fileMetrics.get.fileSizeBytes shouldBe testFile.length()
    result.fileMetrics.get.fileSizeBytes shouldBe content.getBytes("UTF-8").length

    testFile.delete()
  }

  it should "generate consistent fileId for the same file and project" in {
    val testFile = createTempFile(
      """package com.example
        |
        |object Test
        |""".stripMargin
    )

    val source = parseFile(testFile)
    val ctx1 = AnalysisCtx(testFile, source, Some("test-project"))
    val result1 = FileAnalyzer.run(ctx1)

    val ctx2 = AnalysisCtx(testFile, source, Some("test-project"))
    val result2 = FileAnalyzer.run(ctx2)

    result1.fileMetrics shouldBe defined
    result2.fileMetrics shouldBe defined
    result1.fileMetrics.get.fileId shouldBe result2.fileMetrics.get.fileId

    testFile.delete()
  }

  it should "generate different fileId for different projects" in {
    val testFile = createTempFile(
      """package com.example
        |
        |object Test
        |""".stripMargin
    )

    val source = parseFile(testFile)
    val ctx1 = AnalysisCtx(testFile, source, Some("project-1"))
    val result1 = FileAnalyzer.run(ctx1)

    val ctx2 = AnalysisCtx(testFile, source, Some("project-2"))
    val result2 = FileAnalyzer.run(ctx2)

    result1.fileMetrics shouldBe defined
    result2.fileMetrics shouldBe defined
    result1.fileMetrics.get.fileId should not be result2.fileMetrics.get.fileId

    testFile.delete()
  }

  it should "update context with both fileId and fileMetrics" in {
    val testFile = createTempFile(
      """package com.example
        |
        |object Test
        |""".stripMargin
    )

    val source = parseFile(testFile)
    val ctx = AnalysisCtx(testFile, source, Some("test-project"))

    // Initial context should have no file metrics
    ctx.fileId shouldBe None
    ctx.fileMetrics shouldBe None

    val result = FileAnalyzer.run(ctx)

    // Result should have both fileId and fileMetrics set
    result.fileId shouldBe defined
    result.fileMetrics shouldBe defined
    result.fileId shouldBe Some(result.fileMetrics.get.fileId)

    testFile.delete()
  }

  it should "handle files with package objects" in {
    val testFile = createTempFile(
      """package com.example
        |
        |package object utils {
        |  val constant = 42
        |}
        |""".stripMargin
    )

    val source = parseFile(testFile)
    val ctx = AnalysisCtx(testFile, source, Some("test-project"))
    val result = FileAnalyzer.run(ctx)

    result.fileMetrics shouldBe defined
    // Package objects typically result in "package.objectName" format
    result.fileMetrics.get.packageName should include("com.example")

    testFile.delete()
  }

  it should "not modify existing methods or members in context" in {
    val testFile = createTempFile(
      """package com.example
        |
        |object Test
        |""".stripMargin
    )

    val source = parseFile(testFile)
    val ctx = AnalysisCtx(testFile, source, Some("test-project"))

    val result = FileAnalyzer.run(ctx)

    // FileAnalyzer should not modify methods or members
    result.methods shouldBe empty
    result.members shouldBe empty

    testFile.delete()
  }

  it should "handle Scala 3 syntax with given definitions" in {
    val testFile = createTempFile(
      """package com.example
        |
        |trait Show[A]:
        |  def show(a: A): String
        |
        |given Show[Int] with
        |  def show(n: Int) = n.toString
        |""".stripMargin
    )

    val source = parseFile(testFile, dialects.Scala3)
    val ctx = AnalysisCtx(testFile, source, Some("test-project"))
    val result = FileAnalyzer.run(ctx)

    result.fileMetrics shouldBe defined
    result.fileMetrics.get.packageName shouldBe "com.example"
    result.fileMetrics.get.linesOfCode shouldBe 5

    testFile.delete()
  }

  // Helper methods

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
