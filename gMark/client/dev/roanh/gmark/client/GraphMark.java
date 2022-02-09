package dev.roanh.gmark.client;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.client.component.GraphPanel;
import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.util.ConfigGraph;
import dev.roanh.gmark.util.EdgeGraph;
import dev.roanh.gmark.util.EdgeGraphData;
import dev.roanh.gmark.util.Graph;
import dev.roanh.gmark.util.Graph.GraphNode;
import dev.roanh.util.Util;
import dev.roanh.gmark.util.SchemaGraph;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.SelectivityType;

public class GraphMark{
	private static final JFrame frame = new JFrame("gMark");

	public static void main(String[] args) throws GenerationException{
		Util.installUI();
		
		Configuration config = ConfigParser.parse(Paths.get("./test/test.xml"));
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
		//SelectivityType trg = SelectivityType.of(config.getTypes().get(1), SelectivityClass.ONE_N);
		//journal 1=1
		//SelectivityType src = SelectivityType.of(config.getTypes().get(2), SelectivityClass.ONE_ONE);
		//journal N=N
		SelectivityType src = SelectivityType.of(config.getTypes().get(2), SelectivityClass.EQUALS);
		//paper 1x1
		//SelectivityType trg = SelectivityType.of(config.getTypes().get(1), SelectivityClass.CROSS);
		//paper NxN
		SelectivityType trg = SelectivityType.of(config.getTypes().get(1), SelectivityClass.CROSS);

		int maxLen = 3;
		
		EdgeGraph eg = new EdgeGraph(gs, maxLen, src, trg, 10);
		ConfigGraph cg = new ConfigGraph(config);
		SelectivityGraph sg = new SelectivityGraph(schema, maxLen);
		gs.removeUnreachable();

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Config Graph", new GraphPanel<Type, Predicate>(cg, Type::getAlias, Predicate::getAlias));
		tabs.addTab("Schema Graph", new GraphPanel<SelectivityType, Predicate>(gs, SelectivityType::toString, Predicate::getAlias));
		tabs.addTab("Edge Graph", new GraphPanel<EdgeGraphData, Void>(eg));
		tabs.addTab("Selectivity Graph", new GraphPanel<SelectivityType, SelectivityClass>(sg));
		tabs.addTab("Ref", buildRefGraph());
		tabs.addTab("Sel Ref", buildSelRefGraph());
		tabs.addTab("Query", new QueryTab());
		
		frame.add(tabs);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
//		triplesToSQL(
//			Paths.get("C:\\Users\\RoanH\\Downloads\\Capita Selecta\\gmark\\play-graph.txt0.txt"),
//			Paths.get("C:\\Users\\RoanH\\Downloads\\Capita Selecta\\gmark\\test10k.sql")
//		);
	}
	
	private static void triplesToSQL(Path triples, Path target) throws IOException{
		if(Files.notExists(target)){
			try(PrintWriter out = new PrintWriter(Files.newBufferedWriter(target))){
				out.print("INSERT INTO edge VALUES ");
				boolean first = true;
				for(String line : Files.readAllLines(triples)){
					if(!first){
						out.print(", ");
					}else{
						first = false;
					}
					String[] args = line.split(" ");
					out.print("(" + args[0] + ", " + args[1] + ", " + args[2] + ")");
				}
				out.println(";");
			}
		}
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
	
	private static GraphPanel<SelectivityType, Void> buildSelRefGraph(){
		System.out.println("build sel ref graph");
		
		Type t1 = new Type(0, "T1", 0.0D);
		Type t2 = new Type(1, "T2", 0.0D);
		Type t3 = new Type(2, "T3", 0.0D);
		
		Graph<SelectivityType, Void> g = new Graph<SelectivityType, Void>();
		GraphNode<SelectivityType, Void> n1 = g.addUniqueNode(SelectivityType.of(t1, SelectivityClass.EQUALS));
		GraphNode<SelectivityType, Void> n2 = g.addUniqueNode(SelectivityType.of(t2, SelectivityClass.EQUALS));
		GraphNode<SelectivityType, Void> n3 = g.addUniqueNode(SelectivityType.of(t3, SelectivityClass.GREATER));
		GraphNode<SelectivityType, Void> n4 = g.addUniqueNode(SelectivityType.of(t2, SelectivityClass.CROSS));
		
		n1.addUniqueEdgeTo(n1);
		n2.addUniqueEdgeTo(n2);
		n3.addUniqueEdgeTo(n3);
		n4.addUniqueEdgeTo(n4);
		
		n1.addUniqueEdgeTo(n2);
		n1.addUniqueEdgeTo(n3);
		n1.addUniqueEdgeTo(n4);
		
		n2.addUniqueEdgeTo(n1);
		n2.addUniqueEdgeTo(n4);
		n2.addUniqueEdgeTo(n3);
		
		n3.addUniqueEdgeTo(n4);
		
		n4.addUniqueEdgeTo(n3);
		
		GraphPanel<SelectivityType, Void> p = new GraphPanel<SelectivityType, Void>(g);
		p.setBackground(Color.WHITE);
		return p;
	}
}
