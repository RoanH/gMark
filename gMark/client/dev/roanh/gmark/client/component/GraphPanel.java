package dev.roanh.gmark.client.component;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JPanel;

import dev.roanh.gmark.util.Graph;
import dev.roanh.gmark.util.Graph.GraphEdge;
import dev.roanh.gmark.util.Graph.GraphNode;

public class GraphPanel<V, E> extends JPanel implements MouseListener, MouseMotionListener{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = -3008224073658504239L;
	@Deprecated
	private Graph<V, E> graph;
	private List<Node> nodes = new ArrayList<Node>();
	private List<Edge> edges = new ArrayList<Edge>();
	private Point lastLocation = null;
	
	public GraphPanel(Graph<V, E> graph){
		this.graph = graph;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
		Map<GraphNode<V, E>, Node> nodeMap = new HashMap<GraphNode<V, E>, Node>();
		graph.getNodes().forEach(n->{
			Node node = new Node(n);
			nodes.add(node);
			nodeMap.put(n, node);
		});
		
		graph.getEdges().forEach(e->edges.add(new Edge(nodeMap.get(e.getSourceNode()), nodeMap.get(e.getTargetNode()), e)));
	}
	
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
		Node node = findNode(lastLocation);
		System.out.println("node: " + node);
		if(node != null){
			node.move(e.getX() - lastLocation.x, e.getY() - lastLocation.y);
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
	}

	@Override
	public void mouseReleased(MouseEvent e){
		lastLocation = null;
	}

	@Override
	public void mouseEntered(MouseEvent e){
	}

	@Override
	public void mouseExited(MouseEvent e){
	}
	
	private class Node{
		private static final int RADIUS = 20;
		private Ellipse2D.Double shape;
		private GraphNode<V, E> data;
		private Point location = new Point();
		
		private Node(GraphNode<V, E> data){
			this.data = data;
			location.move(ThreadLocalRandom.current().nextInt(800), ThreadLocalRandom.current().nextInt(600));
			shape = new Ellipse2D.Double(-RADIUS, -RADIUS, 2 * RADIUS, 2 * RADIUS);
		}
		
		private void move(int dx, int dy){
			location.move(location.x + dx, location.y + dy);
		}
		
		private boolean contains(Point point){
			return shape.contains(point.x - location.x, point.y - location.y);
		}
		
		private void paint(Graphics2D g){
			final int y = -RADIUS - 5;
			FontMetrics fm = g.getFontMetrics();
			
			g.translate(location.x, location.y);
			g.setColor(Color.WHITE);
			g.fill(shape);
			g.setColor(Color.BLACK);
			g.draw(shape);
			String str = data.toString();
			int sw = g.getFontMetrics().stringWidth(str);
			g.setColor(Color.WHITE);
			g.fillRect(-sw / 2 - 1, y - fm.getHeight() + fm.getDescent(), sw + 2, fm.getHeight() + 1);
			g.setColor(Color.BLACK);
			g.drawRect(-sw / 2 - 1, y - fm.getHeight() + fm.getDescent(), sw + 2, fm.getHeight() + 1);
			g.drawString(str, -sw / 2, y);
			g.translate(-location.x, -location.y);
		}
	}
	
	private class Edge{
		private static final int UNIT = 5;//arrow head size
		private Node source;
		private Node target;
		private GraphEdge<V, E> data;
		
		private Edge(Node source, Node target, GraphEdge<V, E> data){
			this.source = source;
			this.target = target;
			this.data = data;
		}
		
		private void paint(Graphics2D g){
			g.drawLine(source.location.x, source.location.y, target.location.x, target.location.y);
			drawArrowHead(g);
		}
		
		private void drawArrowHead(Graphics2D g){
			double x1 = source.location.getX();
			double y1 = source.location.getY();
			double x2 = target.location.getX();
			double y2 = target.location.getY();
			double offset = Node.RADIUS;
			
			Path2D head = new Path2D.Double(Path2D.WIND_NON_ZERO, 3);
			if(Double.compare(x1, x2) == 0){
				if(y1 > y2){
					head.moveTo(x2, y2 + offset);
					head.lineTo(x2 - UNIT, y2 + UNIT + offset);
					head.lineTo(x2 + UNIT, y2 + UNIT + offset);
					y2 += UNIT;
				}else{
					head.moveTo(x2, y2 - offset);
					head.lineTo(x2 - UNIT, y2 - UNIT - offset);
					head.lineTo(x2 + UNIT, y2 - UNIT - offset);
					y2 -= UNIT;
				}
			}else if(Double.compare(y1, y2) == 0){
				if(x1 > x2){
					head.moveTo(x2 + offset, y2);
					head.lineTo(x2 + UNIT + offset, y2 - UNIT);
					head.lineTo(x2 + UNIT + offset, y2 + UNIT);
					x2 += UNIT;
				}else{
					head.moveTo(x2 - offset, y2);
					head.lineTo(x2 - UNIT - offset, y2 - UNIT);
					head.lineTo(x2 - UNIT - offset, y2 + UNIT);
					x2 -= UNIT;
				}
			}else{
				double invrc = -1.0D / ((y1 - y2) / (x1 - x2));
				double dx = Math.sqrt((UNIT * UNIT) / (1.0D + invrc * invrc));
				double dy = invrc * dx;
				
				double dist = Math.hypot(x1 - x2, y1 - y2);
				double f1 = (dist - offset) / dist;
				double f2 = (dist - UNIT - offset) / dist;
				
				head.moveTo(x1 + (x2 - x1) * f1, y1 + (y2 - y1) * f1);
				double tx = x1 + (x2 - x1) * f2;
				double ty = y1 + (y2 - y1) * f2;
				head.lineTo(tx + dx, ty + dy);
				head.lineTo(tx - dx, ty - dy);
			}
			head.closePath();
			g.fill(head);
		}
	}
}
