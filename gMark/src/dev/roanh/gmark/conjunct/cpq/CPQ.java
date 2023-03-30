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
package dev.roanh.gmark.conjunct.cpq;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dev.roanh.gmark.conjunct.cpq.QueryGraphCPQ.Vertex;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.output.OutputSQL;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Interface for conjunctive path queries (CPQs).
 * @author Roan
 * @see <a href="https://cpqkeys.roanh.dev/notes/cpq_definition">CPQ Definition</a>
 */
public abstract interface CPQ extends OutputSQL, OutputXML{
	/**
	 * The character used to denote the intersection/conjunction operator.
	 */
	public static final char CHAR_CAP = '∩';
	/**
	 * The character used to denote the join/concatenation operator.
	 */
	public static final char CHAR_JOIN = '◦';
	/**
	 * Constant for the special identity CPQ.
	 */
	public static final CPQ IDENTITY = new CPQ(){
		@Override
		public String toString(){
			return "id";
		}

		@Override
		public String toSQL(){
			throw new IllegalStateException("Identity to OutputSQL not supported (and never generated).");
		}

		@Override
		public void writeXML(IndentWriter writer){
			writer.println("<cpq type=\"identity\"></cpq>");
		}

		@Override
		public QueryGraphCPQ toQueryGraph(Vertex source, Vertex target){
			return new QueryGraphCPQ(source, target);
		}
	};
	
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
	 * Computes and returns the query graph for this CPQ using
	 * the given source and target vertices for the computation.
	 * Usually using {@link #toQueryGraph()} instead should be
	 * sufficient for most use cases.
	 * @param source The source vertex to use.
	 * @param target The target vertex to use.
	 * @return The query graph for this CPQ.
	 * @see QueryGraphCPQ
	 * @see #toQueryGraph()
	 */
	public abstract QueryGraphCPQ toQueryGraph(Vertex source, Vertex target);
	
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
	 * of the two given CPQs.
	 * @param first The first CPQ.
	 * @param second The second CPQ.
	 * @return The intersection of the given CPQs.
	 */
	public static CPQ intersect(CPQ first, CPQ second){
		return new IntersectionCPQ(first, second);
	}
	
	/**
	 * Returns a CPQ representing the intersection (conjunction)
	 * of the two given single edge label traversals.
	 * @param first The first edge label.
	 * @param second The second edge label.
	 * @return The intersection of the given label traversals.
	 */
	public static CPQ intersect(Predicate first, Predicate second){
		return new IntersectionCPQ(label(first), label(second));
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
	 * @param cpqs The CPQs to intersect.
	 * @return The intersection of the given CPQs.
	 * @throws IllegalArgumentException When less than 2 CPQs are provided.
	 */
	public static CPQ intersect(List<CPQ> cpqs) throws IllegalArgumentException{
		if(cpqs.size() < 2){
			throw new IllegalArgumentException("Not enough CPQs given (need at least 2)");
		}
		
		CPQ cpq = intersect(cpqs.get(0), cpqs.get(1));
		for(int i = 2; i < cpqs.size(); i++){
			cpq = intersect(cpq, cpqs.get(i));
		}
		
		return cpq;
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
	 * of the given CPQs.
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
	 * @throws IllegalArgumentException When the list of labels is empty.
	 * @see <a href="https://cpqkeys.roanh.dev/notes/cpq_definition">CPQ Definition</a>
	 * @see GeneratorCPQ#generatePlainCPQ(int, java.util.List)
	 */
	public static CPQ generateRandomCPQ(int ruleApplications, int labels) throws IllegalArgumentException{
		return GeneratorCPQ.generatePlainCPQ(ruleApplications, labels);
	}
	
	/**
	 * Parses the given CPQ in string form to a CPQ instance. The input is assumed
	 * to use brackets where possible and to use the '{@code id}', '{@value CPQ#CHAR_JOIN}',
	 * '{@value CPQ#CHAR_CAP}' and '{@value Predicate#CHAR_INVERSE}' symbols to denote
	 * operations. Example input: {@code (0◦(((1◦0) ∩ (1◦1))◦1⁻))}.
	 * @param query The CPQ to parse.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ.
	 * @see GeneratorCPQ#parse(String, char, char, char)
	 */
	public static CPQ parse(String query) throws IllegalArgumentException{
		return GeneratorCPQ.parse(query);
	}
}
