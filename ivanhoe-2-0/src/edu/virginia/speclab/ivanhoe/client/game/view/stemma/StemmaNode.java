/*
 * Created on Jul 28, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.view.stemma;

import java.awt.Color;
import java.awt.Dimension;

import edu.virginia.speclab.ivanhoe.shared.blist.BArrayList;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;


/**
 * @author benc
 */
public class StemmaNode {
    
    private static final Dimension NODE_DIMENSIONS = new Dimension(32, 32);
    private final DocumentVersion documentVersion;
    private final int documentVersionRank;
    private final StemmaNode parentNode;
    private final BArrayList children;
    private Color fillColor, strokeColor;
    private boolean selected;
    private boolean highlighted;
    
    StemmaNode(DocumentVersion documentVersion, int documentVersionRank, StemmaNode parentNode,
            Color fillColor, Color strokeColor) {
        this.documentVersion = documentVersion;
        this.parentNode = parentNode;
        this.children = new BArrayList();
        this.fillColor = fillColor;
        this.strokeColor = strokeColor;
        
        if (parentNode != null)
        {
            parentNode.add(this);
        }
        
        this.documentVersionRank = documentVersionRank;
    }
    
    Dimension getDimensions() {
        return (Dimension)NODE_DIMENSIONS.clone();
    }
    
    void add(StemmaNode childNode) {
        this.children.add(childNode);
    }
    
	/**
	 * @param removedNode
	 */
	boolean remove(StemmaNode removedNode) {
		return this.children.remove(removedNode);
	}
    
    DocumentVersion getDocumentVersion() {
        return documentVersion;
    }
    
    StemmaNode getParentNode() {
        return parentNode;
    }
    
    BArrayList getChildren() {
        return children;
    }

    boolean isRootNode() {
        return documentVersion.getParentID() == DocumentVersion.NO_PARENT_ID; 
    }
    
    final boolean isSelected() {
        return selected;
    }
    
    final void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    final int getDocumentVersionRank() {
        return documentVersionRank;
    }
    
    public String toString() {
        return "[StemmaNode for "+documentVersion+"]";
    }
    
    final Color getFillColor()
    {
        return fillColor;
    }
    
    final Color getStrokeColor()
    {
        return strokeColor;
    }
    
    final boolean isHighlighted()
    {
        return highlighted;
    }
    
    final void setHighlighted(boolean highlighted)
    {
        this.highlighted = highlighted;
    }

    public void setFillColor(Color fillColor)
    {
        this.fillColor = fillColor;
    }
    

    public void setStrokeColor(Color strokeColor)
    {
        this.strokeColor = strokeColor;
    }
}
