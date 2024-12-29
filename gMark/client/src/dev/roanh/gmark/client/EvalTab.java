package dev.roanh.gmark.client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dev.roanh.gmark.eval.DatabaseGraph;
import dev.roanh.gmark.eval.QueryEvaluator;
import dev.roanh.gmark.util.Util;
import dev.roanh.gmark.util.graph.IntGraph;
import dev.roanh.util.Dialog;
import dev.roanh.util.FileSelector;
import dev.roanh.util.FileSelector.FileExtension;

public class EvalTab extends JPanel{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = -3895083891700621666L;
	private static final FileExtension EDGE_EXT = FileSelector.registerFileExtension("Database Graph Files", "edge");
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	private JButton loadGraph = new JButton("Load Database Graph");
	private JLabel graphInfo = new JLabel("No graph loaded");
	private QueryEvaluator evaluator = null;

	public EvalTab(){
		super(new BorderLayout());
	
		JPanel input = new JPanel(new GridLayout(2, 1));
		
		JPanel graph = new JPanel(new GridLayout(1, 2));
		graph.add(loadGraph);
		graph.add(graphInfo);
		
		input.add(graph);
		
		this.add(input, BorderLayout.PAGE_START);
		//TODO output
	}
	
	
	
	
	
	
	
	private void loadGraph(){
		Path file = Dialog.showFileOpenDialog(EDGE_EXT);
		if(file != null){
			loadGraph.setEnabled(false);
			executor.submit(()->{
				try{
					IntGraph data = Util.readGraph(file);
					evaluator = new QueryEvaluator(data);
					
					
					
					
					
				}catch(IOException e){
					Dialog.showErrorDialog("Failed to read the database graph: " + e.getMessage());
				}finally{
					loadGraph.setEnabled(true);
				}
			});
		}
	}
}
