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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.apache.commons.cli.HelpFormatter;

import dev.roanh.gmark.cli.Main;
import dev.roanh.gmark.cli.client.EvaluatorClient;
import dev.roanh.gmark.cli.client.WorkloadClient;

/**
 * Tab showing command line usage and an example configuration.
 * @author Roan
 */
public class UsageTab extends JPanel{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = 7240133091326502006L;

	/**
	 * Constructs a new usage tab.
	 */
	public UsageTab(){
		super(new BorderLayout());
		this.setBorder(BorderFactory.createTitledBorder("Usage"));
		
		//command line arguments
		JPanel cli = new JPanel(new BorderLayout());
		JTextArea usage = new JTextArea();
		StringWriter writer = new StringWriter();
		//TODO eval usage and graph gen usage, also add a tab in teh GUI for eval?
		//new HelpFormatter().printHelp(new PrintWriter(writer), 100, "gmark evaluate", "", EvaluatorClient.options, 1, 3, "", true);
		//new HelpFormatter().printHelp(new PrintWriter(writer), 100, "gmark workload", "", WorkloadClient.options, 1, 3, "", true);
		usage.setText(writer.toString());
		usage.setEditable(false);
		cli.add(new JScrollPane(usage), BorderLayout.CENTER);
		
		//example gmark config
		JPanel config = new JPanel(new BorderLayout());
		JTextArea xml = new JTextArea();
		try(BufferedReader example = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("example.xml"), StandardCharsets.UTF_8))){
			xml.setText(example.lines().collect(Collectors.joining("\n")));
		}catch(IOException ignore){
			xml.setText("Failed to load example.");
		}
		xml.setEditable(false);
		config.add(new JScrollPane(xml), BorderLayout.CENTER);
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Command line arguments", cli);
		tabs.addTab("Example configuration", config);
		
		this.add(new JLabel("On this tab we display the command line arguments that can be used when running gmark as well as a complete example gMark configuration file."), BorderLayout.PAGE_START);
		this.add(tabs, BorderLayout.CENTER);
	}
}
