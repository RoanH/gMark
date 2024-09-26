package nl.group9.quicksilver.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.rpq.RPQ;

import nl.group9.quicksilver.core.data.BenchmarkResult;
import nl.group9.quicksilver.core.data.CardStat;
import nl.group9.quicksilver.core.data.PathQuery;
import nl.group9.quicksilver.core.spec.Evaluator;
import nl.group9.quicksilver.core.spec.EvaluatorProvider;
import nl.group9.quicksilver.core.spec.Graph;

public class Benchmark{

	public static <G extends Graph, R extends Graph> BenchmarkResult runEvaluatorBenchmark(EvaluatorProvider<G, R> provider, Path graphFile, Path workloadFile) throws IOException{
		System.out.println("[LOAD] Reading graph...");
		long loadStart = System.nanoTime();
		G graph = readGraph(provider, graphFile);
		long loadEnd = System.nanoTime();
		System.out.println("[LOAD] Graph read in " + (loadEnd - loadStart) + " ns");
		
		System.out.println("[PREP] Preparing evaluator...");
		long prepStart = System.nanoTime();
		Evaluator<G, R> evaluator = provider.createEvaluator();
		evaluator.prepare(graph);
		long prepEnd = System.nanoTime();
		System.out.println("[PREP] Evaluator prepared in " + (prepEnd - prepStart) + " ns");
		
		System.out.println("[EVAL] Executing the query workload...");
		String filename = workloadFile.getFileName().toString();
		String type = filename.substring(0, filename.lastIndexOf('.'));
		List<PathQuery> queries = switch(type){
		case "cpq": 
			yield readWorkload(workloadFile, CPQ::parse);
		case "rpq": 
			yield readWorkload(workloadFile, RPQ::parse);
		default:
			throw new IllegalArgumentException("Unknown query language: " + type);
		};
		
		long evalTime = 0;
		for(PathQuery query : queries){
			System.out.println("[EVAL] Evaluating query: " + query);
			long start = System.nanoTime();
			CardStat result = evaluator.computeCardinality(evaluator.evaluate(query));
			long end = System.nanoTime();
			System.out.println("[EVAL] Evaluation result: " + result);
			System.out.println("[EVAL] Evaluation time: " + (end - start) + " ns");
			evalTime += end - start;
		}
		
		return new BenchmarkResult(
			loadEnd - loadStart,
			prepEnd - prepStart,
			evalTime
		);  
	}
	
	private static final List<PathQuery> readWorkload(Path file, Function<String, QueryLanguageSyntax> parser) throws IOException{
		return readWorkload(Files.newInputStream(file), parser);
	}
	
	private static final List<PathQuery> readWorkload(InputStream in, Function<String, QueryLanguageSyntax> parser) throws IOException{
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))){
			List<PathQuery> queries = new ArrayList<PathQuery>();
			
			String line;
			while((line = reader.readLine()) != null){
				String[] args = line.split(",");
				if(args.length != 3 || line.isBlank() || line.startsWith("#")){
					continue;
				}
				
				String src = args[0].trim();
				String trg = args[2].trim();
				
				queries.add(new PathQuery(
					src.equals("*") ? Optional.empty() : Optional.of(Integer.parseInt(src)),
					parser.apply(args[1].trim()),
					trg.equals("*") ? Optional.empty() : Optional.of(Integer.parseInt(trg))
				));
			}
			
			return queries;
		}
	}
	
	private static final <G extends Graph> G readGraph(EvaluatorProvider<G, ?> evaluator, Path file) throws IOException{
		return readGraph(evaluator, Files.newInputStream(file));
	}

	//n tripples ish
	private static final <G extends Graph> G readGraph(EvaluatorProvider<G, ?> provider, InputStream in) throws IOException{
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))){
			String header = reader.readLine();
			if(header == null){
				return null;
			}
			
			String[] metadata = header.split(" ");
			int vertices = Integer.parseInt(metadata[0]);
			int edges = Integer.parseInt(metadata[1]);
			int labels = Integer.parseInt(metadata[2]);
			G graph = provider.createGraph(vertices, edges, labels);
			
			String line;
			while((line = reader.readLine()) != null){
				String[] edge = line.split(" ");
				if(edge.length == 0){
					break;
				}

				int src = Integer.parseInt(edge[0]);
				int trg = Integer.parseInt(edge[1]);
				int lab = Integer.parseInt(edge[2]);

				graph.addEdge(src, trg, lab);
			}
			
			return graph;
		}
	}
}
