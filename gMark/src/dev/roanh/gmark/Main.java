package dev.roanh.gmark;

import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.exception.ConfigException;

public class Main{

	/**
	 * Main entry point for the CLI version of the application.
	 * @param args The passed command line arguments.
	 */
	public static void main(String[] args){
		Options options = new Options();
		options.addOption("h", "help", false, "Prints this help text");
		options.addOption(Option.builder("c").longOpt("config").hasArg().argName("file").desc("The workload and graph configuration file").build());
		options.addOption(Option.builder("s").longOpt("syntax").hasArgs().argName("syntax").desc("The concrete syntax(es) to output").build());
		options.addOption(Option.builder("w").longOpt("workload").hasArg().argName("file").desc("Triggers workload generation, a previous generated input workload can be provided to generate concrete syntaxes for").build());
		options.addOption(Option.builder("g").longOpt("graph").hasArgs().optionalArg(true).argName("size").desc("Triggers graph generation, a graph size can be provided (overrides the ones set in the configuration file)").build());
		options.addOption(Option.builder("o").longOpt("output").hasArg().argName("folder").desc("The folder to write the generated output to").build());
		options.addOption(Option.builder("f").longOpt("force").desc("Overwrite existing files if present").build());
		
		CommandLineParser parser = new DefaultParser();
		try{
			CommandLine cli = parser.parse(options, args);
			if(!cli.getArgList().isEmpty() && !cli.hasOption('h')){
				handleInput(cli);
			}
		}catch(ParseException ignore){
		}
		
		HelpFormatter help = new HelpFormatter();
		help.setWidth(80);
		help.printHelp("gmark", options, true);
	}
	
	private static void handleInput(CommandLine cli){
		if(!cli.hasOption('c')){
			System.out.println("No configuration file provided.");
			return;
		}
		
		try{
			Configuration config = ConfigParser.parse(Paths.get(cli.getOptionValue('c')));
			
			//TODO, probably print some statistics
			
			//TODO continue
			
			//generate graphs
			if(cli.hasOption('g')){
				
			}
			
			//generate workloads
			if(cli.hasOption('w')){
				
			}
			
			
		}catch(ConfigException e){
			System.out.println("Failed to parse the given configuration file: " + e.getMessage());
			return;
		}
	}
}
