/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics.model

/**
  * Represents a collection of metrics associated with a single method in the source code.
  *
  * This case class encapsulates various analyses of a method's complexity, structure, and usage. It combines metadata,
  * structural counts, parameters, and other metrics for assessing the quality, maintainability, and design intricacies
  * of the method.
  *
  * @param metadata
  *   Metadata associated with the method. Contains information such as the method's name, access modifier, signature,
  *   lines of code, and other descriptive elements.
  * @param cComplexity
  *   Cyclomatic complexity of the method, which indicates the number of linearly independent paths through the method's
  *   control flow.
  * @param nestingDepth
  *   The maximum depth of nested control flow constructs within the method.
  * @param hasScaladoc
  *   Indicates whether the method is documented with Scaladoc.
  * @param parameterMetrics
  *   Metrics related to the parameters of the method, such as counts of parameter lists, implicit parameters, and
  *   parameters with special characteristics.
  * @param inlineAndImplicitMetrics
  *   Metrics related to the method's usage of inline and implicit features, such as whether the method is marked as
  *   `inline`, whether it is abstract, and whether it has explicit or inferred return types.
  * @param pmMetrics
  *   Metrics related to the pattern matching constructs within the method, including counts of matches, case clauses,
  *   guards, and nesting levels.
  * @param bdMetrics
  *   Metrics related to branch density, including counts of branches, boolean operations, and normalized densities of
  *   these elements in comparison to the total code.
  */
case class MethodMetrics(
    metadata: Metadata,
    cComplexity: Int,
    nestingDepth: Int,
    hasScaladoc: Boolean,
    parameterMetrics: ParameterMetrics,
    inlineAndImplicitMetrics: InlineAndImplicitMetrics,
    pmMetrics: PatternMatchingMetrics,
    bdMetrics: BranchDensityMetrics
) extends Symbol
