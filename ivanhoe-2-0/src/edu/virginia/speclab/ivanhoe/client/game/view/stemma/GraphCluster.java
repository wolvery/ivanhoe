/*
 * Created on Jul 28, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.view.stemma;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Iterator;

import edu.virginia.speclab.ivanhoe.shared.blist.BLinkedList;
import edu.virginia.speclab.ivanhoe.shared.blist.BList;

/**
 * @author benc
 */
public class GraphCluster extends
        GraphObject {

    private final static float SIBLING_NODE_PADDING = 32f;
    private final static float PARENT_NODE_PADDING = StemmaView.VERTICAL_TICK_SPACING;
    
    private final StemmaNode mainNode;
    private final BList children;
    private float siblingWidth;

    GraphCluster(StemmaNode mainNode) {
        this.mainNode = mainNode;
        this.children = new BLinkedList();

        this.setDimensions(getMainNode().getDimensions());
    }

    private void setParent(GraphCluster parent) {
        // get vertical offset in relation to the parent.
        float parentOffset = (parent == null ? 0 : 
            parent.getMainNode().getDocumentVersionRank());
        float nodeOffset = getMainNode().getDocumentVersionRank();
        
        float verticalOffset = (nodeOffset - parentOffset ) * PARENT_NODE_PADDING;
        
        this.setLocation(getLocation().x, verticalOffset);
    }
    
    void addChild(GraphCluster childCluster) {
        children.add(childCluster);
        childCluster.setParent(this);
    }

    StemmaNode getMainNode() {
        return mainNode;
    }
    
    BList getChildren() {
        return (BList) children.createCopy();
    }
    
    private void resetBoundingBox() {
        this.getBoundingBox().setRect(-16, -16, 32, 32);
    }
    
    void layoutChildren() {
        this.resetBoundingBox();
        siblingWidth = 0.0f;
        
        for (Iterator i=children.iterator(); i.hasNext(); ) {
            if (siblingWidth != 0.0f) {
                siblingWidth += SIBLING_NODE_PADDING;
            }
            
            final GraphCluster gCluster = (GraphCluster) i.next();
            final Dimension nodeDimensions = gCluster.getDimensions();
            gCluster.setLocation(siblingWidth, gCluster.getLocation().y);
            siblingWidth += nodeDimensions.width;
        }
    }
    
    void centerChildren() {
        final float horizOffset = (siblingWidth / -2.0f)
                + (this.getMainNode().getDimensions().width / 2.0f);
        
        for (Iterator i=children.iterator(); i.hasNext(); ) {
            // We're in local space here
            final GraphCluster gCluster = (GraphCluster) i.next();
            final Point2D.Float location = gCluster.getLocation();
            gCluster.setLocation(location.x + horizOffset, location.y);

            final BoundingBox bBox = (BoundingBox) gCluster.getBoundingBox().clone();
            final Point2D.Float offset = gCluster.getLocation();
            bBox.offset(offset);
            this.getBoundingBox().add(bBox);
        }
    }
    
    public String toString() {
        return "GraphCluster [main node:"+mainNode+"]";
    }
    
    static class ConstructionOrderException extends
            RuntimeException {

        ConstructionOrderException(GraphCluster gc, StemmaNode sn) {
            super("GraphCluster ["+gc+"] has child ["+sn+"] with no corresponding"
                    +" GraphCluster.  The clusters at the bottom of the tree must"
                    +" be constructed first.");
        }
    }
}
