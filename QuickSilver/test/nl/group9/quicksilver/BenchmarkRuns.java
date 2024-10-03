package nl.group9.quicksilver;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;

import dev.roanh.gmark.lang.QueryLanguage;

import nl.group9.quicksilver.core.Benchmark;
import nl.group9.quicksilver.core.data.BenchmarkResult;
import nl.group9.quicksilver.impl.Provider;

/**
 * Just a more convenient place to run datasets.
 * @author Roan
 */
public class BenchmarkRuns{
	/**
	 * The evaluator/graph configuration to use.
	 */
	private static final Provider config = new Provider();

	/**
	 * Test main for benchmarks.
	 * @param args No valid CLI arguments.
	 * @throws IOException When an IOException occurs.
	 */
	public static void main(String[] args) throws IOException{
		run("syn/mini", QueryLanguage.CPQ, config);
	}
	
	/**
	 * Runs the benchmark on the given dataset with the given workload
	 * using the evaluator given by the provider.
	 * @param dataset The dataset to use for the benchmark. This is the
	 *        path relative to the workload directly.
	 * @param workload The workload type to run on the database graph.
	 * @param provider The provider for the concrete evaluator and graph
	 *        implementation to use.
	 * @return The result of the benchmark.
	 * @throws IOException When an IOException occurs.
	 * @see Benchmark
	 */
	private static BenchmarkResult run(String dataset, QueryLanguage workload, Provider provider) throws IOException{
		System.out.println("Running dataset: " + dataset + " with workload type " + workload);
		return Benchmark.runEvaluatorBenchmark(
			new Provider(), 
			Paths.get("workload", dataset, "graph.edge"),
			Paths.get("workload", dataset, workload.name().toLowerCase(Locale.ROOT) + ".query")
		);
	}
}
