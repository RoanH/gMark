package dev.roanh.gmark.query.shape;

import dev.roanh.gmark.core.ConjunctGenerator;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Query;

public abstract class ShapeGenerator{
	protected final ConjunctGenerator conjGen;
	protected final Workload workload;
	
	protected ShapeGenerator(Workload workload){
		this.workload = workload;
		conjGen = workload.getConjunctGenerator();
	}
	
	public abstract Query generateQuery() throws GenerationException;
}
