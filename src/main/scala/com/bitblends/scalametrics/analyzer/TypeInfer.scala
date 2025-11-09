/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.analyzer

import scala.meta._

/**
  * Provides functionality for inferring simplified string representations of types associated with terms (expressions)
  * in Scala code. It includes utility methods for analyzing common patterns in type usage and converting them into
  * human-readable or simplified format.
  */
object TypeInfer {

  /**
    * A set of strings representing the names of various time-related types from the `java.time` package. These types
    * are commonly used for date, time, and duration manipulation.
    */
  private val timeTypeNames: Set[String] =
    Set(
      "Instant",
      "LocalDate",
      "LocalDateTime",
      "ZonedDateTime",
      "OffsetDateTime",
      "OffsetTime",
      "Duration",
      "Period",
      "ZoneId",
      "ZoneOffset",
      "Year",
      "YearMonth",
      "MonthDay"
    )

  /**
    * A mapping of common Java `java.util` collection class names to their respective type argument arity. The keys
    * represent the class names of various standard collections. The corresponding values indicate the number of type
    * arguments each collection class requires.
    */
  private val utilCollectionArity: Map[String, Int] = Map(
    "ArrayList" -> 1,
    "LinkedList" -> 1,
    "Vector" -> 1,
    "Stack" -> 1,
    "HashSet" -> 1,
    "LinkedHashSet" -> 1,
    "TreeSet" -> 1,
    "EnumSet" -> 1,
    "HashMap" -> 2,
    "LinkedHashMap" -> 2,
    "TreeMap" -> 2,
    "Hashtable" -> 2,
    "ConcurrentHashMap" -> 2,
    "WeakHashMap" -> 2,
    "IdentityHashMap" -> 2
  )

  /**
    * Infers a simplified string representation of the type associated with a given term.
    *
    * Note: This method uses heuristics and common patterns to deduce the type, and may not cover all possible cases. It
    * is a recursive function that analyzes the structure of the term to determine its type.
    * @param rhs
    *   the term for which the type is to be inferred
    * @return
    *   an optional string representing the simplified type of the term, or None if the type cannot be inferred
    */
  def inferSimpleType(rhs: Term): Option[String] = {

    rhs match {
      // literals / basics
      case Lit.Byte(_)               => Some("Byte")
      case Lit.Short(_)              => Some("Short")
      case Lit.Int(_)                => Some("Int")
      case Lit.Long(_)               => Some("Long")
      case Lit.Float(_)              => Some("Float")
      case Lit.Double(_)             => Some("Double")
      case Lit.Boolean(_)            => Some("Boolean")
      case Lit.Char(_)               => Some("Char")
      case Lit.String(_)             => Some("String")
      case Lit.Unit()                => Some("Unit")
      case Lit.Null()                => Some("Null")
      case Term.Interpolate(_, _, _) => Some("String")
      // new java.util.* / java.time.* with/without type args
      case Term.New(Init(tpe: Type.Apply, _, _)) => inferNewWithTypeArgs(tpe)
      case Term.New(Init(tpe, _, _))             => Some(prettyJavaBase(fqcnOf(tpe)))
      case Term.NewAnonymous(templ)              => inferAnonymousNew(templ)
      // Term-side type application: java.util.HashMap[String, Int](...)
      case Term.Apply(Term.ApplyType(fun, targs), _) if inferJavaUtilTermSideTypeApplication(fun, targs).isDefined =>
        inferJavaUtilTermSideTypeApplication(fun, targs)
      // java.time factories: Instant.now / LocalDate.parse
      case Term.Select(qual, Term.Name(_)) => isTimeStaticSelect(qual)
      // Scala collections (homogeneous element inference)
      case Term.Apply(Term.Name(n @ ("List" | "Vector" | "Seq" | "Set" | "Array")), args) =>
        inferScalaCollectionApply(n, args)
      // Map inference across all entries; fall back to wildcard if mixed or unknown
      case Term.Apply(Term.Name("Map"), args) => inferMapType(args)
      // empties
      case Term.Name("Nil") => Some("List[Nothing]")
      // type application: Foo[Bar]
      case Term.ApplyType(fun, targs) => Some(baseNameOfTerm(fun) + targs.map(_.syntax).mkString("[", ", ", "]"))
      // ascriptions
      case Term.Ascribe(_, tpe) => Some(tpe.syntax)
      // control-flow/block heuristics
      case Term.Block(stats) if stats.nonEmpty => lastTerm(stats).flatMap(inferSimpleType)
      // if-expression: infer from branches or condition
      case Term.If(cond, thenp, elsep) => inferIf(cond, thenp, elsep)
      // match-expression: infer from homogeneous case bodies
      case Term.Match(_, cases) if cases.nonEmpty => inferMatch(cases)
      // arithmetic/numeric binary operations: infer from operands
      // Special case: for basic arithmetic with numeric literals, infer the literal's type
      case Term.ApplyInfix(lhs, Term.Name(op), _, List(rhs)) if isArithmeticOp(op) =>
        inferArithmeticResult(lhs, op, rhs)
      // unary operations: infer from operand
      case Term.ApplyUnary(_, arg) => inferSimpleType(arg)
      // generic call fallback: use base name of callee
      case Term.Apply(fun, _) => Some(prettyJavaBase(baseNameOfTerm(fun)))
      // default: unable to infer
      case _ => None
    }
  }

