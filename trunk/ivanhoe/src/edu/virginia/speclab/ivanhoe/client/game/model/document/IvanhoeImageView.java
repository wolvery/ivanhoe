/*
 * Created on Nov 22, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.model.document;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.ImageView;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;

/**
 * @author benc
 */
public class IvanhoeImageView extends ImageView
{
    protected final static Color SHOW_DELETE_COLOR = new Color(1.0f, 1.0f, 1.0f, 0.8f);
    
    protected boolean isDeleted;
    
    public IvanhoeImageView(Element elem)
    {
        super(elem);
        setDeletedStatus(elem);
    }
    
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f)
    {
        super.changedUpdate(e,a,f);
        setDeletedStatus(super.getElement());
    }
    
    public float getPreferredSpan(int axis)
    {
        float span;
        
        if (isDeleted && !showDeletes())
        {
            span = 0.0f;
        }
        else
        {
            span = super.getPreferredSpan(axis);
        }
        
        return span;
    }
    
    public void paint(Graphics g, Shape a)
    {
        if (!isDeleted)
        {
            super.paint(g,a);
        }
        else if (showDeletes())
        {
            super.paint(g,a);
            
            Graphics2D g2 = Ivanhoe.getGraphics2D(g);
            g2.setColor(SHOW_DELETE_COLOR);
            g2.fill(a);
        } 
        else
        {
            // no-op
        }
    }
    
    protected boolean showDeletes()
    {
        IvanhoeDocument doc = (IvanhoeDocument)this.getDocument();
        return doc.areDeleteTagsVisible();
    }
    
    private void setDeletedStatus(Element elem)
    {
        AttributeSet aset = elem.getAttributes();
        Object deleteAttribsObj = aset.getAttribute(IvanhoeTag.DELETE);
        isDeleted = (deleteAttribsObj != null);
    }
}
