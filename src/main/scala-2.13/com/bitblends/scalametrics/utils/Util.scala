/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.utils

import java.io.File
import scala.io.Source
import scala.meta.Input.VirtualFile
import scala.meta.{
  Dialect,
  Init,
  Mod,
  Parsed,
  Pkg,
  Token,
  Tokens,
  Tree,
  Type,
  XtensionCollectionLikeUI,
  XtensionParseInputLike,
  XtensionSyntax
}

/**
  * Utility object providing various helper methods for Scala source code analysis. This includes functionalities for
  * parsing source code into syntax trees, determining dialects and access modifiers, analyzing code properties like
  * lines of code, and extracting metadata such as package names or Scaladoc comments.
  */
object Util {

  /**
    * Parses the provided Scala source code contained in a file using the specified dialect and returns an abstract
    * syntax tree (AST) representation if parsing is successful. If parsing fails, an optional warning message is
    * printed and None is returned.
    *
    * @param source
    *   the source containing the contents of the Scala file to be parsed
    * @param file
    *   the file being parsed, used to provide additional context such as the file name for warnings or errors
    * @param dialect
    *   the Scala language dialect to use for parsing the source file
    * @return
    *   an Option containing the parsed abstract syntax tree (`meta.Source`) if parsing is successful; None otherwise
    */
  def getParsed(source: Source, file: File, dialect: Dialect): Option[meta.Source] = {
    val content = source.mkString
    val input = VirtualFile(file.getPath, content)
    implicit val implicitDialect: Dialect = dialect
    input.parse[scala.meta.Source] match {
      case Parsed.Success(t: scala.meta.Source) => Some(t)
      case Parsed.Error(_, message, _)          =>
        println(s"Warning: Failed to parse ${file.getName}: $message")
        None
    }
  }

  /**
    * Parses the given source string using the specified Scala dialect and returns an abstract syntax tree (AST)
    * representation if parsing is successful. If parsing fails, a warning message is printed, and None is returned.
    *
    * @param sourceStr
    *   the source code string to be parsed
    * @param dialect
    *   the Scala language dialect to use for parsing
    * @return
    *   an Option containing the parsed abstract syntax tree (`meta.Source`) if parsing is successful; None otherwise
    */
  def getParsed(sourceStr: String, dialect: Dialect): Option[meta.Source] = {
    val input = VirtualFile("<input>", sourceStr)
    implicit val implicitDialect: Dialect = dialect
    input.parse[scala.meta.Source] match {
      case Parsed.Success(t: scala.meta.Source) => Some(t)
      case Parsed.Error(_, message, _)          =>
        println(s"Warning: Failed to parse input: $message")
        None
    }
  }

  /**
    * Determines the Scala language dialect to use for processing the given source file.
    *
    * @param sourceFile
    *   the file representing the source code whose dialect needs to be determined
    * @param projectBaseDir
    *   the base directory of the project, used as a reference for resolving paths and configurations
    * @param scalaVersion
    *   the version of Scala to be used as a base for determining the dialect
    * @param dialectOverride
    *   an optional overspecified dialect that, if provided, will override automatic determination
    * @param crossScalaVersions
    *   a sequence of Scala versions to consider for cross-compilation
    * @return
    *   the selected Scala dialect for processing the given source file
    */
  def getDialect(
      sourceFile: File,
      projectBaseDir: File,
      scalaVersion: String,
      dialectOverride: Option[Dialect] = None,
      crossScalaVersions: Seq[String] = Seq.empty
  ): Dialect = {
    dialectOverride.getOrElse {
      DialectConfig.getDialectForFile(
        sourceFile,
        projectBaseDir,
        scalaVersion,
        crossScalaVersions
      )
    }
  }

  /**
    * Computes the number of lines of code (LOC) for a given abstract syntax tree (AST) node.
    *
    * @param t
    *   the abstract syntax tree (AST) node from which to compute the LOC
    * @return
    *   the number of LOC in the given AST node
    */
  def locOf(t: Tree): Int = {
    val code = t.pos.input.text.substring(t.pos.start, t.pos.end)
    countLOC(code)
  }

  /**
    * Counts the number of non-empty, non-whitespace-only lines in the provided text.
    *
    * @param text
    *   the input string for which the lines of code are to be counted
    * @return
    *   the number of lines containing non-whitespace characters
    */
  def countLOC(text: String): Int = text.linesIterator.count(_.exists(!_.isWhitespace))

