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
package dev.roanh.gmark.lang.cpq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.lang.QueryLanguage;
import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.lang.cpq.QueryGraphCPQ.Vertex;
import dev.roanh.gmark.type.schema.Predicate;

/**
 * Interface for conjunctive path queries (CPQs).
 * @author Roan
 * @see <a href="https://research.roanh.dev/Indexing%20Conjunctive%20Path%20Queries%20for%20Accelerated%20Query%20Evaluation.pdf#subsection.2.2.2">
 *      Indexing Conjunctive Path Queries for Accelerated Query Evaluation, Section 2.2.2: Conjunctive Path Queries</a>
 * @see <a href="https://cpqkeys.roanh.dev/notes/cpq_definition">CPQ Definition</a>
 */
public abstract interface CPQ extends QueryLanguageSyntax{
	/**
	 * Constant for the special identity CPQ.
	 */
	public static final CPQ IDENTITY = new IdentityCPQ();
	
	/**
	 * Computes and returns the query graph for this CPQ.
	 * @return The query graph for this CPQ.
	 * @see QueryGraphCPQ
	 */
	public default QueryGraphCPQ toQueryGraph(){
		QueryGraphCPQ graph = toQueryGraph(new Vertex(), new Vertex());
		graph.merge();
		return graph;
	}
	
	/**
	 * Computes and returns the core of the query graph for this CPQ.
	 * @return The core of this CPQ.
	 * @see QueryGraphCPQ
	 * @see QueryGraphCPQ#computeCore()
	 */
	public default QueryGraphCPQ computeCore(){
		return toQueryGraph().computeCore();
	}
	
	/**
	 * Tests if the query graph for this CPQ is homomorphic
	 * to the query graph for the given other CPQ.
	 * @param other The other CPQ to test against.
	 * @return True if this CPQ is homorphic to the other CPQ.
	 * @see #toQueryGraph()
	 * @see QueryGraphCPQ#isHomomorphicTo(QueryGraphCPQ)
	 */
	public default boolean isHomomorphicTo(CPQ other){
		return toQueryGraph().isHomomorphicTo(other.toQueryGraph());
	}

	@Override
	public default QueryLanguage getQueryLanguage(){
		return QueryLanguage.CPQ;
	}
	
	/**
	 * Computes and returns the query graph for this CPQ using
	 * the given source and target vertices for the computation.
	 * Usually using {@link #toQueryGraph()} instead should be
	 * sufficient for most use cases, using this version instead
	 * can lead to unexpected results.
	 * @param source The source vertex to use.
	 * @param target The target vertex to use.
	 * @return The query graph for this CPQ.
	 * @see QueryGraphCPQ
	 * @see #toQueryGraph()
	 */
	public abstract QueryGraphCPQ toQueryGraph(Vertex source, Vertex target);
	
	/**
	 * Returns the diameter of this CPQ. The diameter of a CPQ is
	 * the largest number of labels to which the join operation is
	 * applied. A simpler way to think of this concept is the length
	 * of the largest path through the CPQ from source to target.
	 * @return The diameter of this CPQ.
	 */
	public abstract int getDiameter();
	
	/**
	 * Tests if this CPQ is a loop, meaning its source and target
	 * are the same. When visualised as a query graph this means
	 * that the source and target node are the same node. In its
	 * grammar form this means that there is an intersection with
	 * identity at the highest level in the CPQ.
	 * @return True if this CPQ is a loop.
	 * @see QueryGraphCPQ
	 */
	public abstract boolean isLoop();
	
	/**
	 * Returns the identity CPQ.
	 * @return The identity CPQ.
	 * @see #IDENTITY
	 */
	public static CPQ id(){
		return IDENTITY;
	}
	
	/**
	 * Returns a CPQ representing the intersection (conjunction)
	 * of the two given single edge label traversals.
	 * @param predicates The labels to traverse.
	 * @return The intersection of the given label traversals.
	 * @throws IllegalArgumentException When less than 2 predicates are provided.
	 */
	public static CPQ intersect(Predicate... predicates) throws IllegalArgumentException{
		List<CPQ> labels = new ArrayList<CPQ>(predicates.length);
		for(Predicate p : predicates){
			labels.add(label(p));
		}
		return new IntersectionCPQ(labels);
	}
	
	/**
	 * Returns a CPQ representing the intersection of the given CPQs
	 * from left to right. For example {@code ((q1 ∩ q2) ∩ q3)}.
	 * @param cpqs The CPQs to intersect.
	 * @return The intersection of the given CPQs.
	 * @throws IllegalArgumentException When less than 2 CPQs are provided.
	 */
	public static CPQ intersect(CPQ... cpqs) throws IllegalArgumentException{
		return intersect(Arrays.asList(cpqs));
	}
	
	/**
	 * Returns a CPQ representing the intersection of the given CPQs
	 * from left to right. For example {@code ((q1 ∩ q2) ∩ q3)}.
	 * Note that the given list is not copied.
	 * @param cpqs The CPQs to intersect.
	 * @return The intersection of the given CPQs.
	 * @throws IllegalArgumentException When less than 2 CPQs are provided.
	 */
	public static CPQ intersect(List<CPQ> cpqs) throws IllegalArgumentException{
		return new IntersectionCPQ(cpqs);
	}
	
