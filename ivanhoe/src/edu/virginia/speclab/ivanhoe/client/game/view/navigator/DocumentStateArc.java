/*
 * Created on June 7, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator;

import java.awt.*;
import java.awt.geom.*;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;
import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.document.DocumentEditor;
import edu.virginia.speclab.ivanhoe.client.game.view.document.InfoPanel;

/**
 * 
 * @author Ben Cummings
 */
public class DocumentStateArc
{
    private final DocumentVersion documentVersion;
    
	private PlayerCircle playerCircle;
	private double angle, arcLength;
	private ActionArc actionArc;
	private Area arcArea;
	private boolean selected;
	private boolean arcAreaDirty;
	
	private static DocumentEditor lastEditor;
	
	public static final double GUTTER_WIDTH = 8.0;
	public static final double OUTER_ARC_WIDTH = 10.0;
	private static final float STROKE_WIDTH = 2.0f;
	private static final float HIGHLIGHT_WIDTH = 6.0f;
	private static final int HIGHLIGHT_STEPS = 3;
	private static final Stroke arcStroke = new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	private static final double WEDGE_ARC_LENGTH = -60.0; 
    
	public DocumentStateArc( PlayerCircle pCircle, DiscourseFieldStateManager manager,
	                         ActionArc aArc, DocumentVersion documentVersion)
	{   
        this.documentVersion = documentVersion;
		playerCircle = pCircle;
		actionArc = aArc;
		this.updateDocumentStateArc(actionArc.getAngle(), actionArc.getArcLength());
		arcArea = new Area();
		selected = false;
		arcAreaDirty = true;
	}
    
    public Area getArea()
    {
    	// TODO: verify that arcArea is correct, since it's only updated when
    	//       you paint().
    	if (arcAreaDirty)
    	{
    		throw new RuntimeException("DocumentStateArc.getArea() called " +
    		                           "while area still dirty");
    	}
    	return arcArea;
    }
    
	public void updateDocumentStateArc( double newAngle, double newArcLength )
	{        
		angle = newAngle;
       	arcLength = newArcLength;
       	arcAreaDirty = true;    
	} 
    
	public void paint( Graphics2D g2, double aspectW, double aspectH, int drawCircle, boolean useWedge)
	{
        if (arcAreaDirty)
        {
			arcArea = constructArcArea(aspectW,aspectH,drawCircle,useWedge);
        }
        
        if (selected)
        {
            drawHighlight(g2, arcArea);
        }
        
		g2.setPaint(actionArc.getFillColor());
		g2.fill(arcArea);
        
		g2.setColor(actionArc.getStrokeColor());
		g2.setStroke(arcStroke);
       
		g2.draw(arcArea);
	}

