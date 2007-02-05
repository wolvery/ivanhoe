/*
 * Created on May 4, 2004
 *
 * CurrentAction
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse;

import java.io.File;
import java.util.Date;

import edu.virginia.speclab.ivanhoe.client.game.model.document.IvanhoeDocument;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.ActionType;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;


/**
 * Marker for an action that is currently in progress.
 * It does not attempt to hold any content or offset info
 * for the act. Instead, it relies upon the document instance
 * to look up the actual offset & content. This way, the info
 * is always accurate and no additional book keeping is necessary.
 */
public class CurrentAction extends IvanhoeAction
{  
   private final DiscourseField discourseField;
   private final DocumentVersion documentVersion;
   
   public CurrentAction( DiscourseField discourseField, int versionID,
           String actionId, ActionType type, Date date)
   {
      super(type, versionID, discourseField.getCurrentMove().getRoleName(),
              discourseField.getCurrentMove().getRoleID(), actionId, date);
      this.discourseField = discourseField;
      this.documentVersion = discourseField.getDocumentVersionManager().getDocumentVersion(versionID);
   }
   
   /**
    * Lookup the action offset in the document
    */
   public int getOffset()
   {
      
      IvanhoeDocument doc = 
          discourseField.getOpenDocument(getDocumentVersion());
      if (doc != null)
      {
         return doc.getActionOffset(getId());
      }
      
      return 0;
   }
   
   /**
    * Lookup the action content in the document
    */
    public Object getContent()
    {
        final Object content;
        if (getType().equals(ActionType.ADD_DOC_ACTION))
        {
            content = discourseField.getDocumentInfo(documentVersion.getDocumentTitle());
        }
        else
        {
            IvanhoeDocument doc = discourseField.getOpenDocument(getDocumentVersion());
            if (doc != null)
            {
                final ActionType actionType = getType();
                if ( ActionType.IMAGE_ACTION.equals(actionType) )
                {
                    File img = new File(doc.getActionContent(getId(), actionType));
                    content = img.getName();
                }
                else if ( ActionType.LINK_ACTION.equals(actionType) )
                {
					content = doc.getLinkManager().getLink(getId());
                }
                else
                {
                    content = doc.getActionContent(getId(), actionType);
                }
            }
            else
            {
                content = "";
            }
        }
        
        if (content == null)
        {
            SimpleLogger.logError("CurrentAction is returning null content");
        }
        return content;
    }
   
   public DocumentVersion getDocumentVersion()
   {
       return documentVersion;
   }
}