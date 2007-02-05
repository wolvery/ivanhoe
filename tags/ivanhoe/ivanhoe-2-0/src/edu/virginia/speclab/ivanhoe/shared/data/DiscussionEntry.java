/*
 * Created on Mar 11, 2004
 *
 * DiscussionEntry
 */
package edu.virginia.speclab.ivanhoe.shared.data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lfoster
 *
 * Data for an entry in Discussion
 */
public class DiscussionEntry implements Serializable
{
   protected final int id;
   protected final String title;
   protected final int roleID;
   protected final Date postingDate;
   protected final String message;
   protected final int parentId;
   
   public DiscussionEntry(int id, String title, int roleID, 
      Date date, String msg)
   {
      this(id, title, roleID, date, msg, -1);
   }
   
   public DiscussionEntry(String title, int roleID, Date date, 
      String msg, int parentId)
   {
      this(-1, title, roleID, date, msg, parentId);
   }
   
   public DiscussionEntry(int id, String title, int roleID, 
      Date date, String msg, int parentId)
   {
      this.id = id;
      this.title = title;
      this.roleID = roleID;
      this.postingDate = date;
      this.message = msg;
      this.parentId = parentId;
   }
   
   public DiscussionEntry(DiscussionEntry src)
   {
      this.id = src.id;
      this.title = src.title;
      this.roleID = src.roleID;
      this.postingDate = src.postingDate;
      this.message = src.message;
      this.parentId = src.parentId;   
   }
   
   public boolean isResponse()
   {
      return (this.parentId > -1);
   }

   public int getId()
   {
      return id;
   }

   public String getMessage()
   {
      return message;
   }

   public int getParentId()
   {
      return parentId;
   }

   public int getRoleID()
   {
      return roleID;
   }

   public Date getPostingDate()
   {
      return postingDate;
   }

   public String getTitle()
   {
      return title;
   }

}
