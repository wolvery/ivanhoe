/*
 * Created on Oct 20, 2003
 *
 * IvanhoeDocument
 */
package edu.virginia.speclab.ivanhoe.client.game.model.document;

import java.util.*;

import javax.swing.text.*;
import javax.swing.text.html.*;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DocumentVersionManager;
import edu.virginia.speclab.ivanhoe.client.game.model.util.StatusEventMgr;
import edu.virginia.speclab.ivanhoe.shared.GuidGenerator;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.*;

import javax.swing.event.DocumentEvent.EventType;


/**
 * @author lfoster
 *
 * Extension of an HTML documengt that handles Ivanhoe specific tags
 * and fires Ivanhoe specific events
 */
public class IvanhoeDocument extends HTMLDocument
{
    private final DocumentVersionManager dvManager;
    
   private DocumentInfo docInfo;
   private DocumentVersion version;
   private LinkManager linkMgr;
   private List actionHistory;
   private List actionListeners;
   private boolean eventsEnabled;
   private boolean readOnly;
   private boolean showDeleteTags;
   private boolean showUnderlines;
     
   /**
    * Construct the document using the passed stylesheet
    * @param styles StyleSheet used for the document
    */
   public IvanhoeDocument(DocumentVersionManager dvManager, StyleSheet styles)
   {
      super(styles);
      this.dvManager = dvManager;
      
      this.setPreservesUnknownTags(false);
      this.linkMgr = new LinkManager(this);
      this.actionHistory = new ArrayList();
      
      // create the vectors of listeners for the document
      this.actionListeners = new ArrayList();
      this.eventsEnabled = true;
      
      this.showDeleteTags = true;
      this.showUnderlines = true;
      this.readOnly = false;
   }
   
   /**
    * Toggle visibility of deleted text
    * @param show
    */
   public void setShowDeleteTags(boolean show)
   {
      this.showDeleteTags = show;
      this.fireChangedUpdate( new DefaultDocumentEvent(0, getLength(), 
         EventType.CHANGE));
   }
   
   /**
    * Toggle the underline on links
    * @param show 
    */
   public void setShowUnderlines(boolean show)
   {
      this.showUnderlines = show;
      this.fireChangedUpdate( new DefaultDocumentEvent(0, getLength(), 
         EventType.CHANGE));
   }   
   
   /**
    * @return Returns true if deleted text is visible
    */
   public boolean areDeleteTagsVisible()
   {
      return this.showDeleteTags;
   }
   
   /**
    * @return Returns true if deleted text is visible
    */
   public boolean areLinksUnderlined()
   {
      return this.showUnderlines;
   }
   
   /**
    * Set the document info
    * @param string
    */
   public void setDocumentInfo(DocumentInfo docInfo)
   {
      this.docInfo = docInfo;
   }
   
   /**
    * Get the version info for this document
    * @return
    */
   public DocumentVersion getVersion()
   {
      return this.version;
   }
   
   public void setVersion(DocumentVersion version)
   {
      this.version = version;
   }
   
   /**
    * Enable / Disable the firing of events from this document
    * @param enable
    */
   public void setEventsEnabled(boolean enable)
   {
      this.eventsEnabled = enable;  
   }
   
   /**
    * Check if events are enabled for this document
    * @return
    */
   public boolean areEventsEnabled()
   {
      return this.eventsEnabled;
   }
   
   /**
    * Get the link manager
    * @return
    */
   public LinkManager getLinkManager()
   {
      return this.linkMgr;
   }
   
   /**
    * Get the title of this document
    * @return
    */
   public String getTitle()
   {
      if (this.docInfo == null)
         return "Undefined";
         
      return this.docInfo.getTitle();
   }
   
   /**
    * Get the documentInfo data 
    */
   public DocumentInfo getInfo()
   {
      return this.docInfo;
   }
   
   /**
    * Add a listener for action events generated 
    * when IvanhoeTags are added, deleted or changed
    * @param listener
    */
   public void addActionListener(IDocumentActionListener listener)
   {
      if (this.actionListeners.contains(listener) == false)
      {
         this.actionListeners.add(listener);
      }
   }
   
   /**
    * Send notification an ivanhoe action has been added to the doc
    * @param id id the new ActionID that was added
    * @param tag Type of tag
    */
   protected void fireActionAdded(String id, HTML.Tag tag)
   {
      // add this action id to the action history
      this.actionHistory.add(id);
      
      if (areEventsEnabled())
      {  
         final DocumentVersion version = getVersion();
         for (java.util.Iterator i=actionListeners.iterator(); i.hasNext(); )
         {
            IDocumentActionListener daListener = (IDocumentActionListener) i.next();
            daListener.actionAdded(version, id, tag);
         }
      }
   }
   
   /**
    * Send notification an ivanhoe action has been deleted
    * @param deletedElement the element data for the action
    */
   protected void fireActionDeleted(String actionId)
   {
      // remove actionId from history
      this.actionHistory.remove(actionId);
      
      if (areEventsEnabled())
      {
         for (java.util.Iterator i=actionListeners.iterator(); i.hasNext(); )
         {
            final IDocumentActionListener daListener = (IDocumentActionListener) i.next();
            daListener.actionDeleted(actionId);
         }
      }
   }
   
   /**
    * Remove an action listener
    * @param listener
    */
   public void removeActionListener(IDocumentActionListener listener)
   {
      this.actionListeners.remove(listener);
   }
   
