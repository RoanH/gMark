package dev.roanh.gmark.eval;

import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;

import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.core.graph.Predicate;

/**
 * Implementation of a simple reachability query evaluator. Note that for simplicity
 * and performance vertex, edge and label information is abstracted away and instead
 * associated with an integer.
 * @author Roan
 * @see <a href="https://research.roanh.dev/Graph%20Database%20&%20Query%20Evaluation%20Terminology%20v1.3.pdf">
 *      Graph Database &amp; Query Evaluation Terminology</a>
 * @see <a href="https://research.roanh.dev/Optimising%20a%20Simple%20Graph%20Database%20v1.1.pdf">
 *      Optimising a Simple Graph Database</a>
 * @see DatabaseGraph
 * @see ResultGraph
 */
public class QueryEvaluator{
	private static final int UNBOUND = -1;
	/**
	 * The main database graph.
	 */
	private final DatabaseGraph graph;
	
	public QueryEvaluator(DatabaseGraph graph){
		this.graph = graph;
	}

	/**
	 * Evaluates the given reachability path query on the database graph for
	 * this evaluator and returns the result graph.
	 * @param query The path query to evaluate.
	 * @return The query answer result graph containing the matched paths.
	 * @see PathQuery
	 * @see ResultGraph
	 */
	public ResultGraph evaluate(PathQuery query){
		return evaluate(
			query.source().orElse(UNBOUND),
			query.query().toAbstractSyntaxTree(),
			query.target().orElse(UNBOUND)
		);
	}

	/**
	 * Evaluates the given query tree (AST) bottom up.
	 * @param path The path query tree (AST) to evaluate.
	 * @return The result of evaluating the given query tree.
	 * @see QueryTree
	 */
	private ResultGraph evaluate(int source, QueryTree path, int target){
		switch(path.getOperation()){
		case CONCATENATION://TODO simple join planner?
			//return evaluate(source, path.getLeft(), UNBOUND).join(evaluate(UNBOUND, path.getRight(), target));

			return planJoin(source, path, target);
		case DISJUNCTION:
			return evaluate(source, path.getLeft(), target).union(evaluate(source, path.getRight(), target));
		case EDGE:
			return selectEdge(source, path.getPredicate(), target);
		case IDENTITY:
			return selectIdentity(source, target);
		case INTERSECTION:
			return planIntersection(source, path, target);
		case KLEENE:
			return planTransitiveClosure(source, path, target);
		}

		throw new IllegalArgumentException("Unsupported database operation.");
	}
	
	private ResultGraph planJoin(int source, QueryTree path, int target){
		if(path.getLeft().getOperation() == OperationType.CONCATENATION || path.getRight().getOperation() == OperationType.CONCATENATION){
			JoinChain prefix = flattenJoinChainLeft(source, path.getLeft());
			JoinChain suffix = flattenJoinChainRight(path.getRight(), target);
			prefix.right = suffix;
			suffix.left = prefix;
			
			//find the minimal join
			JoinChain chain = prefix.getChainStart();
			
			//compute a cost for each join and build an array
			JoinChain[] order = new JoinChain[chain.getRightJoinCount()];
			JoinChain idx = chain;
			int offset = 0;
			while(idx.right != null){
				idx.computeCostWithRight();
				if(idx.cost == 0){
					//if the out -> in intersection between any two graphs in the chain is empty, the result is also empty
					//return ResultGraph.empty(idx.data.getVertexCount());
				}
				
				order[offset++] = idx;
				idx = idx.right;
			}
			
			System.out.println("Order: " + Arrays.toString(order));
			chain.printChain();
			
			//order based on cost
			Arrays.sort(order, null);
			
			//evaluate from low to high cost
			for(JoinChain join : order){
				System.out.println("eval " + join + " as " + join.data.getEdgeCount() + " vs " + join.right.data.getEdgeCount() + " p: " + join.processed);
				//note that we can process a left node
				
				//TODO proper fix is to build an AST
				
				if(join.processed){
					join = join.right.left;
				}
				
				ResultGraph result = join.data.join(join.right.data);
				join.data = result;
				join.relinkWithNextRight();
				System.out.println("save result as " + result.getEdgeCount());
				chain.printChain();
			}
			
			//first node will be the only remaining node
			return chain.data;
			
			
//			while(chain.right != null){
//				JoinChain min = chain;
//				int minCost = min.data.getEdgeCount() * min.right.data.getEdgeCount();
//				
//				while(min.right != null){
//					min = min.right;
//					
//					//if()
//				}
//				
//				
//				//TODO see DB systems join planner ?
//				//actually maybe compute it? 
//				//left right, make a bitmask on source/target
//				//(left.outmask & right.inmask).cardinality, lowest first
//				//would account very well for hub nodes but well...
//				
//			}
			
			
//			return null;//TODO
			
		}else{
			return evaluate(source, path.getLeft(), UNBOUND).join(evaluate(UNBOUND, path.getRight(), target));
		}
	}
	