  /**
    * Infers the type of a match-expression from its case bodies when homogeneous.
    *
    * @param cases
    *   the list of case clauses of the match
    * @return
    *   an Option of the inferred homogeneous body type, otherwise None
    */
  private def inferMatch(cases: List[Case]): Option[String] = {
    val bodyTs = cases.collect { case Case(_, _, body: Term) => inferSimpleType(body) }
    homogeneous(bodyTs)
  }

  /**
    * Infers the resulting type of an if-expression based on the types of the then and else branches, or from the
    * condition if necessary.
    *
    * @param cond
    *   the condition of the if-expression
    * @param thenp
    *   the term representing the then-branch of the if-expression
    * @param elsep
    *   the term representing the else-branch of the if-expression
    * @return
    *   an Option containing the inferred type as a String, or None if the type cannot be determined
    */
  private def inferIf(cond: Term, thenp: Term, elsep: Term): Option[String] = {
    (inferSimpleType(thenp), inferSimpleType(elsep)) match {
      case (Some(t1), Some(t2)) if t1 == t2 => Some(t1)
      case (Some(t), None)                  => Some(t)
      case (None, Some(t))                  => Some(t)
      case (None, None)                     => inferFromComparison(cond)
      case _                                => None
    }
  }

  /**
    * Infers the type of a Scala collection based on its name and the types of its elements.
    *
    * @param n
    *   The name of the Scala collection, such as "Array" or another collection type.
    * @param args
    *   A list of terms representing the elements of the collection.
    * @return
    *   An optional string representing the inferred type of the collection. Returns the collection type with the
    *   inferred element type, or a wildcard type if unable to infer.
    */
  private def inferScalaCollectionApply(n: String, args: List[Term]): Option[String] = {
    val elem = homogeneous(args.collect { case t: Term => inferSimpleType(t) })
    if (n == "Array") elem.map(t => s"Array[$t]").orElse(Some("Array[_]"))
    else elem.map(t => s"$n[$t]").orElse(Some(s"$n[_]"))
  }

  /**
    * Infers the type of a Java utility term based on the function name and type arguments.
    *
    * @param fun
    *   the term representing the function or method being analyzed
    * @param targs
    *   the list of type arguments associated with the term
    * @return
    *   an optional string representation of the inferred type, or None if it cannot be inferred
    */
  private def inferJavaUtilTermSideTypeApplication(fun: Term, targs: List[Type]): Option[String] = {
    val segs = segmentsOf(fun)
    if (isJavaUtil(segs) || utilCollectionArity.contains(segs.lastOption.getOrElse("")))
      Some(renderUtil(baseNameOfTerm(fun), targs))
    else None
  }

