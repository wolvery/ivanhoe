/*
 * Created on Jan 6, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.PlayerCircle;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.ColorPair;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.CustomColors;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.CustomColorsWindow;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.IColorModelUpdateListener;

/**
 * @author benc
 */

abstract class AbstractRolePanel extends JPanel
{
    protected final RoleManager manager;
    protected final boolean isEditable;
    protected final ActionListener okButtonListener;
    
    public final static String USE_ROLE_TEXT = "use this role";
    
    protected JTextArea roleDescription;
    protected JTextArea roleObjective;
    protected CustomColorsWindow colorPicker;
    protected ColorPreviewPanel colorPreviewPanel;
    protected JButton okButton;
    protected JPanel namePanel;
    protected JPanel buttonPanel;
    
    protected AbstractRolePanel(RoleManager manager, boolean isEditable, ActionListener okButtonListener)
    {
        super();
        this.manager = manager;
        this.isEditable = isEditable;
        this.okButtonListener = okButtonListener;
    }
    
    
    /**
     * This method must be called after getRoleColors() returns a valid result
     */
    protected final void initUI()
    {
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setBorder(new EmptyBorder(12,8,8,8));
        
        namePanel = new JPanel();
        namePanel.setBorder(new EmptyBorder(4,8,4,8));
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
        namePanel.add(new JLabel("role name (public): "));
        this.add(namePanel);
        
        this.add(this.createCenterPanel(isEditable));
        
        okButton = new JButton(USE_ROLE_TEXT);
        okButton.setFont(IvanhoeUIConstants.SMALL_FONT);

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                acceptRole();
            }
        });

        
        if (okButtonListener != null)
        {
            okButton.addActionListener(okButtonListener);
        }
        
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createGlue());
        buttonPanel.add(okButton);
        
        this.add(buttonPanel);
        
        manager.addColorUpdateListener(colorPreviewPanel);
    }
    
    protected JPanel createCenterPanel(boolean isEditable)
    {
        JPanel infoPanel = createInfoPanel(isEditable);
        JPanel colorPanel = createColorPanel(isEditable);
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel,BoxLayout.X_AXIS));
        
        centerPanel.add(infoPanel);
        centerPanel.add(colorPanel);
        
        return centerPanel;
    }
    
    protected JPanel createInfoPanel(boolean isEditable)
    {
        JPanel infoPanel = new JPanel(new BorderLayout());
        
        infoPanel.setBorder(new TitledBorder(
           new LineBorder(Color.GRAY, 1),
           "role information (private)",
           TitledBorder.LEFT,
           TitledBorder.DEFAULT_POSITION,
           IvanhoeUIConstants.BOLD_FONT));
        
        JPanel top = new JPanel(new BorderLayout());
        
        // create role desc
        JPanel descPnl = new JPanel( new BorderLayout());
        JLabel l2 = new JLabel("description:");
        l2.setFont(IvanhoeUIConstants.SMALL_FONT);
        descPnl.add(l2, BorderLayout.NORTH);
        
        roleDescription = new JTextArea();
        roleDescription.setWrapStyleWord(true);
        roleDescription.setLineWrap(true);
        roleDescription.setFont(IvanhoeUIConstants.SMALL_FONT);
        JScrollPane descSp = new JScrollPane(roleDescription);
        descSp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descSp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        descPnl.add(descSp, BorderLayout.CENTER);
        roleDescription.setEditable(isEditable);
        
        // create role objectives
        JPanel objPnl = new JPanel( new BorderLayout());
        JLabel l3 = new JLabel("objectives:");
        l3.setFont(IvanhoeUIConstants.SMALL_FONT);
        objPnl.add(l3, BorderLayout.NORTH);
        
        roleObjective = new JTextArea();
        roleObjective.setFont(IvanhoeUIConstants.SMALL_FONT);
        roleObjective.setWrapStyleWord(true);
        roleObjective.setLineWrap(true);
        JScrollPane objSp = new JScrollPane(roleObjective);
        objSp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        objSp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        objPnl.add(objSp, BorderLayout.CENTER);
        roleObjective.setEditable(isEditable);
        
        // glom obj & desc into one panel
        JPanel glomfulness = new JPanel (new GridLayout(2,1,5,5));
        glomfulness.add(descPnl);
        glomfulness.add(objPnl);
        
        // add role stuff to content
        top.add(glomfulness, BorderLayout.CENTER);
        
        infoPanel.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        infoPanel.add(top, BorderLayout.CENTER);
        infoPanel.setPreferredSize(new Dimension(400,400));
        
        return infoPanel;
    }
    
    abstract protected void acceptRole();
    
  private JPanel createColorPanel(boolean isEditable)
  {
      JPanel circlePanel = new JPanel();
      circlePanel.setLayout(new BoxLayout(circlePanel, BoxLayout.PAGE_AXIS));
      circlePanel.setBorder( new TitledBorder( 
              new LineBorder(Color.GRAY, 1), "role colors"));
      
      colorPreviewPanel = new ColorPreviewPanel(getRoleColors());
      circlePanel.add(colorPreviewPanel);
      
      if (isEditable)
      {
          JButton editColorsButton = new JButton("edit colors");
          ActionListener editColorsListener = new ActionListener()
          {
              public void actionPerformed(ActionEvent ae)
              {
                  Workspace.instance.openColorsWindow(AbstractRolePanel.this.getRoleName(),getRoleColors());
              }
          };
          editColorsButton.addActionListener(editColorsListener);

          JPanel buttonPanel = new JPanel();
          buttonPanel.add(editColorsButton);
          
          circlePanel.add(buttonPanel);
      }
      
      circlePanel.add(Box.createVerticalGlue());
      
      return circlePanel;
  }

  protected void removeListeners()
  {
      manager.removeColorUpdateListener(colorPreviewPanel);
  }

  abstract protected ColorPair getRoleColors();
  
  abstract protected boolean roleMatches(String roleName);
  
  abstract protected String getRoleName();

	protected class ColorPreviewPanel extends JPanel implements IColorModelUpdateListener
	{
	    private static final int PREVIEW_AREA_SIZE = 100;
	    private final Component PREVIEW_AREA = Box.createRigidArea(new Dimension(PREVIEW_AREA_SIZE, PREVIEW_AREA_SIZE));
	    private static final float STROKE_RATIO = 
	        PlayerCircle.UNSELECTED_STROKE / ((float)PlayerCircle.OPTIMAL_RADIUS * 2);
	    private static final int MARGIN = 8;
	    
	    private ColorPair colors;
	    
	    public ColorPreviewPanel(ColorPair colors)
	    {
	        super();
	        
	        this.colors = colors;
	        
	        this.setBorder(new LineBorder(Color.GRAY, 1));
	        this.setBackground(Color.BLACK);
	        this.add(PREVIEW_AREA);
	        
	        Insets insets = this.getInsets();
	        Dimension maximumSize = new Dimension(
	                PREVIEW_AREA_SIZE + insets.left + insets.right, 
	                PREVIEW_AREA_SIZE + insets.top + insets.bottom);
	        this.setMaximumSize(maximumSize);
	    }
	    
	    public ColorPair getColors()
	    {
	        return colors;
	    }
	    
	    public void setColors(ColorPair colors)
	    {
	        this.colors = colors;
	    }
	    
	    public void paint(Graphics g)
	    {
	        super.paint(g);
	        
	        Graphics2D previewG2 = Ivanhoe.getGraphics2D(g);
	        
	        int circleX = PREVIEW_AREA.getX() + MARGIN;
	        int circleY = PREVIEW_AREA.getY() + MARGIN;
	        int circleW = PREVIEW_AREA.getWidth() - 2*MARGIN;
	        int circleH = PREVIEW_AREA.getHeight() - 2*MARGIN;
	        
	        // XXX: reenable this assert
	        // assert circleW == circleH;
	        
	        Ellipse2D circle = new Ellipse2D.Float(
	                circleX, circleY, circleW, circleH);
	        
	        previewG2.setColor(colors.fillColor);
	        previewG2.fill(circle);
	        
	        previewG2.setStroke(new BasicStroke(circleW * STROKE_RATIO));
	        previewG2.setColor(colors.strokeColor);
	        previewG2.draw(circle);
	    }
	
	    public void updateColorModel(String roleName, ColorPair colors, int arcType)
	    {
	        if (AbstractRolePanel.this.roleMatches(roleName) 
	                && arcType == CustomColors.PLAYER_CIRCLE)
	        {
	            this.setColors(colors);
	            this.repaint();
	        }
	        
	    }
	}
}