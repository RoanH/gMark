package dev.roanh.gmark.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class GraphTest{
	private static final int MAX_LEN = 6;//6
	private static final int MAX_RECURSION = 4;//4

	public static void main(String[] args){
		Graph graph = new Graph();
		for(int i = 1; i <= 18; i++){
			graph.addNode(String.valueOf(i));
		}
		
		graph.addEdge(1, 2, "a");
		graph.addEdge(1, 4, "b");
		graph.addEdge(4, 6, "c");
		graph.addEdge(4, 5, "d");
		graph.addEdge(5, 6, "y");
		graph.addEdge(6, 5, "e");
		graph.addEdge(2, 5, "f");
		graph.addEdge(2, 3, "g");
		graph.addEdge(5, 7, "h");
		graph.addEdge(3, 7, "i");
		graph.addEdge(7, 9, "j");
		graph.addEdge(6, 8, "k");
		graph.addEdge(9, 12, "l");
		graph.addEdge(8, 12, "m");
		graph.addEdge(8, 11, "n");
		graph.addEdge(11, 14, "o");
		graph.addEdge(14, 12, "p");
		graph.addEdge(10, 7, "q");
		graph.addEdge(13, 10, "r");
		graph.addEdge(16, 13, "s");
		graph.addEdge(14, 16, "t");
		graph.addEdge(14, 15, "u");
		graph.addEdge(15, 18, "v");
		graph.addEdge(18, 17, "w");
		graph.addEdge(17, 16, "x");
		
//		for(Edge e : edges){
//			System.out.println(e.source.id + " " + e.name + "-> " + e.target.id);
//		}
		
		Edge[] hist = new Edge[MAX_LEN];
		List<Edge[]> paths = new ArrayList<Edge[]>();
		printPaths("", graph.nodeMap.get("1"), graph.nodeMap.get("12"), MAX_LEN, hist, paths);
		
		for(Edge[] p : paths){
			System.out.println(Arrays.toString(p));
		}
		
		//make the edge graph
		Graph edgeGraph = new Graph();
		edgeGraph.addNode("src");
		edgeGraph.addNode("trg");
		graph.edges.forEach(e->edgeGraph.addNode(e.name));
		
		for(Edge[] p : paths){
			edgeGraph.addEdgeIfNotExists("src", p[0].name);
			for(int i = 0; i < p.length - 1; i++){
				edgeGraph.addEdgeIfNotExists(p[i].name, p[i + 1].name);
			}
			edgeGraph.addEdgeIfNotExists(p[p.length - 1].name, "trg");
		}
		
		//build up the graph
		for(int i = 0; i < MAX_RECURSION; i++){
			System.out.println("---------- " + i + " ----------");
			Set<Intersection> parallel = printParallel(edgeGraph);
			for(Intersection inter : parallel){
				String name = inter.getIntersectionString();
				Node n = edgeGraph.addNodeIfNotExists(name, inter.size());
				edgeGraph.addEdgeIfNotExists(inter.source, n);
				edgeGraph.addEdgeIfNotExists(n, inter.target);
			}
			System.out.println("Total nodes: " + edgeGraph.nodes.size() + ", total edges: " + edgeGraph.edges.size());
			System.out.println("-----------------------");
		}
		
		//draw some paths, which is rather simple as there are no cycles and all paths lead from src to trg
		System.out.println("\n\nFinal queries:");
		for(int i = 0; i < 10; i++){
			System.out.println(drawPath(edgeGraph).stream().map(Node::toString).reduce("", String::concat));
		}
	}
	
	public static List<Node> drawPath(Graph g){
		Node target = g.nodeMap.get("trg");
		Node n = g.nodeMap.get("src");
		List<Node> path = new ArrayList<Node>();
		
		do{
			n = n.out.get(ThreadLocalRandom.current().nextInt(n.out.size())).target;
			path.add(n);
		}while(n != target);
		path.remove(path.size() - 1);
		
		return path;
	}
	
	public static Set<Intersection> printParallel(Graph g){
		Node source = g.nodeMap.get("src");
		
		Deque<Node> head = new ArrayDeque<Node>();
		head.push(source);
		//store predecessor node
		Map<Node, Node> pred = new HashMap<Node, Node>();
		//mark processed nodes
		Set<Node> found = new HashSet<Node>();
		
		List<ReverseTask> reverseTasks = new ArrayList<ReverseTask>();
		
		while(!head.isEmpty()){
			Node n = head.pop();//random?
			for(Edge e : n.out){//random?
				if(!found.contains(e.target)){
					pred.put(e.target, n);
					found.add(e.target);
					head.push(e.target);
				}else{
					reverseTasks.add(()->reverseTask(n, pred, source, e));
				}
			}
		}
		
		Set<Intersection> parallel = new HashSet<Intersection>();
		//filter intersections past max length to reduce the graph size
		reverseTasks.stream().map(ReverseTask::run).filter(Objects::nonNull).forEach(parallel::add);
		parallel.forEach(System.out::println);
		
		return parallel;
	}
	
	private static Intersection reverseTask(Node n, Map<Node, Node> pred, Node source, Edge e){
		//cycle it, we basically reverse the route how we got here
		List<Node> secondPath = new ArrayList<Node>();
		{
			Node prev = n;
			do{
				secondPath.add(prev);
				prev = pred.get(prev);
			}while(prev != source);
		}
		Collections.reverse(secondPath);//since we added nodes from the end of the path first
		
		//if the current node has multiple back links then those are all 'cycles'
		for(Edge back : e.target.in){
			if(back != e){//that's the way we came...
				List<Node> firstPath = new ArrayList<Node>();
				Node prev = back.source;
				do{
					firstPath.add(prev);
					prev = pred.get(prev);//we would have to consider all predecessors to find all cycles, but this is expensive, instead we can randomise which
					                      //nodes we select to get a similar effect across a multitude of generated clauses
				}while(prev != source);
				Collections.reverse(firstPath);
				
				//it's entirely possible we went back too far and that both paths have an identical prefix
				int offset = 0;
				while(firstPath.get(offset) == secondPath.get(offset)){
					offset++;
				}
				
				//System.out.println("parallel: " + firstPath.subList(offset, firstPath.size()) + " | " + secondPath.subList(offset, secondPath.size()));
				return new Intersection(
					offset == 0 ? source : firstPath.get(offset - 1),
					e.target,
					firstPath.subList(offset, firstPath.size()),
					secondPath.subList(offset, secondPath.size())
				);
			}
		}
		throw new RuntimeException("impossible state");
	}
	
	public static void printPaths(String path, Node source, Node target, int maxLen, Edge[] hist, List<Edge[]> paths){
		if(source == target){
			System.out.println("path: " + path);
			paths.add(Arrays.copyOf(hist, hist.length - maxLen));
		}else if(maxLen == 0 || source.out.isEmpty()){
			return;
		}else{
			for(Edge edge : source.out){
				hist[hist.length - maxLen] = edge;
				printPaths(path + edge.name, edge.target, target, maxLen - 1, hist, paths);
			}
		}
	}
	
	private static final class Graph{
		private List<Edge> edges = new ArrayList<Edge>();
		private List<Node> nodes = new ArrayList<Node>();
		private Map<String, Node> nodeMap = new HashMap<String, Node>();
		
		private void addEdge(String source, String target, String name){
			addEdge(nodeMap.get(source), nodeMap.get(target), name);
		}
		
		private void addEdge(Node source, Node target, String name){
			edges.add(new Edge(source, target, name));
		}
		
		private void addEdge(int source, int target, String name){
			addEdge(String.valueOf(source), String.valueOf(target), name);
		}
		
		private void addEdgeIfNotExists(String source, String target){
			if(nodeMap.get(source).out.stream().noneMatch(e->e.target.name.equals(target))){
				addEdge(source, target, null);
			}
		}
		
		private void addEdgeIfNotExists(Node source, Node target){
			if(source.out.stream().noneMatch(e->e.target.equals(target))){
				addEdge(source, target, null);
			}
		}
		
		private Node addNode(String id, int weight){
			Node n = addNode(id);
			n.weight = weight;
			return n;
		}
		
		private Node addNode(String id){
			Node n = new Node(id);
			nodes.add(n);
			nodeMap.put(id, n);
			return n;
		}
		
		private Node addNodeIfNotExists(String id, int weight){
			Node n = nodeMap.get(id);
			return n == null ? addNode(id, weight) : n;
		}
	}
	
	private static final class Edge{
		Node source;
		Node target;
		String name;
		
		private Edge(Node source, Node target, String name){
			this.source = source;
			this.target = target;
			this.name = name;
			source.out.add(this);
			target.in.add(this);
		}
		
		@Override
		public String toString(){
			return "(" + source + "," + name + "," + target + ")";
		}
	}
	
	private static final class Node{
		String name;
		List<Edge> out = new ArrayList<Edge>();
		List<Edge> in = new ArrayList<Edge>();
		int weight = 1;
		
		private Node(String id, int weight){
			this(id);
			this.weight = weight;
		}
		
		private Node(String id){
			this.name = id;
		}
		
		@Override
		public String toString(){
			return name;
		}
	}
	
	private static final class Intersection{
		private Node source;
		private Node target;
		private List<Node> firstPath;
		private List<Node> secondPath;
		
		private Intersection(Node source, Node target, List<Node> firstPath, List<Node> secondPath){
			this.source = source;
			this.target = target;
			
			//use a consistent order on the intersection elements to avoid duplication
			if(firstPath.hashCode() > secondPath.hashCode()){
				this.firstPath = firstPath;
				this.secondPath = secondPath;
			}else{
				this.firstPath = secondPath;
				this.secondPath = firstPath;
			}
		}
		
		private int size(){
			return Math.max(firstPath.stream().mapToInt(n->n.weight).sum(), secondPath.stream().mapToInt(n->n.weight).sum());
		}
		
		private String getIntersectionString(){
			return "(" + firstPath.stream().map(n->n.name).reduce("", String::concat) + " ∩ " + secondPath.stream().map(n->n.name).reduce("", String::concat) + ")";
		}
		
		@Override
		public String toString(){
			return "[" + size() + "] " + source.name + getIntersectionString() + target;
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(source, target, (firstPath.hashCode() + secondPath.hashCode()));
		}
		
		@Override
		public boolean equals(Object other){
			if(other instanceof Intersection){
				Intersection obj = (Intersection)other;
				return source.equals(obj.source) && target.equals(obj.target) && firstPath.equals(obj.firstPath) && secondPath.equals(obj.secondPath);
			}else{
				return false;
			}
		}
	}
	
	@FunctionalInterface
	public static abstract interface ReverseTask{
		
		public abstract Intersection run();
	}
}