  /**
    * Determines the access level of a given list of modifiers, with an option to override to a local access level.
    *
    * @param mods
    *   a list of modifiers (`List[Mod]`) that represent the access level of a definition
    * @param localOverride
    *   a boolean flag indicating whether to override the access level to "local"
    * @return
    *   a string representing the access level, which can be "local", "private", "protected", or "public"
    */
  def accessOf(mods: List[Mod], localOverride: Boolean): String =
    if (localOverride) "local"
    else if (mods.exists(_.isInstanceOf[Mod.Private])) "private"
    else if (mods.exists(_.isInstanceOf[Mod.Protected])) "protected"
    else "public"

  /**
    * Checks whether a given definition has an associated Scaladoc comment.
    *
    * @param defn
    *   the abstract syntax tree (AST) node representing the definition to check for Scaladoc
    * @param allTokens
    *   all tokens available in the source, used to locate relevant comments around the definition
    * @return
    *   true if the definition has an associated Scaladoc comment; false otherwise
    */
  def hasScaladocComment(defn: Tree, allTokens: Tokens): Boolean = {
    // Find the first token of this definition
    val defnTokens = defn.tokens
    if (defnTokens.isEmpty) return false

    val firstTokenPos = defnTokens.head.start

    // Look backwards through all tokens to find a /** comment immediately before this definition
    val precedingTokens = allTokens.filter(_.end <= firstTokenPos)

    if (precedingTokens.isEmpty) return false

    // Find the last Scaladoc comment before this definition
    val scalaDocComments = precedingTokens.filter { (token: Token) =>
      val syntax = token.syntax.trim
      syntax.startsWith("/**") && syntax.endsWith("*/")
    }

    if (scalaDocComments.isEmpty) return false

    // Get the last Scaladoc comment
    val lastScalaDoc = scalaDocComments.last

    // Check if there are only whitespace/newline tokens between the comment and definition
    val tokensBetween = precedingTokens.filter(t => t.start > lastScalaDoc.end && t.end < firstTokenPos)

    // If all tokens between are whitespace (spaces, tabs, newlines), consider it documented
    tokensBetween.isEmpty || tokensBetween.forall { t =>
      t.syntax.trim.isEmpty ||
      t.syntax.forall(c => c.isWhitespace || c == '\n' || c == '\r')
    }
  }

  /**
    * Extracts the package name of a given abstract syntax tree (AST) representation of a source file. The method
    * determines the package name either from regular package declarations, from a package object definition if present,
    * or uses a default name if no package is declared.
    *
    * @param tree
    *   the abstract syntax tree (AST) to extract the package name from
    * @return
    *   a string representing the package name, <code>&lt;default&gt;</code> if no package is declared
    */
  def extractPackageName(tree: Tree): String = {
    // Find package object definition
    // For package objects, combine the package declaration with the package object name
    val packageObjectName = tree.collect { case Pkg.Object(_, name, _) => name.value }.headOption
    // Collect regular package declarations
    val packageParts = tree.collect { case pkg: Pkg => pkg.ref.toString() }.filter(_.nonEmpty)

    (packageParts, packageObjectName) match {
      case (Nil, _)                   => "<default>"
      case (base :: _, Some(objName)) => s"$base.$objName"
      case (single :: Nil, None)      => single
      case (parts, None)              => parts.mkString(".") // Multiple package declarations (chained packages)
    }
  }

  /**
    * Checks whether the provided list of modifiers contains an `implicit` modifier.
    *
    * @param mods
    *   a list of modifiers (`List[Mod]`) representing annotations or modifiers of a Scala definition
    * @return
    *   true if the modifier list contains an `implicit` modifier; false otherwise
    */
  def hasImplicitModifier(mods: List[Mod]): Boolean = mods.exists {
    case Mod.Implicit() => true
    case _              => false
  }

  /**
    * Checks whether the provided list of modifiers contains an `inline` modifier.
    *
    * @param mods
    *   a list of modifiers (`List[Mod]`) representing annotations or modifiers of a Scala definition
    * @return
    *   true if the list contains an `inline` modifier; false otherwise
    */
  def hasInlineModifier(mods: List[Mod]): Boolean = mods.exists {
    case Mod.Inline() => true
    case _            => false
  }

