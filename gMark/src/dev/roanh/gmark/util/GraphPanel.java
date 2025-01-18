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
package dev.roanh.gmark.util;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.cpq.QueryGraphCPQ;
import dev.roanh.gmark.util.UniqueGraph.GraphEdge;
import dev.roanh.gmark.util.UniqueGraph.GraphNode;

/**
 * Simple component that visualises the nodes and
 * edges in a {@link UniqueGraph}. The user able to move
 * the graph nodes around by dragging. In addition
 * attached edges will be highlighted when a node is
 * selected. Nodes are initially placed randomly at
 * random positions on the canvas.
 * @author Roan
 * @param <V> The graph node data type.
 * @param <E> The graph edge data type.
 */
public class GraphPanel<V, E> extends JPanel implements MouseListener, MouseMotionListener{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = -3008224073658504239L;
	/**
	 * List of nodes to render.
	 */
	private List<Node> nodes = new ArrayList<Node>();
	/**
	 * List of edges to render.
	 */
	private List<Edge> edges = new ArrayList<Edge>();
	/**
	 * Last location the user held down the mouse.
	 * This will be <code>null</code> when the user
	 * is not dragging the mouse.
	 */
	private Point lastLocation = null;
	/**
	 * The currently selected node.
	 */
	private Node activeNode = null;
	/**
	 * Function to convert node data to a string
	 * to show to the user.
	 */
	private Function<V, String> nodeLabel;
	/**
	 * Function to convert edge data to a string
	 * to show to the user.
	 */
	private Function<E, String> edgeLabel;
	/**
	 * True if the graph should be drawn as a directed graph.
	 */
	private boolean directed = true;
	
	/**
	 * Constructs a new graph panel for the given graph.
	 * Data is converted to string form for display purposes
	 * using a call to the standard {@link Object#toString()} method.
	 * @param graph The graph to visualise.
	 * @see UniqueGraph
	 * @see #GraphPanel(UniqueGraph, Function, Function)
	 */
	public GraphPanel(UniqueGraph<V, E> graph){
		this(graph, V::toString, E::toString);
	}
	
	/**
	 * Constructs a new graph panel for the given graph
	 * and with the given functions to convert graph
	 * data to string form.
	 * @param graph The graph to visualise.
	 * @param nodeLabel The function to use to convert
	 *        graph node data to a string to display to the user.
	 * @param edgeLabel The function to use to convert
	 *        graph edge data to a string to display to the user.
	 * @see UniqueGraph
	 */
	public GraphPanel(UniqueGraph<V, E> graph, Function<V, String> nodeLabel, Function<E, String> edgeLabel){
		this.nodeLabel = nodeLabel;
		this.edgeLabel = edgeLabel;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
		setGraph(graph);
	}
	
	/**
	 * Sets whether the graph is directed. Although by definition
	 * the input {@link UniqueGraph} is directed, setting this to
	 * <code>false</code> will make it so arrow heads are not drawn
	 * and that a directed edge in both directions between two nodes
	 * is not drawn as a curved arc.
	 * @param directed True to draw the directed properties of the
	 *        graph, false to not draw these features.
	 */
	public void setDirected(boolean directed){
		this.directed = directed;
	}
	
	/**
	 * Sets the graph to display. This will replace the
	 * previous graph if any.
	 * @param graph The new graph to display.
	 */
	public void setGraph(UniqueGraph<V, E> graph){
		nodes.clear();
		edges.clear();
		activeNode = null;
		
		Map<GraphNode<V, E>, Node> nodeMap = new HashMap<GraphNode<V, E>, Node>();
		graph.getNodes().forEach(n->{
			Node node = new Node(n);
			nodes.add(node);
			nodeMap.put(n, node);
		});
		
		graph.getEdges().forEach(e->edges.add(new Edge(nodeMap.get(e.getSourceNode()), nodeMap.get(e.getTargetNode()), e)));
	}
	
	/**
	 * Finds the first node that contains the
	 * given point. If such a node exists.
	 * @param p The point to check.
	 * @return The first node that contains the
	 *         given point, or <code>null</code>
	 *         if no such node exists.
	 */
	private Node findNode(Point p){
		for(Node n : nodes){
			if(n.contains(p)){
				return n;
			}
		}
		return null;
	}
	
