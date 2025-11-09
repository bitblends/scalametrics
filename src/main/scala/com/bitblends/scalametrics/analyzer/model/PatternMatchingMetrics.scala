/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

// ---- Metric model (per function/val body) ----------------

/**
  * Represents metrics related to the usage and structure of pattern matching in a codebase.
  *
  * This case class captures various aspects of pattern matching constructs, including the total number of matches,
  * cases, guards, and wildcards, as well as metrics related to nesting depth and distribution of cases.
  *
  * @param matches
  *   The total number of pattern matching constructs (e.g., match expressions) in the analyzed code.
  * @param cases
  *   The total number of cases across all pattern matching constructs.
  * @param guards
  *   The total number of guards used across all pattern matching cases.
  * @param wildcards
  *   The total number of wildcard patterns (e.g., `_`) across all cases in pattern matching.
  * @param maxMatchNesting
  *   The maximum nesting depth of pattern matching constructs within a single match expression.
  * @param nestedMatches
  *   The total number of occurrences where match expressions are nested within other match expressions.
  * @param perMatchCases
  *   A list representing the number of cases for each individual match expression.
  */
final case class PatternMatchingMetrics(
    matches: Int,
    cases: Int,
    guards: Int,
    wildcards: Int,
    maxMatchNesting: Int,
    nestedMatches: Int,
    perMatchCases: List[Int]
) {

  /**
    * Computes the average number of cases per match expression in the analyzed code.
    *
    * @return
    *   The average number of cases per match expression as a Double. If there are no matches, returns 0.0.
    */
  def avgCasesPerMatch: Double =
    if (matches == 0) 0.0 else cases.toDouble / matches
}
