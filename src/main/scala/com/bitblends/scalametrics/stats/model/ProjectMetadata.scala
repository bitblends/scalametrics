/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents metadata and configuration details for a project.
  *
  * @param name
  *   The name of the project.
  * @param version
  *   The version of the project.
  * @param scalaVersion
  *   The Scala version used in the project.
  * @param description
  *   An optional description or summary of the project.
  * @param crossScalaVersions
  *   A sequence of Scala versions for cross-compilation.
  * @param organization
  *   An optional identifier for the organization overseeing the project.
  * @param organizationName
  *   An optional name of the organization overseeing the project.
  * @param organizationHomepage
  *   An optional homepage URL of the organization.
  * @param homepage
  *   An optional homepage URL for the project.
  * @param licenses
  *   An optional license information for the project, typically in the form of SPDX identifier(s).
  * @param startYear
  *   An optional start year indicating when the project was initiated.
  * @param isSnapshot
  *   An optional string indicating if the project version is a snapshot.
  * @param apiURL
  *   An optional URL to the project's API documentation.
  * @param scmInfo
  *   An optional string containing source control management information.
  * @param developers
  *   An optional string detailing information about the developers involved in the project.
  * @param versionScheme
  *   An optional versioning scheme used by the project (e.g., "semantic").
  * @param projectInfoNameFormal
  *   An optional, formal name for the project, if applicable.
  */
case class ProjectMetadata(
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
    developers: Seq[String] = Seq.empty,
    versionScheme: Option[String] = None,
    projectInfoNameFormal: Option[String] = None
) extends Serializer {

  /**
    * Generates a formatted string representation of the project's metadata.
    *
    * @return
    *   A multiline string containing the project's metadata details.
    */
  override def formattedString: String = {
    s"""Project Metadata:
       |----------------------------------------------------------
       |  Name: $name
       |  Version: $version
       |  Scala Version: $scalaVersion
       |  Description: ${description.getOrElse("N/A")}
       |  Cross Scala Versions: ${if (crossScalaVersions.nonEmpty) crossScalaVersions.mkString(", ") else "N/A"}
       |  Organization: ${organization.getOrElse("N/A")}
       |  Organization Name: ${organizationName.getOrElse("N/A")}
       |  Organization Homepage: ${organizationHomepage.getOrElse("N/A")}
       |  Homepage: ${homepage.getOrElse("N/A")}
       |  Licenses: ${licenses.getOrElse("N/A")}
       |  Start Year: ${startYear.getOrElse("N/A")}
       |  Is Snapshot: ${isSnapshot.getOrElse("N/A")}
       |  API URL: ${apiURL.getOrElse("N/A")}
       |  SCM Info: ${scmInfo.getOrElse("N/A")}
       |  Developers: ${if (developers.nonEmpty) developers.mkString(", ") else "N/A"}
       |  Version Scheme: ${versionScheme.getOrElse("N/A")}
       |  Project Info Name Formal: ${projectInfoNameFormal.getOrElse("N/A")}
       |""".stripMargin
  }
}
