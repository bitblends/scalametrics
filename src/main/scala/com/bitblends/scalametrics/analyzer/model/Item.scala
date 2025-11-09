/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

import scala.meta.Position

/**
  * An abstract class that represents a generic code item to be analyzed.
  *
  * This class serves as a base class for various specific items that need to encapsulate common properties like the
  * name, kind, and position of a code entity. Subclasses of this class typically represent various kinds of metrics or
  * analysis results derived from analyzing source code.
  *
  * @param name
  *   The name of the code entity (e.g., function name, class name).
  * @param kind
  *   The kind or type of the code entity (e.g., "function", "method", "class").
  * @param pos
  *   The source code position of the code entity, which can provide details about the location of the entity in the
  *   code base.
  */
abstract class Item(name: String, kind: String, pos: Position)
