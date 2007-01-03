/*
 * Created on Mar 31, 2005
 */
package edu.virginia.speclab.ivanhoe.client.pregame;

import edu.virginia.speclab.ivanhoe.shared.AbstractProxy;
import edu.virginia.speclab.ivanhoe.shared.message.Message;


public class CreatorProxy extends AbstractProxy
   {           
      public void receiveMessage(Message msg)
      {
          routeMessage(msg);
      }

      public String getID()
      {
         return "AccountCreator";
      }     
   }