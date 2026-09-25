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
public class ColoredGraph{
	/**
	 * The graph.
	 */
	private final SparseGraph graph;
	/**
	 * A collection of lists where each list has the
	 * IDs of nodes with the same colour. The label
	 * for the colour is also present in each entry.
	 * Excludes the special collection of nodes without
	 * label. The list items are sorted on predicate ID.
	 */
	private final List<Entry<Predicate, int[]>> labels;
	/**
	 * List of node IDs that have no label.
	 */
	private final int[] noLabel;
	/**
	 * The ID of the source vertex of the graph.
	 */
	private final int source;
	/**
	 * The ID of the target vertex of the graph.
	 */
	private final int target;
	
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
	
	/**
	 * Computes a canonical labelling of this coloured graph.
	 * @param nauty The nauty instance to use to compute the canonical result.
	 * @return The computed canonical result transformation.
	 * @throws InterruptedException When the current thread is interrupted.
	 */
	public CanonicalResult computeCanonicalLabelling(NautyApi nauty) throws InterruptedException{
		return nauty.computeCanonicalLabelling(graph, computeLab(), computePtn());
	}
	
	/**
	 * Computes the label range array required by nauty as input for color information.
	 * @return The label range information.
	 * @see NautyApi#computeCanonicalLabelling(SparseGraph, int[], int[])
	 */
	private int[] computePtn(){
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
	
	/**
	 * Computes the vertex labels array required by nauty as input for color information.
	 * @return The vertex label assignment information.
	 * @see NautyApi#computeCanonicalLabelling(SparseGraph, int[], int[])
	 */
	private int[] computeLab(){
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
}