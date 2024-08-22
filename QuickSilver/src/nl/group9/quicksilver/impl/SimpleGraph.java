package nl.group9.quicksilver.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dev.roanh.gmark.util.RangeList;

import nl.group9.quicksilver.core.spec.Graph;
import nl.group9.quicksilver.impl.data.SourceLabelPair;
import nl.group9.quicksilver.impl.data.SourceTargetPair;
import nl.group9.quicksilver.impl.data.TargetLabelPair;

public class SimpleGraph implements Graph{
	private final int labelCount;
	private final int vertexCount;
	private final RangeList<List<TargetLabelPair>> adjacenyList;
	private final RangeList<List<SourceLabelPair>> reverseAdjacencyList;
	
	public SimpleGraph(int vertexCount, int labelCount){
		this.vertexCount = vertexCount;
		this.labelCount = labelCount;
		
		adjacenyList = new RangeList<List<TargetLabelPair>>(vertexCount, ArrayList::new);
		reverseAdjacencyList = new RangeList<List<SourceLabelPair>>(vertexCount, ArrayList::new);
	}
	
	public List<TargetLabelPair> getOutgoingEdges(int source){
		return adjacenyList.get(source);
	}
	
	public List<SourceLabelPair> getIncomingEdges(int target){
		return reverseAdjacencyList.get(target);
	}

	@Override
	public int getNoVertices(){
		return vertexCount;
	}

	@Override
	public int getNoEdges(){
		int count = 0;
		for(List<TargetLabelPair> out : adjacenyList){
			count += out.size();
		}
		
		return count;
	}

	@Override
	public int getNoDistinctEdges(){
		int count = 0;
		for(List<TargetLabelPair> out : adjacenyList){
			out.sort(Comparator.comparingInt(TargetLabelPair::target).thenComparingInt(TargetLabelPair::label));
			
			TargetLabelPair prev = null;
			for(TargetLabelPair edge : out){
				if(!edge.equals(prev)){
					count++;
					prev = edge;
				}
			}
		}
		
		return count;
	}
	
	@Override
	public int getNoLabels(){
		return labelCount;
	}

	@Override
	public void addEdge(int from, int to, int label){
		if(from >= vertexCount || to >= vertexCount || label >= labelCount){
			throw new IllegalArgumentException("Edge data out of bounds: (source=%d, target=%d, label=%d)".formatted(from, to, label));
		}
		
		adjacenyList.get(from).add(new TargetLabelPair(to, label));
		reverseAdjacencyList.get(to).add(new SourceLabelPair(from, label));
	}

	@Override
	public boolean hasEdge(int source, int target, int label){
		return adjacenyList.get(source).contains(new TargetLabelPair(target, label));
	}
	
	@Override
	public List<SourceTargetPair> getSourceTargetPairs(){
		List<SourceTargetPair> edges = new ArrayList<SourceTargetPair>();
		
		for(int i = 0; i < vertexCount; i++){
			for(TargetLabelPair edge : adjacenyList.get(i)){
				edges.add(new SourceTargetPair(i, edge.target()));
			}
		}
		
		return edges;
	}
}
