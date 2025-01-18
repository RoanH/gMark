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
package dev.roanh.gmark.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.eval.PathQuery;
import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.util.SimpleGraph.SimpleEdge;
import dev.roanh.gmark.util.SimpleGraph.SimpleVertex;
import dev.roanh.gmark.util.UniqueGraph.GraphEdge;
import dev.roanh.gmark.util.UniqueGraph.GraphNode;
import dev.roanh.gmark.util.graph.IntGraph;

/**
 * Class providing various small utilities as well
 * as thread bound random operations.
 * @author Roan
 */
public final class Util{
	/**
	 * Random instances for each thread.
	 */
	private static final ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);

	/**
	 * Prevent instantiation.
	 */
	private Util(){
	}
	
	/**
	 * Gets a random instance bound to the current thread.
	 * @return A thread local random instance.
	 */
	public static Random getRandom(){
		return random.get();
	}
	
	/**
	 * Sets the seed of the random instance for the current thread.
	 * @param seed The new seed.
	 * @see #getRandom()
	 */
	public static void setRandomSeed(long seed){
		random.get().setSeed(seed);
	}
	
	/**
	 * Selects an element at random from the given list.
	 * @param <T> The element data type.
	 * @param data The list to pick an element from.
	 * @return The selected element or <code>null</code>
	 *         when the provided list was empty.
	 * @see #selectRandom(Collection)
	 */
	public static <T> T selectRandom(List<T> data){
		return data.isEmpty() ? null : data.get(getRandom().nextInt(data.size()));
	}

	/**
	 * Randomly selects an element from the given collection.
	 * @param <T> The element data type.
	 * @param data The collection to pick an element from.
	 * @return The selected element or <code>null</code>
	 *         when the provided collection was empty.
	 * @see #selectRandom(List)
	 */
	public static <T> T selectRandom(Collection<T> data){
		if(!data.isEmpty()){
			int idx = getRandom().nextInt(data.size());
			for(T item : data){
				if(idx-- == 0){
					return item;
				}
			}
		}
		return null;
	}
	
	/**
	 * Generates a random integer between the given
	 * minimum and maximum value (both inclusive).
	 * @param min The minimum value.
	 * @param max The maximum value.
	 * @return The randomly generated value.
	 */
	public static int uniformRandom(int min, int max){
		return min + getRandom().nextInt(max - min + 1);
	}
	
	/**
	 * Returns a {@link Supplier} that constructs a new enum map
	 * from the {@link SelectivityClass} enum to the given data type.
	 * @param <T> The data type to map to.
	 * @return A supplier that returns a map that maps from
	 *         selectivity classes to the given data type.
	 * @see Supplier
	 * @see SelectivityClass
	 */
	public static <T> Supplier<Map<SelectivityClass, T>> selectivityMapSupplier(){
		return ()->new EnumMap<SelectivityClass, T>(SelectivityClass.class);
	}
	
	/**
	 * Applies the given function to the given data, unless the
	 * data is <code>null</code> then <code>null</code> is returned
	 * instead.
	 * @param <T> The input data type.
	 * @param <R> The output data type.
	 * @param data The data to give to the function.
	 * @param function The function to run on the data.
	 * @return The result of applying the given function to the given
	 *         data or <code>null</code> when the given data is <code>null</code>.
	 */
	public static <T, R> R applyOrNull(T data, Function<T, R> function){
		return data == null ? null : function.apply(data);
	}
	
	/**
	 * Checks if the given folder is empty or not.
	 * @param folder The folder to check.
	 * @return True if the given folder does not
	 *         contain any files or folders.
	 * @throws IOException When an IOException occurs.
	 */
	public static boolean isEmpty(Path folder) throws IOException{
		try(Stream<Path> paths = Files.walk(folder)){
			return paths.noneMatch(path->!path.equals(folder));
		}
	}
	
	/**
	 * Takes a file containing a graph in RDF triple
	 * format and outputs a SQL INSERT statement to
	 * populate a database with the graph.
	 * @param triples The input file with the RDF triple graph.
	 * @param sql The file to write the generated SQL statement to.
	 * @param overwrite True if the given output file should be
	 *        overwritten if it already exists.
	 * @throws IOException When an IOException occurs.
	 * @throws FileNotFoundException When the given graph file was not found.
	 * @throws FileAlreadyExistsException When the given output file already exists
	 *         and overwriting is not requested.
	 */
	public static void triplesToSQL(Path triples, Path sql, boolean overwrite) throws IOException, FileNotFoundException, FileAlreadyExistsException{
		if(Files.exists(sql) && !overwrite){
			throw new FileAlreadyExistsException(sql.toString());
		}else if(Files.notExists(triples)){
			throw new FileNotFoundException(triples.toString());
		}
		
		try(PrintWriter out = new PrintWriter(Files.newBufferedWriter(sql, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))){
			out.print("INSERT INTO edge VALUES ");
			boolean first = true;
			for(String line : Files.readAllLines(triples)){
				if(!first){
					out.print(", ");
				}else{
					first = false;
				}
				String[] args = line.split(" ");
				out.print("(" + args[0] + ", " + args[1] + ", " + args[2] + ")");
			}
			out.println(";");
		}catch(Exception e){
			throw new IOException(e);
		}
	}
	
	/**
	 * Converts the given edge labelled input graph to a graph where all edge labels
	 * have been turned into labelled vertices. Essentially, for each edge <code>
	 * (a) --b--&gt; (c)</code> the edge will be turned into two edges with a new node
	 * with the former edge label in the middle, giving <code> (a) --&gt; (b) --&gt; (c)
	 * </code>. Thus this transform doubles the number of edges in the graph and adds as
	 * many new nodes as there used to be edges in the old graph. The returned graph has
	 * {@link Object} as the vertex data type. There are two options for the actual
	 * class of these vertex data objects. Either they are a vertex data object from the
	 * old graph and thus of generic type V. Or they are a {@link DataProxy} instance
	 * wrapping an old edge label of generic type E.
	 * @param <V> The vertex data type.
	 * @param <E> The edge label data type.
	 * @param in The input graph to transform.
	 * @return The transformed graph without edge labels.
	 * @see <a href="https://cpqkeys.roanh.dev/notes/to_unlabelled">Notes on transforming edge labelled graphs to graphs without edge labels</a>
	 */
	public static <V, E> UniqueGraph<Object, Void> edgeLabelsToNodes(UniqueGraph<V, E> in){
		UniqueGraph<Object, Void> out = new UniqueGraph<Object, Void>();
		
		in.getNodes().forEach(node->out.addUniqueNode(node.getData()));
		for(GraphEdge<V, E> edge : in.getEdges()){
			GraphNode<Object, Void> mid = out.addUniqueNode(new DataProxy<E>(edge.getData()));
			mid.addUniqueEdgeFrom(edge.getSource());
			mid.addUniqueEdgeTo(edge.getTarget());
		}
		
		return out;
	}
	
	/**
	 * Generates a list of the given size <code>n</code> with
	 * sequentially numbered predicate objects. This means
	 * each predicate will have a textual name that matches
	 * their numerical ID.
	 * @param n The number of labels to generate.
	 * @return The generated set of labels.
	 */
	public static List<Predicate> generateLabels(int n){
		List<Predicate> labels = new ArrayList<Predicate>(n);
		for(int i = 0; i < n; i++){
			labels.add(new Predicate(i, String.valueOf(i)));
		}
		return labels;
	}
	
	/**
	 * Runs the give consumer on the given data only if the
	 * given data is not equal to <code>null</code>.
	 * @param <T> The data type.
	 * @param data The data to run the consumer on.
	 * @param fun The consumer to pass the given data to.
	 */
	public static <T> void runIfNotNull(T data, Consumer<T> fun){
		if(data != null){
			fun.accept(data);
		}
	}
	
	/**
	 * Creates a new array list populated with the given items.
	 * @param <T> The type of the data.
	 * @param items The items to put in the list.
	 * @return The newly created and populated array list.
	 * @see ArrayList
	 */
	@SafeVarargs
	public static <T> ArrayList<T> asArrayList(T... items){
		ArrayList<T> data = new ArrayList<T>();
		for(T item : items){
			data.add(item);
		}
		return data;
	}
	
	/**
	 * Computes the tree decomposition of the given input graph
	 * assuming that the input graph has the following properties:
	 * <ol>
	 * <li>The input graph has a tree width of at most 2.</li>
	 * <li>The input graph is connected.</li>
	 * <li>The input graph contains no self loops.</li>
	 * <li>The input graph contains no parallel edges.</li>
	 * </ol>
	 * @param <T> The data type of the graph.
	 * @param graph The graph to compute the tree decomposition of. This
	 *        graph instance will be modified so a copy should be passed
	 *        if this is a problem.
	 * @return The computed tree decomposition, not necessarily a nice tree decomposition.
	 * @throws IllegalArgumentException When the input graph is not connected or has a
	 *         treewidth that is larger than 2.
	 */
	public static <T extends IDable> Tree<List<T>> computeTreeDecompositionWidth2(SimpleGraph<T, List<Tree<List<T>>>> graph) throws IllegalArgumentException{
		Deque<SimpleVertex<T, List<Tree<List<T>>>>> deg2 = new ArrayDeque<SimpleVertex<T, List<Tree<List<T>>>>>();
		
		//collect all vertices of degree at most k
		for(SimpleVertex<T, List<Tree<List<T>>>> vertex : graph.getVertices()){
			if(vertex.getDegree() <= 2){
				deg2.add(vertex);
			}
		}
		
		//contract nodes of degree at most 2
		while(graph.getVertexCount() > 3){
			if(deg2.isEmpty()){
				throw new IllegalArgumentException("Treewidth of the input graph is more than 2.");
			}
			
			SimpleVertex<T, List<Tree<List<T>>>> v = deg2.pop();
			if(v.getDegree() == 1){
				//move degree 1 node data to the node it is connected to
				SimpleEdge<T, List<Tree<List<T>>>> edge = v.getEdges().iterator().next();
				SimpleVertex<T, List<Tree<List<T>>>> target = edge.getTarget(v);
				
				//save maps
				Tree<List<T>> bag = new Tree<List<T>>(asArrayList(v.getData(), target.getData()));
				
				if(v.getMetadata() != null){
					for(Tree<List<T>> tree : v.getMetadata()){
						bag.addChild(tree);
					}
				}
				
				if(edge.getMetadata() != null){
					for(Tree<List<T>> tree : edge.getMetadata()){
						bag.addChild(tree);
					}
				}
				
				if(target.getMetadata() == null){
					target.setMetadata(new ArrayList<Tree<List<T>>>());
				}
				target.getMetadata().add(bag);
				
				//update graph
				graph.deleteVertex(v);
				if(target.getDegree() == 2){
					deg2.add(target);
				}
			}else if(v.getDegree() == 2){
				Iterator<SimpleEdge<T, List<Tree<List<T>>>>> edges = v.getEdges().iterator();
				SimpleEdge<T, List<Tree<List<T>>>> e1 = edges.next();
				SimpleEdge<T, List<Tree<List<T>>>> e2 = edges.next();
				SimpleVertex<T, List<Tree<List<T>>>> v1 = e1.getTarget(v);
				SimpleVertex<T, List<Tree<List<T>>>> v2 = e2.getTarget(v);
				
				//reuse existing edges if possible
				SimpleEdge<T, List<Tree<List<T>>>> edge = v1.getEdge(v2);
				boolean newEdge = false;
				if(edge == null){
					edge = graph.addEdge(v1, v2);
					newEdge = true;
				}
				
				//save map
				Tree<List<T>> bag = new Tree<List<T>>(asArrayList(v.getData(), v1.getData(), v2.getData()));
				
				if(e1.getMetadata() != null){
					for(Tree<List<T>> tree : e1.getMetadata()){
						bag.addChild(tree);
					}
				}
				
				if(e2.getMetadata() != null){
					for(Tree<List<T>> tree : e2.getMetadata()){
						bag.addChild(tree);
					}
				}
				
				if(v.getMetadata() != null){
					for(Tree<List<T>> tree : v.getMetadata()){
						bag.addChild(tree);
					}
				}
				
				if(edge.getMetadata() == null){
					edge.setMetadata(new ArrayList<Tree<List<T>>>());
				}
				edge.getMetadata().add(bag);
				
				//update graph
				graph.deleteVertex(v);
				if(!newEdge){
					if(v1.getDegree() == 2){
						deg2.add(v1);
					}
					
					if(v2.getDegree() == 2){
						deg2.add(v2);
					}
				}
			}else{
				throw new IllegalArgumentException("Input graph is not connected.");
			}
		}
		
		//everything remaining is the root node
		List<T> rootData = new ArrayList<T>();
		Tree<List<T>> root = new Tree<List<T>>(rootData);
		for(SimpleVertex<T, List<Tree<List<T>>>> v : graph.getVertices()){
			rootData.add(v.getData());
			if(v.getMetadata() != null){
				for(Tree<List<T>> tree : v.getMetadata()){
					root.addChild(tree);
				}
			}
		}
		
		for(SimpleEdge<T, List<Tree<List<T>>>> edge : graph.getEdges()){
			if(edge.getMetadata() != null){
				for(Tree<List<T>> tree : edge.getMetadata()){
					root.addChild(tree);
				}
			}
		}

		return root;
	}
	
	/**
	 * Finds a maximal (not maximum) matching in the given graph.
	 * @param <T> The graph data type.
	 * @param <M> The metadata data type.
	 * @param graph The graph to find a maximal matching for.
	 * @return The edges of the found maximal matching.
	 */
	public static <T extends IDable, M> List<SimpleEdge<T, M>> findMaximalMatching(SimpleGraph<T, M> graph){
		List<SimpleEdge<T, M>> matching = new ArrayList<SimpleEdge<T, M>>();
		Set<SimpleVertex<T, M>> usedVertices = new HashSet<SimpleVertex<T, M>>();
		
		for(SimpleEdge<T, M> edge : graph.getEdges()){
			if(!usedVertices.contains(edge.getFirstVertex()) && !usedVertices.contains(edge.getSecondVertex())){
				matching.add(edge);
				usedVertices.add(edge.getFirstVertex());
				usedVertices.add(edge.getSecondVertex());
			}
		}
		
		return matching;
	}
	
	/**
	 * Computes the Cartesian product of the given input sets. For example, for
	 * the following input sets:
	 * <pre>
	 * {
	 *   {A, B},
	 *   {C, D},
	 *   {E}
	 * }
	 * </pre>
	 * The output is as follows:
	 * <pre>
	 * {
	 *   {A, C, E},
	 *   {A, D, E},
	 *   {B, C, E},
	 *   {B, D, E}
	 * }
	 * </pre>
	 * 
	 * @param <T> The type of the data in the lists.
	 * @param sets The sets to compute the Cartesian product of.
	 * @return The computed Cartesian product of the input sets.
	 * @throws ArithmeticException When the size of the output list with
	 *         sets would be more than {@link Integer#MAX_VALUE}.
	 */
	public static <T> List<List<T>> cartesianProduct(List<List<T>> sets) throws ArithmeticException{
		int size = sets.stream().mapToInt(List::size).reduce(StrictMath::multiplyExact).getAsInt();
		List<List<T>> product = new ArrayList<List<T>>(size);
		for(int i = 0; i < size; i++){
			product.add(new ArrayList<T>());
		}
		
		if(size == 0){
			return product;
		}
		
		for(List<T> set : sets){
			size /= set.size();
			
			int idx = 0;
			for(int o = 0; idx < product.size(); o++){
				T obj = set.get(o % set.size());
				for(int i = 0; i < size; i++){
					product.get(idx++).add(obj);
				}
			}
		}
		
		return product;
	}
	
	/**
	 * Computes all unordered subsets of the given list of items. Computed
	 * sets are passed to the given consumer and should be treated as ephemeral.
	 * They only exist for the during of the consumer call and should be copied
	 * if persistence is required. Note that unordered subsets are computed,
	 * this means that the output will not contain identical sets with a different
	 * order of items. In addition, while technically a valid subset, the empty set
	 * is never returned from this subroutine. Note that the input list itself will
	 * be one of the returned subsets. As an implementation note, it is allowed to
	 * add elements to the passed list of items in the consumer, though these items
	 * will not be considered for any subsets.
	 * @param <T> The list data type.
	 * @param items The items to compute subsets of.
	 * @param consumer The consumer computed subsets are passed to.
	 */
	public static <T> void computeAllSubsets(List<T> items, Consumer<List<T>> consumer){
		computeAllSubsets(items, 0, items.size(), new ArrayList<T>(), consumer);
	}
	
	/**
	 * Computes all unordered subsets of the given list of items. Computed
	 * sets are passed to the given consumer and should be treated as ephemeral.
	 * They only exist for the during of the consumer call and should be copied
	 * if persistence is required. Note that unordered subsets are computed,
	 * this means that the output will not contain identical sets with a different
	 * order of items. In addition, while technically a valid subset, the empty set
	 * is never returned from this subroutine. Note that the input list itself will
	 * be one of the returned subsets. As an implementation note, it is allowed to
	 * add elements to the passed list of items in the consumer, though these items
	 * will not be considered for any subsets.
	 * @param <T> The list data type.
	 * @param items The items to compute subsets of.
	 * @param offset The current item to consider of inclusion in the output.
	 * @param max The maximum index in the array to consider for any subset.
	 * @param set The current output working set.
	 * @param consumer The consumer computed subsets are passed to.
	 */
	private static <T> void computeAllSubsets(List<T> items, int offset, final int max, List<T> set, Consumer<List<T>> consumer){
		if(offset >= max){
			if(!set.isEmpty()){
				consumer.accept(set);
			}
		}else{
			//don't pick the element
			computeAllSubsets(items, offset + 1, max, set, consumer);
			
			//pick the element
			set.add(items.get(offset));
			computeAllSubsets(items, offset + 1, max, set, consumer);
			set.remove(set.size() - 1);
		}
	}
	
	/**
	 * Reads a database graph from the file at the given path. The graph file is expected
	 * to be encoded using UTF-8 and have the following format:
	 * <ol>
	 * <li>First a single header line containing the following three
	 * integers in order separated by a single space:<br>
	 * <pre>{@code <vertex count> <edge count> <label count>}</pre></li>
	 * <li>Then a number of lines matching the header edge count,
	 * each containing a single edge definition specified by three
	 * integers separated by a single space in the following format:<br>
	 * <pre>{@code <source vertex> <target vertex> <label>}</pre></li>
	 * </ol>
	 * Note: empty lines and comments are not permitted between edge definitions.
	 * @param file The input file to read from.
	 * @return The constructed database graph instance.
	 * @throws IOException When an IOException occurs.
	 */
	public static final IntGraph readGraph(Path file) throws IOException{
		try(InputStream in = Files.newInputStream(file)){
			return readGraph(in);
		}
	}

	/**
	 * Reads a database graph from the given input stream. The graph file is expected
	 * to be encoded using UTF-8 and have the following format:
	 * <ol>
	 * <li>First a single header line containing the following three
	 * integers in order separated by a single space:<br>
	 * <pre>{@code <vertex count> <edge count> <label count>}</pre></li>
	 * <li>Then a number of lines matching the header edge count,
	 * each containing a single edge definition specified by three
	 * integers separated by a single space in the following format:<br>
	 * <pre>{@code <source vertex> <target vertex> <label>}</pre></li>
	 * </ol>
	 * Note: empty lines and comments are not permitted between edge definitions.
	 * @param in The input stream to read from.
	 * @return The constructed database graph instance.
	 * @throws IOException When an IOException occurs.
	 */
	public static final IntGraph readGraph(InputStream in) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		String header = reader.readLine();
		if(header == null){
			return null;
		}

		String[] metadata = header.split(" ");
		int vertices = Integer.parseInt(metadata[0]);
		int labels = Integer.parseInt(metadata[2]);
		IntGraph graph = new IntGraph(vertices, labels);

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

	/**
	 * Reads a query workload from the given file. The file is assumed to be encoded
	 * using UTF-8 with at most one query per line in the following format:
	 * <pre>{@code <source>, <query>, <target>}</pre>
	 * Lines that do not contain a valid query definition are ignored. The source and
	 * target vertex are expected to be given as integers, however, if unbound/free then
	 * <code>*</code> has to be specified.
	 * @param file The file to read from.
	 * @param parser The parser to use to parse the query definition into a query instance.
	 * @return A list with the parsed queries.
	 * @throws IOException When an IOException occurs.
	 */
	public static final List<PathQuery> readWorkload(Path file, Function<String, QueryLanguageSyntax> parser) throws IOException{
		try(InputStream in = Files.newInputStream(file)){
			return readWorkload(in, parser);
		}
	}

	/**
	 * Reads a query workload from the given input stream. The input stream is assumed
	 * to represent a text file encoded using UTF-8 with at most one query per line in
	 * the following format:
	 * <pre>{@code <source>, <query>, <target>}</pre>
	 * Lines that do not contain a valid query definition are ignored. The source and
	 * target vertex are expected to be given as integers, however, if unbound/free then
	 * <code>*</code> has to be specified.
	 * @param in The input stream to read from.
	 * @param parser The parser to use to parse the query definition into a query instance.
	 * @return A list with the parsed queries.
	 * @throws IOException When an IOException occurs.
	 */
	public static final List<PathQuery> readWorkload(InputStream in, Function<String, QueryLanguageSyntax> parser) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
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
