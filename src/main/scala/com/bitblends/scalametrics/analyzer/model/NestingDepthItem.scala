/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

import scala.meta.Position

/**
  * Represents the nesting depth metric for a specific code entity.
  *
  * This case class extends the `Item` class and provides additional information about the nesting depth of a code
  * entity, such as a function, method, or class.
  *
  * The nesting depth metric indicates the maximum level of nesting within a code entity, which can be used to assess
  * its readability and maintainability.
  *
  * @param name
  *   The name of the code entity being analyzed.
  * @param kind
  *   The kind or type of the code entity (e.g., "function", "method", "class").
  * @param pos
  *   The source code position of the code entity.
  * @param depth
  *   The maximum level of nesting found within the code entity.
  */
final case class NestingDepthItem(name: String, kind: String, pos: Position, depth: Int) extends Item(name, kind, pos)
