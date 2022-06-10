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
package dev.roanh.gmark;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.core.Distribution;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.core.WorkloadType;
import dev.roanh.gmark.core.graph.Edge;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;
import dev.roanh.gmark.exception.ConfigException;

/**
 * Parser to read complete gMark task configurations.
 * @author Roan
 */
public class ConfigParser{
	//TODO better validation
	/**
	 * Function to cast nodes to elements.
	 */
	private static final Function<Node, Element> TO_ELEMENT = n->(Element)n;
	
	/**
	 * Parses a gMark XML configuration from the given file.
	 * @param file The file to read from.
	 * @return The parsed gMark configuration.
	 * @throws ConfigException When something when wrong
	 *         while parsing the configuration.
	 */
	public static final Configuration parse(Path file) throws ConfigException{
		try(InputStream in = Files.newInputStream(file)){
			return parse(in);
		}catch(IOException e){
			throw new ConfigException(e);
		}
	}
	
	/**
	 * Parses a gMark XML configuration from the given input stream.
	 * @param in The input stream to read from.
	 * @return The parsed gMark configuration.
	 * @throws ConfigException When something when wrong
	 *         while parsing the configuration.
	 */
	public static final Configuration parse(InputStream in) throws ConfigException{
		try{
			Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
			xml.getDocumentElement().normalize();
			Element root = xml.getDocumentElement();
			
			List<Integer> sizes = stream(root, "graph").map(n->{
				return n.getElementsByTagName("nodes").item(0);
			}).map(Node::getTextContent).map(Integer::parseInt).collect(Collectors.toList());
			
			List<Predicate> predicates = parsePredicates(getElement(root, "predicates"));
			List<Type> types = parseTypes(getElement(root, "types"));
			Schema schema = parseSchema(getElement(root, "schema"), types, predicates);
			
			List<Workload> workloads = stream(root, "workload").map(data->WorkloadType.parse(data, schema)).collect(Collectors.toList());
			if(workloads.size() != workloads.stream().mapToInt(Workload::getID).distinct().count()){
				throw new ConfigException("Not all workloads have a distinct ID.");
			}
			
			return new Configuration(sizes, schema, workloads);
		}catch(Exception e){
			throw new ConfigException(e);
		}
	}
	
	/**
	 * Parses the <code>schema</code> section of the configuration XML.
	 * @param elem The schema XML element to parse.
	 * @param types A list of already parsed configuration node types.
	 * @param predicates A list of already parsed configuration predicates.
	 * @return The parsed graph schema.
	 */
	private static final Schema parseSchema(Element elem, List<Type> types, List<Predicate> predicates){
		List<Edge> edges = new ArrayList<Edge>();
		
		stream(elem, "source").forEach(s->{
			Type source = types.get(Integer.parseInt(s.getAttribute("type")));
			stream(s, "target").forEach(t->{
				edges.add(new Edge(
					source,
					types.get(Integer.parseInt(t.getAttribute("type"))),
					predicates.get(Integer.parseInt(t.getAttribute("symbol"))),
					parseDistribution(t, "indistribution"),
					parseDistribution(t, "outdistribution")
				));
			});
		});
		
		return new Schema(edges, types, predicates);
	}
	
	/**
	 * Parses a distribution section (<code>indistribution</code> or
	 * <code>outdistribution</code>) of the configuration XML.
	 * @param elem The element to parse.
	 * @param key The name of the tag to parse of the element
	 *        (<code>indistribution</code> or <code>outdistribution</code>).
	 * @return The parsed distribution.
	 */
	private static final Distribution parseDistribution(Element elem, String key){
		NodeList items = elem.getElementsByTagName(key);
		return items.getLength() == 0 ? Distribution.UNDEFINED : Distribution.fromXML(TO_ELEMENT.apply(items.item(0)));
	}
	
	/**
	 * Parses the <code>types</code> section of the configuration OutputXML.
	 * This section has the following format:
	 * <pre>
	 * &lt;types&gt;
	 *     &lt;size&gt;5&lt;/size&gt;
	 *     &lt;alias type="0"&gt;researcher&lt;/alias&gt;
	 *     &lt;proportion type="0"&gt;0.5&lt;/proportion&gt;
	 *
	 *     &lt;alias type="1"&gt;paper&lt;/alias&gt;
	 *     &lt;proportion type="1"&gt;0.3&lt;/proportion&gt;
	 *
	 *     &lt;alias type="2"&gt;journal&lt;/alias&gt;
	 *     &lt;proportion type="2"&gt;0.1&lt;/proportion&gt;
	 *
	 *     &lt;alias type="3"&gt;conference&lt;/alias&gt;
	 *     &lt;proportion type="3"&gt;0.1&lt;/proportion&gt;
	 *
	 *     &lt;alias type="4"&gt;city&lt;/alias&gt;
	 *     &lt;fixed type="4"&gt;100&lt;/fixed&gt;
	 * &lt;/types&gt;
	 * </pre>
	 * @param elem The element to parse with the type data.
	 * @return The types parsed from the element sorted by ID.
	 */
	private static final List<Type> parseTypes(Element elem){
		Map<Integer, String> alias = new LinkedHashMap<Integer, String>();
		Map<Integer, Double> prop = new HashMap<Integer, Double>();
		Map<Integer, Integer> fixed = new HashMap<Integer, Integer>();
		
		stream(elem, "alias").forEach(n->{
			alias.put(Integer.parseInt(n.getAttribute("type")), n.getTextContent());
		});
		
		stream(elem, "proportion").forEach(n->{
			prop.put(Integer.parseInt(n.getAttribute("type")), Double.parseDouble(n.getTextContent()));
		});
		
		stream(elem, "fixed").forEach(n->{
			fixed.put(Integer.parseInt(n.getAttribute("type")), Integer.parseInt(n.getTextContent()));
		});
		
		List<Type> types = new ArrayList<Type>(alias.size());
		for(Entry<Integer, String> entry : alias.entrySet()){
			Double proportion = prop.get(entry.getKey());
			if(proportion == null){
				types.add(new Type(entry.getKey(), entry.getValue(), fixed.get(entry.getKey())));
			}else{
				types.add(new Type(entry.getKey(), entry.getValue(), proportion));
			}
		}
		types.sort(Comparator.comparing(Type::getID));
		
		return types;
	}
	
