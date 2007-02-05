/*
 * Created on Feb 22, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author Nick
 * 
 * This messages contains the timestamp to be used as the basis for all client timestamps.
 */
public class TimeSynch extends Message
{
    private long serverTimeMs;
    
    public TimeSynch()
    {
       super(MessageType.TIME_SYNCH);
       this.serverTimeMs = System.currentTimeMillis();
    }
    
    public long getServerTime()
    {
       return this.serverTimeMs;
    }
}
