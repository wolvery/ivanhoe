/*
/*
 * Created on Apr 26, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator;

import java.awt.*;
import java.awt.geom.*;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTimeline;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.ColorPair;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.CustomColors;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.ActionType;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;
import edu.virginia.speclab.ivanhoe.shared.data.Link;
import edu.virginia.speclab.ivanhoe.shared.data.LinkType;
import edu.virginia.speclab.ivanhoe.shared.data.Move;

/**
 * An arc representing a single player action within a given discourse field by a specific player. 
 * @author Nick Laiacona
 */
public class ActionArc 
{
    private final DocumentVersion documentVersion;
    private PlayerCircle playerCircle;
    private DiscourseFieldStateManager documentAreaManager;
	private DocumentStateArc documentStateArc;
	private IvanhoeAction action;
	private final Move move;
   	
    private double angle, arcLength;
    
    private static final Stroke arcStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    private static final float minimumArcAngleFactor = 3.0f;
    
    public ActionArc( PlayerCircle pCircle, DiscourseFieldStateManager manager,
    		DocumentStateArcManager dsaManager,
            IvanhoeAction act, Move move, double actionTime  )
    {
        this.playerCircle = pCircle;
        this.documentVersion = pCircle.getDocumentVersionManager().getDocumentVersion(act);
        
        if (documentVersion == null)
        {
            throw new RuntimeException("Action ["+act+"] does not correspond to any known DocumentVersion");
        }
        
        this.documentAreaManager = manager;

        this.action = act;    	
        this.move = move;
        moveToTick(actionTime);
        documentStateArc = new DocumentStateArc(pCircle, manager, this, documentVersion);
    }
    
    public DocumentStateArc getDocumentStateArc()
    {
    	return documentStateArc;
    }
    
    public void moveToTick( double actionTime )
    {        
        // Gather action offset and length

    	int actionOffset, actionLength;
    	
        if( action.getType().equals(ActionType.LINK_ACTION) == true )
    	{
    	    actionOffset = action.getOffset();
    	    Link linkTarget = (Link)action.getContent();
    	    actionLength = linkTarget.getAnchorText().length();
    	}
    	else 
        {
            actionOffset = action.getOffset();
    	    actionLength = action.getLength();   	       	        	    
    	}
        
        String docName = documentVersion.getDocumentTitle();	    
        DocumentArc documentArc = documentAreaManager.getDocumentArc(docName,actionTime);
        
	    if( action.getType().equals(ActionType.ADD_DOC_ACTION) == true )
		{
		    angle = documentArc.angle;
		    arcLength = documentArc.length;
		}
		else
		{
	        // Determine the total length of the document	        
	        final DiscourseFieldTimeline timeline = this.playerCircle.getDiscourseFieldCircle().getNavigator().getDiscourseFieldTimeline();
	        final int documentLength = timeline.getDocumentLength(documentVersion);
	
	        if( documentLength == 0 )
	        {
	            SimpleLogger.logError("Document: "+docName+" length=0, cannot update ActionArc.");
	            angle = 0;
	            arcLength = 0;
	        }
	        else
	        {
	            // calculate the angle of the action arc
	            double angleOffset = (documentArc.length * actionOffset) / documentLength;
	            angle = documentArc.angle + angleOffset;
	            
	            if( action.getType().equals(ActionType.ADD_ACTION) == true ||
	                action.getType().equals(ActionType.IMAGE_ACTION) == true	  )
	            {
	                arcLength = -0.01;
	            }
	            else
	            {
		            // calculate the arc length                    
		            arcLength = (documentArc.length * actionLength) / documentLength;                    
	            }
	        }
		}
	    
        // update the document state arc to reflect the new angle
	    if( documentStateArc != null )
	    {
	        documentStateArc.updateDocumentStateArc( angle, arcLength );
	    }
	}     
    
    /**
     * Returns an angle to the center of the arc's span in degrees.
     * @return 
     */
    public double getCenter()
    {
    	return (arcLength/2.0) + angle;		
    }
    
