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
package dev.roanh.gmark.gen.shape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.roanh.gmark.data.PathSegment;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.gen.workload.Workload;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.query.Query;
import dev.roanh.gmark.query.Variable;
import dev.roanh.gmark.type.Selectivity;
import dev.roanh.gmark.util.Util;

/**
 * A query generator that generates queries with the
 * conjuncts arranged in a cycle shape. Specifically,
 * the conjuncts form a chain with the last conjunct
 * target variable being the same as the source variable
 * of the first conjunct.
 * @author Roan
 */
public class CycleGenerator extends ShapeGenerator{

	/**
	 * Constructs a new generator for cycle shaped queries.
	 * @param workload The workload specification.
	 */
	public CycleGenerator(Workload workload){
		super(workload);
	}

	@Override
	public Query generateQuery() throws GenerationException{
		int conjunctNum = randomConjunctNumber();
		List<Conjunct> conjuncts = new ArrayList<Conjunct>(conjunctNum);
		Selectivity selectivity = randomSelectivity();
		
		int n1 = conjunctNum / 2;
		int n2 = conjunctNum - n1;
		
		if(n1 == 0){
			throw new GenerationException("Cycle needs at least 2 conjuncts.");
		}
		
		List<PathSegment> path = gSel.generateRandomPath(selectivity, n1, workload.getStarProbability());
		List<Variable> variables = createVariables(n1 + n2);
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
		
		List<PathSegment> path2 = gSel.generateRandomPath(selectivity, path.get(0).getSource(), n2, workload.getStarProbability());
		for(int i = 0; i < n2; i++){
			PathSegment segment = path2.get(i);
			conjuncts.add(conjGen.generateConjunct(
				gSel,
				segment.getSource(),
				segment.getTarget(),
				variables.get(i == 0 ? 0 : (n1 + i)),
				variables.get(i == n2 - 1 ? n1 : (n1 + i + 1)),
				segment.hasStar()
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
		
		return new Query(conjuncts, variables, selectivity, QueryShape.CYCLE);
	}
}
