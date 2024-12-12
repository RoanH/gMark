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
package dev.roanh.gmark.cli.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import dev.roanh.gmark.cli.CommandLineClient;
import dev.roanh.gmark.cli.InputException;
import dev.roanh.gmark.data.CardStat;
import dev.roanh.gmark.eval.DatabaseGraph;
import dev.roanh.gmark.eval.PathQuery;
import dev.roanh.gmark.eval.QueryEvaluator;
import dev.roanh.gmark.lang.QueryLanguage;
import dev.roanh.gmark.util.Util;
import dev.roanh.gmark.util.graph.IntGraph;

public class EvaluatorClient extends CommandLineClient{

	public EvaluatorClient(){
		super(
			"evaluate",
			Option.builder("l").longOpt("language").hasArg().argName("query language").desc("The query language for the queries to execute.").build(),
			Option.builder("g").longOpt("graph").hasArg().argName("data").desc("The database graph file.").build(),
			Option.builder("w").longOpt("workload").hasArg().argName("file").desc("The query workload to run, one query per line with format 'source, query, target'.").build(),
			Option.builder("s").longOpt("source").hasArg().argName("source").desc("Optionally the bound source node for the query.").build(),
			Option.builder("q").longOpt("query").hasArg().argName("query").desc("The query to evaluate.").build(),
			Option.builder("t").longOpt("target").hasArg().argName("target").desc("Optionally the bound target node for the query.").build(),
			Option.builder("o").longOpt("output").hasArg().argName("file").desc("The file to write the query output to.").build(),
			Option.builder("f").longOpt("force").desc("Overwrite the output file if present.").build()
		);
		
		//gmark evaluate -l cpq -s 56 -q a -t 5 -g ./graph.edge -o out.txt
		//gmark evaluate -l cpq -w ./q.cpq -g ./graph.edge -o out.txt

	}

	@Override
	protected void handleInput(CommandLine cli) throws InputException{
		QueryLanguage language = QueryLanguage.fromName(cli.getOptionValue('l')).orElse(null);
		if(language == null){
			throw new InputException("No query langauge specified.");
		}
		
		List<PathQuery> queries = readQueries(language, cli);
		executeQueries(readDatabaseGraph(cli), queries, resolveOutputPath(cli));
	}
	
	private List<PathQuery> readQueries(QueryLanguage language, CommandLine cli) throws InputException{
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
				return Util.readWorkload(Paths.get(cli.getOptionValue('w')), language::parse);
			}catch(IOException e){
				e.printStackTrace();
				throw new InputException("Failed to read the provided workload file.");
			}
		}
	}
	
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
	
	private Path resolveOutputPath(CommandLine cli) throws InputException{
		Path output = cli.hasOption('o') ? Paths.get(cli.getOptionValue('o')) : null;
		if(output != null && !cli.hasOption('f') && Files.exists(output)){
			throw new InputException("The given output file already exists and overwriting is not requested.");
		}
		
		return output;
	}
	
	private void executeQueries(DatabaseGraph graph, List<PathQuery> queries, Path outputFile){
		QueryEvaluator evaluator = new QueryEvaluator(graph);
		
		for(PathQuery query : queries){
			System.out.println("Evaluating query: " + query);
			long start = System.nanoTime();
			CardStat result = evaluator.evaluate(query).computeCardinality();
			long end = System.nanoTime();
			System.out.println("Evaluation cardinality statistics: " + result);
			System.out.println("Evaluation time: " + (end - start) + " ns");
			//TODO write result if requested
		}
		
		//TODO done!
	}
}
