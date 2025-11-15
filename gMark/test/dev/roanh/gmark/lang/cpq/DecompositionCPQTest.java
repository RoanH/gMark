package dev.roanh.gmark.lang.cpq;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.lang.cq.CQ;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;

public class DecompositionCPQTest{
	
	@Test
	public void trivial(){
		CQ cq = CQ.empty();
		cq.addFreeVariable("s");
		cq.addFreeVariable("t");
		
		
	}

	@Test
	public void decompose0(){
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