    /**
     * Returns the length of the arc in degrees.
     * @return
     */
    public double getArcLength()
    {
    	return arcLength;
    }
    
    public double getAngle()
    {
    	return angle;
    }
    
    private void drawLine( Graphics g, double aspectW, double aspectH )
    {
        Graphics2D g2 = Ivanhoe.getGraphics2D(g);
		Line2D line = constructLine(aspectW, aspectH);
		
		Color strokeColor;
		if( this.playerCircle.isSelected() == true )
		{
		    strokeColor = getStrokeColor();
		}
		else
		{
		    strokeColor = Ivanhoe.addAlphaToColor(getStrokeColor(),playerCircle.getArcFlashAlpha());
		}

		g2.setStroke(arcStroke);
		g2.setPaint(strokeColor);
		g2.draw(line);       
    }
    
    private void drawArc( Graphics g, double aspectW, double aspectH )
    {
        Graphics2D g2 = Ivanhoe.getGraphics2D(g);

		Area arc = constructArea(aspectW, aspectH);
		
		Rectangle arcAreaBounds = arc.getBounds();

		Paint arcPaint;
		if( this.playerCircle.isSelected() == true )
		{
			Color translucentFillColor = Ivanhoe.addAlphaToColor(getFillColor(),0);

			arcPaint = new GradientPaint(
					arcAreaBounds.x,
					arcAreaBounds.y, 
					translucentFillColor,
					(arcAreaBounds.x + arcAreaBounds.width),
					(arcAreaBounds.y + arcAreaBounds.height), 
					Color.black,
					true);
		}
		else
		{
		    arcPaint = Ivanhoe.addAlphaToColor(getFillColor(),playerCircle.getArcFlashAlpha());
		}

		g2.setPaint(arcPaint);
		g2.fill(arc);
		
		Color strokeColor;
		if( this.playerCircle.isSelected() == true )
		{
		    strokeColor = getStrokeColor();
		}
		else
		{
		    strokeColor = Ivanhoe.addAlphaToColor(getStrokeColor(),50);
		}

		g2.setColor(strokeColor);
		g2.setStroke(arcStroke);
		g2.draw(arc);
    }
    
    public static double getMinimumArcAngle(double aspectW, double aspectH)
    {
    	double constrainingAspect;
        if( aspectW < aspectH ) constrainingAspect = aspectW;
        else                    constrainingAspect = aspectH;
        
    	double dfRadius = DiscourseFieldCircle.OPTIMAL_RADIUS * constrainingAspect;
        
        // there should be roughly minimumArcAngleFactor pixels for each degree of arc
    	return (180.0 / (dfRadius * Math.PI) ) * minimumArcAngleFactor;
    }
    
    public void paint(Graphics g, double aspectW, double aspectH) 
    {
		// if the angle is too small, just draw a line
        if (getMinimumArcAngle(aspectW, aspectH) > Math.abs(arcLength))
		{
        	drawLine(g,aspectW,aspectH);
		} 
		else 
		{
			drawArc(g,aspectW,aspectH);
		}
	}
    