	/**
	 * Returns a CPQ representing the concatenation in order
	 * of the given CPQs.
	 * @param cpqs The CPQs to concatenate (in order).
	 * @return The concatenation of the given CPQs.
	 * @throws IllegalArgumentException When the given list
	 *         of CPQs is empty.
	 */
	public static CPQ concat(CPQ... cpqs) throws IllegalArgumentException{
		return new ConcatCPQ(Arrays.asList(cpqs));
	}
	
	/**
	 * Returns a CPQ representing the concatenation in order
	 * of the given CPQs. Note that the given list is not copied.
	 * @param cpqs The CPQs to concatenate (in order).
	 * @return The concatenation of the given CPQs.
	 * @throws IllegalArgumentException When the given list
	 *         of CPQs is empty.
	 */
	public static CPQ concat(List<CPQ> cpqs) throws IllegalArgumentException{
		return new ConcatCPQ(cpqs);
	}
	
	/**
	 * Returns a CPQ representing a single labelled edge traversal.
	 * @param label The label of the traversed edge.
	 * @return The label traversal CPQ.
	 */
	public static CPQ label(Predicate label){
		return new EdgeCPQ(label);
	}
	
	/**
	 * Returns a CPQ representing a chain of labelled edge traversals.
	 * @param labels The labels of the traversed edges.
	 * @return The label traversal CPQ.
	 */
	public static CPQ labels(Predicate... labels){
		return new ConcatCPQ(Arrays.stream(labels).map(EdgeCPQ::new).collect(Collectors.toList()));
	}
	
	/**
	 * Returns a CPQ representing a chain of labelled edge traversals.
	 * @param labels The labels of the traversed edges.
	 * @return The label traversal CPQ.
	 */
	public static CPQ labels(List<Predicate> labels){
		return new ConcatCPQ(labels.stream().map(EdgeCPQ::new).collect(Collectors.toList()));
	}
	
	/**
	 * Generates a random CPQ created by applying the intersection
	 * (conjunction) and concatenation steps from the CPQ grammar
	 * the given number of times. A set of labels of the given size
	 * is automatically generated together with corresponding inverse
	 * labels for each label. Three relatively meaningless but technically
	 * valid patterns are intentionally never generated:
	 * <ol>
	 * <li>Concatenation with identity.</li>
	 * <li>Intersection of identity with identity.</li>
	 * <li>The query consisting of only identity and nothing else.</li>
	 * </ol>
	 * @param ruleApplications The number of times the intersection and
	 *        concatenation steps are allowed to be applied.
	 * @param labels The number of distinct labels to use (upper limit).
	 * @return The randomly generated CPQ.
	 * @throws IllegalArgumentException When the given number of labels is 0 or less.
	 * @see <a href="https://cpqkeys.roanh.dev/notes/cpq_definition">CPQ Definition</a>
	 * @see GeneratorCPQ#generatePlainCPQ(int, java.util.List)
	 */
	public static CPQ generateRandomCPQ(int ruleApplications, int labels) throws IllegalArgumentException{
		return GeneratorCPQ.generatePlainCPQ(ruleApplications, labels);
	}
	
	/**
	 * Parses the given CPQ in string form to a CPQ instance. The input is assumed
	 * to use brackets where possible and to use the '{@code id}', '{@value QueryLanguageSyntax#CHAR_JOIN}',
	 * '{@value QueryLanguageSyntax#CHAR_INTERSECTION}' and '{@value QueryLanguageSyntax#CHAR_INVERSE}' symbols to denote
	 * operations. Example input: {@code (0◦(((1◦0) ∩ (1◦1))◦1⁻))}.
	 * @param query The CPQ to parse.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ.
	 * @see ParserCPQ#parse(String, char, char, char)
	 * @see QueryLanguageSyntax
	 */
	public static CPQ parse(String query) throws IllegalArgumentException{
		return ParserCPQ.parse(query);
	}
	
	/**
	 * Parses the given CPQ in string form to a CPQ instance. The input is assumed
	 * to use brackets where possible and to use the '{@code id}', '{@value QueryLanguageSyntax#CHAR_JOIN}',
	 * '{@value QueryLanguageSyntax#CHAR_INTERSECTION}' and '{@value QueryLanguageSyntax#CHAR_INVERSE}' symbols to denote
	 * operations. Example input: {@code (0◦(((1◦0) ∩ (1◦1))◦1⁻))}.
	 * @param query The CPQ to parse.
	 * @param labels The label set to use, this list should only contain forward labels. New labels will be created
	 *        if labels are found in the input that are not covered by the given list.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ.
	 * @see ParserCPQ#parse(String, Map, char, char, char)
	 * @see QueryLanguageSyntax
	 */
	public static CPQ parse(String query, List<Predicate> labels) throws IllegalArgumentException{
		return ParserCPQ.parse(query, labels);
	}
	
	/**
	 * Attempts to parse the given AST to a CPQ.
	 * @param ast The AST to parse to a CPQ.
	 * @return The CPQ represented by the given AST.
	 * @throws IllegalArgumentException When the given AST does
	 *         not represent a valid CPQ.
	 * @see QueryTree
	 */
	public static CPQ parse(QueryTree ast) throws IllegalArgumentException{
		switch(ast.getOperation()){
		case CONCATENATION:
			return concat(parse(ast.getLeft()), parse(ast.getRight()));
		case EDGE:
			return label(ast.getPredicate());
		case IDENTITY:
			return id();
		case INTERSECTION:
			return intersect(parse(ast.getLeft()), parse(ast.getRight()));
		default:
			throw new IllegalArgumentException("The given AST contains operations that are not part of the CPQ query language.");
		}
	}
}
