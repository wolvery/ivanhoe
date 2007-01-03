/*
 * Created on Dec 16, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.pregame;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.network.GameProxy;
import edu.virginia.speclab.ivanhoe.client.network.LobbyProxy;
import edu.virginia.speclab.ivanhoe.client.util.ServerSynchronizedClock;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;
import edu.virginia.speclab.ivanhoe.shared.message.GameInfoMsg2;
import edu.virginia.speclab.ivanhoe.shared.message.LoginResponseMsg;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.TimeSynch;

/**
 * @author Nick
 * 
 * Manages the login process 
 */
public class LoginTransaction implements IMessageHandler
{
	private GameInfo gameInfo;
    private String lobbyHost;
    private int lobbyPort;
    private LoginTransactionListener listener;
    private boolean gotLoginResponse,gotServerTime,gotGameInfo;
    
    public LoginTransaction( String lobbyHost, int lobbyPort )
    {
        this.lobbyHost = lobbyHost;
        this.lobbyPort = lobbyPort;        
    }
    
    public void setListener( LoginTransactionListener listener )
    {
        this.listener = listener;
    }
    
    /**
     * Sends login credentials to the server
     */
    public boolean login( String userName, String password )
    {
        SimpleLogger.logInfo("Connecting to "+lobbyHost+":"+lobbyPort);

        GameProxy gameProxy = new GameProxy(userName, password); 
        Ivanhoe.setProxy(gameProxy);

        Ivanhoe.getProxy().registerMsgHandler(MessageType.GAME_INFO, this);
        Ivanhoe.getProxy().registerMsgHandler(MessageType.LOGIN_RESPONSE, this);
        Ivanhoe.getProxy().registerMsgHandler(MessageType.TIME_SYNCH, this);
        if (Ivanhoe.getProxy().connect(this.lobbyHost, this.lobbyPort)) 
        {
            return ((GameProxy)Ivanhoe.getProxy()).authenticate();
        } 
        else 
        {
            fireFailedConnection();
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.shared.IMessageHandler#handleMessage(edu.virginia.speclab.ivanhoe.shared.message.Message)
     */
    public void handleMessage(Message msg)
    {        
        if( msg instanceof LoginResponseMsg )
        {
            LoginResponseMsg resp = (LoginResponseMsg) msg;
            if (!resp.isSuccess()) 
            {
                fireFailedAuthorization();
            }
            else
            {
                gotLoginResponse = true;
            }
        }
        
        if( msg instanceof TimeSynch )
        {
            TimeSynch time = (TimeSynch) msg;
            long serverTime = time.getServerTime();
            ServerSynchronizedClock clock = new ServerSynchronizedClock(serverTime);
            Ivanhoe.setIvanhoeClock(clock);
            gotServerTime = true;
        }

        if( msg instanceof GameInfoMsg2 )
        {
        	GameInfoMsg2 gameInfoMsg = (GameInfoMsg2) msg;
        	gameInfo = gameInfoMsg.getInfo();
            gotGameInfo = true;
        }

        if( gotLoginResponse && gotServerTime && gotGameInfo )
        {
            fireLoginSuccessful();
        }            
    }

    /**
     * Notify the listener that a succesful login has occurred.
     */
    private void fireLoginSuccessful()
    {
        if( listener != null )
        {
            listener.loginSuccessful();
        }
        else
        {
            SimpleLogger.logError("No listener set to recieve successful login message.");
        }
    }
    
    /**
     * Notify the listener, unable to connect
     */
    private void fireFailedConnection()
    {
        if( listener != null )
        {
            listener.failedConnection();
        }
        else
        {
            SimpleLogger.logError("No listener set to recieve successful login message.");
        }
    }
    
    /**
     * Notify the listener, failed authorization
     */
    private void fireFailedAuthorization()
    {
        if( listener != null )
        {
            listener.failedAuthorization();
        }
        else
        {
            SimpleLogger.logError("No listener set to recieve successful login message.");
        }
    }

	public GameInfo getGameInfo() {
		return gameInfo;
	}
}
