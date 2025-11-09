package com.bitblends.scalametrics.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Files
import scala.meta.dialects._
import scala.util.Random

/**
  * Unit tests for the DialectConfig object.
  *
  * These tests verify that the dialect configuration correctly:
  *   - Maps version strings to Scalameta dialects
  *   - Determines dialects from Scala version strings
  *   - Reads dialect configuration from .scalafmt.conf files
  *   - Resolves dialects for files based on source directory structure
  *   - Handles various edge cases and fallbacks
  */
class DialectConfigSpec extends AnyFlatSpec with Matchers {

  behavior of "DialectConfig"

  // ========== Dialect Map Tests ==========

  it should "map scala210 to Scala210 dialect" in {
    DialectConfig.dialectMap("scala210") shouldBe Scala210
    DialectConfig.dialectMap("Scala210") shouldBe Scala210
    DialectConfig.dialectMap("2.10") shouldBe Scala210
  }

  it should "map scala211 to Scala211 dialect" in {
    DialectConfig.dialectMap("scala211") shouldBe Scala211
    DialectConfig.dialectMap("Scala211") shouldBe Scala211
    DialectConfig.dialectMap("2.11") shouldBe Scala211
  }

  it should "map scala212 to Scala212 dialect" in {
    DialectConfig.dialectMap("scala212") shouldBe Scala212
    DialectConfig.dialectMap("Scala212") shouldBe Scala212
    DialectConfig.dialectMap("2.12") shouldBe Scala212
  }

  it should "map scala213 to Scala213 dialect" in {
    DialectConfig.dialectMap("scala213") shouldBe Scala213
    DialectConfig.dialectMap("Scala213") shouldBe Scala213
    DialectConfig.dialectMap("2.13") shouldBe Scala213
  }

  it should "map scala3 to Scala3 dialect" in {
    DialectConfig.dialectMap("scala3") shouldBe Scala3
    DialectConfig.dialectMap("Scala3") shouldBe Scala3
    DialectConfig.dialectMap("3") shouldBe Scala3
    DialectConfig.dialectMap("dotty") shouldBe Scala3
    DialectConfig.dialectMap("Dotty") shouldBe Scala3
  }

  it should "map specific Scala 3 versions to their dialects" in {
    DialectConfig.dialectMap("scala30") shouldBe Scala30
    DialectConfig.dialectMap("Scala30") shouldBe Scala30
    DialectConfig.dialectMap("3.0") shouldBe Scala30

    DialectConfig.dialectMap("scala31") shouldBe Scala31
    DialectConfig.dialectMap("Scala31") shouldBe Scala31
    DialectConfig.dialectMap("3.1") shouldBe Scala31

    DialectConfig.dialectMap("scala32") shouldBe Scala32
    DialectConfig.dialectMap("Scala32") shouldBe Scala32
    DialectConfig.dialectMap("3.2") shouldBe Scala32

    DialectConfig.dialectMap("scala33") shouldBe Scala33
    DialectConfig.dialectMap("Scala33") shouldBe Scala33
    DialectConfig.dialectMap("3.3") shouldBe Scala33
  }

  it should "map unsupported Scala 3.4 and 3.5 to Scala3 fallback" in {
    DialectConfig.dialectMap("3.4") shouldBe Scala3
    DialectConfig.dialectMap("3.5") shouldBe Scala3
  }

  it should "map source3 variants to Source3 dialects" in {
    DialectConfig.dialectMap("scala212source3") shouldBe Scala212Source3
    DialectConfig.dialectMap("Scala212Source3") shouldBe Scala212Source3

    DialectConfig.dialectMap("scala213source3") shouldBe Scala213Source3
    DialectConfig.dialectMap("Scala213Source3") shouldBe Scala213Source3
  }

  // ========== dialectFromScalaVersion Tests ==========

  it should "convert Scala 2.10.x versions to Scala210 dialect" in {
    DialectConfig.dialectFromScalaVersion("2.10") shouldBe Scala210
    DialectConfig.dialectFromScalaVersion("2.10.7") shouldBe Scala210
    DialectConfig.dialectFromScalaVersion("2.10.0") shouldBe Scala210
  }