  /**
    * Determines whether a given definition is deprecated based on its associated modifiers or documentation.
    *
    * @param mods
    *   a list of modifiers (`List[Mod]`) representing annotations or modifiers of a Scala definition
    * @param defn
    *   the abstract syntax tree (AST) node representing the definition to check for deprecation
    * @param allTokens
    *   all tokens available in the source, used to inspect comments or annotations related to the definition
    * @return
    *   true if the definition is considered deprecated either due to a deprecated annotation or a deprecated Scaladoc
    *   comment; false otherwise
    */
  def isDeprecated(mods: List[Mod], defn: Tree, allTokens: Tokens): Boolean =
    hasDeprecatedAnnotation(mods) || hasDeprecatedDoc(defn, allTokens)

  /**
    * Checks if the provided list of modifiers contains a deprecated annotation.
    *
    * @param mods
    *   a list of modifiers (`List[Mod]`) representing annotations or modifiers of a Scala definition
    * @return
    *   true if the list contains an annotation indicating deprecation (e.g., `@deprecated`, `@deprecatedInheritance`,
    *   or `@deprecatedOverriding`); false otherwise
    */
  def hasDeprecatedAnnotation(mods: List[Mod]): Boolean =
    mods.exists {
      case Mod.Annot(Init(tpe, _, _)) =>
        val last = tpe.syntax.split('.').last // e.g., "deprecated", "Deprecated"
        last == "deprecated" ||
        last == "Deprecated" ||
        last == "deprecatedInheritance" ||
        last == "deprecatedOverriding"
      case _ => false
    }

  /**
    * Determines if a given Scala definition has an associated deprecated Scaladoc comment that is contiguous to the
    * definition without blank lines and contains the `@deprecated` tag, case-insensitive.
    *
    * @param defn
    *   the abstract syntax tree (AST) node representing the definition to check for the deprecated Scaladoc comment
    * @param allTokens
    *   the list of all tokens in the source, used to locate relevant comments around the definition
    * @return
    *   true if the definition has an associated deprecated Scaladoc comment that is contiguous and contains the
    *   <code>@deprecated</code> tag; false otherwise
    */
  def hasDeprecatedDoc(defn: Tree, allTokens: Tokens): Boolean = {
    val start = defn.pos.start
    val before = allTokens
      .takeWhile(_.pos.end <= start)
      .reverse
      .dropWhile(isWs)
      .headOption

    before match {
      case Some(c: Token.Comment) if c.value.startsWith("/**") || c.value.startsWith("/*") =>
        // must be contiguous (no blank line) and contain @deprecated (case-insensitive)
        val between = defn.pos.input.text.substring(c.pos.end, start)
        val noBlankLine = !(between.contains("\n\n") || between.contains("\r\n\r\n"))
        val hasTag = c.value.toLowerCase.contains("@deprecated")
        noBlankLine && hasTag
      case _ => false
    }
  }

  /**
    * Determines if the specified token is a whitespace character.
    *
    * @param t
    *   the token to check
    * @return
    *   true if the token is a whitespace character such as Space, Tab, CR, LF, or LFLF; false otherwise
    */
  private def isWs(t: Token): Boolean = t match {
    case _: Token.Space | _: Token.Tab | _: Token.CR | _: Token.LF | _: Token.LFLF => true
    case _                                                                         => false
  }

  /**
    * Extracts the last type name from the given type representation.
    *
    * @param t
    *   the type representation (`Type`) from which to extract the last type name
    * @return
    *   a string representing the last type name in the provided type representation
    */
  def lastTypeName(t: Type): String = t match {
    case Type.Name(n)                  => n
    case Type.Select(_, Type.Name(n))  => n
    case Type.Project(_, Type.Name(n)) => n
    case Type.Apply(tpe, _)            => lastTypeName(tpe)
    case Type.Annotate(tpe, _)         => lastTypeName(tpe)
    case other                         => other.syntax
  }

  /**
    * Determines if a list of modifiers marks an entity as public.
    *
    * @param mods
    *   A list of modifiers applied to the entity.
    * @return
    *   True if the entity is public (has no access modifier or is explicitly public), false otherwise.
    */
  private def isPublicMod(mods: List[Mod]): Boolean = {
    // A function is public if it has no access modifier (default is public in Scala)
    // or explicitly marked as public
    !mods.exists {
      case _: Mod.Private   => true
      case _: Mod.Protected => true
      case _                => false
    }
  }
}
