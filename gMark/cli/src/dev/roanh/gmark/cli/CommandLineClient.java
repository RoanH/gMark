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

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public abstract class CommandLineClient{
	private final String name;
	private final Options options;
	
	protected CommandLineClient(String name, Option... args){
		this.name = name;

		options = new Options();
		options.addOption("h", "help", false, "Prints this help text.");
		for(Option option : args){
			options.addOption(option);
		}
	}
	
	public void handleInput(String[] args){
		try{
			CommandLine cli = new DefaultParser().parse(options, args);
			if(cli.getOptions().length != 0 && !cli.hasOption('h')){
				handleInput(cli);
			}
		}catch(InputException e){
			System.out.println(e.getMessage());
			return;
		}catch(ParseException ignore){
		}
	
		printHelp();
	}
	
	public void printHelp(){
		printHelp(new PrintWriter(System.out, true, StandardCharsets.UTF_8));
	}
	
	public void printHelp(PrintWriter out){
		HelpFormatter help = new HelpFormatter();
		help.printHelp(out, 100, "gmark " + name, "", options, 1, 3, getHelpFooter(), true);
	}
	
	public String getHelpFooter(){
		return "";
	}
	
	public String getName(){
		return name;
	}

	public Options getOptions(){
		return options;
	}
	
	protected abstract void handleInput(CommandLine cli) throws InputException;
}
