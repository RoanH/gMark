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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import dev.roanh.gmark.cli.CommandLineClient;
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
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Evaluate Command Line Syntax", createHelpPanel(EvaluatorClient.INSTANCE));
		tabs.addTab("Example Database Graph", createExampleConfigPanel("example.edge"));
		tabs.addTab("Workload Command Line Syntax", createHelpPanel(WorkloadClient.INSTANCE));
		tabs.addTab("Example Workload Configuration", createExampleConfigPanel("example.xml"));
		
		this.add(new JLabel("On this tab we display the command line arguments that can be used when running gmark as well as a complete example gMark configuration file."), BorderLayout.PAGE_START);
		this.add(tabs, BorderLayout.CENTER);
	}
	
	/**
	 * Constructs a CLI help panel for the given client.
	 * @param client The CLI client to create a help panel for.
	 * @return A panel with CLI instructions for the given client.
	 */
	private static JPanel createHelpPanel(CommandLineClient client){
		JTextArea usage = new JTextArea();
		StringWriter writer = new StringWriter();
		client.printHelp(new PrintWriter(writer));
		usage.setText(writer.toString());
		usage.setEditable(false);
		return createScrollPanel(usage);
	}
	
	/**
	 * Creates a panel with an example configuration.
	 * @param resource The configuration file to load.
	 * @return A panel displaying the given configuration resource.
	 */
	private static JPanel createExampleConfigPanel(String resource){
		JTextArea area = new JTextArea();
		area.setText(readTextResource(resource));
		area.setEditable(false);
		return createScrollPanel(area);
	}
	
	/**
	 * Creates a scroll panel containing the given component.
	 * @param content The component to make scrollable.
	 * @return The newly created scrollable display panel.
	 */
	private static JPanel createScrollPanel(JComponent content){
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(content), BorderLayout.CENTER);
		return panel;
	}
	
	/**
	 * Reads the text resource with the given name and returns it.
	 * @param name The name of the resource to read.
	 * @return The UTF-8 text content of the requested resource.
	 */
	private static String readTextResource(String name){
		try(InputStream in = ClassLoader.getSystemResourceAsStream(name)){
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}catch(IOException ignore){
			//not really possible given that this is an internal resource
			return "Failed to load example.";
		}
	}
}
