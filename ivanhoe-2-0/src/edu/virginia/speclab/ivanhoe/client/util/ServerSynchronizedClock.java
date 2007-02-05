/*
 * Created on Feb 22, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.util;

import java.util.Date;

/**
 * @author Nick
 *
 * Provides a clock that is synchronized to another clock. Must be constructed immediately after taking
 * sample from other machine's clock for best results.
 */
public class ServerSynchronizedClock
{
    private static long serverTimeMs;
    private static long clientTimeMs;

    public ServerSynchronizedClock( long serverTime )
    {
        serverTimeMs = serverTime;
        clientTimeMs = System.currentTimeMillis();
    }
    
    /**
     * @return Returns the corrected date based on the server start time stamp
     */
    public Date getDate()
    {
       return new Date(serverTimeMs + (System.currentTimeMillis()-clientTimeMs) );
    }
}
