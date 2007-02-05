/*
 * Created on Jun 17, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse;

import java.util.*;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.blist.BCollection;
import edu.virginia.speclab.ivanhoe.shared.blist.BLinkedList;
import edu.virginia.speclab.ivanhoe.shared.data.ActionType;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;
import edu.virginia.speclab.ivanhoe.shared.data.Move;

/**
 * @author Nick
 */
public class DiscourseFieldTimeline implements IDiscourseFieldListener
{
    private final DiscourseField discourseField;
    private final BLinkedList timeline;
    private final BLinkedList documentTimeLine;
    private final BLinkedList timeLineListeners;
    private final DocumentVersionTickCache dvTickCache;

    public DiscourseFieldTimeline( DiscourseField discourseField )
    {
        timeline = new BLinkedList();
        documentTimeLine = new BLinkedList();
        timeLineListeners = new BLinkedList();
        dvTickCache = new DocumentVersionTickCache();
        this.discourseField = discourseField;
        init();
        discourseField.addListener(this);
    }

    public void addListener(ITimeLineListener listener)
    {
        timeLineListeners.add(listener);
    }

    private void fireMoveAddedToHistory(MoveEvent event)
    {
        for (Iterator i = timeLineListeners.iterator(); i.hasNext();) {
            ITimeLineListener listener = (ITimeLineListener) i.next();
            listener.moveAddedToHistory(event);
        }
    }

    private void fireAddNewDocument(DocumentInfo docInfo)
    {
        for (Iterator i = timeLineListeners.iterator(); i.hasNext();) {
            ITimeLineListener listener = (ITimeLineListener) i.next();
            listener.addNewDocument(docInfo);
        }
    }

    private void fireRemoveDocument(DocumentInfo docInfo)
    {
        for (Iterator i = timeLineListeners.iterator(); i.hasNext();) {
            ITimeLineListener listener = (ITimeLineListener) i.next();
            listener.removeDocument(docInfo);
        }
    }

    /**
     * Obtain the total length of the document, taking into account insertions
     * of text by the specified player up to the specified time.
     *
     * @param documentVersion the particular version of the document
     * @return the total length of the provided documentVersion
     */
    public int getDocumentLength( DocumentVersion documentVersion )
    {        
        final DocumentInfo docInfo =
            discourseField.getDocumentInfo(documentVersion.getDocumentTitle());
        int totalLength = docInfo.getDocumentLength();
        final DocumentVersionManager dvManager = discourseField.getDocumentVersionManager();
        final BCollection actions = dvManager.getActions(documentVersion);
        
        for (Iterator i = actions.iterator(); i.hasNext();) 
        {
            final Object actObj = i.next();
            final IvanhoeAction act = (IvanhoeAction) actObj;
            if ( ActionType.ADD_ACTION.equals(act.getType()) ) 
            {
                // add them to the total length
                totalLength += act.getLength();
            }
            else if ( ActionType.IMAGE_ACTION.equals(act.getType()) )
            {
                totalLength += 1;
            }
        }

        return totalLength;
    }

    /**
     * Find the closest move event to a given tick value.
     * @param tick a value in move number
     * @return the event which corresponds to this tick
     */
    public MoveEvent lookupEvent( int tick )
    {
        MoveEvent prevEvent = (MoveEvent) timeline.getFirst();
        
        // go through the list of events and find the one that is active at this time
        for( Iterator i = timeline.iterator(); i.hasNext(); )
        {
            MoveEvent event = (MoveEvent)i.next();
            
            if( event != null )
            {
	            if( event.getTick() >= tick )
	            {
	                break;
	            }
            }
            
            prevEvent = event;
        }
        
        return prevEvent;
    }
    
    public Date getDocumentPublishedTime(String docName)
    {
    	for (Iterator i=timeline.iterator(); i.hasNext(); )
    	{
    		MoveEvent me = (MoveEvent)(i.next());
    		if (me.getDocumentList().contains(docName))
    		{
    			return me.getDate();
    			
    		}
    	}
    	
    	return null;
    }
    
    // find the event for a given move
    public MoveEvent lookupEvent( Move move )
    {
        // go through the list of events and find the one that is active at this time
        for( Iterator i = timeline.iterator(); i.hasNext(); )
        {
            MoveEvent event = (MoveEvent)i.next();
            
            if( event != null )
            {
	            if( event.getMove() == move )
	            {
	                return event;
	            }
            }
        }
        
        return null;
    }
    
    public MoveEvent lookupPreviousEvent( int tick, String playerName )
    {
        MoveEvent previousEvent = null;

        // go through the list of events and find the one that is active at this time
    	LinkedList moveHistory = getMoveEventHistory(playerName);
        for( Iterator i = moveHistory.iterator(); i.hasNext(); )
        {
            MoveEvent event = (MoveEvent)i.next();

            if (event.getTick() >= tick) {
                break;
            }

            previousEvent = event;
        }
        
        return previousEvent;
    }
    
    
    public MoveEvent getPreviousMoveEvent( MoveEvent currentEvent )
    {
        MoveEvent previousEvent = (MoveEvent) timeline.getFirst();

        for (Iterator i = timeline.iterator(); i.hasNext();) {
            MoveEvent event = (MoveEvent) i.next();

            if (event == currentEvent) {
                break;
            }

            previousEvent = event;
        }

        return previousEvent;
    }

