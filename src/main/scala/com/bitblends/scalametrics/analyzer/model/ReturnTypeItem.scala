/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

import scala.meta.Position

/**
  * Represents an element in the source code with detailed information about its return type and visibility.
  *
  * "def" | "decl-def" | "val" | "var"
  *
  * This class extends the abstract `Item` class and provides additional details specific to entities that have
  * associated return types, such as functions or methods. Information includes whether the return type was explicitly
  * declared, inferred, or publicly accessible.
  *
  * @param name
  *   The name of the entity (e.g., function or method name).
  * @param kind
  *   The type of entity (e.g., "function", "method").
  * @param pos
  *   The location in the source code where the entity is defined.
  * @param isPublic
  *   Indicates whether the entity is publicly accessible.
  * @param hasExplicitReturnType
  *   Indicates whether the return type of the entity is explicitly declared in the source code.
  * @param inferredReturnType
  *   An optional field providing the inferred return type of the entity, if applicable and available.
  */
final case class ReturnTypeItem(
    name: String,
    kind: String,
    pos: Position,
    isPublic: Boolean,
    hasExplicitReturnType: Boolean,
    inferredReturnType: Option[String] = None
) extends Item(name, kind, pos)
