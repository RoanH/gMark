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
import dev.roanh.gmark.util.SelectivityType;
import dev.roanh.gmark.util.Util;

/**
 * A query generator that generates queries with the
 * conjuncts arranged in a star shape. Specifically,
 * all the conjuncts share the same source variable.
 * @author Roan
 */
public class StarGenerator extends ShapeGenerator{

	/**
	 * Constructs a new generator for star shaped queries.
	 * @param workload The workload specification.
	 */
	public StarGenerator(Workload workload){
		super(workload);
	}

	@Override
	public Query generateQuery() throws GenerationException{
		int conjunctNum = randomConjunctNumber();
		List<Conjunct> conjuncts = new ArrayList<Conjunct>(conjunctNum);
		List<Variable> variables = createVariables(conjunctNum + 1);
		Selectivity selectivity = randomSelectivity();
		
		PathSegment path = gSel.generateRandomPath(selectivity, 1, workload.getStarProbability()).get(0);
		
		SelectivityType sourceType = path.getSource();
		conjuncts.add(conjGen.generateConjunct(gSel, sourceType, path.getTarget(), variables.get(0), variables.get(1), path.hasStar()));
		
		for(int i = 1; i < conjunctNum; i++){
			path = gSel.generateRandomPath(selectivity, 1, workload.getStarProbability()).get(0);
			conjuncts.add(conjGen.generateConjunct(
				gSel,
				sourceType,
				path.getTarget(),
				variables.get(0),
				variables.get(i + 1),
				path.hasStar()
			));
		}
		
		int arity = randomArity(variables.size());
		if(arity == 1){
			variables = Collections.singletonList(variables.get(0));
		}else if(arity == 2){
			variables = Arrays.asList(variables.get(0), variables.get(1));
		}else{
			while(variables.size() > arity){
				variables.remove(Util.uniformRandom(0, variables.size() - 1));
			}
		}
		
		return new Query(conjuncts, variables, selectivity, QueryShape.STAR);
	}
}
