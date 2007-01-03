/*
 * Created on Aug 9, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse;

import java.util.Date;
import java.util.LinkedList;


/**
 * This class hides the special case neceissitate by the requirement to display the initial
 * state in the discourse field visualizations. The DiscourseField is an unordered table of
 * Moves sorted by player. The DiscourseFieldTimeline is an ordered list of MoveEvent objects
 * sorted by time. The first element in the list is always a StartingMoveEvent.
 * 
 * @author Nick
 */
public class StartingMoveEvent extends MoveEvent
{
    private LinkedList startingDocumentList;
    private Date startDate;
    
    /**
     * @param m
     */
    public StartingMoveEvent( Date startDate )
    {
        super(null, null);
        this.startDate = startDate;        
        startingDocumentList = new LinkedList();
    }
    
    public void addStartingDocument( String docName )
    {
        startingDocumentList.add(docName);
    }
    
    public LinkedList getDocumentList()
    {
        return startingDocumentList;
    }
    
    public boolean isCompressed()
    {
        return false;
    }
    
    public Date getDate()
    {
        return startDate; 
    }
    
    public long getTime()
    {
        return startDate.getTime();    
    }

    public int getTick()
    {
        return 0;
    }
}