   /**
    * Obtain the timeline assocated with this document.
    * @return a timeline object.
    */
   public List getActionHistory()
   {
      return new ArrayList(this.actionHistory);
   }
   
   /**
    * Tests if the document object is set to read only mode.
    * @return
    */
   public boolean isReadOnly()
   {
      return this.readOnly;
   }

   /**
    * Sets the document to be read only.
    */
   public void setReadOnly(boolean readOnly)
   {
      this.readOnly = readOnly;   
   }
   /**
    * Return the IvanhoeReader for the passed in position
    */
   public HTMLEditorKit.ParserCallback getReader(int pos)
   {
      IvanhoeReader reader = new IvanhoeReader(pos);
      return reader;
   }
   
   /**
    * Get the content for a copy/cut operation. This does not
    * include text tagged with delete
    * @param start Beginning of the selection to be copied
    * @param end End of the selection to be copied
    * @return IvanhoeClipboardContent
    */
   public IvanhoeClipboard copyContent( DiscourseField discourseField, int start, int end)
   {     
      StringBuffer buf = new StringBuffer();
      java.util.Iterator spanned = getSpannedElements(start, end).iterator();
      
      Element ele;
      int actualStart = start;
      int actualEnd = end;
      int offset = 0;
      String copiedTxt;
      Vector processed = new Vector();
      Vector actions = new Vector();
      while (spanned.hasNext())
      {
         ele = (Element)spanned.next();
         
         // get the actual starting place - this accounts for
         // selections that start/end in the middle of an element
         actualStart = Math.max(start, ele.getStartOffset());
         actualEnd = Math.min(end, ele.getEndOffset() );
               
         // only copy non-deletes
         if (ele.getAttributes().isDefined(IvanhoeTag.DELETE) == false)
         {
            // get the text content
            try
            {
               copiedTxt = getText(actualStart, actualEnd-actualStart);
               buf.append( copiedTxt );
            }
            catch (BadLocationException e)
            {
               SimpleLogger.logError("Unable to get text for copy");
               return null;
            }
            
            // Copy ADD action info
            if (ele.getAttributes().isDefined(IvanhoeTag.ADD))
            {
               String actId;
               IvanhoeAction act = null;
               actId = getTagId(IvanhoeTag.ADD, ele);
               act = discourseField.lookupAction( actId );
               if (act != null && processed.contains(actId) == false)
               {
                  actions.add(act);
                  processed.add(actId);
               }
            }
            
            // Copy LINK action info
            if (ele.getAttributes().isDefined(IvanhoeTag.ILINK))
            {
               // grab the linkId
               String linkId;
               linkId = getTagId(IvanhoeTag.ILINK, ele);
               if (processed.contains(linkId) == false)
               {
                  // mark it as processed
                  processed.add(linkId);
                  
                  // get the link data
                  List tgts = this.linkMgr.getLinks(linkId);
                  IvanhoeAction linkAct;
                  Link tgt, newLink;
                  java.util.Iterator tgtItr = tgts.iterator();
                  while (tgtItr.hasNext())
                  {
                     tgt = (Link)tgtItr.next();
                     linkAct = discourseField.lookupAction(tgt.getId() );  
                     newLink = new Link(tgt);
                     newLink.setAnchorText(copiedTxt);
                     String currentRoleName = discourseField.getCurrentMove().getRoleName();
                     int currentRoleID = discourseField.getCurrentMove().getRoleID();
                     actions.add(new IvanhoeAction(ActionType.LINK_ACTION,getVersion().getID(),
                        currentRoleName, currentRoleID, linkAct.getId(), offset, newLink,
                        Ivanhoe.getDate())); 
                  }
               }
            }
            
            offset+= (actualEnd - actualStart);
         }
      }
      
      // return the content class
      SimpleLogger.logInfo("Content copied from " + getVersion().getRoleName());
      return new IvanhoeClipboard(getVersion(), buf.toString(), actions );
   }
   
   /**
    * Insert an image at the specified location
    * @param guid Unique action ID with which to mark the insert
    * @param offset Location of the insert
    * @param imageFile Image file to insert
    */
   public void insertImage(String guid, int offset, String imageFile, boolean isNewAdd)
   {
      if (offset > getLength()) 
      {
         StatusEventMgr.fireErrorMsg("Unable to insert an image at the current position");
         return;
      }
      
      if (offset <= 0)
      {
          SimpleLogger.logError("Trying to insert image before start of document.  Failing");
          return;
      }
      
      try
      {
          SimpleAttributeSet imgAttribs = new SimpleAttributeSet();
          SimpleAttributeSet addAttribs = new SimpleAttributeSet();
          
          addAttribs.addAttribute(HTML.Attribute.ID, guid);
          //if (isNewAdd) addAttribs.addAttribute("new", "true")
          // TODO: don't add this 'til we make sure not to duplicate the IMG
          // attribute every place we grab attributes from new blocks to
          // aggregate elements.
          
          imgAttribs.addAttribute(StyleConstants.NameAttribute, HTML.Tag.IMG);
          imgAttribs.addAttribute(HTML.Attribute.ID, guid);
          imgAttribs.addAttribute(HTML.Attribute.SRC, imageFile);
          imgAttribs.addAttribute(HTML.Attribute.ALIGN, "middle");
          imgAttribs.addAttribute(IvanhoeTag.ADD, addAttribs);
                    
          this.writeLock();
          this.insertString(offset, " ", null);
          this.replace(offset, 1, " ", imgAttribs);
          this.writeUnlock();
          
          fireActionAdded(guid, HTML.Tag.IMG);
      }
      catch (Exception e)
      {
         StatusEventMgr.fireErrorMsg("<html><b>Insert image failed!</b><br>Reason: " + e.toString());
      }
   }
   
