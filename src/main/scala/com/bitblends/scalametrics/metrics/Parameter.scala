/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics

import com.bitblends.scalametrics.analyzer.model.{Arity, ParameterItem}

import scala.meta._

/**
  * The `Parameter` object provides utility methods for analyzing and computing arity metrics for methods, constructors,
  * and other parameterized definitions in Scala code. It includes functionality to inspect parameter lists, their
  * attributes, and modifiers such
  */
object Parameter {

  /**
    * Computes the `Arity` for a given method definition (`Defn.Def`) by analyzing its parameter lists.
    *
    * @param d
    *   A method definition (`Defn.Def`) whose parameter structure is to be analyzed.
    * @return
    *   An `Arity` object containing detailed metrics about the parameter lists and parameters of the given method.
    */
  def forDef(d: Defn.Def): Arity =
    arityOfParamLists(paramsOf(d))

  /**
    * Computes the `Arity` for a given method declaration (`Decl.Def`) by analyzing its parameter lists.
    *
    * @param d
    *   A method declaration (`Decl.Def`) whose parameter structure is to be analyzed.
    * @return
    *   An `Arity` object containing detailed metrics about the parameter lists and parameters of the given method.
    */
  def forDecl(d: Decl.Def): Arity =
    arityOfParamLists(paramsOf(d))

  /**
    * Extracts the parameter lists of a given method declaration (`Decl.Def`) as `List[List[Term.Param]]`.
    *
    * @param d
    *   A method declaration (`Decl.Def`) whose parameter lists are to be extracted.
    * @return
    *   A nested list of parameters (`List[List[Term.Param]]`), where each inner list represents one parameter list of
    *   the method. Returns an empty list if no parameters are defined.
    */
  def paramsOf(d: Decl.Def): List[List[Term.Param]] = d match {
    // ≥ 4.6: params live in a ParamClauseGroup
    case Decl.Def.After_4_6_0(_, _, pgOpt, _) =>
      pgOpt
        .map(_.paramClauses.collect { case Term.ParamClause(params, _) => params })
        .getOrElse(Nil)

    // < 4.6: legacy paramss
    case Decl.Def.Initial(_, _, _, paramss, _) =>
      paramss

    // single-version safety net
    case Decl.Def(_, _, _, paramss: List[List[Term.Param]] @unchecked, _) =>
      paramss
  }

  /**
    * Computes various arity-related characteristics of the provided parameter lists.
    *
    * @param paramLists
    *   A list of parameter lists, where each inner list represents a group of parameters.
    * @return
    *   An instance of `Arity` containing detailed counts of parameters and parameter lists, including totals, implicit
    *   and using parameters, defaults, by-name parameters, vararg parameters, and inline parameters.
    */
  private def arityOfParamLists(paramLists: List[List[Term.Param]]): Arity = {
    val totalParams = paramLists.map(_.size).sum
    val paramListCount = paramLists.size

    var implLists = 0
    var usingLists = 0
    var implParams = 0
    var usingParams = 0
    var defaults = 0
    var bynames = 0
    var varargs = 0
    var `inline` = 0

    paramLists.foreach { ps =>
      val (isImplList, isUsingList) = listKind(ps)
      if (isImplList) implLists += 1
      if (isUsingList) usingLists += 1

      ps.foreach { (p: Term.Param) =>
        if (isImplicit(p)) implParams += 1
        if (isUsing(p)) usingParams += 1
        if (isDefaulted(p)) defaults += 1
        if (isByName(p)) bynames += 1
        if (isVarArg(p)) varargs += 1
        if (isInline(p)) `inline` += 1
      }
    }

    Arity(
      totalParams = totalParams,
      paramLists = paramListCount,
      implicitParamLists = implLists,
      usingParamLists = usingLists,
      implicitParams = implParams,
      usingParams = usingParams,
      defaultedParams = defaults,
      byNameParams = bynames,
      varargParams = varargs,
      inlineParams = `inline`
    )
  }

  /**
    * Determines if a given parameter has the `implicit` modifier.
    *
    * @param p
    *   A parameter (`Term.Param`) whose modifiers are to be checked for the presence of `implicit`.
    * @return
    *   `true` if the parameter has the `implicit` modifier, otherwise `false`.
    */
  private def isImplicit(p: Term.Param): Boolean =
    p.mods.exists(_.isInstanceOf[Mod.Implicit])

  /**
    * Determines if the given parameter has the `using` modifier in its list of modifiers.
    *
    * @param p
    *   A parameter (`Term.Param`) whose modifiers are to be checked for the presence of `using`.
    * @return
    *   `true` if the parameter has the `using` modifier, otherwise `false`.
    */
  private def isUsing(p: Term.Param): Boolean =
    p.mods.exists(_.isInstanceOf[Mod.Using])

