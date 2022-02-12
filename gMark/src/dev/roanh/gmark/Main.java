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
		options.addOption(Option.builder("w").longOpt("workload").hasArg().argName("file").desc("The directory to write the generated workload to").build());
		options.addOption(Option.builder("g").longOpt("graph").hasArg().argName("file").desc("The file to write the generated graph to").build());
		
		CommandLineParser parser = new DefaultParser();
		try{
			CommandLine cli = parser.parse(options, args);
			if(!cli.getArgList().isEmpty() && !cli.hasOption('h')){
				handleInput(cli);
			}
		}catch(ParseException ignore){
		}
		
		new HelpFormatter().printHelp("gmark", options);
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
			
		}catch(ConfigException e){
			System.out.println("Failed to parse the given configuration file: " + e.getMessage());
			return;
		}
	}
}
