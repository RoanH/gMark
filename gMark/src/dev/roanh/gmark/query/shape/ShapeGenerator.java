package dev.roanh.gmark.query.shape;

import java.util.ArrayList;
import java.util.List;

import dev.roanh.gmark.core.ConjunctGenerator;
import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Query;
import dev.roanh.gmark.query.Variable;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.Util;

public abstract class ShapeGenerator{
	protected final ConjunctGenerator conjGen;
	protected final Workload workload;
	protected final SelectivityGraph gSel;
	
	protected ShapeGenerator(Workload workload){
		this.workload = workload;
		conjGen = workload.getConjunctGenerator();
		gSel = new SelectivityGraph(workload.getGraphSchema(), workload.getMaxSelectivityGraphLength());
	}
	
	public abstract Query generateQuery() throws GenerationException;
	
	protected int randomConjunctNumber(){
		return Util.uniformRandom(workload.getMinConjuncts(), workload.getMaxConjuncts());
	}
	
	protected Selectivity randomSelectivity(){
		return Util.selectRandom(workload.getSelectivities());
	}
	
	protected List<Variable> createVariables(int n){
		List<Variable> variables = new ArrayList<Variable>(n);
		for(int i = 0; i < n; i++){
			variables.add(new Variable(i));
		}
		return variables;
	}
	
	//ensure arity <= |variables|
	protected int randomArity(int maxVariables){
		return Util.uniformRandom(workload.getMinArity(), Math.max(workload.getMaxArity(), maxVariables));
	}
}
