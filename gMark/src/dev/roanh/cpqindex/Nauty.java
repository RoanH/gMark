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

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.nauty.api.CanonicalResult;
import dev.roanh.nauty.api.NautyApi;
import dev.roanh.nauty.struct.SparseGraph;

/**
 * This class provides and interface to the native binding for nauty.
 * @author Roan
 *
 */
public class Nauty{
	
//	/**
//	 * Computes a canonical labelling of the given coloured graph. The labelling
//	 * is returned as an array of integers showing how to relabel the vertices
//	 * in the graph. Each index of this array contains the ID of the vertex that
//	 * previously had the ID of that index in the array.
//	 * @param graph The graph to compute a canonical labelling of.
//	 * @return The computed relabelling mapping.
//	 */

	public static CanonicalResult computeCanonicalLabelling(NautyApi nauty, ColoredGraph graph){
		try{
			return nauty.computeCanonicalLabelling(graph.computeGraph(), graph.computeLab(), graph.computePtn());
		}catch(InterruptedException e){//TODO throw?
			throw new IllegalStateException(e);
		}
	}
	
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
		 * The graph.
		 */
		private SparseGraph graph;
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
		 * graph and colour information.
		 * @param graph The graph.
		 * @param source The node ID of the source vertex of the graph.
		 * @param target The node ID of the target vertex of the graph.
		 * @param labels A list of node IDs for each label.
		 * @param nolabel A list of node IDs without any label.
		 */
		public ColoredGraph(SparseGraph graph, int source, int target, List<Entry<Predicate, int[]>> labels, int[] nolabel){
			this.graph = graph;
			this.labels = labels;
			noLabel = nolabel;
			this.source = source;
			this.target = target;
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
		
		public int[] computePtn(){
			int[] ptn = new int[graph.nv];
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
			int[] lab = new int[graph.nv];
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
		
		public SparseGraph computeGraph(){
			return graph;
		}
	}
}
