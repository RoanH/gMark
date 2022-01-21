package dev.roanh.gmark.query.shape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.query.Query;
import dev.roanh.gmark.query.Variable;
import dev.roanh.gmark.util.PathSegment;
import dev.roanh.gmark.util.Util;

public class CycleGenerator extends ShapeGenerator{

	public CycleGenerator(Workload workload){
		super(workload);
	}

	@Override
	public Query generateQuery() throws GenerationException{
		int conjunctNum = randomConjunctNumber();
		List<Conjunct> conjuncts = new ArrayList<Conjunct>(conjunctNum);
		
		int n1 = conjunctNum / 2;
		int n2 = conjunctNum - n1;
		
		Selectivity selectivity = randomSelectivity();
		//TODO store selected selectivity info for the query we're working on
		
		List<PathSegment> path = gSel.generateRandomPath(selectivity, n1, workload.getStarProbability());
		List<Variable> variables = createVariables(n1 + n2);
		for(int i = 0; i < n1; i++){
			PathSegment segment = path.get(i);
			Conjunct conj = conjGen.generateConjunct(gSel, segment.getSource(), segment.getTarget());
			conj.setData(variables.get(i), variables.get(i + 1), segment.hasStar());
			conjuncts.add(conj);
		}
		
		List<PathSegment> path2 = gSel.generateRandomPath(selectivity, path.get(0).getSource(), n2, workload.getStarProbability());
		for(int i = 0; i < n2; i++){
			PathSegment segment = path2.get(i);
			Conjunct conj = conjGen.generateConjunct(gSel, segment.getSource(), segment.getTarget());
			conj.setData(variables.get(i == 0 ? 0 : (n1 + i)), variables.get(i == n2 - 1 ? n1 : (n1 + i + 1)), segment.hasStar());
			conjuncts.add(conj);
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
		
		return new Query(conjuncts, variables);
	}
}
