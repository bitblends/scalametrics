/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

/**
  * Represents a contextual scope model used for analyzing code structures, specifically for tracking ownership and
  * block/lambda nesting. This class provides utilities for managing a list of `Owner` entities and retrieving
  * information related to the scope hierarchy.
  *
  * @param owners
  *   The list of `Owner` objects representing the hierarchy of ownership within the current scope. An empty list
  *   indicates no associated owners.
  * @param blockId
  *   A unique identifier for a specific block in the analysis. This is often used to differentiate between different
  *   scopes or segments of code.
  * @param lambdaId
  *   A unique identifier for a lambda or anonymous function within the analysis. This is used for tracking nested
  *   anonymous constructs.
  */
final case class ScopeCtx(
    owners: List[Owner] = Nil,
    blockId: Int = 0,
    lambdaId: Int = 0
) {

  /**
    * Adds the provided `Owner` to the list of owners in the current scope context.
    *
    * This method prepends the given `Owner` to the existing list of owners, creating a new `ScopeCtx` instance with the
    * updated list of owners.
    *
    * @param o
    *   The `Owner` instance to be added to the list of owners in the current scope.
    * @return
    *   A new `ScopeCtx` instance with the updated list of owners.
    */
  def push(o: Owner): ScopeCtx = copy(owners = o :: owners)

  /**
    * Determines whether the current scope is nested within another local definition.
    *
    * The method evaluates the `defDepth` property, which represents the count of `DefOwner` instances in the scope. A
    * positive count indicates that the current scope is nested within one or more definitions.
    *
    * @return
    *   `true` if the current scope is nested within at least one local definition; `false` otherwise.
    */
  def isNestedLocal: Boolean = defDepth > 0

  /**
    * Calculates the depth of nested definitions in the current scope.
    *
    * This method determines the number of `DefOwner` instances in the `owners` list, which represent nested `def`
    * entities. The count reflects the scope's nesting level within `def` definitions.
    *
    * @return
    *   The count of `DefOwner` instances in the `owners` list, representing the depth of nested `def` entities in the
    *   scope.
    */
  def defDepth: Int = owners.count(_.isInstanceOf[DefOwner])

  /**
    * Constructs the fully qualified prefix of the current scope by concatenating the names of all owners in reverse
    * order.
    *
    * This method traverses the list of owners in the scope, reverses their order, extracts their names if present, and
    * concatenates them into a dot-separated string. This represents the hierarchical structure of the scope's
    * ownership.
    *
    * @return
    *   A string representing the fully qualified prefix of the current scope, with owner names separated by dots. If no
    *   owner names are present, returns an empty string.
    */
  def qualifiedPrefix: String = owners.reverse.flatMap(_.named).mkString(".")
}