  /**
    * Infers a new type with the provided type arguments.
    *
    * @param tpe
    *   the type application instance containing the base type and its arguments
    * @return
    *   an Option containing a string representation of the inferred type, or None if it cannot be determined
    */
  private def inferNewWithTypeArgs(tpe: Type.Apply): Option[String] = {
    val baseFq = fqcnOf(tpe.tpe)
    val targs = tpe.args.toList
    if (baseFq.startsWith("java.util.")) Some(renderUtil(baseFq, targs))
    else Some(prettyJavaBase(baseFq) + targs.map(_.syntax).mkString("[", ", ", "]"))
  }

  /**
    * Infers the type of an anonymous new instance based on the provided template.
    *
    * @param templ
    *   the template representing the structure and type information of the instance
    * @return
    *   an optional string representing the inferred type of the anonymous new instance, or None if it cannot be
    *   inferred
    */
  private def inferAnonymousNew(templ: Template): Option[String] = {
    templ.inits.headOption.map {
      case Init(tpe: Type.Apply, _, _) =>
        val baseFq = fqcnOf(tpe.tpe)
        if (baseFq.startsWith("java.util.")) renderUtil(baseFq, tpe.args.toList)
        else prettyJavaBase(baseFq) + tpe.args.map(_.syntax).mkString("[", ", ", "]")
      case Init(tpe, _, _) =>
        val baseFq = fqcnOf(tpe)
        prettyJavaBase(baseFq)
    }
  }

  /**
    * Infers a simplified type for an arithmetic/comparison/logical infix operation. Extraction of logic from
    * inferSimpleType case for Term.ApplyInfix.
    */
  private def inferArithmeticResult(lhs: Term, op: String, rhs: Term): Option[String] = {
    (inferSimpleType(lhs), inferSimpleType(rhs)) match {
      case (Some(t1), Some(t2)) if t1 == t2                               => Some(t1)
      case (None, Some(t)) if isBasicArithmeticOp(op) && isNumericType(t) =>
        lhs match {
          case Term.Name(_) => Some(t)
          case _            => None
        }
      case (Some(t), None) if isBasicArithmeticOp(op) && isNumericType(t) =>
        rhs match {
          case Term.Name(_) => Some(t)
          case _            => None
        }
      case _ => None
    }
  }

  /**
    * Infers the type of a map based on a list of key-value term arguments.
    *
    * @param args
    *   A list of terms representing potential key-value pairs in the map.
    * @return
    *   An optional string representing the inferred type of the map in the form `Map[KeyType, ValueType]`. If the type
    *   cannot be determined, the default type `Map[_, _]` is returned.
    */
  private def inferMapType(args: List[Term]): Option[String] = {
    val pairs = args.flatMap(kvTupleOf)
    if (pairs.isEmpty) Some("Map[_, _]")
    else {
      val kt = homogeneous(pairs.map { case (k, _) => inferSimpleType(k) })
      val vt = homogeneous(pairs.map { case (_, v) => inferSimpleType(v) })
      (kt, vt) match {
        case (Some(ka), Some(va)) => Some(s"Map[$ka, $va]")
        case _                    => Some("Map[_, _]")
      }
    }
  }

  /**
    * Determines if all non-None elements in the given list of options are the same and returns that value if so.
    *
    * @param xs
    *   A list of `Option[T]` elements to be checked for homogeneity.
    * @return
    *   An `Option[T]` containing the single distinct value if all non-None elements in the list are the same; None
    *   otherwise.
    */
  private def homogeneous[T](xs: List[Option[T]]): Option[T] = {
    val distinct = xs.flatten.distinct
    distinct match {
      case h :: Nil => Some(h)
      case _        => None
    }
  }

  /**
    * Determines whether the given operator string corresponds to an arithmetic, comparison, or logical operation.
    *
    * @param op
    *   A string representing the operator to check.
    * @return
    *   True if the operator is an arithmetic, comparison, or logical operation, otherwise false.
    */
  private def isArithmeticOp(op: String): Boolean = op match {
    case "+" | "-" | "*" | "/" | "%"           => true
    case "<" | ">" | "<=" | ">=" | "==" | "!=" => true
    case "&&" | "||"                           => true
    case _                                     => false
  }

