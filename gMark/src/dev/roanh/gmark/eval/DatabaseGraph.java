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
 * This interface is intentionally minimal and only specifies the methods
 * strictly required for the rest of the application to work.
 * <p>
 * Some other properties of the database graph:
 * <ul>
 * <li>Vertices, edges and label are represented using integers. This is
 * an abstractation away from the actual data, typically done for performance
 * reasons and for QuickSilver this conversion was done in advance for simplicity.</li>
 * <li>The numerical vertex identifiers are consecutive, that is, if the database
 * graph has <i>V</i> vertices, then all vertices will have an identifier
 * <i>v</i> that satisfies {@code 0 <= v < V}.</li>
 * <li>The numerical label identifiers are consecutive, that is, if the database
 * graph has <i>L</i> labels, then all labels will have an identifier
 * <i>l</i> that satisfies {@code 0 <= l < L}.</li>
 * <li>In particular for real world data graph, there is no guarantee that no
 * duplicates exist of some edges.</li>
 * </ul>
 * @author Roan
 */
public class DatabaseGraph{
	private final int vertexCount;
	private final int[] syn1;
	private final int[] slt;
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
