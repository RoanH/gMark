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
package dev.roanh.gmark.lang.rpq;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.lang.QueryLanguage;
import dev.roanh.gmark.lang.QueryLanguageSyntax;

/**
 * Interface for regular path queries (RPQs).
 * @author Roan
 * @see <a href="https://en.wikipedia.org/wiki/Regular_path_query">Regular Path Query</a>
 */
public abstract interface RPQ extends QueryLanguageSyntax{

	@Override
	public default QueryLanguage getQueryLanguage(){
		return QueryLanguage.RPQ;
	}
	
	/**
	 * Returns an RPQ representing the transitive closure of the given RPQ.
	 * @param rpq The RPQ for the transitive closure.
	 * @return The transitive closure of the given RPQ.
	 */
	public static RPQ kleene(RPQ rpq){
		return new KleeneRPQ(rpq);
	}
	
	/**
	 * Returns an RPQ representing the transitive closure of the disjunction
	 * of the given set of labels. 
	 * @param labels The labels for the transitive closure.
	 * @return The transitive closure of disjunction of the given labels.
	 * @throws IllegalArgumentException When the array of labels is empty.
	 */
	public static RPQ kleene(Predicate... labels){
		return labels.length == 1 ? kleene(label(labels[0])) : kleene(disjunct(labels));
	}
	
	/**
	 * Returns an RPQ representing the disjunction of the given labels
	 * from left to right. For example {@code ((l1 ∪ l2) ∪ l3)}.
	 * @param labels The labels for the disjunction.
	 * @return The disjunction of the given labels.
	 * @throws IllegalArgumentException When less than 2 labels are provided.
	 */
	public static RPQ disjunct(Predicate... labels){
		return disjunct(Arrays.stream(labels).map(RPQ::label).toList());
	}

	/**
	 * Returns an RPQ representing the disjunction of the given RPQs
	 * from left to right. For example {@code ((q1 ∪ q2) ∪ q3)}.
	 * @param rpqs The RPQs for the disjunction.
	 * @return The disjunction of the given RPQs.
	 * @throws IllegalArgumentException When less than 2 RPQs are provided.
	 */
	public static RPQ disjunct(RPQ... rpqs){
		return disjunct(Arrays.asList(rpqs));
	}
	
	/**
	 * Returns an RPQ representing the disjunction of the given RPQs
	 * from left to right. For example {@code ((q1 ∪ q2) ∪ q3)}.
	 * Note that the given list is not copied.
	 * @param rpqs The RPQs for the disjunction.
	 * @return The disjunction of the given RPQs.
	 * @throws IllegalArgumentException When less than 2 RPQs are provided.
	 */
	public static RPQ disjunct(List<RPQ> rpqs){
		return new DisjunctionRPQ(rpqs);
	}
	
	/**
	 * Returns an RPQ representing the concatenation in order
	 * of the given RPQs.
	 * @param rpqs The RPQs to concatenate (in order).
	 * @return The concatenation of the given RPQs.
	 * @throws IllegalArgumentException When the given list
	 *         of RPQs is empty.
	 */
	public static RPQ concat(RPQ... rpqs) throws IllegalArgumentException{
		return new ConcatRPQ(Arrays.asList(rpqs));
	}
	
	/**
	 * Returns an RPQ representing the concatenation in order
	 * of the given RPQs. Note that the given list is not copied.
	 * @param rpqs The RPQs to concatenate (in order).
	 * @return The concatenation of the given RPQs.
	 * @throws IllegalArgumentException When the given list
	 *         of RPQs is empty.
	 */
	public static RPQ concat(List<RPQ> rpqs) throws IllegalArgumentException{
		return new ConcatRPQ(rpqs);
	}
	
	/**
	 * Returns an RPQ representing a single labelled edge traversal.
	 * @param label The label of the traversed edge.
	 * @return The label traversal RPQ.
	 */
	public static RPQ label(Predicate label){
		return new EdgeRPQ(label);
	}
	
	/**
	 * Returns an RPQ representing a chain of labelled edge traversals.
	 * @param labels The labels of the traversed edges.
	 * @return The label traversal RPQ.
	 */
	public static RPQ labels(Predicate... labels){
		return new ConcatRPQ(Arrays.stream(labels).map(EdgeRPQ::new).collect(Collectors.toList()));
	}
	
