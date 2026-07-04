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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.cpq.QueryGraphCPQ;
import dev.roanh.gmark.lang.cpq.QueryGraphCPQ.Edge;
import dev.roanh.gmark.lang.cpq.QueryGraphCPQ.Vertex;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.nauty.Nauty;
import dev.roanh.nauty.api.CanonicalResult;
import dev.roanh.nauty.api.NautyApi;
import dev.roanh.nauty.struct.SparseGraph;

/**
 * Utility class to compute and represent the canonical form of a CPQ.
 * @author Roan
 * @see Nauty
 */
public class CanonForm{
	/**
	 * Maximum number of bits that will ever be required to encode a vertex label ID.
	 */
	private static final int MAX_LABEL_BITS = 5;//TODO input validation
	/**
	 * Maximum number of bits that will ever be required to encode a vertex ID.
	 */
	private static final int MAX_VERTEX_BITS = 10;//TODO input validation
	/**
	 * The vertex ID of the source vertex of the CPQ.
	 */
	private final int source;
	/**
	 * The vertex ID of the target vertex of the CPQ.
	 */
	private final int target;
	/**
	 * A map containing the IDs of vertices with a specific label. Note that this
	 * is encoded as the length of ranges of IDs with the same label.
	 */
	private final Map<Predicate, Integer> labels;
	/**
	 * The canonically labelled transformed CPQ query graph.
	 */
	private final SparseGraph graph;
	/**
	 * The CPQ this canonical form was constructed from.
	 */
	private final CPQ cpq;
	/**
	 * True if the original input CPQ was a core.
	 */
	private final boolean wasCore;
	
	/**
	 * Constructs a new canonical form with the given data.
	 * @param source The ID of the source vertex.
	 * @param target The ID of the target vertex.
	 * @param labels The IDs of labelled nodes by label.
	 * @param graph The canonically labelled graph.
	 * @param cpq The original CPQ.
	 * @param wasCore True if the original input was a core.
	 */
	private CanonForm(int source, int target, Map<Predicate, Integer> labels, SparseGraph graph, CPQ cpq, boolean wasCore){
		this.source = source;
		this.target = target;
		this.labels = labels;
		this.graph = graph;
		this.cpq = cpq;
		this.wasCore = wasCore;
	}
	
	/**
	 * Gets the CPQ this canonical form was constructed from.
	 * @return The CPQ this canonical form was constructed from.
	 */
	public CPQ getCPQ(){
		return cpq;
	}
	
	/**
	 * Check if the original input CPQ turned out to be a core.
	 * @return True if the original input was a core.
	 */
	public boolean wasCore(){
		return wasCore;
	}
	
	/**
	 * Constructs a canonical form for the core of the given CPQ.
	 * @param cpq The CPQ to compute a canonical form for.
	 * @param isCore If the given CPQ is guaranteed to be a core.
	 * @return The computed canonical form.
	 * @throws InterruptedException When the current thread is interrupted.
	 */
	public static CanonForm computeCanon(NautyApi nauty, CPQ cpq, boolean isCore) throws InterruptedException{
		QueryGraphCPQ original = cpq.toQueryGraph();
		QueryGraphCPQ core = isCore ? original : original.computeCore();
		
		//compute a coloured graph
		ColoredGraph input = toColoredGraph(core);
		
		//compute the canonical labelling with nauty
		CanonicalResult canon = input.computeCanonicalLabelling(nauty);

 		//relabel the source and target node
 		int source = canon.relabel(core.getSourceVertex().getID());
 		int target = canon.relabel(core.getTargetVertex().getID());
 		
 		//relabel labels
 		Map<Predicate, Integer> labels = new LinkedHashMap<Predicate, Integer>();
 		for(Entry<Predicate, int[]> pair : input.getLabels()){
 			labels.put(pair.getKey(), pair.getValue().length);
 		}
 		
 		//relabel the graph itself
		return new CanonForm(source, target, labels, canon.getCanonicalGraph(), cpq, original.getEdgeCount() == core.getEdgeCount());
	}
	
