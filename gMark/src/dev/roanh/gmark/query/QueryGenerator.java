package dev.roanh.gmark.query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.shape.ShapeGenerator;
import dev.roanh.gmark.util.Util;

public class QueryGenerator{

	public static Query generateQuery(Workload workload) throws GenerationException{
		GenerationException.rethrow(workload::validate);
		
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
	
	public static List<Query> generateQueries(Workload workload) throws GenerationException{
		return generateQueries(workload, workload.getSize());
	}
	
	public static List<Query> generateQueries(Workload workload, int n) throws GenerationException{
		GenerationException.rethrow(workload::validate);
		
		List<ShapeGenerator> generators = workload.getShapes().stream().map(s->s.getQueryGenerator(workload)).collect(Collectors.toList());
		List<Query> queries = new ArrayList<Query>(n);
		
		while(queries.size() < n){
			try{
				queries.add(Util.selectRandom(generators).generateQuery());
				//System.out.println("done: " + queries.size());
			}catch(Exception e){
				System.err.println("fail: " + e.getMessage());
				//e.printStackTrace();
				//TODO better handling or a bound?
			}
		}
		
		return queries;
	}
}
