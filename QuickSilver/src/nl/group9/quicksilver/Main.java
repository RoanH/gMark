package nl.group9.quicksilver;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import nl.group9.quicksilver.core.Benchmark;
import nl.group9.quicksilver.core.data.BenchmarkResult;
import nl.group9.quicksilver.impl.Provider;

public class Main{
	private static final Options options;

	public static void main(String[] args){
		try{
			CommandLine cli = new DefaultParser().parse(options, args);
			if(cli.hasOption('g') && cli.hasOption('w')){
				BenchmarkResult result = Benchmark.runEvaluatorBenchmark(
					new Provider(),
					Paths.get(cli.getOptionValue('g')),
					Paths.get(cli.getOptionValue('w'))
				);
				
				System.out.println("Benchmark result: " + result);
				if(cli.hasOption('o')){
					//TODO output result
				}
			}
		}catch(ParseException ignore){
		}catch(IOException e){
			e.printStackTrace();
			return;
		}
		
		HelpFormatter help = new HelpFormatter();
		help.setWidth(80);
		help.printHelp("quicksilver", options, true);
	}
	
	static{
		options = new Options();
		options.addOption(Option.builder("g").longOpt("graph").hasArg().argName("file").desc("The database graph file.").build());
		options.addOption(Option.builder("w").longOpt("workload").hasArg().argName("file").desc("The query workload to run.").build());
		options.addOption(Option.builder("o").longOpt("output").hasArg().argName("file").desc("The output file to write benchmark times to.").build());
	}
}
