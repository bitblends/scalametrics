/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics

import scala.meta.Tree

/**
  * Scala 3 version: A trait that provides a mechanism for traversing the nodes of a tree structure, with additional
  * support for traversing child nodes.
  *
  * This trait is designed for use cases where specific processing needs to be performed on each node of a syntax tree.
  * Subclasses or objects extending this trait can override the `apply` method to define custom traversal logic. The
  * `traverseChildren` method is available as a helper to recursively traverse all child nodes of a given tree node.
  */
trait TraverserCompat:
  /**
    * Applies processing logic to the given tree node. Subclasses or implementations can override this method to define
    * specific behavior for traversing or handling tree nodes.
    *
    * @param tree
    *   the tree node to process
    * @return
    *   Unit
    */
  def apply(tree: Tree): Unit

  /**
    * Traverses the child nodes of the given tree and applies the `apply` method to each child.
    *
    * @param tree
    *   the tree node whose children will be traversed
    * @return
    *   Unit
    */
  protected def traverseChildren(tree: Tree): Unit = {
    tree.children.foreach(apply)
  }
