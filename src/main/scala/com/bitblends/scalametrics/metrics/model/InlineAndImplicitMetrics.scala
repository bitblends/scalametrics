/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics.model

/**
  * Represents metrics related to inline and implicit constructs of a symbol in a Scala codebase. This case class
  * encapsulates information about the symbol's usage of inline modifiers, inline parameters, abstractness, return type
  * characteristics, and `given` constructs in Scala 3.
  *
  * @param hasInlineModifier
  *   Indicates whether the symbol has an `inline` modifier.
  *
  * @param inlineParamCount
  *   Represents the count of parameters defined as `inline`, if applicable. The value is optional.
  *
  * @param isImplicitConversion
  *   Indicates whether the symbol represents an implicit conversion.
  *
  * @param isAbstract
  *   Indicates whether the symbol is abstract.
  *
  * @param hasExplicitReturnType
  *   Denotes whether an explicit return type is specified for the symbol.
  *
  * @param inferredReturnType
  *   Provides the inferred return type of the symbol, if not explicitly specified. The value is optional.
  *
  * @param isGivenInstance
  *   Indicates whether the symbol represents a `given` instance in Scala 3.
  *
  * @param isGivenConversion
  *   Indicates whether the symbol represents a `given` conversion in Scala 3.
  */
case class InlineAndImplicitMetrics(
    // inline
    hasInlineModifier: Boolean,
    inlineParamCount: Option[Int] = None,
    // implicit
    isImplicitConversion: Boolean,
    isImplicit: Boolean,
    // explicitness
    isAbstract: Boolean,
    hasExplicitReturnType: Boolean,
    inferredReturnType: Option[String] = None,
    // given
    isGivenInstance: Option[Boolean] = None, // Scala 3 'given'
    isGivenConversion: Option[Boolean] = None
)
