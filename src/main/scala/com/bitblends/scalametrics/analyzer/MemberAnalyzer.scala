/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer

import com.bitblends.scalametrics.analyzer.TypeInfer._
import com.bitblends.scalametrics.analyzer.model._
import com.bitblends.scalametrics.metrics._
import com.bitblends.scalametrics.metrics.model.{
  MemberMetrics,
  Metadata,
  PatternMatchingMetrics,
  BranchDensityMetrics,
  InlineAndImplicitMetrics => InlineImplicitsMetrics
}
import com.bitblends.scalametrics.utils.Util._

import scala.annotation.tailrec
import scala.meta._

/**
  * Analyzes syntax trees to compute member-related metrics, such as complexity, nesting, pattern matching, and branch
  * density. This object extends the `Analyzer` class to process program members, including classes, traits, objects,
  * functions, and variable declarations. Results are annotated within the provided analysis context.
  */
object MemberAnalyzer extends Analyzer {

  /**
    * Provides the name of the member analyzer.
    *
    * @return
    *   A string representing the name of the member analyzer.
    */
  override def name: String = "member"

  /**
    * Analyzes the syntax tree of a program for member-related metrics and updates the analysis context with the
    * gathered data. This includes examining classes, traits, objects, functions, blocks, and member definitions such as
    * `val`, `var`, and `type` declarations.
    *
    * @param ctx
    *   The analysis context containing the syntax tree to process and associated metadata such as file information.
    *   This context will be used to extract relevant tokens, maintain state during traversal, and store processing
    *   results.
    * @return
    *   The updated analysis context enriched with computed member metrics, such as cyclomatic complexity, branch
    *   density, nesting depth, pattern matching metrics, and additional metadata for members, including access
    *   modifiers, qualified names, and presence of Scaladoc.
    */
  override def run(ctx: AnalysisCtx): AnalysisCtx = {
    val allTokens = ctx.tree.tokens
    val fid = ctx.fileId.getOrElse(sys.error("MemberAnalyzer: fileId not set"))
    val s0 = ScopeCtx()

    /**
      * Constructs the signature for a val or var declaration by combining its name with an explicit or inferred type.
      *
      * @param name
      *   The name of the declared val or var.
      * @param decltpe
      *   An optional explicit type associated with the declaration.
      * @param rhs
      *   An optional right-hand side term used for type inference if no explicit type is provided.
      * @return
      *   The signature of the declaration, consisting of the name and either the explicit or inferred type, or just the
      *   name if no type can be determined.
      */
    def signatureOfValVar(name: String, decltpe: Option[Type], rhs: Option[Term]): String = {
      decltpe match {
        case Some(tp) => s"$name: ${tp.syntax}"
        case None     =>
          val inferred = rhs.flatMap(inferSimpleType)
          inferred.map(t => s"$name: $t").getOrElse(name) // fall back to just the name
      }
    }

    /**
      * Analyzes the given term to compute various metrics, including cyclomatic complexity, nesting depth, pattern
      * matching metrics, and branch density metrics.
      *
      * @param t
      *   The term to be analyzed. Represents a fragment of the abstract syntax tree (AST) for which the metrics need to
      *   be calculated.
      * @return
      *   A tuple containing:
      *   - Cyclomatic complexity as an Int.
      *   - Nesting depth as an Int.
      *   - Pattern matching metrics as a `PatternMatchingMetrics` object.
      *   - Branch density metrics as a `BranchDensityMetrics` object.
      */
    def analyzeTerm(t: Term): (Int, Int, PatternMatchingMetrics, BranchDensityMetrics) = {
      val cc = Cyclomatic.compute(t)
      val nestingDepth = NestingDepth.compute(t)
      val pm = PatternMatching.compute(t)
      val bd = ExpressionBranchDensity.compute(t)
      (cc, nestingDepth, pm, bd)
    }

    /**
      * Processes a list of stats (class/object/trait) and extracts term-related elements. If multiple terms are
      * present, they are grouped into a block.
      *
      * @param stats
      *   A list of stats from which terms will be extracted.
      * @return
      *   An optional term. Returns `None` if no terms are found, a single term if exactly one is present, or a block of
      *   terms if there are multiple terms.
      */
    def termFromTemplateStats(stats: List[Stat]): Option[Term] = {
      val terms = stats.collect { case t: Term => t }
      if (terms.isEmpty) None
      else if (terms.length == 1) Some(terms.head)
      else Some(Term.Block(terms))
    }

    /**
      * Extracts the last type name from a given type.
      *
      * This method recursively traverses the structure of the input type to determine and return the relevant type
      * name. It handles various type constructs such as `Type.Name`, `Type.Select`, `Type.Project`, and others.
      *
      * @param t
      *   The type from which to extract the last type name.
      * @return
      *   A string representing the last type name.
      */
    @tailrec
    def lastTypeName(t: Type): String = t match {
      // Given Conversion? (for Scala 3)
      case Type.Name(n)                  => n
      case Type.Select(_, Type.Name(n))  => n
      case Type.Project(_, Type.Name(n)) => n
      case Type.Apply(tpe, _)            => lastTypeName(tpe)
      case Type.Annotate(tpe, _)         => lastTypeName(tpe)
      case other                         => other.syntax
    }

    /**
      * Determines if the given type represents a conversion type by checking its name.
      *
      * @param t
      *   The type to be checked.
      * @return
      *   True if the type is identified as "Conversion" or "scala.Conversion", false otherwise.
      */
    def isConversionType(t: Type): Boolean =
      lastTypeName(t) == "Conversion" || lastTypeName(t) == "scala.Conversion"

    /**
      * Extracts a string representation of a `Name` instance, specifically handling anonymous given names.
      *
      * @param n
      *   The `Name` instance from which the string representation should be extracted.
      * @return
      *   A string corresponding to the provided `Name`. If the name is anonymous, returns "<given>". Otherwise, returns
      *   the value of the name.
      */
    def givenNameString(n: Name): String = n match {
      case Name.Anonymous() => "<given>" // anonymous given
      case other            => other.value // e.g. Term.Name("showInt"), Name.Indeterminate("x")
    }

    /**
      * Builds a MemberMetrics instance for a given member definition.
      *
      * @param s
      *   The current state of the analysis, including context such as qualified prefix and nesting information.
      * @param kind
      *   The kind of member being analyzed (e.g., "class", "object", "val", "var", "type", "given").
      * @param name
      *   The name of the member.
      * @param signature
      *   The signature of the member, including its name and type information.
      * @param mods
      *   A list of modifiers applied to the member (e.g., access modifiers, implicit/given modifiers).
      * @param defn
      *   The syntax tree node representing the member definition.
      * @param rhsTerm
      *   An optional term representing the right-hand side of the member definition, used for complexity analysis.
      * @param givenFlags
      *   A tuple indicating whether the member is a given instance and/or a given conversion (Scala 3).
      * @param abstractMetrics
      *   Optional return type information for abstract members (e.g., vals/vars with inferred types).
      * @return
      *   A MemberMetrics instance containing computed metrics and metadata for the specified member.
      */
    def buildMemberMetric(
        s: ScopeCtx,
        kind: String,
        name: String,
        signature: String,
        mods: List[Mod],
        defn: Tree,
        rhsTerm: Option[Term],
        givenFlags: (Boolean, Boolean),
        abstractMetrics: Option[ReturnTypeItem] = None
    ): MemberMetrics = {
      val access = accessOf(mods, s.isNestedLocal)
      val hasScaladoc = hasScaladocComment(defn, allTokens)
      val loc = locOf(defn)
      val deprecated = isDeprecated(mods, defn, allTokens)
      val isImplicit = hasImplicitModifier(mods)
      val isInline = hasInlineModifier(mods)
      val (cc, nestingDepth, pm, bd) =
        rhsTerm
          .map(analyzeTerm)
          .getOrElse((1, 0, PatternMatchingMetrics(), BranchDensityMetrics()))
      val fullName = {
        val q = s.qualifiedPrefix
        if (q.isEmpty) name else s"$q.$name"
      }

      MemberMetrics(
        metadata = Metadata(
          fileId = fid,
          name = fullName,
          signature = signature,
          accessModifier = access,
          linesOfCode = loc,
          isDeprecated = deprecated,
          isNested = false,
          declarationType = kind
        ),
        cComplexity = cc,
        nestingDepth = nestingDepth,
        hasScaladoc = hasScaladoc,
        inlineAndImplicitMetrics = InlineImplicitsMetrics(
          hasInlineModifier = isInline,
          isImplicitConversion = false,
          isImplicit = isImplicit,
          isAbstract = abstractMetrics.exists(_.inferredReturnType.nonEmpty),
          hasExplicitReturnType = abstractMetrics.exists(_.hasExplicitReturnType),
          inferredReturnType = abstractMetrics.flatMap(_.inferredReturnType),
          isGivenInstance = Some(givenFlags._1),
          isGivenConversion = Some(givenFlags._2)
        ),
        pmMetrics = PatternMatchingMetrics(
          matches = pm.matches,
          cases = pm.cases,
          guards = pm.guards,
          wildcards = pm.wildcards,
          maxNesting = pm.maxNesting,
          nestedMatches = pm.nestedMatches,
          avgCasesPerMatch = if (pm.matches == 0) 0.0 else pm.cases.toDouble / pm.matches,
          matchCases = Nil
        ),
        bdMetrics = BranchDensityMetrics(
          loc = loc,
          branches = bd.branches,
          ifCount = bd.ifCount,
          caseCount = bd.caseCount,
          loopCount = bd.loopCount,
          catchCaseCount = bd.catchCaseCount,
          boolOpsCount = bd.boolOpsCount,
          densityPer100 = if (loc == 0) 0.0 else 100.0 * bd.branches.toDouble / loc,
          boolOpsPer100 = if (loc == 0) 0.0 else 100.0 * bd.boolOpsCount.toDouble / loc
        )
      )

    }

    /**
      * Modifies the current scope context by introducing a new owner into the scope, executes a processing function
      * with this modified context, and then reverts the scope context to its original state afterward.
      *
      * @param s
      *   The initial `ScopeCtx` representing the current scope context before any modifications.
      * @param owner
      *   The `Owner` to be added to the `ScopeCtx`, representing the entity owning the new scope being introduced.
      * @param f
      *   A function that takes the modified `ScopeCtx` as input and returns a tuple containing an updated `ScopeCtx`
      *   and a list of `MemberMetrics` resulting from the processing of the modified context.
      * @return
      *   A tuple where the first element is the restored original `ScopeCtx` after processing, and the second element
      *   is the list of `MemberMetrics` computed during the operation.
      */
    def inOwner(s: ScopeCtx, owner: Owner)(
        f: ScopeCtx => (ScopeCtx, List[MemberMetrics])
    ): (ScopeCtx, List[MemberMetrics]) = {
      val s1 = s.push(owner)
      val (s2, items) = f(s1)
      val sPopped = s2.copy(owners = s.owners)
      (sPopped, items)
    }

    /**
      * Processes a list of syntax trees (`Tree`) and computes metrics for members within those trees while updating the
      * contextual scope model (`ScopeCtx`).
      *
      * @param ts
      *   A list of `Tree` instances representing syntax trees to be analyzed. Each tree in the list corresponds to a
      *   code structure such as a class, object, method, block, or other syntactic constructs.
      * @param s
      *   The initial contextual model represented by `ScopeCtx`. This context is used to track ownership, nested
      *   scopes, and additional metadata that may influence the processing of the trees.
      * @return
      *   A tuple where the first element is the updated `ScopeCtx`, reflecting the state of the scope after processing
      *   the list of trees, and the second element is a list of `MemberMetrics` instances containing computed metrics
      *   for the processed members.
      */
    def visitList(ts: List[Tree], s: ScopeCtx): (ScopeCtx, List[MemberMetrics]) =
      ts.foldLeft((s, List.empty[MemberMetrics])) { case ((st, acc), t) =>
        val (st2, ms) = visit(t, st)
        (st2, acc ::: ms)
      }

    /**
      * Extracts a term from the provided template, if any exists. The term is derived from the statistics (stats) of
      * the template.
      *
      * @param templ
      *   The input template from which the term is to be extracted. The template contains a collection of member
      *   definitions and statements.
      * @return
      *   An optional term derived from the statistics of the template, or None if no term can be extracted.
      */
    def templateTerm(templ: Template): Option[Term] =
      Option(templ.stats).flatMap(termFromTemplateStats)

    /**
      * Extracts metrics for constructor fields in the primary constructor of a class.
      *
      * This method processes the parameters of the primary constructor, identifies those defined as `val` or `var`, and
      * generates metrics for each of these fields. It also gathers information on the field's type, visibility, and
      * modifiers while constructing the appropriate `MemberMetrics` objects.
      *
      * @param s
      *   The scope context (`ScopeCtx`) containing metadata about the analysis scope, such as ownership and block
      *   nesting.
      * @param ctor
      *   The primary constructor node (`Ctor.Primary`) to be analyzed for field parameters.
      * @return
      *   A list of `MemberMetrics` objects representing the metrics for `val` and `var` fields defined in the primary
      *   constructor.
      */
    def extractConstructorFieldMetrics(s: ScopeCtx, ctor: Ctor.Primary): List[MemberMetrics] = {
      val paramss: List[List[Term.Param]] = ctor match {
        case Ctor.Primary(_, _, ps: List[List[Term.Param]] @unchecked) => ps
        case _                                                         => Nil
      }

      paramss.flatten.collect {
        case param
            if param.mods.exists(_.isInstanceOf[Mod.ValParam]) || param.mods.exists(_.isInstanceOf[Mod.VarParam]) =>
          val isVar = param.mods.exists(_.isInstanceOf[Mod.VarParam])
          val kind = if (isVar) "var" else "val"
          val paramName = param.name.value
          val paramType = param.decltpe.map(_.syntax).getOrElse("_")
          val sig = s"$paramName: $paramType"

          val isPublic = !param.mods.exists {
            case _: Mod.Private   => true
            case _: Mod.Protected => true
            case _                => false
          }

          val rtItem = ReturnTypeItem(
            name = paramName,
            kind = kind,
            pos = param.pos,
            isPublic = isPublic,
            hasExplicitReturnType = param.decltpe.isDefined,
            inferredReturnType = param.decltpe.map(_.syntax)
          )

          buildMemberMetric(
            s = s,
            kind = kind,
            name = paramName,
            signature = sig,
            mods = param.mods,
            defn = param,
            rhsTerm = None,
            givenFlags = (false, false),
            abstractMetrics = Some(rtItem)
          )
      }
    }

    /**
      * Handles the analysis of a class definition, extracting metrics for the class and its members and updating the
      * scope context as necessary.
      *
      * @param s
      *   The current scope context representing the state of ownership and nested definitions during analysis.
      * @param cls
      *   The class definition (Defn.Class) to be analyzed. This includes metadata such as modifiers, name, constructor,
      *   and template body.
      * @return
      *   A tuple containing:
      *   - The updated scope context (ScopeCtx) after processing the class and its members.
      *   - A list of metrics (List[MemberMetrics]) computed for the class and its members, including any relevant
      *     properties or attributes identified during analysis.
      */
    def handleClassDef(s: ScopeCtx, cls: Defn.Class): (ScopeCtx, List[MemberMetrics]) = {
      val Defn.Class(mods, name, _, ctor, templ) = cls
      val rhs = templateTerm(templ)
      val clsItem = buildMemberMetric(s, "class", name.value, name.value, mods, cls, rhs, (false, false))
      val (s2, msT) = inOwner(s, ClsOwner(name.value)) { ss =>
        val fields = extractConstructorFieldMetrics(ss, ctor)
        val (ss2, tms) = visit(templ, ss)
        (ss2, fields ++ tms)
      }
      (s2, clsItem :: msT)
    }

    /**
      * Handles the analysis of a trait definition, extracting metrics for the trait and its members and updating the
      * scope context as necessary.
      *
      * @param s
      *   The current scope context.
      * @param trt
      *   The trait definition (Defn.Trait) to analyze.
      * @return
      *   Updated scope context and list of member metrics for the trait and its members.
      */
    def handleTraitDef(s: ScopeCtx, trt: Defn.Trait): (ScopeCtx, List[MemberMetrics]) = {
      val Defn.Trait(mods, name, _, _, templ) = trt
      val rhs = templateTerm(templ)
      val trtItem = buildMemberMetric(s, "trait", name.value, name.value, mods, trt, rhs, (false, false))
      val (s2, msT) = inOwner(s, TrtOwner(name.value)) { ss => visit(templ, ss) }
      (s2, trtItem :: msT)
    }

    /**
      * Processes a `val` declaration in the syntax tree, extracting metrics and updating the scope context.
      *
      * This method analyzes the provided `Defn.Val` declaration to extract relevant information such as the variable
      * name, its signature, modifiers, and associated return type metrics. It also processes the right-hand side (RHS)
      * of the declaration for additional metrics related to its definition and nesting context. The updated scope
      * context and a list of metrics for the `val` declaration and its RHS are returned.
      *
      * @param s
      *   The current scope context, representing the ownership hierarchy and block/lambda nesting level during
      *   analysis.
      * @param d
      *   The `Defn.Val` syntax tree node representing the `val` declaration to be processed.
      * @return
      *   A tuple where the first element is the updated scope context, and the second is a list of member metrics
      *   extracted from the `val` declaration and its RHS.
      */
    def handleValDef(s: ScopeCtx, d: Defn.Val): (ScopeCtx, List[MemberMetrics]) = {
      val Defn.Val(mods, pats, decltpe, rhs) = d
      val nm = pats.map(_.syntax).mkString(", ")
      val rhsT: Option[Term] = Some(rhs)
      val sig = signatureOfValVar(nm, decltpe, rhsT)
      val rtItem = ReturnTypeExplicitness.forVal(d)
      val item = buildMemberMetric(s, "val", nm, sig, mods, d, rhsT, (false, false), Some(rtItem))
      val (s2, rhsMs) =
        rhsT.map { t => inOwner(s, MemberOwner("val", nm)) { ss => visit(t, ss) } }.getOrElse((s, Nil))
      (s2, item :: rhsMs)
    }

    /**
      * Processes a `var` declaration in the syntax tree, extracting metrics and updating the scope context.
      *
      * This method analyzes the provided `Defn.Var` declaration to extract relevant information such as the variable
      * name, its signature, modifiers, and associated return type metrics. It also processes the right-hand side (RHS)
      * of the declaration for additional metrics related to its definition and nesting context. The updated scope
      * context and a list of metrics for the `var` declaration and its RHS are returned.
      *
      * @param s
      *   The current scope context, representing the ownership hierarchy and block/lambda nesting level during
      *   analysis.
      * @param d
      *   The `Defn.Var` syntax tree node representing the `var` declaration to be processed.
      * @return
      *   A tuple where the first element is the updated scope context, and the second is a list of member metrics
      *   extracted from the `var` declaration and its RHS.
      */
    def handleVarDef(s: ScopeCtx, d: Defn.Var): (ScopeCtx, List[MemberMetrics]) = {
      val Defn.Var(mods, pats, decltpe, maybeRhs) = d
      val nm = pats.map(_.syntax).mkString(", ")
      val rhsT = maybeRhs
      val sig = signatureOfValVar(nm, decltpe, rhsT)
      val rtItem = ReturnTypeExplicitness.forVar(d)
      val item = buildMemberMetric(s, "var", nm, sig, mods, d, rhsT, (false, false), Some(rtItem))
      val (s2, rhsMs) =
        rhsT.map { t => inOwner(s, MemberOwner("var", nm)) { ss => visit(t, ss) } }.getOrElse((s, Nil))
      (s2, item :: rhsMs)
    }

    /**
      * Processes a type definition in the syntax tree, extracting metrics and updating the scope context.
      *
      * This method analyzes the provided `Defn.Type` declaration to extract relevant information such as type name,
      * signature, and modifiers. It generates metrics for the type definition without affecting the scope context.
      *
      * @param s
      *   The current scope context, representing the ownership hierarchy and block/lambda nesting level during
      *   analysis.
      * @param d
      *   The `Defn.Type` syntax tree node representing the type definition to be processed.
      * @return
      *   A tuple where the first element is the unchanged scope context, and the second is a list containing metrics
      *   for the type definition.
      */
    def handleTypeDef(s: ScopeCtx, d: Defn.Type): (ScopeCtx, List[MemberMetrics]) = {
      val Defn.Type(mods, name, _, _) = d
      val sig = s"type ${name.value}"
      val item = buildMemberMetric(s, "type", name.value, sig, mods, d, None, (false, false))
      (s, item :: Nil)
    }

    /**
      * Processes a `given` definition in the syntax tree, extracting metrics and updating the scope context.
      *
      * This method analyzes the provided `Defn.Given` declaration to extract relevant information such as its name,
      * signature, modifiers, and associated return type metrics. The method also processes the body of the `given`
      * definition to gather additional metrics. The updated scope context and a list of member metrics for the `given`
      * definition and its body are returned.
      *
      * @param s
      *   The current scope context, representing the ownership hierarchy and block/lambda nesting level during
      *   analysis.
      * @param g
      *   The `Defn.Given` syntax tree node representing the `given` definition to be processed.
      * @return
      *   A tuple where the first element is the updated scope context, and the second is a list of member metrics
      *   extracted from the `given` definition and its body.
      */
    def handleGivenDef(s: ScopeCtx, g: Defn.Given): (ScopeCtx, List[MemberMetrics]) = {
      val Defn.Given(mods, name, _, _, templ) = g
      val nm = givenNameString(name)
      val rhs = templateTerm(templ)
      val isConv = templ.inits.exists(i => isConversionType(i.tpe))
      val sig = templ.inits.map(_.tpe.syntax).mkString(" with ")
      val item = buildMemberMetric(s, "given", nm, sig, mods, g, rhs, (true, isConv))
      val (s2, ms) = inOwner(s, ObjOwner(nm)) { ss => visit(templ, ss) }
      (s2, item :: ms)
    }

    /**
      * Processes a `given alias` definition in the syntax tree, extracting metrics and updating the scope context.
      *
      * This method analyzes the provided `Defn.GivenAlias` declaration to extract relevant information such as its
      * name, signature, modifiers, and associated return type metrics. The method also processes the right-hand side
      * (RHS) of the `given alias` definition to gather additional metrics. The updated scope context and a list of
      * member metrics for the `given alias` definition and its RHS are returned.
      *
      * @param s
      *   The current scope context, representing the ownership hierarchy and block/lambda nesting level during
      *   analysis.
      * @param ga
      *   The `Defn.GivenAlias` syntax tree node representing the `given alias` definition to be processed.
      * @return
      *   A tuple where the first element is the updated scope context, and the second is a list of member metrics
      *   extracted from the `given alias` definition and its RHS.
      */
    def handleGivenAliasDef(s: ScopeCtx, ga: Defn.GivenAlias): (ScopeCtx, List[MemberMetrics]) = {
      val Defn.GivenAlias(mods, name, _, _, decltpe, rhs) = ga
      val nm = givenNameString(name)
      val isConv = isConversionType(decltpe)
      val item = buildMemberMetric(s, "givenAlias", nm, decltpe.syntax, mods, ga, Some(rhs), (true, isConv))
      val (s2, ms) = inOwner(s, MemberOwner("givenAlias", nm)) { ss => visit(rhs, ss) }
      (s2, item :: ms)
    }

    /**
      * Handles the analysis of an object definition, extracting metrics for the object and its members and updating the
      * scope context as necessary.
      *
      * @param s
      *   The current scope context representing the state of ownership and nested definitions during analysis.
      * @param obj
      *   The object definition (Defn.Object) to be analyzed.
      * @return
      *   A tuple containing the updated scope context and a list of metrics computed for the object and its members.
      */
    def handleObjectDef(s: ScopeCtx, obj: Defn.Object): (ScopeCtx, List[MemberMetrics]) = {
      val Defn.Object(mods, name, templ) = obj
      val rhs = templateTerm(templ)
      val objItem = buildMemberMetric(s, "object", name.value, name.value, mods, obj, rhs, (false, false))
      val (s2, ms) = inOwner(s, ObjOwner(name.value)) { ss => visit(templ, ss) }
      (s2, objItem :: ms)
    }

    /**
      * Analyzes a syntax tree to extract member-related metrics within a given scope context. Traverses and processes
      * various tree nodes such as packages, classes, traits, objects, functions, blocks, and member definitions (`val`,
      * `var`, `type`, etc.) recursively, updating the scope context and producing a list of computed member metrics.
      *
      * @param tree
      *   The syntax tree representing a segment of Scala code for analysis.
      * @param s
      *   The current scope context, which includes information about the current traversal state.
      * @return
      *   A tuple containing the updated scope context and a list of member metrics computed during the tree traversal.
      */
    def visit(tree: Tree, s: ScopeCtx): (ScopeCtx, List[MemberMetrics]) = tree match {
      // package definitions
      case Pkg(ref, stats) =>
        inOwner(s, PkgOwner(ref.syntax)) { ss => visitList(stats, ss) }
      // object definitions
      case obj @ Defn.Object(_, _, _) =>
        handleObjectDef(s, obj)
      // class definitions
      case cls @ Defn.Class(mods, name, _, ctor, templ) =>
        handleClassDef(s, cls)
      // trait definitions
      case trt @ Defn.Trait(_, _, _, _, _) =>
        handleTraitDef(s, trt)
      // template bodies
      case Template(_, _, _, stats) =>
        inOwner(s, TplOwner()) { ss => visitList(Option(stats).getOrElse(Nil), ss) }
      // blocks
      case Term.Block(stats) =>
        val next = s.copy(blockId = s.blockId + 1)
        inOwner(next, BlkOwner(next.blockId)) { ss => visitList(stats, ss) }
      // lambda functions
      case Term.Function(_, body) =>
        val next = s.copy(lambdaId = s.lambdaId + 1)
        inOwner(next, LamOwner(next.lambdaId)) { ss => visit(body, ss) }
      // val definitions
      case d @ Defn.Val(_, _, _, _) =>
        handleValDef(s, d)
      // var definitions
      case d @ Defn.Var(_, _, _, _) =>
        handleVarDef(s, d)
      // type definitions
      case d @ Defn.Type(_, _, _, _) =>
        handleTypeDef(s, d)
      // given definitions (Scala 3)
      case g @ Defn.Given(_, _, _, _, _) =>
        handleGivenDef(s, g)
      // given alias definitions (Scala 3)
      case ga @ Defn.GivenAlias(_, _, _, _, _, _) =>
        handleGivenAliasDef(s, ga)
      // Handle other definitions by visiting their children
      case _: Defn.Def =>
        (s, Nil)
      // Default case: visit children
      case other =>
        visitList(other.children, s)
    }

    val (_, members) = visit(ctx.tree, s0)
    ctx.addMembers(members.toVector)

  }

}
