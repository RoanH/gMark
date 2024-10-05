package nl.group9.quicksilver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import nl.group9.quicksilver.core.spec.DatabaseGraph;
import nl.group9.quicksilver.core.spec.EvaluatorProvider;

public final class GraphUtil{
	
	public static final <G extends DatabaseGraph> G readGraph(EvaluatorProvider<G, ?> evaluator, Path file) throws IOException{
		return readGraph(evaluator, Files.newInputStream(file));
	}

	//n tripples ish
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