   /**
    * Determine if a given action is present in this document.
    * @param act The action in question.
    * @return <code>true</code> if it is present, <code>false</code> otherwise.
    */
   public boolean containsAction(IvanhoeAction act)
   {
      // sanity check.. action must be non-null and targeted to this doc
      if (act == null)
      {
         return false;
      }
      if (dvManager.getDocumentVersion(act).equals(getTitle()) == false)
      {
         return false;
      }
      
      // check to see if it is part of the doc structure
      boolean found = false;
      String id;
      
      // links can have many anchor ids. the action is only 
      // present if all anchor ids are present
      if (act.getType().equals(ActionType.LINK_ACTION) )
      {
         found = (this.linkMgr.getLink(act.getId()) != null);
      }
      else
      {
         id = act.getId();
         if ( getElement(id) != null )
         {
            found = true;
         }
      }
      
      return found;
   }
   
   /**
    * Remove the new attribute from the specified action. Call this 
    * method when an action is submitted to the server
    * @param actionId
    * @param tagType
    */
   public void removeNewAttribute(String actionId)
   {
      // grab the tag for this action and determine if there is
      // anything more to do. Link anchors do not have a new tag,
      // and AddDoc actions have no tag representation
      HTML.Tag tag = getActionTag(actionId);
      if (tag == null || tag.equals(IvanhoeTag.ILINK))
      {
         // nothing more to do
         return;
      }
      
      SimpleLogger.logInfo("Updating new attribute for tag " + actionId);
      writeLock();
      
      TagIterator iter = new TagIterator(this, tag, actionId);
      while (iter.isValid() ) 
      {  
         MutableAttributeSet eleAtts = new SimpleAttributeSet(
            iter.getAttributes());
         SimpleAttributeSet tagAtts = new SimpleAttributeSet(
            (AttributeSet)eleAtts.getAttribute(tag));
         if (tagAtts != null && tagAtts.isDefined("new"))
         {
            eleAtts.removeAttribute(tag);
            tagAtts.removeAttribute("new");
            eleAtts.addAttribute(tag, tagAtts);
            setCharacterAttributes(iter.getStartOffset(),
               iter.getLength(), eleAtts, true);
         }
            
         iter.next();
      }    
      
      writeUnlock();
   }
  
   /**
    * Remove IvanhoeActionTag with ID = actionId
    * @param actionId
    */
   public boolean removeActionTag(IvanhoeAction act)
   { 
      HTML.Tag tagType;
      String actionId;
      
      // grab the tag representation of this action
      tagType = IvanhoeTag.getTagForActionType(act.getType());
      actionId = act.getId();
      
      // some actions have no tag. If this is one, we're done.
      if ( tagType == null)
      {
         return true;
      }
      
      // if its an add, just hack out the given length of text
      if (tagType.equals(IvanhoeTag.ADD))
      {
         try
         { 
            remove(act.getOffset(), act.getLength());
         }
         catch (BadLocationException e)
         {
            SimpleLogger.logError(
               "Unable to remove add action " + act.getId() 
               + ": " + e);
               return false;
         }
      }
      else if (tagType.equals(HTML.Tag.IMG))
      {
         // hack out the image element
         Element imgEle = getElement(act.getId());
         try
         {
            remove(imgEle.getStartOffset(), imgEle.getEndOffset()-imgEle.getStartOffset());
         }
         catch (BadLocationException e)
         {
            SimpleLogger.logError(
               "Unable to remove image action " + act.getId() 
               + ": " + e);
               return false;
         }
      }
      else if (tagType.equals(IvanhoeTag.ILINK))
      {
         // adjust the links list if the action being removed is a link
         this.linkMgr.remove(actionId);
      }
      else 
      {
         // just remove the tag
         removeTag(tagType, actionId);
      }
       
      // notify others
      fireActionDeleted(actionId);
      
      return true; 
   }

   protected boolean removeTag(HTML.Tag tagType, String tagId)
   {
      boolean tagFound = false;
      writeLock();
      
      // remove all instances of this tag from the document
      TagIterator iter = new TagIterator(this, tagType, tagId);
      if (iter.isValid()) tagFound = true;
      while (iter.isValid() ) 
      {
         MutableAttributeSet newAtts = new SimpleAttributeSet();
         newAtts.addAttributes( iter.getAttributes() );  
         newAtts.removeAttribute(tagType);
         setCharacterAttributes(iter.getStartOffset(),
            iter.getEndOffset() - iter.getStartOffset(),
            newAtts,true); 
         
         iter.next();
      }
      
      writeUnlock();
      
      return tagFound;
   }

   public void restoreLink(int start, int len, Link linkTarget)
   {
      setEventsEnabled(false);
      markLinkedText(start, start+len, linkTarget);
      setEventsEnabled(true);
   }
   
   public void createLink(int start, int end, Link linkTarget)
   {
      markLinkedText(start, end, linkTarget);
   }
   
