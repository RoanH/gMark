package dev.roanh.gmark.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.core.QueryShape;
import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Query;
import dev.roanh.gmark.query.QueryGenerator;
import dev.roanh.gmark.query.QueryGenerator.ProgressListener;
import dev.roanh.gmark.query.QuerySet;
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
	private JPanel queries = new JPanel(new BorderLayout());

	public QueryTab(){
		super(new BorderLayout());
		
		info.setBorder(BorderFactory.createTitledBorder("Workload Info"));
		queries.setBorder(BorderFactory.createTitledBorder("Queries"));
		
		this.add(info, BorderLayout.PAGE_START);
		this.add(queries, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel(new GridLayout(1, 0));
		
		JButton open = new JButton("Open Configuration");
		buttons.add(open, BorderLayout.PAGE_END);
		open.addActionListener(e->openWorkload());
		
		
		
		this.add(buttons, BorderLayout.PAGE_END);
	}
	
	private void openWorkload(){
		Path file = Dialog.showFileOpenDialog(XML_EXT);
		if(file != null){
			Configuration config = ConfigParser.parse(file);
			info.removeAll();
			for(Workload wl : config.getWorkloads()){
				JPanel wlInfo = new JPanel(new BorderLayout());
				wlInfo.setBorder(BorderFactory.createTitledBorder(wl.getType().getName() + " Workload " + wl.getID()));
				
				JPanel details = new JPanel(new GridLayout(0, 1));
				details.add(new JLabel("Size: " + wl.getSize()));
				details.add(new JLabel("Conjuncts: " + wl.getMinConjuncts() + " - " + wl.getMaxConjuncts()));
				details.add(new JLabel("Arity: " + wl.getMinArity() + " - " + wl.getMaxArity()));
				details.add(new JLabel("Multiplicity (star probablility): " + wl.getStarProbability()));
				details.add(new JLabel("Selectivity: " + wl.getSelectivities().stream().map(Selectivity::getName).reduce((a, b)->a + ", " + b).get()));
				details.add(new JLabel("Shapes: " + wl.getShapes().stream().map(QueryShape::getName).reduce((a, b)->a + ", " + b).get()));
				wlInfo.add(details, BorderLayout.CENTER);
				
				JButton gen = new JButton("Generate queries");
				wlInfo.add(gen, BorderLayout.PAGE_END);
				gen.addActionListener(e->genWorkload(wl));
				
				info.add(wlInfo);
			}
			this.revalidate();
			this.repaint();
		}
	}
	
	private void genWorkload(Workload wl){
		executor.execute(()->{
			try{
				JProgressBar progress = new JProgressBar(0, wl.getSize());
				ProgressListener listener = (done, total)->{
					progress.setValue(done);
					progress.repaint();
				};
				
				SwingUtilities.invokeLater(()->{
					queries.removeAll();
					JPanel content = new JPanel(new FlowLayout(FlowLayout.CENTER));
					content.add(progress);
					queries.add(content, BorderLayout.CENTER);
					queries.revalidate();
					queries.repaint();
				});
				
				QuerySet data = QueryGenerator.generateQueries(wl, listener);
				
				SwingUtilities.invokeLater(()->{
					queries.removeAll();
					JTabbedPane queryTabs = new JTabbedPane();
					for(int i = 0; i < data.getSize(); i++){
						Query query = data.get(i);

						JTextArea sql = new JTextArea(query.toSQL());
						sql.setLineWrap(true);
						sql.setEditable(false);
						sql.setBackground(this.getBackground());

						JTextArea rule = new JTextArea(query.toString());
						rule.setLineWrap(true);
						rule.setEditable(false);
						rule.setBackground(this.getBackground());

						JPanel queryTab = new JPanel(new BorderLayout());
						queryTab.add(rule, BorderLayout.PAGE_START);
						queryTab.add(new JScrollPane(sql), BorderLayout.CENTER);

						queryTabs.addTab("Query " + i, queryTab);
					}
					
					JPanel details = new JPanel(new GridLayout(0, 1));
					details.add(new JLabel("Generation Time: " + (data.getGenerationTime() / 1000) + "ms"));
					details.add(new JLabel("Arity: " + data.getMinArity() + " - " + data.getMaxArity() + " (" + data.getBinaryQueryCount() + " queries are binary)"));
					details.add(new JLabel("Number of conjuncts: " + data.getMinConjuncts() + " - " + data.getMaxConjuncts()));
					
					StringBuilder buffer = new StringBuilder("Shapes: ");
					for(QueryShape shape : QueryShape.values()){
						buffer.append(shape.getName());
						buffer.append(String.format(" (%.0f%%)", 100.0D * data.getShapeFraction(shape)));
						buffer.append(", ");
					}
					buffer.delete(buffer.length() - 2, buffer.length());
					details.add(new JLabel(buffer.toString()));
					
					buffer = new StringBuilder("Selectivities: ");
					for(Selectivity sel : Selectivity.values()){
						buffer.append(sel.getName());
						buffer.append(String.format(" (%.0f%%)", 100.0D * data.getSelectivityFraction(sel)));
						buffer.append(", ");
					}
					buffer.delete(buffer.length() - 2, buffer.length());
					details.add(new JLabel(buffer.toString()));
					
					queries.add(details, BorderLayout.PAGE_START);
					queries.add(queryTabs, BorderLayout.CENTER);
					
					queries.revalidate();
					queries.repaint();
				});
			}catch(GenerationException e){
				// TODO Auto-generated catch block -- report
				e.printStackTrace();
			}
		});
	}
}
