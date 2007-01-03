/*
 * Created on Oct 2, 2003
 *
 * Player
 */
package edu.virginia.speclab.ivanhoe.client.network;

import javax.swing.SwingUtilities;

import edu.virginia.speclab.ivanhoe.shared.*;
import edu.virginia.speclab.ivanhoe.shared.message.*;

/**
 * @author lfoster
 * @author benc
 */
public class ClientProxy extends AbstractProxy
{
    protected final String userName;
    protected final String password;

    // a wrapper class used with SwingUtilities.invokeLater() to perform message
    // processing on the AWT-Event Thread
    private class MessageProcessor implements Runnable
    {
        private Message msg;

        public MessageProcessor(Message msg)
        {
            this.msg = msg;
        }

        public void run()
        {
            processMessage(msg);
        }
    }

    // keep from being instantiated in any other way
    protected ClientProxy(String userName, String password)
    {
        this.userName = userName;
        this.password = password;
    }

    /**
     * @return Returns the user name of the player, note this is not the same as
     *         the the role name.
     */
    public String getUserName()
    {
        return this.userName;
    }

    /**
     * Closes connection to server.
     * 
     */
    public void close()
    {
        SimpleLogger.logInfo("Closing connection to server.");

        removeAllHandlers();

        // disconnect from game instance
        if (isConnected())
        {
            logout();
            disconnect();
        }

        SimpleLogger.logInfo("Connection to server successfully closed.");
    }

    /**
     * @return Returns the ID of this proxy
     */
    public String getID()
    {
        String className = this.getClass().getName();
        int lastSep = className.lastIndexOf('.');
        return this.userName + className.substring(lastSep + 1);
    }

    /**
     * Send logout notification to the server
     */
    public void logout()
    {
        LogoutMsg msg = new LogoutMsg();
        sendMessage(msg);
    }

    public boolean sendMessage(Message msg)
    {
        msg.setSender(userName);
        msg.setPassword(password);
        return super.sendMessage(msg);
    }

    /**
     * Takes the incoming message and prepares it for processing on the
     * AWT-Event thread.
     */
    public void receiveMessage(Message msg)
    {
        MessageProcessor processor = new MessageProcessor(msg);
        SwingUtilities.invokeLater(processor);
    }

    public void processMessage(Message msg)
    {
        SimpleLogger.logInfo(getID() + " got [" + msg.toString() + "]");
        routeMessage(msg);
    }

    public void goOnline()
    {
        UserOnlineMsg msg = new UserOnlineMsg();
        sendMessage(msg);
    }

    /**
     * @return Returns the password.
     */
    public String getPassword()
    {
        return password;
    }
    
    public boolean registerMsgHandler(MessageType msgType, IMessageHandler handler)
    {
        SimpleLogger.logInfo("Registering "+msgType+" messages with "+handler+" in "+this.getID());
        return super.registerMsgHandler(msgType, handler);
    }
}
