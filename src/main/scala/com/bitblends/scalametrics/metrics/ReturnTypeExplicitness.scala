/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics

import com.bitblends.scalametrics.analyzer.TypeInfer._
import com.bitblends.scalametrics.analyzer.model.{ReturnTypeItem, ReturnTypeSummary}

import scala.meta._

/**
  * Return-type explicitness
  *
  * For a method def `f(...) = expr`, the compiler infers the result type from expr. For a method def
  * `f(...): T = expr`, the result type is declared (explicit).
  *
  * For analyzing:
  *   - API stability: Public methods with inferred types can silently change when implementation changes (or when
  *     dependencies upgrade), leaking new types to users.
  *   - Binary/source compatibility: Explicit types reduce accidental ABI changes and make MiMa-style checks more
  *     meaningful.
  *   - Compile speed and errors: Explicit types often improve error messages and can speed compilation in
  *     generic/inline-heavy code.
  *   - Docs & readability: Clear signatures are easier to read, especially for generic and higher-kinded code.
  *
  * Typical policy:
  *   - Track % of public methods with explicit return types.
  *   - Track for val/var type ascriptions.
  *   - Optionally track for non-public types
  */
object ReturnTypeExplicitness {

  /**
    * Analyzes a variable declaration (`var`) and extracts metadata including its name, visibility, and information
    * about whether its return type is explicit or inferred.
    *
    * @param v
    *   The `var` declaration to be analyzed. This should be a representation of a Scala `var` including its pattern,
    *   modifiers, type, and optionally its right-hand-side initialization.
    * @return
    *   A `ReturnTypeItem` containing the name of the variable, its kind ("var"), the source position, whether it is
    *   publicly accessible, whether an explicit return type is provided, and, if applicable, an inferred return type
    *   based on the variable's right-hand-side initialization.
    */
  def forVar(v: Defn.Var): ReturnTypeItem = {
    val explicitRT = v.decltpe.isDefined
    val inferredRT = if (!explicitRT) v.rhs.flatMap(inferSimpleType) else None
    val nm = extractPatternNames(v.pats)
    ReturnTypeItem(nm, "var", v.pos, isPublic(v.mods), explicitRT, inferredRT)
  }

  /**
    * Analyzes a value declaration (`val`) and extracts metadata including its name, visibility, and information about
    * whether its return type is explicit or inferred.
    *
    * @param v
    *   The `val` declaration to be analyzed. This should be a representation of a Scala `val` including its patterns,
    *   modifiers, type, and right-hand-side initialization.
    * @return
    *   A `ReturnTypeItem` containing the name of the value(s), its kind ("val"), the source position, whether it is
    *   publicly accessible, whether an explicit return type is provided, and, if applicable, an inferred return type
    *   based on the value's right-hand-side initialization.
    */
  def forVal(v: Defn.Val): ReturnTypeItem = {
    val explicitRT = v.decltpe.isDefined
    val inferredRT = if (!explicitRT) inferSimpleType(v.rhs) else None
    val nm = extractPatternNames(v.pats)
    ReturnTypeItem(nm, "val", v.pos, isPublic(v.mods), explicitRT, inferredRT)
  }

  /**
    * Analyzes a method declaration (`def`) and extracts metadata including its name, visibility, and information about
    * whether its return type is explicit or inferred.
    *
    * @param d
    *   The `def` declaration to be analyzed. This should be a representation of a Scala `def` including its name,
    *   modifiers, optionally declared type, and body.
    * @return
    *   A `ReturnTypeItem` containing the name of the method, its kind ("def"), the source position, whether it is
    *   publicly accessible, whether an explicit return type is provided, and, if applicable, an inferred return type
    *   based on the method's body.
    */
  def forDef(d: Defn.Def): ReturnTypeItem = {
    val explicitRT = d.decltpe.isDefined
    val inferredRT = if (!explicitRT) inferSimpleType(d.body) else None
    ReturnTypeItem(d.name.value, "def", d.pos, isPublic(d.mods), explicitRT, inferredRT)
  }

  /**
    * Analyzes a method declaration (`def`) and extracts metadata including its name, visibility, and whether its return
    * type is explicitly declared.
    *
    * @param d
    *   The `def` declaration to be analyzed. This should be a representation of a Scala `def` including its name,
    *   modifiers, explicitly declared return type, and position in the source code.
    * @return
    *   A `ReturnTypeItem` containing the name of the method, its kind ("decl-def"), the source position, whether it is
    *   publicly accessible, and a flag indicating that its return type is explicitly declared.
    */
  def forDecl(d: Decl.Def) =
    ReturnTypeItem(d.name.value, "decl-def", d.pos, isPublic(d.mods), hasExplicitReturnType = true, None)

