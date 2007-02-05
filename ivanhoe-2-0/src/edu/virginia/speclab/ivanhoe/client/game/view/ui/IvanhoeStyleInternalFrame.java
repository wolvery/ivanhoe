/*
 * Created on May 18, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.beans.PropertyVetoException;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.view.document.InfoPanel;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;

/**
 * 
 * @author Nick Laiacona
 */
public class IvanhoeStyleInternalFrame extends JInternalFrame
{
	private final static float TEXT_BACKGROUND_BRIGHTNESS_LIMIT = 0.55f;
	
    protected IvanhoeUI editorUI;
    protected IvanhoeBorder editorBorder;
    protected InfoPanel infoPanel; 
    
    public void applyTint(int hexTintVal)
    {
       int borderColor = editorBorder.getRGB();
       borderColor = borderColor | hexTintVal;
       editorBorder.setColor(new Color(borderColor).darker());
       IvanhoeTitlePane titlePane = ((IvanhoeUI)getUI()).getTitlePane();
       int titleColor = titlePane.getRGB();
       titleColor = titleColor | (hexTintVal/2);
       titlePane.setColor(new Color(titleColor).darker());
    }
    
    public void setTitleColor( Color titleColor )
    {
		IvanhoeTitlePane titlePane = ((IvanhoeUI)getUI()).getTitlePane();
		titlePane.setColor(titleColor);
    }


    /**
     * Custom UI for editor. It replaces the stock title panel
     * with a custom panel
     */
    protected class IvanhoeUI extends BasicInternalFrameUI
    {
       public IvanhoeUI(JInternalFrame frame)
       {
          super(frame);
       }
       
       public void installUI(JComponent c) 
       {
          super.installUI(c);
          JComponent content = (JComponent)frame.getContentPane();
          content.setBorder(new EtchedBorder());
        }
       
       protected JComponent createNorthPane(JInternalFrame w) 
       {
          titlePane = new IvanhoeTitlePane(w);
          return titlePane;
        }
       
       public IvanhoeTitlePane getTitlePane()
       {
          return (IvanhoeTitlePane)this.titlePane;
       }
    }


 /**
     * Custom border for editor frames
     * Simple border with rounded corners
     */
    protected class IvanhoeBorder extends EmptyBorder
    {
       private Color borderColor;
       
       public IvanhoeBorder()
       {
          super(10,4,4,2);
          borderColor = IvanhoeUIConstants.DARKEST_GRAY;
       }
       
       public void paintBorder(Component c, Graphics g,  
          int x,  int y, int width,  int height) 
       {
          Graphics2D g2d = Ivanhoe.getGraphics2D(g);

          g2d.setColor(IvanhoeUIConstants.DARKEST_GRAY);          
          g2d.fillRect(x,y,width,height);
          g2d.setColor(IvanhoeUIConstants.BLACK);
          g2d.drawRect(x,y,width,height);
          g2d.drawRect(x+1,y+1,width-2,height-2);
       }
       
    /**
     * @param borderColor The borderColor to set.
     */
    public void setColor(Color color)
    {
        this.borderColor = color;
    }
    
    /**
     * @return Returns the borderColor.
     */
    public int getRGB()
    {
        return borderColor.getRGB();
    }
    }

 /**
     * Custom title pane for the editor. 
     */
    protected class IvanhoeTitlePane extends BasicInternalFrameTitlePane
    {  
       private Color titleColor, titleTextColor;
       private JPanel buttonPanel;
       private Icon lightMax, lightMin, lightClose, darkMax, darkMin, darkClose;
       
       public IvanhoeTitlePane(JInternalFrame f)
       {
          super(f);
          setColor(IvanhoeUIConstants.titleColor);
       }
       
       protected void assembleSystemMenu() {}
       
       protected void addSubComponents()
       {
          if (buttonPanel != null) remove(buttonPanel);
       	  buttonPanel = new JPanel();
          buttonPanel.setOpaque(false);
          
          //TODO make these work properly or remove them
//       	  buttonPanel.add(maxButton);
//       	  buttonPanel.add(iconButton);
       	  buttonPanel.add(closeButton);

          add(buttonPanel, BorderLayout.EAST);
       }
       
