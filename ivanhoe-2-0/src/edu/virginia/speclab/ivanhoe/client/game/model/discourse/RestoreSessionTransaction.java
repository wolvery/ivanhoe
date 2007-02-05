/*
 * Created on Dec 20, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.data.Move;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.RestoreMsg;
import edu.virginia.speclab.ivanhoe.shared.message.SelectRoleMsg;

/**
 * @author Nick
 *
 * Manages the process of restoring the state of a previous play session. 
 */
public class RestoreSessionTransaction implements IMessageHandler
{
    private Role role;
    private RestoreSessionTransactionListener listener;
    private CurrentMove currentMove;
    
    public RestoreSessionTransaction( Role role, CurrentMove currentMove, RestoreSessionTransactionListener listener )
    {
        this.role = role;
        this.listener = listener;
        this.currentMove = currentMove;

        // listen for restore message
        Ivanhoe.registerGameMsgHandler( MessageType.RESTORE, this );
        
        // send selected role to server
        SelectRoleMsg selectRole = new SelectRoleMsg(role.getId());
        Ivanhoe.getProxy().sendMessage(selectRole);
    }
    
    private void fireSessionRestored()
    {
        if( listener != null )
        {
            listener.sessionRestored();
        }
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.shared.IMessageHandler#handleMessage(edu.virginia.speclab.ivanhoe.shared.message.Message)
     */
    public void handleMessage(Message msg)
    {
        if( msg.getType().equals(MessageType.RESTORE) )
        {
            Ivanhoe.getProxy().unregisterMsgHandler( MessageType.RESTORE, this );
            RestoreMsg restoreMsg = (RestoreMsg) msg;
            
            // restore the last session's activity
            Move savedState = restoreMsg.getMove();
            
            // if there is a move to restore, do it
            if( savedState != null )
            {
                currentMove.restoreMove(savedState);
            }
            
            // set the current role to use for authoring moves.
            currentMove.setCurrentRole(role);
            
            // notify listener that session is restored
            fireSessionRestored();
        }
    }
}
