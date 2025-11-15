/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

import scala.deriving.Mirror
import scala.compiletime.constValueTuple
import scala.collection.immutable.ListMap

/**
  * Provides serialization utilities for classes that extend this trait. Enables conversion of an instance into a map or
  * its JSON string representation.
  *
  * This trait must be mixed in with classes that extend `Product`, leveraging the product's fields for serialization.
  */
trait Serializer extends Product:

  /**
    * Converts the current instance of a class implementing StatsBase into a Map where keys represent the field names of
    * the class and values represent the corresponding field values.
    *
    * @return
    *   a Map[String, Any] representation of the current instance.
    */
  def toMap: Map[String, Any] = Serializer.productToMap(this)

  /**
    * Converts the current instance of a class that extends `StatsBase` into its JSON string representation. This method
    * utilizes the `toMap` method to first convert the instance into a map and then serializes that map into a JSON
    * string.
    *
    * @return
    *   A JSON-formatted string representation of the current instance.
    */
  def toJson: String = Serializer.toJson(toMap)

  /**
    * Ordered field names (top level only, not flattened).
    */
  def fieldNames: Vector[String] =
    Serializer.paramNamesOrdered(this)

  /**
    * Ordered field->value pairs (top level only, not flattened).
    */
  def toListMap: ListMap[String, Any] =
    Serializer.productToMapOrdered(this)

  /**
    * Flattened, ordered (depth-first) dotted keys, e.g. metadata.name
    */
  def toFlatVector(prefix: String = ""): Vector[(String, Any)] =
    Serializer.flattenOrdered(toListMap, prefix)

  /**
    * Convenience: just the headers for CSV (flattened).
    */
  def csvHeaders(prefix: String = ""): Vector[String] =
    toFlatVector(prefix).map(_._1)

  /**
    * Convenience: the values for CSV (stringified, flattened).
    */
  def csvValues(prefix: String = ""): Vector[String] =
    toFlatVector(prefix).map(_._2).map(Serializer.valueToCsv)

  /**
    * Provides a formatted string representation of the current instance. This method can be overridden by subclasses to
    * provide custom formatting.
    *
    * @return
    *   A formatted string representation of the current instance.
    */
  def formattedString: String

/**
  * Companion object for the `StatsBase` trait, providing utility functions to facilitate data transformation,
  * serialization, and manipulation for statistical models. These utilities enable conversion of data structures such as
  * maps and products into alternative representations like flattened maps or JSON strings.
  */
