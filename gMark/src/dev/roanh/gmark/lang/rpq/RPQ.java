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
package dev.roanh.gmark.lang.rpq;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.lang.QueryLanguage;
import dev.roanh.gmark.lang.QueryLanguageSyntax;

/**
 * Interface for regular path queries (RPQs).
 * @author Roan
 * @see <a href="https://en.wikipedia.org/wiki/Regular_path_query/">Regular Path Query</a>
 */
public abstract interface RPQ extends QueryLanguageSyntax{

	@Override
	public default QueryLanguage getQueryLanguage(){
		return QueryLanguage.RPQ;
	}
	
	/**
	 * returns a RPQ representing the transitive closure of the given RPQ.
	 * @param rpq The RPQ for the transitive closure.
	 * @return The transitive closure of the given RPQ.
	 */
	public static RPQ kleene(RPQ rpq){
		return new KleeneRPQ(rpq);
	}

	/**
	 * Returns a RPQ representing the disjunction of the given RPQs
	 * from left to right. For example {@code ((q1 ∪ q2) ∪ q3)}.
	 * @param rpqs The RPQs for the disjunction.
	 * @return The disjunction of the given RPQs.
	 * @throws IllegalArgumentException When less than 2 RPQs are provided.
	 */
	public static RPQ disjunct(RPQ... rpqs){
		return disjunct(Arrays.asList(rpqs));
	}
	
	/**
	 * Returns a RPQ representing the disjunction of the given RPQs
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
	 * Parses the given RPQ in string form to an RPQ instance. The input is assumed
	 * to use brackets where possible and to use the '{@value QueryLanguageSyntax#CHAR_CUP}',
	 * '{@value QueryLanguageSyntax#CHAR_JOIN}', '{@value QueryLanguageSyntax#CHAR_KLEENE}' and
	 * '{@value QueryLanguageSyntax#CHAR_INVERSE}' symbols to denote operations.
	 * Example input: {@code (0◦(((1◦0) ∪ (1◦1))◦1⁻))}.
	 * @param query The RPQ to parse.
	 * @return The parsed RPQ.
	 * @throws IllegalArgumentException When the given string is not a valid RPQ.
	 * @see ParserRPQ#parse(String, char, char, char, char)
	 */
	public static RPQ parse(String query) throws IllegalArgumentException{
		return ParserRPQ.parse(query);
	}
}
