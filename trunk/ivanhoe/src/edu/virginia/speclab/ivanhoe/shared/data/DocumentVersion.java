/*
 * Created on May 7, 2004
 *
 * DocumentVersion
 */
package edu.virginia.speclab.ivanhoe.shared.data;

import java.io.Serializable;
import java.util.Date;

import edu.virginia.speclab.ivanhoe.shared.blist.BCollection;
import edu.virginia.speclab.ivanhoe.shared.blist.BHashSet;
import edu.virginia.speclab.ivanhoe.shared.blist.BSet;

/**
 * @author lfoster
 *
 * This class is a collection of the data required to specify a version
 * of an IvanhoeDocument
 */
public class DocumentVersion implements Serializable
{
   public static final int INVALID_ID = -1;
   public static final int NO_PARENT_ID = 0;
    
   private DocumentVersion parentVersion;
   private final int roleID; 
   private final String roleName;
   private final String documentTitle;
   private final Date   date;
   private final BSet actionIDs;
   private final int id, parentID;
   private boolean published;
   
   /**
    * Create a version of a doc for the given player at the given moment in time
    * 
    * @param id
    * @param docTitle
    * @param player
    * @param roleID
    * @param date
    * @param parentID
    */
   public DocumentVersion( int id, String docTitle, String roleName, int roleID, Date date, int parentID, boolean published )
   {
      if( docTitle == null || roleName == null || date == null )
      {
          throw new IllegalArgumentException();
      }
      
      this.id = id;
      this.parentID = parentID;
      this.roleID = roleID;
      this.date = date;
      this.documentTitle = docTitle;
      this.roleName = roleName;
      this.actionIDs = new BHashSet();
      this.published = published;
   }

   /**
    * @return Returns the date.
    */
   public Date getDate()
   {
      return date;
   }
   
   /**
    * @return Returns the documentTitle.
    */
   public String getDocumentTitle()
   {
      return documentTitle;
   }
   
   /**
    * @return Returns the roleName.
    */
   public String getRoleName()
   {
      return roleName;
   }
   
   /**
    * Test equality of two versions
    * @param that
    * @return
    */
   public boolean equals(DocumentVersion that)
   {
      if (this.id == that.id)
      {
         return true;
      }
      
      return false;
   }
   
   /**
    * Check if this version occurs before another
    * @param that
    * @return
    */
   public boolean before(DocumentVersion that)
   {
      if (this.date.before(that.date) &&
         this.documentTitle.equals(that.documentTitle))
     {
        return true;
     }
     
     return false;
   }
   
   /**
    * Check if this version occurs after another
    * @param that
    * @return
    */
   public boolean after(DocumentVersion that)
   {
      if (this.date.after(that.date) &&
         this.documentTitle.equals(that.documentTitle))
     {
        return true;
     }
     
     return false;
   }
   
   public boolean isOwner(int id )
   {
      return ( this.roleID == id );
   }
   
   public String toString()
   {
      return "[" + id +" "+ getDocumentTitle()+", " + 
         getRoleName() + ", " + getDate() + " # of actions: "+actionIDs.size()+"]"; 
   }
    /**
     * @return Returns the roleID.
     */
    public int getRoleID()
    {
        return roleID;
    }
    
    public void addActionID(String actionID)
    {
        actionIDs.add(actionID);
    }
    
    public DocumentVersion getParentVersion()
    {
        return parentVersion;
    }

    public void setParentVersion(DocumentVersion parentVersion)
    {
        this.parentVersion = parentVersion;
    }
    
    public int getID()
    {
        return id;
    }

    public int getParentID()
    {
        return parentID;
    }

    public BCollection getActionIDs()
    {
        return actionIDs.createCopy();
    }

    public boolean isPublished()
    {
        return published;
    }
    
    public boolean removeActionID(String actionID)
    {
        return this.actionIDs.remove(actionID);
    }
}
