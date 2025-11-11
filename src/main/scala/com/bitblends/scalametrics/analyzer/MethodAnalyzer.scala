/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer

import com.bitblends.scalametrics.analyzer.TypeInfer._
import com.bitblends.scalametrics.analyzer.model._
import com.bitblends.scalametrics.metrics._
import com.bitblends.scalametrics.metrics.model.{
  BranchDensityMetrics,
  InlineAndImplicitMetrics,
  Metadata,
  MethodMetrics,
  PatternMatchingMetrics,
  ParameterMetrics => ParamMetrics
}
import com.bitblends.scalametrics.utils.Util
import com.bitblends.scalametrics.utils.Util._

import scala.meta._

/**
  * Object for analyzing methods within an abstract syntax tree (AST). This includes extracting detailed metrics for
  * methods such as lines of code, cyclomatic complexity, nesting depth, and other method-level characteristics.
  */
object MethodAnalyzer extends Analyzer {

  /**
    * Provides the name of the analyzer. This is used to identify the analyzer when it is registered with the analyzer.
    *
    * @return
    *   A string representing the name of the analyzer.
    */
  override def name: String = "method"

  /**
    * Analyzes a given Scala source file's Abstract Syntax Tree (AST) to extract method-level metrics.
    *
    * The analysis includes various metrics such as method name, signature, access modifier, lines of code, cyclomatic
    * complexity, nesting depth, parameter metrics, pattern matching metrics, and branch density metrics.
    *
    * @param ctx
    *   AnalysisCtx containing the parsed Scala source file's Abstract Syntax Tree (AST) and metadata.
    * @return
    *   Updated AnalysisCtx containing computed method-level metrics.
    */
  override def run(ctx: AnalysisCtx): AnalysisCtx = {
    val allTokens = ctx.tree.tokens
    val fid = ctx.fileId.getOrElse(sys.error("MethodAnalyzer: fileId not set"))
    val s0 = ScopeCtx()

    /**
      * Executes a computation within a modified `ScopeCtx` by pushing an `Owner` to the context and restoring the
      * original context after execution.
      *
      * @param s
      *   The initial scope context (`ScopeCtx`) that represents the current state of the analysis.
      * @param owner
      *   The owner being pushed to the context, typically representing the current method or scope.
      * @param f
      *   A function taking a modified `ScopeCtx` and returning a tuple consisting of an updated `ScopeCtx` and a vector
      *   of `MethodMetrics`.
      * @return
      *   A tuple of the restored `ScopeCtx` (with the added owner removed) and a vector of `MethodMetrics` produced by
      *   the computation.
      */
    def inOwner(s: ScopeCtx, owner: Owner)(
        f: ScopeCtx => (ScopeCtx, Vector[MethodMetrics])
    ): (ScopeCtx, Vector[MethodMetrics]) = {
      val s1 = s.push(owner)
      val (s2, metrics) = f(s1)
      val sPopped = s2.copy(owners = s.owners)
      (sPopped, metrics)
    }

    /**
      * Recursively visits a list of `Tree` elements within the context of a given `ScopeCtx` and computes method-level
      * metrics for each element in the list.
      *
      * @param ts
      *   A list of `Tree` elements to visit. Each `Tree` element represents a part of the Scala Abstract Syntax Tree
      *   (AST) to analyze.
      * @param s
      *   The initial `ScopeCtx`, representing the current state of the analysis scope, used for contextual tracking
      *   during the traversal and metrics computation.
      * @return
      *   A tuple containing:
      *   - An updated `ScopeCtx` after visiting all `Tree` elements and applying any modifications resulting from the
      *     analysis.
      *   - A `Vector` of `MethodMetrics` containing the computed metrics for each method encountered during the
      *     traversal.
      */
    def visitList(ts: List[Tree], s: ScopeCtx): (ScopeCtx, Vector[MethodMetrics]) =
      ts.foldLeft((s, Vector.empty[MethodMetrics])) { case ((st, acc), t) =>
        val (st2, ms) = visit(t, st)
        (st2, acc ++ ms)
      }

    /**
      * Analyzes a Scala method definition to compute various metrics related to its structure, such as cyclomatic
      * complexity, nesting depth, parameter details, return type explicitness, branch density, and pattern matching
      * metrics. It also updates the current scope context after processing the method.
      *
      * @param d
      *   the method definition (Defn.Def) to be analyzed
      * @param s
      *   the current scope context representing the state during analysis
      * @param fid
      *   a string identifier for the file that contains the method definition
      * @param allTokens
      *   all tokens from the source file where the method exists
      * @return
      *   a tuple containing the updated scope context and a vector of method metrics
      */
    def analyzeDefn(d: Defn.Def, s: ScopeCtx, fid: String, allTokens: Tokens): (ScopeCtx, Vector[MethodMetrics]) = {
      val deprecated = Util.isDeprecated(d.mods, d, allTokens)
      val hasScaladoc = Util.hasScaladocComment(d, allTokens)
      val rtExplicitness = ReturnTypeExplicitness.forDef(d)
      val access = accessOf(d.mods, s.isNestedLocal)
      val parentMember = s.owners.collectFirst { case MemberOwner(k, n) => s"$k $n" }
      val loc = Util.locOf(d)
      val cc = d.body match { case t: Term => Cyclomatic.compute(t); case _ => 1 }
      val nestDepth = d.body match { case t: Term => NestingDepth.compute(t); case _ => 0 }
      val arity = Parameter.forDef(d)
      val pmMetrics = d.body match {
        case t: Term => PatternMatching.compute(t)
        case _       => PatternMatchingMetrics()
      }
      val bdMetricsResult = d.body match {
        case t: Term => ExpressionBranchDensity.compute(t)
        case _       => BranchDensityMetrics()
      }
      val inlineAndImplicits = InlineAndImplicits.forDef(d)
      val fullName = {
        val p = s.qualifiedPrefix
        if (p.isEmpty) d.name.value else s"$p.${d.name.value}"
      }

      val mm = MethodMetrics(
        metadata = Metadata(
          fileId = fid,
          name = fullName,
          signature = renderSignature(d),
          accessModifier = access,
          linesOfCode = loc,
          isDeprecated = deprecated,
          isNested = s.isNestedLocal,
          declarationType = "def",
          parentMember = parentMember
        ),
        cComplexity = cc,
        nestingDepth = nestDepth,
        hasScaladoc = hasScaladoc,
        parameterMetrics = ParamMetrics(
          totalParams = arity.totalParams,
          paramLists = arity.paramLists,
          implicitParamLists = arity.implicitParamLists,
          usingParamLists = arity.usingParamLists,
          implicitParams = arity.implicitParams,
          usingParams = arity.usingParams,
          defaultedParams = arity.defaultedParams,
          byNameParams = arity.byNameParams,
          varargParams = arity.varargParams
        ),
        inlineAndImplicitMetrics = InlineAndImplicitMetrics(
          hasInlineModifier = inlineAndImplicits.isInlineMethod,
          inlineParamCount = Some(arity.inlineParams),
          isImplicitConversion = inlineAndImplicits.hasImplicitConversion,
          isImplicit = inlineAndImplicits.hasImplicitMod,
          isAbstract = false,
          hasExplicitReturnType = rtExplicitness.hasExplicitReturnType,
          inferredReturnType = rtExplicitness.inferredReturnType
        ),
        pmMetrics = PatternMatchingMetrics(
          matches = pmMetrics.matches,
          cases = pmMetrics.cases,
          guards = pmMetrics.guards,
          wildcards = pmMetrics.wildcards,
          maxNesting = pmMetrics.maxNesting,
          nestedMatches = pmMetrics.nestedMatches,
          avgCasesPerMatch = if (pmMetrics.matches == 0) 0.0 else pmMetrics.cases.toDouble / pmMetrics.matches,
          matchCases = Nil
        ),
        bdMetrics = BranchDensityMetrics(
          branches = bdMetricsResult.branches,
          ifCount = bdMetricsResult.ifCount,
          caseCount = bdMetricsResult.caseCount,
          loopCount = bdMetricsResult.loopCount,
          catchCaseCount = bdMetricsResult.catchCaseCount,
          boolOpsCount = bdMetricsResult.boolOpsCount,
          densityPer100 = if (loc == 0) 0.0 else 100.0 * bdMetricsResult.branches.toDouble / loc,
          boolOpsPer100 = if (loc == 0) 0.0 else 100.0 * bdMetricsResult.boolOpsCount.toDouble / loc
        )
      )

      val (s2, inner) = inOwner(s, DefOwner(d.name.value)) { ss => visit(d.body, ss) }
      (s2, mm +: inner)
    }

    /**
      * Analyzes a Scala method declaration to compute various metrics and update the scope context.
      *
      * This analysis includes determining the method's arity, access modifiers, documentation presence, location, and
      * other characteristics like implicit or inline parameters and parent member details.
      *
      * @param d
      *   The method declaration (`Decl.Def`) to be analyzed, containing information about the method's name,
      *   parameters, return type, and modifiers.
      * @param s
      *   The current scope context (`ScopeCtx`), used for context-aware computations such as determining nesting levels
      *   and qualified names.
      * @param fid
      *   A string identifier for the source file containing the method declaration.
      * @param allTokens
      *   All tokens (`Tokens`) from the parsed source file that can be referenced to extract additional information
      *   about the method, such as annotations or documentation.
      * @return
      *   A tuple where:
      *   - The first element is the updated scope context (`ScopeCtx`) reflecting the analyzed method.
      *   - The second element is a vector of `MethodMetrics` containing computed metrics for the analyzed method.
      */
    def analyzeDecl(d: Decl.Def, s: ScopeCtx, fid: String, allTokens: Tokens): (ScopeCtx, Vector[MethodMetrics]) = {
      val arity: Arity = Parameter.forDecl(d)
      val deprecated = Util.isDeprecated(d.mods, d, allTokens)
      val access = accessOf(d.mods, s.isNestedLocal)
      val hasDoc = hasScaladocComment(d, allTokens)
      val loc = locOf(d)
      val parentMember = s.owners.collectFirst { case MemberOwner(k, n) => s"$k $n" }
      val inlineAndImplicits = InlineAndImplicits.forDecl(d)
      val fullName = {
        val p = s.qualifiedPrefix
        if (p.isEmpty) d.name.value else s"$p.${d.name.value}"
      }

      val mm = MethodMetrics(
        metadata = Metadata(
          fileId = fid,
          name = fullName,
          signature = renderSignatureDecl(d),
          accessModifier = access,
          linesOfCode = loc,
          isDeprecated = deprecated,
          isNested = s.isNestedLocal,
          declarationType = "def",
          parentMember = parentMember
        ),
        cComplexity = 0,
        nestingDepth = s.defDepth,
        hasScaladoc = hasDoc,
        parameterMetrics = ParamMetrics(
          totalParams = arity.totalParams,
          paramLists = arity.paramLists,
          implicitParamLists = arity.implicitParamLists,
          usingParamLists = arity.usingParamLists,
          implicitParams = arity.implicitParams,
          usingParams = arity.usingParams,
          defaultedParams = arity.defaultedParams,
          byNameParams = arity.byNameParams,
          varargParams = arity.varargParams
        ),
        inlineAndImplicitMetrics = InlineAndImplicitMetrics(
          hasInlineModifier = inlineAndImplicits.isInlineMethod,
          inlineParamCount = Some(arity.inlineParams),
          isImplicitConversion = inlineAndImplicits.hasImplicitConversion,
          isImplicit = inlineAndImplicits.hasImplicitMod,
          isAbstract = true,
          hasExplicitReturnType = true,
          inferredReturnType = None
        ),
        pmMetrics = PatternMatchingMetrics(),
        bdMetrics = BranchDensityMetrics()
      )
      (s, Vector(mm))
    }

    /**
      * Visits a `Defn.Val` node in the Abstract Syntax Tree (AST) to analyze its right-hand side expression within the
      * context of the given `ScopeCtx`.
      *
      * @param d
      *   The value definition (`Defn.Val`) node to visit and analyze.
      * @param s
      *   The current scope context (`ScopeCtx`) that provides information about the analysis state.
      * @return
      *   A tuple containing the updated scope context and a vector of method-level metrics (`Vector[MethodMetrics]`)
      *   extracted from analyzing the right-hand side expression of the value definition.
      */
    def visitVal(d: Defn.Val, s: ScopeCtx): (ScopeCtx, Vector[MethodMetrics]) = {
      val nm = d.pats.map(_.syntax).mkString(", ")
      val rhsT = Option(d.rhs).collect { case t: Term => t }
      rhsT
        .map { t => inOwner(s, MemberOwner("val", nm)) { ss => visit(t, ss) } }
        .getOrElse((s, Vector.empty))
    }

    /**
      * Visits a `Defn.Var` node in the Abstract Syntax Tree (AST) to analyze its right-hand side expression within the
      * context of the given `ScopeCtx`.
      *
      * @param d
      *   The variable definition (`Defn.Var`) node to visit and analyze.
      * @param s
      *   The current scope context (`ScopeCtx`) that provides information about the analysis state.
      * @return
      *   A tuple containing the updated scope context and a vector of method-level metrics (`Vector[MethodMetrics]`)
      *   extracted from analyzing the right-hand side expression of the variable definition.
      */
    def visitVar(d: Defn.Var, s: ScopeCtx): (ScopeCtx, Vector[MethodMetrics]) = {
      val nm = d.pats.map(_.syntax).mkString(", ")
      val rhsT = d.rhs.collect { case t: Term => t }
      rhsT
        .map { t => inOwner(s, MemberOwner("var", nm)) { ss => visit(t, ss) } }
        .getOrElse((s, Vector.empty))
    }

    /**
      * Processes a syntax tree recursively and collects metrics on the definitions encountered, while also updating the
      * contextual scope throughout the traversal.
      *
      * @param tree
      *   The syntax tree to process.
      * @param s
      *   The current scope context, representing the state of ownership, nesting, and other relevant metadata required
      *   during the tree traversal.
      * @return
      *   A tuple containing the updated scope context and a vector of `MethodMetrics`, which represent the metrics
      *   collected for methods within the tree.
      */
    def visit(tree: Tree, s: ScopeCtx): (ScopeCtx, Vector[MethodMetrics]) = tree match {
      case Pkg(ref, stats) =>
        inOwner(s, PkgOwner(ref.toString)) { ss => visitList(stats, ss) }
      // Top-level objects, classes, and traits
      case Defn.Object(_, name, templ) =>
        inOwner(s, ObjOwner(name.value)) { ss => visit(templ, ss) }
      // Classes
      case Defn.Class(_, name, _, _, templ) =>
        inOwner(s, ClsOwner(name.value)) { ss => visit(templ, ss) }
      // Traits
      case Defn.Trait(_, name, _, _, templ) =>
        inOwner(s, TrtOwner(name.value)) { ss => visit(templ, ss) }
      // Template bodies
      case Template(_, _, _, stats) =>
        inOwner(s, TplOwner()) { ss => visitList(Option(stats).getOrElse(Nil), ss) }
      // Blocks
      case Term.Block(stats) =>
        val next = s.copy(blockId = s.blockId + 1)
        inOwner(next, BlkOwner(next.blockId)) { ss => visitList(stats, ss) }
      // Lambdas
      case Term.Function(_, body) =>
        val next = s.copy(lambdaId = s.lambdaId + 1)
        inOwner(next, LamOwner(next.lambdaId)) { ss => visit(body, ss) }
      // Value definitions
      case d @ Defn.Val(_, _, _, _) => visitVal(d, s)
      // Variable definitions
      case d @ Defn.Var(_, _, _, _) => visitVar(d, s)
      // Abstract declaration
      case d @ Decl.Def(_, _, _, _, _) => analyzeDecl(d, s, fid, allTokens)
      // Concrete definition
      case d @ Defn.Def(_, _, _, _, _, _) => analyzeDefn(d, s, fid, allTokens)
      // Default case: visit children
      case other => visitList(other.children, s)
    }

    val (_, methods) = visit(ctx.tree, s0)
    ctx.addMethods(methods)

  }

