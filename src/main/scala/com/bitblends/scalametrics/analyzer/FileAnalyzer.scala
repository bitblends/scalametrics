/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer

import com.bitblends.scalametrics.metrics.model.FileMetadata
import com.bitblends.scalametrics.utils.{FileId, RepoRoot, Util}

/**
  * The FileAnalyzer specializes in analyzing characteristics of source files within a project. It extracts file-level
  * metrics, such as package name, lines of code, and file size, and updates the analysis context with these details.
  *
  * This analyzer primarily works at the file level and serves as the entry point for per-file metadata gathering. It
  * extends the Analyzer class and overrides its behavior to tailor the analysis for file-level insights.
  */
object FileAnalyzer extends Analyzer {

  /**
    * Returns the name of this analyzer, identifying its specialization.
    *
    * @return
    *   A string representing the name of the analyzer, which is "file" for the FileAnalyzer.
    */
  override def name: String = "file"

  /**
    * Executes the analysis logic of this analyzer on the provided context and returns an updated context.
    *
    * @param ctx
    *   The initial analysis context containing the source file and accumulated metrics or annotations.
    * @return
    *   A new AnalysisCtx instance with updates or modifications as a result of the analysis performed.
    */
  override def run(ctx: AnalysisCtx): AnalysisCtx = {
    val pkg = Util.extractPackageName(ctx.tree)
    val content = ctx.tree.pos.input.text
    val fm = FileMetadata(
      projectId = ctx.projectId,
      fileId = FileId.idFor(ctx.file, ctx.projectId, RepoRoot.discover()),
      file = ctx.file,
      packageName = pkg,
      linesOfCode = Util.countLOC(content),
      fileSizeBytes = ctx.file.length()
    )
    ctx.withFile(fm)
  }

}
