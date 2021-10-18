package dev.roanh.gmark.client;

import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.client.component.GraphPanel;
import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Configuration;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.EdgeGraph;
import dev.roanh.gmark.util.EdgeGraphData;
import dev.roanh.gmark.util.SchemaGraph;
import dev.roanh.gmark.util.SelectivityType;

public class GraphMark{
	private static final JFrame frame = new JFrame("gMark");

	public static void main(String[] args){
		Configuration config = ConfigParser.parse(Paths.get("C:\\Users\\RoanH\\Downloads\\tmp\\gmark\\use-cases\\test.xml"));
		SchemaGraph gs = new SchemaGraph(config.getSchema());
		SelectivityType src = SelectivityType.of(config.getTypes().get(1), SelectivityClass.ONE_ONE);
		SelectivityType trg = SelectivityType.of(config.getTypes().get(3), SelectivityClass.ONE_N);

		EdgeGraph eg = new EdgeGraph(gs, 2, src, trg);
		

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Schema Graph", new GraphPanel<SelectivityType, Predicate>(gs, SelectivityType::toString, Predicate::getAlias));
		tabs.addTab("Edge Graph", new GraphPanel<EdgeGraphData, Void>(eg));
		
		
		
		frame.add(tabs);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
