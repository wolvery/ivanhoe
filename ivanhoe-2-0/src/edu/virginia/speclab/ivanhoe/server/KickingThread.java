/*
 * Created on Apr 21, 2005
 */
package edu.virginia.speclab.ivanhoe.server;

import edu.virginia.speclab.ivanhoe.server.game.UserProxy;

/**
 * @author benc
 */
public class KickingThread extends Thread
{
    public static final int KICK_GRACE_TIME = 30000;

    private final ProxyMgr proxyMgr; 
    private final UserProxy kickedProxy;
    private int gameToDie;
    private final int timeout;
    
    public KickingThread(ProxyMgr proxyMgr, int gameID, int timeout)
    {
        this.proxyMgr = proxyMgr;
        this.kickedProxy = null;
        this.gameToDie = gameID;
        this.timeout = timeout;
    }
    
    public KickingThread(ProxyMgr proxyMgr, UserProxy kickedProxy, int timeout)
    {
        this.proxyMgr = proxyMgr;
        this.kickedProxy = kickedProxy;
        this.timeout = timeout;
    }
    
    public void run()
    {
        try
        {
            synchronized (this)
            {
                wait(timeout);
            }
        }
        catch (InterruptedException ie) {}
        
        if (kickedProxy != null)
        {
            synchronized (kickedProxy)
            {
                proxyMgr.removeProxy(kickedProxy);
            }
        }
        else
        {
            proxyMgr.removeAllProxies(gameToDie);
        }
    }
}