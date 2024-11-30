package dev.roanh.gmark.eval;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.data.SourceLabelPair;
import dev.roanh.gmark.data.TargetLabelPair;
import dev.roanh.gmark.util.graph.IntGraph;

/**
 * Graph specification used for the graph representing the database data.
 * <p>
 * Some properties of the database graph:
 * <ul>
 * <li>Vertices, edges and label are represented using integers. This is
 * an abstraction away from the actual data, typically done for performance
 * reasons and for this implementation the conversion was done in advance for simplicity.</li>
 * <li>The numerical vertex identifiers are consecutive, that is, if the database
 * graph has <i>V</i> vertices, then all vertices will have an identifier
 * <i>v</i> that satisfies {@code 0 <= v < V}.</li>
 * <li>The numerical label identifiers are consecutive, that is, if the database
 * graph has <i>L</i> labels, then all labels will have an identifier
 * <i>l</i> that satisfies {@code 0 <= l < L}.</li>
 * <li>In particular for real world data graphs, there is no guarantee that no
 * duplicates exist of some edges.</li>
 * </ul>
 * @author Roan
 */
public class DatabaseGraph{
	/**
	 * The total number of vertices in this database graph. Note that not all vertices
	 * need to be associated with an edge. Furthermore, vertices are identified by all the
	 * integers in the interval [0, V).
	 */
	private final int vertexCount;
	/**
	 * Synopsis of paths of length 1 with a specific label. Effectively this is a
	 * lookup map from label identifier to the total number of edges with that label
	 * in the database graph. Concretely, {@code syn1[l]} contains the count of
	 * edges in the database graph with label <code>l</code>.
	 */
	private final int[] syn1;
	/**
	 * Source-Label-Target (SLT) is a data structure that encodes all edges in the complete
	 * database graph. The general design for this data structure is heavily inspired
	 * by a compressed spare row matrix (CSR) and primarily intended to minimise CPU
	 * cache misses. The SLT is represented using a single array, meaning it is designed
	 * to be able to use a single contiguous block of memory.
	 * <p>
	 * The layout of this memory block is as follows:
	 * <ol>
	 * <li>Source Index: The start of the structure records at which offset in the SLT to
	 * find the data for a specific node. For a node with some <code>id</code> the range
	 * where information about that node is stored is between SLT index {@code SLT[id]}
	 * (inclusive) and {@code SLT[id + 1]} (exclusive). If these two indices are identical
	 * then a node does not have any outgoing edges. In this case no information is stored
	 * for the node in question.
	 * <li>Label Index: After resolving the start index for the data of a node we can read
	 * the label index for that node. Similar to the source index, the label index records
	 * for each label type where the target nodes are stored in the SLT. If no edges from
	 * the source node exist with a given label, then these two indices are again equal and
	 * no target nodes are stored.
	 * <li>Target Range: The target range is the slice of the SLT referred to by the label
	 * index which contains the node IDs of the actual targets for edges from the source
	 * with the associated label.
	 * </ol>
	 * 
	 * In general this means that if target nodes exist for some label we can find them in the
	 * following slice of the SLT: {@code SLT[SLT[SLT[source] + label]...SLT[SLT[source] + label + 1]]}.
	 * <p>
	 * A small example graph with eight nodes, eight edges, two label types and the SLT
	 * representing it can be seen in the figure below. Here the source index is shown in
	 * red, the label index in blue, the target vertices associated with label 0 in purple
	 * and the target vertices associated with label 1 in green.
	 * 
	 * <img alt="SLT Example" src="https://media.roanh.dev/gmark/SLT.svg" style="width: 100%;"/>
	 * 
	 * Assuming we want to find the data for node 4, then the data for this node is between index
	 * {@code SLT[4] = 21} (inclusive) and {@code SLT[4 + 1] = 25} (exclusive). If we then
	 * want to find the data for edges with label 0, this information is stored between index
	 * {@code SLT[SLT[4] + 0] = 24} (inclusive) and {@code SLT[SLT[4] + 0 + 1] = 25} (exclusive). 
	 */
	private final int[] slt;
	/**
	 * The reverse SLT is analogous to the regular SLT, except the source index represents
	 * target vertices and the edges are traversed in the reverse (target to source) direction.
	 * In essence this is effectively a Target-Label-Source (TLS) index instead.
	 * @see #slt SLT
	 */
	private final int[] reverseSlt;
	
