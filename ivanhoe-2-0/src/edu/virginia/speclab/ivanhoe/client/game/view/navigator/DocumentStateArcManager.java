/*
 * Created on Aug 9, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator;

import java.awt.*;
import java.util.*;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;

/**
 * @author benc
 */

public class DocumentStateArcManager implements IActionSelectionListener
{
    private DiscourseFieldNavigator navigator;
    private DocumentStateArc selectedArc;
    private IvanhoeAction selectedAction;
	private LinkedList dsArcRings; // contains lists of ranges
	private LinkedList arcs;
	
	public DocumentStateArcManager(DiscourseFieldNavigator navigator)
	{
	    this.navigator = navigator;
	    this.navigator.getActionSelection().addListener(this);
	    this.selectedAction = navigator.getActionSelection().getSelectedAction();
	    this.actionSelectionChanged(selectedAction);
	    
		dsArcRings = new LinkedList();
		arcs = new LinkedList();
	}
	
	public void cleanUp()
	{
	    this.navigator.getActionSelection().removeListener(this);
	}
	
	public void addDocStateArc(DocumentStateArc dsArc)
	{
		arcs.add(dsArc);
		dsArc.setSelection(false);
		
		if (selectedAction != null && selectedArc == null)
		{
		    if (dsArc.getAction().equals(selectedAction)) dsArc.setSelection(true);
		}
	}
	
	public void paintArcs(Graphics g, double aspectW, double aspectH)
	{
		Graphics2D g2 = Ivanhoe.getGraphics2D(g);
		LinkedList nonWedgeArcs = new LinkedList();
		LinkedList wedgeArcs = new LinkedList();
		double constrainingAspect;
		if( aspectW < aspectH ) constrainingAspect = aspectW;
        else                    constrainingAspect = aspectH;
	
		for (Iterator i=arcs.iterator(); i.hasNext(); )
		{
			DocumentStateArc dsArc = (DocumentStateArc)i.next();
			double arcLength = dsArc.getActionArc().getArcLength();
			if (Math.abs(arcLength) < ActionArc.getMinimumArcAngle(aspectW, aspectH) )
			{
				wedgeArcs.add(dsArc);
			}
			else
			{
				nonWedgeArcs.add(dsArc);
			}

		}
		
		for (Iterator i=nonWedgeArcs.iterator(); i.hasNext(); )
		{
			addArcToRings((DocumentStateArc)i.next(), aspectW, aspectH, constrainingAspect);
		}
		
		for (Iterator i=wedgeArcs.iterator(); i.hasNext(); )
		{
			addArcToRings((DocumentStateArc)i.next(), aspectW, aspectH, constrainingAspect);
		}
		
		int ringNumber = 0;
		for (Iterator i=dsArcRings.iterator(); i.hasNext(); )
		{
			LinkedList ring = (LinkedList)i.next();
			for (Iterator j=ring.iterator(); j.hasNext(); )
			{
				ArcRange arcRange = (ArcRange)j.next();
				arcRange.dsArc.paint(g2,aspectW,aspectH,ringNumber,arcRange.isWedge);
			}

			++ringNumber;
		}		
	}
	
	private void addArcToRings(DocumentStateArc dsArc, double aspectW, double aspectH, double constrainingAspect)
	{
		ArcRange range = new ArcRange(dsArc, aspectW, aspectH, constrainingAspect);
		boolean collided;
		
		ListIterator i = dsArcRings.listIterator();
		ListIterator j;
		do
		{
			collided = false;
			
			LinkedList ring;
			if (!i.hasNext()){
				int iteratorOffset = i.nextIndex();
				ring = new LinkedList();
				dsArcRings.addLast(ring);
				i = dsArcRings.listIterator(iteratorOffset);
			}
			else
			{
				ring = (LinkedList)i.next();
			}
			
			for(j=ring.listIterator(); j.hasNext(); )
			{
				ArcRange otherRange = (ArcRange)j.next();
				if (range.intersects(otherRange))
				{
					collided = true;
					break;
				}
			}
		} while (collided);

		j.add(range);
	}

	public void actionSelectionChanged(IvanhoeAction action)
	{
	    if (selectedArc != null)
	    {
	        selectedArc.setSelection(false);
	        selectedArc = null;
	    }
	    
	    this.selectedAction = action;
	    
	    if (selectedAction != null && arcs != null)
	    {
		    for ( Iterator i=arcs.iterator(); i.hasNext(); )
		    {
		        DocumentStateArc dsArc = ((DocumentStateArc)i.next());
		        if (dsArc.getAction().equals(action))
		        {
		            selectedArc = dsArc;
		            dsArc.setSelection(true);
		        }
		    }
	    }
	    
	    if (navigator != null) navigator.dirty();
	}
	
	private class ArcRange 
	{
		ArcRange(DocumentStateArc dsArc, double aspectW, double aspectH, double constrainingAspect)
		{
			this.dsArc = dsArc;
			double arcLength = dsArc.getActionArc().getArcLength();
			double arcAngle = dsArc.getActionArc().getAngle();
			double minArcAngle = ActionArc.getMinimumArcAngle(aspectW, aspectH);
			
			this.isWedge = Math.abs(arcLength) < minArcAngle;
			
			if (this.isWedge) 
			{
				double wedgeAngularExtent = DocumentStateArc.OUTER_ARC_WIDTH / (constrainingAspect * 2.0);
				this.start = arcAngle - wedgeAngularExtent / 2.0;
				this.extent = wedgeAngularExtent;
			} 
			else 
			{
				this.start = arcAngle;
				this.extent = arcLength;
			}
			
		}
		
		public double start, extent;
		public boolean isWedge;
		public final DocumentStateArc dsArc;
		
		public double getEnd()
		{
			return start + extent;
		}
		
		public boolean intersects(ArcRange that)
		{
			double thisEnd = this.getEnd();
			double thatEnd = that.getEnd();
			
			// TODO: this doesn't account for the fact that this is a circle
			//   but i'm not sure if the rest of the code will generate numbers
			//   where that is a problem, since all action arcs must be inside
			//   a single DocumentStateArc.  Investigate.
			
			// this starts in the middle of that
			if (this.start < thatEnd && this.start >= that.start) 
			{
				return true;
			}
			// that starts in the middle of this
			if (thisEnd > that.start && thisEnd <= thatEnd)
			{
				return true;
			}
			// this encloses that
			if (thisEnd >= thatEnd && this.start <= that.start)
			{
				return true;
			}
			// that encloses this
			if (thisEnd <= thatEnd && this.start >= that.start)
			{
				return true;
			}
			
			return false;
		}
		
		public String toString()
		{
			return "(start: "+start+";\textent: "+extent+";\tisWedge: "+isWedge+")";
		}
	}
}
