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
package dev.roanh.gmark.lang.cpq;

import java.util.List;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.Util;

/**
 * Simple generator for CPQs (Conjunctive Path Queries).
 * @author Roan
 * @see CPQ
 * @see <a href="https://research.roanh.dev/cpqkeys/CPQ%20Keys%20v1.1.pdf#subsubsection.2.5.1">CPQ Keys: Plain CPQ Generation</a>
 */
public final class GeneratorCPQ{

	/**
	 * Prevent instantiation.
	 */
	private GeneratorCPQ(){
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
	 * @see CPQ
	 * @see <a href="https://cpqkeys.roanh.dev/notes/cpq_definition">CPQ Definition</a>
	 */
	public static CPQ generatePlainCPQ(int ruleApplications, int labels) throws IllegalArgumentException{
		return generatePlainCPQ(ruleApplications, Util.generateLabels(labels));
	}
	
	/**
	 * Generates a random CPQ created by applying the intersection
	 * (conjunction) and concatenation steps from the CPQ grammar
	 * the given number of times. Labels and inverse labels are
	 * drawn from the given set of labels. Three relatively meaningless
	 * but technically valid patterns are intentionally never generated:
	 * <ol>
	 * <li>Concatenation with identity.</li>
	 * <li>Intersection of identity with identity.</li>
	 * <li>The query consisting of only identity and nothing else.</li>
	 * </ol>
	 * @param ruleApplications The number of times the intersection and
	 *        concatenation steps are allowed to be applied.
	 * @param labels The set of labels to draw from.
	 * @return The randomly generated CPQ.
	 * @throws IllegalArgumentException When the list of labels is empty.
	 * @see CPQ
	 * @see <a href="https://cpqkeys.roanh.dev/notes/cpq_definition">CPQ Definition</a>
	 */
	public static CPQ generatePlainCPQ(int ruleApplications, List<Predicate> labels) throws IllegalArgumentException{
		if(labels.isEmpty()){
			throw new IllegalArgumentException("List of labels cannot be empty.");
		}
		
		return generatePlainCPQ(ruleApplications, false, labels);
	}
	
	/**
	 * Generates a random CPQ created by applying the intersection
	 * (conjunction) and concatenation steps from the CPQ grammar
	 * the given number of times. Labels and inverse labels are
	 * drawn from the given set of labels. Two relatively meaningless
	 * but technically valid patterns are intentionally never generated:
	 * <ol>
	 * <li>Concatenation with identity.</li>
	 * <li>Intersection of identity with identity.</li>
	 * </ol>
	 * @param ruleApplications The number of times the intersection and
	 *        concatenation steps are allowed to be applied.
	 * @param allowId Whether the result of this call is allowed to be
	 *        the identity CPQ.
	 * @param labels The set of labels to draw from.
	 * @return The randomly generated CPQ.
	 */
	private static CPQ generatePlainCPQ(int ruleApplications, boolean allowId, List<Predicate> labels){
		if(ruleApplications == 0){
			if(allowId && Util.getRandom().nextBoolean()){
				return CPQ.IDENTITY;
			}else{
				//edge label
				Predicate label = Util.selectRandom(labels);
				return CPQ.label(Util.getRandom().nextBoolean() ? label : label.getInverse());
			}
		}else{
			if(Util.getRandom().nextBoolean()){
				//concatenation
				int split = Util.uniformRandom(0, ruleApplications - 1);
				return CPQ.concat(generatePlainCPQ(split, false, labels), generatePlainCPQ(ruleApplications - split - 1, false, labels));
			}else{
				//intersect
				int split = Util.uniformRandom(0, ruleApplications - 1);
				return CPQ.intersect(generatePlainCPQ(split, true, labels), generatePlainCPQ(ruleApplications - split - 1, false, labels));
			}
		}
	}
}
