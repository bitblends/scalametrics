/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

import scala.meta.Position

/**
  * Represents an analyzed item in the source code associated with pattern matching constructs.
  *
  * This case class extends the `Item` class and incorporates detailed metrics about pattern matching constructs found
  * in a specific code entity such as a function or value. The metrics include insights on matches, cases, guards,
  * wildcards, and nesting levels for pattern matching analysis.
  *
  * @param name
  *   The name of the code entity (e.g., method or value) that contains the pattern matching logic.
  * @param kind
  *   The type of code entity, such as "def" or "val", indicating whether it is a method or a value.
  * @param pos
  *   The position in the source code where the code entity resides.
  * @param metrics
  *   Metrics specific to the pattern matching constructs within the code entity.
  */
final case class PatternMatchingItem(
    name: String, // def name or val name
    kind: String, // "def" | "val"
    pos: Position,
    metrics: PatternMatchingMetrics
) extends Item(name, kind, pos)
