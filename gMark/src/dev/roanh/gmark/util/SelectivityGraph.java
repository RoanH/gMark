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

public class SelectivityGraph extends Graph<SelectivityType, Void>{
	//TODO, move to constructor if not used anywhere else
	//gmark: graph.neighbors.size()
	private final RangeList<Map<SelectivityClass, GraphNode<SelectivityType, Void>>> index;
	private final Schema schema;
	
	
	public SelectivityGraph(Schema schema, int maxLength){
		this.schema = schema;
		index = new RangeList<Map<SelectivityClass, GraphNode<SelectivityType, Void>>>(schema.getTypeCount(), Util.selectivityMapSupplier());
		
		//compute distance between types (matrix)
		//compute graph from matrix
		
		DistanceMatrix matrix = computeDistanceMatrix(schema);
	
		//TODO in the original codebase this creates links (t1,s1) --s2-> (t2,s1*s2) which would imply this graph is labelled?
		//--> looks like these labels are used when drawing paths
		for(Type t1 : schema.getTypes()){
			for(Type t2 : schema.getTypes()){
				for(SelectivityClass sel1 : SelectivityClass.values()){
					for(SelectivityClass sel2 : SelectivityClass.values()){
						//if there exists a path of valid length from t1 to t2 with result selectivity class sel2
						if(matrix.get(t1, t2).getOrDefault(sel2, Integer.MAX_VALUE) <= maxLength){
							//add graph edge (t1, sel1) -> (t2, sel1 * sel2)
							resolve(t1, sel1).addUniqueEdgeTo(resolve(t2, sel1.conjunction(sel2)));//TODO see todo higher up about labelling
						}
					}
				}
			}
		}
	
	}
	
	private GraphNode<SelectivityType, Void> resolve(Type type, SelectivityClass sel){
		return index.get(type).computeIfAbsent(sel, k->addUniqueNode(new SelectivityType(type, sel)));
	}
	
	
	
	public void generateRandomPath(Selectivity selectivity, int length){
		generateRandomPath(selectivity, length, -1.0D);
	}
	
	//sel graph, matrix_of_paths, first_node (always -1?), len, star, path (return value)
	//generate_random_path(g, pathmat, -1, nb_conjs, wconf.multiplicity, path);
	//implementation assumes first node is always -1 (aka ommitted)
	public void generateRandomPath(Selectivity selectivity, int length, double star){//multiplicity - double / star fraction of stars 0~1
		DistanceMatrix matrix = computeNumberOfPaths(selectivity, length);
		
		int currentNode = 0;
		SelectivityClass currentSel = SelectivityClass.EQUALS;
		
		//TODO alternative never used branch in gmark here
		{
			int m = index.size();
			int paths = 0;
			for(int i = 0; i < m; i++){
				paths += matrix.get(length, i).getOrDefault(SelectivityClass.EQUALS, 0);
			}

			if(paths == 0){
				//TODO reconsider this behaviour
				throw new IllegalStateException("Failed to generate a random path");
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
		
		if(!matrix.get(length, currentNode).containsKey(currentSel)){
			//TODO reconsider this behaviour
			throw new IllegalStateException("Failed to generate a random path");
		}
		
		//TODO there is some has star logic in gmark, this does nothing so it was not copied
		List<PathSegment> path = new ArrayList<PathSegment>(length);
		for(int i = length; i > 0; i--){
			
			//TODO for some paths, e.g. CPQ's we never want a star so this test is redundant and expensive
			//self loop test
			boolean test = false;
			for(GraphEdge<SelectivityType, Void> edge : index.get(currentNode).get(SelectivityClass.EQUALS).getOutEdges()){
				SelectivityType target = edge.getTarget();
				if(target.getSelectivity() == SelectivityClass.EQUALS && target.getType().getID() == currentNode){
					test = true;
					break;
				}
			}
			
			if(test && Util.getRandom().nextDouble() <= star){
				//TODO could also pushback entire graph edges -- would miss star status
				path.add(new PathSegment(schema.getType(currentNode), SelectivityClass.EQUALS, schema.getType(currentNode), true));
			}else{
				int previousNode = currentNode;
				SelectivityClass sel;
				
				int rnd = Util.uniformRandom(1, matrix.get(i, currentNode).get(currentSel));
				int acc = 0;
				for(GraphEdge<SelectivityType, Void> edge : index.get(currentNode).get(currentSel).getOutEdges()){
					SelectivityType target = edge.getTarget();
					acc += matrix.get(i - 1, target.getType()).get(target.getSelectivity());
					if(acc >= rnd){
						currentNode = target.getTypeID();
						currentSel = target.getSelectivity();
						//sel = 
					}
				}
			}
		}
		
		
	}
	
	private DistanceMatrix computeNumberOfPaths(Selectivity selectivity, int length){
		//TODO number of columns should be the type count, should find a better way to pass that maybe
		int types = index.size();
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
					GraphNode<SelectivityType, Void> node = index.get(j).get(sel);
					if(node != null && node.getOutCount() > 0){
						for(GraphEdge<SelectivityType, Void> edge : node.getOutEdges()){
							SelectivityType target = edge.getTarget();
							matrix.get(i, j).merge(
								sel,
								matrix.get(i - 1, target.getType()).get(target.getSelectivity()),
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
	private static DistanceMatrix computeDistanceMatrix(Schema schema){
		int size = schema.getTypeCount();
		int logSize = (int)Math.log(size) + 4;//TODO why does this use a natural log?
		
		DistanceMatrix matrix = new DistanceMatrix(size);
		DistanceMatrix tmp = new DistanceMatrix(size);
		
		//add all length 1 paths
		for(Edge edge : schema.getEdges()){
			SelectivityClass sel = edge.getSelectivty();
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
	
	private static final class DistanceMatrix extends RangeMatrix<Map<SelectivityClass, Integer>>{
		
		private DistanceMatrix(int size){
			this(size, size);
		}
		
		private DistanceMatrix(int rows, int cols){
			super(rows, cols, Util.selectivityMapSupplier());
		}
		
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
