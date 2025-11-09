/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.utils

import java.nio.file.{Files, Path, Paths}
import scala.sys.process.{Process, ProcessLogger}
import scala.util.Try

object RepoRoot {

  /**
    * Attempts to discover the root directory based on a combined strategy. First, it tries to determine the directory
    * using `fromGit`, which checks if the given directory is part of a Git repository. If that fails, it then falls
    * back to `walkUpFind`, which searches upwards from the starting directory for common project markers such as
    * ".git", "build.sbt", or "pom.xml".
    *
    * @param startDir
    *   the starting directory to begin the discovery process; defaults to the current working directory
    * @return
    *   an `Option` containing the path to the discovered root directory if found, or `None` if neither strategy finds
    *   an appropriate directory
    */
  def discover(
      startDir: Path = Paths.get(".").toAbsolutePath.normalize
  ): Option[Path] = {
    // Combined strategy: git -> walkUp -> None
    fromGit(startDir).orElse(walkUpFind(startDir))
  }

  /**
    * Attempts to determine the root directory of a Git repository starting from the given directory. This is done using
    * the `git rev-parse --show-toplevel` command.
    *
    * @param startDir
    *   the starting directory to search for the Git repository's root
    * @return
    *   an `Option` containing the absolute and normalized path to the root directory of the Git repository, or `None`
    *   if the directory is not part of a Git repository or if an error occurs
    */
  def fromGit(startDir: Path): Option[Path] = {
    // Try `git rev-parse --show-toplevel` first
    Try {
      // run in startDir, suppressing stderr to avoid "not a git repository" errors
      val logger = ProcessLogger(_ => (), _ => ()) // Discard stdout and stderr
      val out = Process(
        Seq("git", "rev-parse", "--show-toplevel"),
        startDir.toFile
      ).!!(logger).trim
      if (out.nonEmpty) Some(Paths.get(out).toAbsolutePath.normalize)
      else None
    }.toOption.flatten
  }

  /**
    * Walks upward from the given starting directory, searching for the presence of any marker file or directory
    * specified in the provided list.
    *
    * @param startDir
    *   the directory to start searching from
    * @param markers
    *   the sequence of file or directory names to look for in each parent directory; defaults to ".git", "build.sbt",
    *   "pom.xml"
    * @return
    *   an `Option` containing the path of the first directory containing any of the markers, or `None` if no such
    *   directory is found
    */
  def walkUpFind(
      startDir: Path,
      markers: Seq[String] = Seq(".git", "build.sbt", "pom.xml")
  ): Option[Path] = {
    val start = startDir.toAbsolutePath.normalize

    @annotation.tailrec
    def loop(p: Path): Option[Path] =
      if (p == null) None
      else if (markers.exists(m => Files.exists(p.resolve(m)))) Some(p)
      else loop(p.getParent)

    loop(start)
  }
}