object Serializer:

  def paramNamesOrdered(p: Product): Vector[String] =
    val names = p.productElementNames.toVector
    if (names.nonEmpty) names
    else (0 until p.productArity).map(i => s"_${i + 1}").toVector

  /**
    * Converts a given `Product` instance into an ordered map representation where the keys are the field names of the
    * instance's primary constructor, and the values are the corresponding field values. Field values are normalized
    * using the `norm` method.
    *
    * @param p
    *   The product instance to be converted into a map. This is typically an instance of a case class or other product
    *   type whose fields are to be mapped.
    * @return
    *   A map where the keys represent the names of fields and the values are the normalized field values, preserving
    *   the order of fields as defined in the primary constructor.
    */
  def productToMapOrdered(p: Product): ListMap[String, Any] =
    val names = paramNamesOrdered(p)
    val values = p.productIterator.toVector
    ListMap(names.zip(values).map { case (n, v) => n -> norm(v) }: _*)

  /**
    * Flattens a nested map into a single-level map with keys representing the hierarchical path.
    *
    * @param map
    *   the nested map to be flattened, where each key represents a string and the value can be any type
    * @param prefix
    *   an optional prefix to prepend to the keys of the map, default is an empty string
    * @return
    *   a single-level map where keys represent the hierarchical path in the original map and values are preserved
    */
  def flatten(map: Map[String, Any], prefix: String = ""): Map[String, Any] =
    map.flatMap {
      case (k, v: Map[_, _]) =>
        flatten(v.asInstanceOf[Map[String, Any]], if (prefix.isEmpty) k else s"$prefix.$k")
      case (k, v) =>
        Map((if (prefix.isEmpty) k else s"$prefix.$k") -> v)
    }

  /**
    * Converts a given value to its JSON string representation.
    *
    * @param v
    *   the value to be converted into JSON. Supported types include: null, Map, Seq, String, Boolean, Int, Long,
    *   Double, or any other type. Maps are expected to have string keys.
    * @return
    *   the JSON string representation of the value.
    */
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

  /**
    * Escapes special characters in a given string to make it JSON-safe.
    *
    * @param s
    *   the input string to be escaped
    * @return
    *   the escaped string where special characters are replaced with their corresponding escape sequences
    */
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

  /**
    * Converts a given Product instance (e.g., case class, tuple) into a Map representation where the keys are the
    * element names and the values are the normalized representations of the corresponding elements.
    *
    * @param p
    *   the Product instance to be converted into a Map
    * @return
    *   a Map where the keys are the names of the Product's elements and the values are their normalized values
    */
  private def productToMap(p: Product): Map[String, Any] =
    p.productElementNames.zip(p.productIterator.toSeq).map { case (n, v) => n -> norm(v) }.toMap

  def flattenOrdered(map: ListMap[String, Any], prefix: String = ""): Vector[(String, Any)] =
    val buf = Vector.newBuilder[(String, Any)]
    map.foreach {
      case (k, v: ListMap[_, _]) =>
        buf ++= flattenOrdered(v.asInstanceOf[ListMap[String, Any]], if (prefix.isEmpty) k else s"$prefix.$k")
      case (k, v: Map[_, _]) =>
        // convert non-ListMap to ListMap to keep a deterministic order
        val lm = ListMap(v.asInstanceOf[Map[String, Any]].toSeq.sortBy(_._1): _*)
        buf ++= flattenOrdered(lm, if (prefix.isEmpty) k else s"$prefix.$k")
      case (k, v) =>
        val key = if (prefix.isEmpty) k else s"$prefix.$k"
        buf += key -> v
    }
    buf.result()

  def valueToCsv(v: Any): String = v match {
    case null       => ""
    case None       => ""
    case Some(x)    => valueToCsv(x)
    case s: String  => escCsv(s)
    case b: Boolean => if (b) "true" else "false"
    case n: Int     => n.toString
    case n: Long    => n.toString
    case n: Double  =>
      if (n.isNaN || n.isInfinity) "" else java.lang.String.format(java.util.Locale.ROOT, "%.6f", n: java.lang.Double)
    case seq: Seq[_]  => escCsv(seq.map(valueToCsv).mkString("[", " ", "]"))
    case m: Map[_, _] =>
      // fallback to JSON for complex nested leftovers
      escCsv(toJson(m))
    case other => escCsv(other.toString)
  }

  /**
    * Escapes a string for inclusion in a CSV file, adding quotes if necessary and escaping internal quotes.
    *
    * Uses RFC-4180
    *
    * @param s
    *   the input string to be escaped
    * @return
    *   the escaped string, with quotes added if needed and internal quotes doubled
    */
  private def escCsv(s: String): String = {
    val needs = s.indexOf(',') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0 || s.indexOf('"') >= 0
    val body = s.replace("\"", "\"\"")
    if (needs) "\"" + body + "\"" else body
  }

  /**
    * Normalizes a given value into a simplified or specific representation based on its type.
    *
    * @param v
    *   the input value to be normalized; it can be an instance of StatsBase, Option, Seq, Map, Product, or any other
    *   type
    * @return
    *   the normalized representation of the input value
    */
  private def norm(v: Any): Any = v match {
    case s: Serializer => s.toMap
    case o: Option[_]  => o.map(norm).orNull
    case seq: Seq[_]   => seq.map(norm)
    case m: Map[_, _]  =>
      m.asInstanceOf[Map[Any, Any]].map { case (k, v2) => k.toString -> norm(v2) }
    case p: Product if !p.isInstanceOf[Serializer] =>
      // If you want to auto-convert any Product: enable next line, else leave as toString
      productToMap(p)
    // p.toString
    case other => other
  }
