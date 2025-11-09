/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer

/**
  * Defines an interface for analyzers that process and transform an AnalysisCtx during static code analysis. Each
  * analyzer performs a specific type of analysis and can modify the context by adding metrics or annotations to it.
  */
trait Analyzer {

  /**
    * Retrieves the name of the analyzer.
    *
    * @return
    *   the name of the analyzer as a string
    */
  def name: String

  /**
    * Executes the analysis logic of this analyzer on the provided context and returns an updated context.
    *
    * @param ctx
    *   The initial analysis context containing the source file and accumulated metrics or annotations.
    * @return
    *   A new AnalysisCtx instance with updates or modifications as a result of the analysis performed.
    */
  def run(ctx: AnalysisCtx): AnalysisCtx
}
