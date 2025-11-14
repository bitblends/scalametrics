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
    explicitDefsValsVars: Int = 0,
    explicitPublicDefsValsVars: Int = 0,
    // Inline metrics
    inlineMethods: Int = 0,
    inlineVals: Int = 0,
    inlineVars: Int = 0,
    inlineParams: Int = 0,
    // Implicit metrics
    implicitVals: Int = 0,
    implicitVars: Int = 0,
    implicitConversions: Int = 0,
    // Given metrics
    givenInstances: Int = 0, // scala 3
    givenConversions: Int = 0 // scala 3
) extends Serializer {

  /**
    * Combines the metrics of the current `InlineAndImplicitStats` instance with another `InlineAndImplicitStats`
    * instance.
    *
    * This method performs an element-wise addition of all metrics between the two `InlineAndImplicitStats` instances
    * and returns a new `InlineAndImplicitStats` instance containing the aggregated results.
    *
    * @param that
    *   Another `InlineAndImplicitStats` instance to combine with the current one.
    * @return
    *   A new `InlineAndImplicitStats` instance representing the sum of the metrics from both instances.
    */
  def +(that: InlineAndImplicitStats): InlineAndImplicitStats = {
    InlineAndImplicitStats(
      explicitDefsValsVars = this.explicitDefsValsVars + that.explicitDefsValsVars,
      explicitPublicDefsValsVars = this.explicitPublicDefsValsVars + that.explicitPublicDefsValsVars,
      inlineMethods = this.inlineMethods + that.inlineMethods,
      inlineVals = this.inlineVals + that.inlineVals,
      inlineVars = this.inlineVars + that.inlineVars,
      inlineParams = this.inlineParams + that.inlineParams,
      implicitVals = this.implicitVals + that.implicitVals,
      implicitVars = this.implicitVars + that.implicitVars,
      implicitConversions = this.implicitConversions + that.implicitConversions,
      givenInstances = this.givenInstances + that.givenInstances,
      givenConversions = this.givenConversions + that.givenConversions
    )
  }

  /**
    * Generates a formatted string representation of the current `InlineAndImplicitStats` instance.
    *
    * @return
    *   A formatted string representation of the current instance.
    */
  override def formattedString: String = {
    s"""InlineAndImplicitStats:
       |----------------------------------------------------------
       |  Explicit Defs/Vals/Vars: $explicitDefsValsVars
       |  Explicit Public Defs/Vals/Vars: $explicitPublicDefsValsVars
       |  Inline Methods: $inlineMethods
       |  Inline Vals: $inlineVals
       |  Inline Vars: $inlineVars
       |  Inline Params: $inlineParams
       |  Implicit Vals: $implicitVals
       |  Implicit Vars: $implicitVars
       |  Implicit Conversions: $implicitConversions
       |  Given Instances: $givenInstances
       |  Given Conversions: $givenConversions
       |""".stripMargin
  }
}
