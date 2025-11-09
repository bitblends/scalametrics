/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics.model

/**
  * Represents the result of analyzing metrics for a specific file, including file-level metrics and detailed metrics
  * for the methods and members within the file.
  *
  * @param fileMetrics
  *   Metrics associated with the file, such as its identifier, size, and lines of code.
  * @param methodMetrics
  *   A collection of metrics for each method in the file, providing detailed insights into method-level
  *   characteristics.
  * @param memberMetrics
  *   A collection of metrics for each member (non-method) in the file, containing relevant information at the member
  *   level.
  */
case class FileMetricsResult(
    fileMetrics: FileMetrics,
    methodMetrics: Vector[MethodMetrics] = Vector.empty,
    memberMetrics: Vector[MemberMetrics] = Vector.empty
)
