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
package dev.roanh.gmark.cli;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Generic client for handling command line input.
 * @author Roan
 */
public abstract class CommandLineClient{
	/**
	 * The name of this client.
	 */
	private final String name;
	/**
	 * The CLI options accepted by this client.
	 */
	private final Options options;

	/**
	 * Constructs a new command line client.
	 * @param name The name of this client.
	 * @param args The options accepted by this client.
	 */
	protected CommandLineClient(String name, Option... args){
		this.name = name;

		options = new Options();
		options.addOption("h", "help", false, "Prints this help text.");
		for(Option option : args){
			options.addOption(option);
		}
	}

	/**
	 * Handles the given CLI input with this client.
	 * @param args The CLI input.
	 */
	public void handleInput(String[] args){
		try{
			CommandLine cli = new DefaultParser().parse(options, args);
			if(cli.getOptions().length != 0 && !cli.hasOption('h')){
				handleInput(cli);
			}else{
				printHelp();
			}
		}catch(InputException e){
			System.out.println(e.getMessage());
		}catch(ParseException ignore){
			printHelp();
		}
	}

	/**
	 * Prints the help text for this client to standard out.
	 * @see #printHelp(PrintWriter)
	 */
	public void printHelp(){
		printHelp(new PrintWriter(System.out, true, StandardCharsets.UTF_8));
	}

	/**
	 * Prints the help text for this client to the given writer.
	 * @param out The writer to write to.
	 * @see #printHelp()
	 */
	public void printHelp(PrintWriter out){
		HelpFormatter help = new HelpFormatter();
		help.printHelp(out, 100, "gmark " + name, "", options, 1, 3, getHelpFooter(), true);
	}

	/**
	 * The help footer to show at the end of the help text for this client.
	 * @return The help footer for this client.
	 */
	public String getHelpFooter(){
		return "";
	}

	/**
	 * Gets the display name of this client.
	 * @return The name of this client.
	 */
	public String getName(){
		return name;
	}

	/**
	 * Gets the arguments accepted by this client.
	 * @return The argument for this client.
	 */
	public Options getOptions(){
		return options;
	}

	/**
	 * Handles received command line input with this client.
	 * @param cli The input that was received.
	 * @throws InputException When the provided CLI input contains issues.
	 */
	protected abstract void handleInput(CommandLine cli) throws InputException;
}
