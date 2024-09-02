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

/**
 * Enumeration of database operations. These are the individual operations
 * that query languages are constructed with that can be executed directly
 * by a database without having to further decompose the query. Every node
 * in the {@link QueryTree AST} of a query is associated with exactly one operation.
 * <p>
 * There are three types of operations based on the number of operands required:
 * <ul><li><b>Atom</b>: These operations are leaf nodes in the query AST and
 * thus take no input from other AST nodes, meaning they selected data
 * directly from the database graph.</li>
 * <li><b>Unary</b>: These are operations that only take a single input, thus
 * the AST nodes for these operations have exactly one child node in the query
 * AST that produces their input.</li>
 * <li><b>Binary</b>: These are operations that take exactly two inputs, thus
 * the AST nodes for these operations have exactly two child nodes in the query
 * AST that produce their input.</li>
 * </ul>
 * @author Roan
 * @see QueryTree QueryTree (AST)
 */
public enum OperationType{
	/**
	 * The <i>concatenation</i> or <i>join</i> operation is a binary operation
	 * that joins its two input graphs at shared vertices such that paths from
	 * the left input graph are extended with paths in the right input graph.
	 * <p>
	 * For example, if the left input graph contains the path from vertex
	 * <code>v</code> to vertex <code>m</code> denoted as <code>(v, m)</code>,
	 * and the right input graph contains the path from vertex <code>m</code>
	 * to vertex <code>u</code> denoted as <code>(m, u)</code>. Then one of
	 * the paths in the output result graph will be <code>(v, u)</code>, the
	 * path from vertex <code>v</code> to vertex <code>u</code>.
	 */
	CONCATENATION(2),
	INTERSECTION(2),//conjunction
	EDGE(0),//predicate, label
	IDENTITY(0),
	KLEENE(1),
	DISJUNCTION(2);
	
	/**
	 * The number of operands (inputs) required to execute this operation.
	 */
	private final int operands;

	/**
	 * Constructs a new operation type with the given number of operands.
	 * @param operands The number of operands required for this operation.
	 */
	private OperationType(int operands){
		this.operands = operands;
	}
	
	/**
	 * Checks if this operation is an atom or leaf node of the query AST, meaning
	 * it cannot be decomposed any further and has no AST child nodes. This in turn
	 * implies that the number of operands for the operation is 0 and that the operation
	 * executes directly on the database graph.
	 * @return True if this operation is an atom.
	 * @see QueryTree QueryTree (AST)
	 */
	public boolean isAtom(){
		return operands == 0;
	}
	
	/**
	 * Check if this a unary operation, meaning it takes exactly one operand and has
	 * exactly one child node in the query AST.
	 * @return True if this operation is unary.
	 * @see QueryTree QueryTree (AST)
	 */
	public boolean isUnary(){
		return operands == 1;
	}
	
	/**
	 * Check if this a binary operation, meaning it takes exactly two operands and has
	 * exactly two child nodes in the query AST.
	 * @return True if this operation is binary.
	 * @see QueryTree QueryTree (AST)
	 */
	public boolean isBinary(){
		return operands == 2;
	}
	
	/**
	 * Gets the arity of this operator, that is, the number of arguments or operands it takes.
	 * @return The arity of this operator.
	 */
	public int getArity(){
		return operands;
	}
}