   /**
    * Add a new link tag of type linkType to the document at position
    * start - end.
    * @param start start of the text range to be used as the link anchor
    * @param end end of the text range to be used as the link anchor
    * @param link the details of the link
    * @return
    */
   protected void markLinkedText(int start, int end, Link link)
   {  
      writeLock();
      
      // add the link to mgr
      this.linkMgr.add(link);
      
      // Grab a list of all elements spanned by this selection, and
      // mark each element as a link
      TextRange selRange = new TextRange(start, end);
      for (java.util.Iterator itr = getSpannedElements(start, end).iterator();itr.hasNext();)
      {
         Element spannedElement = (Element)itr.next();
         TextRange eleRange = new TextRange(spannedElement);
         TextRange overlap = selRange.getOverlap( eleRange );
         
         if (spannedElement.getAttributes().isDefined(IvanhoeTag.ILINK))
         {   
            // grab the exiting id
            String existingAnchorId = 
               this.getTagId(IvanhoeTag.ILINK, spannedElement);
               
            // full overlap?
            if (overlap.equals(eleRange))
            {
               // Add this id to the new link as an anchor
               this.linkMgr.addAnchorId(link.getId(), existingAnchorId);
            }
            else
            {
               // Split existing id into 2 ids. 1st step, grab old
               // attributes at overlapped positions
               MutableAttributeSet eleAtts = 
                  new SimpleAttributeSet(spannedElement.getAttributes());
               SimpleAttributeSet linkAtts = new SimpleAttributeSet(
                  (AttributeSet)eleAtts.getAttribute(IvanhoeTag.ILINK) );
               eleAtts.removeAttribute(IvanhoeTag.ILINK);
               
               // replace the id of this region with a new one, thereby
               // splitting the existing anchor into 2 anchors
               String newId = GuidGenerator.generateID();
               linkAtts.addAttribute(HTML.Attribute.ID, newId);
               eleAtts.addAttribute(IvanhoeTag.ILINK, linkAtts);
               setCharacterAttributes(overlap.start, overlap.getLength(),
                  eleAtts, true);
               
               // add new anchor to the new link and
               // to all links achored at existing id
               this.linkMgr.addAnchorId(link.getId(), newId);
               this.linkMgr.splitAnchor(existingAnchorId, newId);
            }
         }
         else
         {
            // generate the attributes for an anchor tag
            SimpleAttributeSet anchorAttribs = new SimpleAttributeSet();
            SimpleAttributeSet aSet = new SimpleAttributeSet();
            String newAnchorId = GuidGenerator.generateID();
            aSet.addAttribute(HTML.Attribute.ID, newAnchorId);
            
            anchorAttribs.addAttribute(IvanhoeTag.ILINK, aSet);
            
            // no links exist here, just add a new anchor
            MutableAttributeSet currAtts = new SimpleAttributeSet();
            currAtts.addAttributes( spannedElement.getAttributes() );
            currAtts.addAttributes( anchorAttribs );
            setCharacterAttributes(overlap.start, overlap.getLength(),
               currAtts, true);
            
            this.linkMgr.addAnchorId(link.getId(), newAnchorId);
         }
      }
      
      // add it to the list, and notify others that action added
      
      fireActionAdded( link.getId(), IvanhoeTag.ILINK );
      
      writeUnlock();
   }
   
   /**
    * Add new text to the doc and wrap it with a new Ivanhoe Add tag
    * If the new add is in the middle of an existing add, just add the
    * text and fire a change event; no new tag will be created
    * @param insertPos the location to begin the addition
    * @param newTxt the text to add
    * @return true if text was successfully added, false otherwise
    */
   public boolean addNewText(int insertPos, String newTxt)
   {      
      try
      {
         markAddedText(GuidGenerator.generateID(), insertPos, newTxt, true);
      }
      catch (Exception e)
      {
         SimpleLogger.logError("Unable to add text [" + newTxt + 
            "] at position [" + insertPos + "]: " + e);
         StatusEventMgr.fireErrorMsg("Unable to add new text at this location");
         return false;
      }

      return true; 
   }
   
   /**
    * Restore text that was added in a prior action
    * @param guid ID of the tag 
    * @param insertPos Position to make the insert
    * @param newTxt Text to insert
    * @return true if add was restored, false otherwise
    */
   public void restoreAddedText(String guid, int insertPos, 
      String newTxt, boolean isCurrent)
   {
      setEventsEnabled(false);
      
      try
      {
         markAddedText(guid, insertPos, newTxt, isCurrent);        
      }
      catch (Exception e)
      {
         SimpleLogger.logError("Unable to restore added text [" + newTxt + 
            "] at position [" + insertPos + "]: ", e);
      }


      setEventsEnabled(true);
   }
   