    // take the point specified on circle alpha and calculate the angle from the
	// center
    // of beta to arrive at the same point
    private double translateArcAngle( double alphaCenterX, double alphaCenterY, double alphaRadius, double alphaAngle, double betaCenterX, double betaCenterY )
    {
        // convert the point to cartesian coordinates
        double targetPointX = Math.cos(Math.toRadians(alphaAngle))* alphaRadius + alphaCenterX;
        double targetPointY = Math.sin(Math.toRadians(alphaAngle)) * -alphaRadius + alphaCenterY;
         
        // convert back to polar coordinates in beta circle
        double deltaX = targetPointX - betaCenterX;
        double deltaY = (targetPointY - betaCenterY) * -1;
        
        double betaAngle = 0.0;
        
        // calculate the angle in the correct quadrant 
        
        if( deltaX >= 0 && deltaY >= 0 )
        {
            betaAngle = Math.toDegrees(Math.atan(deltaY/deltaX));    
        }
        else if( deltaX < 0 && deltaY >= 0 )
        {
            betaAngle = Math.toDegrees(Math.atan(deltaY/deltaX)) + 180;
        }
        else if( deltaX < 0 && deltaY < 0 )
        {
            betaAngle = Math.toDegrees(Math.atan(deltaY/deltaX)) + 180;
        }
        else if( deltaX >= 0 && deltaY < 0 )
        {
            betaAngle = Math.toDegrees(Math.atan(deltaY/deltaX));
        }
                
        // unscrew it 
        betaAngle %= 360;
        
        // always positive 
        if( betaAngle < 0 )
            betaAngle = 360 + betaAngle;

        return betaAngle;        
    }
    
    private Line2D constructLine( double aspectW, double aspectH )
    {
        Dimension windowSize = playerCircle.getDiscourseFieldCircle().getNavigator().getSize();
        double dfCenterX = windowSize.width/2.0;
        double dfCenterY = windowSize.height/2.0;
        
        //convert the player's location from world space to screen space.
        double centerX = playerCircle.getViewCenterX(aspectW,aspectH);
        double centerY = playerCircle.getViewCenterY(aspectW,aspectH);
        
        double constrainingAspect;
        if( aspectW < aspectH ) constrainingAspect = aspectW;
        else                    constrainingAspect = aspectH;
        
        // the radius for the circle shape used to draw the arcs is twice the size of the df circle
        double dfRadius = DiscourseFieldCircle.OPTIMAL_RADIUS * constrainingAspect;

        double unitX = Math.cos(Math.toRadians(angle));
        double unitY = -Math.sin(Math.toRadians(angle));
        double pcRadius = PlayerCircle.getRadius(aspectW,aspectH);
        
		Point.Double lineEnd = new Point.Double(
				unitX * dfRadius + dfCenterX,
		        unitY * dfRadius + dfCenterY);
		
		double lineLength = Math.sqrt(
				Math.pow(lineEnd.x - centerX, 2) +
				Math.pow(lineEnd.y - centerY, 2));
				
        Point.Double lineBegin = new Point.Double(
				(lineEnd.x - centerX) * pcRadius / lineLength + centerX,
				(lineEnd.y - centerY) * pcRadius / lineLength + centerY);
		Line2D.Double line = new Line2D.Double(lineBegin, lineEnd);
		
		return line;
    }
    
    private Area constructArea( double aspectW, double aspectH )
    {
        if (Math.abs(arcLength) >= 360.0)
        {
            return constructCircle(aspectW, aspectH);
        }
        else
        {
            return constructArc(aspectW, aspectH);
        }
    }
    
    private Area constructCircle( double aspectW, double aspectH )
    {
        Dimension windowSize = playerCircle.getDiscourseFieldCircle().getNavigator().getSize();
        double dfCenterX = windowSize.width/2.0;
        double dfCenterY = windowSize.height/2.0;
        
        double constrainingAspect;
        if( aspectW < aspectH ) constrainingAspect = aspectW;
        else                    constrainingAspect = aspectH;
        
        double dfRadius = DiscourseFieldCircle.OPTIMAL_RADIUS * constrainingAspect;
        
//      create the discourse field circle
        double dfX = dfCenterX - dfRadius;
        double dfY = dfCenterY - dfRadius;
        double dfW = dfRadius * 2.0;
        double dfH = dfW;
        
        Shape circle = new Ellipse2D.Double(dfX,dfY,dfW,dfH);
        Area circleArea = new Area(circle);
        
        Area playerCircleArea = this.playerCircle.getCircleArea(aspectW, aspectH);
        circleArea.subtract(playerCircleArea);
        
        return circleArea;
    }
    
