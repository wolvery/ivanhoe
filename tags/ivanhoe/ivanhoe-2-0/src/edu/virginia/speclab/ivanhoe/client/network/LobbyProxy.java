/*
 * Created on Feb 15, 2005
 */
package edu.virginia.speclab.ivanhoe.client.network;

import java.util.Iterator;
import java.util.LinkedList;

import edu.virginia.speclab.ivanhoe.shared.IDisconnectListener;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.IvanhoeException;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.message.LoginMsg;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;

/**
 * @author benc
 */
public class LobbyProxy extends ClientProxy
{
    private GameProxy gameProxy;
    private LinkedList gameProxyListeners;
    
    /**
     * @param userName
     * @param password
     */
    public LobbyProxy(String userName, String password)
    {
        super(userName, password);
        gameProxyListeners = new LinkedList();
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
    
    /**
     * Connect to the game at the specified host/port
     * 
     * @param gameName
     * @param gameHost
     * @param gamePort
     * @throws IvanhoeException
     */
    public void joinGame(String gameHost, int gamePort)
            throws IvanhoeException
    {
        gameProxy = new GameProxy(userName, password,1);
        
        // make connection to the game
        SimpleLogger.logInfo("Connecting to " + gameHost + ":" + gamePort
                + "...");
        gameProxy.connect(gameHost, gamePort);
        gameProxy.setEnabled(true);
        populateGameProxyListeners();
                    
        // send authentication
        SimpleLogger.logInfo("Logging in...");
        LoginMsg msg = new LoginMsg(this.password, System.getProperties());
        gameProxy.sendMessage(msg);
    }
        
    public GameProxy getGameProxy()
    {
        return gameProxy;
    }
    
    private void populateGameProxyListeners()
    {
        if (gameProxy != null)
        {
            for (Iterator i=gameProxyListeners.iterator(); i.hasNext();)
            {
                MessageHandlerInfo mhi = ((MessageHandlerInfo)i.next());
                gameProxy.registerMsgHandler(mhi.messageType, mhi.messageHandler);
            }
            
            for (Iterator i=this.getDisconnectListeners().iterator(); i.hasNext();)
            {
                gameProxy.registerDisconnectHandler((IDisconnectListener)i.next());
            }
        }
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
