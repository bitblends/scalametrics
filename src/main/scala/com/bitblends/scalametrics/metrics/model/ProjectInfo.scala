/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics.model
import com.bitblends.scalametrics.utils.Id

/**
  * Represents metadata and configuration details about a project, including its name, version, Scala version,
  * description, organization, homepage, licensing information, and more. This class encapsulates key project properties
  * in a concise structure.
  *
  * @param projectId
  *   A unique identifier for the project.
  * @param name
  *   The name of the project.
  * @param version
  *   The version of the project.
  * @param scalaVersion
  *   The Scala version the project is using.
  * @param description
  *   An optional description of the project.
  * @param crossScalaVersions
  *   A collection of Scala versions the project supports for cross-compilation.
  * @param organization
  *   An optional identifier for the project organization.
  * @param organizationName
  *   An optional name of the project organization.
  * @param organizationHomepage
  *   An optional homepage for the organization.
  * @param homepage
  *   An optional homepage for the project itself.
  * @param licenses
  *   An optional licensing information for the project.
  * @param startYear
  *   An optional year indicating when the project was started.
  * @param isSnapshot
  *   An optional snapshot indicator for the project version.
  * @param apiURL
  *   An optional URL for the project's API documentation.
  * @param scmInfo
  *   An optional source control management (SCM) information for the project.
  * @param developers
  *   An optional list of developers contributing to the project.
  * @param versionScheme
  *   An optional versioning scheme employed by the project.
  * @param projectInfoNameFormal
  *   An optional formal name for the project information.
  */
case class ProjectInfo(
    projectId: String,
    name: String,
    version: String,
    scalaVersion: String,
    description: Option[String] = None,
    crossScalaVersions: Seq[String] = Seq.empty,
    organization: Option[String] = None,
    organizationName: Option[String] = None,
    organizationHomepage: Option[String] = None,
    homepage: Option[String] = None,
    licenses: Option[String] = None,
    startYear: Option[String] = None,
    isSnapshot: Option[String] = None,
    apiURL: Option[String] = None,
    scmInfo: Option[String] = None,
    developers: Option[String] = None,
    versionScheme: Option[String] = None,
    projectInfoNameFormal: Option[String] = None
)

/**
  * Companion object for the `ProjectInfo` class, offering helper methods for creation and management of project
  * metadata. This object provides utilities for constructing instances of `ProjectInfo`, including generating a unique
  * project identifier (`projectId`) derived from the project name.
  */
object ProjectInfo {

  /**
    * Constructs a `ProjectInfo` instance with a generated `projectId` and the specified project details.
    *
    * @param name
    *   the name of the project
    * @param version
    *   the version of the project
    * @param scalaVersion
    *   the Scala version used by the project
    * @return
    *   a `ProjectInfo` instance populated with the provided details and a `projectId` derived from the project `name`
    */
  def apply(
      name: String,
      version: String,
      scalaVersion: String
  ): ProjectInfo = {
    ProjectInfo(
      projectId = Id.of(name),
      name,
      version,
      scalaVersion
    )
  }

  /**
    * Constructs a `ProjectInfo` instance with detailed project metadata and a generated `projectId`.
    *
    * @param name
    *   the name of the project
    * @param version
    *   the version of the project
    * @param scalaVersion
    *   the Scala version used by the project
    * @param description
    *   an optional description of the project
    * @param crossScalaVersions
    *   a collection of Scala versions the project supports for cross-compilation
    * @param organization
    *   an optional identifier for the project organization
    * @param organizationName
    *   an optional name of the project organization
    * @param organizationHomepage
    *   an optional homepage for the organization
    * @param homepage
    *   an optional homepage for the project itself
    * @param licenses
    *   an optional licensing information for the project
    * @param startYear
    *   an optional year indicating when the project was started
    * @param isSnapshot
    *   an optional snapshot indicator for the project version
    * @param apiURL
    *   an optional URL for the project's API documentation
    * @param scmInfo
    *   an optional source control management (SCM) information for the project
    * @param developers
    *   an optional list of developers contributing to the project
    * @param versionScheme
    *   an optional versioning scheme employed by the project
    * @param projectInfoNameFormal
    *   an optional formal name for the project information
    * @return
    *   a `ProjectInfo` instance populated with the provided details and a `projectId` derived from the project `name`
    */
  def apply(
      name: String,
      version: String,
      scalaVersion: String,
      description: Option[String],
      crossScalaVersions: Seq[String],
      organization: Option[String],
      organizationName: Option[String],
      organizationHomepage: Option[String],
      homepage: Option[String],
      licenses: Option[String],
      startYear: Option[String],
      isSnapshot: Option[String],
      apiURL: Option[String],
      scmInfo: Option[String],
      developers: Option[String],
      versionScheme: Option[String],
      projectInfoNameFormal: Option[String]
  ): ProjectInfo = ProjectInfo(
    projectId = Id.of(name),
    name,
    version,
    scalaVersion,
    description,
    crossScalaVersions,
    organization,
    organizationName,
    organizationHomepage,
    homepage,
    licenses,
    startYear,
    isSnapshot,
    apiURL,
    scmInfo,
    developers,
    versionScheme,
    projectInfoNameFormal
  )
}
