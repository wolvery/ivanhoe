/*
 * Created on May 4, 2004
 *
 * ActionType
 */
package edu.virginia.speclab.ivanhoe.shared.data;

import java.io.Serializable;

/**
 * @author lfoster
 *
 * Enumeration of the actions avaiable to a player of Ivanhoe
 */
public class ActionType implements Serializable
{
   private final String name;
  
   private ActionType(String name)
   {
      this.name = name;
   }
   
   public String toString()
   {
      return name;
   }
   
   public boolean equals(ActionType that)
   {
      return ( name.equals(that.toString()) );
   }

   public static final ActionType ADD_ACTION = new ActionType("Add");
   public static final ActionType ADD_DOC_ACTION = new ActionType("AddDocument");
   public static final ActionType DELETE_ACTION = new ActionType("Delete");
   public static final ActionType LINK_ACTION = new ActionType("Link");
   public static final ActionType IMAGE_ACTION = new ActionType("Image");
}
