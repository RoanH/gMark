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
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.Graph.GraphEdge;
import dev.roanh.gmark.util.Graph.GraphNode;

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
	public static <V, E> Graph<Object, Void> edgeLabelsToNodes(Graph<V, E> in){
		Graph<Object, Void> out = new Graph<Object, Void>();
		
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
	
	public static void computeTreeDecomposition(Object graph, int k){
		
		
		//if |E| <= k * |V| - 0.5D * k * (k - 1)
		
			//i. compute a maximal matching
		
		
			//ii. compute graph G' obtained by contracting all edges in M
		
		
		
			//iii. Recurse the algorithm on G'
		
		
			//iv. If recursion says the graph has larger treewidth, then exit
		
		
			//v. Otherwise with the return tree decomposition make one for G with Lemma 3.3
		
		
			//vi. Theorem 2.4 check to see if the result graph has treewidth at most k
		
			//return decomposition
		
		//ELSE 
		
			//i. Compute the improved graph of G by paragraph 6
		
			//ii. Exit if an I-simplicial vertex with degree at least K+1 exists
		
			//iii. Put I-simplicial vertices in a set SL, compute graph by removing all 
		    //     vertices (and edges) from SL
		
			//iv. If |SL| < c2 * |V| then exit
		
			//v. Recursively run the algorithm on G'
		
		
			//vi. Stop if recursion says the treewidth is >k
		
			//vii. Put back SL vertices according to the given procedure
		
			//return the constructed decomposition
		
		
		
		
		
		
	}
}
