/*
 * gMark: A domain- and query language-independent graph instance and query workload generator.
 * Copyright (C) 2021  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/gMark
 *
 * gMark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gMark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.gmark.client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import dev.roanh.gmark.cli.client.EvaluatorClient;
import dev.roanh.gmark.eval.DatabaseGraph;
import dev.roanh.gmark.eval.PathQuery;
import dev.roanh.gmark.eval.QueryEvaluator;
import dev.roanh.gmark.eval.ResultGraph;
import dev.roanh.gmark.lang.QueryLanguage;
import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.util.Util;
import dev.roanh.gmark.util.graph.IntGraph;
import dev.roanh.util.Dialog;
import dev.roanh.util.FileSelector;
import dev.roanh.util.FileSelector.FileExtension;

/**
 * Tab where you can evaluate queries on a database graph.
 * @author Roan
 * @see QueryEvaluator
 */
public class EvalTab extends JPanel{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = -3895083891700621666L;
	/**
	 * Extension used for database graph files.
	 */
	private static final FileExtension EDGE_EXT = FileSelector.registerFileExtension("Database Graph Files", "edge");
	/**
	 * List of special syntax symbols used in CPQ and RPQ queries.
	 */
	private static final List<String> QUERY_SYMBOLS;
	/**
	 * Background worker executor.
	 */
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	/**
	 * Button to start evaluating the input query.
	 */
	private final JButton run = new JButton("Evaluate Query");
	/**
	 * Label showing basic graph statistics.
	 */
	private final JLabel graphInfo = new JLabel("No graph loaded", SwingConstants.CENTER);
	/**
	 * Text area displaying the query output.
	 */
	private final JTextArea queryOutput = new JTextArea();
	/**
	 * The current evaluator used to evaluate queries.
	 */
	private QueryEvaluator evaluator = null;

	/**
	 * Constructs a new query evaluation tab.
	 */
	public EvalTab(){
		super(new BorderLayout());
		
		JPanel graph = new JPanel(new GridLayout(1, 2));
		graph.setBorder(BorderFactory.createTitledBorder("Database Graph"));
		JButton loadGraph = new JButton("Load Database Graph");
		loadGraph.addActionListener(this::loadGraph);
		graph.add(loadGraph);
		graph.add(graphInfo);
			
		JPanel input = new JPanel(new GridLayout(2, 1));
		input.add(graph);
		input.add(createQueryPanel(run));
		
		JPanel output = new JPanel(new BorderLayout());
		output.setBorder(BorderFactory.createTitledBorder("Query Output"));
		output.add(run, BorderLayout.PAGE_START);
		queryOutput.setEditable(false);
		output.add(new JScrollPane(queryOutput), BorderLayout.CENTER);
		
		this.add(input, BorderLayout.PAGE_START);
		this.add(output, BorderLayout.CENTER);
	}
	
	/**
	 * Creates the query input panel.
	 * @param run The query run button to configure the run listener for.
	 * @return A panel to input a query.
	 */
	private JPanel createQueryPanel(JButton run){
		JPanel sourcePanel = new JPanel(new GridLayout(2, 1));
		sourcePanel.add(new JLabel("Source (-1 for any)", SwingConstants.CENTER));
		JSpinner source = new JSpinner(new SpinnerNumberModel(-1, -1, Integer.MAX_VALUE, 1));
		sourcePanel.add(source);
		
		JPanel queryPanel = new JPanel(new GridLayout(2, 1));
		JTextField query = new JTextField();
		queryPanel.add(createQueryEditorButtons(query));
		queryPanel.add(query);

		JPanel targetPanel = new JPanel(new GridLayout(2, 1));
		targetPanel.add(new JLabel("Target (-1 for any)", SwingConstants.CENTER));
		JSpinner target = new JSpinner(new SpinnerNumberModel(-1, -1, Integer.MAX_VALUE, 1));
		targetPanel.add(target);
		
		JPanel langPanel = new JPanel(new GridLayout(2, 1));
		JComboBox<QueryLanguage> lang = new JComboBox<QueryLanguage>(QueryLanguage.values());
		langPanel.add(new JLabel("Language"));
		langPanel.add(lang);
		
		run.addActionListener(e->{
			try{
				if(evaluator == null){
					Dialog.showMessageDialog("Please load a database graph first.");
					return;
				}
				
				if(query.getText().isBlank()){
					Dialog.showMessageDialog("No query provided.");
					return;
				}

				runQuery(PathQuery.of(
					(int)source.getValue(),
					((QueryLanguage)lang.getSelectedItem()).parse(query.getText(), evaluator.getLabels()),
					(int)target.getValue()
				));
			}catch(RuntimeException e1){
				e1.printStackTrace();
				Dialog.showErrorDialog("Failed to parse query: " + e1.getMessage());
			}
		});

		JPanel line = new JPanel();
		line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
		line.setBorder(BorderFactory.createTitledBorder("Query"));
		line.add(sourcePanel);
		line.add(Box.createHorizontalStrut(2));
		line.add(queryPanel);
		line.add(Box.createHorizontalStrut(2));
		line.add(targetPanel);
		line.add(Box.createHorizontalStrut(2));
		line.add(langPanel);
		line.add(Box.createHorizontalStrut(2));
		line.add(run);
		return line;
	}
	
