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
 * </ul><p>
 * It is worth noting that the result of each operation is a set of paths that
 * are represented by their source and target vertices in the format <code>
 * (source, target)</code>.
 * @author Roan
 * @see QueryTree QueryTree (AST)
 * @see <a href="https://research.roanh.dev/Graph%20Database%20&%20Query%20Evaluation%20Terminology%20v1.3.pdf">
 *      Graph Database &amp; Query Evaluation Terminology</a>
 */
public enum OperationType{
	/**
	 * The <i>concatenation</i> or (limited) <i>join</i> operation is a binary operation
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
	/**
	 * The <i>intersection</i> or <i>conjunction</i> operation is a binary operation
	 * that intersects its two input graphs such that paths from the left input graph
	 * are only included in the output if they are also present in the right input graph.
	 * <p>
	 * For example, if the left input graph contains the paths <code>(a, b)</code> and
	 * <code>(c, d)</code> and the right input graph contains the paths <code>(b, c)</code>
	 * and <code>(a, b)</code>, then the only path in the output is <code>(a, b)</code>.
	 */
	INTERSECTION(2),
	/**
	 * The <i>edge</i>, <i>predicate</i>, or <i>label</i> operation is an atom that executes
	 * directly on the database graph to select all the edges with a specific edge label.
	 * Optionally edges can be selected in inverse direction, meaning the source and target
	 * vertices in the output paths are reversed.
	 * <p>
	 * For example, if the database graph contains the path from vertex <code>1</code> to
	 * vertex <code>2</code> with label <code>a</code> and we select all edges with label
	 * <code>a</code>, then the output contains the path <code>(1, 2)</code>. Similarly, if
	 * we select all edges with label <code>a</code> in inverse direction, then the output
	 * contains the path <code>(2, 1)</code>.
	 */
	EDGE(0),
	/**
	 * The <i>identity</i> operation is an atom that executes directly on the database
	 * graph to select all the vertices in the graph. Alternatively, one can think of this
	 * operation as selecting all length 0 paths from the database.
	 * <p>
	 * For example, if the database graphs contains the nodes <code>1</code>, <code>2</code>,
	 * and <code>3</code>, then the output graph will contain <code>(1, 1)</code>, <code>(2, 2)</code>,
	 * and <code>(3, 3)</code>.
	 */
	IDENTITY(0),
	/**
	 * The <i>Kleene</i> or <i>transitive closure</i> operation is a unary operation that
	 * computes the transitive closure of its input graph. Note that the transitive closure
	 * of a relation <i>R</i> is the smallest binary relation of set the set of all distinct
	 * source and target vertices in <i>R</i> that both contains <i>R</i> and is transitive.
	 * <p>
	 * For example, if the input graph contains the paths <code>(1, 2)</code> and <code>(2, 3)</code>,
	 * then the output graph contains <code>(1, 2)</code>, <code>(2, 3)</code> and <code>(1, 3)</code>
	 */
	KLEENE(1),
	/**
	 * The <i>disjunction</i> operation is a binary operation that computes the disjunction
	 * for its two input graphs such that paths are present in the output graph when that
	 * are present in either the left input graph or the right input graph.
	 * <p>
	 * For example, if the left input graph contains the path <code>(a, b)</code> and
	 * and the right input graph contains the path <code>(b, c)</code>, then the output
	 * contains both <code>(a, b)</code> and <code>(b, c)</code>.
	 */
	DISJUNCTION(2),
	/**
	 * The <i>join</i> operation is an operation of variable arity that joins its operands on
	 * all the shared vertices between them. It is the general case of the {@link #CONCATENATION}
	 * operation, which is limited to just two operands.
	 */
	JOIN(-1);
	
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
	 * @return True if this operation is atomic.
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
	 * @return The arity of this operator, will be -1 the operator can be applied to any number of inputs (n-ary).
	 */
	public int getArity(){
		return operands;
	}
	
	/**
	 * Checks if the arity of this operation is fixed.
	 * @return True if this operation has a fixed arity, false if it
	 *         is variable (i.e., the operation accepts any number of inputs).
	 */
	public boolean hasBoundedArity(){
		return operands != -1;
	}
}
