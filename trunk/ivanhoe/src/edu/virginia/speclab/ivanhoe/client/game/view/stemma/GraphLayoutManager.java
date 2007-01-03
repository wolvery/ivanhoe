/*
 * Created on Jul 28, 2005
 */

package edu.virginia.speclab.ivanhoe.client.game.view.stemma;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.blist.BArrayList;
import edu.virginia.speclab.ivanhoe.shared.blist.BCollection;
import edu.virginia.speclab.ivanhoe.shared.blist.BLinkedList;
import edu.virginia.speclab.ivanhoe.shared.blist.BList;

/**
 * @author benc
 */
public class GraphLayoutManager implements
        TreeModelListener {
    private final static int[] xPoints = {-500, 500, 0};
    private final static int[] yPoints = {-433, -433, 433};
    
    private final static Color DESELECTED_LINE_COLOR = Color.DARK_GRAY;
    private final static Color LINE_COLOR = Color.WHITE;
    private final static Color SELECTED_LINE_COLOR = Color.WHITE;
    
    private final static Stroke DESELECTED_LINE_STROKE = new BasicStroke(2.0f);
    private final static Stroke LINE_STROKE = new BasicStroke(2.0f);
    private final static Stroke SELECTED_LINE_STROKE = new BasicStroke(2.0f);
    
    private final static Stroke NODE_STROKE = new BasicStroke(1.8f);
    private final static Stroke SELECTED_NODE_STROKE = new BasicStroke(3.2f);
    private final static Shape REFERENCE_NODE_SHAPE = new Polygon(
            xPoints, yPoints, Math.min(xPoints.length, yPoints.length));
    
    private final StemmaManager stemmaManager;
    private final Shape nodeShape;
    
    private GraphCluster rootCluster;
    private final BLinkedList graphClusters;
    private final HashMap nodeClusterMapping;
    private boolean clustersAreDirty;
    
    GraphLayoutManager(StemmaManager stemmaManager)
    {
        this.stemmaManager = stemmaManager;
        this.graphClusters = new BLinkedList();
        this.nodeClusterMapping = new HashMap();
        this.clustersAreDirty = true;
        
        this.stemmaManager.addTreeModelListener(this);
        AffineTransform transform = new AffineTransform();
        transform.scale(0.02, 0.02);
        this.nodeShape = transform.createTransformedShape(REFERENCE_NODE_SHAPE);
    }
    
    void processNodes(StemmaNode rootNode) {
        graphClusters.clear();
        nodeClusterMapping.clear();
        
        // We need a breadth first, bottom-up traversal to do layout, lets make
        // a list that represents that traversal. 
        BLinkedList breadthFirstTraversal = new BLinkedList();
        BLinkedList openNodes = new BLinkedList();
        openNodes.add(rootNode);
        
        while (!openNodes.isEmpty()) {
            BLinkedList newOpenNodes = new BLinkedList();
            

            for (Iterator i=openNodes.iterator(); i.hasNext(); ) {
                StemmaNode sNode = (StemmaNode)i.next();
                newOpenNodes.addAll(sNode.getChildren());
                breadthFirstTraversal.add(sNode);
            }
            
            openNodes = newOpenNodes;
        }
        
        // We're going to build a list of clusters in the same order as the
        // list of nodes, but we need to do it backwards, because we have to
        // process the nodes from bottom to top. 
        final int nClusters = breadthFirstTraversal.size();
        final BArrayList boundingClusterBreadthTraversal = new BArrayList(nClusters);
        for (Iterator i=breadthFirstTraversal.reverseIterator(); i.hasNext(); ) {
            final StemmaNode sNode = (StemmaNode)i.next();
            SimpleLogger.logInfo("Processing stemma node: "+sNode);

            final GraphCluster gCluster = buildBoundingCluster(sNode, graphClusters);
            gCluster.centerChildren();
            boundingClusterBreadthTraversal.add(gCluster);
        }
        
        rootCluster = 
            (GraphCluster) boundingClusterBreadthTraversal.get(nClusters-1);
    }
    
    private GraphCluster buildBoundingCluster(
            StemmaNode sNode,
            BCollection aggregateClusters) {
        
        GraphCluster gCluster = new GraphCluster(sNode);
        nodeClusterMapping.put(sNode, gCluster);
        
        for (Iterator i=sNode.getChildren().iterator(); i.hasNext(); ) {
            StemmaNode childNode = (StemmaNode) i.next();
            
            if (!nodeClusterMapping.containsKey(sNode)) {
                throw new GraphCluster.ConstructionOrderException(gCluster, sNode);
            }
            
            GraphCluster childCluster = (GraphCluster) nodeClusterMapping.get(childNode); 
            gCluster.addChild(childCluster);
        }
        gCluster.layoutChildren();
        aggregateClusters.add(gCluster);
        
        return gCluster;
    }
    
    void drawGraph(Graphics2D g2d)
    {   
        // Generate clusters from nodes
        if (clustersAreDirty) {
            processNodes((StemmaNode) stemmaManager.getRoot());
            clustersAreDirty = false;
        }
        
        // Translate clusters into "world space" from branch space
        processClusters(rootCluster);
        
        /* Draw the connections between the nodes before drawing the nodes
        themselves so that the nodes appear over top of the connections.  At
        some point, we can even fade out the nodes themselves and only call
        drawClusterConnections() when looking at the long view of a large
        dataset. */
        
        drawClusterConnections(g2d, rootCluster);
        drawCluster(g2d, rootCluster);
    }

    private void processClusters(GraphCluster gCluster) {
    	    	
    	AffineTransform startingTransform = new AffineTransform();
    	//startingTransform.translate(0, GraphCluster.getStartingPointOffset());
    	
        processClusters(gCluster, startingTransform);
    }
    
    private void processClusters(GraphCluster gCluster, AffineTransform transform) {
        AffineTransform localTransform = gCluster.getTranslation().getAffineTransform();
        
        AffineTransform globalTransform = new AffineTransform(transform);
        globalTransform.concatenate(localTransform);
        
        // If the cluster is in local space, convert it to global
        if (!gCluster.isWorldSpace()) {
            Point2D.Float location = new Point2D.Float();
            globalTransform.transform(new Point2D.Float(), location);
            gCluster.setLocation(location);
            gCluster.setWorldSpace(true);
        }
        
        for (Iterator i=gCluster.getChildren().iterator(); i.hasNext(); )
        {
            GraphCluster childCluster = (GraphCluster) i.next();
            processClusters(childCluster, globalTransform);
        }

    }

    private final void drawCluster(Graphics2D g2d, GraphCluster gCluster) {
        for (Iterator i=gCluster.getChildren().iterator(); i.hasNext(); )
        {
            GraphCluster childCluster = (GraphCluster) i.next();
            drawCluster(g2d, childCluster);
        }
        
        AffineTransform componentTransform = g2d.getTransform();
        g2d.transform(gCluster.getTranslation().getAffineTransform());
        
        Stroke nodeStroke = NODE_STROKE;
        
        StemmaNode sNode = gCluster.getMainNode();
        
        if (sNode == stemmaManager.getSelectedNode() || sNode.isHighlighted()) {
            nodeStroke = SELECTED_NODE_STROKE;
        }
    
        g2d.setPaint(sNode.getFillColor());
        g2d.fill(nodeShape);
        
        g2d.setPaint(sNode.getStrokeColor());
        g2d.setStroke(nodeStroke);
        g2d.draw(nodeShape);
        
//        Rectangle2D boundingRect = gCluster.getBoundingBox();
//        g2d.draw(boundingRect);
        
        g2d.setTransform(componentTransform);
    }

    private final void drawClusterConnections(Graphics2D g2d, GraphCluster gCluster) {
        Point2D.Float ptSrc = new Point2D.Float();
        Point2D.Float ptDst = new Point2D.Float();
        
        ptSrc = gCluster.getLocation();
        
        boolean thisNodeSelected = gCluster.getMainNode().isSelected();
        
        g2d.setPaint(LINE_COLOR);
        g2d.setStroke(LINE_STROKE);
        
        BList children = (BList) gCluster.getChildren().createCopy(); 
        
        for (ListIterator i=children.listIterator(); i.hasNext(); ) {
            GraphCluster childCluster = (GraphCluster)i.next();
            
            if (thisNodeSelected && childCluster.getMainNode().isSelected()) {
                if (i.hasNext()) { 
                    // Defer processing 'til the end so that the selected path
                    // is drawn on top.
                    i.remove();
                    int nextIdx = i.nextIndex();
                    children.add(childCluster);
                    i = children.listIterator(nextIdx);
                    continue;
                } else {
                    g2d.setPaint(SELECTED_LINE_COLOR);
                    g2d.setStroke(SELECTED_LINE_STROKE);
                }
            } else {
                g2d.setPaint(DESELECTED_LINE_COLOR);
                g2d.setStroke(DESELECTED_LINE_STROKE);
            }
            
            ptDst = childCluster.getLocation();
         
            /* Cubic spline control points which have the same horizontal
             * position as their parents, but are vertically closer than their
             * parent to the other point.  Increase the distance of the control
             * from the parent to make a curvier curve. 
             */
            Point2D.Float ctrlPtSrc = new Point2D.Float(ptSrc.x, (ptSrc.y+ptDst.y)/2.0f);
            Point2D.Float ctrlPtDst = new Point2D.Float(ptDst.x, (ptDst.y+ptSrc.y)/2.0f);
            
            g2d.draw(new CubicCurve2D.Float(
                    ptSrc.x, ptSrc.y,
                    ctrlPtSrc.x, ctrlPtSrc.y,
                    ctrlPtDst.x, ctrlPtDst.y,
                    ptDst.x, ptDst.y));
        }
        
        for (Iterator i=gCluster.getChildren().iterator(); i.hasNext(); )
        {
            GraphCluster childCluster = (GraphCluster) i.next();
            drawClusterConnections(g2d, childCluster);
        }
    }
    
    public void treeNodesChanged(TreeModelEvent e) {
        clustersAreDirty = true;
    }

    public void treeNodesInserted(TreeModelEvent e) {
        clustersAreDirty = true;
    }

    public void treeNodesRemoved(TreeModelEvent e) {
        clustersAreDirty = true;
    }

    public void treeStructureChanged(TreeModelEvent e) {
        clustersAreDirty = true;
    }
    
    StemmaNode getStemmaAt(Point2D pt) {
        Point2D.Float dstPt = new Point2D.Float();
        
        for (Iterator i=graphClusters.iterator(); i.hasNext(); ) {
            GraphCluster gCluster = (GraphCluster) i.next();
            try {
                gCluster.getTranslation().getAffineTransform().inverseTransform(pt, dstPt);
            } catch (NoninvertibleTransformException nte) {
                SimpleLogger.logError("Transform for ["+gCluster+"] should be invertable!", nte);
                continue;
            }
            
            if (nodeShape.contains(dstPt)) {
                return gCluster.getMainNode();
            }
        }
        
        return null;
    }
}
