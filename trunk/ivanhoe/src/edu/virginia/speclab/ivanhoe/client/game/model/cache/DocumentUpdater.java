/*
 * Created on May 7, 2004
 *
 * DocumentUpdater
 */
package edu.virginia.speclab.ivanhoe.client.game.model.cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.document.*;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.blist.BCollection;
import edu.virginia.speclab.ivanhoe.shared.blist.BCollectionFilter;
import edu.virginia.speclab.ivanhoe.shared.blist.BLinkedList;
import edu.virginia.speclab.ivanhoe.shared.blist.BList;
import edu.virginia.speclab.ivanhoe.shared.data.*;

/**
 * @author lfoster
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DocumentUpdater
{
   private DiscourseField discourseField;
   private List actionList;
   private List currentActions;
   private IvanhoeDocument document;
   private DocumentVersion version;
   
   public DocumentUpdater( DiscourseField discourseField, IvanhoeDocument doc, DocumentVersion version)
   {
      this.version = version;
      this.document = doc;
      this.actionList = new ArrayList();
      this.currentActions = new ArrayList();
      this.discourseField = discourseField;
      
      SimpleLogger.logInfo("Updater created for " + version.toString());
   }

   public void addActions()
   {
       // TODO: call this from the constructor and make it private
       addActions(this.version);
   }
   
   private void addActions(DocumentVersion docVersion)
   {
       DocumentVersion docParent = docVersion.getParentVersion();
       if (docParent != null)
       {
           addActions(docParent);
       }
       
       if( docVersion.isPublished() )
       {
           BCollection docActions = discourseField.convertTagsToActions(docVersion.getActionIDs());
           BList docActionsList = new BLinkedList(docActions);
           docActions = (BList)docActionsList.filter( new BCollectionFilter(){
               public boolean accept(Object o)
               {
                   return isDocumentAction((IvanhoeAction)o);
               }
           });
           documentOrderSort(docActionsList);
           
           SimpleLogger.logInfo("Adding actions from document version: [" + docVersion+"]" );
           this.actionList.add( new ActionBlock(docVersion.getDate(), docActionsList ) );
       }
   }
   
   /**
    * Add the actions in this move that pertain to this document 
    * @param move The move to parse for pertinent actions.
    */
   public void addActions(Move move)
   {
       // TODO: depricate and remove
      SimpleLogger.logInfo("Adding actions from move submitted on " +
         IvanhoeDateFormat.format(move.getSubmissionDate()) + " by " + move.getRoleName());
      
      List docActs = new ArrayList();
      
      // flag this as a move from future!
      boolean futureMove = false;
      if ( move.getSubmissionDate().after(this.version.getDate()) )
      {
         SimpleLogger.logInfo("This move occurred after the requested version; only links will be considered");
         futureMove = true;
      }
      
      // look at all acts in move
      for (Iterator actions = move.getActions(); actions.hasNext();)
      {
         // collect act data
         IvanhoeAction act = (IvanhoeAction) actions.next();
         DocumentVersion actVersion = discourseField.getDocumentVersionManager().getDocumentVersion(act);
         
         // if this is a future move, only care about links
         if (futureMove == true && act.getType().equals(ActionType.LINK_ACTION) == false)
         {
            continue;
         }
         else if (futureMove == true && 
                  act.getType().equals(ActionType.LINK_ACTION) == true &&
                  actVersion.equals(this.version))
         {
            SimpleLogger.logInfo("Found future link " + act.toString());
         }
         
         // dont care about non-document actions
         if ( isDocumentAction(act) == false)
         {
            continue;
         }
         
         // only care about actions based on this or earlier version
         if ( actVersion.before(this.version) || actVersion.equals(this.version))
         {
            SimpleLogger.logInfo("Adding action [" + act.toString() + "] to update history");
            docActs.add(act);
         }
      }
      
      // sort actions into document order
      documentOrderSort(docActs);
      
      // create an action block with the moves date stamp
      // and add it to the action list
      this.actionList.add( new ActionBlock( move.getSubmissionDate(), docActs) );
   }

   /**
    * Add actions restored from a saved current move that pertain to 
    * this document. 
    * @param move
    */
   public void addCurrentActions(List restoredActs)
   {
      for (Iterator actions = restoredActs.iterator(); actions.hasNext();)
      {
         IvanhoeAction act = (IvanhoeAction) actions.next();
         
         DocumentVersion actVersion = 
             discourseField.getDocumentVersionManager().getDocumentVersion(act);
         
         // dont care about non-document actions
         if ( isDocumentAction(act) == false)
         {
            continue;
         }
         
         // only care about actions based on this or earlier version
         if ( actVersion.before(this.version) || actVersion.equals(this.version))
         {
            this.currentActions.add(act);
         }
      }
      
      // We DON'T sort the current actions into document order so that we
      // preserve the order they were created in.  Why this matters in current
      // moves but not in others isn't entirely understood.
   }
   
   /**
    * Check if this act ais a document-related action
    * @param act
    * @return
    */
   private boolean isDocumentAction(IvanhoeAction act)
   {
      return (IvanhoeTag.getTagForActionType(act.getType()) != null);
   }
   
   /**
    * Sort actions to DocumentOrder
    */
   private void documentOrderSort(List list)
   {
      IvanhoeAction act1, act2;
      for (int i = 0; i < list.size(); i++)
      {
         for (int j = i + 1; j <  list.size(); j++)
         {
            act1 = (IvanhoeAction)list.get(i);
            act2 = (IvanhoeAction)list.get(j);
            if ( act1.getOffset() > act2.getOffset())
            {
               list.set(i, act2);
               list.set(j, act1);
            }
         }
      }
   }
   
   private void dateSort(List list)
   {
      ActionBlock block1, block2;
      for (int i = 0; i < list.size(); i++)
      {
         for (int j = i + 1; j <  list.size(); j++)
         {
            block1 = (ActionBlock)list.get(i);
            block2 = (ActionBlock)list.get(j);
            if ( block1.date.after(block2.date) )
            {
               list.set(i, block2);
               list.set(j, block1);
            }
         }
      }
   }

   /**
    * Update the document to the specified time
    */
   public void updateDocument()
   {  
      // sort all actions; moves are sorted by date. Within a move
      // actions are sorted by document order.
      // current actions are a separate list in document order
      dateSort(this.actionList);
      
      // apply all of the actions in sequence
      Iterator itr = this.actionList.iterator();
      while (itr.hasNext())
      { 
         applyActions(
            ((ActionBlock)itr.next()).actions, false);
      }
      
      // apply all active actions
      applyActions(this.currentActions, true);
   }

   /**
    * Apply an action to the document
    * @param act
    * @param fromLocal
    */
   private void applyAction(IvanhoeAction act, boolean isCurrent)
   {
      SimpleLogger.logInfo("applying action [" + act.toString() + "] from ["+act.getDate()+"]");
      
      if (document.containsAction(act))
      {
         SimpleLogger.logInfo("Skipping action; it already exists in the document");
         return;
      }
     
      if (act.getType().equals(ActionType.ADD_ACTION))
      {
         document.restoreAddedText(
            act.getId(),
            act.getOffset(),
            act.getContent().toString(),
            isCurrent);
      }
      else if (act.getType().equals(ActionType.DELETE_ACTION))
      {
         document.restoreDeletedText(
            act.getId(),
            act.getOffset(),
            act.getLength(),
            isCurrent);
      } 
      else if (act.getType().equals(ActionType.LINK_ACTION))
      {
         Link linkTarget = (Link)act.getContent();
         document.restoreLink(
            act.getOffset(),
            linkTarget.getAnchorText().length(),
            linkTarget);
      }
      else if (act.getType().equals(ActionType.IMAGE_ACTION))
      {
         document.insertImage(act.getId(), act.getOffset(),(String) act.getContent(), isCurrent);
      }
   }

   /**
    * Apply all actions before actionTime to the document
    * @param actionList
    * @param actionTime
    * @param isCurrent
    */
   private void applyActions(List actions, boolean isCurrent)
   {
      // walk the iterator and apply actions
      for (Iterator i = actions.iterator(); i.hasNext();)
      {
         applyAction((IvanhoeAction) i.next(), isCurrent);
      }
   }
   
   /**
    *  A block of actions from a move along with the date of submission
    */
   private final class ActionBlock
   {
      private List actions;
      private Date date;
      
      public ActionBlock(Date date, List acts)
      {
         this.date = date;
         this.actions = new ArrayList(acts);
      }
   }
}
