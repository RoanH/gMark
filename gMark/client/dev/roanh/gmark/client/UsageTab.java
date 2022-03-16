package dev.roanh.gmark.client;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.apache.commons.cli.HelpFormatter;

import dev.roanh.gmark.Main;

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
		new HelpFormatter().printHelp(new PrintWriter(writer), 100, "gmark", "", Main.options, 1, 3, "", true);
		usage.setText(writer.toString());
		usage.setEditable(false);
		cli.add(new JScrollPane(usage), BorderLayout.CENTER);
		
		//example gmark config
		JPanel config = new JPanel(new BorderLayout());
		JTextArea xml = new JTextArea();
		xml.setText(new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("example.xml"))).lines().collect(Collectors.joining("\n")));
		xml.setEditable(false);
		config.add(new JScrollPane(xml), BorderLayout.CENTER);
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Command line arguments", cli);
		tabs.addTab("Example configuration", config);
		
		this.add(new JLabel("On this tab we display the command line arguments that can be used when running gmark as well as a complete example gMark configuration file."), BorderLayout.PAGE_START);
		this.add(tabs, BorderLayout.CENTER);
	}
}
