/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer

import com.bitblends.scalametrics.metrics.model.{FileMetrics, MemberMetrics, MethodMetrics}

import java.io.File

/**
  * Represents the analysis context for a source file in the scope of a project. This class holds file-level information
  * and metrics, as well as details about methods and members analyzed in the context of the source file.
  *
  * Context carried through the pipeline: accumulates facts/results as analyzers run. Keep it all immutable.
  *
  * @param file
  *   The physical `File` object representing the source file on the filesystem.
  * @param tree
  *   The parsed abstract syntax tree (`meta.Source`) of the source file.
  * @param projectId
  *   An optional identifier for the project to which the analyzed file belongs.
  * @param fileId
  *   An optional identifier for the specific file in the context of the project.
  * @param fileMetrics
  *   An optional `FileMetrics` object containing metrics such as file size and lines of code for the source file.
  * @param methods
  *   A vector containing metrics about methods in the file, appended by the method-level analyzer(s).
  * @param members
  *   A vector containing metrics about members in the file (e.g., fields), appended by the member-level analyzer(s).
  */
final case class AnalysisCtx(
    file: File,
    tree: meta.Source,
    projectId: Option[String] = None,
    fileId: Option[String] = None, // set by the first analyzer in the pipeline
    fileMetrics: Option[FileMetrics] = None, // set by the first analyzer in the pipeline
    methods: Vector[MethodMetrics] = Vector.empty, // appended by method analyzer(s)
    members: Vector[MemberMetrics] = Vector.empty // appended by member analyzer(s)
) {

  /**
    * Updates the analysis context with the provided file metrics, setting the file identifier and metrics properties.
    *
    * @param fm
    *   The file metrics object containing the file identifier, size, lines of code, and other file-related information.
    * @return
    *   A new AnalysisCtx instance with the updated file identifier and metrics set to the values provided in the file
    *   metrics object.
    */
  def withFile(fm: FileMetrics): AnalysisCtx = copy(
    fileId = Some(fm.fileId),
    fileMetrics = Some(fm)
  )

  /**
    * Adds a collection of method metrics to the current analysis context and returns an updated context.
    *
    * @param ms
    *   A vector of `MethodMetrics` objects containing detailed information about the methods to be added to the
    *   context.
    * @return
    *   A new `AnalysisCtx` instance with the updated list of method metrics.
    */
  def addMethods(ms: Vector[MethodMetrics]): AnalysisCtx = copy(methods = methods ++ ms)

  /**
    * Adds a collection of member metrics to the current analysis context and returns an updated context.
    *
    * @param ms
    *   A vector of `MemberMetrics` objects containing detailed information about the members to be added to the
    *   context.
    * @return
    *   A new `AnalysisCtx` instance with the updated list of member metrics.
    */
  def addMembers(ms: Vector[MemberMetrics]): AnalysisCtx = copy(members = members ++ ms)
}
