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

import java.util.List;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.Util;

/**
 * Simple generator for RPQs (Regular Path Queries).
 * @author Roan
 * @see RPQ
 */
public final class GeneratorRPQ{

	/**
	 * Prevent instantiation.
	 */
	private GeneratorRPQ(){
	}

	/**
	 * Generates a random RPQ created by applying the disjunction,
	 * Kleene and concatenation steps from the RPQ grammar the
	 * given number of times. A set of labels of the given size
	 * is automatically generated together with corresponding inverse
	 * labels for each label. It is worth noting that concatenation
	 * will be generated at double the rate of the relatively more
	 * expensive Kleene and disjunction operations.
	 * @param ruleApplications The number of times the disjunction,
	 *        Kleene, and concatenation steps are allowed to be applied.
	 * @param labels The number of distinct labels to use (upper limit).
	 * @return The randomly generated RPQ.
	 * @throws IllegalArgumentException When the given number of labels is 0 or less.
	 * @see RPQ
	 */
	public static RPQ generatePlainRPQ(int ruleApplications, int labels) throws IllegalArgumentException{
		return generatePlainRPQ(ruleApplications, Util.generateLabels(labels));
	}
	
	/**
	 * Generates a random RPQ created by applying the disjunction,
	 * Kleene and concatenation steps from the RPQ grammar the
	 * given number of times. Labels and inverse labels are drawn
	 * from the given set of labels. It is worth noting that concatenation
	 * will be generated at double the rate of the relatively more
	 * expensive Kleene and disjunction operations.
	 * @param ruleApplications The number of times the disjunction,
	 *        Kleene, and concatenation steps are allowed to be applied.
	 * @param labels The set of labels to draw from.
	 * @return The randomly generated RPQ.
	 * @throws IllegalArgumentException When the list of labels is empty.
	 * @see RPQ
	 */
	public static RPQ generatePlainRPQ(int ruleApplications, List<Predicate> labels) throws IllegalArgumentException{
		if(labels.isEmpty()){
			throw new IllegalArgumentException("List of labels cannot be empty.");
		}
		
		return generatePlainRPQImpl(ruleApplications, labels);
	}
	
	/**
	 * Generates a random RPQ created by applying the disjunction,
	 * Kleene and concatenation steps from the RPQ grammar the
	 * given number of times. Labels and inverse labels are drawn
	 * from the given set of labels. It is worth noting that concatenation
	 * will be generated at double the rate of the relatively more
	 * expensive Kleene and disjunction operations.
	 * @param ruleApplications The number of times the disjunction,
	 *        Kleene, and concatenation steps are allowed to be applied.
	 * @param labels The set of labels to draw from.
	 * @return The randomly generated RPQ.
	 */
	private static RPQ generatePlainRPQImpl(int ruleApplications, List<Predicate> labels){
		if(ruleApplications == 0){
			//edge label
			Predicate label = Util.selectRandom(labels);
			return RPQ.label(Util.getRandom().nextBoolean() ? label : label.getInverse());
		}else{
			if(Util.getRandom().nextBoolean()){
				//concatenation
				int split = Util.uniformRandom(0, ruleApplications - 1);
				return RPQ.concat(generatePlainRPQImpl(split, labels), generatePlainRPQImpl(ruleApplications - split - 1, labels));
			}else{
				if(Util.getRandom().nextBoolean()){
					//disjunction
					int split = Util.uniformRandom(0, ruleApplications - 1);
					return RPQ.disjunct(generatePlainRPQImpl(split, labels), generatePlainRPQImpl(ruleApplications - split - 1, labels));
				}else{
					//transitive closure
					return RPQ.kleene(generatePlainRPQImpl(ruleApplications - 1, labels));
				}
			}
		}
	}
}
