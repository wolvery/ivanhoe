/*
 * Created on Apr 26, 2004
 */

package edu.virginia.speclab.ivanhoe.client.game.view.navigator;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Timer;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DocumentVersionManager;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.StartingMoveEvent;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.IRoleListener;
import edu.virginia.speclab.ivanhoe.client.game.view.document.InfoPanel;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;
import edu.virginia.speclab.ivanhoe.shared.data.ActionType;
import edu.virginia.speclab.ivanhoe.shared.data.Move;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.data.Link;
import edu.virginia.speclab.ivanhoe.shared.data.LinkType;

/**
 * This class renders the player circle inside the discourse field circle. 
 * 
 * @author Nick Laiacona
 */
public class PlayerCircle implements IRoleListener
{
    private final DocumentVersionManager dvManager;
    private final DiscourseFieldCircle discourseFieldCircle;
    private final DiscourseFieldStateManager docFieldStateManager;
    private MovementDestination currentMove,currentArcSource;
    
    private Date entryDate;
    private boolean isSelected, isRolledOver;
    private boolean isInvisible;
    
    private LinkedList destinationList;
    private double circleX, circleY;
    private Ellipse2D circle;
    private double lastCircleAspectW, lastCircleAspectH;
    private boolean circleIsDirty;
    
    private int arcFlashAlpha;    
    private ArcFlashTimer arcFlashTimer;
    
    private Role role;
    private Stroke pcStroke;
    
    private static Font labelFont = new Font("Verdana", Font.PLAIN, 16);
    
    public static final double OPTIMAL_RADIUS = 8;
    public static final double MAX_SPEED = 1000;
    public static final double MOVE_SPEED = 80;
    private static final int Y_LABEL_PADDING = 24;
    public static final int UNSELECTED_STROKE = 2;
    public static final int SELECTED_STROKE = 4;
    public static final int UNSELECTED_ALPHA = 50;
    private static double ARC_FLASH_DECAY = 1000; //milliseconds
    private static int ARC_FLASH_UPDATE_FREQUENCY = 1000/30;
    
    private static Color selectedTextColor = Color.WHITE;
    private static Color unselectedTextColor = Ivanhoe.addAlphaToColor(Color.WHITE,UNSELECTED_ALPHA);

    // coordinates stored here are calculated in world space, apply
    // the screen aspect ratios to get screen space mapping
    private class MovementDestination
	{
        public LinkedList actionArcList;
    	public double x,y;
    	public MoveEvent event;
    	
    	public MovementDestination(double startX, double startY, MoveEvent event )
    	{
    	    this.event = event; 		
    		x = startX;
    		y = startY;
    		
    		if( event.getMove() == null )
    		{
    		    actionArcList = null;
    		}
    		else
    		{
    			createArcs(event);
    		}
    	}
    	
    	public void createArcs(MoveEvent moveEvent)
    	{
    		actionArcList = new LinkedList();
		    double moveTime = moveEvent.getTick();
		    
    		for( Iterator i = moveEvent.getMove().getActions(); i.hasNext(); )
    		{
                IvanhoeAction act = (IvanhoeAction) i.next();
                final DocumentStateArcManager dsaManager = discourseFieldCircle.getDSAManager();
                final Move move = moveEvent.getMove();
                
                ActionArc arc = new ActionArc(
                        PlayerCircle.this,
                        docFieldStateManager,
                        dsaManager,
                        act,
                        move,
                        moveTime);
                calculateNewPosition(arc, moveEvent.getMove().getActionCount());
                actionArcList.add(arc);
        	}
        }
    	
