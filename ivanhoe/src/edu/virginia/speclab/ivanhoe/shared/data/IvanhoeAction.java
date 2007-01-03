/*
 * Created on Nov 17, 2003
 *
 * AbstractAction
 */
package edu.virginia.speclab.ivanhoe.shared.data;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;

/**
 * @author lfoster
 *
 * Abstract base class for all Ivanhoe Actions. It contains
 * data common to all actions
 */
public class IvanhoeAction implements Serializable
{
   protected final String id;
   protected final int documentVersionID;
   protected final Date date;
   protected final int roleID;
   protected final String roleName;
   protected final ActionType type;
   
   protected final int offset;
   protected final Object content;
   
   protected final boolean dataIsValid;
   
   private static final DateFormat dateFormatter = 
      DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.FULL);
   
   /**
    * Constructor for action with content & location yet to be determined
    * @param type
    * @param version
    * @param player
    * @param actionId
    */
   protected IvanhoeAction(ActionType type, int documentVersionID, String roleName, int roleID,
           String actionId, Date date)
   {
      this.type = type;
      this.id = actionId;
      this.roleName = roleName;
      this.roleID = roleID;
      this.documentVersionID = documentVersionID;
      this.date = date;
      
      this.offset = 0;
      this.content = null;
      this.dataIsValid = false;
   }
   
   public IvanhoeAction(ActionType type, int documentVersionID, String roleName, int roleID, 
      String actionId, int offset, Object content, Date date)
   {
      this.type = type;
      this.id = actionId;
      this.roleName = roleName;
      this.roleID = roleID;
      this.documentVersionID = documentVersionID;
      this.date = date;
      this.offset = offset;
      this.content = content;
      
      verifyData();
      this.dataIsValid = true;
   }
   
   public IvanhoeAction(IvanhoeAction srcAct)
   {
      this.id = srcAct.id;
      this.type = srcAct.type;
      this.documentVersionID = srcAct.documentVersionID;
      this.date = srcAct.date;
      this.offset = srcAct.getOffset();
      this.roleName = srcAct.roleName;
      this.roleID = srcAct.roleID;
      this.content = srcAct.getContent();
      
      verifyData();
      this.dataIsValid = true;
   }
   
   /**
    * For use in the translation step in the server on move submission
    * 
    * @param srcAct
    *       Action containing data to be copied and translated
    * @param dvIDMapping
    *       Mapping from input document version ID to new document version ID
    */
   public IvanhoeAction(IvanhoeAction srcAct, Map dvIDMapping)
   {
      this.id = srcAct.id;
      this.type = srcAct.type;
      this.date = srcAct.date;
      this.offset = srcAct.getOffset();
      this.roleName = srcAct.roleName;
      this.roleID = srcAct.roleID;
      
      final Integer dvIDobj = new Integer(srcAct.documentVersionID);
      if ( dvIDMapping.containsKey(dvIDobj) )
      {
          this.documentVersionID = 
                  ((Integer) dvIDMapping.get(dvIDobj)).intValue();
      }
      else
      {
          this.documentVersionID = dvIDobj.intValue();
      }
      
      
      
      if (srcAct.getContent() instanceof Link)
      {
          final Link srcLink = (Link) srcAct.getContent();
          Link dstLink;
          final Integer srcLinkTagDVID = 
                  new Integer(srcLink.getLinkTag().getDocumentVersionID());
          if ( dvIDMapping.containsKey(srcLinkTagDVID) )
          {
              final int dstLinkTagDVID = ((Integer) dvIDMapping.get(srcLinkTagDVID)).intValue();
              final LinkTag dstLinkTag = new LinkTag(
                      dstLinkTagDVID,
                      srcLink.getLinkTag().getBackLinkID());
              
              dstLink = new Link(
                      srcAct.id,
                      srcLink.getType(),
                      dstLinkTag,
                      srcLink.getLabel());
              
              dstLink.setAnchorText(srcLink.getAnchorText());
          }
          else
          {
              dstLink = srcLink;
          }
          
          this.content = dstLink;
      }
      else
      {
          this.content = srcAct.getContent();
      }
      
      verifyData();
      this.dataIsValid = true;
   }
   
   public Object getContent()
   {
      return this.content;
   }
   
   public String getId()
   {
      return id;
   }

   public int getOffset()
   {
      return offset;
   }

   public String getPlayerName()
   {
      return roleName;
   }
   
   public Date getDate()
   {
      return date;
   }
   
   public int getLength()
   {
       if (ActionType.LINK_ACTION.equals(getType()))
       {
           return ((Link) getContent()).getAnchorText().length();
       }
       else
       {
           return getContent().toString().length();
       }
   }
   
   public ActionType getType()
   {
      return this.type;
   }
   
   public String toHtml(DocumentVersion documentVersion)
   {
	  String actTxt = "";
      String stringContent = getContent().toString();
      if (stringContent.length() > 15)
      {
         stringContent = stringContent.substring(0,15) + "...";
      }
      stringContent = "'<i color=\"#0033ff\">" + stringContent + "</i>'";
      if (getType().equals(ActionType.ADD_ACTION))
      {
         actTxt = "<html>Edited <b>" + documentVersion.getDocumentTitle() + "</b>";
      }
      else if (getType().equals(ActionType.ADD_DOC_ACTION))
      {
         actTxt = "<html>Added <b>" + documentVersion.getDocumentTitle() + "</b>";
      }
      else if (getType().equals(ActionType.DELETE_ACTION))
      {
    	  actTxt = "<html>Edited <b>" + documentVersion.getDocumentTitle() + "</b>";
      }
      else if (getType().equals(ActionType.IMAGE_ACTION))
      {
         actTxt = "<html>Added image to <b>" +
             documentVersion.getDocumentTitle() + "</b>";
      }
      else if (getType().equals(ActionType.LINK_ACTION))
      {
         final Object linkObj = getContent();
         final Link link = (Link)linkObj;
         final DocumentVersion ver = documentVersion;

         // guard against link bugs
         if( link != null && ver != null )
         {             
        	 actTxt = "<html> Link: '" + link.getLabel() + "'</html>";
         }
      }
      
      return actTxt;
   }
   
   public String toString(DocumentVersion documentVersion)
   {
      String actTxt ="";
      String stringContent = getContent().toString();
      if (stringContent.length() > 15)
      {
         stringContent = stringContent.substring(0,15) + "...";
      }
      
      if (documentVersion == null)
      {
          SimpleLogger.logError("IvanhoeAction.toString() called with null argument", new Exception());
          return stringContent + " (summary incomplete do to internal errors)";
      }
      
      stringContent = "'" + stringContent + "'";
      if (getType().equals(ActionType.ADD_ACTION))
      {
         actTxt =
            "Added " + stringContent + " to '" + 
            documentVersion.getDocumentTitle() + "'";
      }
      else if (getType().equals(ActionType.ADD_DOC_ACTION))
      {
         actTxt = "Added Document '" + documentVersion.getDocumentTitle() + "'";
      }
      else if (getType().equals(ActionType.DELETE_ACTION))
      {
         actTxt = "Deleted " + stringContent + " from '" +
                 documentVersion.getDocumentTitle() + "'";
      }
      else if (getType().equals(ActionType.IMAGE_ACTION))
      {
         actTxt = "Added image " + stringContent + " to " +
                 documentVersion.getDocumentTitle() + "";
      }
      else if (getType().equals(ActionType.LINK_ACTION))
      {
         Link link = (Link)getContent();
         actTxt = 
            "Added "
               + link.getType().toString() 
               + " "
               + link.getLabel()
               + " ' in ";
         
         if (documentVersion.getRoleName().equals(
             getPlayerName()))
         {
            actTxt += ("'" + documentVersion.getDocumentTitle() + "'");
         }
         else
         {
            String possessiveName = documentVersion.getRoleName();
            if (possessiveName.endsWith("s") || possessiveName.endsWith("S"))
            {
               possessiveName += "'";
            }
            else
            {
               possessiveName += "'s";
            }
            actTxt += (possessiveName + " version of '"
               + documentVersion.getDocumentTitle() + "' dated " + 
               dateFormatter.format(documentVersion.getDate()));
         }
      }
      
      return actTxt;
   }

   /**
    * Get a textual description of an action. Content can be truncated.
    * @param isCurrent Is this in the current move
    * @param truncateLength Length of the action description
    * @return description string
    */
   public String getDescription(DocumentVersion documentVersion, boolean isCurrent, int truncateLength )
   {
      String msg ="";
      if (getType().equals(ActionType.ADD_ACTION))
      {
         msg = "Added \"";
      }
      else if (getType().equals(ActionType.DELETE_ACTION))
      {
         msg = "Deleted \"";
      }
      else if (getType().equals(ActionType.LINK_ACTION))
      {
          Object object = getContent();
          if( object != null && object instanceof Link )
          {
              Link link = (Link)object;
              msg = link.getType().toString() + " titled \""; 
          }
      }
      else if (getType().equals(ActionType.IMAGE_ACTION))
      {
         msg = "Added image \"";
      }
      else if (getType().equals(ActionType.ADD_DOC_ACTION))
      {
      	 msg = "Added document \"";
      }
      
      String stringContent = getContent().toString();
      if ( truncateLength > 0 )
      {
      	if ( stringContent.length() > truncateLength)
            stringContent = stringContent.substring(0,truncateLength) + "...";	
      }
      
      if ( isCurrent )
         msg = "(Pending) " + msg;
      msg = msg + stringContent  + "\"<br><sup>&nbsp;&nbsp;&nbsp;&nbsp;Date: " + 
         dateFormatter.format(getDate());
      
      if (documentVersion.isOwner(getRoleID()) == false )
      {
         msg = msg + "<br>&nbsp;&nbsp;&nbsp;&nbsp;Author: " + 
            getPlayerName();
      }
      msg +=  "</sup>";
      
      return msg;
   }

    public int getDocumentVersionID()
    {
        return documentVersionID;
    }
    
    public int getRoleID()
    {
        return roleID;
    }
    
    protected void verifyData()
    {
        if (ActionType.LINK_ACTION.equals(this.type))
        {
            // Do link action checks
            if ( !(this.content instanceof Link) )
            {
                throw new IllegalArgumentException(this+" contains no content, but is of type Link");
            }
            else if ( ((Link) this.content).getLinkTag() == null )
            {
                throw new IllegalArgumentException(this+" has a Link with no LinkTag");
            }
        }
        else
        {
            // TODO
        }
    }
}
