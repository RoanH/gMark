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
package dev.roanh.gmark;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import dev.roanh.gmark.output.ConcreteSyntax;
import dev.roanh.gmark.query.QuerySet;

/**
 * Class responsible for writing generated graphs
 * and queries to files.
 * @author Roan
 */
public final class OutputWriter{

	/**
	 * Prevent instantiation.
	 */
	private OutputWriter(){
	}
	
	/**
	 * Writes the given set of queries to the given folder.
	 * @param queries The queries to write.
	 * @param folder The folder to write to, this folder has to exist.
	 * @param syntaxes The concrete syntaxes to write.
	 * @param overwrite True if existing files should be overwritten.
	 * @throws IOException When an IOException occurs.
	 */
	public static void writeGeneratedQueries(QuerySet queries, Path folder, List<ConcreteSyntax> syntaxes, boolean overwrite) throws IOException{
		write(folder.resolve("workload.xml"), queries.toXML(), overwrite);
		
		for(ConcreteSyntax syntax : syntaxes){
			Path dir = folder.resolve(syntax.getID());
			Files.createDirectories(dir);
			for(int i = 0; i < queries.size(); i++){
				write(dir.resolve("query-" + i + "." + syntax.getExtension()), syntax.convert(queries.get(i)), overwrite);
			}
		}
	}
	
	/**
	 * Writes the given string to the given file.
	 * @param file The file to write to.
	 * @param content The string to write to the file.
	 * @param overwrite True if the file should be overwritten
	 *        if it already exists.
	 * @throws IOException When an IOException occurs or
	 *         when the given file already exists and
	 *         overwriting is not enabled.
	 */
	private static void write(Path file, String content, boolean overwrite) throws IOException{
		if(!overwrite && Files.exists(file)){
			throw new IOException("File already exists and overwriting is disabled.");
		}
		
		try(BufferedWriter writer = Files.newBufferedWriter(file)){
			writer.append(content);
			writer.flush();
		}
	}
}