  /**
    * Renders the signature of a Scala method, including its name, type parameters, parameter clauses, and return type.
    *
    * @param d
    *   The method definition (`Defn.Def`) for which the signature is being rendered.
    * @return
    *   A string representing the method's signature, including its name, type parameters, formatted parameter clauses,
    *   and inferred or explicitly declared return type.
    */
  private def renderSignature(d: Defn.Def): String = {
    val typeParams = d match {
      case Defn.Def.After_4_6_0(_, _, pgOpt, _, _) =>
        pgOpt.map(_.tparamClause.values).getOrElse(Nil) match {
          case Nil => ""
          case tps => s"[${tps.map(_.syntax).mkString(", ")}]"
        }
      case Defn.Def.Initial(_, _, tparams, _, _, _) =>
        if (tparams.nonEmpty) s"[${tparams.map(_.syntax).mkString(", ")}]" else ""
    }
    val clauses = clausesOf(d).map(renderClause).mkString
    val explicitRt = d.decltpe.map(_.syntax)
    val inferredRt = explicitRt.orElse(d.body match { case t: Term => inferSimpleType(t); case _ => None })
    val rt = inferredRt.map(t => s": $t").getOrElse("")
    s"${d.name.value}$typeParams$clauses$rt"
  }

  /**
    * Extracts parameter clauses from a given method definition and represents them in a uniform structure.
    *
    * This function processes both modern parameter clause structures (introduced in Scala 3.0.0 and improved in Scala
    * 4.6.0) and legacy structures (pre-Scala 4.6). For each parameter clause, it captures a list of parameters along
    * with flags indicating whether the clause is a `using` or `implicit` clause.
    *
    * @param d
    *   The method definition (`Defn.Def`) from which the parameter clauses are to be extracted. This includes both
    *   modern and legacy shapes of method definitions.
    * @return
    *   A list of uniform parameter clause representations (`List[UClause]`). Each `UClause` contains:
    *   - A list of parameters (`List[Term.Param]`).
    *   - A flag indicating if the clause is a `using` clause (`isUsing`).
    *   - A flag indicating if the clause is an `implicit` clause (`isImplicit`).
    */
  private def clausesOf(d: Defn.Def): List[UClause] = d match {
    // 4.6+ shape (works on 4.14.1)
    case Defn.Def.After_4_6_0(_, _, pgOpt, _, _) =>
      pgOpt.toList.flatMap(_.paramClauses).map { case Term.ParamClause(params, clauseMods) =>
        val using = clauseMods.exists(_.is[Mod.Using]) ||
          params.headOption.exists(_.mods.exists(_.is[Mod.Using]))
        val impl = params.headOption.exists(_.mods.exists(_.is[Mod.Implicit]))
        UClause(params, isUsing = using, isImplicit = impl)
      }
    // legacy shape (pre-4.6); no ‘using’ concept here
    case Defn.Def.Initial(_, _, _, paramss, _, _) =>
      paramss.map { ps =>
        val impl = ps.headOption.exists(_.mods.exists(_.is[Mod.Implicit]))
        UClause(ps, isUsing = false, isImplicit = impl)
      }
  }

