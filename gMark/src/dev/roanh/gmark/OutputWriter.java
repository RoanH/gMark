package dev.roanh.gmark;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import dev.roanh.gmark.output.ConcreteSyntax;
import dev.roanh.gmark.query.QuerySet;

public class OutputWriter{

	//folder has to exist
	public static void writeGeneratedQueries(QuerySet queries, Path folder, List<ConcreteSyntax> syntaxes, boolean overwrite) throws IOException{
		write(folder.resolve("workload.xml"), queries.toXML(), overwrite);
		
		for(ConcreteSyntax syntax : syntaxes){
			Path dir = folder.resolve(syntax.getName());
			Files.createDirectory(dir);
			for(int i = 0; i < queries.getSize(); i++){
				write(dir.resolve("query-" + i + "." + syntax.getExtension()), syntax.convert(queries.get(i)), overwrite);
			}
		}
	}
	
	public static void writeGeneratedGraphs() throws IOException{
		//TODO
		throw new IllegalStateException("Not yet implemented.");
	}
	
	private static void write(Path file, String content, boolean overwrite) throws IOException{
		BufferedWriter writer = null;
		if(overwrite){
			writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		}else if(!Files.exists(file)){
			writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		}else{
			throw new IOException("File already exists and overwriting is disabled.");
		}
		
		writer.append(content);
		writer.flush();
		writer.close();
	}
}
