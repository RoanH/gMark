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

import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import dev.roanh.gmark.cli.CommandLineClient;
import dev.roanh.gmark.lang.QueryLanguage;
import dev.roanh.gmark.lang.QueryLanguageSyntax;

public class EvaluatorClient extends CommandLineClient{

	public EvaluatorClient(){
		super(null, null);
		// TODO Auto-generated constructor stub
		//		options.addOption(Option.builder("g").longOpt("graph").hasArg().argName("file").desc("The database graph file.").build());
//		options.addOption(Option.builder("g").longOpt("graph").hasArg().argName("file").desc("The database graph file.").build());
//		options.addOption(Option.builder("w").longOpt("workload").hasArg().argName("file").desc("The query workload to run.").build());
		
		//gmark evaluate -l cpq -s 56 -q a -t 5 -g ./graph.edge -o out.txt
		//gmark evaluate -l cpq -w ./q.cpq -g ./graph.edge -o out.txt

	}

	@Override
	protected void handleInput(CommandLine cli){
		QueryLanguage language = QueryLanguage.fromName(cli.getOptionValue('l')).orElse(null);
		if(language == null){
			//TODO complain
			return;
		}
		
		//(s) q (t) OR w
		
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	
	
	
	
}