  /**
    * Analyzes the provided source code to extract return type information for various code elements such as functions,
    * methods, values, and variables. It uses a specific Scala dialect for parsing and traverses the abstract syntax
    * tree to collect metadata about these elements.
    *
    * @param code
    *   The source code to analyze. This should be a valid Scala source code string.
    * @param dialect
    *   The Scala dialect to use for parsing the source code. Defaults to Scala 2.12 dialect.
    * @return
    *   A list of `ReturnTypeItem` objects, each representing an element with metadata about its name, kind (e.g.,
    *   "def", "val", "var"), source position, visibility, and whether its return type is explicitly declared.
    */
  def analyzeSource(code: String, dialect: Dialect = dialects.Scala212): List[ReturnTypeItem] = {
    val parsed = dialect(code).parse[Source].get
    val buf = scala.collection.mutable.ListBuffer.empty[ReturnTypeItem]

    /**
      * Object `Collect` extends the `TraverserCompat` trait and implements functionality to traverse an abstract syntax
      * tree (AST) for Scala source code. It provides a mechanism to collect metadata about various kinds of code
      * elements, including methods, values, and variables, specifically focusing on their return type explicitness and
      * visibility.
      *
      * The traverser analyzes the following elements:
      *   - Concrete methods (`def`): Captures metadata such as method name, visibility, position, and whether its
      *     return type is explicitly defined or inferred.
      *   - Abstract methods (`def` declarations): Similar to concrete methods but always considers the return type
      *     explicitly defined.
      *   - Values (`val`): Captures metadata including value names, visibility, position, and whether the return type
      *     is explicitly defined or inferred.
      *   - Variables (`var`): Similar to values, but specifically analyzes mutable variables.
      *
      * For each matched tree node, relevant metadata is encapsulated in a `ReturnTypeItem` and added to an internal
      * buffer for aggregation.
      *
      * Fallback behavior delegates traversal to child nodes of the current tree node.
      */
    object Collect extends TraverserCompat {
      override def apply(tree: Tree): Unit = tree match {

        // Concrete methods
        case d: Defn.Def =>
          val explicit = d.decltpe.nonEmpty
          buf += ReturnTypeItem(d.name.value, "def", d.pos, isPublic(d.mods), explicit)
          traverseChildren(tree)

        // Abstract methods
        case d: Decl.Def =>
          buf += ReturnTypeItem(d.name.value, "decl-def", d.pos, isPublic(d.mods), hasExplicitReturnType = true)
          traverseChildren(tree)

        // Values (val)
        case v: Defn.Val =>
          val explicit = v.decltpe.nonEmpty
          val nm = extractPatternNames(v.pats)
          buf += ReturnTypeItem(nm, "val", v.pos, isPublic(v.mods), explicit)
          traverseChildren(tree)

        // Variables (var)
        case v: Defn.Var =>
          val explicit = v.decltpe.nonEmpty
          val nm = extractPatternNames(v.pats)
          buf += ReturnTypeItem(nm, "var", v.pos, isPublic(v.mods), explicit)
          traverseChildren(tree)

        case _ =>
          traverseChildren(tree)
      }
    }

    Collect(parsed)
    buf.toList
  }

  /**
    * Extracts variable names from a list of patterns. Handles both simple patterns (Pat.Var) and tuple patterns
    * (Pat.Tuple).
    *
    * @param pats
    *   The list of patterns to extract names from
    * @return
    *   A comma-separated string of variable names
    */
  private def extractPatternNames(pats: List[Pat]): String = {
    val names = pats.flatMap {
      case Pat.Var(n)      => List(n.value)
      case Pat.Tuple(args) => args.collect { case Pat.Var(n) => n.value }
      case _               => Nil
    }
    if (names.isEmpty) "" else names.mkString(",")
  }

  /**
    * Determines whether a list of modifiers indicates that an entity is publicly accessible.
    *
    * @param mods
    *   The list of modifiers to check. This should represent the visibility modifiers (e.g., `private`, `protected`) of
    *   a Scala member.
    * @return
    *   `true` if the entity is public (i.e., it does not have `private` or `protected` modifiers), otherwise `false`.
    */
  private def isPublic(mods: List[Mod]): Boolean =
    !mods.exists {
      case _: Mod.Private   => true
      case _: Mod.Protected => true
      case _                => false
    }

  /**
    * Generates a summary of return type declarations from a list of analyzed code elements. The summary includes total
    * number of definitions, public definitions, explicitly specified return types, and explicitly specified public
    * return types.
    *
    * @param items
    *   A list of `ReturnTypeItem` objects, each representing a code element with metadata about its name, kind (e.g.,
    *   "def", "val", "var"), visibility, and whether its return type is explicitly declared.
    *
    * @return
    *   A `ReturnTypeSummary` object containing aggregated statistics about the total number of definitions, the total
    *   number of public definitions, the count of definitions with explicit return types, and the count of public
    *   definitions with explicit return types.
    */
  def summarize(items: List[ReturnTypeItem]): ReturnTypeSummary = {
    val MethodKinds = Set("def", "decl-def", "val", "var")
    val all = items.filter(i => MethodKinds(i.kind))
    val total = all.size
    val explicitAll = all.count(_.hasExplicitReturnType)
    val pub = all.filter(_.isPublic)
    val totalPub = pub.size
    val explicitPub = pub.count(_.hasExplicitReturnType)

    ReturnTypeSummary(total, totalPub, explicitAll, explicitPub)
  }

}
