/*
 * Created on Jul 20, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors;

import java.awt.Color;

import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


class PreviewBox extends JPanel implements ChangeListener
{
    private JColorChooser chooser;
    private ColorArc colorArc;
    private boolean fillColorModifier;
    
    public PreviewBox( Color fillColor, Color strokeColor, JColorChooser chooser )
    {
        this.chooser = chooser;
        this.colorArc = new ColorArc(fillColor, strokeColor);
        fillColorModifier = true;
        add( new JLabel("example arc", colorArc, SwingConstants.CENTER));            
        setSize(20,20);
    }
    
    public void stateChanged(ChangeEvent event)
    {            
        if (!fillColorModifier)
        {
        	colorArc.strokeColor = chooser.getColor();
        }
        else
        {
        	colorArc.fillColor = chooser.getColor();
        }
    }
    
    public void setFillModifier()
    {
    	fillColorModifier = true;
    }
    
    public void setStrokeModifier()
    {
    	fillColorModifier = false;
    }
    
    public void setColors(Color fillColor, Color strokeColor)
    {
    	colorArc.fillColor = fillColor;
    	colorArc.strokeColor = strokeColor;
    }
}