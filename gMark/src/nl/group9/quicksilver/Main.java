//package nl.group9.quicksilver;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.Locale;
//
//import org.apache.commons.cli.CommandLine;
//import org.apache.commons.cli.DefaultParser;
//import org.apache.commons.cli.HelpFormatter;
//import org.apache.commons.cli.Option;
//import org.apache.commons.cli.Options;
//import org.apache.commons.cli.ParseException;
//
//import nl.group9.quicksilver.core.Benchmark;
//import nl.group9.quicksilver.core.data.BenchmarkResult;
//import nl.group9.quicksilver.impl.Provider;
//
///**
// * Main class for the command line interface.
// * @author Roan
// */
//public class Main{
//	/**
//	 * Supported CLI options.
//	 */
//	private static final Options options;
//
//	/**
//	 * Runs the benchmark on the provided input graph and workload:
//	 * <p>
//	 * Input arguments:
//	 * <ul>
//	 * <li><code>-g</code>: The path to the database graph file to use.</li>
//	 * <li><code>-w</code>: The path to the query workload file to use (note that the filename indicates the query language).</li>
//	 * <li><code>-o</code>: The path to the file to write the results to in JSON format, this argument is optional.</li>
//	 * </ul>
//	 * @param args The input command line arguments.
//	 */
//	public static void main(String[] args){
//		try{
//			CommandLine cli = new DefaultParser().parse(options, args);
//			if(cli.hasOption('g') && cli.hasOption('w')){
//				BenchmarkResult result = Benchmark.runEvaluatorBenchmark(
//					new Provider(),
//					Paths.get(cli.getOptionValue('g')),
//					Paths.get(cli.getOptionValue('w'))
//				);
//				
//				return;
//			}
//		}catch(ParseException ignore){
//		}catch(IOException e){
//			e.printStackTrace();
//			return;
//		}
//		
//		HelpFormatter help = new HelpFormatter();
//		help.setWidth(80);
//		help.printHelp("quicksilver", options, true);
//	}
//	
//	static{
//		options = new Options();
//		options.addOption(Option.builder("g").longOpt("graph").hasArg().argName("file").desc("The database graph file.").build());
//		options.addOption(Option.builder("w").longOpt("workload").hasArg().argName("file").desc("The query workload to run.").build());
//		options.addOption(Option.builder("o").longOpt("output").hasArg().argName("file").desc("The output file to write benchmark times to.").build());
//	}
//}