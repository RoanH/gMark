/*
 * gMark: A domain- and query language-independent graph instance and query workload generator.
 * Copyright (C) 2021  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/gMark
 *
 * gMark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gMark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.gmark.query;

import java.util.ArrayList;
import java.util.List;

import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.shape.ShapeGenerator;
import dev.roanh.gmark.util.Util;

/**
 * Utility class for generation queries for workloads.
 * @author Roan
 * @see Workload
 */
public final class QueryGenerator{
	/**
	 * Maximum number of times to try to generate a query
	 * before giving up and throwing a generation exception.
	 */
	private static final int ERROR_BOUND = 100;

	/**
	 * Prevent instantiation.
	 */
	private QueryGenerator(){
	}
	
	/**
	 * Generates a single query according to
	 * the given workload specification.
	 * @param workload The workload configuration.
	 * @return The generated query.
	 * @throws GenerationException When generating the query failed.
	 */
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
	
	/**
	 * Generates a complete workload consisting of the number
	 * of queries defined in the given workload configuration
	 * and all conforming to the given workload specification.
	 * @param workload The workload configuration.
	 * @return The generated queries.
	 * @throws GenerationException When generating
	 *         the queries failed.
	 * @see Workload#getSize()
	 */
	public static QuerySet generateQueries(Workload workload) throws GenerationException{
		return generateQueries(workload, workload.getSize(), null);
	}
	
	/**
	 * Generates a complete workload consisting of the number
	 * of queries defined in the given workload configuration
	 * and all conforming to the given workload specification.
	 * The given progress listener is updated with the progress
	 * of the generation task.
	 * @param workload The workload configuration.
	 * @param listener The progress listener to use,
	 *         can be <code>null</code>
	 * @return The generated queries.
	 * @throws GenerationException When generating
	 *         the queries failed.
	 * @see Workload#getSize()
	 */
	public static QuerySet generateQueries(Workload workload, ProgressListener listener) throws GenerationException{
		return generateQueries(workload, workload.getSize(), listener);
	}
	
	/**
	 * Generates a workload of the given number of queries
	 * conforming to the given workload specification. The
	 * given number of queries overrides the workload size
	 * as defined in the workload configuration. The given
	 * progress listener is updated with the progress of
	 * the generation task.
	 * @param workload The workload configuration.
	 * @param n The number of queries to generate.
	 * @param listener The progress listener to use,
	 *         can be <code>null</code>
	 * @return The generated queries.
	 * @throws GenerationException When generating
	 *         the queries failed.
	 */
	public static QuerySet generateQueries(Workload workload, int n, ProgressListener listener) throws GenerationException{
		GenerationException.rethrow(workload::validate);
		
		long start = System.currentTimeMillis();
		List<ShapeGenerator> generators = workload.getShapes().stream().map(s->s.getQueryGenerator(workload)).toList();
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
	
	/**
	 * Interfaces for objects that want to listen
	 * to the progress of a query generation task.
	 * @author Roan
	 */
	public static abstract interface ProgressListener{
		
		/**
		 * Called when a a new query finished generating.
		 * @param done The total number of queries generated so far.
		 * @param total The total number of queries to generate.
		 */
		public abstract void update(int done, int total);
	}
}
