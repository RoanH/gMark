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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import dev.roanh.gmark.cli.client.EvaluatorClient;
import dev.roanh.gmark.cli.client.WorkloadClient;
import dev.roanh.util.Util;

/**
 * Main class of the command line version of
 * the application, responsible for command handling.
 * @author Roan
 */
public class Main{
	/**
	 * The current version of gMark.
	 */
	public static final String VERSION = Objects.requireNonNullElse(Util.readArtifactVersion("dev.roanh.gmark", "gmark"), "unknown");
	private static final Map<String, CommandLineClient> clients = List.of(
		new EvaluatorClient(),
		new WorkloadClient()
	).stream().collect(Collectors.toMap(CommandLineClient::getName, Function.identity()));

	/**
	 * Main entry point for the CLI version of the application.
	 * @param args The passed command line arguments.
	 */
	public static void main(String[] args){
		System.out.println("Running gMark (CLI) version " + VERSION);
		
		if(args.length > 0){
			CommandLineClient client = clients.get(args[0].toLowerCase(Locale.ROOT));
			if(client != null){
				String[] newArgs = new String[args.length];
				System.arraycopy(args, 1, newArgs, 0, newArgs.length);
				client.handleInput(newArgs);
				return;
			}
		}
		
		clients.values().forEach(CommandLineClient::printHelp);
	}
}
