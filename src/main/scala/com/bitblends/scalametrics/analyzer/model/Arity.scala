/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

/**
  * Represents detailed metrics about the structure of parameter lists and parameters in a Scala function, method, or
  * other callable entity.
  *
  * @param totalParams
  *   The total number of parameters across all parameter lists.
  * @param paramLists
  *   The total number of parameter lists in the callable entity.
  * @param implicitParamLists
  *   The total number of parameter lists marked as `implicit`.
  * @param usingParamLists
  *   The total number of parameter lists marked as `using` (Scala 3 only).
  * @param implicitParams
  *   The total number of `implicit` parameters across all parameter lists.
  * @param usingParams
  *   The total number of `using` parameters (Scala 3 only) across all parameter lists.
  * @param defaultedParams
  *   The total number of parameters with default values.
  * @param byNameParams
  *   The total number of parameters defined as by-name parameters (e.g., `=> T`).
  * @param varargParams
  *   The total number of parameters defined as varargs (e.g., `T*`).
  * @param inlineParams
  *   The total number of parameters marked as `inline` (Scala 3 only).
  */
final case class Arity(
    totalParams: Int = 0,
    paramLists: Int = 0,
    implicitParamLists: Int = 0,
    usingParamLists: Int = 0,
    implicitParams: Int = 0,
    usingParams: Int = 0,
    defaultedParams: Int = 0,
    byNameParams: Int = 0,
    varargParams: Int = 0,
    inlineParams: Int = 0
)
