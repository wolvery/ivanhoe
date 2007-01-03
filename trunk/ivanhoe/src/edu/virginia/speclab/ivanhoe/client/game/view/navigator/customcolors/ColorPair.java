/*
 * Created on Jul 20, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors;

import java.awt.Color;


public class ColorPair
{
    public Color strokeColor, fillColor;
    
    public ColorPair( Color stroke, Color fill )
    {
        strokeColor = stroke;
        fillColor = fill;
    }
    
    public ColorPair( String strokeString, String fillString )
    {
        int strokeRGB = Integer.parseInt(strokeString);
        int fillRGB =  Integer.parseInt(fillString);
        
        strokeColor = new Color(strokeRGB);
        fillColor = new Color(fillRGB);
    }        

}