    	public void useInspirationLines(DiscourseFieldCircle dfCircle)
    	{	    	 
    	    String roleName = role.getName();
        	PlayerCircle.this.getDiscourseFieldCircle().removeInspirationLines(roleName);
    		
    		if (event == null) return;
    		Move move = event.getMove();
    		if (move == null) return;
    		
            for( Iterator i = move.getActions(); i.hasNext(); )
    		{
                IvanhoeAction act = (IvanhoeAction) i.next();
            
	        	if ( act.getType().equals(ActionType.LINK_ACTION) )
		        {
		        	Link linkAct = (Link)act.getContent();
		        	if ( linkAct.getType().equals(LinkType.INTERNAL))
		        	{
						DocumentVersion version = dvManager.getDocumentVersion(linkAct.getLinkTag().getDocumentVersionID());
		        		String targetPlayer = version.getRoleName();

		        		if ( targetPlayer != null && 
                             !targetPlayer.equals(Role.GAME_CREATOR_ROLE.getName())
                             && !roleName.equals(targetPlayer) )
		        		{
		        			dfCircle.createInspirationLine(targetPlayer, roleName, move);
		        		}
		        	}
		        	else if ( linkAct.getType().equals(LinkType.COMMENT) )
		        	{
		        		String commentDocOwner = dvManager.getDocumentVersion(act).getRoleName();
			        	if (!(commentDocOwner.equals(Role.GAME_CREATOR_ROLE_NAME)
                                || commentDocOwner.equals(roleName)) )
			        	{
                            dfCircle.createInspirationLine(commentDocOwner, roleName, move);
			        	}
		        	}
		        }
    		}
    	}
    	
    	private void calculateNewPosition( ActionArc arc, int numActions )
    	{
    		double arcForce = MOVE_SPEED / numActions;
			double arcAngle = arc.getCenter();

			// calculate the arc center X,Y
			double arcX = (Math.cos(Math.toRadians(arcAngle)) * DiscourseFieldCircle.OPTIMAL_RADIUS) + DiscourseFieldCircle.OPTIMAL_CENTER_X;		
			double arcY = (Math.sin(Math.toRadians(arcAngle)) * -DiscourseFieldCircle.OPTIMAL_RADIUS) + DiscourseFieldCircle.OPTIMAL_CENTER_Y;
			
			// calculate the angle from current position to arc center
			double dx = arcX - x; 
			double dy = arcY - y;
			double dist = Math.sqrt((dx*dx)+(dy*dy));
			double unitX = dx/dist;
			double unitY = dy/dist;
			
			// calculate the new position
			double newX = x + (unitX * arcForce);
			double newY = y + (unitY * arcForce);

			// test to see if this motion takes us out of bounds
			// if it does, stop at boundary
			Point.Double location = interceptBoundary( x, y, newX, newY );
			x = location.x;
			y = location.y;
    	}
    	
    	public void testInterceptBoundary()
    	{
    	    Point.Double result;
    	    
    	    System.out.println("Intercept Boundary Test");
    	    System.out.println("angle,orignal distance,intercept dist,original quad,intercept quad");
    	    System.out.println("=======================");
    	    
    	    double x1 = DiscourseFieldCircle.OPTIMAL_CENTER_X;
    	    double y1 = DiscourseFieldCircle.OPTIMAL_CENTER_Y;
    	    
    	    for( double angle = 0.0; angle < Math.PI*2; angle += Math.PI / 20.0 )
    	    {
    	        double length =  DiscourseFieldCircle.OPTIMAL_RADIUS;
   	        
        	    double x2 = Math.cos(angle)*length + x1;
        	    double y2 = Math.sin(angle)*-length + y1;
        	    
        	    result = interceptBoundary( x1, y1, x2, y2 );
        	    
        	    double deltaX2 = x2 - x1;
        	    double deltaY2 = y2 - y1;
        	    double deltaXR = result.x - x1;
        	    double deltaYR = result.y - y1;
        	    
        	    double dist2 = Math.sqrt((deltaX2*deltaX2)+(deltaY2*deltaY2));
        	    double distR = Math.sqrt((deltaXR*deltaXR)+(deltaYR*deltaYR));
        	    
        	    int quad2 = determineQuadrant(deltaX2,deltaY2);
        	    int quadR = determineQuadrant(deltaXR,deltaYR);
        	    
        	    System.out.println(angle+","+dist2+","+distR+","+quad2+","+quadR);
    	    }
    	}
    	
