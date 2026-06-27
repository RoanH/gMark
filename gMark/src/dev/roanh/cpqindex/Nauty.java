/*
 * CPQ-native Index: A graph database index with native support for CPQs.
 * Copyright (C) 2023  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/CPQ-native-index
 *
 * CPQ-native Index is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CPQ-native Index is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.cpqindex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.nauty.api.NautyApi;
import dev.roanh.nauty.struct.SparseGraph;

/**
 * This class provides and interface to the native binding for nauty.
 * @author Roan
 *
 */
public class Nauty{

	public static int[] computeCanonicalLabelling(NautyApi nauty, ColoredGraph graph){
		int[] lab = graph.computeLab();
		try{
			nauty.computeCanonicalLabelling2(graph.computeGraph(), lab, graph.computePtn());
		}catch(InterruptedException e){
			throw new IllegalStateException(e);
		}
		return lab;
	}
	
	
	/**
	 * Computes a canonical labelling of the given coloured graph. The labelling
	 * is returned as an array of integers showing how to relabel the vertices
	 * in the graph. Each index of this array contains the ID of the vertex that
	 * previously had the ID of that index in the array.
	 * @param graph The graph to compute a canonical labelling of.
	 * @return The computed relabelling mapping.
	 */
	public static int[] computeCanonicalLabellingNative(ColoredGraph graph){
		int[] colors = prepareColors(graph);
		return computeCanonSparse(graph.getAdjacencyList(), colors);
	}
	
	/**
	 * Performs a canonical labelling of the given input graph.
	 * @param adj The input graph in adjacency list format, <code>n</code>
	 *        arrays with each the indices of the neighbours of the <code>
	 *        n</code>-th vertex.
	 * @param colors The array containing raw color information data. Contains vertex
	 *        indices in blocks of the same color with the start of a block of the same
	 *        color being indicated by a negated value. All vertex indices are also always
	 *        one higher than their actual index in the graph.
	 * @return A canonical relabelling of the graph returned as an array of integers showing
	 *         how to relabel the vertices in the graph. Each index of this array contains
	 *         the ID of the vertex that previously had the ID of that index in the array.
	 */
	protected static native int[] computeCanonSparse(int[][] adj, int[] colors);
	
	/**
	 * Computes a nauty and traces compatible array of color data. The
	 * returned array will have consecutive sections of nodes with the
	 * same color. The node is indicated with a number one higher than
	 * the ID of the actual it corresponds to. Negated number indicate
	 * the end of a range of nodes with the same color.
	 * @param graph The coloured graph to compute color data from.
	 * @return The constructed colour data.
	 */
	protected static int[] prepareColors(ColoredGraph graph){
		int[] colors = new int[graph.getNodeCount()];
		int idx = 0;
		for(int[] group : graph.getColorLists()){
			for(int i = 0; i < group.length - 1; i++){
				colors[idx++] = group[i] + 1;
			}
			colors[idx++] = -group[group.length - 1] - 1;
		}
		return colors;
	}
	
//	static{
//		try{
//			IndexUtil.loadNatives();
//		}catch(IOException e){
//			throw new LinkageError("Failed to extract native librray", e);
//		}
//	}
	
	/**
	 * Represents a coloured graph. Colours are assigned to 4 categories
	 * in this graph:
	 * <ol>
	 * <li>Each label is represented by a colour.</li>
	 * <li>The source vertex is represented by a colour.</li>
	 * <li>The target vertex is represented by a colour
	 * unless the target vertex equals the source vertex.</li>
	 * <li>Any remaining vertices are represented by a colour if any.</li>
	 * </ol>
	 * @author Roan
	 */
	public static class ColoredGraph{
		/**
		 * The adjacency list representing the graph.
		 */
		private int[][] graph;//TODO simplegraph
		/**
		 * A collection of lists where each list has the
		 * IDs of nodes with the same colour. The label
		 * for the colour is also present in each entry.
		 * Excludes the special collection of nodes without
		 * label. The list items are sorted on predicate ID.
		 */
		private List<Entry<Predicate, int[]>> labels;
		/**
		 * List of node IDs that have no label.
		 */
		private int[] noLabel;
		/**
		 * The ID of the source vertex of the graph.
		 */
		private int source;
		/**
		 * The ID of the target vertex of the graph.
		 */
		private int target;
		
