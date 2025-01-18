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
package dev.roanh.gmark.cli.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.OutputWriter;
import dev.roanh.gmark.cli.CommandLineClient;
import dev.roanh.gmark.cli.ProgressReporter;
import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.core.WorkloadType;
import dev.roanh.gmark.exception.ConfigException;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.output.ConcreteSyntax;
import dev.roanh.gmark.query.QueryGenerator;
import dev.roanh.gmark.query.QuerySet;
import dev.roanh.gmark.util.Util;

/**
 * Command line client for workload generation.
 * <p>
 * Example Usage:
 * <ul><li><code>gmark workload -c ./config.xml -s sql -o ./output -f</code></li></ul>
 * @author Roan
 */
public final class WorkloadClient extends CommandLineClient{
	/**
	 * Instance of this client.
	 */
	public static final WorkloadClient INSTANCE = new WorkloadClient();
	
	/**
	 * Constructs a new workload client.
	 */
	private WorkloadClient(){
		super(
			"workload",
			Option.builder("c").longOpt("config").hasArg().argName("file").desc("The workload and graph configuration file.").build(),
			Option.builder("s").longOpt("syntax").hasArgs().argName("syntax").desc("The concrete syntax(es) to output (sql and/or formal).").build(),
			Option.builder("o").longOpt("output").hasArg().argName("folder").desc("The folder to write the generated output to.").build(),
			Option.builder("f").longOpt("force").desc("Overwrite existing files if present.").build()
		);
	}
	
	/**
	 * Handles the input arguments.
	 * @param cli The command line argument.
	 */
	@Override
	protected void handleInput(CommandLine cli){
		if(!cli.hasOption('o')){
			System.out.println("No output folder specified.");
		}else if(!cli.hasOption('c')){
			System.out.println("No configuration file provided.");
		}else{
			handleConfigurationInput(cli);
		}
	}
	
	/**
	 * Handles input when a complete configuration file is provided.
	 * @param cli The command line arguments.
	 */
	private void handleConfigurationInput(CommandLine cli){
		try{
			Configuration config = ConfigParser.parse(Paths.get(cli.getOptionValue('c')));
			Path output = Paths.get(cli.getOptionValue('o'));
			Files.createDirectories(output);
			if(!Util.isEmpty(output) && !cli.hasOption('f')){
				System.out.println("Warning: the given output directory is not empty and force overwritting is disabled.");
			}
			
			System.out.println("Parsed a configuration with " + config.getWorkloads().size() + " workload(s).");
			
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
		}catch(ConfigException e){
			System.out.println("Failed to parse the given configuration file: " + e.getMessage());
		}catch(InvalidPathException e){
			System.out.println("Given output folder is not a valid path: " + e.getMessage());
		}catch(IOException e){
			System.out.println("Failed to write to the output directory: " + e.getMessage());
		}
	}
}
