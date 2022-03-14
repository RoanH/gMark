package dev.roanh.gmark.query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.shape.ShapeGenerator;
import dev.roanh.gmark.util.Util;

public class QueryGenerator{
	private static final int ERROR_BOUND = 100;

	public static Query generateQuery(Workload workload) throws GenerationException{
		GenerationException.rethrow(workload::validate);
		
		ShapeGenerator gen = Util.selectRandom(workload.getShapes()).getQueryGenerator(workload);
		
		int fails = 0;
		while(fails < ERROR_BOUND){
			try{
				return gen.generateQuery();
			}catch(GenerationException e){
				fails++;
			}
		}
		
		throw new GenerationException("Failed to generate a query due to too many failed attempts.");
	}
	
	public static QuerySet generateQueries(Workload workload) throws GenerationException{
		return generateQueries(workload, workload.getSize(), null);
	}
	
	public static QuerySet generateQueries(Workload workload, ProgressListener listener) throws GenerationException{
		return generateQueries(workload, workload.getSize(), listener);
	}
	
	public static QuerySet generateQueries(Workload workload, int n, ProgressListener listener) throws GenerationException{
		GenerationException.rethrow(workload::validate);
		
		long start = System.currentTimeMillis();
		List<ShapeGenerator> generators = workload.getShapes().stream().map(s->s.getQueryGenerator(workload)).collect(Collectors.toList());
		List<Query> queries = new ArrayList<Query>(n);
		
		int fails = 0;
		while(queries.size() < n && fails < ERROR_BOUND){
			try{
				queries.add(Util.selectRandom(generators).generateQuery());
			}catch(Exception e){
				fails++;
				continue;
			}
			
			fails = 0;
			if(listener != null){
				listener.update(queries.size(), n);
			}
		}
		
		if(fails != 0){
			throw new GenerationException("Failed to generate a query due to too many failed attempts.");
		}else{
			return new QuerySet(queries, System.currentTimeMillis() - start);
		}
	}
	
	public static abstract interface ProgressListener{
		
		public abstract void update(int done, int total);
	}
}
