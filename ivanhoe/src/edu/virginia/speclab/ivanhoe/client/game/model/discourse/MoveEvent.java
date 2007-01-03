/*
 * Created on Jun 18, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import edu.virginia.speclab.ivanhoe.shared.data.ActionType;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;
import edu.virginia.speclab.ivanhoe.shared.data.Move;

/**
 * @author Nick
 */
public class MoveEvent implements Comparable
{
    private final DocumentVersionManager dvManager;
    
    private int tick;
    private Move move;
    
    public MoveEvent( DocumentVersionManager dvManager, Move m )
    {
        this.dvManager = dvManager;
        move = m;            
    }
    
    public LinkedList getDocumentList()
    {
        LinkedList documentList = new LinkedList();
        
        // add documents that were added during this move 
		for( Iterator i = move.getActions(); i.hasNext(); )
		{
			IvanhoeAction act = (IvanhoeAction) i.next();
			if( act.getType().equals(ActionType.ADD_DOC_ACTION) == true )
			{
                // add the document area
			    documentList.add(
                        dvManager.getDocumentVersion(act).getDocumentTitle());
			}
		}
		
		return documentList;
    }
    
    public Date getDate()
    {
        if( move != null )
        {
            return move.getSubmissionDate();
        }
        else return null;
    }
    
    public long getTime()
    {
        if( move != null )
        {
            return move.getSubmissionDate().getTime();    
        }
        else return 0;
    }
    /**
     * @return Returns the tick.
     */
    public int getTick()
    {
        return tick;
    }
    /**
     * @param tick The tick to set.
     */
    void setTick(int tick)
    {
        this.tick = tick;
    }
    /**
     * @return Returns the move.
     */
    public Move getMove()
    {
        return move;
    }

    public String toString()
    {
        return "[" + tick + " : " + move + "]"; 
    }
    
    public int compareTo(Object thatObj)
    {
        MoveEvent that = (MoveEvent)thatObj;
        return this.getDate().compareTo(that.getDate());
    }
}