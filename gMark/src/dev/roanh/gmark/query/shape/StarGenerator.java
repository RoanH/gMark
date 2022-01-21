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
import dev.roanh.gmark.util.SelectivityType;
import dev.roanh.gmark.util.Util;

public class StarGenerator extends ShapeGenerator{

	public StarGenerator(Workload workload){
		super(workload);
	}

	@Override
	public Query generateQuery() throws GenerationException{
		int conjunctNum = Util.uniformRandom(workload.getMinConjuncts(), workload.getMaxConjuncts());
		List<Conjunct> conjuncts = new ArrayList<Conjunct>(conjunctNum);
		List<Variable> variables = new ArrayList<Variable>(conjunctNum + 1);
		
		SelectivityGraph g = new SelectivityGraph(workload.getGraphSchema(), workload.getMaxSelectivityGraphLength());
		Selectivity selectivity = Util.selectRandom(workload.getSelectivities());
		
		PathSegment path = g.generateRandomPath(selectivity, 1, workload.getStarProbability()).get(0);
	
		SelectivityType sourceType = path.getSource();
		Conjunct first = conjGen.generateConjunct(g, sourceType, path.getTarget());
		variables.add(new Variable(0));
		variables.add(new Variable(1));
		first.setData(variables.get(0), variables.get(1), path.hasStar());
		conjuncts.add(first);
		
		for(int i = 1; i < conjunctNum; i++){
			path = g.generateRandomPath(selectivity, 1, workload.getStarProbability()).get(0);
			Conjunct conj = conjGen.generateConjunct(g, sourceType, path.getTarget());
			Variable var = new Variable(i + 1);
			variables.add(var);
			conj.setData(variables.get(0), var, path.hasStar());
			
			conjuncts.add(conj);
		}
		
		int arity = Math.min(Util.uniformRandom(workload.getMinArity(), workload.getMaxArity()), variables.size());//ensure arity <= |variables|
		if(arity == 1){
			variables = Collections.singletonList(variables.get(0));
		}else if(arity == 2){
			variables = Arrays.asList(variables.get(0), variables.get(1));
		}else{
			while(variables.size() > arity){
				variables.remove(Util.uniformRandom(0, variables.size() - 1));
			}
		}
		
		return new Query(conjuncts, variables);
	}
}
