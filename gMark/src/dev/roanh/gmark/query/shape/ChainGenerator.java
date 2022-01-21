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
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.Util;

public class ChainGenerator extends ShapeGenerator{
	
	public ChainGenerator(Workload workload){
		super(workload);
	}

	@Override
	public Query generateQuery() throws GenerationException{
		int conjunctNum = Util.uniformRandom(workload.getMinConjuncts(), workload.getMaxConjuncts());
		List<Conjunct> conjuncts = new ArrayList<Conjunct>(conjunctNum);
		
		SelectivityGraph g = new SelectivityGraph(workload.getGraphSchema(), workload.getMaxSelectivityGraphLength());
		Selectivity selectivity = Util.selectRandom(workload.getSelectivities());
		//TODO store selected selectivity info for the query we're working on
		
		List<PathSegment> path = g.generateRandomPath(selectivity, conjunctNum, workload.getStarProbability());
		
		List<Variable> variables = new ArrayList<Variable>(conjunctNum + 1);
		variables.add(new Variable(0));
		for(int i = 0; i < conjunctNum; i++){
			PathSegment segment = path.get(i);
			Conjunct conj = conjGen.generateConjunct(g, segment.getSource(), segment.getTarget());
			
			Variable var = new Variable(i + 1);
			conj.setData(variables.get(variables.size() - 1), var, segment.hasStar());
			variables.add(var);
			
			conjuncts.add(conj);
		}
		
		int arity = Math.min(Util.uniformRandom(workload.getMinArity(), workload.getMaxArity()), variables.size());//ensure arity <= |variables|
		if(arity == 1){
			variables = Collections.singletonList(variables.get(0));
		}else if(arity == 2){
			variables = Arrays.asList(variables.get(0), variables.get(variables.size() - 1));
		}else{
			while(variables.size() > arity){
				variables.remove(Util.uniformRandom(0, variables.size() - 1));
			}
		}
		
		return new Query(conjuncts, variables);
	}
}
