//package nl.group9.quicksilver.core;
//
//import static dev.roanh.gmark.lang.QueryLanguageSyntax.CHAR_DISJUNCTION;
//import static dev.roanh.gmark.lang.QueryLanguageSyntax.CHAR_INTERSECTION;
//import static dev.roanh.gmark.lang.QueryLanguageSyntax.CHAR_INVERSE;
//import static dev.roanh.gmark.lang.QueryLanguageSyntax.CHAR_JOIN;
//import static dev.roanh.gmark.lang.QueryLanguageSyntax.CHAR_KLEENE;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//import dev.roanh.gmark.core.graph.Predicate;
//import dev.roanh.gmark.lang.QueryLanguageSyntax;
//import dev.roanh.gmark.lang.cpq.ParserCPQ;
//import dev.roanh.gmark.lang.rpq.ParserRPQ;
//import dev.roanh.gmark.util.Util;
//
//import nl.group9.quicksilver.core.data.CardStat;
//import nl.group9.quicksilver.core.data.PathQuery;
//import nl.group9.quicksilver.core.spec.DatabaseGraph;
//import nl.group9.quicksilver.core.spec.Evaluator;
//import nl.group9.quicksilver.core.spec.EvaluatorProvider;
//
///**
// * Simple benchmark implementation, note that no warm-up runs are performed.
// * @author Roan
// */
//public final class Benchmark{
//
//	/**
//	 * Prevent instantiation.
//	 */
//	private Benchmark(){
//	}
//	
//	/**
//	 * Runs the given workload on the given graph with the given evaluator and returns some runtime statistics.
//	 * @param <G> The database graph data type.
//	 * @param provider The provider to use to construct and evaluator to evaluate the queries with.
//	 * @param graphFile The path to the file containing the database graph.
//	 * @param workloadFile The path to the file containing the query workload with queries to evaluate, note
//	 *        that the workload query language is derived from the filename.
//	 * @return The result times of the workload evaluation.
//	 * @throws IOException When an IOException occurs.
//	 * @see DatabaseGraph
//	 */
//	public static <G extends DatabaseGraph> BenchmarkResult runEvaluatorBenchmark(EvaluatorProvider<G, ?> provider, Path graphFile, Path workloadFile) throws IOException{
//		System.out.println("[LOAD] Reading graph...");
//		long loadStart = System.nanoTime();
//		G graph = GraphUtil.readGraph(provider, graphFile);
//		long loadEnd = System.nanoTime();
//		System.out.println("[LOAD] Graph read in " + (loadEnd - loadStart) + " ns");
//		
//		System.out.println("[PREP] Preparing evaluator...");
//		long prepStart = System.nanoTime();
//		Evaluator<G, ?> evaluator = provider.createEvaluator();
//		evaluator.prepare(graph);
//		long prepEnd = System.nanoTime();
//		System.out.println("[PREP] Evaluator prepared in " + (prepEnd - prepStart) + " ns");
//		
//		System.out.println("[EVAL] Executing the query workload...");
//		String filename = workloadFile.getFileName().toString();
//		String type = filename.substring(0, filename.lastIndexOf('.'));
//		List<PathQuery> queries = switch(type){
//		case "cpq": 
//			yield readWorkload(workloadFile, q->ParserCPQ.parse(q, createPredicteMap(graph), CHAR_JOIN, CHAR_INTERSECTION, CHAR_INVERSE));
//		case "rpq": 
//			yield readWorkload(workloadFile, q->ParserRPQ.parse(q, createPredicteMap(graph), CHAR_JOIN, CHAR_DISJUNCTION, CHAR_KLEENE, CHAR_INVERSE));
//		default:
//			throw new IllegalArgumentException("Unknown query language: " + type);
//		};
//		
//		long evalTime = 0;
//		for(PathQuery query : queries){
//			System.out.println("[EVAL] Evaluating query: " + query);
//			long start = System.nanoTime();
//			CardStat result = evaluator.evaluate(query).computeCardinality();
//			long end = System.nanoTime();
//			System.out.println("[EVAL] Evaluation result: " + result);
//			System.out.println("[EVAL] Evaluation time: " + (end - start) + " ns");
//			evalTime += end - start;
//		}
//		
//		System.out.println("[EVAL] Total evaluation time: " + evalTime + " ns");
//		return new BenchmarkResult(
//			loadEnd - loadStart,
//			prepEnd - prepStart,
//			evalTime
//		);  
//	}
//	
//	/**
//	 * Constructs a map with predicates for the given graph.
//	 * @param <G> The database graph type.
//	 * @param graph The graph to create a predicate set for.
//	 * @return The constructed predicate set mapping from alias to predicate.
//	 */
//	private static final <G extends DatabaseGraph> Map<String, Predicate> createPredicteMap(G graph){
//		return Util.generateLabels(graph.getLabelCount()).stream().collect(Collectors.toMap(Predicate::getAlias, Function.identity()));
//	}
//	
//	/**
//	 * Reads a query workload from the given file. The file is assumed to be encoded
//	 * using UTF-8 with at most one query per line in the following format:
//	 * <pre>{@code <source>, <query>, <target>}</pre>
//	 * Lines that do not contain a valid query definition are ignored. The source and
//	 * target vertex are expected to be given as integers, however, if unbound then
//	 * <code>*</code> has to be specified.
//	 * @param file The file to read from.
//	 * @param parser The parser to use to parse the query definition into a query instance.
//	 * @return A list with the parsed queries.
//	 * @throws IOException When an IOException occurs.
//	 */
//	private static final List<PathQuery> readWorkload(Path file, Function<String, QueryLanguageSyntax> parser) throws IOException{
//		return readWorkload(Files.newInputStream(file), parser);
//	}
//	
//	/**
//	 * Reads a query workload from the given input stream. The input stream is assumed
//	 * to represent a text file encoded using UTF-8 with at most one query per line in
//	 * the following format:
//	 * <pre>{@code <source>, <query>, <target>}</pre>
//	 * Lines that do not contain a valid query definition are ignored. The source and
//	 * target vertex are expected to be given as integers, however, if unbound then
//	 * <code>*</code> has to be specified.
//	 * @param in The input stream to read from.
//	 * @param parser The parser to use to parse the query definition into a query instance.
//	 * @return A list with the parsed queries.
//	 * @throws IOException When an IOException occurs.
//	 */
//	private static final List<PathQuery> readWorkload(InputStream in, Function<String, QueryLanguageSyntax> parser) throws IOException{
//		try(BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))){
//			List<PathQuery> queries = new ArrayList<PathQuery>();
//			
//			String line;
//			while((line = reader.readLine()) != null){
//				String[] args = line.split(",");
//				if(args.length != 3 || line.isBlank() || line.startsWith("#")){
//					continue;
//				}
//				
//				String src = args[0].trim();
//				String trg = args[2].trim();
//				
//				queries.add(new PathQuery(
//					src.equals("*") ? Optional.empty() : Optional.of(Integer.parseInt(src)),
//					parser.apply(args[1].trim()),
//					trg.equals("*") ? Optional.empty() : Optional.of(Integer.parseInt(trg))
//				));
//			}
//			
//			return queries;
//		}
//	}
//}