	/**
	 * Creates a panel with all the special symbols for query input.
	 * @param query The query text field to insert symbols in.
	 * @return The constructed panel with helper buttons.
	 */
	private JPanel createQueryEditorButtons(JTextField query){
		JPanel buttons = new JPanel(new GridLayout(1, 0));

		for(String symb : QUERY_SYMBOLS){
			JButton button = new JButton(symb);
			button.addActionListener(e->{
				try{
					query.getDocument().insertString(query.getCaretPosition(), symb, null);
					query.requestFocus();
				}catch(BadLocationException ignore){
				}
			});
			
			buttons.add(button);
		}
		
		return buttons;
	}
	
	/**
	 * Runs the given query.
	 * @param query The query to run.
	 */
	private void runQuery(PathQuery query){
		run.setEnabled(false);
		queryOutput.setText("Running query...");
		executor.execute(()->{
			try{
				long start = System.nanoTime();
				ResultGraph result = evaluator.evaluate(query);
				long time = System.nanoTime() - start;
				
				StringWriter buffer = new StringWriter();
				EvaluatorClient.printQueryResult(query, result, time, new PrintWriter(buffer, true));
				SwingUtilities.invokeLater(()->{
					queryOutput.setText(buffer.toString());
					queryOutput.setCaretPosition(0);
					run.setEnabled(true);
				});
			}catch(Exception e){
				e.printStackTrace();
				run.setEnabled(true);
				Dialog.showErrorDialog("Failed to evaluate the query: " + e.getMessage());
			}
		});
	}
	
	/**
	 * Asks the query for a graph to load in the evaluator.
	 * @param event The button click event.
	 */
	private void loadGraph(ActionEvent event){
		Path file = Dialog.showFileOpenDialog(EDGE_EXT);
		if(file != null){
			JButton button = ((JButton)event.getSource());
			button.setEnabled(false);
			executor.execute(()->{
				try{
					IntGraph data = Util.readGraph(file);
					DatabaseGraph db = new DatabaseGraph(data);
					evaluator = new QueryEvaluator(db);
					graphInfo.setText("Vertices: %d, Edges: %d (%d unique), Labels: %d".formatted(data.getVertexCount(), data.getEdgeCount(), db.getEdgeCount(), data.getLabelCount()));
				}catch(IOException e){
					Dialog.showErrorDialog("Failed to read the database graph: " + e.getMessage());
				}finally{
					button.setEnabled(true);
				}
			});
		}
	}
	
	static{
		QUERY_SYMBOLS = List.of(
			String.valueOf(QueryLanguageSyntax.CHAR_DISJUNCTION),
			String.valueOf(QueryLanguageSyntax.CHAR_INTERSECTION),
			String.valueOf(QueryLanguageSyntax.CHAR_JOIN),
			String.valueOf(QueryLanguageSyntax.CHAR_INVERSE),
			String.valueOf(QueryLanguageSyntax.CHAR_KLEENE),
			"id",
			"(",
			")"
		);
	}
}
