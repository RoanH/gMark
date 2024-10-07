package nl.group9.quicksilver.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import nl.group9.quicksilver.core.spec.DatabaseGraph;
import nl.group9.quicksilver.core.spec.EvaluatorProvider;

/**
 * Small utility class for reading database graphs.
 * @author Roan
 */
public final class GraphUtil{

	/**
	 * Prevent instantiation.
	 */
	private GraphUtil(){
	}
	
	/**
	 * Reads a database graph from the file at the given path. The graph file is expected
	 * to be encoded using UTF-8 and have the following format:
	 * <ol>
	 * <li>First a single header line containing the following three
	 * integers in order separated by a single space:<br>
	 * <pre>{@code <vertex count> <edge count> <label count>}</pre></li>
	 * <li>Then a number of lines matching the header edge count,
	 * each containing a single edge definition specified by three
	 * integers separated by a single space in the following format:<br>
	 * <pre>{@code <source vertex> <target vertex> <label>}</pre></li>
	 * </ol>
	 * Note: empty lines and comments are not permitted between edge definitions.
	 * @param <G> The concrete database graph implementation. 
	 * @param provider The provider to use to construct a new database graph to add edges to.
	 * @param file The input file to read from.
	 * @return The constructed database graph instance.
	 * @throws IOException When an IOException occurs.
	 */
	public static final <G extends DatabaseGraph> G readGraph(EvaluatorProvider<G, ?> provider, Path file) throws IOException{
		return readGraph(provider, Files.newInputStream(file));
	}

	/**
	 * Reads a database graph from the given input stream. The graph file is expected
	 * to be encoded using UTF-8 and have the following format:
	 * <ol>
	 * <li>First a single header line containing the following three
	 * integers in order separated by a single space:<br>
	 * {@code <vertex count> <edge count> <label count>}</li>
	 * <li>Then a number of lines matching the header edge count,
	 * each containing a single edge definition specified by three
	 * integers separated by a single space in the following format:<br>
	 * {@code <source vertex> <target vertex> <label>}</li>
	 * </ol>
	 * Note: empty lines and comments are not permitted between edge definitions.
	 * @param <G> The concrete database graph implementation. 
	 * @param provider The provider to use to construct a new database graph to add edges to.
	 * @param in The input stream to read from.
	 * @return The constructed database graph instance.
	 * @throws IOException When an IOException occurs.
	 */
	public static final <G extends DatabaseGraph> G readGraph(EvaluatorProvider<G, ?> provider, InputStream in) throws IOException{
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))){
			String header = reader.readLine();
			if(header == null){
				return null;
			}
			
			String[] metadata = header.split(" ");
			int vertices = Integer.parseInt(metadata[0]);
			int edges = Integer.parseInt(metadata[1]);
			int labels = Integer.parseInt(metadata[2]);
			G graph = provider.createGraph(vertices, edges, labels);
			
			String line;
			while((line = reader.readLine()) != null){
				String[] edge = line.split(" ");
				if(edge.length == 0){
					break;
				}

				int src = Integer.parseInt(edge[0]);
				int trg = Integer.parseInt(edge[1]);
				int lab = Integer.parseInt(edge[2]);

				graph.addEdge(src, trg, lab);
			}
			
			return graph;
		}
	}
}
