/*
 * Created on Apr 26, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator;

import java.awt.*;
import java.awt.geom.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.Move;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.IRoleListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DocumentVersionManager;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.ColorPair;

/**
 * This discourse field circle maintains a list of the player circles
 * it contains and models the "physics" of the their movements. 
 * @author Nick Laiacona
 */
public class DiscourseFieldCircle implements IRoleListener
{
    private DiscourseFieldNavigator navigator;
    private DocumentStateArcManager dsaManager;
    private LinkedList playerCircleList;
    private LinkedList inspirationLines;

    private List discourseFieldNavigatorListeners;
    
    private PlayerCircle selectedPlayerCircle;
    private boolean selectedPlayerDirty;

    private double centerX, centerY, radius;
        
    private Paint dfCirclePaint;
    
    private Ellipse2D.Double circle;
    
    final private ColorPair globalFieldColors;
    
    private static final BasicStroke dfCircleStroke = new BasicStroke(3);
    private static final float[] DASHES = {5.0f};
    private static final BasicStroke inspirationStroke = 
    		new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, DASHES, 0.0f);
    private static final int X_LABEL_MARGIN = 32;
    private static final int Y_LABEL_MARGIN = 5;
    private static final int LABEL_FONT_SIZE = 24;
 
    public static final double OPTIMAL_RADIUS = 225;
    public static final double OPTIMAL_CENTER_X = DiscourseFieldNavigator.WINDOW_OPTIMAL_SIZE_WIDTH/2.0;
    public static final double OPTIMAL_CENTER_Y = DiscourseFieldNavigator.WINDOW_OPTIMAL_SIZE_HEIGHT/2.0;
        
    /**
     * 
     * @param nav
     * @param ownerName Name of the player whose discourse field this is, or
     * null if it is the global discourse field
     */
    public DiscourseFieldCircle( DiscourseFieldNavigator nav)
    {
        globalFieldColors = new ColorPair( IvanhoeUIConstants.GRAY, IvanhoeUIConstants.DARK_GRAY);    	
        playerCircleList = new LinkedList();
        inspirationLines = new LinkedList();
        discourseFieldNavigatorListeners = Collections.synchronizedList(new LinkedList());
        navigator = nav;
        dsaManager = new DocumentStateArcManager(navigator);
        navigator.getRoleManager().addRoleListener(this);
    }
    
    public PlayerCircle addPlayer( Role role )
    {
        PlayerCircle playerCircle = null;
    	for (Iterator i = playerCircleList.iterator(); i.hasNext(); )
    	{
    		PlayerCircle oldPC = (PlayerCircle)i.next();
    		if ( oldPC.getRoleName().equals(role.getName()) ) 
    		{
    			playerCircle = oldPC;
    			break;
    		}
    	}
    	
    	if (playerCircle == null)
    	{
            final DiscourseFieldStateManager dfsManager = navigator.getDocumentAreaManager();
            final DocumentVersionManager dvManager = navigator.getDiscourseField().getDocumentVersionManager();
    		playerCircle = new PlayerCircle(role,this, dfsManager, dvManager);

    		playerCircleList.add(playerCircle);
    		
    		String currentRoleName = this.navigator.getRoleManager().getCurrentRole().getName();
    		if( role.getName().equals(currentRoleName) == true )
    		{
                selectPlayerCircle(playerCircle);
                SimpleLogger.logInfo("selectPlayerCircle(playerCircle)");
    		}
    	}
    	
    	fireRoleAdded(role.getName());
        SimpleLogger.logInfo("fireRoleAdded("+role.getName()+")");
    	navigator.dirty();
        SimpleLogger.logInfo("navigator.dirty()");
        return playerCircle;
    }
    
    public boolean removePlayer( String playerName )
    {
    	for (Iterator i = playerCircleList.iterator(); i.hasNext(); )
    	{
    		PlayerCircle playerCircle = (PlayerCircle)i.next();
    		if ( playerCircle.getRoleName().equals(playerName) ) 
    		{
    			i.remove();
    			fireRoleRemoved(playerCircle.getRoleName());
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public void addListener( IDiscourseFieldNavigatorListener listener )
    {
        discourseFieldNavigatorListeners.add(listener);
    }

    public void removeListener( IDiscourseFieldNavigatorListener listener )
    {
        discourseFieldNavigatorListeners.remove(listener);
    }
    
    private void fireSelectedRoleChange()
    {
    	MoveEvent moveEvent;
    	if( selectedPlayerCircle != null )
    	{
    		moveEvent = selectedPlayerCircle.getCurrentMoveEvent();
    	}
    	else
    	{
    		moveEvent = null;
    	}
    	
    	String selectedRoleName;
    	if (selectedPlayerCircle != null)
    	{
    	    selectedRoleName = selectedPlayerCircle.getRoleName();
    	}
    	else
    	{
    	    selectedRoleName = ""; 
    	}
    	    	
    	synchronized(discourseFieldNavigatorListeners)
    	{
            for( Iterator i = discourseFieldNavigatorListeners.iterator(); i.hasNext(); )
            {
                IDiscourseFieldNavigatorListener listener = (IDiscourseFieldNavigatorListener) i.next();
                listener.selectedRoleChanged(selectedRoleName,moveEvent);
            }            	            
    	}    	
    }
    
    private void fireRoleAdded(String roleName)
    {
    	synchronized(discourseFieldNavigatorListeners)
    	{
            for( Iterator i = discourseFieldNavigatorListeners.iterator(); i.hasNext(); )
            {
                IDiscourseFieldNavigatorListener listener = (IDiscourseFieldNavigatorListener) i.next();
                listener.roleAdded(roleName);
            }
    	}
    }
    
    private void fireRoleRemoved(String roleName)
    {
    	synchronized(discourseFieldNavigatorListeners)
    	{
            for( Iterator i = discourseFieldNavigatorListeners.iterator(); i.hasNext(); )
            {
                IDiscourseFieldNavigatorListener listener = (IDiscourseFieldNavigatorListener) i.next();
                listener.roleRemoved(roleName);
            }            	            
    	}
    }
    
    public void dirtySelectedPlayer()
    {
    	selectedPlayerDirty = true;
    }
    
    public void addMove( MoveEvent event )
    {
        boolean playerFound = false;
        
        for( Iterator i = playerCircleList.iterator(); i.hasNext(); )
        {
            PlayerCircle playerCircle = (PlayerCircle) i.next();
            
            if( playerCircle.getRoleName().equals(event.getMove().getRoleName()) == true )
            {
                playerCircle.addMoveToCourse(event);
                playerFound = true;
            }                
        }
        
        if( playerFound == false )
        {
            Role role = navigator.getRoleManager().getRole(event.getMove().getRoleID());
    		PlayerCircle playerCircle = addPlayer(role);
    		playerCircle.addMoveToCourse(event);
        }
        
        navigator.dirty();
    }
        
    public void moveToTick( double tick )
    {
        for( Iterator i = playerCircleList.iterator(); i.hasNext(); )
        {
            PlayerCircle playerCircle = (PlayerCircle) i.next();
            playerCircle.moveToTick(tick);
          	playerCircle.useInspirationLines(this);
        }
        
    	navigator.dirty();
    }
    
    public void recalculatePaths()
    {
    	navigator.dirty();
    }

    public String getTitle()
    {
    	return "";
/*    	String dfOwnerName = null;
    	if( this.selectedPlayerCircle != null )
    	{
    		dfOwnerName = selectedPlayerCircle.getPlayerName();
    	}
    	
    	String dfTitle;
    	if ( dfOwnerName == null )
    	{
    		dfTitle = "Click on a player circle to select.";
    	} else {
    		if ( dfOwnerName.endsWith("s") )
    		{
    			dfTitle = dfOwnerName + "' Discourse Field";
    		} else {
    			dfTitle = dfOwnerName + "'s Discourse Field";
    		}
    	}
    	return dfTitle;
*/    }
    
    public void paint(Graphics g, double aspectW, double aspectH )
    {
        if (selectedPlayerDirty)
        {
        	fireSelectedRoleChange();
        	selectedPlayerDirty = false;
        }
        
        dsaManager.cleanUp();
    	dsaManager = new DocumentStateArcManager(navigator);
    	
    	Graphics2D g2 = Ivanhoe.getGraphics2D(g);
        
    	String dfTitle = getTitle();
    	
    	final Color dfCircleStrokeColor;
        if (selectedPlayerCircle != null)
        {
            dfCircleStrokeColor = selectedPlayerCircle.getFillColor();
        }
        else
        {
            dfCircleStrokeColor = globalFieldColors.strokeColor;
        }
        
        final Color dfCircleFillColor = globalFieldColors.fillColor; 
        
        // paint the label
    	navigator.getLabelManager().addLabel( dfTitle, X_LABEL_MARGIN, Y_LABEL_MARGIN, 0, 
				new Font("Verdana", Font.BOLD, LABEL_FONT_SIZE), Color.WHITE, LabelManager.LEFT_ORIGIN );
    	
    	// paint the interior of the circle
        Ellipse2D dfCircle = constructCircle( aspectW, aspectH );       
        dfCirclePaint = new GradientPaint((int)(centerX-radius),(int)(centerY-radius), dfCircleFillColor, 
                                           (int)(centerX+radius),(int)(centerY+radius), Color.black, true);

        g2.setPaint(dfCirclePaint);
        g2.fill(dfCircle);
        
        // paint the player circle arcs
		PlayerCircle selectedPC = null;
		for ( Iterator i = playerCircleList.iterator(); i.hasNext(); )
        {
            PlayerCircle playerCircle = (PlayerCircle) i.next();
            if ( playerCircle.isSelected() ) 
            {
            	selectedPC = playerCircle;
            } else {
            	playerCircle.paintArcs(g,aspectW,aspectH);
            }
        }
        if (selectedPC != null)
        {
        	selectedPC.paintArcs(g,aspectW,aspectH);
        }
        
        // paint the inspiration lines
        g2.setStroke(inspirationStroke);
		for ( Iterator i = inspirationLines.iterator(); i.hasNext(); )
		{
			InspirationLine il = (InspirationLine)i.next();
			il.paint(g2, aspectW, aspectH);
		}
        
		
        // paint the player circles
		for ( Iterator i = playerCircleList.iterator(); i.hasNext(); )
        {
            PlayerCircle playerCircle = (PlayerCircle) i.next();
            if ( !playerCircle.isSelected() )
            {
            	playerCircle.paint(g,aspectW,aspectH);
            }
        }
        if (selectedPC != null)
        {
        	selectedPC.paint(g,aspectW,aspectH);
        }
        
        dsaManager.paintArcs(g,aspectW,aspectH);
        
        // paint the outline of the dfCircle
        g2.setColor(dfCircleStrokeColor);
        g2.setStroke(dfCircleStroke);
        g2.draw(dfCircle);
    }

    public boolean testHighlight( Point p )
    {        
        boolean hit = false;
        
		if( (circle != null) && (circle.contains(p) == true) )
        {
            hit = true;
            navigator.getDocumentAreaManager().clearHighlight();
        }
                
    	for (Iterator i = playerCircleList.iterator(); i.hasNext(); )
    	{
    		if ( ((PlayerCircle)i.next()).testHighlight(p) )
    		{
    			hit = true;
    			break;
    		}
    	}
    	
    	for (Iterator i = inspirationLines.iterator(); i.hasNext(); )
    	{
    		if ( ((InspirationLine)i.next()).testHighlight(p) )
    		{
    			hit = true;
    			break;
    		}
    	}
    	
    	return hit;
    }
    
    public boolean testSelection( Point p, int numClicks )
    {
    	for (Iterator i = playerCircleList.iterator(); i.hasNext(); )
    	{
    		if ( ((PlayerCircle)i.next()).testSelection(p, numClicks) )
			{
    			return true;
			}
    	}
    	
    	for (Iterator i = inspirationLines.iterator(); i.hasNext(); )
    	{
    		if ( ((InspirationLine)i.next()).testSelection(p,numClicks) )
    		{
    			return true;
    		}
    	}
    	
    	if ( circle != null && circle.contains(p))
    	{
    		selectPlayerCircle(null);
    		navigator.dirty();
    		return true;
    	}
    	
    	return false;
    }
    
    private Ellipse2D constructCircle( double aspectW, double aspectH )
    {
        Dimension windowSize = navigator.getSize();
        centerX = windowSize.width/2.0;
        centerY = windowSize.height/2.0;
        
        double constrainingAspect;
        if( aspectW < aspectH ) constrainingAspect = aspectW;
        else                    constrainingAspect = aspectH;
        
        radius = OPTIMAL_RADIUS * constrainingAspect;
        
        double x = centerX - radius;
        double y = centerY - radius;
        double w = radius * 2.0;
        double h = w;
        
        circle = new Ellipse2D.Double(x,y,w,h); 
        return circle;
    }
   
    /**
     * @return Returns the centerX.
     */
    public double getCenterX()
    {
        return centerX;
    }
    /**
     * @return Returns the centerY.
     */
    public double getCenterY()
    {
        return centerY;
    }
    /**
     * @return Returns the radius.
     */
    public double getRadius()
    {
        return radius;
    }
	/**
	 * @return Returns the navigator.
	 */
	public DiscourseFieldNavigator getNavigator()
	{
		return navigator;
	}
	/**
	 * @return Returns the current document state arc manager
	 */
	public DocumentStateArcManager getDSAManager()
	{
		return dsaManager;
	}
	/**
	 * @return Returns the name of the owner of this circle or null
	 */
	public String getOwnerName()
	{
		if( selectedPlayerCircle != null )
		{
			return selectedPlayerCircle.getRoleName();
		}
		
		return null;
	}
	/**
	 * @param playerName the name of a player
	 * @return the circle of the player playerName, or null if no circle is 
	 * found 
	 */
	public PlayerCircle getPlayerCircleByName(String playerName)
	{
		for (Iterator i = playerCircleList.iterator(); i.hasNext(); )
		{
			PlayerCircle playerCircle = (PlayerCircle)i.next();
			if ( playerCircle.getRoleName().equals(playerName) ) 
			{
				return playerCircle;
			}
		}
		
		return null;
	}	
	
	public List getPlayerCircleList()
	{
		return playerCircleList;
	}
	
	/**
	 * Sets one player circle to be selected
	 * @param ps The new selected player circle
	 */
	public void selectPlayerCircle( PlayerCircle ps )
	{
	    // if this is the same player circle, do nothing.
	    if( ps == selectedPlayerCircle ) return;
	    
		if ( selectedPlayerCircle != null)
		{
			selectedPlayerCircle.setSelection(false);
		}
		
		selectedPlayerCircle = ps;
		
		if (selectedPlayerCircle != null)
		{
			selectedPlayerCircle.setSelection(true);
		}

        navigator.dirty();
		fireSelectedRoleChange();
	}
	
	public void createInspirationLine(String inspireSrc, String inspireDst, Move inspiration)
	{
		InspirationLine il = new InspirationLine(inspireSrc, inspireDst, inspiration);
		inspirationLines.add(il);
	}
	
	public boolean removeInspirationLines(String inspireDst)
	{
		boolean removedAny = false;
		for ( Iterator i = inspirationLines.iterator(); i.hasNext(); )
		{
			InspirationLine il = (InspirationLine)i.next();
			if (il.getInspireeName().equals(inspireDst))
			{
				i.remove();
				removedAny = true;
			}
		}
		
		return removedAny;
	}
    
    private class InspirationLine
	{
    	private PlayerCircle inspiror, inspiree;
    	private String inspirorName, inspireeName;
    	private Move inspiration;
    	private AffineTransform transform;
    	private Area arrowHead;
    	private static final double INSPIRATION_LINE_CLICK_WIDTH = 5.0;
        private static final double ARROW_ANGLE = 30.0;
        private static final double ARROW_LENGTH = 20.0;
        private static final double ARROW_GAP = 15.0;
    	
    	InspirationLine(String inspirorName, String inspireeName, Move inspiration)
		{
    		this.inspirorName = inspirorName;
    		this.inspireeName = inspireeName;
    		this.inspiration = inspiration;
    		
    		this.inspiror = getPlayerCircleByName(inspirorName);
    		this.inspiree = getPlayerCircleByName(inspireeName);
			
			if( this.inspiror == null )
			{
				throw new IllegalArgumentException("Inspiror Name does not represent a valid role: "+inspirorName);
			}
			
			if( this.inspiree== null )
			{
				throw new IllegalArgumentException("Inspiree Name does not represent a valid role: "+inspireeName);
			}
		}
		
    	/**
		 * @return Returns the inspiror iff it's valid, otherwise throws an 
		 * exception
		 */
		public PlayerCircle getInspiror() {
			checkReferences();
			if (inspiror == null)
			{
				throw new RuntimeException("getInspiror called before inspiror is " +
						"valid, either because of a bad inspiror, or because " +
						"it's called before inspiror was added to the discourse " +
						"field");
			}
			return inspiror;
		}
		/**
		 * @return Returns the inspiree iff it's valid, otherwise throws an 
		 * exception
		 */
		public PlayerCircle getInspiree() {
			checkReferences();
			if (inspiree == null)
			{
				throw new RuntimeException("getInspiree called before inspiree is " +
						"valid, either because of a bad inspiree, or because " +
						"it's called before inspiree was added to the discourse " +
						"field");
			}
			return inspiree;
		}
		
		public String getInspirorName()
		{
			return inspirorName;
		}
		
		public String getInspireeName()
		{
			return inspireeName;
		}
		
		public Move getInspiration()
		{
			return inspiration;
		}
		
		public void paint(Graphics2D g2, double aspectW, double aspectH)
		{
			checkReferences();
			if (inspiree == null || inspiror == null)
			{
				throw new RuntimeException("painting with invalid inspiror or inspiree");
			}
			
			
			Line2D line = constructLine(aspectW, aspectH);
			arrowHead = constructArrowHead(line, ARROW_ANGLE, ARROW_LENGTH, aspectW, aspectH);
			transform = constructTransform(line);
			
			int alpha = inspiree.usingSelectedColor() ? inspiree.getArcFlashAlpha() : 255;
			Color inspireeColor = Ivanhoe.addAlphaToColor(inspiree.getStrokeColor(), alpha);
			
			g2.setPaint(inspireeColor);
			g2.draw(line);
			g2.fill(arrowHead);
		}
		
		private void checkReferences()
		{
			if (inspiror == null)
			{
				inspiror = getPlayerCircleByName(inspirorName);
			}
			
			if (inspiree == null)
			{
				inspiree = getPlayerCircleByName(inspireeName);
			}
		}
		
		private Line2D constructLine(double aspectW, double aspectH)
		{
			double inspirorX = inspiror.getViewCenterX(aspectW, aspectH);
			double inspirorY = inspiror.getViewCenterY(aspectW, aspectH);
			double inspireeX = inspiree.getViewCenterX(aspectW, aspectH);
			double inspireeY = inspiree.getViewCenterY(aspectW, aspectH);
			
			// make the endpoints on the circle, rather than in the middle
			double length = Math.sqrt(Math.pow(inspirorX - inspireeX, 2) + Math.pow(inspirorY - inspireeY, 2));
			double playerCircleRadius = PlayerCircle.getRadius(aspectW, aspectH);
			double modInspireeX = (inspirorX * playerCircleRadius + inspireeX * length) / (length + playerCircleRadius);
			double modInspireeY = (inspirorY * playerCircleRadius + inspireeY * length) / (length + playerCircleRadius);
			double modInspirorX = (inspireeX * playerCircleRadius + inspirorX * length) / (length + playerCircleRadius);
			double modInspirorY = (inspireeY * playerCircleRadius + inspirorY * length) / (length + playerCircleRadius);
			
			double thetaRad = Math.atan2(modInspirorY - modInspireeY, modInspirorX - modInspireeX);
			double xGap = Math.cos(thetaRad) * (ARROW_LENGTH + ARROW_GAP);
			double yGap = Math.sin(thetaRad) * (ARROW_LENGTH + ARROW_GAP);
			
			double adjustedInspirorX = (modInspirorX - modInspireeX) + modInspireeX - xGap; 
			double adjustedInspirorY = (modInspirorY - modInspireeY) + modInspireeY - yGap;
			
			Line2D line = new Line2D.Double(
			        modInspireeX, modInspireeY,
			        adjustedInspirorX, adjustedInspirorY);
			
			return line;
		}

		private Area constructArrowHead(Line2D arrowBase,
		        double angle, double length,
		        double aspectW, double aspectH)
		{
	        double constrainingAspect;
	        if (aspectW < aspectH) constrainingAspect = aspectW;
	        else constrainingAspect = aspectH;
	        
	        length *= constrainingAspect;
	        
		    double startX = arrowBase.getX1();
			double startY = arrowBase.getY1();
			double endX = arrowBase.getX2();
			double endY = arrowBase.getY2();
			
			double lineAngleRad = -Math.atan2(startY-endY, startX-endX);
			double lineAngleDeg = Math.toDegrees(lineAngleRad);
			double start = lineAngleDeg - (angle/2.0);
			double extent = angle;
			
			double x = endX - length - Math.cos(-lineAngleRad) * length;
			double y = endY - length - Math.sin(-lineAngleRad) * length;
			double width = length * 2;
			double height = length * 2;
			
			return new Area(new Arc2D.Double(x, y, width, height, start, extent, Arc2D.PIE));
		}
		
		private AffineTransform constructTransform(Line2D line)
		{
			// This builds an affine transformation that takes a point in
			// screen space and puts it into a space where the rectangle one
			// unit around the origin represents the space over top of the
			// inspiration line.  
			
			double displacementX = line.getX1();
			double displacementY = line.getY1();
			double endPointX = line.getX2() - displacementX;
			double endPointY = line.getY2() - displacementY;
			
			double r, theta;
			r = Math.sqrt(endPointX*endPointX + endPointY*endPointY);
			
			if (endPointX > 0)
			{
				theta = Math.atan(endPointY/endPointX);
			} 
			else if (endPointX < 0)
			{
				if (endPointY >= 0)
				{
					theta = Math.atan(endPointY/endPointX) + Math.PI;
				} 
				else
				{
					theta = Math.atan(endPointY/endPointX) - Math.PI;
				}
			}
			else // endPointX == 0
			{
				if (endPointY > 0)
				{
					theta = Math.PI / 2;
				}
				else if (endPointY < 0)
				{
					theta = - Math.PI / 2;
				}
				else
				{
					theta = 0.0/0.0;
				}
			}
			
			AffineTransform xform = new AffineTransform();
			
			xform.translate(-1.0, 0.0);
			xform.scale(2.0/r, 2.0/INSPIRATION_LINE_CLICK_WIDTH);
			xform.rotate(-theta);
			xform.translate(-displacementX, -displacementY);
			
			return xform;
		}
		
		public boolean testHighlight(Point p)
		{
			if (transform == null)
			{
				return false;
			}
			
			Point2D.Double ptSrc = new Point2D.Double(p.x,p.y);
			Point2D.Double ptDst = new Point2D.Double();
			
			transform.transform(ptSrc, ptDst);
			
			if ((Math.abs(ptDst.x) < 1.0 && Math.abs(ptDst.y) < 1.0) || 
			        (arrowHead != null && arrowHead.contains(ptSrc)))
			{
				return this.doHighlight(p);
			}
			
			return false;
		}
		
		public boolean testSelection(Point p, int numClicks)
		{
			if (transform == null)
			{
				return false;
			}
			
			Point2D.Double ptSrc = new Point2D.Double(p.x,p.y);
			Point2D.Double ptDst = new Point2D.Double();
			
			transform.transform(ptSrc, ptDst);
			
			if ((Math.abs(ptDst.x) < 1.0 && Math.abs(ptDst.y) < 1.0) || 
			        (arrowHead != null && arrowHead.contains(ptSrc)))
			{
				return doSelection(p,numClicks);
			}
			
			return false;
		}
		
		private boolean doSelection(Point p, int numClicks)
		{
			return inspiree.doSelection(p,numClicks);
		}
		
		private boolean doHighlight(Point p)
		{
		    return inspiree.doHighlight(p);
		}
	}
	/**
	 * @return Returns the selectedPlayerCircle.
	 */
	public PlayerCircle getSelectedPlayerCircle()
	{
		return selectedPlayerCircle;
	}
	
	public void roleChanged()
	{
        if (selectedPlayerCircle != null)
        {
            // act as if we'd selected this pcircle anew
            PlayerCircle selected = selectedPlayerCircle; 
            selectPlayerCircle(null);
            selectPlayerCircle(selected);
        }
        else
        {
            selectPlayerCircle(null);
        }
        
	}
}
