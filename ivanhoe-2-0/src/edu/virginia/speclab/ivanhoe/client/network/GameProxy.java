/*
 * Created on Feb 15, 2005
 */
package edu.virginia.speclab.ivanhoe.client.network;

import java.util.Iterator;
import java.util.LinkedList;

import edu.virginia.speclab.ivanhoe.shared.IDisconnectListener;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.message.DocumentRequestMsg;
import edu.virginia.speclab.ivanhoe.shared.message.LoginMsg;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;

/**
 * @author benc
 */
public class GameProxy extends ClientProxy
{
    private LinkedList gameProxyListeners;
    private int gameID;

    public GameProxy(String userName, String password, int gameID)
    {
        super(userName, password);
        this.gameID = gameID;
        gameProxyListeners = new LinkedList();
    }

    public void requestDocument(Integer docId)
    {
       DocumentRequestMsg msg = new DocumentRequestMsg(docId,true);
       sendMessage(msg);
    }
    
    public void registerGameMsgHandler(MessageType msgType, IMessageHandler msgHandler)
    {
        if (msgType == null || msgHandler == null)
        {
            throw new IllegalArgumentException(getID() + " received null input for registerGameMessageHandler()");
        }
        
        MessageHandlerInfo mhi = new MessageHandlerInfo(msgType, msgHandler);
        if (!gameProxyListeners.contains(mhi))
        {
            SimpleLogger.logInfo("Preregistering message handler " + mhi.toString());
            gameProxyListeners.add(mhi);
        }
    }
    
    public void populateGameProxyListeners()
    {
        for (Iterator i=gameProxyListeners.iterator(); i.hasNext();)
        {
            MessageHandlerInfo mhi = ((MessageHandlerInfo)i.next());
            registerMsgHandler(mhi.messageType, mhi.messageHandler);
        }
        
        for (Iterator i=this.getDisconnectListeners().iterator(); i.hasNext();)
        {
            registerDisconnectHandler((IDisconnectListener)i.next());
        }
    }
        
    public void unregisterGameMsgHandler(IMessageHandler msgHandler)
    {
        for (Iterator i=gameProxyListeners.iterator(); i.hasNext();)
        {
            MessageHandlerInfo mhi = ((MessageHandlerInfo)i.next());
            if (mhi.messageHandler.equals(msgHandler))
            {
                i.remove();
            }
        }
    }
    
    public boolean sendMessage(Message msg) {
    	msg.setGameID(this.gameID);
    	return super.sendMessage(msg);
    }
    
    /**
     * Send user authentication information to the server
     */
    public boolean authenticate()
    {
       // enable the proxy
       setEnabled(true);
       
       // send message
       LoginMsg msg = new LoginMsg(password, System.getProperties());
       msg.setSender(userName);
       return sendMessage(msg);
    }

    public void requestDocumentUpdate(Integer docId)
    {
       DocumentRequestMsg msg = new DocumentRequestMsg(docId,false);
       sendMessage(msg);
    }
    
    private class MessageHandlerInfo
    {
        public final MessageType messageType;
        public final IMessageHandler messageHandler;
        
        public MessageHandlerInfo(MessageType msgType, IMessageHandler msgHandler)
        {
            messageType = msgType;
            messageHandler = msgHandler;
        }
        
        public boolean equals(Object that)
        {
            boolean equal = false;
            if (that instanceof MessageHandlerInfo)
            {
                MessageHandlerInfo thatMHI = (MessageHandlerInfo)that;
                equal =
                    messageHandler.equals(thatMHI.messageHandler)
                    && messageType.equals(thatMHI.messageType);
            }
            return equal;
        }
        
        public String toString()
        {
            return "[" + messageType + ": " + messageHandler + "]";
        }
    }    
}