	/**
	 * Converts the given input query graph to a coloured graph instance.
	 * This is done by first transforming the graph to an unlabelled graph
	 * and then group vertices by label.
	 * @param graph The input query graph to transform.
	 * @return The constructed coloured graph.
	 * @see ColoredGraph
	 */
	public static ColoredGraph toColoredGraph(QueryGraphCPQ graph){
		//compute degrees
		Map<Predicate, LabelData> colorMap = new HashMap<Predicate, LabelData>();
		int[] deg = new int[graph.getVertexCount() + graph.getEdgeCount()];
		for(Edge edge : graph.getEdges()){
			deg[edge.getSource().getID()]++;
			deg[edge.getID()]++;
			colorMap.computeIfAbsent(edge.getLabel(), _->new LabelData()).idx++;
		}
		
		//pre size arrays (offsets point to last index initially)
		int[] voff = new int[graph.getVertexCount() + graph.getEdgeCount()];
		int nde = deg[0];
		voff[0] = nde;
		for(int i = 1; i < deg.length; i++){
			int len = deg[i];
			voff[i] = voff[i - 1] + len;
			nde += len;
		}
		int[] e = new int[nde];
		
		//pre size maps
		for(LabelData lab : colorMap.values()){
			lab.data = new int[lab.idx];
		}
		
		//compute adjacencies, fill from the end of the range
		for(Edge edge : graph.getEdges()){
			int eid = edge.getID();
			int sid = edge.getSource().getID();
			
			e[--voff[eid]] = edge.getTarget().getID();
			e[--voff[sid]] = eid;
			
			LabelData data = colorMap.get(edge.getLabel());
			data.data[--data.idx] = eid;
		}
		
		//collect no label vertices
		int[] nolabel = new int[graph.isLoop() ? (graph.getVertexCount() - 1) : (graph.getVertexCount() - 2)];
		int idx = 0;
		for(Vertex vertex : graph.getVertices()){
			if(vertex != graph.getSourceVertex() && vertex != graph.getTargetVertex()){
				nolabel[idx++] = vertex.getID();
			}
		}
		
		//process label data
		List<Entry<Predicate, int[]>> labels = new ArrayList<Entry<Predicate, int[]>>(colorMap.size());
		colorMap.entrySet().stream().sorted(Entry.comparingByKey()).forEach(entry->labels.add(Map.entry(entry.getKey(), entry.getValue().data)));
		
		//put together the final graph
		return new ColoredGraph(
			new SparseGraph(voff, deg, e),
			graph.getSourceVertex().getID(),
			graph.getTargetVertex().getID(),
			labels,
			nolabel
		);
	}
	
	/**
	 * Simple object for compiling information on objects with the same label.
	 * @author Roan
	 * @see CanonForm#toColoredGraph(QueryGraphCPQ)
	 */
	private static final class LabelData{
		/**
		 * Current read/write index.
		 */
		private int idx;
		/**
		 * The IDs of labelled vertices.
		 */
		private int[] data;
	}
	
	/**
	 * Computes the string variant of this canonical form. This representation
	 * is human readable, but it is also larger than {@link #toBinaryCanon()}
	 * or {@link #toBase64Canon()};
	 * @return The string form of this canonical form.
	 */
	public String toStringCanon(){
		StringBuilder buf = new StringBuilder();
		buf.append("s=");
		buf.append(source);
		buf.append(",t=");
		buf.append(target);
		buf.append(',');
		
		for(Entry<Predicate, Integer> pair : labels.entrySet()){
			buf.append('l');
			buf.append(pair.getKey().getID());
			buf.append("=");
			buf.append(pair.getValue());
			buf.append(",");
		}
		
		for(int i = 0; i < graph.nv; i++){
			buf.append('e');
			buf.append(i);
			buf.append("={");
			int voff = graph.v[i];
			for(int v = 0; v < graph.d[i]; v++){
				buf.append(graph.e[voff + v]);
				buf.append(',');
			}
			if(graph.d[i] != 0){
				buf.deleteCharAt(buf.length() - 1);
			}
			buf.append("},");
		}
		buf.deleteCharAt(buf.length() - 1);
		
		return buf.toString();
	}
	
