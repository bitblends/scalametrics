/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics.model

/**
  * Represents metrics related to the usage and complexity of pattern matching constructs in a codebase.
  *
  * This case class provides detailed insights into various attributes of pattern matching, including the number of
  * matches, cases, guards, and wildcards, as well as their structural properties such as nesting depth and
  * distribution.
  *
  * @param matches
  *   The total number of match expressions found.
  * @param cases
  *   The total number of case statements across all match expressions.
  * @param guards
  *   The total number of case guards used in match expressions.
  * @param wildcards
  *   The total number of wildcard patterns (_) used in match expressions.
  * @param maxNesting
  *   The maximum nesting depth of match constructs.
  * @param nestedMatches
  *   The total number of nested match expressions.
  * @param avgCasesPerMatch
  *   The average number of case statements per match expression.
  * @param matchCases
  *   A list representing the count of case statements for each match expression.
  */
case class PatternMatchingMetrics(
    matches: Int = 0,
    cases: Int = 0,
    guards: Int = 0,
    wildcards: Int = 0,
    maxNesting: Int = 0,
    nestedMatches: Int = 0,
    avgCasesPerMatch: Double = 0.0,
    matchCases: List[Int] = List.empty
)
