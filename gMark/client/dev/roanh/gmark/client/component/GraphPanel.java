package dev.roanh.gmark.client.component;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import dev.roanh.gmark.util.Graph;
import dev.roanh.gmark.util.Graph.GraphNode;

public class GraphPanel<V, E> extends JPanel{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = -3008224073658504239L;
	@Deprecated
	private Graph<V, E> graph;
	private List<Node> nodes = new ArrayList<Node>();
	private List<Edge> edges = new ArrayList<Edge>();
	
	public GraphPanel(Graph<V, E> graph){
		this.graph = graph;
	}
	
	
	
	@Override
	public void paintComponent(Graphics g1){
		Graphics2D g = (Graphics2D)g1;
		
		
		
		
	}
	
	
	private class Node{
		private Ellipse2D.Double shape;
		private GraphNode<V, E> data;
	}
	
	private class Edge{
		
	}
}
