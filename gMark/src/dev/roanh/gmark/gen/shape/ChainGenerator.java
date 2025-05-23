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
 * conjuncts arranged in a chain shape. Specifically,
 * the target variable of a conjunct is the same as
 * the source variable of the next conjunct in the chain.
 * @author Roan
 */
public class ChainGenerator extends ShapeGenerator{
	
	/**
	 * Constructs a new generator for chain shaped queries.
	 * @param workload The workload specification.
	 */
	public ChainGenerator(Workload workload){
		super(workload);
	}

	@Override
	public Query generateQuery() throws GenerationException{
		int conjunctNum = randomConjunctNumber();
		List<Conjunct> conjuncts = new ArrayList<Conjunct>(conjunctNum);
		Selectivity selectivity = randomSelectivity();
		
		List<PathSegment> path = gSel.generateRandomPath(selectivity, conjunctNum, workload.getStarProbability());
		
		List<Variable> variables = createVariables(conjunctNum + 1);
		variables.add(new Variable(0));
		for(int i = 0; i < conjunctNum; i++){
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
		
		int arity = randomArity(variables.size());
		if(arity == 1){
			variables = Collections.singletonList(variables.get(0));
		}else if(arity == 2){
			variables = Arrays.asList(variables.get(0), variables.get(variables.size() - 1));
		}else{
			while(variables.size() > arity){
				variables.remove(Util.uniformRandom(0, variables.size() - 1));
			}
		}
		
		return new Query(conjuncts, variables, selectivity, QueryShape.CHAIN);
	}
}
