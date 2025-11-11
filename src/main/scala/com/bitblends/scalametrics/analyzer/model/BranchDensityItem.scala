/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

import com.bitblends.scalametrics.metrics.model.BranchDensityMetrics

import scala.meta.Position

/**
  * Represents a branch density item in the analyzed code.
  *
  * This case class extends the `Item` class and provides additional information about branch density metrics for a code
  * entity (such as a function, method, or class).
  *
  * @param name
  *   The name of the code entity being analyzed.
  * @param kind
  *   The kind or type of the code entity (e.g., "class", "object", "method").
  * @param pos
  *   The source code position of the code entity.
  * @param m
  *   Metrics representing the branch density details for the code entity.
  */
final case class BranchDensityItem(name: String, kind: String, pos: Position, m: BranchDensityMetrics)
    extends Item(name, kind, pos)
