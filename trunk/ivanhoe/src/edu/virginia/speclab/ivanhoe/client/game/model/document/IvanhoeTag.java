/*
 * Created on Nov 11, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.client.game.model.document;

import java.util.Vector;
import javax.swing.text.html.*;
import javax.swing.text.html.HTML.Tag;

import edu.virginia.speclab.ivanhoe.shared.data.*;

/**
 * @@author lfoster
 *
 * Extensions to HTML that are used within Ivanhoe
 * All are based on HTML.UnknownTag
 */
public class IvanhoeTag extends HTML.UnknownTag
{   
   private static Vector supportedTags = new Vector();
   
   public static IvanhoeTag ADD = new IvanhoeTag("add");
   public static IvanhoeTag DELETE = new IvanhoeTag("delete");
   public static IvanhoeTag ILINK = new IvanhoeTag("ilink");
  
   private IvanhoeTag(String name)
   {
      super(name);
      supportedTags.add(this);
   }
   
   public static boolean isSupported(HTML.Tag tag)
   {
      return supportedTags.contains(tag);
   }
   
   public static HTML.Tag getTagForActionType(ActionType type)
   {
      if (type.equals(ActionType.ADD_ACTION))
         return ADD;
      if (type.equals(ActionType.DELETE_ACTION))
         return DELETE;
      if (type.equals(ActionType.LINK_ACTION))
         return ILINK;
      if (type.equals(ActionType.IMAGE_ACTION))
         return Tag.IMG;
      
      return null;
   }
   
   public static ActionType getActionTypeForTag(HTML.Tag tag)
   {
      if (tag.equals(ADD))
         return ActionType.ADD_ACTION;
      if (tag.equals(DELETE))
         return ActionType.DELETE_ACTION;
      if (tag.equals(ILINK))
         return ActionType.LINK_ACTION;
      if (tag.equals(Tag.IMG))
         return ActionType.IMAGE_ACTION;
      
      return null;
   }
}