   /**
    * Insert the text contained in newTxt at the specified position
    * Use the supplied guid as the tag ID. If this is a new addtion, the
    * new attribute will be added to the tag. New occur when a user types in the 
    * doc. Other adds occur when saved changes are restored.
    * @param guid ID to use for the tag
    * @param insertPos Insertion point for the text
    * @param newTxt Text to insert
    * @param isNewAdd Set to true if this is added as a result of user activity,
    *                 Set to false if this is a restore of a saved action    
    */
   protected void markAddedText(String guid, int insertPos, String newTxt, 
      boolean isNewAdd) throws BadLocationException 
   {  
      writeLock();
      
      // replace CR with <p> pairs in long adds
      if (newTxt.length() > 1)
      {
         newTxt.replaceAll("\n", "<br>");
      }
      
      // grab the element we are attempting to insert into
      Element targetElement;
      targetElement = getCharacterElement(insertPos);
      AttributeSet attributes = targetElement.getAttributes();

      // if this is in the middle of a delete, move insert pos to 
      // start of the delete and insert there. 
      if (attributes.isDefined(IvanhoeTag.DELETE))
      {
         insertPos = targetElement.getStartOffset();
      }
      
      int elementPos = insertPos;
      while (attributes.containsAttribute(
              AttributeSet.NameAttribute, IvanhoeTag.getTagForActionType(ActionType.IMAGE_ACTION)))
      {
          // If the neighboring element is an image, we should use the parent
          // attribute instead of taking the image attribute.  So, we'll step
          // backwards until we find it.
          
          targetElement = getCharacterElement(--elementPos);
          attributes = targetElement.getAttributes();
      }
      
      // get length prior to insertion of new content
      int preAddLen = getLength();
      boolean boundaryAddition = (targetElement.getStartOffset() == insertPos);
      
      // *** Add the text preserving existing attributes ***   
      insertString(insertPos, newTxt, attributes);
      
      // get new doc length, and calculate the length of addition
      int postAddLen = this.getLength();
      int addLen = postAddLen - preAddLen;
      boolean createTag = true;
      
      // *** Adjust attributes and generate events ***
      if (hasCurrentTag(targetElement,IvanhoeTag.ADD))
      {
         createTag = false;
      }
      else
      {
         // check neighboring element
         Element priorElement = this.getCharacterElement(insertPos-1);
         if (hasCurrentTag(priorElement,IvanhoeTag.ADD))
         {
            MutableAttributeSet eleAtts = 
               new SimpleAttributeSet(priorElement.getAttributes());
            
            setCharacterAttributes(insertPos, addLen,
               eleAtts, true);
            
            createTag = false;
         }
      }
      
      // If addition was not handled above, just create a new add tag
      if (createTag == true)
      {
         // This is a new addition; create attributes
         SimpleAttributeSet markAttribs = new SimpleAttributeSet();
         SimpleAttributeSet aSet = new SimpleAttributeSet();
         aSet.addAttribute(HTML.Attribute.ID, guid);
         if (isNewAdd)
            aSet.addAttribute("new","true");
         markAttribs.addAttribute(IvanhoeTag.ADD, aSet);
         
         SimpleAttributeSet existAtts;
         if (attributes != null)
         {
	         // grab a copy of existing attributes at the insert point
	         existAtts = new SimpleAttributeSet(attributes.copyAttributes());
         }
         else
         {
             // create an empty attribute set
             existAtts = new SimpleAttributeSet();
         }
            
         // dont propagate existing add tags
         // this causes the existing add to be split around the new
         // add, therby letting players add text to existing adds
         existAtts.removeAttribute(IvanhoeTag.ADD);
         existAtts.removeAttribute(IvanhoeTag.DELETE);
         
         // Text added to middle of links preserves the link tag,
         // but text at boundaries does not
         if ( boundaryAddition )
         {
            existAtts.removeAttribute(IvanhoeTag.ILINK);
         }
         
         // add the existing atts to the new add atts
         markAttribs.addAttributes(existAtts);
        
         // set new attributes and fire new action event
         setCharacterAttributes(insertPos, addLen, markAttribs, true);
         fireActionAdded(guid, IvanhoeTag.ADD);
      }
           
      writeUnlock();
   }
   
  /**
    * Restore a deletion that was made by a prior action 
    * @param guid The ID of the prior deletion
    * @param startPos Staring position of the delete
    * @param length length of the delete
	*/
   public void restoreDeletedText(String guid, int startPos, int length,
      boolean isCurrent)
   {
      this.eventsEnabled = false;
      try
      {
         this.markDeletedRange(guid,startPos,startPos+length,isCurrent);
      }
      catch (BadLocationException e)
      {
         SimpleLogger.logError("Unable to restore deletion at [" + 
            startPos + " - " + (startPos+length) + "]: " + e);
      }
      this.eventsEnabled = true;
   }

   /**
    * Delete text in the range startPos to endPos. This method checks for
    * adjacent, current delete tags and automatically extends the range to
    * include them. Deleting text does not remvoe the text from the doc; it
    * wraps it in a Delete Tag.
    * @param startPos
    * @param endPos
    * @return 
    */
   public boolean deleteText(int startPos, int endPos)
   {
      Element testElement;
      int actualEnd = endPos;
      int actualStart = startPos;
      
      // check if elements immediately following this one are a current delete
      // add them to the range if found
      boolean done = false;
      while (!done)
      {
         if (actualEnd < this.getLength() )
         {
            testElement = this.getCharacterElement(actualEnd);  
            if (hasCurrentTag(testElement,IvanhoeTag.DELETE))
            {
               actualEnd = Math.max(actualEnd,testElement.getEndOffset());
            }
            else
            {
               done = true;
            }
         }
         else
         {
            done = true;
         }
      }
            
      // check if elements immediately preceeding this one are a current delete 
      // add them to the range if found
      done = false;
      while (!done)
      {
         if (actualStart > 0 )
         {
            testElement = this.getCharacterElement(actualStart-1);  
            if (hasCurrentTag(testElement, IvanhoeTag.DELETE))
            {
               actualStart = Math.min(actualStart,testElement.getStartOffset());
            }
            else
            {
               done = true;
            }
         }
         else
         {
            done = true;
         }
      }
      
      try
      {
         this.markDeletedRange(GuidGenerator.generateID(),
            actualStart, actualEnd, true);
      }
      catch (BadLocationException e)
      {
         SimpleLogger.logError("Unable to delete text at position [" + 
            startPos + " - " + endPos + "]: " + e);
         return false;
      }
      
      return true;
   }
 
