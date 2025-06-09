/*
 * gMark: A domain- and query language-independent query workload generator and query language utility library.
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

import java.util.List;
import java.util.stream.Stream;

import dev.roanh.gmark.lang.QueryLanguage;
import dev.roanh.gmark.lang.QueryLanguageSyntax;
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
	 * Operands for this operation, the number of operands equals the arity.
	 */
	private final List<QueryTree> operands;
	/**
	 * The query fragment this query tree node was derived from.
	 */
	private final QueryFragment fragment;
	
	/**
	 * Constructs a new query tree from the given input.
	 * @param operands The input arguments, the list size corresponds to the operation arity.
	 * @param fragment The query fragment this AST node was derived from.
	 */
	private QueryTree(List<QueryTree> operands, QueryFragment fragment){
		this.operands = operands;
		this.fragment = fragment;
	}
	
	/**
	 * Checks if this AST node is a leaf node. This implies that the operation
	 * for this AST node is an atom and that this node has no child nodes or inputs.
	 * @return True if this AST node is a leaf node.
	 * @see OperationType#isAtom()
	 */
	public boolean isLeaf(){
		return operands.isEmpty();
	}
	
	/**
	 * Checks if the operation for this AST node is a unary operation, meaning it takes
	 * exactly one operand and has exactly one child node in the query AST.
	 * @return True if the operation for this AST node is unary.
	 * @see OperationType#isUnary()
	 * @see #getArity()
	 */
	public boolean isUnary(){
		return getOperation().isUnary();
	}
	
	/**
	 * Checks if the operation for this AST node is a binary operation, meaning it takes
	 * exactly two operands and has exactly two child nodes in the query AST.
	 * @return True if the operation for this AST node is binary.
	 * @see OperationType#isBinary()
	 * @see #getArity()
	 */
	public boolean isBinary(){
		return getOperation().isBinary();
	}
	
	/**
	 * Gets the number of provided input operands for the operation at this tree node.
	 * <p>
	 * Note: this function provides the actual arity, which may differ from the arity
	 * of the operation for this AST node, in particular when the operation does not
	 * have a fixed arity (e.g., {@link OperationType#JOIN}).
	 * @return The number of operands for the operation of this node (arity).
	 * @see OperationType#getArity()
	 */
	public int getArity(){
		return operands.size();
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
	 * If the operation for this AST node is {@link OperationType#EDGE}, returns
	 * the atomic edge query operation associated with this AST node.
	 * @return The atomic edge operation at this AST leaf.
	 * @throws IllegalStateException When the operation for this AST node
	 *         is not equal to {@link OperationType#EDGE}.
	 */
	public EdgeQueryAtom getEdgeAtom() throws IllegalStateException{
		if(getOperation() != OperationType.EDGE){
			throw new IllegalStateException("Query fragment is not an AST edge leaf.");
		}
		
		return ((EdgeQueryAtom)fragment);
	}
	
	/**
	 * If this AST node corresponds to a leaf of the AST, gets the
	 * atomic query operation associated with this AST node.
	 * @return The atomic operation at this AST leaf.
	 * @throws IllegalStateException When this AST node is not a leaf node.
	 */
	public QueryAtom getAtom() throws IllegalStateException{
		if(fragment instanceof QueryAtom atom){
			return atom;
		}else{
			throw new IllegalStateException("Query fragment is not an AST leaf.");
		}
	}
	
	/**
	 * Gets the n-th child node of this AST node if any. This child node is
	 * only present if the operation for this AST node has an arity less than
	 * the given value of n.
	 * @param n The n-th operand to get, 0-based offset, i.e., the only operand
	 *        for an operation of arity 1 is at index 0.
	 * @return The left child node of this AST node or null if this node is a leaf.
	 * @see OperationType
	 */
	public QueryTree getOperand(int n){
		return operands.get(n);
	}
	
	/**
	 * Returns a stream over all the AST nodes in the sub-tree rooted at this
	 * AST node, including this AST node itself. The tree will be traversed in
	 * a top down manner with left child nodes appearing before right child nodes.
	 * @return A stream over all AST nodes in the sub-tree rooted at this AST node.
	 */
	public Stream<QueryTree> stream(){
		Stream<QueryTree> stream = Stream.of(this);
		
		for(QueryTree child : operands){
			stream = Stream.concat(stream, child.stream());
		}
		
		return stream;
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
			
			for(QueryTree child : operands){
				child.writeAST(writer);
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
	public static QueryTree ofAtom(QueryAtom fragment){
		return new QueryTree(List.of(), fragment);
	}
	
	/**
	 * Constructs an AST node for a query fragment representing a unary operation.
	 * @param left The AST representing the input to the operation in the given query fragment.
	 * @param fragment The query fragment.
	 * @return The AST for the given query fragment.
	 * @see OperationType#isUnary()
	 */
	public static QueryTree ofUnary(QueryTree left, QueryFragment fragment){
		return new QueryTree(List.of(left), fragment);
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
		return new QueryTree(List.of(left, right), fragment);
	}
	
	/**
	 * Constructs an AST node for a query fragment representing an n-ary operation.
	 * @param inputs The child nodes of this AST nodes, these are the inputs for the n-ary operation.
	 * @param fragment The query fragment.
	 * @return The AST for the given query fragment.
	 */
	public static QueryTree ofNAry(List<QueryTree> inputs, QueryFragment fragment){
		return new QueryTree(inputs, fragment);
	}
}
