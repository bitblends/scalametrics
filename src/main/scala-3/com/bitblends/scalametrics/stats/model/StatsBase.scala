package com.bitblends.scalametrics.stats.model

import com.bitblends.scalametrics.stats.model.StatsBase.norm
import scala.deriving.Mirror
import scala.compiletime.constValueTuple

/**
  * Defines a base trait for statistical models. This serves as a foundation or marker trait for components within the
  * statistics package and can be extended by specific statistical entities.
  */
trait StatsBase extends Product:
  def toMap: Map[String, Any] = StatsBase.productToMap(this)

/**
  * Companion object for the `StatsBase` trait, providing utility functions to facilitate data transformation,
  * serialization, and manipulation for statistical models. These utilities enable conversion of data structures such as
  * maps and products into alternative representations like flattened maps or JSON strings.
  */
object StatsBase:

  def flatten(map: Map[String, Any], prefix: String = ""): Map[String, Any] =
    map.flatMap {
      case (k, v: Map[_, _]) =>
        flatten(v.asInstanceOf[Map[String, Any]], if (prefix.isEmpty) k else s"$prefix.$k")
      case (k, v) =>
        Map((if (prefix.isEmpty) k else s"$prefix.$k") -> v)
    }

  def toJson(v: Any): String = v match {
    case null         => "null"
    case m: Map[_, _] =>
      m.asInstanceOf[Map[String, Any]]
        .map { case (k, v2) => "\"" + escape(k) + "\":" + toJson(v2) }
        .mkString("{", ",", "}")
    case seq: Seq[_] => seq.map(toJson).mkString("[", ",", "]")
    case s: String   => "\"" + escape(s) + "\""
    case b: Boolean  => b.toString
    case n: Int      => n.toString
    case n: Long     => n.toString
    case n: Double   =>
      if (n.isNaN || n.isInfinity) "\"" + n.toString + "\"" else n.toString
    case other => "\"" + escape(other.toString) + "\""
  }

  private def escape(s: String): String =
    s.flatMap {
      case '"'              => "\\\""
      case '\\'             => "\\\\"
      case '\b'             => "\\b"
      case '\f'             => "\\f"
      case '\n'             => "\\n"
      case '\r'             => "\\r"
      case '\t'             => "\\t"
      case c if c.isControl => "\\u%04x".format(c.toInt)
      case c                => c.toString
    }

//  private inline def productToMap[T <: Product](p: T)(using m: Mirror.ProductOf[T]): Map[String, Any] =
//    val names = constValueTuple[m.MirroredElemLabels].toList.asInstanceOf[List[String]]
//    names.zip(p.productIterator.toList.map(norm)).toMap

  private def productToMap(p: Product): Map[String, Any] =
    p.productElementNames.zip(p.productIterator.toSeq).map { case (n, v) => n -> norm(v) }.toMap

  private def norm(v: Any): Any = v match {
    case s: StatsBase => s.toMap
    case o: Option[_] => o.map(norm).orNull
    case seq: Seq[_]  => seq.map(norm)
    case m: Map[_, _] =>
      m.asInstanceOf[Map[Any, Any]].map { case (k, v2) => k.toString -> norm(v2) }
    case p: Product if !p.isInstanceOf[StatsBase] =>
      // If you want to auto-convert any Product: enable next line, else leave as toString
      // productToMap(p)
      p.toString
    case other => other
  }