    	private int determineQuadrant( double posX, double posY )
    	{
    	    if( posX >= 0 && posY >= 0 )
    	    {
    	        return 1;
    	    }
    	    else if( posX < 0 && posY >= 0 )
    	    {
    	        return 2;
    	    }
    	    else if( posX < 0 && posY < 0 )
    	    {
    	        return 3;
    	    }
    	    else if( posX >= 0 && posY < 0 )
    	    {
    	        return 4;
    	    }
    	    
    	    return -1;    	    
    	}
    	
    	private Point.Double interceptBoundary( double posX, double posY, double newX, double newY )
    	{
    	    Point.Double location = new Point.Double();
    	    
    	    double deltaX = newX - DiscourseFieldCircle.OPTIMAL_CENTER_X;
    	    double deltaY = newY - DiscourseFieldCircle.OPTIMAL_CENTER_Y;
    	    double distanceFromCenter = Math.sqrt( (deltaX*deltaX) + (deltaY*deltaY) );
    	    double boundaryRadius = DiscourseFieldCircle.OPTIMAL_RADIUS - (PlayerCircle.OPTIMAL_RADIUS*4.0);
    	    
    	    if( distanceFromCenter >= boundaryRadius )
    	    {
    	        // move origin to center of discourse field and flip y axis 
    	        double x1 = posX - DiscourseFieldCircle.OPTIMAL_CENTER_X;
    	        double x2 = newX - DiscourseFieldCircle.OPTIMAL_CENTER_X;
    	        double y1 = -(posY - DiscourseFieldCircle.OPTIMAL_CENTER_Y);
    	        double y2 = -(newY - DiscourseFieldCircle.OPTIMAL_CENTER_Y);
    	        
    	        // calculate the intersection point 
    	        // http://mathworld.wolfram.com/Circle-LineIntersection.html   	        
    	        double dX = x2 - x1;
    	        double dY = y2 - y1;

    	        double dR = Math.sqrt((dX*dX)+(dY*dY));
    	        double D = (x1*y2)-(x2*y1);
    	        double sqrtDescrim = Math.sqrt( (boundaryRadius*boundaryRadius) * (dR*dR) - (D*D) );
    	        
    	        double interceptX = ((D*dY)+(dX*sqrtDescrim)) / (dR*dR);
    	        double interceptY  = ((-D*dX)+(dY*sqrtDescrim)) / (dR*dR);
    	        
    	        // convert back to world coordinates
    	        location.x = (interceptX) + DiscourseFieldCircle.OPTIMAL_CENTER_X;
    	        location.y = (interceptY*-1) + DiscourseFieldCircle.OPTIMAL_CENTER_Y;
    	    }
    	    else
    	    {
    	        location.x = newX;
    	        location.y = newY;
    	    }
    	    
    	    
    	    return location;
    	}
    	
        public void paintActionArcs(Graphics2D g, double aspectW, double aspectH)
        {
            if( actionArcList == null || actionArcList.size() <= 0 ) return;
            
            for (Iterator i = actionArcList.iterator(); i.hasNext();)
            {
                ActionArc arc = (ActionArc) i.next();
                arc.paint(g, aspectW, aspectH);
            }
        }
        
        public void paintDocumentStateArcs(Graphics2D g, double aspectW, double aspectH)
        {
        	if (actionArcList == null || actionArcList.size() <= 0)
        	{
        		return;
        	}
        	
        	for ( Iterator i = actionArcList.iterator(); i.hasNext(); )
        	{
        		DocumentStateArc dsArc = ((ActionArc)i.next()).getDocumentStateArc();
        		discourseFieldCircle.getDSAManager().addDocStateArc(dsArc);
        	}
        }
        
        
        public boolean testSelection( Point p, int numClicks )
        {
        	if (isSelected)
        	{
	        	for ( Iterator i = actionArcList.iterator(); i.hasNext(); )
	        	{
	        		if (((ActionArc)i.next()).getDocumentStateArc().testSelection(p, numClicks))
	        		{
	        			return true;
	        		}
	        	}
        	}
        	return false;
        }
        
