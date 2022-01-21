package dev.roanh.gmark.query;

import java.util.ArrayList;
import java.util.List;

import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.shape.ShapeGenerator;
import dev.roanh.gmark.util.Util;

public class QueryGenerator{

	public static Query generateQuery(Workload workload){
		ShapeGenerator gen = Util.selectRandom(workload.getShapes()).getQueryGenerator(workload);
		while(true){
			try{
				return gen.generateQuery();
			}catch(GenerationException e){
				e.printStackTrace();
				//TODO better handling or a bound?
			}
		}
	}
	
	public static List<Query> generateQueries(Workload workload){
		ShapeGenerator gen = Util.selectRandom(workload.getShapes()).getQueryGenerator(workload);
		List<Query> queries = new ArrayList<Query>(workload.getSize());
		
		while(queries.size() < workload.getSize()){
			try{
				queries.add(gen.generateQuery());
			}catch(GenerationException e){
				e.printStackTrace();
				//TODO better handling or a bound?
			}
		}
		
		return queries;
	}
}
