package com.bitblends.scalametrics.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Files

/**
  * Unit tests for the FileId object.
  *
  * These tests verify that the file identifier generator correctly:
  *   - Generates deterministic file identifiers
  *   - Produces 16-character hexadecimal strings
  *   - Normalizes file paths correctly
  *   - Handles relative and absolute paths
  *   - Handles path separators (/ and \)
  *   - Handles repository root paths
  *   - Handles edge cases (different roots, non-existent files, etc.)
  */
class FileIdSpec extends AnyFlatSpec with Matchers {

  behavior of "FileId"

  // ========== Basic Functionality ==========

  it should "generate a 16-character hexadecimal string" in {
    val tempFile = Files.createTempFile("test", ".scala").toFile
    try {
      val id = FileId.idFor(tempFile, Some("project1"))

      id should have length 16
      id should fullyMatch regex "[0-9a-f]{16}"
    } finally {
      tempFile.delete()
    }
  }

  it should "generate deterministic identifiers for same inputs" in {
    val tempFile = Files.createTempFile("test", ".scala").toFile
    try {
      val id1 = FileId.idFor(tempFile, Some("project1"))
      val id2 = FileId.idFor(tempFile, Some("project1"))

      id1 shouldBe id2
    } finally {
      tempFile.delete()
    }
  }

  it should "generate different identifiers for different project IDs" in {
    val tempFile = Files.createTempFile("test", ".scala").toFile
    try {
      val id1 = FileId.idFor(tempFile, Some("project1"))
      val id2 = FileId.idFor(tempFile, Some("project2"))

      id1 should not be id2
    } finally {
      tempFile.delete()
    }
  }

  it should "generate different identifiers for different files" in {
    val tempFile1 = Files.createTempFile("test1", ".scala").toFile
    val tempFile2 = Files.createTempFile("test2", ".scala").toFile
    try {
      val id1 = FileId.idFor(tempFile1, Some("project1"))
      val id2 = FileId.idFor(tempFile2, Some("project1"))

      id1 should not be id2
    } finally {
      tempFile1.delete()
      tempFile2.delete()
    }
  }

  // ========== Path Normalization ==========

  it should "normalize absolute path without repo root" in {
    val tempFile = Files.createTempFile("test", ".scala").toFile
    try {
      val normalized = FileId.normalizedPath(tempFile)

      // Should be absolute path with forward slashes
      normalized should include("/")
      normalized should not include "\\"
      normalized should endWith(".scala")
    } finally {
      tempFile.delete()
    }
  }

  it should "use forward slashes in normalized paths" in {
    val tempFile = Files.createTempFile("test", ".scala").toFile
    try {
      val normalized = FileId.normalizedPath(tempFile)

      normalized should not include "\\"
    } finally {
      tempFile.delete()
    }
  }

  it should "normalize path relative to repo root" in {
    val tempDir = Files.createTempDirectory("repo").toFile
    val subDir = new File(tempDir, "src/main/scala")
    subDir.mkdirs()
    val tempFile = new File(subDir, "Test.scala")
    tempFile.createNewFile()

    try {
      val repoRoot = tempDir.toPath
      val normalized = FileId.normalizedPath(tempFile, Some(repoRoot))

      normalized shouldBe "src/main/scala/Test.scala"
    } finally {
      tempFile.delete()
      deleteRecursively(tempDir)
    }
  }

  it should "produce consistent normalized paths for same file" in {
    val tempFile = Files.createTempFile("test", ".scala").toFile
    try {
      val normalized1 = FileId.normalizedPath(tempFile)
      val normalized2 = FileId.normalizedPath(tempFile)

      normalized1 shouldBe normalized2
    } finally {
      tempFile.delete()
    }
  }

  it should "handle nested directory structures in normalized paths" in {
    val tempDir = Files.createTempDirectory("repo").toFile
    val nestedDir = new File(tempDir, "a/b/c/d/e")
    nestedDir.mkdirs()
    val tempFile = new File(nestedDir, "Test.scala")
    tempFile.createNewFile()

    try {
      val repoRoot = tempDir.toPath
      val normalized = FileId.normalizedPath(tempFile, Some(repoRoot))

      normalized shouldBe "a/b/c/d/e/Test.scala"
    } finally {
      tempFile.delete()
      deleteRecursively(tempDir)
    }
  }

  it should "handle path normalization with .. (parent directory references)" in {
    val tempDir = Files.createTempDirectory("repo").toFile
    val subDir = new File(tempDir, "src/main/scala")
    subDir.mkdirs()
    val testDir = new File(tempDir, "test")
    testDir.mkdirs()

    try {
      // Create a file reference with .. in path
      val fileWithParentRef = new File(subDir, "../../../test")
      val normalized = FileId.normalizedPath(fileWithParentRef, Some(tempDir.toPath))

      // After normalization, .. should be resolved
      normalized should not include ".."
    } finally {
      deleteRecursively(tempDir)
    }
  }

  // ========== Repository Root Handling ==========

