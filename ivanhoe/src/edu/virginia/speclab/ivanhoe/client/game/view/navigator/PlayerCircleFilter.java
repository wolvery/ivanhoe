/*
 * Created on Oct 4, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.AffineTransform;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTime;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.document.InfoPanel;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.data.Move;

/**
 * @author benc
 */
public class PlayerCircleFilter extends Box implements IDiscourseFieldNavigatorListener
{
	private static final int OPTIMAL_TRAY_HEIGHT = 66;    
	public static final double PLAYERCIRCLE_MARGIN = 16.0;
	
	private static final int CURRENT_PLAYER_INDICATOR_X_OFFSET = 28;
	private static final int CURRENT_PLAYER_INDICATOR_Y_OFFSET = 6;
	
	private static final Image currentPlayerIndicator = ResourceHelper.getImage("res/icons/indicator.gif");

	private List circles;
	private DiscourseFieldNavigator navigator;
	
	public PlayerCircleFilter(DiscourseFieldNavigator navigator)
	{
		super(BoxLayout.Y_AXIS);
		this.navigator = navigator;
		navigator.addListener(this);
		circles = new LinkedList();
        setOpaque(false);
        setBorder( new EmptyBorder(0,0,0,0));		
	}
	
	public void paint(Graphics g, double aspectW, double aspectH)
	{
		if (circles.isEmpty())
		{
			return;
		}
		
		PlayerCircle pc = (PlayerCircle)(circles.iterator().next());
		
		// Count the number of visible circles to size the tray
		int circleNum = 0;
		for (Iterator i = circles.iterator(); i.hasNext();)
		{
			pc = (PlayerCircle)(i.next());
			if (pc.isVisible())
			{
				 ++circleNum;
			}
		}
		
        // if no circles to draw, done.
		if (circleNum <= 0) return;
		
		Area circleArea = pc.getCircleArea(aspectW, aspectH);
		
        int trayWidth = (int)((circleNum + 1) * 
                (PLAYERCIRCLE_MARGIN + circleArea.getBounds2D().getWidth() ));
        int trayHeight = (int)(2 * PLAYERCIRCLE_MARGIN + circleArea.getBounds2D().getHeight());
                
		if (trayHeight < OPTIMAL_TRAY_HEIGHT)
		{
			trayWidth = OPTIMAL_TRAY_HEIGHT;
		}
		
		super.setSize(trayWidth, trayHeight);		
        
        // paint the underlying tray
		super.paint(g);
		
		// Now draw the circles
		AffineTransform transform = new AffineTransform();
		circleNum = 0;
		for (Iterator i = circles.iterator(); i.hasNext(); ++circleNum)
		{
			pc = ((PlayerCircle)i.next());
			if (!pc.isVisible())
			{
				--circleNum;
				continue;
			}
			
			double xOffset = getXOffsetForPlayerCircle(pc, circleNum, aspectW, aspectH);
			double yOffset = getYOffsetForPlayerCircle(pc, circleNum, aspectW, aspectH);
			transform.setToIdentity();
			transform.translate(xOffset, -yOffset);			
			pc.paint(g,aspectW,aspectH,transform);
            
            //TODO fix and turn back on
            //paintCurrentPlayerIndicator(pc, g, (int)xOffset,(int)-yOffset);			
		}
	}

	// determine if this player circle represents the "current player" if so, highlight them
	private void paintCurrentPlayerIndicator( PlayerCircle pc, Graphics g, int x, int y )
	{
	    DiscourseFieldTime dfTime = pc.getDiscourseFieldCircle().getNavigator().getDiscourseFieldTime();
	    MoveEvent moveEvent = dfTime.lookupMoveEvent();
	    
	    if( moveEvent != null )
	    {
	        Move move = moveEvent.getMove();
	        if( move != null )
	        {
	            if( move.getRoleName().equals(pc.getRoleName()) == true )
	            {
	                g.drawImage(currentPlayerIndicator,
	                            x-CURRENT_PLAYER_INDICATOR_X_OFFSET,
	                            y-CURRENT_PLAYER_INDICATOR_Y_OFFSET,null);            
	            }
	        }
	    }
	}
	
