/*
 * Created on Apr 8, 2004
 * 
 * edu.virginia.speclab.ivanhoe.client.model.document 
 * LinkManager.java
 */
package edu.virginia.speclab.ivanhoe.client.game.model.document;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.ActionType;
import edu.virginia.speclab.ivanhoe.shared.data.Link;
import java.util.*;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

/**
 * @author lfoster
 *
 * Manage a list of links
 */
public class LinkManager
{
   private List links;
   private IvanhoeDocument document;
   
   /**
    * Constrct a linkManager for the specified document
    * @param doc
    */
   public LinkManager(IvanhoeDocument doc)
   {
      this.links = new ArrayList();
      this.document = doc;
   }
   
   private LinkRec getLinkRecord(String linkId)
   {
      LinkRec result = null;
      for (Iterator itr = this.links.iterator(); itr.hasNext();)
      {
         LinkRec test = (LinkRec)itr.next();
         if (test.getLink().getId().equals(linkId))
         {
            result = test;
            break;
         }
      }
         
      return result;
   }
   
   public boolean linkRecForActionID( String actionId )
   {
       LinkRec linkRec = getLinkRecord(actionId);
       
       if( linkRec == null )
       {
           String errorMessage = "getLinkRecord() can't find link rec for action id: "+actionId+" a list of links follows.\n";
           for( Iterator j = this.links.iterator(); j.hasNext(); )
           {
               LinkRec otherlinkRec = (LinkRec) j.next();
               errorMessage += "Link: "+otherlinkRec.getLink().getId()+"\n";
           }
           
           Ivanhoe.sendErrorMessageToHost(errorMessage);
           return false;
       }
       else
       {
           return true;
       }
   }
   
   /**
    * Get the starting offset for which the link specifed by
    * actionId is anchored
    * @param actionId
    * @return
    */
   public int getLinkOffset(String actionId)
   {
      LinkRec linkRec = getLinkRecord(actionId);
      int minOffset = document.getLength();
      for (Iterator itr = linkRec.getAnchorIdList().iterator();itr.hasNext();)
      {
         String anchorId = (String)itr.next();
         Element ele = document.getElement(anchorId);
         if (ele == null)
         {
            SimpleLogger.logError("AnchorID " + anchorId + " not found!");
         }
         else
         {
            if (ele.getStartOffset() < minOffset)
            {
               minOffset = ele.getStartOffset();
            }
         }
      }
      
      return minOffset;
   }
   
   /**
    * Get the text of the anchor for the link identified by actionId
    * @param actionId
    * @return
    */
   public String getLinkAnchorText(String actionId)
   {
      String anchorText = "";
      LinkRec link = getLinkRecord(actionId);
      
      int min = document.getLength();
      int max = 0;
      
      try
      {
          for (Iterator itr = link.getAnchorIdList().iterator();itr.hasNext();)
          {
             String anchorId = (String)itr.next();
             int len = document.getTagContent(anchorId, IvanhoeTag.ILINK, ActionType.LINK_ACTION).length();
             Element ele = document.getElement(anchorId);
             if (ele.getStartOffset() < min)
                min = ele.getStartOffset();
             if (ele.getStartOffset()+len > max)
                max = ele.getStartOffset()+len;
          }

          anchorText = document.getText(min, max-min);
      }
      catch (BadLocationException e)
      {
         SimpleLogger.logError("Unable to get link text", e);
      }
      
      return anchorText;
   }
   
    /**
    * Get the link target associated with actionID
    * @param actionId
    * @return
    */
   public Link getLink(String actionId)
   {
      LinkRec rec = getLinkRecord(actionId);
      if (rec != null)
      {
         return  rec.getLink(); 
      }
      return null;
   }
   
   /**
    * Get a list of all links rooted off of anchorId
    * @param anchorId
    * @return
    */
   public List getLinks(String anchorId)
   {
      List list = new ArrayList();
      for (Iterator itr = this.links.iterator(); itr.hasNext();)
      {
         LinkRec linkRec = (LinkRec)itr.next();
         if (linkRec.isAnchoredFrom(anchorId))
         {
            list.add(linkRec.getLink());
         }
      }
      return list;
   }
   