        public boolean testHighlight( Point p )
        {
        	if (isSelected)
        	{
	        	for ( Iterator i = actionArcList.iterator(); i.hasNext(); )
	        	{
	        		if (((ActionArc)i.next()).getDocumentStateArc().testHighlight(p))
	        		{
	        			return true;
	        		}
	        	}
        	} 
        	
        	if ( circle.contains(p) )
        	{
        		discourseFieldCircle.getNavigator().setCursor(
        				Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        		return true;
        	}
        	
        	return false;
        }
	}
    
    private class ArcFlashTimer implements ActionListener
    {
        private Timer timer;
        private long startTime;
        
        public ArcFlashTimer()
        {            
            timer = new Timer(ARC_FLASH_UPDATE_FREQUENCY,this);                       
        }
        
        private void start()
        {            
            startTime = System.currentTimeMillis();
            timer.start();
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent arg0)
        {
            double timeDiff = System.currentTimeMillis() - startTime;
            
            if( timeDiff > ARC_FLASH_DECAY )
            {
                updateArcFlash(1.0);
                timer.stop();
            }
            else
            {
                updateArcFlash(timeDiff/ARC_FLASH_DECAY);
            }            
        }        
    }
    

    /**
     * Constructs a new player circle that interacts with the specified discourse
     * field circle and document area manager.
     * @param circle The parent discourse field circle.
     * @param manager The associated document area manager.
     */
    public PlayerCircle( Role role, DiscourseFieldCircle dfCircle, DiscourseFieldStateManager manager,
            DocumentVersionManager dvManager)
    {
		this.role = role;
		this.dvManager = dvManager;
		discourseFieldCircle = dfCircle;
        discourseFieldCircle.getNavigator().getRoleManager().addRoleListener(this);
        docFieldStateManager = manager;
        arcFlashAlpha = UNSELECTED_ALPHA;
        pcStroke = new BasicStroke(UNSELECTED_STROKE);
        entryDate = null;
        isSelected = false;
        isInvisible = true;
        circleIsDirty = true;
        destinationList = new LinkedList();

        arcFlashTimer = new ArcFlashTimer();
        circle = new Ellipse2D.Double();

        chartCourse();
        moveToTick(0);

        //TODO: uncomment this to see unit tests in stdout. should do this with junit, we really
        // need a test frame to do that though.
        //testMovementDestination();
    }
    
    public void testMovementDestination()
    {
        double prevX = DiscourseFieldCircle.OPTIMAL_CENTER_X; 
        double prevY = DiscourseFieldCircle.OPTIMAL_CENTER_Y;
        MovementDestination dest = new MovementDestination(prevX, prevY, new MoveEvent(null,null));
        dest.testInterceptBoundary();
    }

    
    /**
     * Calculate a course for this player's journey around the discourse 
     * field circle based on the current state of the <code>DiscourseField</code>.
     * 
     */
    public void chartCourse()
    {
    	// nuke the previous course 
    	destinationList.clear();
    
    	// chart the course for this player circle
    	LinkedList moveHistory = discourseFieldCircle.getNavigator().getDiscourseFieldTimeline().getMoveEventHistory(role.getName());
    
        for( Iterator i = moveHistory.iterator(); i.hasNext(); )
        {
        	MoveEvent event = (MoveEvent) i.next();
        	addMoveToCourse(event);
        }
    }

    // find the next destination in the series after this point in tick time. 
    private MovementDestination findNextDestination( double tick )
    {
        for( Iterator i = destinationList.iterator(); i.hasNext(); )
        {
            MovementDestination destination = (MovementDestination) i.next();
            
            if( destination.event != null && 
                destination.event.getTick() > tick )
            {
                return destination;
            }
        }
        
        return null;
    }
    