	public DatabaseGraph(IntGraph graph){
		final int labelCount = graph.getLabelCount();
		this.vertexCount = graph.getVertexCount();
		syn1 = new int[labelCount];
		
		int nonEmptySources = 0;
		for(int source = 0; source < vertexCount; source++){
			if(!graph.getOutgoingEdges(source).isEmpty()){
				nonEmptySources++;
			}
		}
		
		slt = new int[vertexCount + 1 + graph.getEdgeCount() + nonEmptySources * (labelCount + 1)];
		int idx = vertexCount + 1;
		slt[0] = idx;
		for(int source = 0; source < vertexCount; source++){
			List<TargetLabelPair> out = graph.getOutgoingEdges(source);
			if(!out.isEmpty()){
				out.sort(Comparator.comparing(TargetLabelPair::label).thenComparing(TargetLabelPair::target));
				
				int labIdx = idx;
				idx += labelCount + 1;
				slt[labIdx++] = idx;
				
				int outIdx = 0;
				for(int label = 0; label < labelCount; label++){
					int lastTarget = -1;
					while(outIdx < out.size() && out.get(outIdx).label() == label){
						int target = out.get(outIdx).target();
						if(target != lastTarget){
							slt[idx++] = target;
							syn1[label]++;
							lastTarget = target;
						}
						
						outIdx++;
					}
					
					slt[labIdx++] = idx;
				}
			}
			
			slt[source + 1] = idx;
		}
		
		int nonEmptyTargets = 0;
		for(int target = 0; target < vertexCount; target++){
			if(!graph.getIncomingEdges(target).isEmpty()){
				nonEmptyTargets++;
			}
		}
		
		reverseSlt = new int[vertexCount + 1 + graph.getEdgeCount() + nonEmptyTargets * (labelCount + 1)];
		idx = vertexCount + 1;
		reverseSlt[0] = idx;
		for(int target = 0; target < vertexCount; target++){
			List<SourceLabelPair> in = graph.getIncomingEdges(target);
			if(!in.isEmpty()){
				in.sort(Comparator.comparing(SourceLabelPair::label).thenComparing(SourceLabelPair::source));
				
				int labIdx = idx;
				idx += labelCount + 1;
				reverseSlt[labIdx++] = idx;
				
				int inIdx = 0;
				for(int label = 0; label < labelCount; label++){
					int lastSource = -1;
					while(inIdx < in.size() && in.get(inIdx).label() == label){
						int source = in.get(inIdx).source();
						if(source != lastSource){
							reverseSlt[idx++] = source;
							lastSource = source;
						}
						
						inIdx++;
					}
					
					reverseSlt[labIdx++] = idx;
				}
			}
			
			reverseSlt[target + 1] = idx;
		}
	}
	
	public int getVertexCount(){
		return vertexCount;
	}
	
	public int getEdgeCount(){
		int edges = 0;
		for(int i = 0; i < syn1.length; i++){
			edges += syn1[i];
		}
		
		return edges;
	}
	
	public int getEdgeCount(Predicate label){
		return syn1[label.getID()];
	}
	
	/**
	 * Gets the number of labels in this graph.
	 * @return The number of labels in this graph.
	 */
	public int getLabelCount(){
		return syn1.length;
	}
	
	public ResultGraph selectLabel(Predicate label){
		ResultGraph out = new ResultGraph(vertexCount, getEdgeCount(label), true);
		
		final int[] data = label.isInverse() ? reverseSlt : slt;
		for(int source = 0; source < vertexCount; source++){
			out.setActiveSource(source);
			
			final int start = data[source];
			if(start != data[source + 1]){
				final int labelStart = data[start + label.getID()];
				final int labelEnd = data[start + label.getID() + 1];
				for(int i = labelStart; i < labelEnd; i++){
					out.addTarget(data[i]);
				}
			}
		}
		
		out.endFinalSource();
		return out;
	}
	
	public ResultGraph selectLabel(Predicate label, int target){
		final int[] data = label.isInverse() ? slt : reverseSlt;
		final int offset = data[target];
		
		int idx = data[offset + label.getID()];
		final int end = data[offset + label.getID() + 1];
		
		if(end - idx == 0){
			return ResultGraph.empty(vertexCount);
		}
		
		int vertex = data[idx];
		ResultGraph out = new ResultGraph(vertexCount, end - idx, true);
		for(int source = 0; source < vertexCount; source++){
			out.setActiveSource(source);
		
			if(source == vertex){
				out.addTarget(target);
				if(++idx < end){
					vertex = data[idx];
				}
			}
		}
		
		out.endFinalSource();
		return out;
	}
	
	public ResultGraph selectLabel(int source, Predicate label){
		final int[] data = label.isInverse() ? reverseSlt : slt;
		final int start = data[source];
		return ResultGraph.single(vertexCount, source, true, data[start + label.getID()], data[start + label.getID() + 1], data);
	}
	
	public ResultGraph selectLabel(int source, Predicate label, int target){
		final int[] data = label.isInverse() ? reverseSlt : slt;
		final int start = data[source];
		if(Arrays.binarySearch(data, data[start + label.getID()], data[start + label.getID() + 1], target) >= 0){
			return ResultGraph.single(vertexCount, source, target);
		}else{
			return ResultGraph.empty(vertexCount);
		}
	}
	
	/**
	 * Selects all the vertices from the this database graph. Note that vertices
	 * are selected together with themselves to form a complete source target pair.
	 * @return A result graph containing only vertices selected with themselves.
	 */
	public ResultGraph selectIdentity(){
		ResultGraph out = new ResultGraph(vertexCount, vertexCount, true);

		for(int vertex = 0; vertex < vertexCount; vertex++){
			out.setActiveSource(vertex);
			out.addTarget(vertex);
		}
		
		out.endFinalSource();
		return out;
	}
	
	public ResultGraph selectIdentity(int vertex){
		return ResultGraph.single(vertexCount, vertex, true, vertex);
	}
	
	protected int[] getData(){
		return slt;
	}
	
	protected int[] getReverseData(){
		return reverseSlt;
	}
}
