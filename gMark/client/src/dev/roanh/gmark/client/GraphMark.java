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

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import dev.roanh.gmark.cli.Main;
import dev.roanh.util.ClickableLink;
import dev.roanh.util.Dialog;
import dev.roanh.util.Util;

/**
 * Main class for the GUI version of the application.
 * @author Roan
 * @see Main
 */
public class GraphMark{
	/**
	 * The main GUI frame.
	 */
	private static final JFrame frame = new JFrame("gMark");

	/**
	 * Launches the gMark GUI if not command line
	 * arguments are passed. Otherwise switches
	 * over to the CLI version of gMark.
	 * @param args Command line arguments, if present
	 *        then gMark will switch to command line mode.
	 */
	public static void main(String[] args){
		if(args.length != 0){
			Main.main(args);
			return;
		}
		
		System.out.println("Running gMark (GUI) version " + Main.VERSION.substring(1));
		Util.installUI();
		Dialog.setDialogTitle("gMark");
		Dialog.setParentFrame(frame);
		
		JPanel content = new JPanel(new BorderLayout());
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Example Usage", new UsageTab());
		tabs.addTab("Query Generation", new QueryTab());
		tabs.addTab("Example Graphs", new ExampleTab());
		content.add(tabs, BorderLayout.CENTER);
		
		JPanel footer = new JPanel(new BorderLayout());
		footer.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		footer.add(Util.getVersionLabel("gMark", Main.VERSION, false, SwingConstants.LEFT), BorderLayout.LINE_START);
		JLabel git = new JLabel("<html>GitHub: <font color=blue><u>RoanH/gMark</u></font></html>", SwingConstants.RIGHT);
		git.addMouseListener(new ClickableLink("https://github.com/RoanH/gMark"));
		footer.add(git, BorderLayout.LINE_END);
		footer.add(new JPanel(), BorderLayout.CENTER);
		content.add(footer, BorderLayout.PAGE_END);
		
		frame.add(content);
		frame.setSize(1000, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		SwingUtilities.invokeLater(()->frame.setVisible(true));
	}
}
