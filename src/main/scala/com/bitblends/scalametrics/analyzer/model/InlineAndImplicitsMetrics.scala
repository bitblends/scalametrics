/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

/**
  * Metrics for inline usage and implicit/given usage in Scala source code.
  *
  * Tracks both Scala 2 and Scala 3 features:
  *
  * Inline (compile-time expansion):
  *   - Scala 3: inline modifier on defs, vals, vars, and parameters
  *   - Scala 2.13: @inline annotation on methods
  *
  * Implicit/Given (context passing):
  *   - Scala 2: implicit defs, vals, vars
  *   - Scala 2: implicit conversions (implicit def with single param and non-Unit return)
  *   - Scala 3: given instances and given conversions (Conversion[A,B])
  *
  * @param inlineMethods
  *   Total count of inline methods (Scala 3 inline modifier or Scala 2 @inline)
  * @param inlineVals
  *   Total count of inline vals (Scala 3 only)
  * @param inlineVars
  *   Total count of inline vars (Scala 3 only)
  * @param inlineParams
  *   Total count of inline parameters (Scala 3 only)
  * @param implicitDefs
  *   Total count of implicit defs (Scala 2)
  * @param implicitVals
  *   Total count of implicit vals (Scala 2)
  * @param implicitVars
  *   Total count of implicit vars (Scala 2)
  * @param implicitConversions
  *   Total count of implicit conversions (Scala 2)
  * @param givenInstances
  *   Total count of given instances (Scala 3)
  * @param givenConversions
  *   Total count of given conversions (Scala 3)
  */
case class InlineAndImplicitsMetrics(
    inlineMethods: Int,
    inlineVals: Int,
    inlineVars: Int,
    inlineParams: Int,
    implicitDefs: Int,
    implicitVals: Int,
    implicitVars: Int,
    implicitConversions: Int,
    givenInstances: Int,
    givenConversions: Int
) {

  /**
    * Total inline usage across all categories
    */
  def totalInline: Int = inlineMethods + inlineVals + inlineVars + inlineParams

  /**
    * Total implicit usage (Scala 2) across all categories
    */
  def totalImplicits: Int = implicitDefs + implicitVals + implicitVars

  /**
    * Total given usage (Scala 3) across all categories
    */
  def totalGivens: Int = givenInstances

  /**
    * Total conversions (both Scala 2 implicit and Scala 3 given)
    */
  def totalConversions: Int = implicitConversions + givenConversions
}
