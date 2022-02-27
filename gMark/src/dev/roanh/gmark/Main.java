package dev.roanh.gmark;

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

import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.exception.ConfigException;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.output.ConcreteSyntax;
import dev.roanh.gmark.query.QueryGenerator;
import dev.roanh.gmark.query.QueryGenerator.ProgressListener;
import dev.roanh.gmark.util.Util;

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
		options.addOption(Option.builder("w").longOpt("workload").hasArg().argName("file").desc("Triggers workload generation, a previous generated input workload can be provided to generate concrete syntaxes for instead").build());
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
		if(!cli.hasOption('o')){
			System.out.println("No output folder specified.");
		}else if(cli.hasOption('c') && cli.hasOption('w')){
			System.out.println("Cannot provide both a configuration file and a generated workload.");
		}else if(!cli.hasOption('c') && !cli.hasOption('w')){
			System.out.println("No configuration file or workload provided.");
		}else if(cli.hasOption('c')){
			handleConfigurationInput(cli);
		}else if(cli.hasOption('w')){
			handleWorkloadInput(cli);
		}
	}
	
	private static void handleConfigurationInput(CommandLine cli){
		try{
			Configuration config = ConfigParser.parse(Paths.get(cli.getOptionValue('c')));
			Path output = Paths.get(cli.getOptionValue('o'));
			Files.createDirectories(output);
			if(!Util.isEmpty(output) && !cli.hasOption('f')){
				System.out.println("Warning: the given output directory is not empty and force overwritting is disabled.");
			}
			
			System.out.println("Parsed a configuration with " + config.getWorkloads().size() + " workload(s).");
			
			//generate graphs
			if(cli.hasOption('g')){
				System.out.println("Generating graphs is not implemented yet!");
				//TODO generate graphs
			}
			
			//generate workloads
			if(cli.hasOption('w')){
				List<ConcreteSyntax> syntaxes = new ArrayList<ConcreteSyntax>();
				if(cli.hasOption('c')){
					for(String val : cli.getOptionValues('c')){
						ConcreteSyntax syntax = ConcreteSyntax.fromName(val);
						if(syntax == null){
							System.out.println("Unrecognised concrete syntax: " + val);
						}else{
							syntaxes.add(syntax);
						}
					}
				}
				
				for(Workload workload : config.getWorkloads()){
					System.out.println("Generating queries for workload: " + workload.getID());
					Path folder = output.resolve("workload-" + workload.getID());
					Files.createDirectory(folder);
					try{
						OutputWriter.writeGeneratedQueries(
							QueryGenerator.generateQueries(workload, new ProgressReporter()),
							folder,
							syntaxes,
							cli.hasOption('f')
						);
						System.out.println("Queries generated and saved.");
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
	
	private static void handleWorkloadInput(CommandLine cli){
		//TODO -- requires workload XML parsing
	}
	
	private static final class ProgressReporter implements ProgressListener{
		private int last = Integer.MIN_VALUE;
		
		private ProgressReporter(){
			System.out.print("Progress: ");
		}

		@Override
		public void update(int done, int total){
			if(done == total){
				System.out.println("100%");
			}else if(last + total / 10 == done){
				System.out.print((done / (total / 10)) * 10 + "%...  ");
				last = done;
			}
		}
	}
}
