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
package dev.roanh.gmark.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.SimpleGraph.SimpleEdge;
import dev.roanh.gmark.util.SimpleGraph.SimpleVertex;
import dev.roanh.gmark.util.UniqueGraph.GraphEdge;
import dev.roanh.gmark.util.UniqueGraph.GraphNode;

/**
 * Class providing various small utilities as well
 * as thread bound random operations.
 * @author Roan
 */
public class Util{
	/**
	 * Random instances for each thread.
	 */
	private static final ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);
	
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
		return !Files.walk(folder).filter(path->!path.equals(folder)).findFirst().isPresent();
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
	
	public static <T> void runIfNotNull(T data, Consumer<T> fun){
		if(data != null){
			fun.accept(data);
		}
	}
	
	public static <T> Tree<List<T>> computeTreeDecompositionWidth2(SimpleGraph<T> graph){
		Deque<SimpleVertex<T>> deg2 = new ArrayDeque<SimpleVertex<T>>();
		Map<SimpleVertex<T>, List<Tree<List<T>>>> vMaps = new HashMap<SimpleVertex<T>, List<Tree<List<T>>>>();
		Map<SimpleEdge<T>, Tree<List<T>>> eMaps = new HashMap<SimpleEdge<T>, Tree<List<T>>>();
		
		//collect all vertices of degree at most k
		for(SimpleVertex<T> vertex : graph.getVertices()){
			if(vertex.getDegree() <= 2){
				deg2.add(vertex);
			}
		}
		
		System.out.println("queue: " + deg2.stream().map(v->v.getData()).collect(Collectors.toList()));
		
		//contract nodes of degree at most 2
		while(graph.getVertexCount() > 3){
			if(deg2.isEmpty()){
				throw new IllegalArgumentException("Treewidth of the input graph is more than 2.");
			}
			
			SimpleVertex<T> v = deg2.pop();
			
			System.out.println("Process: " + v.getData() + " / " + v.getDegree());
			
			if(v.getDegree() == 1){
				//move degree 1 node data to the node it is connected to
				SimpleVertex<T> target = v.getEdges().iterator().next().getTarget(v);
				vMaps.computeIfAbsent(target, k->new ArrayList<Tree<List<T>>>()).add(new Tree<List<T>>(Arrays.asList(v.getData(), target.getData())));
				graph.deleteVertex(v);
			}else if(v.getDegree() == 2){
				Iterator<SimpleEdge<T>> edges = v.getEdges().iterator();
				SimpleEdge<T> e1 = edges.next();
				SimpleEdge<T> e2 = edges.next();
				SimpleVertex<T> v1 = e1.getTarget(v);
				SimpleVertex<T> v2 = e2.getTarget(v);
				
				//reuse existing edges if possible
				SimpleEdge<T> edge = v1.getEdge(v2);
				if(edge == null){
					edge = graph.addEdge(v1, v2);
				}
				assert eMaps.get(edge) == null;
				
				//save map
				Tree<List<T>> bag = new Tree<List<T>>(Arrays.asList(v.getData(), v1.getData(), v2.getData()));
				runIfNotNull(eMaps.get(e1), bag::addChild);
				runIfNotNull(eMaps.get(e2), bag::addChild);
				runIfNotNull(vMaps.get(v), children->children.forEach(bag::addChild));
				eMaps.put(edge, bag);
				
				//update graph
				graph.deleteVertex(v);
			}else{
				throw new IllegalArgumentException("Input graph is not connected.");
			}
		}
		
		//everything remaining is the root node
		Tree<List<T>> root = new Tree<List<T>>(graph.getVertices().stream().map(SimpleVertex::getData).collect(Collectors.toList()));
		graph.getEdges().forEach(e->runIfNotNull(eMaps.get(e), root::addChild));
		return root;
	}
	
	public static <T> Tree<List<T>> computeTreeDecomposition(SimpleGraph<T> graph, int k){
		@SuppressWarnings("unchecked")
		SimpleGraph<Object> g = (SimpleGraph<Object>)graph;
		if(g.getEdgeCount() <= k * g.getVertexCount() - k * (k - 1) / 2){
			return null;
		}
		
		//count friendly vertices
		int d = 2 * k * k * k * (k + 1) * (4 * k * k + 12 * k + 16);
		int friendly = 0;
		outer: for(SimpleVertex<Object> v : g.getVertices()){
			if(v.getDegree() <= d){
				for(SimpleEdge<Object> adj : v.getEdges()){
					if(adj.getTarget(v).getDegree() <= d){
						friendly++;
						continue outer;
					}
				}
			}
		}
		
		if(g.getVertexCount() / (4 * k * k + 12 * k + 16) >= friendly){
			//i. compute a maximal matching
			List<SimpleEdge<Object>> matching = findMaximalMatching(g);
			
			//ii. compute graph G' obtained by contracting all edges in M
			for(SimpleEdge<Object> edge : matching){
				SimpleVertex<Object> vertex = g.addVertex(new ContractedEdge(edge.getFirstVertex().getData(), edge.getSecondVertex().getData()));
				g.contractEdge(edge, vertex);
			}
			
			//iii. Recurse the algorithm on G'
			Tree<List<Object>> decomp = computeTreeDecomposition(g, k);
			if(decomp == null){
				return null;
			}else{
				//v. create a tree decomposition by Lemma 3.3 (width at most 2k + 1) 
				decomp.forEach(node->{
					ListIterator<Object> iter = node.getData().listIterator();
					while(iter.hasNext()){
						Object item = iter.next();
						if(item instanceof ContractedEdge){
							ContractedEdge ce = (ContractedEdge)item;
							iter.set(ce.first);
							iter.add(ce.second);
						}
					}
				});
				
				//vi. Check treewidth using Theorem 2.4 and refine down to a width of k
				
				
				
				

				//return decomposition

			}
		}else{ 
			//i. Compute the improved graph of G by paragraph 6
			
			
			
			
			
			//TODO continue at "By inspecting Q" -- also double check the sorting is okay like this
			
		
			//ii. Exit if an I-simplicial vertex with degree at least K+1 exists
		
			//iii. Put I-simplicial vertices in a set SL, compute graph by removing all 
		    //     vertices (and edges) from SL
		
			//iv. If |SL| < c2 * |V| then exit
		
			//v. Recursively run the algorithm on G'
		
		
			//vi. Stop if recursion says the treewidth is >k
		
			//vii. Put back SL vertices according to the given procedure
		
			//return the constructed decomposition
		
		
		
		}
		
		return null;//TODO
	}
	
	//modifies input
	public static <T> SimpleGraph<T> computeSimplicialVertices(SimpleGraph<T> graph, int k){
		List<Triple<T>> queue = new ArrayList<Triple<T>>();
		Set<Pair<T>> exists = new HashSet<Pair<T>>();
//		Map<SimpleVertex<T>, List<Pair<T>>> sets = new HashMap<SimpleVertex<T>, List<Pair<T>>>();
		
		//add pairs of the form ((vi, vj), -)
		for(SimpleEdge<T> e : graph.getEdges()){
			if(e.getFirstVertex().getID() < e.getSecondVertex().getID()){
				//queue.add(new Triple<T>(e.getFirstVertex(), e.getSecondVertex()));
				exists.add(new Pair<T>(e.getFirstVertex(), e.getSecondVertex()));
			}
		}
		
		//add pairs of the form ((vi, vj), v) for v with degree at most k
		for(SimpleVertex<T> v : graph.getVertices()){
			if(v.getDegree() <= k){
				List<SimpleVertex<T>> neighb = v.getEdges().stream().map(e->e.getTarget(v)).collect(Collectors.toList());
				for(SimpleVertex<T> a : neighb){
					for(SimpleVertex<T> b : neighb){
						if(a.getID() < b.getID()){
							queue.add(new Triple<T>(a, b, v));
						}
					}
				}
			}
		}
		
		for(Triple<T> t : queue){
			System.out.println(t);
		}
		
		//sort on first vertex ID then second vertex ID
		Collections.sort(queue, (a, b)->{
			int c = Integer.compare(a.first.getID(), b.first.getID());
			if(c == 0){
				return Integer.compare(a.second.getID(), b.second.getID());
			}else{
				return c;
			}
		});
		
		System.out.println("Post sort");
		for(Triple<T> t : queue){
			System.out.println(t);
		}
		
		//scan for pairs with k+1 shared neighbours
		Triple<T> last = queue.get(0);
		int start = 0;
		for(int i = 1; i < queue.size(); i++){
			Triple<T> t = queue.get(i);
			if(!t.first.equals(last.first) || !t.second.equals(last.second)){
				System.out.println("range end: " + start + " - " + i);
				if(i - start >= k + 1 && t.first.hasEdge(t.second)){
					for(int j = start; j < i; j++){
						graph.addEdge(t.first, t.second);
					}
				}
				start = i;
			}
			
			last = t;
		}
		
		//handle last range
		if(queue.size() - start >= k + 1 && last.first.hasEdge(last.second)){
			System.out.println("range end: " + start + " - " + queue.size());
			for(int j = start; j < queue.size(); j++){
				graph.addEdge(last.first, last.second);
			}
		}
		
		System.out.print("end: " + queue.size() + " / " + start + " | " + last.first.hasEdge(last.second));
		
		return graph;
	}
	
	public static <T> List<SimpleEdge<T>> findMaximalMatching(SimpleGraph<T> graph){
		List<SimpleEdge<T>> matching = new ArrayList<SimpleEdge<T>>();
		Set<SimpleVertex<T>> usedVertices = new HashSet<SimpleVertex<T>>();
		
		for(SimpleEdge<T> edge : graph.getEdges()){
			if(!usedVertices.contains(edge.getFirstVertex()) && !usedVertices.contains(edge.getSecondVertex())){
				matching.add(edge);
				usedVertices.add(edge.getFirstVertex());
				usedVertices.add(edge.getSecondVertex());
			}
		}
		
		return matching;
	}
	
	private static final class ContractedEdge{
		private final Object first;
		private final Object second;
		
		private ContractedEdge(Object first, Object second){
			this.first = first;
			this.second = second;
		}
	}
	
	private static class Pair<T>{
		protected final SimpleVertex<T> first;
		protected final SimpleVertex<T> second;
		
		public Pair(SimpleVertex<T> first, SimpleVertex<T> second){
			this.first = first;
			this.second = second;
		}
		
		@Override
		public boolean equals(Object obj){
			if(obj instanceof Pair){
				Pair<?> p = (Pair<?>)obj;
				return p.first.equals(first) && p.second.equals(second);
			}else{
				return false;
			}
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(first, second);
		}
	}
	
	private static final class Triple<T> extends Pair<T>{
		private final SimpleVertex<T> third;
		
		private Triple(SimpleVertex<T> first, SimpleVertex<T> second, SimpleVertex<T> third){
			super(first, second);
			this.third = third;
		}
		
		@Override
		public boolean equals(Object obj){
			if(obj instanceof Triple){
				Triple<?> t = (Triple<?>)obj;
				return super.equals(t) && Objects.equals(t.third, third);
			}else{
				return false;
			}
		}

		@Override
		public int hashCode(){
			return Objects.hash(first, second, third);
		}
		
		@Override
		public String toString(){
			return "Triple[" + first.getData() + ", " + second.getData() + ", " + third.getData() + "]";
		}
	}
	
	private static final class DecompMeta<T>{
		private int degree;
		private Tree<T> bag = null;
		
		private DecompMeta(int deg){
			degree = deg;
		}
	}
}
