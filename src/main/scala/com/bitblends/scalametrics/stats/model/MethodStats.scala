/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

import com.bitblends.scalametrics.metrics.model._

/**
  * A case class that represents various statistics and metrics gathered for a method within a Scala codebase.
  *
  * This class provides insights into a method's structural and complexity characteristics, including:
  *
  *   - Metadata about the method's declaration.
  *   - Cyclomatic complexity of the method, which reflects the number of independent execution paths.
  *   - Documentation availability, indicating whether the method has associated Scaladoc.
  *   - Nesting depth within the method, representing the depth of block and control structure nesting.
  *   - Parameter-level metrics that detail the use and characteristics of method parameters.
  *   - Inline and implicit-related information, reflecting modifiers and inferred traits of the method.
  *   - Pattern matching-related metrics, capturing the usage and complexity of pattern match constructs.
  *   - Branch density metrics to assess control flow density complexity.
  *
  * @param metadata
  *   Metadata associated with the method's declaration, providing contextual information such as identity, structure,
  *   access modifiers, and declaration properties.
  *
  * @param cComplexity
  *   Cyclomatic complexity of the method, representing the number of independent execution paths.
  *
  * @param hasScaladoc
  *   Indicates whether the method is documented with associated Scaladoc.
  *
  * @param nestingDepth
  *   The maximum depth of nested blocks or control structures within the method.
  *
  * @param paramStats
  *   Detailed statistics related to the method's parameters, such as total parameter count, number of implicit
  *   parameters, variadic parameters, and others.
  *
  * @param inlineAndImplicitStats
  *   Inline and implicit-related metrics summarizing characteristics like inline modifiers, implicit conversions, and
  *   given instances or conversions (Scala 3).
  *
  * @param patternMatchingMetrics
  *   Pattern match-related metrics, capturing the structure, nesting, guard clauses, and wildcard usage within the
  *   method.
  *
  * @param branchDensityMetrics
  *   Metrics assessing branch density within the method, providing insight into the control flow and branching within
  *   its implementation.
  */
case class MethodStats(
    metadata: Metadata,
    cComplexity: Int,
    hasScaladoc: Boolean,
    nestingDepth: Int,
    paramStats: ParameterMetrics,
    inlineAndImplicitStats: InlineAndImplicitMetrics,
    patternMatchingMetrics: PatternMatchingMetrics,
    branchDensityMetrics: BranchDensityMetrics
) extends StatsBase
    with MemberBase