	private void drawHighlight( Graphics2D g2, Area arc )
	{   
	    float iterationStep = (HIGHLIGHT_WIDTH - STROKE_WIDTH) / HIGHLIGHT_STEPS;
	    for (float i=HIGHLIGHT_WIDTH; i>STROKE_WIDTH; i -= iterationStep)
	    {
	        BasicStroke stroke = new BasicStroke(i, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		    g2.setColor(Ivanhoe.addAlphaToColor(Color.WHITE, (int)(128 * i/HIGHLIGHT_WIDTH)));
		    
		    g2.setStroke(stroke);
		    g2.draw(arc);
	    }
	}
	
	private Area constructArcArea(double aspectW, double aspectH, int drawCircle, boolean useWedge)
	{	
		// get the location of the center of the discourse field circle
		double centerX = playerCircle.getDiscourseFieldCircle().getCenterX();
		double centerY = playerCircle.getDiscourseFieldCircle().getCenterY();
		
		double constrainingAspect;
		if( aspectW < aspectH ) constrainingAspect = aspectW;
		else                    constrainingAspect = aspectH;
		
        double drawCircleOffset = (GUTTER_WIDTH + OUTER_ARC_WIDTH) * drawCircle;
		
		if (useWedge)
		{
			// arc's too small so we're drawing a wedge
			double radius = (DiscourseFieldCircle.OPTIMAL_RADIUS + 
					GUTTER_WIDTH) * constrainingAspect + drawCircleOffset;
			 
			double wedgeRadius = OUTER_ARC_WIDTH;
   
			double x = centerX + radius * Math.cos(Math.toRadians(angle)) - wedgeRadius;
			double y = centerY + radius * -Math.sin(Math.toRadians(angle)) - wedgeRadius;
			double w = wedgeRadius * 2.0;
			double h = w;
			
			Area wedge = new Area(new Arc2D.Double(x,y,w,h,
					angle-WEDGE_ARC_LENGTH/2.0,WEDGE_ARC_LENGTH,Arc2D.PIE)); 
			return wedge;
		}
		else
		{
			double radius = (DiscourseFieldCircle.OPTIMAL_RADIUS +
					GUTTER_WIDTH) * constrainingAspect + OUTER_ARC_WIDTH +
					drawCircleOffset;
   
			double x = centerX - radius;
			double y = centerY - radius;
			double w = radius * 2.0;
			double h = w;
	
			Area outerArc = new Area(new Arc2D.Double(x,y,w,h,angle,arcLength,
					  Arc2D.PIE));
			
			radius = (DiscourseFieldCircle.OPTIMAL_RADIUS + GUTTER_WIDTH)
					 * constrainingAspect + drawCircleOffset;
	        
			x = centerX - radius;
			y = centerY - radius;
			w = radius * 2.0;
			h = w;
			
            Area innerArc = new Area(new Ellipse2D.Double(x,y,w,h));
			
			outerArc.subtract(innerArc);
			return outerArc;
		}
	}
	
	/**
	 * Process the selection event: in this case, open an editor or viewer
	 * @return whether or not the selection occured
	 */
	private boolean doSelection()
	{
		final IvanhoeAction act = actionArc.getAction();
		final DiscourseField discourseField =
                playerCircle.getDiscourseFieldCircle().getNavigator().getDiscourseField();
        final DocumentVersion version =
                discourseField.getDocumentVersionManager().getDocumentVersion(act); 
        
        final DocumentEditor editor = Workspace.instance.openEditor(version);
        editor.highlightAction(act);
        
	    // hide the info panel 
	    final InfoPanel infoPanel = Workspace.instance.getInfoPanel();
	    if( infoPanel != null ) infoPanel.setVisible(false);
	    
	    playerCircle.getDiscourseFieldCircle().getNavigator()
	    		.getActionSelection().changeSelection(this.getAction());

		return true;
	}
	
	/**
	 * Process the highlight event: popup an action summary InfoPanel
	 * @param p Point on the screen that's highlighted
	 * @return whether the highlight worked
	 */
	public boolean doHighlight( Point p )
	{	
	    DiscourseFieldNavigator navigator = playerCircle.getDiscourseFieldCircle().getNavigator(); 
		// set the cursor:
		navigator.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		// set the tooltip:
        IvanhoeAction act = actionArc.getAction(); 
		String actionHTML = "<html><center><b>Action</b></center>" + 
			act.getDescription(documentVersion,false, 25);
		navigator.getInfoPanel().setMsg(actionHTML);
		navigator.getInfoPanel().setLocation(p.x + 10, p.y);
		navigator.getInfoPanel().setVisible(true);
		
		return true;
	}
	
	/**
	 * Determine if this document state arc was hit and if so, open the
	 * corresponding document editor.
	 * 
	 * @param numClicks number of times clicked: single (1) or double (2) 
	 * @param p Selected point on the screen
	 * @return whether or not the click was handled by this object
	 */
	public boolean testSelection( Point p, int numClicks )
	{
		boolean wasSelected = false;
		
        // if the last editor we attempted to open is still loading,
        // wait until it is finished before opening a new one.
        if( lastEditor != null )
        {
            if( lastEditor.isLoading() == true ) 
            {
                return false;
            } else {
                lastEditor = null;
            }
        }
        
    	if ( arcArea.contains(p) )
    	{
    		wasSelected = doSelection();
    	}
    	return wasSelected;
	}

	/**
	 * Determine if this document state arc was moused over and if so, create
	 * a tooltip.
	 * 
	 * @param p Point on the screen that the mouse is over  
	 * @return whether or not this object is highlighted
	 */
	public boolean testHighlight( Point p )
	{
		if (arcArea.contains(p))
		{
			doHighlight(p);
			return true;
		}
		
		return false;
	}
	
	public void setSelection(boolean isSelected)
	{
	    selected = isSelected;
	}

	public ActionArc getActionArc()
	{
		return actionArc;
	}
	
	public IvanhoeAction getAction()
	{
		return actionArc.getAction();
	}
}