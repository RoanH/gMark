package dev.roanh.gmark.eval;

import java.util.Comparator;
import java.util.List;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.data.SourceLabelPair;
import dev.roanh.gmark.data.TargetLabelPair;

import nl.group9.quicksilver.impl.IntGraph;

public class DatabaseGraph{
	private final int vertexCount;
	private final int[] syn1;
	protected final int[] slt;
	protected final int[] reverseSlt;
	
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
}
