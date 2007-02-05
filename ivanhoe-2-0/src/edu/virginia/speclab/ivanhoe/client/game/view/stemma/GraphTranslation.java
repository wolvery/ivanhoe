/*
 * Created on Aug 1, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.view.stemma;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


/**
 * @author benc
 */
class GraphTranslation {
    private float dx, dy;
    
    GraphTranslation() {
        this(0,0);
    }
    
    GraphTranslation(float dx, float dy)
    {
        this.dx = dx;
        this.dy = dy;
    }
    
    float getDX() {
        return dx;
    }
    
    float getDY() {
        return dy;
    }
    
    void setDX(float dx) {
        this.dx = dx;
    }
    
    void setDY(float dy) {
        this.dy = dy;
    }

    GraphTranslation concatinate(GraphTranslation that) {
        return new GraphTranslation(this.dx + that.dx, this.dy += that.dy);
    }
    
    Rectangle2D translate(Rectangle2D rect) {
        return new Rectangle2D.Float(
                (float)rect.getX() + dx,
                (float)rect.getY() + dy,
                (float)rect.getWidth(),
                (float)rect.getHeight());
    }
    
    Point2D translate(Point2D pt) {
        return new Point2D.Float(
                (float)pt.getX() + dx,
                (float)pt.getY() + dy);
    }
    
    AffineTransform getAffineTransform() {
        return AffineTransform.getTranslateInstance(this.dx, this.dy);
    }
    
    public String toString() {
        return "(dx:"+dx+", dy:"+dy+")";
    }
}
