package dev.roanh.gmark.client;

import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.client.component.GraphPanel;
import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Configuration;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Type;
import dev.roanh.gmark.util.ConfigGraph;
import dev.roanh.gmark.util.EdgeGraph;
import dev.roanh.gmark.util.EdgeGraphData;
import dev.roanh.gmark.util.SchemaGraph;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.SelectivityType;

public class GraphMark{
	private static final JFrame frame = new JFrame("gMark");

	public static void main(String[] args){
		Configuration config = ConfigParser.parse(Paths.get("C:\\Users\\RoanH\\Downloads\\tmp\\gmark\\use-cases\\test.xml"));
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
		SelectivityType src = SelectivityType.of(config.getTypes().get(2), SelectivityClass.EQUALS);
		//paper 1x1
		SelectivityType trg = SelectivityType.of(config.getTypes().get(1), SelectivityClass.CROSS);

		EdgeGraph eg = new EdgeGraph(gs, 4, src, trg);
		ConfigGraph cg = new ConfigGraph(config);
		SelectivityGraph sg = new SelectivityGraph(config.getSchema(), 2);
				
		eg.removeNodeIf(n->n.getInEdges().size() + n.getOutEdges().size() == 0);
		gs.removeUnreachable();

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Config Graph", new GraphPanel<Type, Predicate>(cg, Type::getAlias, Predicate::getAlias));
		tabs.addTab("Schema Graph", new GraphPanel<SelectivityType, Predicate>(gs, SelectivityType::toString, Predicate::getAlias));
		tabs.addTab("Edge Graph", new GraphPanel<EdgeGraphData, Void>(eg));
		tabs.addTab("Selectivity Graph", new GraphPanel<SelectivityType, Void>(sg));
		
		//just print a few paths randomly
		for(int i = 0; i < 10; i++){
			eg.printPath();
		}
		
		frame.add(tabs);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