  it should "convert Scala 2.11.x versions to Scala211 dialect" in {
    DialectConfig.dialectFromScalaVersion("2.11") shouldBe Scala211
    DialectConfig.dialectFromScalaVersion("2.11.12") shouldBe Scala211
    DialectConfig.dialectFromScalaVersion("2.11.0") shouldBe Scala211
  }

  it should "convert Scala 2.12.x versions to Scala212 dialect" in {
    DialectConfig.dialectFromScalaVersion("2.12") shouldBe Scala212
    DialectConfig.dialectFromScalaVersion("2.12.20") shouldBe Scala212
    DialectConfig.dialectFromScalaVersion("2.12.0") shouldBe Scala212
  }

  it should "convert Scala 2.13.x versions to Scala213 dialect" in {
    DialectConfig.dialectFromScalaVersion("2.13") shouldBe Scala213
    DialectConfig.dialectFromScalaVersion("2.13.17") shouldBe Scala213
    DialectConfig.dialectFromScalaVersion("2.13.0") shouldBe Scala213
  }

  it should "convert Scala 3.0.x versions to Scala30 dialect" in {
    DialectConfig.dialectFromScalaVersion("3.0") shouldBe Scala30
    DialectConfig.dialectFromScalaVersion("3.0.2") shouldBe Scala30
    DialectConfig.dialectFromScalaVersion("3.0.0") shouldBe Scala30
  }

  it should "convert Scala 3.1.x versions to Scala31 dialect" in {
    DialectConfig.dialectFromScalaVersion("3.1") shouldBe Scala31
    DialectConfig.dialectFromScalaVersion("3.1.3") shouldBe Scala31
    DialectConfig.dialectFromScalaVersion("3.1.0") shouldBe Scala31
  }

  it should "convert Scala 3.2.x versions to Scala32 dialect" in {
    DialectConfig.dialectFromScalaVersion("3.2") shouldBe Scala32
    DialectConfig.dialectFromScalaVersion("3.2.2") shouldBe Scala32
    DialectConfig.dialectFromScalaVersion("3.2.0") shouldBe Scala32
  }

  it should "convert Scala 3.3.x versions to Scala33 dialect" in {
    DialectConfig.dialectFromScalaVersion("3.3") shouldBe Scala33
    DialectConfig.dialectFromScalaVersion("3.3.6") shouldBe Scala33
    DialectConfig.dialectFromScalaVersion("3.3.0") shouldBe Scala33
  }

  it should "fall back to Scala3 for Scala 3.4 and 3.5" in {
    DialectConfig.dialectFromScalaVersion("3.4") shouldBe Scala3
    DialectConfig.dialectFromScalaVersion("3.4.0") shouldBe Scala3
    DialectConfig.dialectFromScalaVersion("3.5") shouldBe Scala3
    DialectConfig.dialectFromScalaVersion("3.5.0") shouldBe Scala3
  }

  it should "fall back to Scala3 for unknown Scala 3.x versions" in {
    DialectConfig.dialectFromScalaVersion("3.99") shouldBe Scala3
    DialectConfig.dialectFromScalaVersion("3.100.0") shouldBe Scala3
  }

  it should "fall back to Scala213 for unknown versions" in {
    DialectConfig.dialectFromScalaVersion("4.0") shouldBe Scala213
    DialectConfig.dialectFromScalaVersion("invalid") shouldBe Scala213
    DialectConfig.dialectFromScalaVersion("2") shouldBe Scala213
  }

  it should "handle versions with -RC or -SNAPSHOT suffixes" in {
    DialectConfig.dialectFromScalaVersion("2.13.12-RC1") shouldBe Scala213
    DialectConfig.dialectFromScalaVersion("3.3.1-SNAPSHOT") shouldBe Scala33
    DialectConfig.dialectFromScalaVersion("3.4.0-RC1") shouldBe Scala3
  }

  // ========== readScalafmtDialect Tests ==========

  it should "return None for non-existent .scalafmt.conf file" in {
    val nonExistentFile = new File("/tmp/nonexistent-scalafmt-" + System.currentTimeMillis() + ".conf")
    DialectConfig.readScalafmtDialect(nonExistentFile) shouldBe None
  }