   /**
    * mark all elements in the given range with a delete tag
    * if this range joins current deletions, use the id of an existing delete
    * this method works by removing existing characters, and re-adding them
    * with the correct style
    * Special cases: deletion of an active add just removes the added text
    *                deletion of a link removes link targets
    * @param startPos
    * @param endPos
    * @throws BadLocationException,IOException
    */
   protected void markDeletedRange(String guid, int startPos, int endPos,
      boolean isNewDelete ) throws BadLocationException
   {
      writeLock();
      
      if (endPos > getLength())
      {
          writeUnlock();
          throw new BadLocationException("Deleting characters after the end of the document "
                  +"(length="+getLength()+")", endPos);
      }
      
      if (startPos < 0)
      {
          writeUnlock();
          throw new BadLocationException("Deleting characters before the beginning of the document",
                  startPos);
      }
      
      Collection spannedElements = getSpannedElements(startPos, endPos);
      boolean newDelete = false;
      
      SimpleAttributeSet deleteAttribs = new SimpleAttributeSet();
      SimpleAttributeSet aSet = checkForCurrentDeleteTag(spannedElements);
      if (aSet == null)
      {
         // create attribs for new tag
         aSet = new SimpleAttributeSet();
         aSet.addAttribute(HTML.Attribute.ID, guid);
         if (isNewDelete)
            aSet.addAttribute("new","true");
         deleteAttribs.addAttribute(IvanhoeTag.DELETE, aSet); 
         newDelete = true;    
      }  
      else
      {
         // use existing atts, and save the guid
         guid = (String)aSet.getAttribute(HTML.Attribute.ID);
         deleteAttribs.addAttribute(IvanhoeTag.DELETE, aSet);
         newDelete = false;
      }    
      
      // Iterate over vector of selected elements 
      // add the existing action attributes to each
      // insert each element text with the new attributes
      int actualEnd = endPos;
      boolean fireEvents = false;
      
      for (java.util.Iterator i=spannedElements.iterator(); i.hasNext(); )
      {
         final Element currentElem = (Element) i.next();    
         final int currStart = Math.max(startPos, currentElem.getStartOffset());;
         final int spannedLen;
         
         if (currentElem.getAttributes()
                 .containsAttribute(StyleConstants.NameAttribute, HTML.Tag.IMG))
         {
             // Images are represented by a single glyph, so although their
             // contents' length is the length of the image filename, the
             // spanned length is always 1. --b.c
             spannedLen = 1;
         }
         else
         {
             final int currEnd = Math.min(actualEnd, currentElem.getEndOffset() );
             spannedLen = currEnd - currStart;
         }
            
         // grab attributes of spanned element and put them in a mutable set
         MutableAttributeSet currAtts = new SimpleAttributeSet();
         currAtts.addAttributes( currentElem.getAttributes() );
         
         // if this tag is delete with a differnt guid,
         // replace attributes with new ones
         if ( hasCurrentTag(currentElem, IvanhoeTag.DELETE) )
         {
            String tagId = getTagId(IvanhoeTag.DELETE, currentElem);
            if (tagId.equals(guid) == false)
            {
               fireActionDeleted(tagId);
               currAtts.removeAttribute(IvanhoeTag.DELETE); 
               currAtts.addAttributes( deleteAttribs );
               fireEvents = true;
            }
         }
         else
         {
            if (currAtts.isDefined(IvanhoeTag.DELETE) == false)
            {
               // add delete attribs to current element attribs
               currAtts.addAttributes( deleteAttribs );
               fireEvents = true;
            }
            else
            {
               // this is an existing delete. dont touch
               // and dont fire events
               fireEvents = false;
            }
         }
         
         // Any special handling for a current addition being deleted?
         if (hasCurrentTag(currentElem, IvanhoeTag.ADD) == false)
         {
            setCharacterAttributes(currStart,spannedLen, currAtts, true);
         }
         else
         {
            // remove the added text
            remove(currStart, spannedLen);

            // move back the endPos to account for the skipped text
            actualEnd-=spannedLen; 
            
            // Notify of change ore deltion of this add
            if ( getElement(getTagId(IvanhoeTag.ADD, currentElem)) == null)
            {
               fireActionDeleted(getTagId(IvanhoeTag.ADD, currentElem));
            }
            
            // no need to fire off a new event for this
            fireEvents = false;      
         }
      }
      
      // notify listener of changes
      if (fireEvents == true && newDelete == true)
      {
         fireActionAdded( guid, IvanhoeTag.DELETE);
      }
      
      writeUnlock();
   }

   /**
    * Helper that iterates over a list of elements and returns attributes
    * for the first occurance of a new delete tag.  A current delete tag is
    * denoted by the presence of the 'new' attribute set to 'true'.
    * 
    * @param spannedElements Vector of Element objects
    * @return Attributes of first active delete tag
    */
   protected SimpleAttributeSet checkForCurrentDeleteTag(Collection spannedElements)
   {
      SimpleAttributeSet atts = null;
      
      for (java.util.Iterator i=spannedElements.iterator(); i.hasNext();)
      {
         final Element ele = (Element) i.next();
         if ( hasCurrentTag(ele, IvanhoeTag.DELETE))
         {
            atts = (SimpleAttributeSet)ele.getAttributes().getAttribute(
               IvanhoeTag.DELETE);
            break;
         }
      }
      
      return atts;
   }
     