	public boolean testHighlight(Point p)
	{
		AffineTransform transform = new AffineTransform();
		
		int circleNum = 0;
		for (Iterator i = circles.iterator(); i.hasNext();)
		{
			PlayerCircle pc = (PlayerCircle)(i.next());
			if (!pc.isVisible())
			{
				continue;
			}
			
			double yOffset = getYOffsetForPlayerCircle(pc, circleNum,
					pc.getLastCircleAspectW(), pc.getLastCircleAspectH());
			double xOffset = getXOffsetForPlayerCircle(pc, circleNum, pc.getLastCircleAspectH(), pc.getLastCircleAspectW());
			
			transform.setToIdentity();
			transform.translate(xOffset, -yOffset);
			
			if (pc.testHighlight(p, transform))
			{
				InfoPanel actionInfo = this.navigator.getInfoPanel();
				String msgContent = pc.getRoleName();
				actionInfo.setMsg(msgContent);
				actionInfo.setLocation(p.x + 10, p.y + 10);
				actionInfo.setVisible(true);
				return true;
			}
			
			++circleNum;
		}
		
		if (this.contains(p))
		{
			return true;
		}
		
		return false;
	}
	
	public boolean testSelection(Point p, int numClicks)
	{
		AffineTransform transform = new AffineTransform();
		
		int circleNum = 0;
		for (Iterator i = circles.iterator(); i.hasNext();)
		{
			PlayerCircle pc = (PlayerCircle)(i.next());
			if (!pc.isVisible())
			{
				continue;
			}
			
			double yOffset = getYOffsetForPlayerCircle(pc, circleNum,
					pc.getLastCircleAspectW(), pc.getLastCircleAspectH());
			double xOffset = getXOffsetForPlayerCircle(pc, circleNum, pc.getLastCircleAspectH(), pc.getLastCircleAspectW());
			
			transform.setToIdentity();
			transform.translate(xOffset, -yOffset);
			
			if (pc.testSelection(p, numClicks, transform))
			{
				return true;
			}
			++circleNum;

		}
		
		if (this.contains(p))
		{
			Workspace.instance.getNavigator().getDiscourseFieldCircle().selectPlayerCircle(null);
			return true;
		}
		
		return false;
	}
	
	private double getYOffsetForPlayerCircle(PlayerCircle circle, int circleNumber, 
			double aspectW, double aspectH)
	{
      Area circleArea = circle.getCircleArea(aspectW, aspectH);
      double neededHeight = 2 * PLAYERCIRCLE_MARGIN + circleArea.getBounds2D().getHeight();

      if (neededHeight < OPTIMAL_TRAY_HEIGHT)
      {
            neededHeight = OPTIMAL_TRAY_HEIGHT;
      }

      return -getY() - neededHeight;    
	}
	
	private double getXOffsetForPlayerCircle(PlayerCircle circle, int circleNumber,
			double aspectW, double aspectH)
	{
        Area circleArea = circle.getCircleArea(aspectW,aspectH);
        return getX() - ((circleNumber + 1) * 
                (circleArea.getBounds2D().getWidth() + PLAYERCIRCLE_MARGIN));
	}
	
//	public int getWidth()
//	{
//		return this.isValid() ? super.getWidth() : OPTIMAL_TRAY_WIDTH;
//	}
//	
    public void selectedRoleChanged( String playerName, MoveEvent currentEvent ) {}
    
    public void roleAdded( String playerName )
    {
    	for (Iterator i=circles.iterator(); i.hasNext(); )
    	{
    		PlayerCircle pc = (PlayerCircle)i.next();
    		if (pc.getRoleName().equals(playerName))
    		{
    			return;
    		}
    	}
    	
    	circles.add(navigator.getDiscourseFieldCircle().getPlayerCircleByName(playerName));
    	Collections.sort(circles, new PlayerCircleComparator());
    }
    
    public void roleRemoved( String playerName )
    {
    	for (Iterator i=circles.iterator(); i.hasNext(); )
    	{
    		PlayerCircle pc = (PlayerCircle)i.next();
    		if (pc.getRoleName().equals(playerName))
    		{
    			i.remove();
    			return;
    		}
    	}
    }
    
    private class PlayerCircleComparator implements Comparator
	{
    	public int compare(Object lhs, Object rhs)
    	{
    		PlayerCircle lhsPC = (PlayerCircle)lhs;
    		PlayerCircle rhsPC = (PlayerCircle)rhs;
    		
    		return lhsPC.getEntryDate().compareTo(rhsPC.getEntryDate());
    	}
	}
}
