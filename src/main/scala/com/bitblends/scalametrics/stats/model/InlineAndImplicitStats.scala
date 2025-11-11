package com.bitblends.scalametrics.stats.model

/**
  * Represents statistical metrics related to inline and implicit definitions within a codebase.
  *
  * @param inlineMethods
  *   The number of inline methods present in the code.
  * @param inlineVals
  *   The number of inline values (vals) present in the code.
  * @param inlineVars
  *   The number of inline variables (vars) present in the code.
  * @param inlineParams
  *   The number of inline parameters defined in the code.
  * @param implicitVals
  *   The number of implicit values (vals) present in the code.
  * @param implicitVars
  *   The number of implicit variables (vars) present in the code.
  * @param implicitConversions
  *   The number of implicit conversions defined in the code.
  * @param givenInstances
  *   The number of given instances (`given`) defined in the codebase (Scala 3).
  * @param givenConversions
  *   The number of given conversions defined in the codebase (Scala 3).
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
