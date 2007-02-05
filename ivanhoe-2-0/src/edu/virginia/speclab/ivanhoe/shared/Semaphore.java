package edu.virginia.speclab.ivanhoe.shared;

/**
 * Allows the process to pause at an arbitrary point of execution and resume
 * when awoken by another thread.
 * @author Nick
 *
 */
public class Semaphore
{
    /**
     * Pause execution at this location.
     */
    public void waitHere()
    {            
        try
        {
            synchronized( this ) 
            {
                wait();
            }
        } 
        catch (InterruptedException e) {}
    }
    
    /**
     * Resume execution from the point at which it was paused.
     */
    public void proceed()
    {
        synchronized( this ) {
            notify();
        }
    }
}
