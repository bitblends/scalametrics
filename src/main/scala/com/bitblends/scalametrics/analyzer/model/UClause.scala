/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer.model

import scala.meta.Term

/**
  * Represents a single parameter clause in a Scala function or method definition. (A tiny uniform view over param
  * *clauses* for both shapes)
  *
  * This case class is used to encapsulate information about one specific parameter clause, including the list of
  * parameters it contains and whether the clause is marked as `using` or `implicit`. Such clauses are a key aspect of
  * Scala's function definition syntax, particularly in the context of multiple parameter lists, implicit parameters, or
  * the use of contextual abstractions (e.g., `using` clauses in Scala 3).
  *
  * @param params
  *   The list of parameters defined in this parameter clause.
  * @param isUsing
  *   A flag indicating whether this parameter clause is marked as `using` (Scala 3 only).
  * @param isImplicit
  *   A flag indicating whether this parameter clause is marked as `implicit`.
  */
final case class UClause(params: List[Term.Param], isUsing: Boolean, isImplicit: Boolean)
