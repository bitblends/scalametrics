/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics.model

/**
  * Represents metrics and statistics for a package within a project, capturing various function-related, inline,
  * implicit/given, and pattern-matching metrics.
  *
  * @param packageName
  *   The name of the package for which the metrics are computed.
  * @param totalFunctions
  *   The total number of functions in the package, including public and private.
  * @param publicFunctions
  *   The count of public functions in the package.
  * @param privateFunctions
  *   The count of private functions in the package.
  * @param inlineMethods
  *   The number of methods marked with the `inline` modifier.
  * @param inlineVals
  *   The number of `val` declarations marked with the `inline` modifier.
  * @param inlineVars
  *   The number of `var` declarations marked with the `inline` modifier.
  * @param inlineParams
  *   The number of parameters marked with the `inline` modifier.
  * @param implicitDefs
  *   The number of `def` declarations marked as `implicit`.
  * @param implicitVals
  *   The number of `val` declarations marked as `implicit`.
  * @param implicitVars
  *   The number of `var` declarations marked as `implicit`.
  * @param implicitConversions
  *   The number of implicit conversions defined in the code.
  * @param givenInstances
  *   The number of given instances defined within the package.
  * @param givenConversions
  *   The number of given conversions defined within the package.
  * @param pmMatches
  *   The number of pattern matches within the package.
  * @param pmCases
  *   The total number of cases defined in pattern matches.
  * @param pmGuards
  *   The total number of guards used in pattern matches.
  * @param pmWildcards
  *   The count of wildcard patterns used in pattern matches.
  * @param pmMaxNesting
  *   The maximum depth of nested pattern matches within the package.
  * @param pmNestedMatches
  *   The total count of nested pattern matches in the package.
  */
case class PackageMetrics(
    // Basic package info
    packageName: String,
    totalFunctions: Int,
    publicFunctions: Int,
    privateFunctions: Int,
    // Inline and implicit/given metrics
    inlineMethods: Int,
    inlineVals: Int,
    inlineVars: Int,
    inlineParams: Int,
    // implicit metrics
    implicitDefs: Int,
    implicitVals: Int,
    implicitVars: Int,
    implicitConversions: Int,
    // given
    givenInstances: Int,
    givenConversions: Int,
    // Pattern matching metrics
    pmMatches: Int,
    pmCases: Int,
    pmGuards: Int,
    pmWildcards: Int,
    pmMaxNesting: Int,
    pmNestedMatches: Int
)
