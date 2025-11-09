package com.bitblends.scalametrics.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
  * Unit tests for the Id object.
  *
  * These tests verify that the identifier generator correctly:
  *   - Generates deterministic identifiers
  *   - Produces consistent output for same inputs
  *   - Generates different identifiers for different inputs
  *   - Handles edge cases (empty strings, special characters, etc.)
  *   - Produces 16-character hexadecimal strings
  *   - Uses SHA-1 hash truncated to 8 bytes
  */
class IdSpec extends AnyFlatSpec with Matchers {

  behavior of "Id"

  // ========== Basic Functionality ==========

  it should "generate a 16-character hexadecimal string" in {
    val id = Id.of("test")

    id should have length 16
    id should fullyMatch regex "[0-9a-f]{16}"
  }

  it should "generate deterministic identifiers for same input" in {
    val id1 = Id.of("test")
    val id2 = Id.of("test")

    id1 shouldBe id2
  }

  it should "generate same identifier across multiple calls" in {
    val ids = (1 to 100).map(_ => Id.of("consistent"))

    ids.distinct should have size 1
  }

  it should "generate different identifiers for different inputs" in {
    val id1 = Id.of("foo")
    val id2 = Id.of("bar")

    id1 should not be id2
  }

  // ========== Multiple Parts ==========

  it should "handle multiple parts" in {
    val id = Id.of("part1", "part2", "part3")

    id should have length 16
    id should fullyMatch regex "[0-9a-f]{16}"
  }

  it should "generate deterministic identifiers for same multiple parts" in {
    val id1 = Id.of("foo", "bar", "baz")
    val id2 = Id.of("foo", "bar", "baz")

    id1 shouldBe id2
  }

  it should "generate different identifiers for different part orders" in {
    val id1 = Id.of("foo", "bar")
    val id2 = Id.of("bar", "foo")

    id1 should not be id2
  }

  it should "generate same identifiers when parts concatenate to same string" in {
    val id1 = Id.of("foobar")
    val id2 = Id.of("foo", "bar")

    // Parts are simply concatenated, so "foobar" == "foo" + "bar"
    id1 shouldBe id2
  }

  it should "handle many parts" in {
    val parts = (1 to 50).map(_.toString)
    val id1 = Id.of(parts: _*)
    val id2 = Id.of(parts: _*)

    id1 shouldBe id2
    id1 should have length 16
  }

  // ========== Edge Cases ==========

  it should "handle empty string" in {
    val id = Id.of("")

    id should have length 16
    id should fullyMatch regex "[0-9a-f]{16}"
  }

  it should "generate different identifiers for empty vs non-empty" in {
    val id1 = Id.of("")
    val id2 = Id.of("a")

    id1 should not be id2
  }

  it should "handle multiple empty strings" in {
    val id1 = Id.of("", "")
    val id2 = Id.of("")

    // Multiple empty strings concatenate to same as single empty string
    id1 shouldBe id2
  }

  it should "handle no parts (varargs with zero arguments)" in {
    val id = Id.of()

    id should have length 16
    id should fullyMatch regex "[0-9a-f]{16}"
  }

  it should "generate same identifier for multiple no-part calls" in {
    val id1 = Id.of()
    val id2 = Id.of()

    id1 shouldBe id2
  }

  // ========== Special Characters ==========

  it should "handle strings with spaces" in {
    val id = Id.of("hello world")

    id should have length 16
    id should fullyMatch regex "[0-9a-f]{16}"
  }

  it should "generate different identifiers with and without spaces" in {
    val id1 = Id.of("helloworld")
    val id2 = Id.of("hello world")

    id1 should not be id2
  }

  it should "handle strings with special characters" in {
    val id = Id.of("foo!@#$%^&*()")

    id should have length 16
    id should fullyMatch regex "[0-9a-f]{16}"
  }

  it should "handle strings with unicode characters" in {
    val id = Id.of("héllo", "wörld", "日本語")

    id should have length 16
    id should fullyMatch regex "[0-9a-f]{16}"
  }

  it should "generate deterministic identifiers for unicode" in {
    val id1 = Id.of("日本語")
    val id2 = Id.of("日本語")

    id1 shouldBe id2
  }

  it should "handle strings with newlines" in {
    val id = Id.of("line1\nline2\nline3")

    id should have length 16
    id should fullyMatch regex "[0-9a-f]{16}"
  }

