package dev.roanh.gmark.client;

import java.awt.Color;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.client.component.GraphPanel;
import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.core.graph.Configuration;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;
import dev.roanh.gmark.query.shape.ChainGenerator;
import dev.roanh.gmark.util.ConfigGraph;
import dev.roanh.gmark.util.EdgeGraph;
import dev.roanh.gmark.util.EdgeGraphData;
import dev.roanh.gmark.util.Graph;
import dev.roanh.gmark.util.Graph.GraphNode;
import dev.roanh.gmark.util.SchemaGraph;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.SelectivityType;

public class GraphMark{
	private static final JFrame frame = new JFrame("gMark");

	public static void main(String[] args){
		Configuration config = ConfigParser.parse(Paths.get("C:\\Users\\RoanH\\Downloads\\tmp\\gmark\\use-cases\\test.xml"));
		//Configuration config = ConfigParser.parse(Paths.get("C:\\Users\\RoanH\\Downloads\\tmp\\gmark\\use-cases\\shop.xml"));
		Schema schema = config.getSchema();
		SchemaGraph gs = new SchemaGraph(config.getSchema());
		//paper 1=1
		//SelectivityType src = SelectivityType.of(config.getTypes().get(1), SelectivityClass.ONE_ONE);
		//conference 1<N
		//SelectivityType trg = SelectivityType.of(config.getTypes().get(3), SelectivityClass.ONE_N);
		//city 1<N
		//SelectivityType trg = SelectivityType.of(config.getTypes().get(4), SelectivityClass.ONE_N);
		//paper 1<N
		SelectivityType trg = SelectivityType.of(config.getTypes().get(1), SelectivityClass.ONE_N);
		//journal 1=1
		SelectivityType src = SelectivityType.of(config.getTypes().get(2), SelectivityClass.ONE_ONE);
		//paper 1x1
		//SelectivityType trg = SelectivityType.of(config.getTypes().get(1), SelectivityClass.CROSS);

		int maxLen = 1;
		
		EdgeGraph eg = new EdgeGraph(gs, maxLen, src, trg, 10);
		ConfigGraph cg = new ConfigGraph(config);
		SelectivityGraph sg = new SelectivityGraph(schema, maxLen);
		
		System.out.println(eg.drawPath());
		
//		System.out.println(sg.generateRandomPath(Selectivity.QUADRATIC, 1).get(0));
//		src = SelectivityType.of(schema.getType("Purchase"), SelectivityClass.EQUALS);
//		trg = SelectivityType.of(schema.getType("User"), SelectivityClass.CROSS);
		
		
//		try{
//			Thread.sleep(10000);
//		}catch(InterruptedException e){
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
				
		//eg.removeNodeIf(n->n.getInEdges().size() + n.getOutEdges().size() == 0);
		gs.removeUnreachable();

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Config Graph", new GraphPanel<Type, Predicate>(cg, Type::getAlias, Predicate::getAlias));
		tabs.addTab("Schema Graph", new GraphPanel<SelectivityType, Predicate>(gs, SelectivityType::toString, Predicate::getAlias));
		tabs.addTab("Edge Graph", new GraphPanel<EdgeGraphData, Void>(eg));
		tabs.addTab("Selectivity Graph", new GraphPanel<SelectivityType, SelectivityClass>(sg));
		tabs.addTab("Ref", buildRefGraph());
		
		//TODO put back -ea VM arg
		
		//just print a few paths randomly
//		long start = System.currentTimeMillis();
//		for(int i = 0; i < 10000000; i++){
//			//eg.printPath();
//			eg.drawPath();
//		}
//		System.out.println("10M in: " + (System.currentTimeMillis() - start));
		
		//GeneratorCPQ.generateInnerCPQ(sg, gs, src, trg, 4, 5);
		
//		System.out.println("\n\n\n\n\n\n\n\n\n");
		
		long start = System.currentTimeMillis();
		ChainGenerator generator = new ChainGenerator();
		for(int i = 0; i < 100000; i++){
			try{
				System.out.println(generator.generate(config, Workload.getDummyInstance()));
			}catch(Exception e){
				System.err.println("Error generating query: " + e.getMessage());
				e.printStackTrace();
			}
		}
		System.out.println("10 in: " + (System.currentTimeMillis() - start));
		
		frame.add(tabs);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	private static GraphPanel<SelectivityType, Predicate> buildRefGraph(){
		System.out.println("build ref graph");
		Predicate a = new Predicate(0, "a", 0.0D);
		Predicate b = new Predicate(1, "b", 0.0D);
		Predicate ai = a.getInverse();
		Predicate bi = b.getInverse();
		
		Type t1 = new Type(0, "T1", 0.0D);
		Type t2 = new Type(1, "T2", 0.0D);
		Type t3 = new Type(2, "T3", 0.0D);
		
		Graph<SelectivityType, Predicate> g = new Graph<SelectivityType, Predicate>();
		GraphNode<SelectivityType, Predicate> n1 = g.addUniqueNode(SelectivityType.of(t1, SelectivityClass.LESS));
		GraphNode<SelectivityType, Predicate> n2 = g.addUniqueNode(SelectivityType.of(t1, SelectivityClass.LESS_GREATER));
		GraphNode<SelectivityType, Predicate> n3 = g.addUniqueNode(SelectivityType.of(t1, SelectivityClass.EQUALS));
		GraphNode<SelectivityType, Predicate> n4 = g.addUniqueNode(SelectivityType.of(t2, SelectivityClass.EQUALS));
		GraphNode<SelectivityType, Predicate> n5 = g.addUniqueNode(SelectivityType.of(t3, SelectivityClass.N_ONE));
		GraphNode<SelectivityType, Predicate> n6 = g.addUniqueNode(SelectivityType.of(t2, SelectivityClass.CROSS));
		
		n1.addUniqueEdgeTo(n1, a);
		n1.addUniqueEdgeTo(n2, ai);
		n2.addUniqueEdgeTo(n2, ai);
		n2.addUniqueEdgeTo(n4, a);
		n3.addUniqueEdgeTo(n1, a);
		n3.addUniqueEdgeTo(n4, b);
		n4.addUniqueEdgeTo(n3, bi);
		n4.addUniqueEdgeTo(n4, b);
		n4.addUniqueEdgeTo(n5, b);
		n5.addUniqueEdgeTo(n6, bi);
		n6.addUniqueEdgeTo(n5, b);
		
		
		GraphPanel<SelectivityType, Predicate> p = new GraphPanel<SelectivityType, Predicate>(g, SelectivityType::toString, Predicate::getAlias);
		p.setBackground(Color.WHITE);
		return p;
	}
}
