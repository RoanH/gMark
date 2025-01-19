/*
 * gMark: A domain- and query language-independent query workload generator and query language utility library.
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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
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

import dev.roanh.gmark.exception.ConfigException;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.gen.shape.QueryShape;
import dev.roanh.gmark.gen.workload.ConfigParser;
import dev.roanh.gmark.gen.workload.Configuration;
import dev.roanh.gmark.gen.workload.OutputWriter;
import dev.roanh.gmark.gen.workload.QueryGenerator;
import dev.roanh.gmark.gen.workload.Workload;
import dev.roanh.gmark.gen.workload.QueryGenerator.ProgressListener;
import dev.roanh.gmark.output.ConcreteSyntax;
import dev.roanh.gmark.query.Query;
import dev.roanh.gmark.util.Util;
import dev.roanh.gmark.query.QuerySet;
import dev.roanh.gmark.type.Selectivity;
import dev.roanh.util.Dialog;
import dev.roanh.util.FileSelector;
import dev.roanh.util.FileSelector.FileExtension;

/**
 * Panel to display information about a workload
 * as well as to facilitate generating a query workload.
 * @author Roan
 */
public class QueryTab extends JPanel{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = -7906116640679412583L;
	/**
	 * File extension for xml files.
	 */
	private static final FileExtension XML_EXT = FileSelector.registerFileExtension("Configuration Files", "xml");
	/**
	 * Executor used to run query generation tasks.
	 */
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	/**
	 * Panel showing information about the loaded gMark configuration.
	 */
	private JPanel info = new JPanel(new GridLayout(1, 0));
	/**
	 * Panel showing information about the generated query workload.
	 */
	private JPanel queries = new JPanel(new BorderLayout());
	/**
	 * Button to save the generated queries.
	 */
	private JButton save = new JButton("Save generated workload");
	/**
	 * The most recently generated set of queries.
	 */
	private QuerySet data = null;

	/**
	 * Constructs a new query tab.
	 */
	public QueryTab(){
		super(new BorderLayout());
		
		info.setBorder(BorderFactory.createTitledBorder("Workload Info"));
		info.add(new JLabel("No configuration file selected, please open one..."));
		this.add(info, BorderLayout.PAGE_START);

		queries.setBorder(BorderFactory.createTitledBorder("Queries"));
		queries.add(new JLabel("No workload generated, please generate one..."), BorderLayout.PAGE_START);
		this.add(queries, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel(new GridLayout(1, 0));
		buttons.setBorder(BorderFactory.createTitledBorder("Controls"));
		
		JButton open = new JButton("Open Configuration");
		buttons.add(open);
		open.addActionListener(e->openWorkload());
		
		save.setEnabled(false);
		buttons.add(save);
		save.addActionListener(e->saveWorkload());
		
		this.add(buttons, BorderLayout.PAGE_END);
	}
	
	/**
	 * Saves the most recently generated workload by prompting
	 * the user for a folder to save to.
	 */
	private void saveWorkload(){
		synchronized(data){
			Path folder = Dialog.showFolderOpenDialog();
			
			try{
				if(!Util.isEmpty(folder) && !Dialog.showConfirmDialog("The selected folder is not empty, some files\nmay be overwritten, do you want to continue?")){
					return;
				}
				
				OutputWriter.writeGeneratedQueries(data, folder, Arrays.asList(ConcreteSyntax.values()), true);
			}catch(IOException e){
				Dialog.showErrorDialog("Failed to save queries: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Prompts the user for a gMark configuration file to open.
	 */
	private void openWorkload(){
		Path file = Dialog.showFileOpenDialog(XML_EXT);
		if(file != null){
			try{
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
					details.add(new JLabel("Selectivity: " + wl.getSelectivities().stream().map(Selectivity::getName).reduce((a, b)->a + ", " + b).orElse("-")));
					details.add(new JLabel("Shapes: " + wl.getShapes().stream().map(QueryShape::getName).reduce((a, b)->a + ", " + b).orElse("-")));
					wlInfo.add(details, BorderLayout.CENTER);

					JButton gen = new JButton("Generate queries");
					wlInfo.add(gen, BorderLayout.PAGE_END);
					gen.addActionListener(e->genWorkload(wl));

					info.add(wlInfo);
				}
				this.revalidate();
				this.repaint();
			}catch(ConfigException e){
				Dialog.showErrorDialog("Failed to load configuration file: " + e.getMessage());
			}
		}
	}

	/**
	 * Generates a set queries for the given workload.
	 * @param wl The workload to generate queries for.
	 */
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
				
				QuerySet queryData = QueryGenerator.generateQueries(wl, listener);
				synchronized(this){
					data = queryData;
				}
				
				SwingUtilities.invokeLater(()->{
					queries.removeAll();
					JTabbedPane queryTabs = new JTabbedPane();
					for(int i = 0; i < data.size(); i++){
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
					
					save.setEnabled(true);
					queries.revalidate();
					queries.repaint();
				});
			}catch(GenerationException e){
				Dialog.showErrorDialog("Failed to generate queries: " + e.getMessage());
			}
		});
	}
}
