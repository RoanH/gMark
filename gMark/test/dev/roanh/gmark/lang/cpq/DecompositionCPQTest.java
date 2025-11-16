package dev.roanh.gmark.lang.cpq;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.lang.cpq.DecompositionCPQ.Vertex;
import dev.roanh.gmark.lang.cq.CQ;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphNode;

public class DecompositionCPQTest{
	
	@Test
	public void trivial(){
		CQ cq = CQ.empty();
		cq.addFreeVariable("s");
		cq.addFreeVariable("t");
		
		
	}
	
	@Test
	public void decompose0(){
		UniqueGraph<Vertex, Integer> graph = new UniqueGraph<Vertex, Integer>();

		GraphNode<Vertex, Integer> v1 = graph.addUniqueNode(new Vertex("1", false));
		GraphNode<Vertex, Integer> v2 = graph.addUniqueNode(new Vertex("2", false));
		GraphNode<Vertex, Integer> v3 = graph.addUniqueNode(new Vertex("3", false));
		GraphNode<Vertex, Integer> v4 = graph.addUniqueNode(new Vertex("4", false));
		GraphNode<Vertex, Integer> v5 = graph.addUniqueNode(new Vertex("5", true));
		GraphNode<Vertex, Integer> v6 = graph.addUniqueNode(new Vertex("6", true));
		GraphNode<Vertex, Integer> v7 = graph.addUniqueNode(new Vertex("7", true));
		GraphNode<Vertex, Integer> v8 = graph.addUniqueNode(new Vertex("8", true));
		GraphNode<Vertex, Integer> v9 = graph.addUniqueNode(new Vertex("9", false));
		GraphNode<Vertex, Integer> v10 = graph.addUniqueNode(new Vertex("10", false));
		GraphNode<Vertex, Integer> v11 = graph.addUniqueNode(new Vertex("11", false));
		GraphNode<Vertex, Integer> v12 = graph.addUniqueNode(new Vertex("12", false));
		GraphNode<Vertex, Integer> v13 = graph.addUniqueNode(new Vertex("13", false));
		GraphNode<Vertex, Integer> v14 = graph.addUniqueNode(new Vertex("14", false));
		GraphNode<Vertex, Integer> v15 = graph.addUniqueNode(new Vertex("15", false));
		GraphNode<Vertex, Integer> v16 = graph.addUniqueNode(new Vertex("16", false));
		GraphNode<Vertex, Integer> v17 = graph.addUniqueNode(new Vertex("17", false));
		
		v1.addUniqueEdgeTo(v5);
		v1.addUniqueEdgeTo(v6);
		v1.addUniqueEdgeTo(v4);
		v1.addUniqueEdgeTo(v2);
		v1.addUniqueEdgeTo(v10);
		v2.addUniqueEdgeTo(v3);
		v2.addUniqueEdgeTo(v9);
		v3.addUniqueEdgeTo(v8);
		v4.addUniqueEdgeTo(v7);
		v5.addUniqueEdgeTo(v14);
		v6.addUniqueEdgeTo(v15);
		v7.addUniqueEdgeTo(v15);
		v8.addUniqueEdgeTo(v16);
		v8.addUniqueEdgeTo(v17);
		v9.addUniqueEdgeTo(v10);
		v10.addUniqueEdgeTo(v12);
		v10.addUniqueEdgeTo(v13);
		v11.addUniqueEdgeTo(v12);
		v12.addUniqueEdgeTo(v11);
		v12.addUniqueEdgeTo(v13);
		v13.addUniqueEdgeTo(v15);
		v14.addUniqueEdgeTo(v15);
		v15.addUniqueEdgeTo(v16);
		v16.addUniqueEdgeTo(v17);
		
		DecompositionCPQ.decompose(graph);
	}

	@Test
	public void decompose1(){
		UniqueGraph<String, Integer> graph = new UniqueGraph<String, Integer>();

		graph.addUniqueNode("1s");
		graph.addUniqueNode("2");
		graph.addUniqueNode("3");
		graph.addUniqueNode("4");
		graph.addUniqueNode("5");
		graph.addUniqueNode("6");
		graph.addUniqueNode("7");
		graph.addUniqueNode("8");
		graph.addUniqueNode("9");
		graph.addUniqueNode("10");
		graph.addUniqueNode("11");
		graph.addUniqueNode("12");
		graph.addUniqueNode("13");
		graph.addUniqueNode("14");
		graph.addUniqueNode("15t");

		graph.addUniqueEdge("1s", "2", 1);
		graph.addUniqueEdge("1s", "3", 1);
		graph.addUniqueEdge("1s", "4", 1);
		graph.addUniqueEdge("2", "8", 1);
		graph.addUniqueEdge("2", "9", 1);
		graph.addUniqueEdge("3", "11", 1);
		graph.addUniqueEdge("4", "5", 1);
		graph.addUniqueEdge("4", "5", 2);
		graph.addUniqueEdge("5", "15", 1);
		graph.addUniqueEdge("6", "7", 1);
		graph.addUniqueEdge("6", "8", 1);
		graph.addUniqueEdge("7", "8", 1);
		graph.addUniqueEdge("8", "10", 1);
		graph.addUniqueEdge("9", "10", 1);
		graph.addUniqueEdge("10", "15t", 1);
		graph.addUniqueEdge("11", "12", 1);
		graph.addUniqueEdge("11", "14", 1);
		graph.addUniqueEdge("11", "15t", 1);
		graph.addUniqueEdge("12", "13", 1);
		graph.addUniqueEdge("12", "14", 1);
		graph.addUniqueEdge("13", "13", 1);
		graph.addUniqueEdge("13", "14", 1);

		//       6 --- 7
		//        \   /
		//         \ /
		//      --- 8 ---
		//     /         \
		//    2 --- 9 --- 10
		//   /|             \
		//  / |              \
		// 1s |              /15t--\
		// |\ |             /      |
		// | \3 --------- 11       |
		// |              /\       |
		// 4             /  \      |
		// |\          12 -- 14    |
		// | |           \  /      |
		// |/             \/       |
		// 5       (loop) 13       |
		// |                       |
		// \-----------------------/


		
		
		
		
		
		
		
		//TODO

	}
}