    private Area constructArc( double aspectW, double aspectH )
    {
        Dimension windowSize = playerCircle.getDiscourseFieldCircle().getNavigator().getSize();
        double dfCenterX = windowSize.width/2.0;
        double dfCenterY = windowSize.height/2.0;
        
        //convert the player's location from world space to screen space.
        double centerX = playerCircle.getViewCenterX(aspectW,aspectH);
        double centerY = playerCircle.getViewCenterY(aspectW,aspectH);
        
        double constrainingAspect;
        if( aspectW < aspectH ) constrainingAspect = aspectW;
        else                    constrainingAspect = aspectH;
        
        // the radius for the circle shape used to draw the arcs is twice the size of the df circle
        double dfRadius = DiscourseFieldCircle.OPTIMAL_RADIUS * constrainingAspect;
        double radius = dfRadius * 2.0;
        
    	double x = centerX - radius;
        double y = centerY - radius;
        double w = radius * 2.0;
        double h = w;
        
        double visibleArcAngleStart = translateArcAngle(dfCenterX,dfCenterY,dfRadius,angle,centerX,centerY);
        double visibleArcAngleEnd = translateArcAngle(dfCenterX,dfCenterY,dfRadius,angle+arcLength,centerX,centerY);
        
        // always keep the rotation going counter clockwise so arc draws right
        if( visibleArcAngleEnd > visibleArcAngleStart )
        {
            visibleArcAngleEnd -= 360.0;
        }
        
        double visibleArcAngleLength = visibleArcAngleEnd - visibleArcAngleStart; 
        
        Shape arc = new Arc2D.Double(x,y,w,h,visibleArcAngleStart,visibleArcAngleLength,Arc2D.PIE);

        // create the discourse field circle
        double dfX = dfCenterX - dfRadius;
        double dfY = dfCenterY - dfRadius;
        double dfW = dfRadius * 2.0;
        double dfH = dfW;
        
        Shape circle = new Ellipse2D.Double(dfX,dfY,dfW,dfH);
                    
        // The arc is the intersection of these shapes
        Area arcArea = new Area(arc);
        Area circleArea = new Area(circle);
        Area playerCircleArea = this.playerCircle.getCircleArea(aspectW, aspectH);
        arcArea.intersect(circleArea);
        arcArea.subtract(playerCircleArea);
        
        return arcArea;
    }
    
    public Color getStrokeColor()
    {
        ColorPair colorPair = getArcColors();
        if( colorPair != null )
        {
            return colorPair.strokeColor;
        }
        else return Color.WHITE;
    }
    
    public Color getFillColor()
    {
        ColorPair colorPair = getArcColors();
        if( colorPair != null )
        {          
            return colorPair.fillColor;
        }
        else return Color.WHITE;
    }

    private ColorPair getArcColors()
    {
        CustomColors customColors = this.playerCircle.getDiscourseFieldCircle().getNavigator().getCustomColors();
        ActionType type = action.getType();
        
        if( type.equals(ActionType.ADD_ACTION)     == true || 
            type.equals(ActionType.IMAGE_ACTION)   == true ||
            type.equals(ActionType.ADD_DOC_ACTION) == true    )
        {
            return customColors.getInsertionArcColors();
        }
        else if( type.equals(ActionType.DELETE_ACTION) == true )
        {
            return customColors.getDeletionArcColors();
        }
        else if( type.equals(ActionType.LINK_ACTION) == true )
        {  
            if( action.getContent() instanceof Link )
            {
                Link link = (Link) action.getContent();
                LinkType linkType = link.getType();
                
                if( linkType.equals(LinkType.INTERNAL) == true ||
                    linkType.equals(LinkType.URL) == true )
                {
                    return customColors.getLinkArcColors();
                }
                else
                {
                    return customColors.getAnnotationArcColors();
                }
            }            
        }
        
        return null;
    }
    
    public IvanhoeAction getAction()
    {
    	return action;
    }

	public Move getMove() {
		return move;
	}
}