  /**
    * Renders the signature of a Scala method declaration, including its name, type parameters, parameter clauses, and
    * return type.
    *
    * @param d
    *   The method declaration (`Decl.Def`) from which the signature is being constructed.
    * @return
    *   A string representation of the method's signature, consisting of its name, type parameters, formatted parameter
    *   clauses, and declared return type.
    */
  private def renderSignatureDecl(d: Decl.Def): String = {
    val typeParams = d match {
      case Decl.Def.After_4_6_0(_, _, pgOpt, _) =>
        pgOpt.map(_.tparamClause.values).getOrElse(Nil) match {
          case Nil => ""
          case tps => s"[${tps.map(_.syntax).mkString(", ")}]"
        }
      case Decl.Def.Initial(_, _, tparams, _, _) =>
        if (tparams.nonEmpty) s"[${tparams.map(_.syntax).mkString(", ")}]" else ""
    }
    val clauses = declClausesOf(d).map(renderClause).mkString
    s"${d.name.value}$typeParams$clauses: ${d.decltpe.syntax}"
  }

  /**
    * Renders a string representation of a parameter clause for a method or declaration.
    *
    * The clause includes modifiers such as `using` or `implicit` (if applicable) and formats the parameters with their
    * names and types.
    *
    * @param c
    *   The parameter clause (`UClause`) to render. A `UClause` contains a list of parameters, as well as flags
    *   indicating if the clause is a `using` or `implicit` clause.
    * @return
    *   A string representation of the parameter clause, formatted as `(prefix param1: Type1, param2: Type2, ...)`. An
    *   empty parameter list results in an empty clause, e.g., `()`.
    */
  private def renderClause(c: UClause): String = {
    val prefix = List(
      c.isUsing -> "using ",
      c.isImplicit -> "implicit "
    ).collectFirst { case (true, pfx) => pfx }.getOrElse("")
    val body = c.params
      .map(p => s"${p.name.value}: ${p.decltpe.fold("_")(_.syntax)}")
      .mkString(", ")
    s"(${prefix}${body})"
  }