  /**
    * Determines if a given parameter has a default value defined.
    *
    * @param p
    *   A parameter (`Term.Param`) to check for the presence of a default value.
    *
    * @return
    *   `true` if the parameter has a default value, otherwise `false`.
    */
  private def isDefaulted(p: Term.Param): Boolean =
    p.default.nonEmpty

  /**
    * Determines if the given parameter is a by-name parameter.
    *
    * @param p
    *   A parameter (`Term.Param`) to check if it is of by-name type.
    * @return
    *   `true` if the parameter is of by-name type, otherwise `false`.
    */
  private def isByName(p: Term.Param): Boolean =
    p.decltpe.exists { case Type.ByName(_) => true; case _ => false }

  /**
    * Determines if a given parameter is a vararg (i.e., a repeated parameter).
    *
    * @param p
    *   A parameter (`Term.Param`) to check for the vararg type.
    * @return
    *   `true` if the parameter is a vararg, otherwise `false`.
    */
  private def isVarArg(p: Term.Param): Boolean =
    p.decltpe.exists { case Type.Repeated(_) => true; case _ => false }

  /**
    * Determines if the given parameter has the `inline` modifier in its list of modifiers.
    *
    * @param p
    *   A parameter (`Term.Param`) whose modifiers are to be checked for the presence of `inline`.
    * @return
    *   `true` if the parameter has the `inline` modifier, otherwise `false`.
    */
  private def isInline(p: Term.Param): Boolean =
    p.mods.exists { case Mod.Inline() => true; case _ => false }

  /**
    * Determines the kind of the parameter list by analyzing the modifiers of the first parameter.
    *
    * @param params
    *   A list of parameters (`Term.Param`) whose modifiers are to be analyzed.
    * @return
    *   A tuple consisting of two boolean values:
    *   - The first boolean indicates whether the parameter list contains the `implicit` modifier.
    *   - The second boolean indicates whether the parameter list contains the `using` modifier.
    */
  private def listKind(params: List[Term.Param]): (Boolean, Boolean) = {
    if (params.nonEmpty) {
      val mods = params.head.mods
      val isImpl = mods.exists(_.isInstanceOf[Mod.Implicit])
      val isUsing = mods.exists(_.isInstanceOf[Mod.Using])
      (isImpl, isUsing)
    } else (false, false)
  }

  /**
    * Computes the `Arity` for a given constructor's primary definition (`Ctor.Primary`) by analyzing its parameter
    * lists.
    *
    * @param c
    *   A primary constructor (`Ctor.Primary`) whose parameter structure is to be analyzed.
    * @return
    *   An `Arity` object containing detailed metrics about the parameter lists and parameters of the given constructor.
    */
  def forCtorPrimary(c: Ctor.Primary): Arity =
    arityOfParamLists(paramsOfPrimary(c))

  /**
    * Computes the `Arity` for a given secondary constructor (`Ctor.Secondary`) by analyzing its parameter lists.
    *
    * @param c
    *   A secondary constructor (`Ctor.Secondary`) whose parameter structure is to be analyzed.
    * @return
    *   An `Arity` object containing detailed metrics about the parameter lists and parameters of the given constructor.
    */
  def forCtorSecondary(c: Ctor.Secondary): Arity =
    arityOfParamLists(paramsOfSecondary(c))

  /**
    * Analyzes the given source code to extract information about method definitions and constructors, returning a list
    * of parameter items with associated metadata.
    *
    * @param code
    *   The source code to be analyzed, provided as a string.
    *
    * @param dialect
    *   The dialect of Scala to be used for parsing the source code. Defaults to Scala 2.12.
    *
    * @return
    *   A list of `ParameterItem` objects, each representing a parameter or constructor item found in the source code,
    *   along with its metadata such as name, kind, position, and arity details.
    */
  def analyzeSource(code: String, dialect: Dialect = dialects.Scala212): List[ParameterItem] = {
    val parsed = dialect(code).parse[Source].get
    val buf = scala.collection.mutable.ListBuffer.empty[ParameterItem]

    /**
      * The `Collect` object extends `TraverserCompat` and is used to traverse and collect specific information from a
      * tree structure of Scala source code.
      *
      * This object overrides the `apply` method to handle different types of syntax constructs (`Defn.Def`,
      * `Ctor.Primary`, and `Ctor.Secondary`) encountered during traversal. For each matched tree node, it extracts
      * relevant information and appends it to an internal buffer in the form of `ParameterItem` instances.
      *
      *   - If the tree node is a `Defn.Def`, it represents a method definition.
      *   - If the tree node is a `Ctor.Primary`, it represents a primary constructor.
      *   - If the tree node is a `Ctor.Secondary`, it represents a secondary constructor.
      *
      * For all other nodes, traversal proceeds through the child nodes.
      */
    object Collect extends TraverserCompat {
      override def apply(tree: Tree): Unit = tree match {
        case d: Defn.Def =>
          buf += ParameterItem(d.name.value, "def", d.pos, forDef(d)); traverseChildren(tree)
        case c: Ctor.Primary =>
          buf += ParameterItem("<init>", "ctor-primary", c.pos, forCtorPrimary(c)); traverseChildren(tree)
        case c: Ctor.Secondary =>
          buf += ParameterItem("<init>", "ctor-secondary", c.pos, forCtorSecondary(c)); traverseChildren(tree)
        case _ => traverseChildren(tree)
      }
    }

    Collect(parsed)
    buf.toList
  }

