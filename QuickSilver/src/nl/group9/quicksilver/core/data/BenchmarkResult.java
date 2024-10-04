package nl.group9.quicksilver.core.data;

import java.util.concurrent.TimeUnit;

import nl.group9.quicksilver.core.spec.Evaluator;
import nl.group9.quicksilver.core.spec.Graph;

/**
 * Record containing benchmark result times for the database implementation.
 * @author Roan
 * @param loadTime The time required to load and {@link Graph#addEdge(int, int, int) construct} the database graph in nanoseconds.
 * @param prepTime The time required to {@link Evaluator#prepare(nl.group9.quicksilver.core.spec.Graph) prepare} the evaluator in nanoseconds. 
 * @param evalTime The time required to {@link Evaluator#evaluate(PathQuery) evaluate} the entire query workload in nanoseconds. 
 * @see Graph
 * @see Evaluator
 */
public record BenchmarkResult(long loadTime, long prepTime, long evalTime){
	
	/**
	 * Computes a weighted combined score for this benchmark result.
	 * @return The overall score for this benchmark result.
	 */
	public double computeScore(){
		//note that technically memory usage is major factor for database performance
		//if present in this score formula every MB of RAM used would add 20 score points
		return ((loadTime / 1000.0D) + (prepTime / 500.0D) + evalTime) / TimeUnit.MILLISECONDS.toNanos(1L);
	}
	
	@Override
	public String toString(){
		return "BenchmarkResult[load=" + loadTime + "ns, prep=" + prepTime + "ns, eval=" + evalTime + "ns, score=" + computeScore() + "]";
	}
}
