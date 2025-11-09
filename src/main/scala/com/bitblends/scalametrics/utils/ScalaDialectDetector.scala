/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.utils

import scala.meta.{Dialect, Parsed, Source, XtensionDialectApply}
import scala.meta._
import scala.meta.dialects.{Scala212, Scala213, Scala3}

object ScalaDialectDetector {

  /**
    * A predefined list of tuples that associates string identifiers for Scala dialects with their corresponding
    * `Dialect` implementations. This is used as the baseline for detecting and distinguishing between different
    * versions of Scala based on code analysis.
    *
    * Each entry in the list is composed of:
    *   - A string representing the name or version of the Scala dialect (e.g., "Scala3").
    *   - A corresponding `Dialect` object that represents the specific set of language features and syntax available
    *     for that version.
    */
  private val Dialects: List[(String, Dialect)] =
    List(
      ("Scala3", Scala3),
      ("Scala213", Scala213),
      ("Scala212", Scala212)
    )

  /**
    * A map representing the prior probabilities for each Scala dialect. These priors are initialized with predefined
    * values:
    *   - Scala 3: 30%
    *   - Scala 2.13: 55%
    *   - Scala 2.12: 15%
    *
    * The priors are used as baseline probabilities in the Bayesian model for dialect detection.
    */
  private val Priors: Map[String, Double] =
    Map("Scala3" -> 0.30, "Scala213" -> 0.55, "Scala212" -> 0.15)

  /**
    * A `Map` where the keys are feature names as `String`s and the values are corresponding regular expressions used to
    * detect specific Scala language features within code snippets. This mapping enables pattern-based syntax analysis
    * to identify different language constructs such as enums, given instances, opaque types, and more.
    *
    * Key-value pairs in the map:
    *   - The key represents the name of a Scala feature (e.g., "enum", "opaque").
    *   - The value is a regular expression pattern as a `String`, which is matched against the input code.
    *
    * This is used in the process of heuristic analysis for detecting dialect-specific syntax or patterns in Scala
    * source code.
    */
  private val FeatureRegex: Map[String, String] = Map(
    "enum" -> """(?m)^\s*enum\s+\w+""",
    "given" -> """(?m)^\s*given\b""",
    "using" -> """\busing\b""",
    "extension" -> """(?m)^\s*extension\s*\(""",
    "export" -> """\bexport\s+[\w\.]+\*?""",
    "opaque" -> """\bopaque\b""",
    "derives" -> """\bderives\b""",
    "inline" -> """\binline\b""",
    "LazyList" -> """\bLazyList\b""",
    "ArraySeq" -> """\bArraySeq\b""",
    "toIntOption" -> """\.toIntOption\b""",
    "FactoryImport" -> """(?m)^\s*import\s+scala\.collection\.Factory\b""",
    "BuildFrom" -> """\bBuildFrom\b""",
    "JdkConverters" -> """(?m)^\s*import\s+scala\.jdk\.""",
    "JavaConversions" -> """(?m)^\s*import\s+scala\.collection\.JavaConversions\._""",
    "JavaConverters" -> """(?m)^\s*import\s+scala\.collection\.JavaConverters\._""",
    "breakOut" -> """\bbreakOut\b""",
    "CanBuildFrom" -> """\bCanBuildFrom\b"""
  )

