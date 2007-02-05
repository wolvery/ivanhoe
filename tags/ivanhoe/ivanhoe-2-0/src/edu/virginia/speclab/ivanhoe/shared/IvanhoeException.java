/*
 * Created on Oct 8, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.virginia.speclab.ivanhoe.shared;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @author dgran
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class IvanhoeException extends Exception
{
	/** The root cause generating this exception */
	private Throwable cause;
  
	/** 
	 * Creates a new instance of a METSException
	 *
	 * @param msg The output message for the Exception
	 */
  
	public IvanhoeException(String msg)
	{
	  super(msg);
	}
  
	/** 
	 * Creates a new instance of a METSException
	 *
	 * @param msg The output message for the Exception
	 * @param cause The <code>Throwable</code> object for the Exception
	 */
  
	public IvanhoeException(String msg, Throwable cause)
	{
	  super(msg);
	  this.cause = cause;
	}
  
	/**
	 * Retrieves the error message for this Exception
	 *
	 * @return A String representing the error message
	 */
  
	public String getMessage()
	{
	  if (cause != null)
	  {
		 return super.getMessage() + ": " + cause.getMessage();
	  } else
	  {
		 return super.getMessage();
	  }
	}
  
	/**
	 * Displays the stack trace for the Exception to standard output
	 */
  
	public void printStackTrace()
	{
	  super.printStackTrace();
    
	  if (cause != null)
	  {
		 System.err.print("Root cause: ");
		 cause.printStackTrace();
	  }
	}
  
	/**
	 * Displays the stack trace for the Exception to a <code>PrintStream</code>
	 *
	 * @param stream The <code>PrintStream</code> for output
	 */
  
	public void printStackTrace(PrintStream stream)
	{
	  super.printStackTrace(stream);
    
	  if (cause != null)
	  {
		 stream.print("Root cause: ");
		 cause.printStackTrace(stream);
	  }
	}
  
	/**
	 * Displays the stack trace for the Exception to a <code>PrintWriter</code>
	 *
	 * @param writer The <code>PrintWriter</code> for output
	 */
  
	public void printStackTrace(PrintWriter writer)
	{
	  super.printStackTrace(writer);
    
	  if (cause != null)
	  {
		 writer.print("Root cause: ");
		 cause.printStackTrace(writer);
	  }
	}
  
	/**
	 * Retrieves the <code>Throwable</code> object for this Exception
	 *
	 * @return The <code>Throwable</code> object for this Exception
	 */
  
	public Throwable getCause()
	{
	  return cause;
	}
}