   /**
    * Check if an anchor supports one or many links
    * @param anchorId
    * @return
    */
   public boolean isOneToMany(String anchorId)
   {
      int cnt = 0;
      for (Iterator itr = this.links.iterator();itr.hasNext();)
      {
         LinkRec rec = (LinkRec)itr.next();
         if (rec.isAnchoredFrom(anchorId))
         {
            cnt++;
            if (cnt > 1)
            {
               break;
            }
         }
      }
      
      return (cnt > 1);
   }
   
   /**
    * @param anchorId
    * @return Returns true if the specifed anchorID is in use
    */
   public boolean isAnchorValid(String anchorId)
   {
      boolean valid = false;
      for (Iterator itr = this.links.iterator();itr.hasNext();)
      {
         LinkRec rec = (LinkRec)itr.next();
         if (rec.isAnchoredFrom(anchorId))
         {
            valid = true;
            break;
         }
      }
      
      return valid;
   }
   
   /**
    * @param linkId
    * @param anchorId
    */
   public void addAnchorId(String linkId, String newAnchorId)
   {
      LinkRec rec = getLinkRecord(linkId);
      if (rec != null)
      {
         rec.addAnchorId(newAnchorId);
      }  
   }
   
   /**
    * Adds a new link to the manager
    * @param link
    */
   public void add(Link link)
   {
      LinkRec newRec = new LinkRec(link);
      this.links.add(newRec);
   }
   
   /**
    * remove a link from the manager and clean up any unused anchor tags
    * @param link
    */
   public void remove(String linkId)
   {
      SimpleLogger.logInfo("Removing [" + linkId + "] from linkManager...");
      
      LinkRec rec = this.getLinkRecord(linkId);
      if (rec != null)
      {
         // remove the link from manager
         this.links.remove(rec);
       
         // remove all unused anchor tags from doc       
         for (Iterator anchorItr = rec.getAnchorIdList().iterator(); anchorItr.hasNext();)
         {
            String anchorId = (String)anchorItr.next();
            if ( isAnchorValid(anchorId) == false )
            {
               // remove anchor tag
               SimpleLogger.logInfo("Removing link anchor id [" + anchorId + "]");
               this.document.removeTag(IvanhoeTag.ILINK, anchorId);
            }
         }
         
         SimpleLogger.logInfo("Link [" + linkId + "] removal COMPLETE"); 
      }
   }

   /**
    * <code>existingAnchorId</code> has been split into two anchors.
    * Add <code>newAnchorId</code> as another anchor for all links anchored
    * at the original id
    * @param existingAnchorId
    * @param newAnchorId
    */
   public void splitAnchor(String existingAnchorId, String newAnchorId)
   {
      for (Iterator itr = this.links.iterator();itr.hasNext();)
      {
         LinkRec rec = (LinkRec)itr.next();
         if (rec.isAnchoredFrom(existingAnchorId))
         {
            rec.addAnchorId(newAnchorId);
         }
      }
   }
   
   /**
    * This class wraps a link with its anchor tags
    */
   private static class LinkRec
   {
      private final List anchorIdList;
      private final Link link;
      
      public LinkRec(Link link)
      {
         this.link = link;
         this.anchorIdList = new ArrayList();
      }
      
      /**
       * Add an tag id to the list of anchors from which this target can be triggered
       * @param id
       */
      public void addAnchorId(String newAnchorId)
      {
         if (this.anchorIdList.contains(newAnchorId) == false)
            this.anchorIdList.add(newAnchorId);
      }
      
      /**
       * Get the list of anchors that this link can be triggerd from
       * @return
       */
      public List getAnchorIdList()
      {
         return new ArrayList(this.anchorIdList);
      }
      
      /**
       * Check if this link is anchored on a tag with the specified id
       * @param anchorId
       * @return
       */
      public boolean isAnchoredFrom(String anchorId)
      {
         boolean isAnchored = false;
         for (Iterator itr = this.anchorIdList.iterator();itr.hasNext();)
         {
            String testId = (String)itr.next();
            if (testId.equals(anchorId))
            {
               isAnchored = true;
               break;
            }
         }
         
         return isAnchored;
      }
      
      public Link getLink()
      {
         return this.link;
      }
   }
}