  /**
    * A mapping that defines the likelihood of encountering specific language features in various versions of the Scala
    * language.
    *
    * Each feature is represented as a nested map where:
    *   - The outer map key is the feature name (e.g., "enum", "given").
    *   - The inner map contains Scala version keys (e.g., "Scala3", "Scala213", "Scala212") with their associated
    *     likelihood values as probabilities ranging between 0 and 1.
    *
    * This map is utilized to assess the probability of a given Scala feature's presence, contributing to dialect
    * detection mechanisms.
    */
  private val FeatureLikelihood: Map[String, Map[String, Double]] = Map(
    "enum" -> Map("Scala3" -> 0.98, "Scala213" -> 0.02, "Scala212" -> 0.001),
    "given" -> Map("Scala3" -> 0.97, "Scala213" -> 0.02, "Scala212" -> 0.001),
    "using" -> Map("Scala3" -> 0.85, "Scala213" -> 0.10, "Scala212" -> 0.05),
    "extension" -> Map("Scala3" -> 0.95, "Scala213" -> 0.03, "Scala212" -> 0.02),
    "export" -> Map("Scala3" -> 0.90, "Scala213" -> 0.08, "Scala212" -> 0.02),
    "opaque" -> Map("Scala3" -> 0.90, "Scala213" -> 0.05, "Scala212" -> 0.01),
    "derives" -> Map("Scala3" -> 0.85, "Scala213" -> 0.10, "Scala212" -> 0.05),
    "inline" -> Map("Scala3" -> 0.70, "Scala213" -> 0.20, "Scala212" -> 0.10),
    "LazyList" -> Map("Scala3" -> 0.70, "Scala213" -> 0.95, "Scala212" -> 0.05),
    "ArraySeq" -> Map("Scala3" -> 0.60, "Scala213" -> 0.90, "Scala212" -> 0.10),
    "toIntOption" -> Map("Scala3" -> 0.60, "Scala213" -> 0.90, "Scala212" -> 0.10),
    "FactoryImport" -> Map("Scala3" -> 0.40, "Scala213" -> 0.85, "Scala212" -> 0.05),
    "BuildFrom" -> Map("Scala3" -> 0.40, "Scala213" -> 0.85, "Scala212" -> 0.05),
    "JdkConverters" -> Map("Scala3" -> 0.40, "Scala213" -> 0.90, "Scala212" -> 0.01),
    "JavaConversions" -> Map("Scala3" -> 0.00, "Scala213" -> 0.00, "Scala212" -> 0.98),
    "JavaConverters" -> Map("Scala3" -> 0.05, "Scala213" -> 0.10, "Scala212" -> 0.80),
    "breakOut" -> Map("Scala3" -> 0.00, "Scala213" -> 0.01, "Scala212" -> 0.97),
    "CanBuildFrom" -> Map("Scala3" -> 0.00, "Scala213" -> 0.05, "Scala212" -> 0.90)
  )

  /**
    * Detects the most likely Scala dialect for a given source code snippet.
    *
    * The method analyzes features, patterns, and syntax in the input code. It uses a combination of rules-based
    * heuristics and probabilistic models, such as Naive Bayes, to determine the most appropriate version of the Scala
    * programming language.
    *
    * @param code
    *   the source code snippet to analyze for dialect detection
    * @return
    *   the detected Scala dialect, represented as a `Dialect` instance
    */
  def detect(code: String): Dialect = {
    val feats = presentFeatures(code)
    if (feats.isEmpty) {
      // Fallback: original additive scheme
      val heuristic: Map[String, Int] = heuristicScores(code)
      val parsed: Map[String, Int] = parseScores(code)
      val scores = Dialects.map { case (id, _) =>
        val h = heuristic.getOrElse(id, 0)
        val p = parsed.getOrElse(id, 0)
        id -> (h + p)
      }
      val bestId = if (scores.isEmpty) "Scala213" else scores.maxBy(_._2)._1
      Dialects.find(_._1 == bestId).map(_._2).getOrElse(Scala213)
    } else {
      val bayes = bayesianScores(code)
      val bestId: String = bayes.maxBy(_._2)._1
      Dialects.find(_._1 == bestId).map(_._2).getOrElse(Scala213)
    }
  }

  /**
    * Calculates Bayesian scores for each Scala dialect based on the provided source code.
    *
    * The method evaluates prior probabilities, feature likelihoods, and parse-based probabilities for each dialect by
    * applying a weighted combination. The resulting scores represent the likelihood of the source code belonging to
    * each dialect, mapped by dialect ID.
    *
    * @param codeStr
    *   the source code snippet used for determining dialect probabilities
    * @return
    *   a map where the key is the dialect ID and the value is the corresponding Bayesian score
    */
  private def bayesianScores(codeStr: String): Map[String, Double] = {
    // remove comments and strings to avoid false positives
    val code = removeComments(codeStr)
    val feats: Set[String] = presentFeatures(code)

    val parseProbById: Map[String, Double] =
      Dialects.map { case (id, dialect) =>
        val s = tryParseWithDialect(code, dialect)
        id -> parseLikelihood(s)
      }.toMap

    val eps = 1e-6
    val wParse = 1.5 // weight for parse evidence

    Dialects.map { case (id, _) =>
      val logPrior = math.log(Priors.getOrElse(id, 1.0 / Dialects.size))
      val logFeatures = feats.toList.map { f =>
        val p = FeatureLikelihood.getOrElse(f, Map.empty).getOrElse(id, 0.33) // neutral if unknown
        math.log(math.max(p, eps))
      }.sum
      val logParse = math.log(math.max(parseProbById(id), eps))
      id -> (logPrior + logFeatures + wParse * logParse)
    }.toMap
  }