  it should "handle strings with tabs" in {
    val id = Id.of("col1\tcol2\tcol3")

    id should have length 16
    id should fullyMatch regex "[0-9a-f]{16}"
  }

  // ========== Long Strings ==========

  it should "handle very long strings" in {
    val longString = "a" * 10000
    val id = Id.of(longString)

    id should have length 16
    id should fullyMatch regex "[0-9a-f]{16}"
  }

  it should "generate deterministic identifiers for long strings" in {
    val longString = "x" * 5000
    val id1 = Id.of(longString)
    val id2 = Id.of(longString)

    id1 shouldBe id2
  }

  it should "generate different identifiers for different long strings" in {
    val longString1 = "a" * 10000
    val longString2 = "b" * 10000

    val id1 = Id.of(longString1)
    val id2 = Id.of(longString2)

    id1 should not be id2
  }

  // ========== Realistic Use Cases ==========

  it should "generate identifier for file path" in {
    val id = Id.of("/path/to/file.scala")

    id should have length 16
    id should fullyMatch regex "[0-9a-f]{16}"
  }

  it should "generate identifier for package and class name" in {
    val id = Id.of("com.example.package", "MyClass")

    id should have length 16
    id should fullyMatch regex "[0-9a-f]{16}"
  }

  it should "generate identifier for method signature" in {
    val id = Id.of("com.example.MyClass", "myMethod", "Int", "String")

    id should have length 16
    id should fullyMatch regex "[0-9a-f]{16}"
  }

  it should "generate different identifiers for different methods in same class" in {
    val id1 = Id.of("com.example.MyClass", "method1")
    val id2 = Id.of("com.example.MyClass", "method2")

    id1 should not be id2
  }

  it should "generate different identifiers for same method in different classes" in {
    val id1 = Id.of("com.example.Class1", "method")
    val id2 = Id.of("com.example.Class2", "method")

    id1 should not be id2
  }

  // ========== Hash Properties ==========

  it should "use lowercase hexadecimal" in {
    val id = Id.of("test")

    id should fullyMatch regex "[0-9a-f]{16}"
    id.exists(_.isUpper) shouldBe false
  }

  it should "not produce identifiers starting with specific patterns (no bias)" in {
    // Generate many identifiers and check distribution is reasonable
    val ids = (1 to 1000).map(i => Id.of(s"test$i"))

    // Check that we have good variety in first characters
    val firstChars = ids.map(_.charAt(0)).distinct
    firstChars.size should be > 10 // Should have variety in first hex digit
  }

  it should "produce different identifiers for similar inputs" in {
    val id1 = Id.of("test1")
    val id2 = Id.of("test2")
    val id3 = Id.of("test3")

    id1 should not be id2
    id2 should not be id3
    id1 should not be id3

    // Identifiers should be sufficiently different (no common prefix of more than a few chars)
    val commonPrefix = id1.zip(id2).takeWhile { case (c1, c2) => c1 == c2 }.length
    commonPrefix should be < 8 // Less than half the identifier should match
  }

  // ========== Collision Resistance ==========

  it should "generate unique identifiers for many different inputs" in {
    val ids = (1 to 10000).map(i => Id.of(s"input$i"))

    // With 8 bytes (64 bits), collisions should be extremely rare
    val uniqueIds = ids.distinct
    uniqueIds should have size 10000
  }

  it should "generate unique identifiers for combinations of parts" in {
    val ids = for {
      i <- 1 to 100
      j <- 1 to 100
    } yield Id.of(s"part$i", s"part$j")

    val uniqueIds = ids.distinct
    uniqueIds should have size 10000
  }

  // ========== Known Values (Regression Tests) ==========

  it should "generate expected identifier for empty input (regression)" in {
    val id = Id.of()

    // This is the SHA-1 hash of empty string, truncated to 8 bytes
    id shouldBe "da39a3ee5e6b4b0d"
  }

  it should "generate expected identifier for 'test' (regression)" in {
    val id = Id.of("test")

    // This is the SHA-1 hash of "test", truncated to 8 bytes
    id shouldBe "a94a8fe5ccb19ba6"
  }

  it should "generate expected identifier for multiple parts (regression)" in {
    val id = Id.of("foo", "bar")

    // This is the SHA-1 hash of "foobar", truncated to 8 bytes
    id shouldBe "8843d7f92416211d"
  }
}