	/**
	 * Returns an RPQ representing a chain of labelled edge traversals.
	 * @param labels The labels of the traversed edges.
	 * @return The label traversal RPQ.
	 */
	public static RPQ labels(List<Predicate> labels){
		return new ConcatRPQ(labels.stream().map(EdgeRPQ::new).collect(Collectors.toList()));
	}
	
	/**
	 * Generates a random RPQ created by applying the disjunction,
	 * Kleene and concatenation steps from the RPQ grammar the
	 * given number of times. A set of labels of the given size
	 * is automatically generated together with corresponding inverse
	 * labels for each label. It is worth noting that concatenation
	 * will be generated at double the rate of the relatively more
	 * expensive Kleene and disjunction operations and that nested
	 * transitive closures without additional structure are not generated.
	 * @param ruleApplications The number of times the disjunction,
	 *        Kleene, and concatenation steps are allowed to be applied.
	 * @param labels The number of distinct labels to use (upper limit).
	 * @return The randomly generated RPQ.
	 * @throws IllegalArgumentException When the given number of labels is 0 or less.
	 * @see GeneratorRPQ#generatePlainRPQ(int, List)
	 */
	public static RPQ generatePlainRPQ(int ruleApplications, int labels) throws IllegalArgumentException{
		return GeneratorRPQ.generatePlainRPQ(ruleApplications, labels);
	}
	
	/**
	 * Parses the given RPQ in string form to an RPQ instance. The input is assumed
	 * to use brackets where possible and to use the '{@value QueryLanguageSyntax#CHAR_DISJUNCTION}',
	 * '{@value QueryLanguageSyntax#CHAR_JOIN}', '{@value QueryLanguageSyntax#CHAR_KLEENE}' and
	 * '{@value QueryLanguageSyntax#CHAR_INVERSE}' symbols to denote operations.
	 * Example input: {@code (0◦(((1◦0) ∪ (1◦1))◦1⁻))}.
	 * @param query The RPQ to parse.
	 * @return The parsed RPQ.
	 * @throws IllegalArgumentException When the given string is not a valid RPQ.
	 * @see ParserRPQ#parse(String, char, char, char, char)
	 * @see QueryLanguageSyntax
	 */
	public static RPQ parse(String query) throws IllegalArgumentException{
		return ParserRPQ.parse(query);
	}
	
	/**
	 * Parses the given RPQ in string form to an RPQ instance. The input is assumed
	 * to use brackets where possible and to use the '{@value QueryLanguageSyntax#CHAR_DISJUNCTION}',
	 * '{@value QueryLanguageSyntax#CHAR_JOIN}', '{@value QueryLanguageSyntax#CHAR_KLEENE}' and
	 * '{@value QueryLanguageSyntax#CHAR_INVERSE}' symbols to denote operations.
	 * Example input: {@code (0◦(((1◦0) ∪ (1◦1))◦1⁻))}.
	 * @param query The RPQ to parse.
	 * @param labels The label set to use, new labels will be created if labels are found in the
	 *        input that are not covered by the given list.
	 * @return The parsed RPQ.
	 * @throws IllegalArgumentException When the given string is not a valid RPQ.
	 * @see ParserRPQ#parse(String, Map, char, char, char, char)
	 * @see QueryLanguageSyntax
	 */
	public static RPQ parse(String query, List<Predicate> labels) throws IllegalArgumentException{
		return ParserRPQ.parse(query, labels);
	}
	
	/**
	 * Attempts to parse the given AST to an RPQ.
	 * @param ast The AST to parse to a RPQ.
	 * @return The RPQ represented by the given AST.
	 * @throws IllegalArgumentException When the given AST does
	 *         not represent a valid RPQ.
	 * @see QueryTree
	 */
	public static RPQ parse(QueryTree ast) throws IllegalArgumentException{
		switch(ast.getOperation()){
		case CONCATENATION:
			return concat(parse(ast.getLeft()), parse(ast.getRight()));
		case DISJUNCTION:
			return disjunct(parse(ast.getLeft()), parse(ast.getLeft()));
		case EDGE:
			return label(ast.getPredicate());
		case KLEENE:
			return kleene(parse(ast.getLeft()));
		default:
			throw new IllegalArgumentException("The given AST contains operations that are not part of the RPQ query language.");
		}
	}
}
