/*
 * Created on Jan 4, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.model.metagame;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.data.ReferenceResource;
import edu.virginia.speclab.ivanhoe.shared.message.ReferenceListMsg;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;

/**
 * @author Nick
 *
 */
public class ReferenceResourceManager implements IMessageHandler
{
    private LinkedList listeners;
    private LinkedList resourceList;
    
    public ReferenceResourceManager()
    {
        resourceList = new LinkedList();
        listeners = new LinkedList();
        
        Ivanhoe.registerGameMsgHandler( MessageType.REFERENCE_LIST, this );
    }
    
    public void addListener( IReferenceResourceListener listener )
    {
        listeners.add(listener);
    }
    
    private void fireResourceChanged()
    {
        for( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            IReferenceResourceListener listener = (IReferenceResourceListener) i.next();
            listener.resourceChanged();            
        }
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.shared.IMessageHandler#handleMessage(edu.virginia.speclab.ivanhoe.shared.message.Message)
     */
    public void handleMessage(Message msg)
    {        
        if( msg.getType().equals(MessageType.REFERENCE_LIST) )
        {
            ReferenceListMsg bookmarkMsg = (ReferenceListMsg) msg;            
            List bookmarks = bookmarkMsg.getResources();	        
            updateReferences(bookmarks);
        }
    }

    private void updateReferences( List newReferenceList )
    {               
        synchronized(resourceList)
        {
            resourceList.clear();
                        
            for( Iterator i = newReferenceList.iterator(); i.hasNext(); )
            {
                ReferenceResource bookmark = (ReferenceResource) i.next();
                resourceList.add(bookmark);
            }
        }
        
        fireResourceChanged();
    }
    
    public void updateReferenceList( List newReferenceList )
    {
        updateReferences(newReferenceList);
        sendReferencesToServer();
    }
    
    public LinkedList getReferences()
    {
        return resourceList;
    }
    
    private void sendReferencesToServer()
    {
        ReferenceListMsg bookmarkMsg = new ReferenceListMsg();            

        for( Iterator i = resourceList.iterator(); i.hasNext(); )
        {
            ReferenceResource bookmark = (ReferenceResource) i.next();
            bookmarkMsg.addResource(bookmark);
        }
        
        Ivanhoe.getProxy().sendMessage(bookmarkMsg);        
    }
    
}