  /**
    * Converts a given likelihood score into a normalized probability value (likelihood).
    *
    * The method accepts a score, scales it, and ensures the result is constrained between a minimum value of 1e-6 and a
    * maximum value of 1.0.
    *
    * @param score
    *   an integer representing the likelihood score to be parsed (0..40), typically within a defined range
    * @return
    *   a double representing the normalized probability, constrained between 1e-6 and 1.0 -> (0,1]
    */
  private def parseLikelihood(score: Int): Double = {
    val p = 0.1 + 0.9 * (score.toDouble / 40.0) // 0.1..1.0
    math.max(1e-6, math.min(1.0, p))
  }

  /**
    * Attempts to parse the provided source code using a specific Scala dialect.
    *
    * This method evaluates the parsing success of the given code under the specified dialect. It returns an integer
    * score, where higher scores represent cleaner and more successful parses and lower scores indicate partial or
    * failed parses.
    *
    * @param code
    *   the source code snippet to be parsed
    * @param dialect
    *   the Scala dialect to use during parsing
    * @return
    *   an integer score representing the parsing result, where:
    *   - 40 indicates a successful parse
    *   - a value between 0 and 30 indicates varying levels of parsing errors, with lower values representing more
    *     severe errors
    */
  private def tryParseWithDialect(code: String, dialect: Dialect): Int = {
    dialect(code).parse[Source] match {
      case Parsed.Success(_) =>
        40 // clean parse
      case Parsed.Error(pos, _, _) =>
        // later error => better (crude)
        val line = pos.startLine
        val col = pos.startColumn
        math.min(30, line * 2 + col / 10)
    }
  }

