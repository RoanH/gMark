package dev.roanh.gmark.client;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.client.component.GraphPanel;
import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Type;
import dev.roanh.gmark.exception.ConfigException;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.util.ConfigGraph;
import dev.roanh.gmark.util.EdgeGraph;
import dev.roanh.gmark.util.EdgeGraphData;
import dev.roanh.gmark.util.SchemaGraph;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.SelectivityType;

/**
 * Example tab that shows some of the important structures in gMark.
 * @author Roan
 * @see ConfigGraph
 * @see SchemaGraph
 * @see SelectivityGraph
 * @see EdgeGraph
 */
public class ExampleTab extends JPanel{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = 8727642118457294081L;

	/**
	 * Constructs a new example tab.
	 */
	public ExampleTab(){
		super(new BorderLayout());
		this.setBorder(BorderFactory.createTitledBorder("Example"));

		try{
			Configuration config = ConfigParser.parse(ClassLoader.getSystemResourceAsStream("example.xml"));
			SchemaGraph gs = new SchemaGraph(config.getSchema());
			EdgeGraph eg = new EdgeGraph(gs, 3, SelectivityType.of(config.getTypes().get(2), SelectivityClass.EQUALS), SelectivityType.of(config.getTypes().get(1), SelectivityClass.CROSS), 2);
			eg.removeNodeIf(n->n.getInEdges().size() + n.getOutEdges().size() == 0);
			gs.removeUnreachable();

			JTabbedPane tabs = new JTabbedPane();
			tabs.addTab("Config Graph", new GraphPanel<Type, Predicate>(new ConfigGraph(config), Type::getAlias, Predicate::getAlias));
			tabs.addTab("Schema Graph", new GraphPanel<SelectivityType, Predicate>(gs, SelectivityType::toString, Predicate::getAlias));
			tabs.addTab("Selectivity Graph", new GraphPanel<SelectivityType, SelectivityClass>(new SelectivityGraph(config.getSchema(), 3), SelectivityType::toString, c->""));
			tabs.addTab("Edge Graph", new GraphPanel<EdgeGraphData, Void>(eg));

			this.add(new JLabel("On this tab we visually display some of the core structures used in gmark. You can drag graph nodes around with the mouse."), BorderLayout.PAGE_START);
			this.add(tabs, BorderLayout.CENTER);
		}catch(ConfigException | GenerationException e){
			//cannot happen, this resource is internal and correct
		}
	}
}
