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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dev.roanh.gmark.core.Distribution;
import dev.roanh.gmark.core.graph.Configuration;
import dev.roanh.gmark.core.graph.Edge;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;

/**
 * Parser to read complete gmark task configurations.
 * @author Roan
 */
public class ConfigParser{
	//TODO better validation and exception handling
	/**
	 * Function to cast nodes to elements.
	 */
	private static final Function<Node, Element> TO_ELEMENT = n->(Element)n;
	
	//parse config xml
	public static Configuration parse(Path file){
		try(InputStream in = Files.newInputStream(file)){
			return parse(in);
		}catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO throw a proper exception
		throw new RuntimeException("Failed to parse configuration file.");
	}
		
	public static Configuration parse(InputStream in){
		try{
			Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
			xml.getDocumentElement().normalize();
			
			List<Integer> sizes = stream(xml.getElementsByTagName("graph")).map(TO_ELEMENT).map(n->{
				return n.getElementsByTagName("nodes").item(0);
			}).map(Node::getTextContent).map(Integer::parseInt).collect(Collectors.toList());
			
			List<Predicate> predicates = parsePredicates(TO_ELEMENT.apply(xml.getElementsByTagName("predicates").item(0)));
			List<Type> types = parseTypes(TO_ELEMENT.apply(xml.getElementsByTagName("types").item(0)));
			Schema schema = parseSchema(TO_ELEMENT.apply(xml.getElementsByTagName("schema").item(0)), types, predicates);
			
			//TODO parse workloads -- which are generator specific now
			
			return new Configuration(sizes, schema, null);
		}catch(SAXException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(ParserConfigurationException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO throw a proper exception
		throw new RuntimeException("Failed to parse configuration file.");
	}
	
	private static final Schema parseSchema(Element elem, List<Type> types, List<Predicate> predicates){
		List<Edge> edges = new ArrayList<Edge>();
		
		stream(elem.getElementsByTagName("source")).map(TO_ELEMENT).forEach(s->{
			Type source = types.get(Integer.parseInt(s.getAttribute("type")));
			stream(s.getElementsByTagName("target")).map(TO_ELEMENT).forEach(t->{
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
	
	private static final Distribution parseDistribution(Element elem, String key){
		NodeList items = elem.getElementsByTagName(key);
		return items.getLength() == 0 ? Distribution.UNDEFINED : Distribution.fromXML(TO_ELEMENT.apply(items.item(0)));
	}
	
	/**
	 * Parses the <code>types</code> section of the configuration XML.
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
		
		stream(elem.getElementsByTagName("alias")).map(TO_ELEMENT).forEach(n->{
			alias.put(Integer.parseInt(n.getAttribute("type")), n.getTextContent());
		});
		
		stream(elem.getElementsByTagName("proportion")).map(TO_ELEMENT).forEach(n->{
			prop.put(Integer.parseInt(n.getAttribute("type")), Double.parseDouble(n.getTextContent()));
		});
		
		stream(elem.getElementsByTagName("fixed")).map(TO_ELEMENT).forEach(n->{
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
	 * Parses the <code>predicates</code> section from the configuration XML.
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
		
		stream(elem.getElementsByTagName("alias")).map(TO_ELEMENT).forEach(n->{
			alias.put(Integer.parseInt(n.getAttribute("symbol")), n.getTextContent());
		});
		
		stream(elem.getElementsByTagName("proportion")).map(TO_ELEMENT).forEach(n->{
			prop.put(Integer.parseInt(n.getAttribute("symbol")), Double.parseDouble(n.getTextContent()));
		});
		
		List<Predicate> predicates = new ArrayList<Predicate>(alias.size());
		for(Entry<Integer, String> entry : alias.entrySet()){
			predicates.add(new Predicate(entry.getKey(), entry.getValue(), prop.getOrDefault(entry.getKey(), Double.NaN)));
		}
		predicates.sort(Comparator.comparing(Predicate::getID));
		
		return predicates;
	}
	
	private static final <T> Stream<Node> stream(NodeList list){
		Builder<Node> builder = Stream.builder();
		for(int i = 0; i < list.getLength(); i++){
			builder.add(list.item(i));
		}
		return builder.build();
	}
}
