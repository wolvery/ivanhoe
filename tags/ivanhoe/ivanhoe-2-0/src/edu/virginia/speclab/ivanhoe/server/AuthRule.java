/*
 * Created on Jun 24, 2004
 *
 * AuthRule
 */
package edu.virginia.speclab.ivanhoe.server;

import java.sql.SQLException;

/**
 * Abstract class for authorization rules
 */
public abstract class AuthRule
{
   private String result;
   private String name;
   
   public AuthRule(String name)
   {
      this.name = name;
      this.result = "Success";
   }
   
   public String getName()
   {
      return this.name;
   }
   
   public String getMessage()
   {
      return this.result;
   }
   
   public void setMessage(String msg)
   {
      this.result = msg;
   }
   
   public abstract boolean executeRule(String userName, String passwd, boolean retry ) throws SQLException;
}