  /**
    * Extracts parameter clauses from a given method declaration and represents them in a uniform structure.
    *
    * This method processes both modern and legacy shapes of parameter clauses in method declarations. It captures the
    * list of parameters and determines whether each clause is a `using` clause or an `implicit` clause.
    *
    * @param d
    *   The method declaration (`Decl.Def`) from which the parameter clauses are to be extracted. This includes both
    *   modern parameter clause structures and legacy structures.
    * @return
    *   A list of uniform parameter clause representations (`List[UClause]`). Each `UClause` consists of:
    *   - A list of parameters (`List[Term.Param]`).
    *   - A flag (`isUsing`) indicating if the clause is a `using` clause.
    *   - A flag (`isImplicit`) indicating if the clause is an `implicit` clause.
    */
  private def declClausesOf(d: Decl.Def): List[UClause] = d match {
    // ≥ 4.6 normalized shape (works on 4.14.1)
    case Decl.Def.After_4_6_0(_, _, pgOpt, _) =>
      pgOpt.toList.flatMap(_.paramClauses).map { case Term.ParamClause(params, clauseMods) =>
        val using = clauseMods.exists(_.is[Mod.Using]) ||
          params.headOption.exists(_.mods.exists(_.is[Mod.Using])) // per-param using (Scala 3)
        val impl = params.headOption.exists(_.mods.exists(_.is[Mod.Implicit])) // Scala 2
        UClause(params, isUsing = using, isImplicit = impl)
      }

    // Legacy shape for older scalameta (for source compatibility)
    case Decl.Def.Initial(_, _, _, paramss, _) =>
      paramss.map { ps =>
        val impl = ps.headOption.exists(_.mods.exists(_.is[Mod.Implicit]))
        UClause(ps, isUsing = false, isImplicit = impl)
      }
  }

}
