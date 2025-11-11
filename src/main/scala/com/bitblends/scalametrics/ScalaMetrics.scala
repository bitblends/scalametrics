/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics

import com.bitblends.scalametrics.analyzer._
import com.bitblends.scalametrics.metrics.model.{FileMetadata, FileMetrics, ProjectInfo, ProjectMetrics}
import com.bitblends.scalametrics.stats.Stats
import com.bitblends.scalametrics.stats.model.ProjectStats
import com.bitblends.scalametrics.utils.{ScalaDialectDetector, Util}

import java.io.File
import scala.io.{BufferedSource, Source}
import scala.meta.Dialect

/**
  * The `ScalaMetrics` object provides functionality for analyzing Scala projects and generating metrics by processing
  * source files and applying analyzers. Metrics are aggregated into a `ProjectMetrics` instance, representing the
  * complete analysis of a project.
  *
  * This can serve as the main entry point for generating metrics for Scala codebases.
  */
object ScalaMetrics {

  /**
    * Generates statistical metrics for a given project by analyzing the provided source files using specified
    * analyzers.
    *
    * @param files
    *   A sequence of source files to analyze.
    * @param projectBaseDir
    *   The base directory of the project, used for resolving file paths and contextual analysis.
    * @param projectInfo
    *   Metadata and configuration details about the project, including its identity and settings.
    * @param dialectOverride
    *   An optional Scala dialect to override the default dialect determination logic.
    * @param analyzers
    *   A list of analyzers to apply during analysis. Each analyzer extracts specific metrics or performs
    *   transformations.
    */
  def generateProjectStats(
      files: Seq[File],
      projectBaseDir: File,
      projectInfo: ProjectInfo,
      dialectOverride: Option[Dialect] = None,
      analyzers: List[Analyzer] = List(FileAnalyzer, MemberAnalyzer, MethodAnalyzer)
  ): ProjectStats = {
    Stats.getProjectStats(
      generateProjectMetrics(
        files,
        projectBaseDir,
        projectInfo,
        dialectOverride,
        analyzers
      )
    )
  }

  /**
    * Creates project metrics by analyzing a collection of source files based on the provided project information,
    * directory structure, and analyzers. The analysis process involves parsing source files, applying analyzers, and
    * aggregating results into a `ProjectMetrics` instance.
    *
    * @param files
    *   A sequence of source files to be analyzed.
    * @param projectBaseDir
    *   The base directory of the project, used for resolving file paths and contextual analysis.
    * @param projectInfo
    *   Metadata and configuration information about the project, including its identity and settings.
    * @param dialectOverride
    *   An optional Scala dialect to override the default dialect determination logic.
    * @param analyzers
    *   A list of analyzers to be applied to the analysis context. Each analyzer extracts specific metrics or performs
    *   transformations.
    * @return
    *   A `ProjectMetrics` instance containing aggregated metrics and analysis results for the entire project.
    */
  def generateProjectMetrics(
      files: Seq[File],
      projectBaseDir: File,
      projectInfo: ProjectInfo,
      dialectOverride: Option[Dialect] = None,
      analyzers: List[Analyzer] = List(FileAnalyzer, MemberAnalyzer, MethodAnalyzer)
  ): ProjectMetrics = {
    val results = files.flatMap { file =>
      val src: BufferedSource = Source.fromFile(file)
      try {
        val dialect = Util.getDialect(
          file,
          projectBaseDir,
          projectInfo.scalaVersion,
          dialectOverride,
          projectInfo.crossScalaVersions
        )
        Util.getParsed(src, file, dialect).map { tree =>
          val init: AnalysisCtx = AnalysisCtx(file = file, tree = tree, projectId = Some(projectInfo.projectId))
          val out: AnalysisCtx = analyzers.foldLeft(init)((c: AnalysisCtx, a: Analyzer) => a.run(c))
          val fm: FileMetadata = out.fileMetrics.getOrElse(sys.error(s"No FileMetrics for ${file.getPath}"))
          FileMetrics(fm, out.methods, out.members)
        }
      } finally src.close()
    }.toVector
    ProjectMetrics(projectInfo, results)
  }

  /**
    * Generates metrics for a given source file by analyzing its structure and contents using specified analyzers.
    *
    * The method reads the file, parses it into a syntax tree based on the provided Scala dialect, and applies the
    * analyzers to extract various metrics. The final result includes metrics at the file, method, and member levels.
    *
    * @param file
    *   The source file to be analyzed.
    * @param dialect
    *   An optional Scala dialect to use for parsing the source file. If not provided, the dialect will be automatically
    *   detected based on analysis of the file's contents using a combination of heuristic and Bayesian methods.
    * @return
    *   An `Option[FileMetricsResult]` containing the extracted metrics and analysis results, or `None` if parsing
    *   fails.
    */
  def generateFileMetrics(file: File, dialect: Option[Dialect] = None): Option[FileMetrics] =
    scala.util
      .Using(Source.fromFile(file))(_.mkString)
      .toOption
      .map(_.trim)
      .filter(_.nonEmpty)
      .flatMap { content =>
        val fileDialect: Dialect = dialect.getOrElse(ScalaDialectDetector.detect(content))
        Util.getParsed(content, fileDialect).flatMap { tree =>
          val init: AnalysisCtx = AnalysisCtx(file = file, tree = tree)
          val analyzers: List[Analyzer] = List(FileAnalyzer, MemberAnalyzer, MethodAnalyzer)
          val out: AnalysisCtx = analyzers.foldLeft(init)((c: AnalysisCtx, a: Analyzer) => a.run(c))
          out.fileMetrics.map(fm => FileMetrics(fm, out.methods, out.members))
        }
      }
}
