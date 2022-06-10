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
package dev.roanh.gmark.query.shape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.roanh.gmark.core.QueryShape;
import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.query.Query;
import dev.roanh.gmark.query.Variable;
import dev.roanh.gmark.util.PathSegment;
import dev.roanh.gmark.util.Util;

/**
 * A query generator that generates queries with the
 * conjuncts arranged in a star-chain shape. Specifically,
 * there is one long chain of conjuncts and each end point
 * has a number of conjuncts attached to it to form a star.
 * @author Roan
 */
public class StarChainGenerator extends ShapeGenerator{

	/**
	 * Constructs a new generator for star-chain shaped queries.
	 * @param workload The workload specification.
	 */
	public StarChainGenerator(Workload workload){
		super(workload);
	}

	@Override
	public Query generateQuery() throws GenerationException{
		int conjunctNum = randomConjunctNumber();
		List<Conjunct> conjuncts = new ArrayList<Conjunct>(conjunctNum);
		Selectivity selectivity = randomSelectivity();
		
		int n1 = (int)Math.ceil(conjunctNum / 3.0D);
		int n2 = (conjunctNum - n1) / 2;
		int n3 = conjunctNum - n1 - n2;
		
		List<PathSegment> path = gSel.generateRandomPath(selectivity, n1, workload.getStarProbability());
		List<Variable> variables = createVariables(n1 + n2 + n3 + 1);
		for(int i = 0; i < n1; i++){
			PathSegment segment = path.get(i);
			conjuncts.add(conjGen.generateConjunct(
				gSel,
				segment.getSource(),
				segment.getTarget(),
				variables.get(i),
				variables.get(i + 1),
				segment.hasStar()
			));
		}
		
		for(int i = 0; i < n2; i++){
			PathSegment path2 = gSel.generateRandomPath(selectivity, path.get(0).getSource(), 1, workload.getStarProbability()).get(0);
			conjuncts.add(conjGen.generateConjunct(
				gSel,
				path2.getSource(),
				path2.getTarget(),
				variables.get(0),
				variables.get(n1 + i + 1),
				path2.hasStar()
			));
		}
		
		for(int i = 0; i < n3; i++){
			PathSegment path2 = gSel.generateRandomPath(selectivity, path.get(path.size() - 1).getTarget(), 1, workload.getStarProbability()).get(0);
			conjuncts.add(conjGen.generateConjunct(
				gSel,
				path2.getSource(),
				path2.getTarget(),
				variables.get(n1),
				variables.get(n1 + n2 + i + 1),
				path2.hasStar()
			));
		}
		
		int arity = randomArity(variables.size());
		if(arity == 1){
			variables = Collections.singletonList(variables.get(0));
		}else if(arity == 2){
			variables = Arrays.asList(variables.get(0), variables.get(n1));
		}else{
			while(variables.size() > arity){
				variables.remove(Util.uniformRandom(0, variables.size() - 1));
			}
		}
		
		return new Query(conjuncts, variables, selectivity, QueryShape.STARCHAIN);
	}
}
