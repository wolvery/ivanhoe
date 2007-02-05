/*
 * Created on Jul 13, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTimeline;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.ITimeLineListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;

/**
 * @author Nick
 *
 */
public class DiscourseFieldStateManager implements ITimeLineListener 
{
    private DiscourseFieldNavigator navigator;
    private LinkedList discourseFieldEventList;
    private DiscourseFieldEvent visibleEvent;
    private DiscourseFieldEvent currentEvent;
    
    private class DiscourseFieldEvent
    {
        public DiscourseFieldState state;
        public MoveEvent moveEvent;
        
        public DiscourseFieldEvent( MoveEvent e, DiscourseFieldState state )
        {
            this.state = state;
            this.moveEvent = e;
        }        
    }
    
    public DiscourseFieldStateManager( DiscourseFieldNavigator navigator )
    {
        this.navigator = navigator;
        discourseFieldEventList = new LinkedList();
    }     
    
    public void init()
    {
        DiscourseFieldTimeline timeline = navigator.getDiscourseFieldTimeline();
        LinkedList moveEventList = timeline.getTimeline();

        // go through event list and create corresponding discourse field events
        DiscourseFieldState previousState = null;
        for( Iterator j = moveEventList.iterator(); j.hasNext(); )        
        {               
            MoveEvent event = (MoveEvent) j.next();
            previousState = addMoveEvent(previousState,event);
            updateCurrentEvent(event);
        }
        
        visibleEvent = currentEvent;        
    }

    // examine the move event and coordinate moves with doc adds
    private DiscourseFieldState addMoveEvent( DiscourseFieldState previousState, MoveEvent event )
    {        
        DiscourseFieldState state;
        if( previousState == null )
        {
            state = DiscourseFieldState.createInitialState(navigator);            
        }
        else
        {
        	state = previousState.createNewState(event.getDate());
        }
                
        LinkedList documentList = event.getDocumentList();
                    
        // add documents that were added during this move 
		for( Iterator i = documentList.iterator(); i.hasNext(); )
		{
		    String docName = (String) i.next();
		    state.addDocument(docName, event.getDate());
		}
		
		DiscourseFieldEvent newEvent = new DiscourseFieldEvent(event,state);
		discourseFieldEventList.add(newEvent);
		return state;
    }
    
    private void updateCurrentEvent( MoveEvent event )
    {
        currentEvent = new DiscourseFieldEvent(event,DiscourseFieldState.createCleanState(navigator, event.getDate()));
        
        // Make a list with all the documents
        DiscourseField discourseField = navigator.getDiscourseField();
        List docInfoList = discourseField.getDocumentInfoList();

        LinkedList documentList = new LinkedList();
        for (Iterator i = docInfoList.iterator(); i.hasNext(); )
        {
        	documentList.add(((DocumentInfo)i.next()).getTitle());
        }
        
        // ...and add them all to the current state 
		for( Iterator i = documentList.iterator(); i.hasNext(); )
		{
		    String docName = (String) i.next();
		    Date publishedTime = navigator.getDiscourseFieldTimeline().
					getDocumentPublishedTime(docName);
            currentEvent.state.addDocument(docName, publishedTime);
        }
    }
    
    public boolean isFirstState(DiscourseFieldState state)
    {
    	if (discourseFieldEventList.isEmpty())
    	{
    		return true;
    	}
    	else
    	{
    		return ((DiscourseFieldEvent)(discourseFieldEventList.getFirst())).state == state;
    	}
    }
        
    public DocumentArc getDocumentArc( String docName, double tick )
    {
        DocumentArc docArc;
    
        DiscourseFieldEvent event = lookupEvent(tick);
        
        if( event != null )
        {
            docArc = event.state.getDocumentArc(docName);
        }
        else
        {
            docArc = new DocumentArc(0,0);
        }
       
        return docArc;
    }
    
    public MoveEvent getDocumentArcMoveEvent( String docName, double tick )
    {
        DiscourseFieldEvent event = lookupEvent(tick);        
        return event.moveEvent;        
    }
    
    /**
     * Updates the discourse field to reflect the state of the documents at the 
     * given tick time.
     * @param tick The time to display.
     */
    public void moveToTick( double tick )
    {
        DiscourseFieldEvent event = lookupEvent(tick);
        if( event != null ) visibleEvent = event;
    }
    
    // find the event to the given time
    private DiscourseFieldEvent lookupEvent( double tick )
    {
        // go through the list of events and find the one that is active at this time
        for( Iterator i = discourseFieldEventList.iterator(); i.hasNext(); )
        {
            DiscourseFieldEvent event = (DiscourseFieldEvent)i.next();
            
            if( event.moveEvent != null )
            {
	            if( event.moveEvent.getTick() >= tick )
	            {
	                if( event == discourseFieldEventList.getLast() )
	                {
//	                    SimpleLogger.logInfo("Selected current event at tick: "+tick);
	                    return currentEvent;
	                }
	                else
	                {
//	                    SimpleLogger.logInfo("Selected move at tick: "+tick);
	                    return event;
	                }
	            }
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.model.event.ITimeLineListener#moveAddedToHistory(edu.virginia.speclab.ivanhoe.client.model.MoveEvent)
     */
    public void moveAddedToHistory(MoveEvent event)
    {
        if( discourseFieldEventList != null )
        {
            DiscourseFieldEvent lastEvent = (DiscourseFieldEvent) discourseFieldEventList.getLast();            
            addMoveEvent(lastEvent.state, event);
        }

        updateCurrentEvent(event);    
		
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.model.event.ITimeLineListener#addNewDocument(edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo)
     */
    public void addNewDocument(DocumentInfo docInfo)
    {
        currentEvent.state.addDocument(docInfo.getTitle(), null);  
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.model.event.ITimeLineListener#removeDocument(edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo)
     */
    public void removeDocument(DocumentInfo docInfo)
    {
        currentEvent.state.removeDocument(docInfo.getTitle());        
    }
    
    public void clearHighlight()
    {
        visibleEvent.state.clearHighlight();
    }
    
    public void paint( Graphics g, double aspectW, double aspectH )
    {
        visibleEvent.state.paint(g,aspectW,aspectH);
    }
    
    public boolean testHighlight( Point p )
    {
        return visibleEvent.state.testHighlight(p);
    }
    
    public void testSelection( Point p, int numClicks )
    {
        visibleEvent.state.testSelection(p,numClicks);
    }

}
