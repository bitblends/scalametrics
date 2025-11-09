/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics

import scala.meta.{Defn, Term}

trait Metric[T] {

  /**
    * Processes and evaluates the given term.
    *
    * @param term
    *   the input term to be processed and evaluated
    * @return
    *   the computed result derived from the provided term
    */
  def compute(term: Term): T

  /**
    * Extracts and processes the right-hand side of a given value definition, if it is a term.
    *
    * @param v
    *   the value definition from which the right-hand side term is extracted and processed
    * @return
    *   an Option containing the result of processing the term, or None if the right-hand side is not a term
    */
  def forVal(v: Defn.Val): Option[T] =
    Option(v.rhs).collect { case t: Term => compute(t) }

  /**
    * Extracts and processes the body of a given method definition, if it is a term.
    *
    * @param d
    *   the method definition from which the body term is extracted and processed
    * @return
    *   an Option containing the result of processing the term, or None if the body is not a term
    */
  def forDef(d: Defn.Def): Option[T] =
    Option(d.body).collect { case t: Term => compute(t) }
}
