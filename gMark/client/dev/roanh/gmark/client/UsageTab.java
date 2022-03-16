package dev.roanh.gmark.client;

import java.awt.BorderLayout;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.apache.commons.cli.HelpFormatter;

import dev.roanh.gmark.Main;

public class UsageTab extends JPanel{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = 7240133091326502006L;

	public UsageTab(){
		super(new BorderLayout());
		this.setBorder(BorderFactory.createTitledBorder("Usage"));
		
		//command line arguments
		JPanel cli = new JPanel(new BorderLayout());
		JTextArea usage = new JTextArea();
		StringWriter writer = new StringWriter();
		new HelpFormatter().printHelp(new PrintWriter(writer), 100, "gmark", "", Main.options, 1, 3, "", true);
		usage.setText(writer.toString());
		usage.setEditable(false);
		
		cli.add(new JScrollPane(usage), BorderLayout.CENTER);
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Command line arguments", cli);
		
		this.add(new JLabel("TODO"), BorderLayout.PAGE_START);
		this.add(tabs, BorderLayout.CENTER);
	}
}