		/**
		 * Constructs a new coloured graph with the given
		 * adjacency list and colour information.
		 * @param adj The adjacency list of the graph.
		 * @param source The node ID of the source vertex of the graph.
		 * @param target The node ID of the target vertex of the graph.
		 * @param labels A list of node IDs for each label.
		 * @param nolabel A list of node IDs without any label.
		 */
		public ColoredGraph(int[][] adj, int source, int target, List<Entry<Predicate, int[]>> labels, int[] nolabel){
			graph = adj;
			this.labels = labels;
			noLabel = nolabel;
			this.source = source;
			this.target = target;
		}
		
		/**
		 * Gets the total number of nodes in this graph.
		 * @return The total number of nodes in this graph.
		 */
		public int getNodeCount(){
			return graph.length;
		}
		
		/**
		 * Gets the IDs of nodes without a label/colour.
		 * @return The IDs of nodes without a label/colour.
		 */
		public int[] getNoLabels(){
			return noLabel;
		}
		
		/**
		 * Gets a list of colour information in the form of a list of
		 * entries where each entry has the colour label and IDs of
		 * vertices with that colour.
		 * @return Gets the IDs of the coloured vertices and labels.
		 */
		public List<Entry<Predicate, int[]>> getLabels(){
			return labels;
		}
		
		/**
		 * Gets the colour information of this graph as a list of
		 * lists where each list has the IDs of vertices of the same colour.
		 * @return The colour information as a list of lists.
		 */
		public List<int[]> getColorLists(){
			List<int[]> colors = new ArrayList<int[]>(labels.size() + 1 + (source == target ? 1 : 2));
			
			colors.add(new int[]{source});
			if(target != source){
				colors.add(new int[]{target});
			}
			
			for(Entry<Predicate, int[]> entry : labels){
				colors.add(entry.getValue());
			}
			
			if(noLabel.length > 0){
				colors.add(noLabel);
			}
			
			return colors;
		}
		
		public int[] computePtn(){
			int[] ptn = new int[graph.length];
			int off = 1;
			
			if(target != source){
				off++;
			}
			
			for(Entry<Predicate, int[]> entry : labels){
				int len = entry.getValue().length;
				Arrays.fill(ptn, off, off + len - 1, 1);
				off += len;
			}
			
			if(noLabel.length > 0){
				Arrays.fill(ptn, off, off + noLabel.length - 1, 1);
			}
			
			return ptn;
		}
		
		public int[] computeLab(){
			int[] lab = new int[graph.length];
			int off = 0;
			
			lab[off++] = source;
			if(target != source){
				lab[off++] = target;
			}
			
			for(Entry<Predicate, int[]> entry : labels){
				int len = entry.getValue().length;
				System.arraycopy(entry.getValue(), 0, lab, off, len);
				off += len;
			}
			
			if(noLabel.length > 0){
				System.arraycopy(noLabel, 0, lab, off, noLabel.length);
			}
			
			return lab;
		}
		
		//TODO could probably be the regular data form inside
		public SparseGraph computeGraph(){
			int edges = 0;
			for(int[] v : graph){
				edges += v.length;
			}
			
			SparseGraph g = new SparseGraph(graph.length, edges);
			int off = 0;
			for(int i = 0; i < graph.length; i++){
				int[] v = graph[i];
				g.d[i] = v.length;
				g.v[i] = off;
				System.arraycopy(v, 0, g.e, off, v.length);
				off += v.length;
			}
			
			return g;
		}
		
		/**
		 * Gets the adjacency list representation of this graph.
		 * @return The adjacency list representation of this graph.
		 */
		public int[][] getAdjacencyList(){
			return graph;
		}
	}
}
