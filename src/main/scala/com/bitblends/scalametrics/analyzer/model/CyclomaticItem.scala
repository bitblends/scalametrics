/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

import scala.meta.Position

/**
  * Represents a cyclomatic complexity item in the analyzed code.
  *
  * This case class is used to encapsulate information about the cyclomatic complexity of a code entity such as a
  * function, method, or class. It extends the `Item` class and provides an additional field for maintaining the
  * cyclomatic complexity metric.
  *
  * @param name
  *   The name of the code entity being analyzed.
  * @param kind
  *   The kind or type of the code entity (e.g., "function", "method", "class").
  * @param pos
  *   The source code position of the code entity.
  * @param cc
  *   The cyclomatic complexity value of the code entity, representing the number of linearly independent paths through
  *   the code.
  */
final case class CyclomaticItem(name: String, kind: String, pos: Position, cc: Int) extends Item(name, kind, pos)