       public void paintComponent(Graphics g)
       {
          Graphics2D g2d = Ivanhoe.getGraphics2D(g);
          
          g2d.setColor(this.titleColor);
          g2d.fillRect(0,0,getWidth()-1,getHeight()-3);
          g2d.setColor(this.titleColor);
          g2d.drawRect(0,0,getWidth()-1,getHeight()-3);
          
          g2d.setColor(titleTextColor);
          g2d.setFont(IvanhoeUIConstants.BOLD_FONT);
          g2d.drawString(frame.getTitle(), 6, getHeight()/2+5);
       }
       
       public void setColor()
       {
          float hsbColor[] = new float[3]; 
		  Color.RGBtoHSB(titleColor.getRed(), titleColor.getGreen(), titleColor.getBlue(), hsbColor);
		
	      if (hsbColor[2] < TEXT_BACKGROUND_BRIGHTNESS_LIMIT)
	      {
           titleTextColor = Color.WHITE;
           closeButton.setIcon(lightClose);
           maxButton.setIcon(lightMax);
           iconButton.setIcon(lightMin);
		  }
	      else
	      {
	      	 titleTextColor = Color.BLACK;
	      	 closeButton.setIcon(darkClose);
	      	 maxButton.setIcon(darkMax);
	      	 iconButton.setIcon(darkMin);
	      }
	      
	      this.addSubComponents();
       }
       
       public void setColor(Color color)
       {
          this.titleColor = color;
          this.setColor();
       }
       
       public int getRGB()
       {
          return this.titleColor.getRGB();
       }
       
       protected void installDefaults() 
       {
          super.installDefaults();
          this.minIcon = ResourceHelper.instance.getIcon("res/icons/restore.gif");
          
          lightClose = ResourceHelper.instance.getIcon("res/icons/lightclose.gif");
          lightMax   = ResourceHelper.instance.getIcon("res/icons/lightmax.gif");
          lightMin   = ResourceHelper.instance.getIcon("res/icons/lightmin.gif");
          
          darkClose = ResourceHelper.instance.getIcon("res/icons/darkclose.gif");
          darkMax   = ResourceHelper.instance.getIcon("res/icons/darkmax.gif");
          darkMin   = ResourceHelper.instance.getIcon("res/icons/darkmin.gif");
       }
       
       protected void setButtonIcons()
       {
          super.setButtonIcons();
          this.closeButton.setBorderPainted(false);
          this.closeButton.setContentAreaFilled(false);
          this.closeButton.setOpaque(false);
          this.maxButton.setBorderPainted(false);
          this.maxButton.setContentAreaFilled(false);
          this.iconButton.setBorderPainted(false);
          this.iconButton.setContentAreaFilled(false);
          this.iconButton.setOpaque(false);
       }
       
       protected LayoutManager createLayout()
       {
          return new BorderLayout();
       }
    }
    
    protected IvanhoeStyleInternalFrame( String title )
    {
        super(title, true, true, true, true);
        createUI();
    }

    protected IvanhoeStyleInternalFrame( String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable )
    {
        super(title, resizable, closable, maximizable, iconifiable);
        createUI();
    }

    /**
     * Create all of the UI components needed for the DocumentEditor
     */
    private void createUI()
    {
       // create custom l&f for the editor
       editorUI = new IvanhoeUI(this);
       setUI(editorUI);
       editorBorder = new IvanhoeBorder();
       setBorder(editorBorder);
       setOpaque(false);
       
       this.getContentPane().setLayout(new BorderLayout());     
       this.getContentPane().add(Box.createVerticalStrut(10), BorderLayout.WEST);
       this.getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.NORTH);       
    }
    
    protected void closeWindow()
    {
        try {
            setClosed(true);
        } catch (PropertyVetoException e1) {
            e1.printStackTrace();
        }       
    }
    
    public void setIcon(boolean b)
    {
    	try
		{
    		super.setIcon(b);
    		if (!b)
    		{
    			IvanhoeTitlePane titlePane = ((IvanhoeUI)getUI()).getTitlePane();
    			titlePane.setColor();
    		}
	}
    	catch (Exception e)
		{
    		SimpleLogger.logError("Exception while [de]iconifying window: "+e.getMessage());
		}
    }
}