	/**
	 * Computes the binary variant of this canonical form. This representation
	 * is smaller than the string variant produced by {@link #toStringCanon()},
	 * but can optionally be encoded in Base64 using {@link #toBase64Canon()}.
	 * @return The binary form of this canonical form.
	 */
	public byte[] toBinaryCanon(){
		//bits per vertex
		int vb = (int)Math.ceil(Math.log(graph.nv) / Math.log(2));
		
		//total required bits
		int bits = MAX_VERTEX_BITS + vb * 2 + labels.size() * MAX_LABEL_BITS + MAX_LABEL_BITS + vb * labels.size();
		
		bits += graph.nv * vb;
		bits += graph.nde * vb;
				
		//write canonical form
		BitWriter out = new BitWriter(bits);
		out.writeInt(graph.nv, MAX_VERTEX_BITS);
		out.writeInt(source, vb);
		out.writeInt(target, vb);
		
		out.writeInt(labels.size(), MAX_LABEL_BITS);
		for(Entry<Predicate, Integer> entry : labels.entrySet()){
			out.writeInt(entry.getKey().getID(), MAX_LABEL_BITS);
			out.writeInt(entry.getValue(), vb);
		}
		
		for(int i = 0; i < graph.nv; i++){
			int deg = graph.d[i];
			out.writeInt(deg, vb);
			int voff = graph.v[i];
			for(int v = 0; v < deg; v++){
				out.writeInt(graph.e[voff + v], vb);
			}
		}
		
		return out.getData();
	}
	
	/**
	 * Computes the Base64 encoded string of {@link #toBinaryCanon()}.
	 * @return The Base64 encoded version of the binary canonical form.
	 */
	public String toBase64Canon(){
		return Base64.getEncoder().encodeToString(toBinaryCanon());
	}
	
	/**
	 * Computes a wrapped representation of {@link #toBinaryCanon()}
	 * that is more suitable for equality testing.
	 * @return The CoreHash wrapped version of the binary canonical form.
	 * @see CoreHash
	 */
	public CoreHash toHashCanon(){
		return new CoreHash(toBinaryCanon());
	}
	
	@Override
	public boolean equals(Object obj){
		return obj instanceof CanonForm && Arrays.equals(toBinaryCanon(), ((CanonForm)obj).toBinaryCanon());
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(toBinaryCanon());
	}
	
	/**
	 * A small wrapper class for binary canonical forms that
	 * caches the hash code of the canonical form.
	 * @author Roan
	 */
	public static final class CoreHash{
		/**
		 * The binary canonical form.
		 * @see CanonForm#toBinaryCanon()
		 */
		private final byte[] canon;
		/**
		 * The pre computed hash code of {@link #canon}.
		 */
		private final int hash;
		
		/**
		 * Constructs a new core hash by wrapping the given canonical form.
		 * @param canon The binary canonical form to wrap.
		 */
		private CoreHash(byte[] canon){
			this.canon = canon;
			hash = Arrays.hashCode(canon);
		}
		
		/**
		 * Writes this canonical form to the given output stream.
		 * @param out The stream to write to.
		 * @throws IOException When an IOException occurs
		 * @see #read(DataInputStream)
		 */
		public void write(DataOutputStream out) throws IOException{
			out.writeInt(canon.length);
			out.write(canon);
		}
		
		@Override
		public int hashCode(){
			return hash;
		}
		
		@Override
		public boolean equals(Object obj){
			return Arrays.equals(canon, ((CoreHash)obj).canon);
		}
		
		/**
		 * Reads a previously written CoreHash from the given input stream.
		 * @param in The stream to read from.
		 * @return The read CoreHash instance.
		 * @throws IOException When an IOException occurs
		 */
		public static final CoreHash read(DataInputStream in) throws IOException{
			byte[] data = new byte[in.readInt()];
			in.readFully(data);
			return new CoreHash(data);
		}
	}
}
