package dev.roanh.gmark.client;

import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import dev.roanh.gmark.Main;
import dev.roanh.gmark.client.component.GraphPanel;
import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Type;
import dev.roanh.gmark.util.Graph;
import dev.roanh.gmark.util.Graph.GraphNode;
import dev.roanh.util.Dialog;
import dev.roanh.util.Util;
import dev.roanh.gmark.util.SelectivityType;

/**
 * Main class for the GUI version of the application.
 * @author Roan
 * @see Main
 */
public class GraphMark{
	/**
	 * The main GUI frame.
	 */
	private static final JFrame frame = new JFrame("gMark");

	/**
	 * Launches the gMark GUI if not command line
	 * arguments are passed. Otherwise switches
	 * over to the CLI version of gMark.
	 * @param args Command line arguments, if present
	 *        then gMark will switch to command line mode.
	 */
	public static void main(String[] args){
		if(args.length != 0){
			Main.main(args);
			return;
		}
		
		Util.installUI();
		Dialog.setDialogTitle("gMark");
		Dialog.setParentFrame(frame);
		
		JTabbedPane tabs = new JTabbedPane();
//		tabs.addTab("Ref", buildRefGraph());
//		tabs.addTab("Sel Ref", buildSelRefGraph());
		tabs.addTab("Query Generation", new QueryTab());
		tabs.addTab("Example", new ExampleTab());
//		tabs.addTab("Social Network", buildRefGraph3());
		
		frame.add(tabs);
		frame.setSize(1000, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	/**
	 * Report related figures.
	 * @return A graph.
	 */
	@Deprecated
	private static GraphPanel<String, String> buildRefGraph3(){
		Graph<String, String> g = new Graph<String, String>();
		
		GraphNode<String, String> alice = g.addUniqueNode("Alice");
		GraphNode<String, String> bob = g.addUniqueNode("Bob");
		GraphNode<String, String> charlie = g.addUniqueNode("Charlie");
		
		g.addUniqueEdge("Alice", "Bob", "knows");
		g.addUniqueEdge("Bob", "Alice", "knows");
		g.addUniqueEdge("Alice", "Charlie", "knows");
		
		GraphPanel<String, String> p = new GraphPanel<String, String>(g);
		p.setBackground(Color.WHITE);
		return p;
	}
	
	/**
	 * Report related figures.
	 * @return A graph.
	 */
	@Deprecated
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
	
	/**
	 * Report related figures.
	 * @return A graph.
	 */
	@Deprecated
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
