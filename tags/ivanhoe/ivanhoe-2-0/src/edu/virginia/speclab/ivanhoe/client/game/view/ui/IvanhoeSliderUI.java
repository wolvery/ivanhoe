/*
 * Created on Sep 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;
 
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;


public class IvanhoeSliderUI extends BasicSliderUI
{
    private Image scrollThumb; 
    
    public IvanhoeSliderUI( JSlider slider )
    {
    		super(slider);
        scrollThumb = ResourceHelper.getImage("res/icons/slider.gif");
    }
    
    public void paintThumb( Graphics g )
    {  
    	   thumbRect.width = scrollThumb.getWidth(null);
    	   thumbRect.height = scrollThumb.getHeight(null);
        g.drawImage(scrollThumb,this.thumbRect.x,this.thumbRect.y,null);
    }
}