    // find the destination in the series preceeding this point in tick time.
    private MovementDestination findPrevDestination( double tick )
    {
        MovementDestination prevDestination = null;
        
        for( Iterator i = destinationList.iterator(); i.hasNext(); )
        {
            MovementDestination destination = (MovementDestination) i.next();
            
            if( destination.event != null &&
                destination.event.getTick() > tick )
            {
                break;
            }
            
            prevDestination = destination;
        }
        
        return prevDestination;        
    }
         
    /**
     * Move the player circle temporally to a specific time tick value. 
     * @param tick The time tick to move to.
     */
    public void moveToTick( double tick )
    {
        // determine the destination we are move toward and which one we are departing from
       	this.currentMove = findPrevDestination( tick );
       	MovementDestination nextMove = findNextDestination( tick );
       	
        circleIsDirty = true;
        
        // if the current move is the blank starting move, then don't draw the player circle.                
        if( currentMove == null || currentMove.event.getMove() == null )
        {
            this.isInvisible = true;      
            setCurrentArcSource(null);
        }
        else
        {
            this.isInvisible = false;
	        movePlayerCircle( currentMove, nextMove, tick );
        }
        
        updateArcs( tick );
    }
    
    private void updateArcs( double tickTime )
    {
        // if we are displaying arcs, move them to the current time
        if( currentArcSource != null )
        {
 			for( Iterator i = currentArcSource.actionArcList.iterator(); i.hasNext(); )
 			{
 			    ActionArc actionArc = (ActionArc) i.next();
 			    actionArc.moveToTick(tickTime);
 			}					            				
        }        
    }
       	
    private void movePlayerCircle( MovementDestination origin, MovementDestination destination, double tick )
    {       
        if( origin == null ) return;
        
        if( destination == null )
        {
            // end of the line, show the circle at its present position
			circleX = origin.x;
			circleY = origin.y;
			setCurrentArcSource( origin );	                        
        }
        else
        {
            // if the next move is not null then we are moving. Calculate our position
	        double currentTick = origin.event.getTick();
	        double nextTick = destination.event.getTick();
	        double travelPercentComplete = (tick - currentTick) / (nextTick - currentTick);

			double dx = destination.x - origin.x;
			double dy = destination.y - origin.y;
			circleX = origin.x + (dx*travelPercentComplete);
			circleY = origin.y + (dy*travelPercentComplete);
			
			// display arcs only just after the actions occur
			if( travelPercentComplete < 0.10 )
			{
			    setCurrentArcSource( origin );
			}
			else
			{
			    setCurrentArcSource(null);					
			}
        }
   }
    
    private void setCurrentArcSource( MovementDestination arcSource )
    {
       if( arcSource != null && currentArcSource != arcSource )
        {
            arcFlashTimer.start();
        }
        
        currentArcSource = arcSource;
    }
    
    private void updateArcFlash( double percentComplete )
    {
        double newAlpha = 255 * (1.0-percentComplete);
        
        if( newAlpha > 255 ) newAlpha = 255;
        if( newAlpha < UNSELECTED_ALPHA ) newAlpha = UNSELECTED_ALPHA;
        //System.out.println("## "+getPlayerName()+"-- alpha:"+newAlpha);
        
        arcFlashAlpha = (int)newAlpha;
        this.circleIsDirty = true;
        this.getDiscourseFieldCircle().getNavigator().dirty();
    }
 
