/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

import scala.meta.Position

/**
  * Represents a specific parameter item in the code, including its name, kind, position, and additional arity-related
  * metadata.
  *
  * This case class extends the `Item` abstraction, providing functionality specific to parameters within callable
  * entities like functions or methods.
  *
  * @param name
  *   The name of the parameter.
  * @param kind
  *   The kind or type of the parameter (e.g., "parameter", "implicit parameter").
  * @param pos
  *   The source code position of the parameter, which indicates its location within the codebase.
  * @param arity
  *   Detailed arity-level metadata for the parameter, representing metrics such as the number of parameters, parameter
  *   lists, implicit parameters, and other characteristics.
  */
final case class ParameterItem(name: String, kind: String, pos: Position, arity: Arity) extends Item(name, kind, pos)
