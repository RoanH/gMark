package dev.roanh.gmark.client;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import dev.roanh.gmark.Main;
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
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Example Usage", new UsageTab());
		tabs.addTab("Query Generation", new QueryTab());
		tabs.addTab("Example Graphs", new ExampleTab());
		
		frame.add(tabs);
		frame.setSize(1000, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
