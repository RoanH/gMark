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
			Conjunct conj = conjGen.generateConjunct(gSel, segment.getSource(), segment.getTarget());
			conj.setData(variables.get(i), variables.get(i + 1), segment.hasStar());
			conjuncts.add(conj);
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
