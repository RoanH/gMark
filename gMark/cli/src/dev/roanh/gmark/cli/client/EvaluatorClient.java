/*
 * gMark: A domain- and query language-independent query workload generator and query language utility library.
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
package dev.roanh.gmark.cli.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import dev.roanh.gmark.cli.CommandLineClient;
import dev.roanh.gmark.cli.InputException;
import dev.roanh.gmark.data.SourceTargetPair;
import dev.roanh.gmark.eval.DatabaseGraph;
import dev.roanh.gmark.eval.PathQuery;
import dev.roanh.gmark.eval.QueryEvaluator;
import dev.roanh.gmark.eval.ResultGraph;
import dev.roanh.gmark.lang.QueryLanguage;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.Util;
import dev.roanh.gmark.util.graph.generic.IntGraph;

/**
 * Command line client for query evaluation.
 * <p>
 * Example Usage:
 * <ul><li><code>gmark evaluate -l cpq -s 56 -q "a ◦ b" -t 5 -g ./graph.edge -o out.txt</code></li>
 * <li><code>gmark evaluate -l cpq -w ./queries.cpq -g ./graph.edge -o out.txt</code></li></ul>
 * @author Roan
 */
public final class EvaluatorClient extends CommandLineClient{
	/**
	 * Instance of this client.
	 */
	public static final EvaluatorClient INSTANCE = new EvaluatorClient();

	/**
	 * Constructs a new evaluator client.
	 */
	private EvaluatorClient(){
		super(
			"evaluate",
			Option.builder("l").longOpt("language").hasArg().argName("query language").desc("The query language for the queries to execute (cpq or rpq).").build(),
			Option.builder("g").longOpt("graph").hasArg().argName("data").desc("The database graph file.").build(),
			Option.builder("w").longOpt("workload").hasArg().argName("file").desc("The query workload to run, one query per line with format 'source, query, target'.").build(),
			Option.builder("s").longOpt("source").hasArg().argName("source").desc("Optionally the bound source node for the query.").build(),
			Option.builder("q").longOpt("query").hasArg().argName("query").desc("The query to evaluate.").build(),
			Option.builder("t").longOpt("target").hasArg().argName("target").desc("Optionally the bound target node for the query.").build(),
			Option.builder("o").longOpt("output").hasArg().argName("file").desc("The file to write the query output to.").build(),
			Option.builder("f").longOpt("force").desc("Overwrite the output file if present.").build()
		);
	}

	@Override
	protected void handleInput(CommandLine cli) throws InputException{
		QueryLanguage language = QueryLanguage.fromName(cli.getOptionValue('l')).orElse(null);
		if(language == null){
			throw new InputException("No query language specified.");
		}
		
		DatabaseGraph graph = readDatabaseGraph(cli);
		List<PathQuery> queries = readQueries(language, graph, cli);
		executeQueries(graph, queries, resolveOutputPath(cli));
	}
	
	/**
	 * Reads a query workload in the given language based on the given CLI input.
	 * @param language The workload language.
	 * @param graph The graph the workload has to be evaluated on (used for the label set).
	 * @param cli The command line input.
	 * @return The parsed workload queries.
	 * @throws InputException When the provided CLI input contains issues.
	 */
	private List<PathQuery> readQueries(QueryLanguage language, DatabaseGraph graph, CommandLine cli) throws InputException{
		boolean hasSingleQuery = cli.hasOption('s') || cli.hasOption('q') || cli.hasOption('t');
		if(!hasSingleQuery && !cli.hasOption('w')){
			throw new InputException("No query input provided.");
		}else if(hasSingleQuery && cli.hasOption('w')){
			throw new InputException("Cannot provided both an input query and an input query workload.");
		}

		if(hasSingleQuery){
			return List.of(new PathQuery(
				Optional.ofNullable(cli.getOptionValue('s')).map(Integer::parseInt),
				language.parse(cli.getOptionValue('q')),
				Optional.ofNullable(cli.getOptionValue('t')).map(Integer::parseInt)
			));
		}else{
			try{
				System.out.println("Reading query workload...");
				final List<Predicate> labels = graph.getLabels();
				return Util.readWorkload(Paths.get(cli.getOptionValue('w')), q->language.parse(q, labels));
			}catch(IOException e){
				e.printStackTrace();
				throw new InputException("Failed to read the provided workload file.");
			}
		}
	}
	
