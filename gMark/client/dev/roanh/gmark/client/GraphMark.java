package dev.roanh.gmark.client;

import java.nio.file.Paths;

import javax.swing.JFrame;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.client.component.GraphPanel;
import dev.roanh.gmark.core.graph.Configuration;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.SchemaGraph;
import dev.roanh.gmark.util.SelectivityType;

public class GraphMark{
	private static final JFrame frame = new JFrame("gMark");

	public static void main(String[] args){
		Configuration config = ConfigParser.parse(Paths.get("C:\\Users\\RoanH\\Downloads\\tmp\\gmark\\use-cases\\test.xml"));
		SchemaGraph gs = new SchemaGraph(config.getSchema());

		frame.add(new GraphPanel<SelectivityType, Predicate>(gs, SelectivityType::toString, Predicate::getAlias));
		
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