	private JoinChain flattenJoinChainLeft(int source, QueryTree path){
		if(path.getOperation() == OperationType.CONCATENATION){
			JoinChain prefix = flattenJoinChainLeft(source, path.getLeft());
			JoinChain suffix = flattenJoinChainRight(path.getRight(), UNBOUND);
			prefix.right = suffix;
			suffix.left = prefix;
			return suffix;
		}else{
			return new JoinChain(evaluate(source, path, UNBOUND), path.fragment.toString());
		}
	}
	
	private JoinChain flattenJoinChainRight(QueryTree path, int target){
		if(path.getOperation() == OperationType.CONCATENATION){
			JoinChain prefix = flattenJoinChainLeft(UNBOUND, path.getLeft());
			JoinChain suffix = flattenJoinChainRight(path.getRight(), target);
			prefix.right = suffix;
			suffix.left = prefix;
			return prefix;
		}else{
			return new JoinChain(evaluate(UNBOUND, path, target), path.fragment.toString());
		}
	}
	
	//notably identity
	private ResultGraph planIntersection(int source, QueryTree path, int target){
		if(path.getLeft().getOperation() == OperationType.IDENTITY){
			return evaluate(source, path.getRight(), target).selectIdentity();
		}else if(path.getRight().getOperation() == OperationType.IDENTITY){
			return evaluate(source, path.getLeft(), target).selectIdentity();
		}else{
			return evaluate(source, path.getLeft(), target).intersection(evaluate(source, path.getRight(), target));
		}
	}
	
	private ResultGraph planTransitiveClosure(int source, QueryTree path, int target){
		ResultGraph base = evaluate(UNBOUND, path.getLeft(), UNBOUND);
		
		if(source == UNBOUND){
			return target == UNBOUND ? base.transitiveClosure() : base.transitiveClosureTo(target);
		}else{
			return target == UNBOUND ? base.transitiveClosureFrom(source) : base.transitiveClosure(source, target);
		}
	}
	
	private ResultGraph selectIdentity(int source, int target){
		if(source == UNBOUND){
			return target == UNBOUND ? graph.selectIdentity() : graph.selectIdentity(target);
		}else{
			if(target == UNBOUND){
				return graph.selectIdentity(source);
			}else{
				return source == target ? graph.selectIdentity(source) : ResultGraph.empty(graph.getVertexCount());
			}
		}
	}
	
	private ResultGraph selectEdge(int source, Predicate label, int target){
		if(source == UNBOUND){
			return target == UNBOUND ? graph.selectLabel(label) : graph.selectLabel(label, target);
		}else{
			return target == UNBOUND ? graph.selectLabel(source, label) : graph.selectLabel(source, label, target);
		}
	}
	
	private static class JoinChain implements Comparable<JoinChain>{
		private JoinChain left;
		private JoinChain right;
		private ResultGraph data;
		private int cost;
		private String meta;//TODO remove
		private boolean processed = false;
		
		@Override
		public String toString(){
			return meta + " with " + right.meta + " cost " + cost;
		}
		
		private JoinChain(ResultGraph data, String meta){
			this.data = data;
			this.meta = meta;
		}
		
		public JoinChain getChainStart(){
			return left == null ? this : left.getChainStart();
		}
		
		public int getRightJoinCount(){
			return right == null ? 0 : 1 + right.getRightJoinCount();
		}
		
		public void computeCostWithRight(){
			//TODO
//			return data.getEdgeCount() * right.data.getEdgeCount();
//			return outIndex.
			
			BitSet overlap = data.computeInIndex(); 
			overlap.and(right.data.computeOutIndex());
			cost = overlap.cardinality();
		}
		
		public void relinkWithNextRight(){
			JoinChain next = right.right;
			if(next != null){
				next.left = this;
			}

			right.processed = true;
			right = next;
		}
		
		public void printChain(){
			JoinChain next = this;
			do{
				System.out.print(next + " -> ");
				next = next.right;
			}while(next.right != null);
			
			System.out.println();
		}
		
		@Override
		public int compareTo(JoinChain o){
			return Integer.compare(cost, o.cost);
		}
	}
}