  /**
    * Attempts to infer a simplified string representation of the type based on a comparison operation and its operands.
    *
    * @param cond
    *   The term representing the comparison operation to analyze.
    * @return
    *   An optional string representing the inferred type if it can be determined from the operands, or None if the type
    *   cannot be inferred.
    */
  private def inferFromComparison(cond: Term): Option[String] = cond match {
    // For comparison operations, try to infer type from operands
    case Term.ApplyInfix(lhs, Term.Name(op), _, List(rhs)) if isComparisonOp(op) =>
      inferSimpleType(lhs).orElse(inferSimpleType(rhs))
    case _ => None
  }

  /**
    * Determines whether the given string represents a comparison operator.
    *
    * @param op
    *   A string representing the operator to check.
    * @return
    *   True if the provided string is one of the valid comparison operators ("<", ">", "<=", ">=", "==", "!=");
    *   otherwise false.
    */
  private def isComparisonOp(op: String): Boolean = op match {
    case "<" | ">" | "<=" | ">=" | "==" | "!=" => true
    case _                                     => false
  }

  /**
    * Determines whether the provided string represents a numeric type.
    *
    * @param t
    *   The name of the type as a string, such as "Int" or "Double".
    * @return
    *   True if the provided string corresponds to a numeric type ("Byte", "Short", "Int", "Long", "Float", "Double"),
    *   false otherwise.
    */
  private def isNumericType(t: String): Boolean = t match {
    case "Byte" | "Short" | "Int" | "Long" | "Float" | "Double" => true
    case _                                                      => false
  }

  /**
    * Determines whether the provided operator corresponds to a basic arithmetic operation.
    *
    * @param op
    *   A string representing the operator to check, such as "+" or "-".
    * @return
    *   True if the operator is a basic arithmetic operation (addition or subtraction), false otherwise.
    */
  private def isBasicArithmeticOp(op: String): Boolean = op match {
    // Only infer for addition/subtraction, not multiplication/division
    // This is a heuristic to avoid over-inferring in complex expressions
    case "+" | "-" => true
    case _         => false
  }

  /**
    * Extracts the segments of a term into a list of strings, where each segment corresponds to a part of the term's
    * structure.
    *
    * @param term
    *   The term to be analyzed, which might be a name, a qualified name, or another kind of term structure.
    * @return
    *   A list of strings representing the individual segments of the term.
    */
  private def segmentsOf(term: Term): List[String] = term match {
    case Term.Name(n)                    => n :: Nil
    case Term.Select(qual, Term.Name(n)) => segmentsOf(qual) :+ n
    case Term.Select(qual, name)         => segmentsOf(qual) :+ name.syntax
    case _                               => term.syntax.split('.').toList
  }

  /**
    * Determines if the given list of string segments begins with "java" followed by "util".
    *
    * @param segs
    *   A list of strings representing segments, typically parts of a fully qualified class or package name.
    * @return
    *   True if the list starts with "java" followed by "util", indicating the `java.util` package; otherwise false.
    */
  private def isJavaUtil(segs: List[String]): Boolean =
    segs match {
      case "java" :: "util" :: _ => true
      case _                     => false
    }

  /**
    * Determines whether the provided term represents a static reference to a type within the `java.time` package, or
    * other supported time types.
    *
    * @param qual
    *   The term to analyze, typically referring to a name or a qualified name that might represent a time-related type.
    * @return
    *   An `Option[String]` containing the name of the time-related type if the term matches a recognized time type,
    *   otherwise `None`.
    */
  private def isTimeStaticSelect(qual: Term): Option[String] =
    segmentsOf(qual) match {
      case n :: Nil if timeTypeNames(n)                     => Some(n)
      case "java" :: "time" :: n :: Nil if timeTypeNames(n) => Some(n)
      case _                                                => None
    }