    public MoveEvent getLastMoveEvent()
    {
        if (timeline.size() > 0) {
            return (MoveEvent) timeline.getLast();
        } else {
            return null;
        }

    }

    public MoveEvent getNextMoveEvent( MoveEvent currentEvent )
    {
        MoveEvent event;

        for (Iterator i = timeline.iterator(); i.hasNext();) {
            event = (MoveEvent) i.next();

            if ( event == currentEvent && i.hasNext() ) {
                return (MoveEvent) i.next();
            }
        }

        return null;
    }
    
    public LinkedList getMoveEventHistory(String playername)
    {
        LinkedList moveHistory = new LinkedList();
        for (Iterator i = timeline.iterator(); i.hasNext();) {
            MoveEvent event = (MoveEvent) i.next();
            if (event instanceof StartingMoveEvent == true
                    || event.getMove().getRoleName().equals(playername) == true) {
                moveHistory.add(event);
            }
        }

        return moveHistory;
    }

    public LinkedList getUnpublishedDocuments()
    {
        LinkedList unpublishedDocuments = new LinkedList();

        for (Iterator i = documentTimeLine.iterator(); i.hasNext();) {
            DocumentInfo docInfo = (DocumentInfo) i.next();
            if (docInfo.isPublishedDocument() == false) {
                unpublishedDocuments.add(docInfo);
            }
        }

        return unpublishedDocuments;
    }

    public LinkedList getTimeline()
    {
        return timeline;
    }

    private void init()
    {
        List moveHistory = discourseField.getMoveHistory();
        Collections.sort(moveHistory);
        
        for (Iterator i=moveHistory.iterator(); i.hasNext(); ) {
            Move move = (Move) i.next();
            addMoveToTimeline(move);
        }

        // Add all documents to timeline
        List docList = discourseField.getDocumentInfoList();
        for (Iterator i = docList.iterator(); i.hasNext();) {
            DocumentInfo docInfo = (DocumentInfo) i.next();
            addDocumentToTimeline(docInfo);
        }

        // Add time zero to the timeline
        addStartingMoveToTimeline();

        // set the time values for the events in the time line
        calculateTicks();
    }

    private void calculateTicks()
    {
        // Calculate the length of the time line
        int numberOfMoves = timeline.size();

        if (numberOfMoves <= 0)
            return;

        int i = 0;
        for (Iterator j = timeline.iterator(); j.hasNext();) {
            MoveEvent event = (MoveEvent) j.next();

            event.setTick(++i); 
        }

    }

    // figure out when to start the timeline.
    private Date calculateStartTime()
    {
        Date startDate, firstMove=null, firstDocument=null;
        
        if (timeline.size() > 0) 
        {
            MoveEvent event = (MoveEvent) timeline.getFirst();
            firstMove = event.getMove().getStartDate();
        } 
        
        if( documentTimeLine.size() > 0 )
        {
            DocumentInfo docInfo = (DocumentInfo) documentTimeLine.getFirst();
            firstDocument = docInfo.getCreateTime();            
        }
        
        // if there are moves and docs, pick the earlier of the two
        if( firstMove != null && firstDocument != null )
        {
            if( firstMove.before(firstDocument) )
            {
                startDate = firstMove;
            }
            else
            {
                startDate = firstDocument;
            }
        }
        // if there are docs but not moves, use earliest doc
        else if( firstDocument != null )
        {
            startDate = firstDocument;
        }
        // if there are moves but no docs, use earliest move
        else if( firstMove != null )
        {
            startDate = firstMove;
        }               
        // if there is nothing, use current time
        else
        {
            startDate = Ivanhoe.getDate();
        }

        return startDate;
    }

    // must be called after the timeline has been populated with data from the
    // DiscourseField
    private void addStartingMoveToTimeline()
    {
        Date startDate = calculateStartTime();

        // add the starting documents to the starting move event
        StartingMoveEvent startingEvent = new StartingMoveEvent(startDate);
        for (Iterator i = documentTimeLine.iterator(); i.hasNext();) {
            DocumentInfo docInfo = (DocumentInfo) i.next();
            if (docInfo.isStartingDocument() == true) {
                startingEvent.addStartingDocument(docInfo.getTitle());
            }
        }

        timeline.addFirst(startingEvent);
    }

