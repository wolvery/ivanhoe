/*
 * Created on Oct 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse;

import java.util.Iterator;
import java.util.LinkedList;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;


/**
 * 
 * @author Nick
 */
public class DiscourseFieldTime
{
    private MoveEvent currentMoveEvent;
    private int currentTick;
    private DiscourseFieldTimeline timeline;
    
    private LinkedList listeners;
    
    public DiscourseFieldTime( DiscourseFieldTimeline timeline )
    {
        this.timeline = timeline;
        
        listeners = new LinkedList();

        // move time to the starting move
        StartingMoveEvent event = (StartingMoveEvent) timeline.getTimeline().getFirst();              
        setMoveEvent(event);
    }
    
    public void addListener( IDiscourseFieldTimeListener listener )
    {
        listeners.add(listener);
    }
    
    public void removeListener( IDiscourseFieldTimeListener listener )
    {
        listeners.remove(listener);
    }
    
    private void fireTickChange()
    {        
        for( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            IDiscourseFieldTimeListener listener = (IDiscourseFieldTimeListener) i.next();
            listener.discourseFieldTickChanged(currentTick);
        }        
    }
    
    private void fireMoveEventChange()
    {        
        for( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            IDiscourseFieldTimeListener listener = (IDiscourseFieldTimeListener) i.next();
            listener.discourseFieldMoveEventChanged(currentMoveEvent);
        }        
    }

    /**
     * @return Returns the currentMove, null if the time value has been updated in ticks.
     */
    public MoveEvent getMoveEvent()
    {
        return currentMoveEvent;
    }
    
    /**
     * @return Update the current move event based on the current tick value
     */
    public MoveEvent updateMoveEvent()
    {
        // if we are currently tracking by ticks, look up the move event
        if( currentMoveEvent == null )
        {            
            currentMoveEvent = lookupMoveEvent();
        }
        
        return currentMoveEvent;
    }
    
    /**
     * @return Look up the current move event based on the current tick value
     */
    public MoveEvent lookupMoveEvent()
    {
        // if we are currently tracking by ticks, look up the move event
        if( currentMoveEvent == null )
        {            
            return timeline.lookupEvent(currentTick);
        }
        else
        {
            return currentMoveEvent;    
        }
    }


    /**
     * @param currentMove The currentMove to set.
     */
    public void setMoveEvent(MoveEvent currentMove)
    {
        this.currentMoveEvent = currentMove;
        this.currentTick = currentMove.getTick();
        fireMoveEventChange();
    }
    
    /**
     * @return Returns the currentTick.
     */
    public int getTick()
    {
        return currentTick;
    }
    /**
     * @param currentTick The currentTick to set.
     */
    public void setTick(int currentTick)
    {
        if( this.currentTick == currentTick ) return;
        this.currentTick = currentTick;
        // now tracking by ticks, clear this
        this.currentMoveEvent = null;
        fireTickChange();
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.model.discourse.ITimeLineListener#addNewDocument(edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo)
     */
    public void addNewDocument(DocumentInfo docInfo)
    {
        // do nothing.
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.model.discourse.ITimeLineListener#removeDocument(edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo)
     */
    public void removeDocument(DocumentInfo docInfo)
    {
        // do nothing.
    }
    
}
