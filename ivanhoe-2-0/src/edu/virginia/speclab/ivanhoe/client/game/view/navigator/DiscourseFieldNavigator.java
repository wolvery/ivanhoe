/*
 * Created on Apr 22, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTime;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTimeline;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.IDiscourseFieldTimeListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.ITimeLineListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.document.InfoPanel;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.CustomColors;
import edu.virginia.speclab.ivanhoe.client.util.PropertiesManager;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.Role;

import javax.swing.JPanel;

/**
 * 
 * @author Nick Laiacona
 */
public class DiscourseFieldNavigator extends JPanel implements ITimeLineListener, IDiscourseFieldTimeListener
{    
    private DiscourseField discourseField;
    private InfoPanel infoPanel;
    private DiscourseFieldCircle discourseFieldCircle;
    private DiscourseFieldStateManager documentAreaManager;
    private ActionSelection actionSelection;
    private PlayerCircleFilter playerCircleFilter;
    private LabelManager labelManager;
    private boolean dirty;
    private MouseEvent lastMouseMotion;
    
    private double aspectHeight, aspectWidth;
    private Image image;
    
    private boolean mouseOver;
    
    private CustomColors customColors;
    
    public static final int WINDOW_OPTIMAL_SIZE_WIDTH = 1022;    
    public static final int WINDOW_OPTIMAL_SIZE_HEIGHT = 502;
    