   /**
    * Extract Action ID of the ivanhoe tag from the given element
    * @param element
    * @param tag
    * @return
    */
   protected String getTagId(IvanhoeTag tag, Element element)
   {
      String id = "";
      
      // null pointer check
      if (element == null)
      {
         return "";
      }
      
      AttributeSet eleAtts = element.getAttributes();
      AttributeSet tagAtts = (AttributeSet)eleAtts.getAttribute(tag);
      if (tagAtts == null)
      {
         return "";
      }
      
      if (tagAtts.isDefined(HTML.Attribute.ID))
      {
         id = (String)tagAtts.getAttribute(HTML.Attribute.ID);
      }
       
      return id;
   }

   /**
    * Helper metod that returns all the character elements spanned
    * by the range startPos to endPos
    * @param startPos start of span   
    * @param endPos end of span
    * @return Collection of all Elements spanned
    */
   protected Collection getSpannedElements(int startPos, int endPos)
   {
      /*
       * Check for endPos larger than document, and if so, reset to
       * document length.  Not doing this causes the loop to never end.
       */
      if (endPos > this.getLength() + 1)
      {
         endPos = this.getLength() + 1;
      }
      
      Collection results = new LinkedList();
      
      Element ele;
      boolean done = false;
      int currPos = startPos;
      while (!done)
      {
         ele = this.getCharacterElement(currPos);
         results.add(ele);
         currPos += (ele.getEndOffset()-currPos);
         if (currPos >= endPos)
         {
            done = true;
         }
      }
      return results;
   }
   
   /**
    * Determine the type of tag associated with this action ID
    * @param id
    * @return
    */
   private HTML.Tag getActionTag(String id)
   {
      Element ele = getElement(id);
      if (ele != null)
      {
         AttributeSet atts = ele.getAttributes();
         Object nameAttr = atts.getAttribute(StyleConstants.NameAttribute);
         if (nameAttr != null && nameAttr.toString().equalsIgnoreCase("img"))
         {
            return HTML.Tag.IMG;
         }
         
         HTML.Tag tag = null;
         AttributeSet testAtts;
         String testId;
         HTML.Tag[] tags = {IvanhoeTag.ADD, IvanhoeTag.DELETE,IvanhoeTag.ILINK};
         for (int i=0;i<tags.length;i++)
         {
            testAtts = (AttributeSet)atts.getAttribute(tags[i]);
            if (testAtts != null)
            {
               testId = (String)testAtts.getAttribute(HTML.Attribute.ID);
               if (testId != null && testId.equals(id))
               {
                  tag = tags[i];
                  break;
               }
            }
         }
         
         return tag;
      }
      return null;
   }
   
   /**
    * helper method to extract content from an action
    * @param ele
    * @return
    */
   public String getActionContent(String actionId, ActionType actionType)
   {  
      // is this a link?
      if ( this.linkMgr.getLink(actionId) != null )
      {
          // grab anchor text from link mgr
          return this.linkMgr.getLinkAnchorText(actionId);
      }
            
      // Get the tag for this action
      HTML.Tag tag = getActionTag(actionId);
      if (tag != null)
      {
         return getTagContent(actionId, tag, actionType);
      }
      return "";
   }
      
   /**
    * @param actionId
    * @param tag
    * @return
    */
   protected String getTagContent(String tagId, HTML.Tag tag, ActionType actionType)
   {
      // special handling needed for image actions...
      // the content is the filename, which is stored as
      // an attribute
      if (tag.equals(HTML.Tag.IMG))
      {
         final Element imgEle = getElement(tagId);
         if (imgEle != null)
         {
            Object file = imgEle.getAttributes().getAttribute(HTML.Attribute.SRC);
            if (file != null)
            {
                if ( ActionType.DELETE_ACTION.equals(actionType) )
                {
                    return " ";
                }
                else
                {
                    return file.toString();
                }
            }
            return "";
         }
      }

      // All others, just extract content from document text
      TagIterator iter = new TagIterator(this, tag, tagId);
      int tagStart = -1;
      int tagEnd = this.getLength();
      while (iter.isValid() ) 
      {
         if (tagStart == -1)
         {
            tagStart = iter.getStartOffset();
         }  
         tagEnd = iter.getEndOffset(); 
         iter.next();
      }
      
      try
      {
         String txt = this.getText(tagStart, tagEnd-tagStart);
         return txt;
      }
      catch (BadLocationException e)
      {
         System.err.println("Unable to get text for element");
      }
         
      return "";
   }
   
   /**
    * Get the starting offset for actionId
    * @param actionId
    * @return
    */
   public int getActionOffset(String actionId)
   {
      int offset = 0;
      
      // see if this action is a link
      if (this.linkMgr.getLink(actionId) != null)
      {
         // get offset of links anchor from link mgr
         offset = this.linkMgr.getLinkOffset(actionId);
      }
      else
      {
         // not a link, just grab the ele offset
         Element ele = getElement(actionId);
         if (ele != null)
         {
            offset = ele.getStartOffset();
         }
      }
      
      return offset;
   }
   