	/**
	 * Parses the <code>predicates</code> section from the configuration OutputXML.
	 * This section has the following format:
	 * <pre>
	 * &lt;predicates&gt;
	 *     &lt;size&gt;4&lt;/size&gt;
	 *     &lt;alias symbol="0"&gt;authors&lt;/alias&gt;
	 *     &lt;proportion symbol="0"&gt;0.5&lt;/proportion&gt;
	 * 
	 *     &lt;alias symbol="1"&gt;publishedIn&lt;/alias&gt;
	 *     &lt;proportion symbol="1"&gt;0.3&lt;&lt;/proportion&gt;
	 *
	 *     &lt;alias symbol="2"&gt;heldIn&lt;/alias&gt;
	 *     &lt;proportion symbol="2"&gt;0.01&lt;/proportion&gt;
	 * 
	 *     &lt;alias symbol="3"&gt;extendedTo&lt;/alias&gt;
	 *     &lt;proportion symbol="3"&gt;0.19&lt;/proportion&gt;
	 * &lt;/predicates&gt;
	 * </pre>
	 * @param elem The element to parse with the predicate data.
	 * @return The parsed predicates from the element sorted by ID.
	 */
	private static final List<Predicate> parsePredicates(Element elem){
		Map<Integer, String> alias = new LinkedHashMap<Integer, String>();
		Map<Integer, Double> prop = new HashMap<Integer, Double>();
		
		stream(elem, "alias").forEach(n->{
			alias.put(Integer.parseInt(n.getAttribute("symbol")), n.getTextContent());
		});
		
		stream(elem, "proportion").forEach(n->{
			prop.put(Integer.parseInt(n.getAttribute("symbol")), Double.parseDouble(n.getTextContent()));
		});
		
		List<Predicate> predicates = new ArrayList<Predicate>(alias.size());
		for(Entry<Integer, String> entry : alias.entrySet()){
			predicates.add(new Predicate(entry.getKey(), entry.getValue(), prop.getOrDefault(entry.getKey(), null)));
		}
		predicates.sort(Comparator.comparing(Predicate::getID));
		
		return predicates;
	}
	
	/**
	 * Returns a stream over all the child elements
	 * with the given tag name in the given element.
	 * @param elem The element whose children to stream.
	 * @param name The tag name to filter by.
	 * @return A stream over all child elements with
	 *         the given tag name.
	 */
	public static final Stream<Element> stream(Element elem, String name){
		return stream(elem.getElementsByTagName(name)).map(TO_ELEMENT);
	}
	
	/**
	 * Returns a stream over all the nodes in the given node list.
	 * @param list The node list to stream.
	 * @return A stream over all node list elements.
	 * @see NodeList
	 */
	public static final Stream<Node> stream(NodeList list){
		Builder<Node> builder = Stream.builder();
		for(int i = 0; i < list.getLength(); i++){
			builder.add(list.item(i));
		}
		return builder.build();
	}
	
	/**
	 * Gets the the child element from the given
	 * element with the given tag name. If there
	 * are multiple elements that match the given
	 * tag name only the first is returned.
	 * @param elem The element whose child to get.
	 * @param name The tag name of the child to get.
	 * @return The child element with the given tag name.
	 */
	public static final Element getElement(Element elem, String name){
		return TO_ELEMENT.apply(elem.getElementsByTagName(name).item(0));
	}
	
	/**
	 * Invokes the given consumer with the name and value
	 * of each entry in the given named node map.
	 * @param data The named node map to iterate.
	 * @param consumer The consumer to invoke with
	 *        each name value pair.
	 * @see NamedNodeMap
	 */
	public static final void forEach(NamedNodeMap data, BiConsumer<String, String> consumer){
		for(int i = 0; i < data.getLength(); i++){
			Node item = data.item(i);
			consumer.accept(item.getNodeName(), item.getNodeValue());
		}
	}
}