  it should "generate same ID for file when using repo root" in {
    val tempDir = Files.createTempDirectory("repo").toFile
    val subDir = new File(tempDir, "src")
    subDir.mkdirs()
    val tempFile = new File(subDir, "Test.scala")
    tempFile.createNewFile()

    try {
      val repoRoot = tempDir.toPath
      val id1 = FileId.idFor(tempFile, Some("project1"), Some(repoRoot))
      val id2 = FileId.idFor(tempFile, Some("project1"), Some(repoRoot))

      id1 shouldBe id2
    } finally {
      tempFile.delete()
      deleteRecursively(tempDir)
    }
  }

  it should "generate different IDs when using different repo roots" in {
    val tempDir1 = Files.createTempDirectory("repo1").toFile
    val tempDir2 = Files.createTempDirectory("repo2").toFile
    val subDir1 = new File(tempDir1, "src")
    val subDir2 = new File(tempDir2, "src")
    subDir1.mkdirs()
    subDir2.mkdirs()
    val tempFile1 = new File(subDir1, "Test.scala")
    val tempFile2 = new File(subDir2, "Test.scala")
    tempFile1.createNewFile()
    tempFile2.createNewFile()

    try {
      val id1 = FileId.idFor(tempFile1, Some("project1"), Some(tempDir1.toPath))
      val id2 = FileId.idFor(tempFile2, Some("project1"), Some(tempDir2.toPath))

      // Same relative path, so IDs should be the same
      id1 shouldBe id2
    } finally {
      tempFile1.delete()
      tempFile2.delete()
      deleteRecursively(tempDir1)
      deleteRecursively(tempDir2)
    }
  }

  it should "handle file outside repo root (different roots)" in {
    val tempDir1 = Files.createTempDirectory("repo1").toFile
    val tempDir2 = Files.createTempDirectory("repo2").toFile
    val tempFile = new File(tempDir2, "Test.scala")
    tempFile.createNewFile()

    try {
      // Try to normalize a file from tempDir2 using tempDir1 as root
      val normalized = FileId.normalizedPath(tempFile, Some(tempDir1.toPath))

      // Should fall back to absolute path since roots are different
      normalized should include("/")
      normalized should endWith("Test.scala")
    } finally {
      tempFile.delete()
      tempDir1.delete()
      tempDir2.delete()
    }
  }

  it should "generate ID with None repo root" in {
    val tempFile = Files.createTempFile("test", ".scala").toFile
    try {
      val id1 = FileId.idFor(tempFile, Some("project1"), None)
      val id2 = FileId.idFor(tempFile, Some("project1"))

      // Both should use absolute path
      id1 shouldBe id2
    } finally {
      tempFile.delete()
    }
  }

  // ========== Edge Cases ==========

  it should "handle file with special characters in name" in {
    val tempDir = Files.createTempDirectory("repo").toFile
    val specialFile = new File(tempDir, "Test-File_123.scala")
    specialFile.createNewFile()

    try {
      val id = FileId.idFor(specialFile, Some("project1"), Some(tempDir.toPath))

      id should have length 16
      id should fullyMatch regex "[0-9a-f]{16}"
    } finally {
      specialFile.delete()
      tempDir.delete()
    }
  }

  it should "handle file with spaces in name" in {
    val tempDir = Files.createTempDirectory("repo").toFile
    val fileWithSpaces = new File(tempDir, "Test File.scala")
    fileWithSpaces.createNewFile()

    try {
      val id = FileId.idFor(fileWithSpaces, Some("project1"), Some(tempDir.toPath))

      id should have length 16
      id should fullyMatch regex "[0-9a-f]{16}"
    } finally {
      fileWithSpaces.delete()
      tempDir.delete()
    }
  }

  it should "handle deeply nested paths" in {
    val tempDir = Files.createTempDirectory("repo").toFile
    val deepPath = "a/b/c/d/e/f/g/h/i/j"
    val deepDir = new File(tempDir, deepPath)
    deepDir.mkdirs()
    val deepFile = new File(deepDir, "Deep.scala")
    deepFile.createNewFile()

    try {
      val id = FileId.idFor(deepFile, Some("project1"), Some(tempDir.toPath))

      id should have length 16
      id should fullyMatch regex "[0-9a-f]{16}"
    } finally {
      deepFile.delete()
      deleteRecursively(tempDir)
    }
  }

  it should "handle non-existent file" in {
    val tempDir = Files.createTempDirectory("repo").toFile
    val nonExistentFile = new File(tempDir, "DoesNotExist.scala")

    try {
      // Should still generate an ID even if file doesn't exist
      val id = FileId.idFor(nonExistentFile, Some("project1"), Some(tempDir.toPath))

      id should have length 16
      id should fullyMatch regex "[0-9a-f]{16}"
    } finally {
      tempDir.delete()
    }
  }

  it should "normalize path for non-existent file" in {
    val tempDir = Files.createTempDirectory("repo").toFile
    val nonExistentFile = new File(tempDir, "src/main/scala/DoesNotExist.scala")

    try {
      val normalized = FileId.normalizedPath(nonExistentFile, Some(tempDir.toPath))

      // Should still normalize even if file doesn't exist
      normalized shouldBe "src/main/scala/DoesNotExist.scala"
    } finally {
      tempDir.delete()
    }
  }