  /**
    * Extracts the base name of the given term by determining the last segment of its structure. If the term has no
    * segments, the last segment of its syntax representation is returned.
    *
    * @param t
    *   The term for which the base name is to be extracted. This term might represent a name, a qualified name, or
    *   another structured term.
    * @return
    *   A string representing the base name of the term, derived from its last segment or syntax.
    */
  private def baseNameOfTerm(t: Term): String =
    segmentsOf(t).lastOption.getOrElse(lastSegment(t.syntax))

  /**
    * Extracts the last term from a list of statements, if any. The method traverses the list in reverse and selects the
    * first element that is a term.
    *
    * @param stats
    *   A list of `Stat` elements from which the last `Term` is to be extracted.
    * @return
    *   An `Option[Term]` representing the last `Term` found in the list, or `None` if no `Term` is present.
    */
  private def lastTerm(stats: List[Stat]): Option[Term] =
    stats.reverse.collectFirst { case t: Term => t }

  /**
    * Extracts a key-value tuple from the given term if it matches one of the supported patterns.
    *
    * @param term
    *   The term to extract the key-value tuple from. The term can represent a tuple or an infix/postfix expression
    *   formatted as "key -> value".
    * @return
    *   An `Option` containing a tuple `(Term, Term)` that represents the key and value if the term matches a supported
    *   pattern, or `None` otherwise.
    */
  private def kvTupleOf(term: Term): Option[(Term, Term)] = term match {
    case Term.Tuple(List(k: Term, v: Term))                          => Some((k, v))
    case Term.ApplyInfix(k: Term, Term.Name("->"), _, List(v: Term)) => Some((k, v))
    case Term.Apply(Term.Name("->"), List(k: Term, v: Term))         => Some((k, v))
    case _                                                           => None
  }

  /**
    * Extracts the last segment of a qualified name. A qualified name is assumed to be a string with components
    * separated by dots ('.').
    *
    * @param qualified
    *   the fully qualified name from which the last segment is to be extracted
    * @return
    *   the last segment of the qualified name, or the original string if no dots are present
    */
  private def lastSegment(qualified: String): String =
    qualified.split('.').lastOption.getOrElse(qualified)

  /**
    * Simplifies a fully qualified class name from the `java.time` or `java.util` package, or any other package, by
    * extracting the last segment of the qualified name.
    *
    * @param base
    *   the fully qualified class name to be simplified
    * @return
    *   the last segment of the qualified name, representing the base class name
    */
  private def prettyJavaBase(base: String): String = base match {
    case s if s.startsWith("java.time.") => lastSegment(s)
    case s if s.startsWith("java.util.") => lastSegment(s)
    case other                           => lastSegment(other)
  }

  /**
    * Extracts the fully qualified class name (FQCN) of the provided type.
    *
    * @param t
    *   the type for which the fully qualified class name is to be extracted
    * @return
    *   the string representation of the fully qualified class name of the type
    */
  private def fqcnOf(t: Type): String = t.syntax

  /**
    * Renders a utility representation for the given base name and type arguments, resolving specific arities from the
    * `utilCollectionArity` mapping.
    *
    * @param base0
    *   the initial base name, typically a fully qualified class name, which will be simplified using the
    *   `prettyJavaBase` method
    * @param targs
    *   the list of type arguments associated with the base name to be rendered, where their syntax will be extracted if
    *   available
    * @return
    *   the rendered string that represents the base name and type arguments in a simplified and formatted manner, with
    *   specific handling for arities of 1 and 2, or just the simplified base name if no arity matches
    */
  private def renderUtil(base0: String, targs: List[Type]): String = {
    val base = prettyJavaBase(base0)
    utilCollectionArity.get(base) match {
      case Some(1) =>
        val a = targs.headOption.map(_.syntax).getOrElse("_")
        s"$base[$a]"
      case Some(2) =>
        val a = targs.headOption.map(_.syntax).getOrElse("_")
        val b = targs.lift(1).map(_.syntax).getOrElse("_")
        s"$base[$a, $b]"
      case _ => base
    }
  }

}