    public synchronized void addMoveToCourse( MoveEvent event )
    {
    	MovementDestination dest = null;
    	
    	if( destinationList.size() == 0 )
    	{
            double startX = DiscourseFieldCircle.OPTIMAL_CENTER_X; 
            double startY = DiscourseFieldCircle.OPTIMAL_CENTER_Y;
            dest = new MovementDestination(startX,startY,event);
    	}
    	else
    	{
      		MovementDestination lastMove = (MovementDestination) destinationList.getLast();
      		dest = new MovementDestination(lastMove.x,lastMove.y,event);
    	}
        if (!(event instanceof StartingMoveEvent)) 
    	{
            if (entryDate == null || entryDate.after(event.getMove().getSubmissionDate()))
	  		{
	  			entryDate = event.getMove().getSubmissionDate();
	  		}
    	}
    	
    	destinationList.add(dest);
    	
        // move forward to this tick
        moveToTick(event.getTick());
    }

    public void paintArcs(Graphics g, double aspectW, double aspectH)
    {
    	Graphics2D g2 = Ivanhoe.getGraphics2D(g);
    	// don't draw anything if the player hasn't done anything.
        if( isInvisible ) return; 
        
    	if( currentArcSource != null )
    	{
    	    currentArcSource.paintActionArcs(g2, aspectW, aspectH);
	    	if ( this.isSelected )
	    	{
	    		currentArcSource.paintDocumentStateArcs(g2, aspectW, aspectH);
	    	}
    	}
    }
    
    public boolean usingSelectedColor()
    {
    	return (this.isSelected == false && this.isRolledOver == false);
    }
    
    public void paint(Graphics g, double aspectW, double aspectH)
    {
    	paint(g, aspectW, aspectH, getViewTransform(aspectW,aspectH));
    }
    
    public void paint(Graphics g, double aspectW, double aspectH, AffineTransform transform)
    {
    	if ( isInvisible ) return;

    	Graphics2D g2 = Ivanhoe.getGraphics2D(g);
                
        Color playerFillColor, playerStrokeColor, textColor;
        if( usingSelectedColor() )
        {
            textColor = unselectedTextColor;
            playerFillColor = Ivanhoe.addAlphaToColor(role.getFillPaint(),getArcFlashAlpha());
            playerStrokeColor = Ivanhoe.addAlphaToColor(role.getStrokePaint(),getArcFlashAlpha());
        }
        else
        {
            textColor = selectedTextColor;
            playerFillColor = role.getFillPaint();
            playerStrokeColor = role.getStrokePaint();
        }
        
        Area transformedCircle = new Area(transform.createTransformedShape(getCircleArea(aspectW,aspectH)));

        g2.setPaint(playerFillColor);
        g2.fill(transformedCircle);
        g2.setColor(playerStrokeColor);
        g2.setStroke(pcStroke);
        g2.draw(transformedCircle);
        
        int labelX = (int)transform.getTranslateX();
        int labelY = (int)transform.getTranslateY() + Y_LABEL_PADDING;
        discourseFieldCircle.getNavigator().getLabelManager().addLabel(
                role.getName(), labelX, labelY, 0, labelFont, textColor, LabelManager.CENTER_ORIGIN );
    }

    public boolean testSelection( Point p, int numClicks )
    {
    	return testSelection(p, numClicks, getViewTransform(lastCircleAspectW, lastCircleAspectH));
    }
    
    public boolean testSelection( Point p, int numClicks, AffineTransform transform )
    {
    	Area transformedCircle = new Area(
    			transform.createTransformedShape(circle));
    	
    	if ( isInvisible )
    	{
    		return false;
    	}
    	boolean wasHit = false;
    	if (transformedCircle.contains(p))
    	{
    		wasHit = true;
    	}
    	
    	    	
    	if ( wasHit && doSelection(p, numClicks))
    	{
    		return true;
    	}
    	
    	
    	if ( currentArcSource != null )
    	{
    		return currentArcSource.testSelection(p, numClicks);
    	}
    	
    	return false;
    }
    
    public boolean doSelection(Point p, int numClicks)
    {
    	if (!isSelected)
    	{
    		// if the circle is selected, then it's no longer single-clickable:
    		discourseFieldCircle.getNavigator().setCursor(
    				Cursor.getDefaultCursor());
    		discourseFieldCircle.selectPlayerCircle(this);
    		return true;
    	}
    	return false;
    }
    