  /**
    * Extracts the parameter lists of a given method definition (`Defn.Def`) as `List[List[Term.Param]]`
    *
    * @param d
    *   A method definition (`Defn.Def`) whose parameter lists are to be extracted.
    * @return
    *   A nested list of parameters (`List[List[Term.Param]]`) where each inner list represents one parameter list of
    *   the method.
    */
  def paramsOf(d: Defn.Def): List[List[Term.Param]] = d match {
    // ≥ 4.6: params live in a ParamClauseGroup
    case Defn.Def.After_4_6_0(_, _, pgOpt, _, _) =>
      pgOpt
        .map(_.paramClauses.collect { case Term.ParamClause(params, _) => params })
        .getOrElse(Nil)

    // < 4.6: legacy paramss
    case Defn.Def.Initial(_, _, _, paramss, _, _) =>
      paramss

    // single-version safety net
    case Defn.Def(_, _, _, paramss: List[List[Term.Param]] @unchecked, _, _) =>
      paramss
  }

  /**
    * Extracts the type parameters of a given method definition (`Defn.Def`) as a list of `Type.Param`.
    *
    * @param d
    *   A method definition (`Defn.Def`) whose type parameters are to be extracted.
    * @return
    *   A list of type parameters (`List[Type.Param]`) associated with the given method definition.
    */
  def tparamsOf(d: Defn.Def): List[Type.Param] = d match {
    case Defn.Def.After_4_6_0(_, _, pgOpt, _, _) =>
      // NOTE: tparamClause is a value (not Option) on 4.14.1
      pgOpt.map(_.tparamClause.values).getOrElse(Nil)
    case _ => Nil
  }

  /**
    * Counts the number of parameter clauses in the given method definition (`Defn.Def`) that contain parameters with
    * the `using` modifier.
    *
    * @param d
    *   A method definition (`Defn.Def`) whose parameter clauses are to be analyzed.
    * @return
    *   The number of parameter clauses containing `using` parameters.
    */
  def usingParamListCount(d: Defn.Def): Int = d match {
    case Defn.Def.After_4_6_0(_, _, pgOpt, _, _) =>
      pgOpt
        .map { pg =>
          pg.paramClauses.count { case Term.ParamClause(_, clauseMods) =>
            clauseMods.exists(_.is[Mod.Using])
          }
        }
        .getOrElse(0)
    case _ => 0
  }

  /**
    * Extracts the parameter lists of the given primary constructor.
    *
    * @param c
    *   A primary constructor (`Ctor.Primary`) whose parameter lists are to be extracted.
    * @return
    *   A nested list of parameters (`List[List[Term.Param]]`) where each inner list represents one parameter list of
    *   the primary constructor. If the constructor does not match the expected pattern, an empty list is returned.
    */
  private def paramsOfPrimary(c: Ctor.Primary): List[List[Term.Param]] = c match {
    case Ctor.Primary.Initial(_, _, paramss) => paramss
    case _                                   => Nil
  }

  /**
    * Extracts the parameter lists of the given secondary constructor.
    *
    * @param c
    *   A secondary constructor (`Ctor.Secondary`) whose parameter lists are to be analyzed.
    *
    * @return
    *   A nested list of parameters (`List[List[Term.Param]]`) where each inner list represents one parameter list of
    *   the secondary constructor. If the constructor does not match the expected pattern, an empty list is returned.
    */
  private def paramsOfSecondary(c: Ctor.Secondary): List[List[Term.Param]] = c match {
    case Ctor.Secondary.Initial(_, _, paramss, _, _) => paramss
    case _                                           => Nil
  }

}