  // ========== Realistic Use Cases ==========

  it should "generate consistent IDs for Scala source files" in {
    val tempDir = Files.createTempDirectory("project").toFile
    val srcDir = new File(tempDir, "src/main/scala/com/example")
    srcDir.mkdirs()
    val scalaFile = new File(srcDir, "Main.scala")
    scalaFile.createNewFile()

    try {
      val id1 = FileId.idFor(scalaFile, Some("myproject"), Some(tempDir.toPath))
      val id2 = FileId.idFor(scalaFile, Some("myproject"), Some(tempDir.toPath))

      id1 shouldBe id2
    } finally {
      scalaFile.delete()
      deleteRecursively(tempDir)
    }
  }

  it should "generate different IDs for files in same directory" in {
    val tempDir = Files.createTempDirectory("project").toFile
    val srcDir = new File(tempDir, "src/main/scala")
    srcDir.mkdirs()
    val file1 = new File(srcDir, "File1.scala")
    val file2 = new File(srcDir, "File2.scala")
    file1.createNewFile()
    file2.createNewFile()

    try {
      val id1 = FileId.idFor(file1, Some("myproject1"), Some(tempDir.toPath))
      val id2 = FileId.idFor(file2, Some("myproject1"), Some(tempDir.toPath))

      id1 should not be id2
    } finally {
      file1.delete()
      file2.delete()
      deleteRecursively(tempDir)
    }
  }

  it should "generate IDs that include project ID context" in {
    val tempDir = Files.createTempDirectory("project").toFile
    val srcDir = new File(tempDir, "src")
    srcDir.mkdirs()
    val file = new File(srcDir, "Test.scala")
    file.createNewFile()

    try {
      val idProjectA = FileId.idFor(file, Some("projectA"), Some(tempDir.toPath))
      val idProjectB = FileId.idFor(file, Some("projectB"), Some(tempDir.toPath))

      // Same file, different project IDs should produce different identifiers
      idProjectA should not be idProjectB
    } finally {
      file.delete()
      deleteRecursively(tempDir)
    }
  }

  // ========== Hex Conversion (Internal) ==========

  it should "produce lowercase hexadecimal in IDs" in {
    val tempFile = Files.createTempFile("test", ".scala").toFile
    try {
      val id = FileId.idFor(tempFile, Some("project1"))

      id should fullyMatch regex "[0-9a-f]{16}"
      id.exists(_.isUpper) shouldBe false
    } finally {
      tempFile.delete()
    }
  }

  // ========== Regression Tests ==========

  it should "generate expected ID for known input (regression)" in {
    // Create a file with a known path structure
    val tempDir = Files.createTempDirectory("scalametrics-test").toFile
    val srcDir = new File(tempDir, "src/main/scala")
    srcDir.mkdirs()
    val testFile = new File(srcDir, "Test.scala")
    testFile.createNewFile()

    try {
      val id = FileId.idFor(testFile, Some("testproject"), Some(tempDir.toPath))

      // The ID should be deterministic for this specific combination
      id should have length 16
      id should fullyMatch regex "[0-9a-f]{16}"

      // Generate again to ensure consistency
      val id2 = FileId.idFor(testFile, Some("testproject"), Some(tempDir.toPath))
      id shouldBe id2
    } finally {
      testFile.delete()
      deleteRecursively(tempDir)
    }
  }

  it should "handle Windows-style path separators" in {
    val tempDir = Files.createTempDirectory("repo").toFile
    val subDir = new File(tempDir, "src")
    subDir.mkdirs()
    val testFile = new File(subDir, "Test.scala")
    testFile.createNewFile()

    try {
      val normalized = FileId.normalizedPath(testFile, Some(tempDir.toPath))

      // Should use forward slashes, not backslashes
      normalized should not include "\\"
      normalized should include("/")
      normalized shouldBe "src/Test.scala"
    } finally {
      testFile.delete()
      deleteRecursively(tempDir)
    }
  }

  // ========== Collision Resistance ==========

  it should "generate unique IDs for many different files" in {
    val tempDir = Files.createTempDirectory("project").toFile
    val srcDir = new File(tempDir, "src")
    srcDir.mkdirs()

    try {
      val ids = (1 to 100).map { i =>
        val file = new File(srcDir, s"File$i.scala")
        file.createNewFile()
        val id = FileId.idFor(file, Some("project1"), Some(tempDir.toPath))
        file.delete()
        id
      }

      val uniqueIds = ids.distinct
      uniqueIds should have size 100
    } finally {
      deleteRecursively(tempDir)
    }
  }

  // ========== Helper Methods ==========

  /**
    * Recursively deletes a directory and all its contents.
    *
    * @param file
    *   the file or directory to delete
    */
  private def deleteRecursively(file: File): Unit = {
    if (file.isDirectory) {
      file.listFiles().foreach(deleteRecursively)
    }
    file.delete()
  }
}