  it should "read dialect from .scalafmt.conf with runner.dialect = value" in {
    val tempFile = createTempScalafmtConf("runner.dialect = scala213")
    try {
      DialectConfig.readScalafmtDialect(tempFile) shouldBe Some("scala213")
    } finally {
      tempFile.delete()
    }
  }

  it should "read dialect from .scalafmt.conf with runner.dialect=value (no spaces)" in {
    val tempFile = createTempScalafmtConf("runner.dialect=scala3")
    try {
      DialectConfig.readScalafmtDialect(tempFile) shouldBe Some("scala3")
    } finally {
      tempFile.delete()
    }
  }

  it should "read dialect from .scalafmt.conf with quotes" in {
    val tempFile = createTempScalafmtConf("""runner.dialect = "scala212"""")
    try {
      DialectConfig.readScalafmtDialect(tempFile) shouldBe Some("scala212")
    } finally {
      tempFile.delete()
    }
  }

  it should "read dialect from .scalafmt.conf with single quotes" in {
    val tempFile = createTempScalafmtConf("runner.dialect = 'scala33'")
    try {
      DialectConfig.readScalafmtDialect(tempFile) shouldBe Some("scala33")
    } finally {
      tempFile.delete()
    }
  }

  it should "read dialect from .scalafmt.conf ignoring comments" in {
    val tempFile = createTempScalafmtConf("runner.dialect = scala213 # this is a comment")
    try {
      DialectConfig.readScalafmtDialect(tempFile) shouldBe Some("scala213")
    } finally {
      tempFile.delete()
    }
  }

  it should "read dialect from .scalafmt.conf with surrounding whitespace" in {
    val tempFile = createTempScalafmtConf("  runner.dialect  =  scala212  ")
    try {
      DialectConfig.readScalafmtDialect(tempFile) shouldBe Some("scala212")
    } finally {
      tempFile.delete()
    }
  }

  it should "return None when runner.dialect is not present" in {
    val tempFile = createTempScalafmtConf(
      """version = "3.0.0"
        |maxColumn = 120
        |""".stripMargin
    )
    try {
      DialectConfig.readScalafmtDialect(tempFile) shouldBe None
    } finally {
      tempFile.delete()
    }
  }

  it should "read dialect from multi-line .scalafmt.conf" in {
    val tempFile = createTempScalafmtConf(
      """version = "3.0.0"
        |maxColumn = 120
        |runner.dialect = scala33
        |align.preset = more
        |""".stripMargin
    )
    try {
      DialectConfig.readScalafmtDialect(tempFile) shouldBe Some("scala33")
    } finally {
      tempFile.delete()
    }
  }

  // ========== getDialect Tests (legacy method) ==========

  it should "use .scalafmt.conf dialect when available" in {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile
    val scalafmtFile = new File(tempDir, ".scalafmt.conf")

    try {
      writeFile(scalafmtFile, "runner.dialect = scala212")

      val dialect = DialectConfig.getDialect(tempDir, "2.13.17")
      dialect shouldBe Scala212
    } finally {
      scalafmtFile.delete()
      tempDir.delete()
    }
  }

  it should "fall back to scalaVersion when .scalafmt.conf is not present" in {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile

    try {
      val dialect = DialectConfig.getDialect(tempDir, "2.13.17")
      dialect shouldBe Scala213
    } finally {
      tempDir.delete()
    }
  }

  it should "fall back to scalaVersion when .scalafmt.conf has no dialect" in {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile
    val scalafmtFile = new File(tempDir, ".scalafmt.conf")

    try {
      writeFile(scalafmtFile, "version = \"3.0.0\"")

      val dialect = DialectConfig.getDialect(tempDir, "3.3.6")
      dialect shouldBe Scala33
    } finally {
      scalafmtFile.delete()
      tempDir.delete()
    }
  }

  it should "handle case-insensitive dialect strings from .scalafmt.conf" in {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile
    val scalafmtFile = new File(tempDir, ".scalafmt.conf")

    try {
      writeFile(scalafmtFile, "runner.dialect = SCALA213")

      val dialect = DialectConfig.getDialect(tempDir, "2.12.20")
      dialect shouldBe Scala213
    } finally {
      scalafmtFile.delete()
      tempDir.delete()
    }
  }

  // ========== getDialectForFile Tests ==========

  it should "detect dialect from scala-2.13 source directory" in {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile
    val scalaDir = new File(tempDir, "src/main/scala-2.13")
    scalaDir.mkdirs()
    val sourceFile = new File(scalaDir, "Test.scala")

    try {
      sourceFile.createNewFile()

      val dialect = DialectConfig.getDialectForFile(sourceFile, tempDir, "3.3.6")
      dialect shouldBe Scala213
    } finally {
      sourceFile.delete()
      scalaDir.delete()
      new File(tempDir, "src/main").delete()
      new File(tempDir, "src").delete()
      tempDir.delete()
    }
  }

  it should "detect dialect from scala-3.3 source directory" in {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile
    val scalaDir = new File(tempDir, "src/main/scala-3.3")
    scalaDir.mkdirs()
    val sourceFile = new File(scalaDir, "Test.scala")

    try {
      sourceFile.createNewFile()

      val dialect = DialectConfig.getDialectForFile(sourceFile, tempDir, "2.13.17")
      dialect shouldBe Scala33
    } finally {
      sourceFile.delete()
      scalaDir.delete()
      new File(tempDir, "src/main").delete()
      new File(tempDir, "src").delete()
      tempDir.delete()
    }
  }

  it should "use .scalafmt.conf for scala-2 directory without minor version" in {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile
    val scalafmtFile = new File(tempDir, ".scalafmt.conf")
    val scalaDir = new File(tempDir, "src/main/scala-2")
    scalaDir.mkdirs()
    val sourceFile = new File(scalaDir, "Test.scala")

    try {
      writeFile(scalafmtFile, "runner.dialect = scala212")
      sourceFile.createNewFile()

      val dialect = DialectConfig.getDialectForFile(sourceFile, tempDir, "3.3.6")
      dialect shouldBe Scala212
    } finally {
      sourceFile.delete()
      scalaDir.delete()
      scalafmtFile.delete()
      new File(tempDir, "src/main").delete()
      new File(tempDir, "src").delete()
      tempDir.delete()
    }
  }

  it should "fall back to scalaVersion for scala-2 directory when no .scalafmt.conf" in {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile
    val scalaDir = new File(tempDir, "src/main/scala-2")
    scalaDir.mkdirs()
    val sourceFile = new File(scalaDir, "Test.scala")

    try {
      sourceFile.createNewFile()

      val dialect = DialectConfig.getDialectForFile(sourceFile, tempDir, "2.12.20")
      dialect shouldBe Scala212
    } finally {
      sourceFile.delete()
      scalaDir.delete()
      new File(tempDir, "src/main").delete()
      new File(tempDir, "src").delete()
      tempDir.delete()
    }
  }

  it should "use cross versions for scala-2 directory when main version is Scala 3" in {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile
    val scalaDir = new File(tempDir, "src/main/scala-2")
    scalaDir.mkdirs()
    val sourceFile = new File(scalaDir, "Test.scala")

    try {
      sourceFile.createNewFile()

      val dialect = DialectConfig.getDialectForFile(
        sourceFile,
        tempDir,
        "3.3.6",
        Seq("2.12.20", "2.13.17")
      )
      dialect shouldBe Scala212
    } finally {
      sourceFile.delete()
      scalaDir.delete()
      new File(tempDir, "src/main").delete()
      new File(tempDir, "src").delete()
      tempDir.delete()
    }
  }

  it should "use specific Scala 3.x dialect from .scalafmt.conf for scala-3 directory" in {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile
    val scalafmtFile = new File(tempDir, ".scalafmt.conf")
    val scalaDir = new File(tempDir, "src/main/scala-3")
    scalaDir.mkdirs()
    val sourceFile = new File(scalaDir, "Test.scala")

    try {
      writeFile(scalafmtFile, "runner.dialect = scala32")
      sourceFile.createNewFile()

      val dialect = DialectConfig.getDialectForFile(sourceFile, tempDir, "2.13.17")
      dialect shouldBe Scala32
    } finally {
      sourceFile.delete()
      scalaDir.delete()
      scalafmtFile.delete()
      new File(tempDir, "src/main").delete()
      new File(tempDir, "src").delete()
      tempDir.delete()
    }
  }

  it should "ignore generic scala3 from .scalafmt.conf for scala-3 directory" in {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile
    val scalafmtFile = new File(tempDir, ".scalafmt.conf")
    val scalaDir = new File(tempDir, "src/main/scala-3")
    scalaDir.mkdirs()
    val sourceFile = new File(scalaDir, "Test.scala")

    try {
      writeFile(scalafmtFile, "runner.dialect = scala3")
      sourceFile.createNewFile()

      val dialect = DialectConfig.getDialectForFile(sourceFile, tempDir, "3.2.2")
      dialect shouldBe Scala38
    } finally {
      sourceFile.delete()
      scalaDir.delete()
      scalafmtFile.delete()
      new File(tempDir, "src/main").delete()
      new File(tempDir, "src").delete()
      tempDir.delete()
    }
  }

  it should "use .scalafmt.conf for generic scala directory" in {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile
    val scalafmtFile = new File(tempDir, ".scalafmt.conf")
    val scalaDir = new File(tempDir, "src/main/scala")
    scalaDir.mkdirs()
    val sourceFile = new File(scalaDir, "Test.scala")

    try {
      writeFile(scalafmtFile, "runner.dialect = scala33")
      sourceFile.createNewFile()

      val dialect = DialectConfig.getDialectForFile(sourceFile, tempDir, "2.13.17")
      dialect shouldBe Scala33
    } finally {
      sourceFile.delete()
      scalaDir.delete()
      scalafmtFile.delete()
      new File(tempDir, "src/main").delete()
      new File(tempDir, "src").delete()
      tempDir.delete()
    }
  }

  it should "fall back to scalaVersion for generic scala directory when no .scalafmt.conf" in {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile
    val scalaDir = new File(tempDir, "src/main/scala")
    scalaDir.mkdirs()
    val sourceFile = new File(scalaDir, "Test.scala")

    try {
      sourceFile.createNewFile()

      val dialect = DialectConfig.getDialectForFile(sourceFile, tempDir, "3.1.3")
      dialect shouldBe Scala31
    } finally {
      sourceFile.delete()
      scalaDir.delete()
      new File(tempDir, "src/main").delete()
      new File(tempDir, "src").delete()
      tempDir.delete()
    }
  }

  it should "use default Scala213 for scala-2 directory with no version info" in {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile
    val scalaDir = new File(tempDir, "src/main/scala-2")
    scalaDir.mkdirs()
    val sourceFile = new File(scalaDir, "Test.scala")

    try {
      sourceFile.createNewFile()

      val dialect = DialectConfig.getDialectForFile(sourceFile, tempDir, "3.3.6", Seq.empty)
      dialect shouldBe Scala213
    } finally {
      sourceFile.delete()
      scalaDir.delete()
      new File(tempDir, "src/main").delete()
      new File(tempDir, "src").delete()
      tempDir.delete()
    }
  }

  it should "use default Scala33 for scala-3 directory with no version info" in {
    val tempDir = Files.createTempDirectory("scalametrics-test" + Random.nextInt()).toFile
    val scalaDir = new File(tempDir, "src/main/scala-3")
    scalaDir.mkdirs()
    val sourceFile = new File(scalaDir, "Test.scala")

    try {
      sourceFile.createNewFile()

      val dialect = DialectConfig.getDialectForFile(sourceFile, tempDir, "2.13.17", Seq.empty)
      dialect shouldBe Scala33
    } finally {
      sourceFile.delete()
      scalaDir.delete()
      new File(tempDir, "src/main").delete()
      new File(tempDir, "src").delete()
      tempDir.delete()
    }
  }

  // ========== Helper Methods ==========

  /**
    * Creates a temporary .scalafmt.conf file with the given content.
    *
    * @param content
    *   the content to write to the file
    * @return
    *   the created temporary file
    */
  private def createTempScalafmtConf(content: String): File = {
    val tempFile = File.createTempFile("scalafmt-test-", ".conf")
    writeFile(tempFile, content)
    tempFile
  }

  /**
    * Writes content to a file.
    *
    * @param file
    *   the file to write to
    * @param content
    *   the content to write
    */
  private def writeFile(file: File, content: String): Unit = {
    val writer = new java.io.PrintWriter(file)
    try {
      writer.write(content)
    } finally {
      writer.close()
    }
  }
}
