package dev.roanh.gmark.client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.core.QueryShape;
import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.core.Workload;
import dev.roanh.util.Dialog;
import dev.roanh.util.FileSelector;
import dev.roanh.util.FileSelector.FileExtension;

public class QueryTab extends JPanel{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = -7906116640679412583L;
	private Executor executor = Executors.newSingleThreadExecutor();
	private static final FileExtension XML_EXT = FileSelector.registerFileExtension("XML Files", "xml");
	private JPanel info = new JPanel(new GridLayout(1, 0));
	private JTabbedPane queries = new JTabbedPane();
	private Configuration config;

	public QueryTab(){
		super(new BorderLayout());
		
		info.setBorder(BorderFactory.createTitledBorder("Workload Info"));
		queries.setBorder(BorderFactory.createTitledBorder("Queries"));
		
		this.add(info, BorderLayout.PAGE_START);
		this.add(queries, BorderLayout.CENTER);
		
		JButton open = new JButton("Open Configuration");
		JButton gen = new JButton("Generate queries");
		this.add(open, BorderLayout.PAGE_END);
		open.addActionListener(e->openWorkload());
	}
	
	
	public void openWorkload(){
		Path file = Dialog.showFileOpenDialog(XML_EXT);
		if(file != null){
			config = ConfigParser.parse(file);
			info.removeAll();
			for(Workload wl : config.getWorkloads()){
				JPanel details = new JPanel(new GridLayout(0, 1));
				details.setBorder(BorderFactory.createTitledBorder(wl.getType().getName() + " Workload " + wl.getID()));
				details.add(new JLabel("Size: " + wl.getSize()));
				details.add(new JLabel("Conjuncts: " + wl.getMinConjuncts() + " ~ " + wl.getMaxConjuncts()));
				details.add(new JLabel("Arity: " + wl.getMinArity() + " ~ " + wl.getMaxArity()));
				details.add(new JLabel("Multiplicity (star probablility): " + wl.getStarProbability()));
				details.add(new JLabel("Selectivity: " + wl.getSelectivities().stream().map(Selectivity::getName).reduce((a, b)->a + ", " + b).get()));
				details.add(new JLabel("Shapes: " + wl.getShapes().stream().map(QueryShape::getName).reduce((a, b)->a + ", " + b).get()));
				info.add(details);
			}
			this.revalidate();
			this.repaint();
		}
	}
}
