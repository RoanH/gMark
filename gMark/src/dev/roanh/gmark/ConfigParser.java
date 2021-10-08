package dev.roanh.gmark;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dev.roanh.gmark.core.graph.Configuration;
import dev.roanh.gmark.core.graph.Predicate;

public class ConfigParser{
	private static final Function<Node, Element> TO_ELEMENT = n->(Element)n;
	
	
	
	//parse config xml
	public static Configuration parse(Path file){
		try(InputStream in = Files.newInputStream(file)){
			Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
			xml.getDocumentElement().normalize();
			
			System.out.println("root: " + xml.getDocumentElement().getNodeName());
			
			List<Integer> sizes = stream(xml.getElementsByTagName("graph")).map(TO_ELEMENT).map(n->{
				return n.getElementsByTagName("nodes").item(0);
			}).map(Node::getTextContent).map(Integer::parseInt).collect(Collectors.toList());
			
			List<Predicate> predicates = parsePredicates(TO_ELEMENT.apply(xml.getElementsByTagName("predicates").item(0)));
			
			//xml.getDocumentElement().get
			

			
			System.out.println(xml.getElementsByTagName("graph").item(0).getTextContent());
			
			
			
			
			
			return new Configuration(sizes, predicates, null, null, null);
			
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
		
		
		
		
		return null;
	}
	
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
			predicates.add(new Predicate(entry.getKey(), entry.getValue(), prop.get(entry.getKey())));
		}
		
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
