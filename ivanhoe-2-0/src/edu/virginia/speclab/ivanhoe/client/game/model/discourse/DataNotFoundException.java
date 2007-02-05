/*
 * Created on Sep 13, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse;

/**
 * @author benc
 */
public class DataNotFoundException extends RuntimeException
{
    public DataNotFoundException()
    {
        super();
    }

    public DataNotFoundException(String message)
    {
        super(message);
    }
    public DataNotFoundException(Throwable cause)
    {
        super(cause);
    }

    public DataNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