   /**
    * Helper method to check if the passed element is a new tag of
    * the passed type
    * @param element
    * @param markType
    * @return
    */
   protected boolean hasCurrentTag(Element element, IvanhoeTag tag)
   {
      if (tag == null || element == null)
      {
         return false;
      }
      
      if (element.getAttributes().isDefined(tag))
      {
         AttributeSet as = 
            (AttributeSet)element.getAttributes().getAttribute(tag);
         if (as.isDefined("new"))
         {
            return true;
         }
      }
      
      return false;
   }
   
   // find the search string within the content string starting at start position. 
   // if deleted tags aren't visible, ignore them
   private int findString( String content, int startPos, String searchTxt )
   {         
       int pos;
              
       if (areDeleteTagsVisible() == false)
       {          
          pos = content.toLowerCase().indexOf( searchTxt.toLowerCase(), startPos );
          
          if( pos != -1 )
          {              
              SimpleLogger.logInfo("Found text... making sure its not in a hidden delete");
	       
              Element ele = this.getCharacterElement(pos);
	          if (ele.getAttributes().isDefined(IvanhoeTag.DELETE))
	          {
	             SimpleLogger.logInfo("Text found, but it is in a hidden delete; skipping");
	             pos = findString( content, pos+searchTxt.length(), searchTxt );
	          }
          }     
       }
       else
       {
           pos = content.toLowerCase().indexOf( searchTxt.toLowerCase(), startPos );
       }

       return pos;
   }
   
   /**
    * Search the document for the first occurance of searchTxt
    * starting from startPos
    * @param startPos Start position for the search
    * @param searchTxt Text to look for
    * @return the position of the found text or -1 of not found
    */
   public int search(int startPos, String searchTxt, boolean wrap)
   {
      if (startPos < 0)
      {
         SimpleLogger.logInfo("Adjusting search start to 0");
         startPos = 0;        
      }
      
      String content;

      // get document content
      try
      {
         content = getText(0, getLength());
      }
      catch (BadLocationException e)
      {
         StatusEventMgr.fireErrorMsg("Search for '" + searchTxt + "' failed");
         return -1;
      }
      
      // perform the search 
      int pos = findString( content, startPos, searchTxt );         
      
      if (pos == -1 && wrap ) 
      {
	    // wrap and search from start
	    SimpleLogger.logInfo("Not found; wrapping");
	    pos = findString( content, 0, searchTxt );
      }
      
      return pos;
   }
   
   /**
    * 
    * @author lfoster
    *
    * To change the template for this generated type comment go to
    * Window>Preferences>Java>Code Generation>Code and Comments
    */
   public class IvanhoeReader extends HTMLDocument.HTMLReader
   {
      public IvanhoeReader(int offset)
      {
         super(offset, 0, 0, null);
         
         registerIvanhoeTags();
      }

      private void registerIvanhoeTags()
      {
         // add ivanhoe tags to the action map
         this.registerTag(IvanhoeTag.ADD, new CharacterAction() );
         this.registerTag(IvanhoeTag.DELETE, new CharacterAction() );
         this.registerTag(IvanhoeTag.ILINK, new CharacterAction() );
      }

      /**
       * handleSimpleTag is called by the parser when an unknown tag is
       * encountered. Determine if it is an Ivanhoe tag and handle it
       * appropriately
       */
      public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos)
      {
         if ( IvanhoeTag.isSupported(t) )
         { 
            if (a.isDefined(HTML.Attribute.ENDTAG))
            {
               this.handleEndTag(t,pos);
            }
            else
            {            
               this.handleStartTag(t,a,pos);
            }
         }
         else
         {
            super.handleSimpleTag(t, a, pos);
         }
      }
   }
   
   /**
    * Class to iterate over all tags with a given ID
    */
   private static class TagIterator 
   {
      private HTML.Tag tag;
      private ElementIterator pos;
      private String targetId;

      TagIterator(Document doc, HTML.Tag t, String id) 
      {
         this.tag = t;
         this.pos = new ElementIterator(doc);
         this.targetId  = id;
         next();
      }
      
      public int getStartOffset()
      {
         Element elem = pos.current();
         if (elem != null) 
         {
            return elem.getStartOffset();
         }
         return 0;   
      }
      
      public int getEndOffset()
      {
         Element elem = pos.current();
         if (elem != null) 
         {
            return elem.getEndOffset();
         }
         return 0;   
      }
      
      public int getLength()
      {
         Element elem = pos.current();
         if (elem != null) 
         {
            return elem.getEndOffset() - elem.getStartOffset();
         }
         return 0;   
      }

      public AttributeSet getAttributes() 
      {
         Element elem = pos.current();
         if (elem != null) 
         {
            return elem.getAttributes();
         }
         return null;
      }
      
      public boolean isCurrent()
      {
         AttributeSet tagAtts = 
            (AttributeSet)getAttributes().getAttribute(tag);
         if (tagAtts != null)
         {
            return tagAtts.isDefined("new");
         }
         
         return false;
      }

      public void next() 
      {
         for (pos.next(); isValid(); pos.next()) 
         {
            AttributeSet attribs = getAttributes();
            AttributeSet tagAtts = 
               (AttributeSet)attribs.getAttribute(tag);
            if (tagAtts != null)
            {
               String testId = (String)tagAtts.getAttribute(
                  HTML.Attribute.ID);
               if ((testId != null) && testId.equals(this.targetId)) 
               {
                  break;
               }
            }
         }
      }

      public boolean isValid() 
      {
         return (pos.current() != null);
      }
   }
}