    // add a move to the ordered list of moves in the timeline
    private MoveEvent addMoveToTimeline(Move move)
    {
        // FIXME: this method now breaks if you call it with moves that are out
        // of order.  This is because the move coming back after move
        // submission has a submit date that has all the hours and seconds
        // truncated off (and it's formatted differently).  Ultimately, this
        // method should at the very least complain if things are out of order,
        // but it seems that for the time being, the submit dates aren't
        // trustworthy.
        
        MoveEvent newEvent = new MoveEvent(discourseField.getDocumentVersionManager(),
                move);
        timeline.addLast(newEvent);
        
        return newEvent;
    }

    // add a document to the ordered list of documents in the timeline
    private void addDocumentToTimeline(DocumentInfo docInfo)
    {

        if (documentTimeLine.size() == 0) {
            documentTimeLine.add(docInfo);
            return;
        }

        for (int i = 0; i < documentTimeLine.size(); i++) {
            DocumentInfo curDocInfo = (DocumentInfo) documentTimeLine.get(i);

            if (curDocInfo.getCreateTime().after(docInfo.getCreateTime()) == true) {
                documentTimeLine.add(i, docInfo);
                return;
            }
        }

        // still here? must be last one
        documentTimeLine.addLast(docInfo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.virginia.speclab.ivanhoe.client.model.event.IDiscourseFieldListener#moveAddedToHistory(edu.virginia.speclab.ivanhoe.shared.data.Move)
     */
    public void moveAddedToHistory(Move move)
    {
        MoveEvent event = addMoveToTimeline(move);
        calculateTicks();
        fireMoveAddedToHistory(event);
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.virginia.speclab.ivanhoe.client.model.event.IDiscourseFieldListener#addNewDocument(edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo)
     */
    public void addNewDocument(DocumentInfo docInfo)
    {
        fireAddNewDocument(docInfo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.virginia.speclab.ivanhoe.client.model.event.IDiscourseFieldListener#removeDocument(edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo)
     */
    public void removeDocument(DocumentInfo docInfo)
    {
        fireRemoveDocument(docInfo);
    }
   
    public Date getBeginTime()
    {
    	return ((MoveEvent)(timeline.getFirst())).getDate();
    }
    
    public IvanhoeAction getAction(String actionId)
    {
        IvanhoeAction action = null;
        for ( Iterator i=timeline.iterator(); i.hasNext(); )
        {
            final Move move = ((MoveEvent)i.next()).getMove();
            if (move != null)
            {
                action = move.getAction(actionId);
                if (action != null) break;
            }
        }
        if(action == null ) SimpleLogger.logError("Unable to lookup action tag id: "+actionId);
        return action;
    }

    public int getMoveNumberForPlayer(Move move)
    {
        int moveNumber = 1;
        if (move == null)
        {
            throw new IllegalArgumentException("Cannot get move number for null move");
        }
        
        for (Iterator i=timeline.iterator(); i.hasNext(); )
        {
            Move pastMove = ((MoveEvent)i.next()).getMove();
            if (pastMove == null)
            {
                continue;
            }
            
            // break if this move is after the argument move
            if (pastMove.getSubmissionDate().compareTo(move.getSubmissionDate()) >= 0)
            {
                break;
            }
            
            if (pastMove.getRoleID() == move.getRoleID())
            {
                ++moveNumber;
            }
        }
        
        return moveNumber;
    }
    public BCollection getDocumentTimeLine()
    {
        return documentTimeLine.createCopy();
    }
    
    public MoveEvent getMoveEventAdded(int docVersionID)
    {
        final Integer docVersionIDObj = new Integer(docVersionID);
        for (Iterator i=timeline.iterator(); i.hasNext(); )
        {
            final MoveEvent me = (MoveEvent) i.next();
            final Move move = me.getMove();
            if (move != null)
            {
                if (move.getDocumentVersionIDs().contains(docVersionIDObj)) {
                    return me;
                }
            }
            else if ( discourseField.isStartingDocumentVersion(docVersionID) )
            {
                return me; 
            }
        }
        
        return null;
    }
    
    public final int getTick(DocumentVersion docVersion)
    {
        return dvTickCache.getTick(docVersion.getID());
    }
    
    private class DocumentVersionTickCache
    {
        private HashMap cache;
        
        public DocumentVersionTickCache()
        {
            cache = new HashMap();
        }
        
        public int getTick(int docVersionID)
        {
            Integer dvID = new Integer(docVersionID);
            if (cache.containsKey(dvID))
            {
                final Integer cachedValue = (Integer) cache.get(dvID); 
                return cachedValue.intValue();
            }
            else
            {
                MoveEvent me = getMoveEventAdded(docVersionID);
                if (me != null)
                {
                    final int tick = me.getTick();
                    if (! (me.getMove() instanceof CurrentMove) )
                    {
                        cache.put(dvID, new Integer(tick));
                    }
                    return tick;
                }
                else
                {
                    // This must be in the current move, so we put this after
                    // the last move's tick mark
                    me = (MoveEvent) timeline.getLast();
                    return me.getTick() + 1;
                }
            }
        }
        
    }
}