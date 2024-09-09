/*
 * gMark: A domain- and query language-independent graph instance and query workload generator.
 * Copyright (C) 2021  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/gMark
 *
 * gMark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gMark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.gmark.ast;

import java.util.stream.Stream;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.lang.QueryLanguage;
import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.lang.generic.GenericEdge;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Representation of an Abstract Syntax Tree (AST) for queries.
 * @author Roan
 * @see QueryLanguage
 * @see QueryFragment
 * @see QueryLanguageSyntax
 * @see <a href="https://en.wikipedia.org/wiki/Abstract_syntax_tree">Abstract syntax tree on wikipedia</a>
 */
public class QueryTree{
	/**
	 * For unary and binary operations the left input argument, for atoms null.
	 */
	private final QueryTree left;
	/**
	 * For binary operations the right input argument, for unary operations and atoms null.
	 */
	private final QueryTree right;
	/**
	 * The query fragment this query tree node was derived from.
	 */
	private final QueryFragment fragment;
	
	/**
	 * Constructs a new query tree from the given input.
	 * @param left The left input argument for unary and binary operations.
	 * @param right The right input argument binary operations.
	 * @param fragment The query fragment this AST node was derived from.
	 */
	private QueryTree(QueryTree left, QueryTree right, QueryFragment fragment){
		this.left = left;
		this.right = right;
		this.fragment = fragment;
	}
	
	/**
	 * Checks if this AST node is a leaf node. This implies that the operation
	 * for this AST node is an atom and that this node has no child nodes or inputs.
	 * @return True if this AST node is a leaf node.
	 * @see OperationType#isAtom()
	 */
	public boolean isLeaf(){
		return left == null && right == null;
	}
	
	/**
	 * Checks if the operation for this AST node is a unary operation, meaning it takes
	 * exactly one operand and has exactly one child node in the query AST. The child
	 * node of the input for the operation in this node is given by {@link #getLeft()}.
	 * @return True if the operation for this AST node is unary.
	 * @see OperationType#isUnary()
	 * @see #getLeft()
	 */
	public boolean isUnary(){
		return getOperation().isUnary();
	}
	
	/**
	 * Checks if the operation for this AST node is a binary operation, meaning it takes
	 * exactly two operands and has exactly two child nodes in the query AST. The two
	 * inputs for the operation in this node are given in order by {@link #getLeft()}
	 * and {@link #getRight()}.
	 * @return True if the operation for this AST node is binary.
	 * @see OperationType#isBinary()
	 * @see #getLeft()
	 * @see #getRight()
	 */
	public boolean isBinary(){
		return getOperation().isBinary();
	}
	
	/**
	 * Gets the operation represented by this AST node.
	 * @return The operation for this AST node.
	 * @see OperationType
	 */
	public OperationType getOperation(){
		return fragment.getOperationType();
	}
	
	/**
	 * If the operation for this AST node is {@link OperationType#EDGE},
	 * returns the edge label or predicate associated with edge at this AST node.
	 * @return The label at this AST node.
	 * @throws IllegalStateException When the operation for this AST node
	 *         is not equal to {@link OperationType#EDGE}.
	 * @see Predicate
	 * @see OperationType#EDGE
	 * @see #getOperation()
	 */
	public Predicate getPredicate() throws IllegalStateException{
		if(getOperation() != OperationType.EDGE){
			throw new IllegalStateException("Query fragment is not an AST edge leaf.");
		}
		
		return ((GenericEdge)fragment).getLabel();
	}
	
	/**
	 * Gets the left child node of this AST node if any. This child node is
	 * only present if the operation for this AST node is unary or binary
	 * and for binary operation represents the first input argument.
	 * @return The left child node of this AST node or null if this node is a leaf.
	 * @see OperationType
	 */
	public QueryTree getLeft(){
		return left;
	}
	
	/**
	 * Gets the right child node of this AST node if any. This child node is
	 * only present if the operation for this AST node is binary and represents
	 * the second input argument.
	 * @return The right child node of this AST node of null if the operation for this node is not binary.
	 * @see OperationType
	 */
	public QueryTree getRight(){
		return right;
	}
	
	/**
	 * Returns a stream over all the AST nodes in the sub-tree rooted at this
	 * AST node, including this AST node itself. The tree will be traversed in
	 * a top down manner with left child nodes appearing before right child nodes.
	 * @return A stream over all AST nodes in the sub-tree rooted at this AST node.
	 */
	public Stream<QueryTree> stream(){
		Stream<QueryTree> stream = Stream.of(this);
		
		int arity = getOperation().getArity();
		if(arity > 0){
			stream = Stream.concat(stream, left.stream());
		}
		
		return arity > 1 ? Stream.concat(stream, right.stream()) : stream;
	}
	
	/**
	 * Writes this AST in list from to the given indent writer.
	 * @param writer The indent writer to write to.
	 */
	private void writeAST(IndentWriter writer){
		if(isLeaf()){
			writer.print("- ");
			writer.println(fragment.toString());
		}else{
			writer.println("- " + getOperation(), 2);
			left.writeAST(writer);
			if(right != null){
				right.writeAST(writer);
			}
			
			writer.decreaseIndent(2);
		}
	}
	
	@Override
	public String toString(){
		IndentWriter writer = new IndentWriter();
		writer.println("AST for " + fragment.toString());
		writeAST(writer);
		return writer.toString();
	}
	
	/**
	 * Constructs an AST node for a query fragment representing an atomic operation.
	 * @param fragment The query fragment.
	 * @return The AST for the given query fragment.
	 * @see OperationType#isAtom()
	 */
	public static QueryTree ofAtom(QueryFragment fragment){
		return new QueryTree(null, null, fragment);
	}
	
	/**
	 * Constructs an AST node for a query fragment representing a unary operation.
	 * @param left The AST representing the input to the operation in the given query fragment.
	 * @param fragment The query fragment.
	 * @return The AST for the given query fragment.
	 * @see OperationType#isUnary()
	 */
	public static QueryTree ofUnary(QueryTree left, QueryFragment fragment){
		return new QueryTree(left, null, fragment);
	}

	/**
	 * Constructs an AST node for a query fragment representing a binary operation.
	 * @param left The AST representing the first input argument to the operation in the given query fragment.
	 * @param right The AST representing the second input argument to the operation in the given query fragment.
	 * @param fragment The query fragment.
	 * @return The AST for the given query fragment.
	 * @see OperationType#isBinary()
	 */
	public static QueryTree ofBinary(QueryTree left, QueryTree right, QueryFragment fragment){
		return new QueryTree(left, right, fragment);
	}
}
