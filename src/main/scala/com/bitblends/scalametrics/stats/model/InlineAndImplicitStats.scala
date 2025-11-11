/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents statistical metrics related to explicitness, inlining, and implicit usage in a codebase. This case class
  * extends `StatsBase` and provides detailed insights into the declared methods, variables, and functional usage within
  * a Scala codebase, particularly addressing different Scala language features.
  *
  * @param explicitDefsValsVars
  *   The total number of explicitly defined defs, vals, and vars in the code.
  * @param explicitPublicDefsValsVars
  *   The total number of explicitly defined public defs, vals, and vars.
  * @param returnTypeExplicitness
  *   The percentage of methods, vals, and vars in the codebase with explicitly declared return types.
  * @param publicReturnTypeExplicitness
  *   The percentage of public methods, vals, and vars in the codebase with explicitly declared return types.
  * @param inlineMethods
  *   The number of methods defined with the `inline` modifier.
  * @param inlineVals
  *   The number of vals defined with the `inline` modifier.
  * @param inlineVars
  *   The number of vars defined with the `inline` modifier.
  * @param inlineParams
  *   The number of parameters defined with the `inline` modifier.
  * @param implicitVals
  *   The number of vals defined with the `implicit` modifier.
  * @param implicitVars
  *   The number of vars defined with the `implicit` modifier.
  * @param implicitConversions
  *   The number of declared implicit conversions present in the codebase.
  * @param givenInstances
  *   The number of given instances declared in the codebase (Scala 3).
  * @param givenConversions
  *   The number of given conversions declared in the codebase (Scala 3).
  */
case class InlineAndImplicitStats(
    // explicitness metrics
    explicitDefsValsVars: Int,
    explicitPublicDefsValsVars: Int,
    returnTypeExplicitness: Double, // Percentage for all defs/vals/vars
    publicReturnTypeExplicitness: Double, // Percentage for public defs/vals/vars only
    // Inline metrics
    inlineMethods: Int,
    inlineVals: Int,
    inlineVars: Int,
    inlineParams: Int,
    // Implicit metrics
    implicitVals: Int,
    implicitVars: Int,
    implicitConversions: Int,
    // Given metrics
    givenInstances: Int, // scala 3
    givenConversions: Int // scala 3
) extends StatsBase
