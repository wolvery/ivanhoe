/*
 * Created on Oct 9, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.server.exception;

import edu.virginia.speclab.ivanhoe.shared.IvanhoeException;

/**
 * @author lfoster
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MapperException extends IvanhoeException
{
   /** 
    * Creates a new instance of a SequenceException
    *
    * @param msg The output message for the Exception
    */
   public MapperException(String msg)
   {
      super("Mapper Exception: " + msg);
   }

   /** 
    * Creates a new instance of a SequenceException
    *
    * @param msg The output message for the Exception
    * @param cause The <code>Throwable</code> object for the Exception
    */
   public MapperException(String msg, Throwable cause)
   {
      super("Mapper Exception: " + msg, cause);
   }
}