	@Override
	public void paintComponent(Graphics g1){
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D)g1;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		for(Edge edge : edges){
			edge.paint(g);
		}
		
		for(Node node : nodes){
			node.paint(g);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e){
		if(activeNode != null){
			activeNode.move(e.getX() - lastLocation.x, e.getY() - lastLocation.y);
		}
		
		lastLocation.move(e.getX(), e.getY());
		this.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e){
	}

	@Override
	public void mouseClicked(MouseEvent e){
	}

	@Override
	public void mousePressed(MouseEvent e){
		lastLocation = e.getPoint();
		activeNode = findNode(lastLocation);
		this.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e){
		lastLocation = null;
		activeNode = null;
		this.repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e){
	}

	@Override
	public void mouseExited(MouseEvent e){
	}
	
	/**
	 * Utility subroutine to show the given graph in a JFrame.
	 * @param <V> The vertex data type.
	 * @param <E> The edge data type.
	 * @param graph The graph to display.
	 * @param nodeLabel The function to use to determine node labels.
	 * @param edgeLabel The function to use to determine edge labels.
	 */
	public static <V, E> void show(UniqueGraph<V, E> graph, Function<V, String> nodeLabel, Function<E, String> edgeLabel){
		JFrame frame = new JFrame("GraphPanel v:" + graph.getNodeCount() + " e:" + graph.getEdgeCount());
		frame.add(new GraphPanel<V, E>(graph, nodeLabel, edgeLabel));
		frame.setSize(600, 400);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	/**
	 * Utility subroutine to show the given graph in a JFrame.
	 * @param <V> The vertex data type.
	 * @param <E> The edge data type.
	 * @param graph The graph to display.
	 */
	public static <V, E> void show(UniqueGraph<V, E> graph){
		show(graph, Object::toString, Object::toString);
	}
	
	/**
	 * Utility subroutine to show the query graph of the given CPQ in a JFrame.
	 * @param q The CPQ to display.
	 */
	public static void show(CPQ q){
		show(q.toQueryGraph());
	}
	
	/**
	 * Utility subroutine to show the given CPQ query graph in a JFrame.
	 * @param cpq The CPQ query graph to display.
	 */
	public static void show(QueryGraphCPQ cpq){
		show(cpq.toUniqueGraph(), cpq::getVertexLabel, Predicate::getAlias);
	}
	
	/**
	 * Object that holds both data on the actual
	 * graph node and data required to properly
	 * visualise the node.
	 * @author Roan
	 * @see Edge
	 * @see UniqueGraph
	 * @see GraphNode
	 */
	private class Node{
		/**
		 * The radius of graph nodes in pixels.
		 */
		private static final int RADIUS = 20;
		/**
		 * The shape of this node.
		 */
		private Ellipse2D.Double shape;
		/**
		 * The actual graph node associated with this node.
		 */
		private GraphNode<V, E> data;
		/**
		 * The current location of this node.
		 */
		private Point location = new Point();
		
		/**
		 * Constructs a new node with the
		 * given associated graph data. The
		 * node given a random x-coordinate
		 * between 0 and 800 and a random
		 * y-coordinate between 0 and 600.
		 * @param data The graph node this
		 *        node visualises.
		 */
		private Node(GraphNode<V, E> data){
			this.data = data;
			location.move(ThreadLocalRandom.current().nextInt(800), ThreadLocalRandom.current().nextInt(600));
			shape = new Ellipse2D.Double(-RADIUS, -RADIUS, 2.0D * RADIUS, 2.0D * RADIUS);
		}
		
		/**
		 * Moves this node by the given distance
		 * values relative to its current position.
		 * @param dx Relative x-axis displacement.
		 * @param dy Relative y-axis displacement.
		 */
		private void move(int dx, int dy){
			location.move(location.x + dx, location.y + dy);
		}
		
		/**
		 * Checks if the given point is contained
		 * within the circle representing this node.
		 * @param point The point to test.
		 * @return True if the given point is contained
		 *         within this node.
		 */
		private boolean contains(Point point){
			return shape.contains(point.x - location.x, point.y - location.y);
		}
		
		/**
		 * Renders this node with the given
		 * graphics context.
		 * @param g The graphics to use
		 *        to render this node.
		 */
		private void paint(Graphics2D g){
			final int y = -RADIUS - 5;
			FontMetrics fm = g.getFontMetrics();
			
			g.translate(location.x, location.y);
			g.setColor(this == activeNode ? Color.CYAN : Color.WHITE);
			g.fill(shape);
			g.setColor(Color.BLACK);
			g.draw(shape);
			String str = nodeLabel.apply(data.getData());
			if(str != null && !str.isEmpty()){
				int sw = g.getFontMetrics().stringWidth(str);
				g.setColor(Color.WHITE);
				g.fillRect(-sw / 2 - 1, y - fm.getHeight() + fm.getDescent(), sw + 2, fm.getHeight() + 1);
				g.setColor(Color.BLACK);
				g.drawRect(-sw / 2 - 1, y - fm.getHeight() + fm.getDescent(), sw + 2, fm.getHeight() + 1);
				g.drawString(str, -sw / 2, y);
			}
			g.translate(-location.x, -location.y);
		}
	}
	
	/**
	 * Object that holds both data on the actual
	 * graph edge and data required to properly
	 * visualise the edge.
	 * @author Roan
	 * @see Node
	 * @see UniqueGraph
	 * @see GraphEdge
	 */
	private class Edge{
		/**
		 * The size of the arrow heads for the directed edges.
		 */
		private static final int UNIT = 5;
		/**
		 * The source node for this edge.
		 */
		private Node source;
		/**
		 * The target node for this edge.
		 */
		private Node target;
		/**
		 * The actual graph edge this edge is associated with.
		 */
		private GraphEdge<V, E> data;
		/**
		 * The edge going in the opposite direction if it exists.
		 */
		private GraphEdge<V, E> twin;
		
		/**
		 * Constructs a new edge with the given source
		 * node, target node and associated graph edge.
		 * @param source The source node for this edge.
		 * @param target The target node for this edge.
		 * @param data The graph edge this edge visualises.
		 */
		private Edge(Node source, Node target, GraphEdge<V, E> data){
			this.source = source;
			this.target = target;
			this.data = data;
			twin = data.getTargetNode().getOutEdges().stream().filter(e->e.getTargetNode().equals(data.getSourceNode())).findAny().orElse(null);
		}
		
		/**
		 * Renders this edge using the given graphics context.
		 * @param g The graphics context to use.
		 */
		private void paint(Graphics2D g){
			if(source.equals(target)){
				g.setColor(source == activeNode ? Color.RED : Color.BLACK);
				g.drawArc(source.location.x, target.location.y, 2 * Node.RADIUS, 2 * Node.RADIUS, 90, -270);
				drawArrowHead(g, source.location.getX() + 1, source.location.getY() + Node.RADIUS + UNIT, source.location.getX() - 1, source.location.getY());
				
				E meta = data.getData();
				if(meta != null){
					g.drawString(edgeLabel.apply(data.getData()), source.location.x + 2 * Node.RADIUS, source.location.y + 2 * Node.RADIUS);
				}
			}else{
				g.setColor((source == activeNode || target == activeNode) ? Color.RED : Color.BLACK);
				if(twin != null && directed){
					if(source.hashCode() > target.hashCode()){
						AffineTransform transform = g.getTransform();
						
						g.translate(source.location.x, source.location.y);
						double rad = Math.atan2(target.location.y - source.location.y, target.location.x - source.location.x);
						g.rotate(rad);
						int dist = (int)Math.hypot(target.location.x - source.location.x, target.location.y - source.location.y);
						g.drawArc(0, -Node.RADIUS, dist, Node.RADIUS * 2, 0, 360);
						
						double x = dist / 4.0D - Node.RADIUS + UNIT;
						int y = (int)Math.sqrt(Node.RADIUS * Node.RADIUS - (x * x * Node.RADIUS * Node.RADIUS) / ((dist / 4.0D) * (dist / 4.0D)));
						drawArrowHead(g, Node.RADIUS, y, 0, 0);
						drawArrowHead(g, dist - Node.RADIUS, -y, dist, 0);
						g.setTransform(transform);
						
						E meta = data.getData();
						if(meta != null){
							g.drawString(
								edgeLabel.apply(meta),
								(source.location.x + target.location.x) / 2.0F + (float)(Math.sin(rad) * (2 + Node.RADIUS)),
								(source.location.y + target.location.y) / 2.0F + (float)(Math.cos(rad) * -(2 + Node.RADIUS))
							);
							meta = twin.getData();
							g.drawString(
								edgeLabel.apply(meta),
								(source.location.x + target.location.x) / 2.0F - (float)(Math.sin(rad) * (-2 + Node.RADIUS)),
								(source.location.y + target.location.y) / 2.0F - (float)(Math.cos(rad) * -(-2 + Node.RADIUS))
							);
						}
					}
				}else{
					g.drawLine(source.location.x, source.location.y, target.location.x, target.location.y);
					drawArrowHead(g, source.location.getX(), source.location.getY(), target.location.getX(), target.location.getY());
					E meta = data.getData();
					if(meta != null){
						g.drawString(edgeLabel.apply(meta), (source.location.x + target.location.x) / 2, (source.location.y + target.location.y) / 2 - UNIT);
					}
				}
			}
		}
		
		/**
		 * Draws the arrow head for a directed edge at
		 * the given location and with the given graphics context.
		 * Note: the arrow head is automatically offset from the
		 * center of the target node based on the radius of the node.
		 * @param g The graphics context to use.
		 * @param x1 The x-coordinate of the source node of the edge
		 *        the arrow head belongs to.
		 * @param y1 The y-coordinate of the source node of the edge
		 *        the arrow head belongs to.
		 * @param x2 The x-coordinate of the target node of the edge
		 *        the arrow head belongs to.
		 * @param y2 The y-coordinate of the target node of the edge
		 *        the arrow head belongs to.
		 */
		private void drawArrowHead(Graphics2D g, double x1, double y1, double x2, double y2){
			if(!directed){
				return;
			}
			
			//Mathematical details: https://www.desmos.com/calculator/4wofflsoqx
			double offset = Node.RADIUS;
			
			Path2D head = new Path2D.Double(Path2D.WIND_NON_ZERO, 3);
			if(Double.compare(x1, x2) == 0){
				if(y1 > y2){
					head.moveTo(x2, y2 + offset);
					head.lineTo(x2 - UNIT, y2 + UNIT + offset);
					head.lineTo(x2 + UNIT, y2 + UNIT + offset);
				}else{
					head.moveTo(x2, y2 - offset);
					head.lineTo(x2 - UNIT, y2 - UNIT - offset);
					head.lineTo(x2 + UNIT, y2 - UNIT - offset);
				}
			}else if(Double.compare(y1, y2) == 0){
				if(x1 > x2){
					head.moveTo(x2 + offset, y2);
					head.lineTo(x2 + UNIT + offset, y2 - UNIT);
					head.lineTo(x2 + UNIT + offset, y2 + UNIT);
				}else{
					head.moveTo(x2 - offset, y2);
					head.lineTo(x2 - UNIT - offset, y2 - UNIT);
					head.lineTo(x2 - UNIT - offset, y2 + UNIT);
				}
			}else{
				double invrc = -1.0D / ((y1 - y2) / (x1 - x2));
				double dx = Math.sqrt((UNIT * UNIT) / (1.0D + invrc * invrc));
				double dy = invrc * dx;
				
				double dist = Math.hypot(x1 - x2, y1 - y2);
				double f1 = (dist - offset) / dist;
				double f2 = (dist - UNIT - offset) / dist;
				
				head.moveTo(x1 + (x2 - x1) * f1, y1 + (y2 - y1) * f1);
				x2 = x1 + (x2 - x1) * f2;
				y2 = y1 + (y2 - y1) * f2;
				head.lineTo(x2 + dx, y2 + dy);
				head.lineTo(x2 - dx, y2 - dy);
			}
			head.closePath();
			g.fill(head);
		}
	}
}
