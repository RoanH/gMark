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
package dev.roanh.gmark.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.OutputWriter;
import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.core.WorkloadType;
import dev.roanh.gmark.exception.ConfigException;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.output.ConcreteSyntax;
import dev.roanh.gmark.query.QueryGenerator;
import dev.roanh.gmark.query.QueryGenerator.ProgressListener;
import dev.roanh.gmark.query.QuerySet;
import dev.roanh.gmark.util.Util;

/**
 * Main class of the command line version of
 * the application, responsible for command handling.
 * @author Roan
 */
public class Main{
	/**
	 * The current version of gMark.
	 */
	public static final String VERSION;
	/**
	 * The command line options.
	 */
	public static final Options options;

	/**
	 * Main entry point for the CLI version of the application.
	 * @param args The passed command line arguments.
	 */
	public static void main(String[] args){
		System.out.println("Running gMark (CLI) version " + VERSION);
		
		CommandLineParser parser = new DefaultParser();
		try{
			CommandLine cli = parser.parse(options, args);
			if(cli.getOptions().length != 0 && !cli.hasOption('h')){
				handleInput(cli);
				return;
			}
		}catch(ParseException ignore){
		}
		
		HelpFormatter help = new HelpFormatter();
		help.setWidth(80);
		help.printHelp("gmark", options, true);
	}
	
	/**
	 * Handles the input arguments.
	 * @param cli The command line argument.
	 */
	private static void handleInput(CommandLine cli){
		if(!cli.hasOption('o')){
			System.out.println("No output folder specified.");
		}else if(!cli.hasOption('c') && !cli.hasOption('w')){
			System.out.println("No configuration file or workload provided.");
		}else if(cli.hasOption('c') && cli.hasOption('w') && cli.getOptionValue('w') != null){
			System.out.println("Cannot provide both a configuration file and a generated workload.");
		}else if(cli.hasOption('c')){
			handleConfigurationInput(cli);
		}else if(cli.hasOption('w') && cli.getOptionValue('w') == null){
			System.out.println("Workload generation requested but no configuration file provided.");
		}else if(cli.hasOption('w')){
			handleWorkloadInput(cli);
		}
	}
	
	/**
	 * Handles input when a complete configuration file is provided.
	 * @param cli The command line arguments.
	 */
	private static void handleConfigurationInput(CommandLine cli){
		try{
			Configuration config = ConfigParser.parse(Paths.get(cli.getOptionValue('c')));
			Path output = Paths.get(cli.getOptionValue('o'));
			Files.createDirectories(output);
			if(!Util.isEmpty(output) && !cli.hasOption('f')){
				System.out.println("Warning: the given output directory is not empty and force overwritting is disabled.");
			}
			
			System.out.println("Parsed a configuration with " + config.getWorkloads().size() + " workload(s).");
			
			//generate workloads
			if(cli.hasOption('w')){
				List<ConcreteSyntax> syntaxes = new ArrayList<ConcreteSyntax>();
				if(cli.hasOption('s')){
					for(String val : cli.getOptionValues('s')){
						ConcreteSyntax syntax = ConcreteSyntax.fromName(val);
						if(syntax == null){
							System.out.println("Unrecognised concrete syntax: " + val);
						}else{
							syntaxes.add(syntax);
						}
					}
				}
				
				for(Workload workload : config.getWorkloads()){
					if(workload.getType() == WorkloadType.RPQ){
						System.out.println("Generating RPQ workloads is not supported yet, skipping workload with ID " + workload.getID());
						continue;
					}
					
					System.out.println("Generating queries for workload with ID " + workload.getID());
					Path folder = output.resolve("workload-" + workload.getID());
					Files.createDirectories(folder);
					try{
						QuerySet queries = QueryGenerator.generateQueries(workload, new ProgressReporter());
						OutputWriter.writeGeneratedQueries(queries, folder, syntaxes, cli.hasOption('f'));
						System.out.println("Queries generated and saved, query generation took " + queries.getGenerationTime() + "ms.");
					}catch(GenerationException e){
						System.out.println("An error occurred while generating queries: " + e.getMessage());
					}
				}
			}
		}catch(ConfigException e){
			System.out.println("Failed to parse the given configuration file: " + e.getMessage());
		}catch(InvalidPathException e){
			System.out.println("Given output folder is not a valid path: " + e.getMessage());
		}catch(IOException e){
			System.out.println("Failed to write to the output directory: " + e.getMessage());
		}
	}
	
	/**
	 * Handles input when a generated workload file is provided.
	 * @param cli The command line arguments.
	 */
	private static void handleWorkloadInput(CommandLine cli){
		//TODO -- requires workload XML parsing
		System.out.println("Parsing already generated workloads is not supported yet.");
	}
	
	static{
		options = new Options();
		options.addOption("h", "help", false, "Prints this help text");
		
		//generation
		options.addOption(Option.builder("c").longOpt("config").hasArg().argName("file").desc("The workload and graph configuration file").build());
		options.addOption(Option.builder("s").longOpt("syntax").hasArgs().argName("syntax").desc("The concrete syntax(es) to output").build());
		options.addOption(Option.builder("w").longOpt("workload").hasArg().optionalArg(true).argName("file").desc("Triggers workload generation, a previously generated input workload can optionally be provided to generate concrete syntaxes for instead").build());
		options.addOption(Option.builder("o").longOpt("output").hasArg().argName("folder").desc("The folder to write the generated output to").build());
		options.addOption(Option.builder("f").longOpt("force").desc("Overwrite existing files if present").build());
		
		options.addOption(Option.builder("g").longOpt("graph").hasArg().argName("file").desc("The database graph file.").build());
		
		String version = dev.roanh.util.Util.readArtifactVersion("dev.roanh.gmark", "gmark");
		VERSION = version == null ? "unknown" : version;
	}
	
	/**
	 * Progress listener that report progress to
	 * standard out at 10% intervals.
	 * @author Roan
	 */
	private static final class ProgressReporter implements ProgressListener{
		/**
		 * The last completion count progress was reported.
		 */
		private int last = 0;
		
		/**
		 * Constructs a new progress reporter.
		 */
		private ProgressReporter(){
			System.out.print("Progress: ");
		}

		@Override
		public void update(int done, int total){
			if(done == total){
				System.out.println("100%");
			}else if(last + total / 10 == done){
				System.out.print((done / (total / 10)) * 10 + "%...");
				last = done;
			}
		}
	}
}
