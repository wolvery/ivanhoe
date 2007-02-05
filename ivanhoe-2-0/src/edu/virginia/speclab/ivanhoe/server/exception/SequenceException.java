/*
 * Created on Oct 8, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.virginia.speclab.ivanhoe.server.exception;

import edu.virginia.speclab.ivanhoe.shared.IvanhoeException;

/**
 * A <code>SequenceException</code> occurs when the application fails to
 * obtain a new primary key for a database table.
 *
 * @author Duane Gran
 */
public class SequenceException extends IvanhoeException
{
   /** 
   	* Creates a new instance of a SequenceException
   	*
   	* @param msg The output message for the Exception
   	*/

   public SequenceException(String msg)
   {
      super("Sequence Exception: " + msg);
   }

   /** 
   	* Creates a new instance of a SequenceException
   	*
   	* @param msg The output message for the Exception
   	* @param cause The <code>Throwable</code> object for the Exception
   	*/

   public SequenceException(String msg, Throwable cause)
   {
      super("Sequence Exception: " + msg, cause);
   }
}
