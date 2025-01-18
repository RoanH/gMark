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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Edge;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;
import dev.roanh.gmark.exception.GenerationException;

/**
 * The selectivity graph is a directed graph between
 * {@link SelectivityType selectivity types}. Given some
 * maximum path length this graph shows between which selectivity
 * types a path of length at most the given maximum path length
 * exists. Originally the selectivity graph is unlabelled, but
 * within gMark it is labelled with the selectivity class needed
 * to follow an edge. That is the links are as follows
 * <code>(t1,s1) --s2-&gt; (t2,s1*s2)</code>.
 * @author Roan
 * @see SelectivityType
 * @see SelectivityClass
 * @see Schema
 */
public class SelectivityGraph extends UniqueGraph<SelectivityType, SelectivityClass>{
	/**
	 * Efficient lookup index for the graph from selectivity type to graph node.
	 */
	private final RangeList<Map<SelectivityClass, GraphNode<SelectivityType, SelectivityClass>>> index;
	/**
	 * The graph schema used to construct
	 */
	private final Schema schema;
	
	/**
	 * Constructs a new selectivity graph based off the
	 * given graph schema and with the given maximum path length.
	 * @param schema The graph schema to use.
	 * @param maxLength The maximum path length.
	 */
	public SelectivityGraph(Schema schema, int maxLength){
		this.schema = schema;
		index = new RangeList<Map<SelectivityClass, GraphNode<SelectivityType, SelectivityClass>>>(schema.getTypeCount(), Util.selectivityMapSupplier());
		DistanceMatrix matrix = computeDistanceMatrix(schema);
	
		//add graph edges
		for(Type t1 : schema.getTypes()){
			for(Type t2 : schema.getTypes()){
				for(SelectivityClass sel1 : SelectivityClass.values()){
					for(SelectivityClass sel2 : SelectivityClass.values()){
						//if there exists a path of valid length from t1 to t2 with result selectivity class sel2
						if(matrix.get(t1, t2).getOrDefault(sel2, Integer.MAX_VALUE) <= maxLength){
							//add graph edge (t1, sel1) --sel2-> (t2, sel1 * sel2)
							resolve(t1, sel1).addUniqueEdgeTo(resolve(t2, sel1.conjunction(sel2)), sel2);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Resolves the given selectivity type presented as
	 * a type and selectivity class to the associated
	 * graph node adding a new node if required.
	 * @param type The type of the selectivity type.
	 * @param sel The selectivity of the selectivity type.
	 * @return The graph node associated with the given selectivity type.
	 */
	private GraphNode<SelectivityType, SelectivityClass> resolve(Type type, SelectivityClass sel){
		return index.get(type).computeIfAbsent(sel, k->addUniqueNode(new SelectivityType(type, sel)));
	}
	
	/**
	 * Randomly generates a path through the selectivity graph with
	 * the requested length that ends at a node with the given
	 * selectivity (meaning that this is also the selectivity of
	 * the path as a whole). Finally, the multiplicity of the path
	 * can be specified (Kleene star probability).
	 * @param selectivity The selectivity of the path to generate.
	 * @param length The length of the path to generate.
	 * @param star The probability that a path segment can have a Kleene
	 *        star above it, should be between 0 and 1.
	 * @return A list of path segments representing the edges in the generated path.
	 * @throws GenerationException When something going wrong while
	 *         generating the path. Most likely meaning not valid
	 *         path was found satisfying all the requirements.
	 * @see #generateRandomPath(Selectivity, SelectivityType, int, double)
	 */
	public List<PathSegment> generateRandomPath(Selectivity selectivity, int length, double star) throws GenerationException{
		return generateRandomPath(selectivity, null, length, star);
	}
	
	/**
	 * Randomly generates a path through the selectivity graph with
	 * the requested length that ends at a node with the given
	 * selectivity (meaning that this is also the selectivity of
	 * the path as a whole). Optionally a starting selectivity 
	 * type can be provided. Finally, the multiplicity of the path
	 * can be specified (Kleene star probability).
	 * @param selectivity The selectivity of the path to generate.
	 * @param start The selectivity type to start from, if this is
	 *        <code>null</code> the path is started from any node
	 *        that has the {@link SelectivityClass#EQUALS} selectivity.
	 * @param length The length of the path to generate.
	 * @param star The probability that a path segment can have a Kleene
	 *        star above it, should be between 0 and 1.
	 * @return A list of path segments representing the edges in the generated path.
	 * @throws GenerationException When something going wrong while
	 *         generating the path. Most likely meaning not valid
	 *         path was found satisfying all the requirements.
	 * @see #generateRandomPath(Selectivity, int, double)
	 */
	public List<PathSegment> generateRandomPath(Selectivity selectivity, SelectivityType start, int length, double star) throws GenerationException{
		DistanceMatrix matrix = computeNumberOfPaths(selectivity, length);
		
		//TODO currentNode and currentSel form a valid SelecitivityType see if we can rewrite the logic to use that
		int currentNode = 0;
		SelectivityClass currentSel = SelectivityClass.EQUALS;
		
		if(start != null){
			currentNode = start.getTypeID();
		}else{
			int m = index.size();
			int paths = 0;
			for(int i = 0; i < m; i++){
				paths += matrix.get(length, i).getOrDefault(SelectivityClass.EQUALS, 0);
			}

			if(paths == 0){
				throw new GenerationException("Failed to generate a random path (no paths)");
			}
			
			int rnd = Util.uniformRandom(1, paths);
			int acc = 0;
			for(int i = 0; i < m; i++){
				acc += matrix.get(length, i).getOrDefault(SelectivityClass.EQUALS, 0);
				if(acc >= rnd){
					currentNode = i;
					break;
				}
			}
		}
		
		if(matrix.get(length, currentNode).getOrDefault(currentSel, 0) == 0){
			throw new GenerationException("Failed to generate a random path");
		}
		
		List<PathSegment> path = new ArrayList<PathSegment>(length);
		for(int i = length; i > 0; i--){
			
			//self loop test
			boolean test = false;
			for(GraphEdge<SelectivityType, SelectivityClass> edge : index.get(currentNode).get(SelectivityClass.EQUALS).getOutEdges()){
				SelectivityType target = edge.getTarget();
				if(target.getSelectivity() == SelectivityClass.EQUALS && target.getType().getID() == currentNode){
					test = true;
					break;
				}
			}
			
			if(test && Util.getRandom().nextDouble() <= star){
				path.add(PathSegment.of(schema, currentNode, SelectivityClass.EQUALS, currentNode, true));
			}else{
				int previousNode = currentNode;
				SelectivityClass sel = null;
				
				int rnd = Util.uniformRandom(1, matrix.get(i, currentNode).get(currentSel));
				int acc = 0;
				for(GraphEdge<SelectivityType, SelectivityClass> edge : index.get(currentNode).get(currentSel).getOutEdges()){
					SelectivityType target = edge.getTarget();
					acc += matrix.get(i - 1, target.getType()).getOrDefault(target.getSelectivity(), 0);
					if(acc >= rnd){
						currentNode = target.getTypeID();
						currentSel = target.getSelectivity();
						sel = edge.getData();
						break;
					}
				}
				path.add(PathSegment.of(schema, previousNode, sel, currentNode, false));
			}
		}
		
		return path;
	}
	
	/**
	 * Computes a distance matrix containing for each pair of
	 * selectivity types how many paths of at most the given
	 * maximum length and with the given selectivity exist.
	 * @param selectivity The selectivity of the paths to count.
	 * @param length The maximum length of the paths to count.
	 * @return The number of paths for each pair of selectivity types.
	 */
	public DistanceMatrix computeNumberOfPaths(Selectivity selectivity, int length){
		int types = schema.getTypeCount();
		DistanceMatrix matrix = new DistanceMatrix(length + 1, types);
		
		for(int j = 0; j < types; j++){
			for(SelectivityClass sel : SelectivityClass.values()){
				if(sel.getSelectivity() == selectivity){
					matrix.get(0, j).put(sel, 1);
				}
			}
		}

		for(int i = 1; i <= length; i++){
			for(int j = 0; j < types; j++){
				for(SelectivityClass sel : SelectivityClass.values()){
					GraphNode<SelectivityType, SelectivityClass> node = index.get(j).get(sel);
					if(node != null && node.getOutCount() > 0){
						for(GraphEdge<SelectivityType, SelectivityClass> edge : node.getOutEdges()){
							SelectivityType target = edge.getTarget();
							matrix.get(i, j).merge(
								sel,
								matrix.get(i - 1, target.getType()).getOrDefault(target.getSelectivity(), 0),
								Integer::sum
							);
						}
					}
				}
			}
		}
		
		return matrix;
	}
	
	/**
	 * Computes the shortest path between any two nodes in
	 * the schema graph. This is done using a modified version
	 * of the Floyd-Warshall algorithm (all-pairs shortest path).
	 * @param schema The schema to compute the distance matrix for.
	 * @return The distance matrix for the given graph schema.
	 */
	public static DistanceMatrix computeDistanceMatrix(Schema schema){
		int size = schema.getTypeCount();
		int logSize = (int)Math.log(size) + 4;//TODO why does this use a natural log?
		
		DistanceMatrix matrix = new DistanceMatrix(size);
		DistanceMatrix tmp = new DistanceMatrix(size);
		
		//add all length 1 paths
		for(Edge edge : schema.getEdges()){
			SelectivityClass sel = edge.getSelectivity();
			matrix.get(edge.getSourceType(), edge.getTargetType()).put(sel, 1);
			matrix.get(edge.getTargetType(), edge.getSourceType()).put(sel.negate(), 1);
		}
		
		for(int step = 0; step < logSize; step++){
			//make a copy of the matrix
			tmp.overwrite(matrix);
			
			//for each source type
			for(int i = 0; i < size; i++){
				//for each target type
				for(int j = 0; j < size; j++){
					//for pairs of connected edges
					for(int k = 0; k < size; k++){
						for(Entry<SelectivityClass, Integer> first : tmp.get(i, k).entrySet()){
							for(Entry<SelectivityClass, Integer> second : tmp.get(k, j).entrySet()){
								//update length to be the minimum of an existing length and the current path
								matrix.get(i, j).merge(
									first.getKey().conjunction(second.getKey()),//result of following these two edges after each other
									first.getValue() + second.getValue(),
									Math::min
								);
							}
						}
					}
				}
			}
		}
		
		return matrix;
	}
	
	/**
	 * Matrix that encoder pair wise information for
	 * all selectivity types in a graph.
	 * @author Roan
	 */
	public static final class DistanceMatrix extends RangeMatrix<Map<SelectivityClass, Integer>>{
		
		/**
		 * Constructs a new square distance matrix
		 * with the given dimensions. Note that these
		 * dimensions only apply to the type part of
		 * the selectivity types stored in this matrix.
		 * @param size The matrix dimensions (number of node types).
		 */
		private DistanceMatrix(int size){
			this(size, size);
		}
		
		/**
		 * Constructs a new distance matrix with the given
		 * dimensions. Note that these dimensions only apply
		 * to the type part of the selectivity types stored 
		 * in this matrix.
		 * @param rows The number of rows in the matrix.
		 * @param cols The number of columns in the matrix.
		 */
		private DistanceMatrix(int rows, int cols){
			super(rows, cols, Util.selectivityMapSupplier());
		}
		
		/**
		 * Overwrites all the data in this matrix with
		 * data from the given other matrix effectively
		 * making this matrix a copy of the given matrix.
		 * @param data The matrix to copy from.
		 */
		private void overwrite(DistanceMatrix data){
			for(int i = 0; i < getRowCount(); i++){
				for(int j = 0; j < getColumnCount(); j++){
					Map<SelectivityClass, Integer> cell = get(i, j);
					Map<SelectivityClass, Integer> other = data.get(i, j);
					for(SelectivityClass sel : SelectivityClass.values()){
						Integer value = other.get(sel);
						if(value == null){
							cell.remove(sel);
						}else{
							cell.put(sel, value);
						}
					}
				}
			}
		}
	}
}
