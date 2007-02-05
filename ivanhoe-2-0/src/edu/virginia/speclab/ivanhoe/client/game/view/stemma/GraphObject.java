/*
 * Created on Jul 28, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.view.stemma;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author benc
 */
public abstract class GraphObject {
    
    private final BoundingBox boundingBox;
    private final Point2D.Float location;
    private boolean isWorldSpace;
    
    GraphObject() {
        this.boundingBox = new BoundingBox();
        this.location = new Point2D.Float();
        this.isWorldSpace = false;
    }
    
    final BoundingBox getBoundingBox() {
        return boundingBox;
    }
    
    final Point2D.Float getLocation()
    {
        return location;
    }
    
    final Dimension getDimensions() {
        return new Dimension(
                (int) Math.ceil(boundingBox.width),
                (int) Math.ceil(boundingBox.height));
    }
    
    final void setLocation(float x, float y) {
        location.setLocation(x, y);
    }
    
    final void setLocation(Point2D.Float pt) {
        location.setLocation(pt);
    }

    void setDimensions(Dimension dim) {
        boundingBox.setDimensions(dim);
    }
    
    void setDimensions(float w, float h) {
        boundingBox.setRect(boundingBox.x, boundingBox.y, w, h);
    }
    
    final GraphTranslation getTranslation() {
        return new GraphTranslation(location.x, location.y);
    }
    
    final boolean isWorldSpace() {
        return isWorldSpace;
    }
    
    final void setWorldSpace(boolean isWorldSpace) {
        this.isWorldSpace = isWorldSpace;
    }
    
    static class BoundingBox extends
            Rectangle2D.Float {

        BoundingBox() {
            super();
        }
        
        BoundingBox(float x, float y, float w, float h) {
            super(x,y,w,h);
        }
        
        BoundingBox(Point2D.Float offset, Dimension bounds) {
            this(offset.x, offset.y, bounds.width, bounds.height);
        }
        
        BoundingBox(Point2D.Float cornerA, Point2D.Float cornerB) {
            this(cornerA.x, cornerA.y, cornerB.x, cornerB.y);
        }
    
        BoundingBox offset(Point2D.Float offset) {
            this.x += offset.x;
            this.y += offset.y;
            return this;
        }
        
        BoundingBox setDimensions(Dimension bounds) {
            this.width = bounds.width;
            this.height = bounds.height;
            return this;
        }
    }
}