  /**
    * Removes comments from the provided source code string.
    *
    * The method processes the input source code and eliminates both single-line (`// ...`) and multi-line (`/* ... */`)
    * comments, while preserving string literals and character literals.
    *
    * @param src
    *   the source code string from which comments should be removed
    * @return
    *   a new string representing the source code without comments
    */
  private def removeComments(src: String): String = {
    val n = src.length
    val out = new StringBuilder(n)

    sealed trait Mode
    case object Code extends Mode
    case object LineComment extends Mode
    case object BlockComment extends Mode
    case object TripleQuote extends Mode
    final case class DoubleQuote(escape: Boolean) extends Mode
    final case class CharLit(escape: Boolean) extends Mode

    @scala.annotation.tailrec
    def loop(i: Int, mode: Mode): String = {
      if (i >= n) out.result()
      else {
        val c = src.charAt(i)
        mode match {
          case Code =>
            if (c == '/' && i + 1 < n) {
              val c2 = src.charAt(i + 1)
              if (c2 == '/') loop(i + 2, LineComment)
              else if (c2 == '*') loop(i + 2, BlockComment)
              else { out.append(c); loop(i + 1, Code) }
            } else if (c == '"') {
              if (i + 2 < n && src.charAt(i + 1) == '"' && src.charAt(i + 2) == '"') {
                out.append("\"\"\"")
                loop(i + 3, TripleQuote)
              } else {
                out.append('"')
                loop(i + 1, DoubleQuote(escape = false))
              }
            } else if (c == '\'') {
              out.append('\'')
              loop(i + 1, CharLit(escape = false))
            } else {
              out.append(c)
              loop(i + 1, Code)
            }

          case LineComment =>
            if (c == '\n') { out.append('\n'); loop(i + 1, Code) }
            else if (c == '\r') {
              out.append('\r')
              if (i + 1 < n && src.charAt(i + 1) == '\n') { out.append('\n'); loop(i + 2, Code) }
              else loop(i + 1, Code)
            } else loop(i + 1, LineComment)

          case BlockComment =>
            if (c == '*' && i + 1 < n && src.charAt(i + 1) == '/') loop(i + 2, Code)
            else loop(i + 1, BlockComment)

          case TripleQuote =>
            if (i + 2 < n && c == '"' && src.charAt(i + 1) == '"' && src.charAt(i + 2) == '"') {
              out.append("\"\"\"")
              loop(i + 3, Code)
            } else {
              out.append(c)
              loop(i + 1, TripleQuote)
            }

          case DoubleQuote(escape) =>
            out.append(c)
            if (escape) loop(i + 1, DoubleQuote(escape = false))
            else if (c == '\\') loop(i + 1, DoubleQuote(escape = true))
            else if (c == '"') loop(i + 1, Code)
            else loop(i + 1, DoubleQuote(escape = false))

          case CharLit(escape) =>
            out.append(c)
            if (escape) loop(i + 1, CharLit(escape = false))
            else if (c == '\\') loop(i + 1, CharLit(escape = true))
            else if (c == '\'') loop(i + 1, Code)
            else loop(i + 1, CharLit(escape = false))
        }
      }
    }

    loop(0, Code)
  }

  /**
    * Identifies and extracts feature names from the given source code based on predefined patterns.
    *
    * The method processes the input code and uses regular expressions to match specific features. It returns the set of
    * feature names that are successfully detected within the source code.
    *
    * @param code
    *   the source code snippet to analyze for feature extraction
    * @return
    *   a set of feature names found in the input code
    */
  private def presentFeatures(code: String): Set[String] =
    FeatureRegex.collect { case (name, rx) if rx.r.findFirstIn(code).isDefined => name }.toSet

  /**
    * Computes heuristic scores for each Scala version (Scala 3, Scala 2.13, and Scala 2.12) based on patterns and
    * syntax features detected in the provided source code.
    *
    * The method analyzes the input code for specific keywords, patterns, and import statements that indicate
    * compatibility with a particular Scala version. The scores are then aggregated to determine the likelihood of
    * compatibility with each version.
    *
    * @param codeStr
    *   the source code snippet to analyze for Scala version compatibility
    * @return
    *   a map where the keys are Scala version identifiers ("Scala3", "Scala213", "Scala212") and the values are the
    *   corresponding heuristic scores indicating the likelihood of compatibility with each version
    */
  private def heuristicScores(codeStr: String): Map[String, Int] = {
    // remove comments and strings to avoid false positives
    val code = removeComments(codeStr)
    val lines = code.split('\n').toList

    def has(regex: String): Boolean =
      regex.r.findFirstIn(code).isDefined

    def hasImport(importPattern: String): Boolean =
      has(s"""(?m)^\\s*import\\s+$importPattern""")

    // --------------------------------------------------------------------------------------------
    // Scala 3 signals
    // --------------------------------------------------------------------------------------------
    var scala3Score = 0

    if (has("""(?m)^\s*enum\s+\w+""")) {
      scala3Score += 30
    }
    if (has("""(?m)^\s*given\b""")) {
      scala3Score += 25
    }
    if (has("""\busing\b""")) {
      scala3Score += 12
    }
    if (has("""(?m)^\s*extension\s*\(""")) {
      scala3Score += 25
    }
    if (has("""\bexport\s+[\w\.]+\*?""")) {
      scala3Score += 10
    }
    if (has("""(?m)^\s*end\s+""")) {
      scala3Score += 8
    }
    // Scala 3 multi-line package header
    if (
      lines.sliding(2).exists {
        case List(a, b) =>
          a.trim.startsWith("package ") && b.trim.startsWith("package ")
        case _ => false
      }
    ) {
      scala3Score += 6
    }
    if (has("""\bopaque\b""")) {
      scala3Score += 10
    }
    if (has("""\bderives\b""")) {
      scala3Score += 6
    }
    if (has("""\binline\b""")) {
      scala3Score += 5
    }

    // --------------------------------------------------------------------------------------------
    //  Scala 2.13 signals
    // --------------------------------------------------------------------------------------------
    var scala213Score = 0

    // 2.13-only or strong indicators
    // -------------------------------
    if (has("""\bLazyList\b""")) {
      scala213Score += 18
    }
    if (has("""\.toIntOption\b""")) {
      scala213Score += 10
    }
    if (has("""\.toDoubleOption\b""")) {
      scala213Score += 6
    }
    if (has("""\.toLongOption\b""")) {
      scala213Score += 6
    }
    if (has("""\bArraySeq\b""")) {
      scala213Score += 10
    }
    if (has("""\b(List|Vector|Seq|Map|Set)\.from\(""")) {
      scala213Score += 10
    }
    if (has("""\bFactory\[""")) {
      scala213Score += 8
    }
    if (has("""\bBuildFrom\b""")) {
      scala213Score += 8
    }
    if (has("""\.view\.grouped\(""")) {
      scala213Score += 5
    }
    if (has("""\.view\.sliding\(""")) {
      scala213Score += 5
    }

    // 2.13 import indicators
    // -----------------------
    if (hasImport("""scala\.jdk\.""")) {
      scala213Score += 20
    }
    if (hasImport("""scala\.collection\.Factory""")) {
      scala213Score += 15
    }
    if (hasImport("""scala\.collection\.BuildFrom""")) {
      scala213Score += 15
    }
    if (hasImport("""scala\.collection\.immutable\.LazyList""")) {
      scala213Score += 18
    }
    if (hasImport("""scala\.collection\.immutable\.ArraySeq""")) {
      scala213Score += 12
    }
    if (hasImport("""scala\.util\.chaining\._""")) {
      scala213Score += 10
    }
    if (hasImport("""scala\.collection\.IterableFactory""")) {
      scala213Score += 10
    }

    // --------------------------------------------------------------------------------------------
    // common 2.x style nudges
    // --------------------------------------------------------------------------------------------
    if (has("""\bimplicit\s+def\b""")) {
      scala213Score += 3
    }
    if (has("""\bimplicit\s+class\b""")) {
      scala213Score += 4
    }
    if (has("""extends\s+App\b""")) {
      scala213Score += 2
    }
    // prefer 2.13 over 2.12 as a base
    scala213Score += 5

    // --------------------------------------------------------------------------------------------
    // Scala 2.12 signals
    // --------------------------------------------------------------------------------------------
    var scala212Score = 0

    // 2.12 import indicators (definitive markers)
    // -------------------------------------------
    if (hasImport("""scala\.collection\.JavaConversions\._""")) {
      scala212Score += 25
    }
    if (hasImport("""scala\.collection\.JavaConverters\._""")) {
      scala212Score += 15
    }
    if (hasImport("""scala\.collection\.breakOut""")) {
      scala212Score += 20
    }
    if (hasImport("""scala\.collection\.generic\.CanBuildFrom""")) {
      scala212Score += 20
    }
    if (hasImport("""scala\.collection\.parallel\._""")) {
      scala212Score += 10
    }

    if (has("""\.to\[""")) {
      scala212Score += 10
    }
    if (has("""breakOut""")) {
      scala212Score += 15
    }
    if (has("""\.view\.force\b""")) {
      scala212Score += 10
    }
    if (has("""\bCanBuildFrom\b""")) {
      scala212Score += 12
    }

    // small floor so 2.12 isn't zero
    scala212Score += 1

    Map(
      "Scala3" -> scala3Score,
      "Scala213" -> scala213Score,
      "Scala212" -> scala212Score
    )
  }

  /**
    * Parses the provided source code and evaluates parsing scores for each predefined Scala dialect.
    *
    * The method iterates over the available Scala dialects and attempts to parse the given source code with each. It
    * returns a map of dialect IDs to their respective parsing success scores, as determined by the
    * `tryParseWithDialect` method.
    *
    * @param code
    *   the source code snippet to be analyzed for parsing success across different Scala dialects
    * @return
    *   a map where the key is the dialect ID (as a string) and the value is an integer score representing the parsing
    *   success for the corresponding dialect
    */
  private def parseScores(code: String): Map[String, Int] =
    Dialects.map { case (id, dialect) =>
      id -> tryParseWithDialect(code, dialect)
    }.toMap
}