	/**
	 * Reads a database graph provided on the command line.
	 * @param cli The command line arguments.
	 * @return The read database graph.
	 * @throws InputException When the provided CLI input contains issues.
	 */
	private DatabaseGraph readDatabaseGraph(CommandLine cli) throws InputException{
		if(!cli.hasOption('g')){
			throw new InputException("No database graph provided.");
		}
		
		try{
			System.out.println("Reading database graph...");
			IntGraph data = Util.readGraph(Paths.get(cli.getOptionValue('g')));
			if(data == null){
				throw new InputException("Failed to parse the provided graph file.");
			}
			
			return new DatabaseGraph(data);
		}catch(IOException e){
			e.printStackTrace();
			throw new InputException("Failed to read the provided graph file.");
		}
	}
	
	/**
	 * Attempts to resolve the output path results should be written to.
	 * @param cli The command line input.
	 * @return The resolved results file path or null if none.
	 * @throws InputException When the provided CLI input contains issues.
	 */
	private Path resolveOutputPath(CommandLine cli) throws InputException{
		Path output = cli.hasOption('o') ? Paths.get(cli.getOptionValue('o')) : null;
		if(output != null && !cli.hasOption('f') && Files.exists(output)){
			throw new InputException("The given output file already exists and overwriting is not requested.");
		}
		
		return output;
	}
	
	/**
	 * Executes the given list of queries on the given graph and writes the results to the given file.
	 * @param graph The graph to evaluate the queries on.
	 * @param queries The queries to evaluate.
	 * @param outputFile The file to write the results to (or null to not write any results to disk).
	 */
	private void executeQueries(DatabaseGraph graph, List<PathQuery> queries, Path outputFile){
		if(outputFile == null){
			executeAndWriteQueries(graph, queries, null);
		}else{
			try(PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile))){
				executeAndWriteQueries(graph, queries, writer);
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Executes the given list of queries on the given graph and writes the results to the given file.
	 * @param graph The graph to evaluate the queries on.
	 * @param queries The queries to evaluate.
	 * @param output The writer to write detailed query results to (or null to not write detailed results).
	 */
	private void executeAndWriteQueries(DatabaseGraph graph, List<PathQuery> queries, PrintWriter output){
		QueryEvaluator evaluator = new QueryEvaluator(graph);
		
		for(PathQuery query : queries){
			System.out.println("Evaluating query: " + query);
			long start = System.nanoTime();
			ResultGraph result = evaluator.evaluate(query);
			long end = System.nanoTime();
			System.out.println("Evaluation time: " + TimeUnit.NANOSECONDS.toMillis(end - start) + " ms");
			System.out.println("Result cardinality: " + result.computeCardinality());
			
			if(output != null){
				printQueryResult(query, result, end - start, output);
				output.println();
			}
		}
		
		System.out.println("Finished evaluating all input queries.");
	}
	
	@Override
	public String getHelpFooter(){
		return "note: the evaluator is intended to be used with either a single query to evaluate (-s/-q/-t) or with a complete workload of queries (-w).";
	}
	
	/**
	 * Formats the result of the evaluation of the given query.
	 * @param query The query that was evaluated.
	 * @param result The evaluation result.
	 * @param timeNs The time in nanoseconds it took to execute the query.
	 * @param writer The writer to write to.
	 */
	public static final void printQueryResult(PathQuery query, ResultGraph result, long timeNs, PrintWriter writer){
		writer.println("Evaluated query: " + query);
		writer.println("Evaluation time: " + TimeUnit.NANOSECONDS.toMillis(timeNs) + " ms");
		writer.println("Result cardinality: " + result.computeCardinality());
		writer.println("===== Result Paths =====");
		for(SourceTargetPair pair : result.getSourceTargetPairs()){
			writer.println(pair);
		}
	}
}
