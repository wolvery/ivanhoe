/*
 * Recreated on Oct 13, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author benc
 */
public class ColorPairView extends JPanel implements ChangeListener
{   
	private Color startingFillColor, startingStrokeColor;
	private Color fillColor, strokeColor;
	private LinkedList listeners;
	private boolean fillModifier;
	private final int colorArc;
	
	private JColorChooser colorChooser;
	private PreviewBox previewBox;
	
	public ColorPairView(String title, ColorPair initialColors, IColorPairViewListener listener, int colorArc) {
		super(new BorderLayout());
		this.colorArc = colorArc;
		
		this.listeners = new LinkedList();
		this.addListener(listener);
		
		fillColor = this.startingFillColor = initialColors.fillColor;
		strokeColor = this.startingStrokeColor = initialColors.strokeColor;
		fillModifier = true;
		
		setBorder(new TitledBorder(title));
		
		colorChooser = new JColorChooser(startingFillColor);
		colorChooser.getSelectionModel().addChangeListener(this);
		AbstractColorChooserPanel colorChooserPanels[] = {new ColorPalette()};
        colorChooser.setChooserPanels(colorChooserPanels);
        
		// setup the preview panel
		previewBox = new PreviewBox(startingFillColor, startingStrokeColor, colorChooser);
        colorChooser.setPreviewPanel(previewBox);
        colorChooser.getSelectionModel().addChangeListener(previewBox);
        
        // top of the screen buttons
        JToggleButton fillColorButton = new JToggleButton("fill color");
        JToggleButton strokeColorButton = new JToggleButton("stroke color");
        fillColorButton.addActionListener(getFillColorButtonListener());
        strokeColorButton.addActionListener(getStrokeColorButtonListener());
        ButtonGroup topButtonGroup = new ButtonGroup();
        topButtonGroup.add(fillColorButton);
        topButtonGroup.add(strokeColorButton);
        fillColorButton.doClick();
        JPanel topButtons = new JPanel();
        topButtons.add(fillColorButton);
        topButtons.add(strokeColorButton);
        
        // bottom of the screen buttons
        JButton okButton = new JButton("ok");
        JButton applyButton = new JButton("apply");
        JButton resetButton = new JButton("reset");
        JButton cancelButton = new JButton("cancel");
        
        okButton.addActionListener(getOkButtonListener());
        applyButton.addActionListener(getApplyButtonListener());
        resetButton.addActionListener(getResetButtonListener());
        cancelButton.addActionListener(getCancelButtonListener());
        
        okButton.setSize(okButton.getPreferredSize());
        applyButton.setSize(applyButton.getPreferredSize());
        resetButton.setSize(resetButton.getPreferredSize());
        cancelButton.setSize(cancelButton.getPreferredSize());
        
        JPanel bottomButtons = new JPanel();
        bottomButtons.add(okButton);
        bottomButtons.add(applyButton);
        bottomButtons.add(resetButton);
        bottomButtons.add(cancelButton);
        
        
        add(topButtons, BorderLayout.PAGE_START);
        add(colorChooser, BorderLayout.CENTER);
		add(bottomButtons, BorderLayout.PAGE_END);
	}
	
	private ActionListener getOkButtonListener()
    {
    	return new ActionListener()
			{
				public void actionPerformed(ActionEvent a)
				{
					fireOk();
				}
			};
    }
    
    private ActionListener getCancelButtonListener()
    {
    	return new ActionListener()
			{
				public void actionPerformed(ActionEvent a)
				{
					getResetButtonListener().actionPerformed(a);
					fireCancel();
				}
			};
    }

	
	private ActionListener getFillColorButtonListener()
    {
    	return new ActionListener()
			{
				public void actionPerformed(ActionEvent a)
				{
					fillModifier = true;
					previewBox.setFillModifier();
					colorChooser.setColor(fillColor);
				}
			};
    }
	
	private ActionListener getStrokeColorButtonListener()
    {
    	return new ActionListener()
			{
				public void actionPerformed(ActionEvent a)
				{
					fillModifier = false;
					previewBox.setStrokeModifier();
					colorChooser.setColor(strokeColor);
				}
			};
    }
	
    private ActionListener getApplyButtonListener()
    {
    	return new ActionListener()
			{
				public void actionPerformed(ActionEvent a)
				{
					fireApply();
				}
			};
    }
    
    private ActionListener getResetButtonListener()
    {
    	return new ActionListener()
			{
				public void actionPerformed(ActionEvent a)
				{
					if (fillModifier)
					{
						setSelectedColor(startingFillColor);
					}
					else
					{
						setSelectedColor(startingStrokeColor);
					}

					previewBox.setColors(startingFillColor, startingStrokeColor);
					
					fillColor = startingFillColor;
					strokeColor = startingStrokeColor;
				}
			};
    }
    
    private void setSelectedColor(Color newColor)
    {
    	colorChooser.getChooserPanels()[0].getColorSelectionModel()
				.setSelectedColor(newColor);
    }
    
    private void fireOk()
    {
        for (Iterator i=listeners.iterator(); i.hasNext(); )
        {
            ((IColorPairViewListener)i.next()).handleOk(this);
        }
    }
    
    private void fireCancel()
    {
        for (Iterator i=listeners.iterator(); i.hasNext(); )
        {
            ((IColorPairViewListener)i.next()).handleCancel(this);
        }
    }
    
    private void fireApply()
    {
        for (Iterator i=listeners.iterator(); i.hasNext(); )
        {
            ((IColorPairViewListener)i.next()).handleApply(this);
        }
    }
    
    public void stateChanged(ChangeEvent event)
    {            
        if (!fillModifier)
        {
        	strokeColor = colorChooser.getColor();
        }
        else
        {
        	fillColor = colorChooser.getColor();
        }
    }
    
    public void addListener(IColorPairViewListener listener)
    {
        if (!listeners.contains(listener)) listeners.add(listener);
    }
    
    public boolean removeListener(IColorPairViewListener listener)
    {
        return listeners.remove(listener);
    }

    public ColorPair getColorPair()
    {
        return new ColorPair(this.strokeColor, this.fillColor);
    }
    
    public int getArcType()
    {
        return colorArc;
    }
}