    public boolean testHighlight( Point p )
    {
        if (isInvisible)
        {
        	return false;
        }
    	
    	if ( currentArcSource != null && currentArcSource.testHighlight(p))
    	{
    		return true;
    	}
    	
    	if (testHighlight(p, getViewTransform(lastCircleAspectW, lastCircleAspectH)))
    	{
    		doHighlightSecondary(p);
    		return true;
    	}
    	return false;
    }
    
    public boolean testHighlight( Point p, AffineTransform transform )
    {
    	Area transformedCircle = new Area(
    			transform.createTransformedShape(circle));
  
    	if ( isInvisible )
    	{
    		return false;
    	}
    	
    	if ( transformedCircle.contains(p) )
    	{
    		return doHighlight(p);
    	}
       	
		if( isRolledOver == true )
		{
			isRolledOver = false;
			discourseFieldCircle.getNavigator().dirty();
		}

    	return false;
    }
    
    /**
     * Execute the primary functionality of the highlighting
     * @param p point at which the highlight occurs
     * @return whether or not the highlight actually took place
     */
    public boolean doHighlight(Point p)
    {
    	if ( !isSelected )
		{
			// is single-clickable, so it should be the hand cursor
			discourseFieldCircle.getNavigator().setCursor(
					Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			
			if( isRolledOver == false )
			{
				setRolledOver();
			}
		}
    	
    	return true;
    }
    
    public boolean doHighlightSecondary(Point p)
    {
        DiscourseField discourseField = this.getDiscourseFieldCircle().getNavigator().getDiscourseField();
    	List moveHistory = discourseField.getMoveHistory(role.getName());
		
		if ( currentArcSource == null || currentArcSource.actionArcList.isEmpty() )
		{
			return false;
		}
		
		IvanhoeAction firstAction = 
			((ActionArc)currentArcSource.actionArcList.getFirst()).getAction();
        Move displayedMove = null;
		for (Iterator i=moveHistory.iterator(); i.hasNext(); )
        {
			displayedMove = (Move)i.next();
			if (displayedMove.containsAction( firstAction.getId() ))
			{
				break;
			}
		}
		
		if (displayedMove != null)
        {
		    InfoPanel actionInfo = getDiscourseFieldCircle().getNavigator().getInfoPanel();
		    String msgContent = displayedMove.getDescription();

            // Perhaps it'd be nice if these things were truncated at a certain
            // length, but that's not the way it's working for the time being. 
		    actionInfo.setMsg(msgContent);
		    actionInfo.setLocation(p.x + 10, p.y);
		    actionInfo.setVisible(true);
        }
		
		return true;
    }
    
    private AffineTransform getViewTransform(double aspectW, double aspectH)
    {
    	AffineTransform transform = new AffineTransform();
    	transform.translate(getViewCenterX(aspectW,aspectH), getViewCenterY(aspectW,aspectH));
    	return transform;
    }
    
    public double getViewCenterX(double aspectW, double aspectH) 
    {
    	return getViewCenterX(aspectW, aspectH, circleX);
    }
    
    public double getViewCenterY(double aspectW, double aspectH) 
    {
    	return getViewCenterY(aspectW, aspectH, circleY);
    }
    
    public double getViewCenterX(double aspectW, double aspectH, double locationX)
    {
        double constrainingAspect;
        if (aspectW < aspectH)
            constrainingAspect = aspectW;
        else 
        	constrainingAspect = aspectH;

        double optimalCenterX = locationX - DiscourseFieldCircle.OPTIMAL_CENTER_X;
        double centerX = discourseFieldCircle.getCenterX() + (optimalCenterX * constrainingAspect);
        return centerX;
    }
    
    public double getViewCenterY(double aspectW, double aspectH, double locationY)
    {
        double constrainingAspect;
        if (aspectW < aspectH)
            constrainingAspect = aspectW;
        else 
        	constrainingAspect = aspectH;

        double optimalCenterY = locationY - DiscourseFieldCircle.OPTIMAL_CENTER_Y;
        double centerY = discourseFieldCircle.getCenterY() + (optimalCenterY * constrainingAspect);       
        return centerY;
    }
    
    public static double getRadius(double aspectW, double aspectH)
    {
    	double constrainingAspect;
        if (aspectW < aspectH)
            constrainingAspect = aspectW;
        else 
        	constrainingAspect = aspectH;
        
        return OPTIMAL_RADIUS * constrainingAspect;        
    }
    
    private Ellipse2D constructCircle(double aspectW, double aspectH)
    {   
        double radius = getRadius(aspectW, aspectH);
        
        double x = -radius;
        double y = -radius;
        double w = radius * 2.0;
        double h = w;
        return new Ellipse2D.Double(x, y, w, h);
    }
    
    /**
     * @return Returns the X coordinate of the circle's center in world space.
     */
    public double getCenterX()
    {
        return circleX;
    }

    /**
     * @return Returns the Y coordinate of the circle's center in world space.
     */
    public double getCenterY()
    {
        return circleY;
    }

    /**
     * @return Returns the name of the role this circle represents.
     */
    public String getRoleName()
    {
        return role.getName();
    }

    /**
     * @return the player's color
     */
    public Color getFillColor()
    {
    	return role.getFillPaint();
    }
	/**
	 * @return Returns the discourseFieldCircle.
	 */
	public DiscourseFieldCircle getDiscourseFieldCircle()
	{
		return discourseFieldCircle;
	}

	/**
	 * @return whether or not this circle is the selected circle
	 */
	public boolean isSelected()
	{
		return isSelected;
	}
	
	/**
	 * Set whether or not this player circle is the selected one
	 * @param s
	 */
	public void setSelection(boolean s)
	{
		isSelected = s;
		if ( this.isSelected )
		{
			pcStroke = new BasicStroke(SELECTED_STROKE);
		} else {
			pcStroke = new BasicStroke(UNSELECTED_STROKE);
		}
	}

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.model.event.IRoleListener#roleChanged()
     */
    public void roleChanged()
    {
        role = discourseFieldCircle.getNavigator().getRoleManager().getRole(role.getId());
        this.discourseFieldCircle.getNavigator().dirty();
    }
	
	public Color getStrokeColor()
	{
		return role.getStrokePaint();
	}
	
	public Area getCircleArea(double aspectW, double aspectH)
	{
		if (aspectW != lastCircleAspectW || aspectH != lastCircleAspectH)
		{
			lastCircleAspectW = aspectW;
			lastCircleAspectH = aspectH;
			circleIsDirty = true;
		}
		
		if (circleIsDirty)
		{
			circle = constructCircle(aspectW, aspectH);
			circleIsDirty = false;
		}
		
	    return new Area(circle);
	}
	
	public void useInspirationLines(DiscourseFieldCircle dfCircle)
	{
		if (currentMove != null)
		{
			currentMove.useInspirationLines(dfCircle);
		}
	}
    /**
     * @return Returns the arcFlashAlpha.
     */
    public int getArcFlashAlpha()
    {
        return arcFlashAlpha;
    }

    /**
	 * @return Returns the currentMove.
	 */
	public MoveEvent getCurrentMoveEvent()
	{
	    if( currentMove == null ) return null;
		else return currentMove.event;
	}
	
	public double getLastCircleAspectH() 
	{
		return lastCircleAspectH;
	}
	
	public double getLastCircleAspectW() 
	{
		return lastCircleAspectW;
	}
	
	public boolean isVisible()
	{
		return !isInvisible;
	}
	
	private void setRolledOver()
	{
		if (!isRolledOver)
		{
			isRolledOver = true;
			discourseFieldCircle.getNavigator().dirty();
		}
	}
	
	public Date getEntryDate()
	{
		return entryDate;
	}
    
    public DocumentVersionManager getDocumentVersionManager()
    {
        return dvManager;
    }
}