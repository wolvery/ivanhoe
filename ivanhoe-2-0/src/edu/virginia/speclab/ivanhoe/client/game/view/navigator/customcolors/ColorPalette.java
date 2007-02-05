/*
 * Created on Oct 11, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;

/**
 * @author benc
 */
public class ColorPalette extends AbstractColorChooserPanel
		implements ActionListener
{
	private static final int ICON_SIZE = 16;
	
	private static final float SATURATION_START = 0.4f;
	private static final float SATURATION_END = 0.8f;
	private static final int SATURATION_STEPS = 2;
	private static final float SATURATION_INCREMENT = (SATURATION_END-SATURATION_START) / (SATURATION_STEPS-1);
	
	private static final float BRIGHTNESS_START = 1.0f;
	private static final float BRIGHTNESS_END = 0.5f;
	private static final int BRIGHTNESS_STEPS = 3;
	private static final float BRIGHTNESS_INCREMENT = (BRIGHTNESS_END-BRIGHTNESS_START) / (BRIGHTNESS_STEPS-1);
	
	private static final int INKWELL_COLS = 12;
	private static final int INKWELL_ROWS = SATURATION_STEPS * BRIGHTNESS_STEPS;
	private static final Color INKWELLS[] = initInkwells(INKWELL_COLS, INKWELL_ROWS);
	
	
	private static Color[] initInkwells(int cols, int rows)
	{
		Color inkwells[] = new Color[cols * rows];
		// columns correspond to hue
		// rows correspond to saturation and brightness
		float hueIncrement = 1.0f / cols;
		
		// setup hue table 
		float hues[] = new float[cols];
		for (int i=0; i<cols; ++i)
		{
			hues[i] = i*hueIncrement;
		}
		
		// setup saturation table with redundancies
		float saturations[] = new float[rows];
		for (int i=0; i<SATURATION_STEPS; ++i)
		{
			float thisSaturation = SATURATION_START + i*SATURATION_INCREMENT;
			for (int j=0; j<BRIGHTNESS_STEPS; ++j)
			{
				saturations[i * BRIGHTNESS_STEPS + j] = thisSaturation;
			}
		}
		
		// setup brightness table with redundancies
		float brightnesses[] = new float[rows];
		for (int i=0; i<BRIGHTNESS_STEPS; ++i)
		{
			float thisBrightness = BRIGHTNESS_START + i*BRIGHTNESS_INCREMENT;
			for (int j=0; j<SATURATION_STEPS; ++j)
			{
				brightnesses[i + BRIGHTNESS_STEPS * j] = thisBrightness;
			}
		}
		
		// combine the three tables so that each color is unique
		for (int i=0; i<rows; ++i) 
		{
		    float saturation = saturations[i];
			float brightness = brightnesses[i];
			for (int j=0; j<cols; ++j)
			{
				float hue = hues[j];
				inkwells[j + i*cols] = new Color(Color.HSBtoRGB(hue, saturation, brightness));
			}
		}
		
		return inkwells;
	}
	
	private JButton createInkwell(Color c, Border border)
	{
		JButton inkwell = new JButton();
		inkwell.setActionCommand(c.getRGB()+"");
		inkwell.setSize(ICON_SIZE,ICON_SIZE);
		inkwell.setIcon(new InkwellIcon(c,ICON_SIZE,ICON_SIZE));
		inkwell.addActionListener(this);
		inkwell.setBorder(border);
		
		return inkwell;
	}
	
	protected void buildChooser() 
	{
		int rows = (int)Math.ceil(INKWELLS.length/(double)INKWELL_COLS);
		setLayout(new GridLayout(rows,INKWELL_COLS));
		setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
		ButtonGroup inkWellButtonGroup = new ButtonGroup();
		Border border = BorderFactory.createEmptyBorder(1,1,1,1);
		
		for (int i=0; i<INKWELLS.length; ++i)
		{
			JButton inkwell = createInkwell(INKWELLS[i], border);
			inkWellButtonGroup.add(inkwell);
			this.add(inkwell);
		}
	}

	public void updateChooser() 
	{

	}

	public String getDisplayName() 
	{
		return "Color Palette";
	}

	public Icon getLargeDisplayIcon()
	{
		return null;
	}

	public Icon getSmallDisplayIcon() 
	{
		return null;
	}

	public void actionPerformed(ActionEvent e)
	{
		String command = ((JButton)e.getSource()).getActionCommand();
		Color newColor = new Color(new Integer(command).intValue());
		getColorSelectionModel().setSelectedColor(newColor);
	}
	
	private class InkwellIcon implements Icon
	{
		private Color color;
		private int w, h;
		
		public InkwellIcon(Color color, int w, int h)
		{
			this.color = color;
			this.w = w;
			this.h = h;
		}
		
		public int getIconHeight() 
		{
			return h;
		}
		
		public int getIconWidth() 
		{
			return w;
		}
		
		public void paintIcon(Component c, Graphics g, int x, int y) 
		{
			Graphics2D g2 = Ivanhoe.getGraphics2D(g);
			g2.setPaint(color);
			g2.fillRect(x,y,w,h);
		}
	}
	
	public Color[] getInkwells()
	{
		return INKWELLS;
	}
}