    public DiscourseFieldNavigator( DiscourseField discourseField, PropertiesManager propertiesManager )
    {
        this.discourseField = discourseField;
        
        actionSelection = new ActionSelection(this);
        discourseFieldCircle = new DiscourseFieldCircle(this);
        playerCircleFilter = new PlayerCircleFilter(this);
        documentAreaManager = new DiscourseFieldStateManager(this);
                
        labelManager = new LabelManager(this);
        customColors = new CustomColors(propertiesManager);
        customColors.loadColors(propertiesManager);
        
        addMouseMotionListener( new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) 
            {    
                handleMouseMotion(e);
            }
        });
        
        addMouseListener( new MouseAdapter() {
            public void mousePressed(MouseEvent e) 
            {    
                handleMouseClick(e);
            }
        });
        
        addMouseListener( new MouseAdapter() {
        	public void mouseExited(MouseEvent e)
        	{
        	    mouseOver = false;
        	    
        	    if( infoPanel != null ) infoPanel.setVisible(false);
        	    
        		documentAreaManager.clearHighlight();
        	}

        	public void mouseEntered(MouseEvent e)
        	{
        	    mouseOver = true;
        		documentAreaManager.clearHighlight();
        	}

        });

        this.addComponentListener( new ComponentAdapter()
           {  
              public void componentResized(ComponentEvent arg0)
              {
                 handleWindowResize();
              }
           });

    }
   
    public void init()
    {    	        
    	DiscourseFieldTimeline discourseFieldTimeline = discourseField.getDiscourseFieldTimeline();
        discourseFieldTimeline.addListener(this);
        this.documentAreaManager.init();
 
   		// Add all moves to timeline
        List playerList = discourseField.getPlayerList();
        for( Iterator i = playerList.iterator(); i.hasNext(); )
        {
            String playerName = (String) i.next();            
            Role role = discourseField.getRoleManager().getRole(playerName);
            SimpleLogger.logInfo("Got role: "+role.getName());
            discourseFieldCircle.addPlayer(role);
            SimpleLogger.logInfo("Added role: "+role.getName());
        }
        
        // start listening for changes in the current time 
        discourseField.getDiscourseFieldTime().addListener(this);
    }
    
    public void addListener( IDiscourseFieldNavigatorListener listener )
    {
        this.discourseFieldCircle.addListener(listener);
    }

    public void removeListener( IDiscourseFieldNavigatorListener listener )
    {
        this.discourseFieldCircle.removeListener(listener);
    }
    
    private void moveToTick( int tick )
    {
    	documentAreaManager.moveToTick(tick);
    	discourseFieldCircle.moveToTick(tick);
    }
        
    private void handleMouseMotion( MouseEvent e )
    {
    	lastMouseMotion = e;
    	Point p = e.getPoint();
    	
    	// reset some state
    	this.setCursor(Cursor.getDefaultCursor());
    	
    	if( infoPanel != null )
    	{
    	    infoPanel.setVisible(false);
    	}
    	
    	if( mouseOver )
    	{
	    	if ( discourseFieldCircle.testHighlight(p) ) 
	    	{
	    		return;
	    	}
	    	
	    	if ( playerCircleFilter.testHighlight(p) )
	    	{
	    		documentAreaManager.clearHighlight();
	    		return;
	    	}
	    	
	    	documentAreaManager.testHighlight(p);
    	}
    	
    }
        
    private void handleMouseClick( MouseEvent e )
    {
    	Point p = e.getPoint();
    	int clickCount = e.getClickCount();
    	
		if ( discourseFieldCircle.testSelection(p, clickCount) ) 
		{
			return;
		}
		
		if ( playerCircleFilter.testSelection(p, clickCount) )
		{
			return;
		}
		
		documentAreaManager.testSelection(p, clickCount);
    }
    
    private void handleWindowResize()
    {
        Dimension size = this.getSize();
        aspectWidth = (double)size.width/(double)WINDOW_OPTIMAL_SIZE_WIDTH;
        aspectHeight = (double)size.height/(double)WINDOW_OPTIMAL_SIZE_HEIGHT;
        
        GraphicsEnvironment local = 
           GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice screen = local.getDefaultScreenDevice();
        GraphicsConfiguration conf = screen.getDefaultConfiguration();
        this.image = conf.createCompatibleImage(size.width, size.height);
        
        dirty();
    }
    
    /**
     * @return Returns the aspectHeight.
     */
    public double getAspectHeight()
    {
        return aspectHeight;
    }
    
    /**
     * @return Returns the aspectWidth.
     */
    public double getAspectWidth()
    {
        return aspectWidth;
    }

    public void render()
    {   
        if( dirty == true && this.image != null)
        {
        	if (lastMouseMotion != null)
        	{
        		// This may set dirty=true again, but we avoid an infinite loop
        		// by setting dirty=false at the end of this block.
        		handleMouseMotion(lastMouseMotion);
        	}
        	
            Graphics2D g2 = Ivanhoe.getGraphics2D(this.image.getGraphics());
            g2.setBackground(Color.black);
            Dimension size = this.getSize();

            g2.clearRect(0,0,size.width,size.height);
           
            documentAreaManager.paint(g2,aspectWidth,aspectHeight);
            discourseFieldCircle.paint(g2,aspectWidth,aspectHeight);
            labelManager.paint(g2, aspectWidth, aspectHeight);
            // paint this after we paint the labels so that none of its labels get painted
            playerCircleFilter.paint(g2, aspectWidth, aspectHeight);
            labelManager.clear();
            g2.dispose();
           
            dirty = false;
            repaint();
        }
    }
        
    public void paint(Graphics g)
    {
        if( g == null || this.image == null ) return;
        
        Graphics2D g2 = Ivanhoe.getGraphics2D(g);
        g2.drawImage(this.image,0,0,this);
    }
    
    /**
     * @return Returns the discourseFieldCircle.
     */
    public DiscourseFieldCircle getDiscourseFieldCircle()
    {
        return discourseFieldCircle;
    }
    
    /**
     * @return Returns the documentAreaManager.
     */
    public DiscourseFieldStateManager getDocumentAreaManager()
    {
        return documentAreaManager;
    }
    
    public void dirty()
    {
        this.dirty = true;
    }

	/* (non-Javadoc)
	 * @see edu.virginia.speclab.ivanhoe.client.model.event.IDiscourseFieldListener#moveAddedToHistory(edu.virginia.speclab.ivanhoe.shared.data.Move)
	 */
	public void moveAddedToHistory( MoveEvent event )
	{
	    // update the document area 
		documentAreaManager.moveAddedToHistory(event);
		
		// update the discourse field circle
		discourseFieldCircle.recalculatePaths();
		
		// add the move to the discourse field circle
	    discourseFieldCircle.addMove(event);	    
	}

	/* (non-Javadoc)
	 * @see edu.virginia.speclab.ivanhoe.client.model.event.IDiscourseFieldListener#addNewDocument(edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo)
	 */
	public void addNewDocument(DocumentInfo docInfo)
	{
		documentAreaManager.addNewDocument(docInfo);
		discourseFieldCircle.recalculatePaths();	
	}
	
	public void removeDocument(DocumentInfo docInfo)
	{
		documentAreaManager.removeDocument(docInfo);
		discourseFieldCircle.recalculatePaths();		
	}

    /**
     * @return Returns the discourseFieldTimeline.
     */
    public DiscourseFieldTimeline getDiscourseFieldTimeline()
    {
        return discourseField.getDiscourseFieldTimeline();
    }
    
    /**
     * Get the user name of the currently selected player in the navigator.
     * @return The selected user's username
     */
    public String getSelectedPlayer()
    {
        if( this.discourseFieldCircle != null )
        {
            PlayerCircle selectedCircle = this.discourseFieldCircle.getSelectedPlayerCircle();
            if( selectedCircle != null )
            {
                return selectedCircle.getRoleName();
            }
        }
        
        return null;
    }

    /**
     * Select the player with the given name if the name is valid.
     * @param role The role to select.
     */
    public void setSelectedRole( Role role )
    {
        if( role != null && this.discourseFieldCircle != null )
        {
            PlayerCircle selectedCircle = this.discourseFieldCircle.getPlayerCircleByName(role.getName());
            if( selectedCircle != null )
            {
                this.discourseFieldCircle.selectPlayerCircle(selectedCircle);
            }
        }
    }
    
    /**
     * @return Returns the customColors.
     */
    public CustomColors getCustomColors()
    {
        return customColors;
    }
    
    public LabelManager getLabelManager()
    {
    	return labelManager;
    }
    
    /**
     * @return Returns the current discourse field time in ticks.
     */
    public double getTick()
    {
        return discourseField.getDiscourseFieldTime().getTick();
    }

    /**
     * @param infoPanel The infoPanel to use for popup messages in this navigator.
     */
    public void setInfoPanel(InfoPanel infoPanel)
    {
        this.infoPanel = infoPanel;
    }
    
    /**
     * @return Returns the infoPanel used to display popups in this navigator.
     */
    public InfoPanel getInfoPanel()
    {
        return infoPanel;
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.model.discourse.IDiscourseFieldTimeListener#discourseFieldTickChanged(double)
     */
    public void discourseFieldTickChanged(int tick)
    {
        moveToTick(tick);        
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.model.discourse.IDiscourseFieldTimeListener#discourseFieldMoveEventChanged(edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent)
     */
    public void discourseFieldMoveEventChanged(MoveEvent event)
    {
        if( event == null ) return;        
        moveToTick(event.getTick());
    }
    
    public PlayerCircleFilter getPlayerCircleFilter() 
	{
		return playerCircleFilter;
	}
    /**
     * @return Returns the discourseFieldTime.
     */
    public DiscourseFieldTime getDiscourseFieldTime()
    {
        return discourseField.getDiscourseFieldTime();
    }
    /**
     * @return Returns the discourseField.
     */
    public DiscourseField getDiscourseField()
    {
        return discourseField;
    }
    /**
     * @return Returns the roleManager.
     */
    public RoleManager getRoleManager()
    {
        return discourseField.getRoleManager();
    }
    
    public ActionSelection getActionSelection()
    {
        return actionSelection;
    }
}
