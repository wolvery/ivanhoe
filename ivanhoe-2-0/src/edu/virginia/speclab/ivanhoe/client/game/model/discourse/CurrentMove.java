/*
 * Created on Dec 8, 2003
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse;

import java.util.*;

import javax.swing.text.html.HTML.Tag;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.document.*;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.MoveListItem;
import edu.virginia.speclab.ivanhoe.client.game.model.util.StatusEventMgr;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.shared.GuidGenerator;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.blist.BArrayList;
import edu.virginia.speclab.ivanhoe.shared.blist.BList;
import edu.virginia.speclab.ivanhoe.shared.data.*;
import edu.virginia.speclab.ivanhoe.shared.message.CancelMoveMsg;
import edu.virginia.speclab.ivanhoe.shared.message.DocumentErrorMsg;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.MoveResponseMsg;
import edu.virginia.speclab.ivanhoe.shared.message.MoveSubmitMsg;
import edu.virginia.speclab.ivanhoe.shared.message.SaveMsg;

/**
 * @author lfoster
 */
public class CurrentMove extends Move implements IDocumentActionListener,
        IMessageHandler
{
    private BList cachedActions;
    private List listeners;
    private final DiscourseField discourseField;
    private final DocumentVersionManager dvManager;
    private Role currentRole;

    public CurrentMove(DiscourseField discourseField)
    {
        super();

        this.cachedActions = new BArrayList();
        this.listeners = new LinkedList();

        this.discourseField = discourseField;
        this.dvManager = discourseField.getDocumentVersionManager();

        // register message handlers
        Ivanhoe.registerGameMsgHandler(MessageType.MOVE_RESPONSE, this);
        Ivanhoe.registerGameMsgHandler(MessageType.SAVE_RESPONSE, this);
        Ivanhoe.registerGameMsgHandler(MessageType.DOCUMENT_ERROR, this);
    }

    /**
     * Set the move summary
     * 
     * @param string
     */
    public void setDescription(String txt)
    {
        this.description = txt;
    }

    public void setCategory(int category)
    {
        this.category = category;
    }

    public void setInpiration(List inspirationList)
    {
        this.inspirations = inspirationList;
    }

    /**
     * Resets the current move; Removes all actions Resets the move start &
     * submit dates Clears the description
     */
    public void reset()
    {
        SimpleLogger.logInfo("Reseting current move");
        this.id = -1;
        this.description = "";
        this.startDate = null;
        this.submissionDate = null;
        this.cachedActions.clear();
        this.actions.clear();

        fireCurrentMoveChanged(null);
    }

    /**
     * Check if the given ID is a current action
     * 
     * @param string
     * @return
     */
    public boolean containsAction(String actId)
    {
        // first check superclass
        boolean found = super.containsAction(actId);

        // if its not there, check the cached actions
        if (found == false)
        {
            Iterator itr = this.cachedActions.iterator();
            while (itr.hasNext())
            {
                if (((IvanhoeAction) itr.next()).getId().equals(actId))
                {
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    /**
     * Search the current move for the action which added this document and
     * remove it if it exists. All removes any actions pertaining to this
     * document.
     * 
     * @param docName
     *           The document to remove from the current move.
     */
    public void removeDocument(String docName)
    {
        // go through both the actions that were added this session and actions
        // that were restored from a previous session.

        Iterator itr = this.actions.iterator();
        while (itr.hasNext())
        {   
            CurrentAction action = (CurrentAction) itr.next();
            DocumentVersion docVersion = dvManager.getDocumentVersion(action);
            
            if (action.getType().equals(ActionType.ADD_DOC_ACTION) == true
                    && docVersion.getDocumentTitle().equals(docName) == true)
            {
                removeAction(action.getId());
                return;
            }
        }

        Iterator itr2 = this.cachedActions.iterator();
        while (itr2.hasNext())
        {
            IvanhoeAction action = (IvanhoeAction) itr2.next();
            DocumentVersion docVersion = dvManager.getDocumentVersion(action);
            
            if (action.getType().equals(ActionType.ADD_DOC_ACTION) == true
                    && docVersion.getDocumentTitle().equals(docName) == true)
            {
                removeAction(action.getId());
                return;
            }
        }

    }

    /**
     * Remove the action identified by actionId from this move Also removes
     * associated tags from document
     * 
     * @param actionId
     *           The ID of the action to remove
     * @return true if successfully removed; false otherwise
     */
    public boolean removeAction(String actionId)
    {
        SimpleLogger.logInfo("Removing action [" + actionId + "]");

        IvanhoeAction act = getAction(actionId);
        if (act != null)
        {
            if (act.getType().equals(ActionType.ADD_DOC_ACTION))
            {
                final DocumentInfo docInfo = (DocumentInfo) act.getContent();
                final String title = docInfo.getTitle();
                // remove all actions in the added document
                removeAllActionsInDocument(title);
                discourseField.deleteDocument(title);
                Workspace.instance.closeDocumentWindows(title);
            }

            // remove the action
            if (act instanceof CurrentAction)
            {
                this.actions.remove(act);
                SimpleLogger.logInfo("Removed from current actions list");
            }
            else
            {
                this.cachedActions.remove(act);
                SimpleLogger.logInfo("Removed from cached actions list");
            }

            // if the action just removed was link, blow away
            // any related link targets
            if (act.getType().equals(ActionType.LINK_ACTION))
            {
                removeRelatedLinks(act);
            }

            // Update the document if it is currently open
            final DocumentVersion docVersion = dvManager.getDocumentVersion(act);
            if (docVersion != null)
            {
                docVersion.removeActionID(actionId);
            }
            
            final IvanhoeDocument doc = discourseField.getOpenDocument(docVersion);
            if (doc != null)
            {
                SimpleLogger.logInfo("Removing tag from open doc instance");
                
                if (ActionType.ADD_ACTION.equals(act.getType()) )
                {
                    removeMatchingLinks(act, doc);
                }
                
                doc.removeActionTag(act);
            }

            // if all actions are now gone, notify server so DB can be updated
            if (this.getActionCount() == 0)
            {
                // notify server that any pending data for this player should be
                // removed
                Ivanhoe.getProxy().sendMessage(new CancelMoveMsg(this.roleID));
            }

            return true;
        }

        return false;
    }

    /**
     * Remove all rekated links for 2-way internal links
     * 
     * @param act
     */
    private void removeRelatedLinks(IvanhoeAction act)
    {
        Link link = (Link) act.getContent();
        if (link.getType().equals(LinkType.INTERNAL))
        {
            String linkId = link.getLinkTag().getBackLinkID();
            if (containsAction(linkId))
            {
                SimpleLogger.logInfo("Removing related link " + linkId);
                removeAction(linkId);
            }
        }
    }

    /**
     * @param act
     */
    private void removeMatchingLinks(IvanhoeAction act, IvanhoeDocument doc)
    {
        Vector removals = new Vector();
        Iterator itr = this.actions.iterator();

        // find all links that exactly match the span of the action
        // and add them to a remove list
        final int actStart = act.getOffset();
        final int actEnd = actStart + act.getLength();
        SimpleLogger.logInfo("Comparing act ["+act.getType()+", "+
                actStart+"-"+actEnd+"]");
        
        while (itr.hasNext())
        {
            final IvanhoeAction testAct = (IvanhoeAction) itr.next();
            if (testAct.getType().equals(ActionType.LINK_ACTION))
            {
                final int testActStart = testAct.getOffset();
                final int testActEnd = testActStart + 
                    doc.getLinkManager().getLinkAnchorText(testAct.getId()).length();
                
                SimpleLogger.logInfo("    to ["+testAct.getType()+", "+
                        testActStart+"-"+testActEnd+"]");
                if (testActStart >= actStart &&
                        testActEnd <= actEnd)
                {
                    SimpleLogger.logInfo("        Matched.");
                    removals.add(testAct.getId());
                }
            }
        }

        // drop all the links
        itr = removals.iterator();
        while (itr.hasNext())
        {
            removeAction((String) itr.next());
        }
    }

    /**
     * Notification that a document has been added to the discourse field.
     * 
     * @param fileName
     *           The name of the file to add
     * @param path
     *           the full path (including filename) to the new file
     */
    public void documentAdded(DocumentInfo docInfo)
    {
        SimpleLogger.logInfo("Document " + docInfo.toString() + " added");

        // if this move has not been started, start it now
        if (isStarted() == false)
        {
            startMove();
        }

        DocumentVersion docVersion = dvManager.createNewDocumentVersion(docInfo);
        
        CurrentAction act = new CurrentAction(this.discourseField, docVersion.getID(), GuidGenerator
              .generateID(), ActionType.ADD_DOC_ACTION, docInfo.getCreateTime());
        

        this.actions.add(act);
        fireCurrentMoveChanged(act);
    }

    /**
     * Start a new move. This is called when the first action is added to a move
     */
    private void startMove()
    {
        if (currentRole != null)
        {
            SimpleLogger.logInfo("A new move has been started");
            this.startDate = Ivanhoe.getDate();
            this.roleName = currentRole.getName();
            this.roleID = currentRole.getId();
        }
        else
        {
            SimpleLogger
                    .logError("Error starting new move, must setCurrentRole() first!");
        }
    }

    public void setCurrentRole(Role role)
    {
        currentRole = role;
    }

    /**
     * notification that an ivanhoe action tag has been added to a doc
     */
    public void actionAdded(DocumentVersion version, String newId, Tag tag)
    {
        SimpleLogger.logInfo("Action [" + tag.toString() + ", " + newId
                + "] added to " + version.toString());

        // if this move has not been started, start it now
        if (isStarted() == false)
        {
            startMove();
        }

        // construct an action for this move
        CurrentAction act = new CurrentAction(this.discourseField, version.getID(),
                newId, IvanhoeTag.getActionTypeForTag(tag), Ivanhoe.getDate());

        // add the action to the list
        for (Iterator itr = this.actions.iterator(); itr.hasNext();)
        {
            CurrentAction testAct = (CurrentAction) itr.next();
            if (testAct.getId().equals(act.getId()))
            {
                SimpleLogger
                        .logInfo("Updating existing action with the same ID");
                this.actions.remove(testAct);
                break;
            }
        }
        this.actions.add(act);
        fireCurrentMoveChanged(act);
    }

    /**
     * notification that a ivanhoe action tag has been deleted from a doc
     */
    public void actionDeleted(String actionId)
    {
        SimpleLogger.logInfo("Action [" + actionId + "] has been deleted");

        IvanhoeAction act = getAction(actionId);
        if (act != null)
        {
            // this method is fired from an open document, so the
            // action being removed MUST be a current action and
            // is contained in the current actions list
            this.actions.remove(act);
        }

        fireCurrentMoveChanged(act);
    }

    /**
     * Removes all actions on a given document
     * 
     * @param string
     */
    public void removeAllActionsInDocument(String docTitle)
    {
        SimpleLogger.logInfo("Removing all actions in document " + docTitle);

        // build a list of actions to remove
        Iterator itr = this.actions.iterator();
        IvanhoeAction act;
        Vector removeList = new Vector();
        while (itr.hasNext())
        {
            act = (IvanhoeAction) itr.next();
            DocumentVersion docVersion = dvManager.getDocumentVersion(act);
            
            if (docVersion.getDocumentTitle().equalsIgnoreCase(docTitle))
            {
                if (act.getType().equals(ActionType.ADD_DOC_ACTION) == false)
                {
                    removeList.add(act.getId());
                }
            }
        }

        // iterate over collection and remove all
        itr = removeList.iterator();
        while (itr.hasNext())
        {
            this.removeAction((String) itr.next());
        }
    }

    /**
     * Remove all actions in the move. This includes cached actions
     */
    public void removeAllActions()
    {
        // generate a list of IDs to be removed based on the
        // action and cachedAction lists
        List removeIds = new ArrayList();
        Iterator itr = this.actions.iterator();
        while (itr.hasNext())
        {
            removeIds.add(((CurrentAction) itr.next()).getId());
        }
        itr = this.cachedActions.iterator();
        while (itr.hasNext())
        {
            removeIds.add(((IvanhoeAction) itr.next()).getId());
        }

        // remove all the ids in the list
        itr = removeIds.iterator();
        while (itr.hasNext())
        {
            removeAction((String) itr.next());
        }

        // clear lists
        this.actions.clear();
        this.cachedActions.clear();

        fireCurrentMoveChanged(null);
    }

    /**
     * Notification that <code>document</code> has been closed. All active
     * action in that doc are moved from the active acts list to a cached list.
     * 
     * @param document
     */
    public void documentClosing(IvanhoeDocument document)
    {
        SimpleLogger
                .logInfo("CurrentMove is caching all active actions for closing document "
                        + document.getVersion().toString());

        // find all actions in the given document
        DocumentVersion version = document.getVersion();
        List convertedList = new ArrayList();
        CurrentAction act;
        Iterator itr = getActions();
        while (itr.hasNext())
        {
            act = (CurrentAction) itr.next();
            if (act.getDocumentVersionID() == version.getID())
            {
                // move this act from active (stubbed)
                // to cached (full-fledged)
                IvanhoeAction fullAct = convertAction(act, document);
                if (fullAct != null)
                {
                    convertedList.add(act);
                    this.cachedActions.add(fullAct);
                }
            }
        }

        // purge all acts that were converted
        itr = convertedList.iterator();
        while (itr.hasNext())
        {
            this.actions.remove(itr.next());
        }
    }

    /**
     * Save the current move
     * 
     * @return
     */
    public boolean save()
    {
        if (isStarted() && getActionCount() > 0 && getSubmissionDate() == null)
        {
            // convert stubbed actions to full acts
            List saveList = generateActionList();

            // add any restored actions
            if (this.cachedActions.size() > 0)
            {
                saveList.addAll(this.cachedActions);
            }

            // copy this move into a read-only move instance
            Move saveMove = new Move(getId(), getRoleID(), getRoleName(),
                    getStartDate(), saveList, getCategory(), getInspirations() );

            // get the document version mappings
            Map dvMappings = dvManager.getCurrentDocumentVersionOrigins(this);
            
            // Send it to the server
            SaveMsg saveMsg = new SaveMsg(saveMove, dvMappings);
            return Ivanhoe.getProxy().sendMessage(saveMsg);
        }
        return true;
    }

    /**
     * Submits the current move to the server
     * 
     * @return
     */
    public boolean submit()
    {
        boolean cleanSubmit = true;

        if (isStarted() == false || getActionCount() == 0)
        {
            SimpleLogger
                    .logError("Failed attempt to publish an unstarted or empty move");
            return false;
        }

        // convert stubbed actions to full acts
        List submitList = generateActionList();

        // add any restored actions
        if (this.cachedActions.size() > 0)
        {
            submitList.addAll(this.cachedActions);
        }

        if (submitList.size() == 0)
        {
            Ivanhoe
                    .sendErrorMessageToHost("No actions to submit, aborting submit.");
            return false;
        }

        // stamp date of submission
        this.submissionDate = Ivanhoe.getDate();

        // copy this move into a read-only move instance
        // and send it to the server
        Move submitMove = new Move(getId(), getRoleID(), getRoleName(),
                getStartDate(), getSubmissionDate(), getDescription(),
                submitList, getCategory(), getInspirations() );
        Map dvMappings = dvManager.getCurrentDocumentVersionOrigins(this);
        MoveSubmitMsg msg = new MoveSubmitMsg(submitMove, dvMappings);

        if (!Ivanhoe.getProxy().sendMessage(msg))
        {
            Ivanhoe
                    .sendErrorMessageToHost("Error sending move message to host.");
            cleanSubmit = false;
        }

        return cleanSubmit;
    }

    /**
     * Iterate over the stubbed currentAction list and generate full-fledged
     * actions that will be sent to the server for persistance
     * 
     * @return List of actions
     */
    private List generateActionList()
    {
        List acts = new ArrayList();
        Iterator actItr = this.actions.iterator();
        IvanhoeAction fullAct;

        while (actItr.hasNext())
        {
            CurrentAction currAct = (CurrentAction) actItr.next();
            fullAct = convertAction(currAct);
            if (fullAct != null)
            {
                acts.add(fullAct);
            }
        }

        return acts;
    }

    /**
     * Convert CurrentAction to full-fledged IvanhoeAction
     * 
     * @param currAct
     * @return
     */
    private IvanhoeAction convertAction(CurrentAction currAct)
    {
        IvanhoeDocument doc = discourseField.getOpenDocument(currAct.getDocumentVersion());
        return convertAction(currAct, doc);
    }
    
    private IvanhoeAction convertAction(CurrentAction currAct, IvanhoeDocument doc)
    {
        IvanhoeAction fullAct = null;
        Object content = null;

        if (currAct.getType().equals(ActionType.LINK_ACTION))
        {
            if (doc == null)
            {
                // This shouldn't happen, but if it doesn, handle it gracefully.
                SimpleLogger.logInfo("convertAction called on link action with null document");
                return null;
            }
            
            Link link = doc.getLinkManager().getLink(currAct.getId());

            boolean validLinkRecord = doc.getLinkManager().linkRecForActionID(
                    currAct.getId());
            if (link == null)
            {
                String validString = validLinkRecord ? "valid" : "invalid";
                Ivanhoe.sendErrorMessageToHost("Unable to lookup link id: "
                        + currAct.getId() + " link record is " + validString
                        + ".");
                return null;
            }

            if (validLinkRecord == false)
            {
                Ivanhoe
                        .sendErrorMessageToHost("Link record is invalid for link id:"
                                + currAct.getId());
                return null;
            }

            final String anchorText =
                doc.getLinkManager().getLinkAnchorText(currAct.getId());

            link.setAnchorText(anchorText);
            content = link;
        }
        else
        {
            content = currAct.getContent();
        }

        fullAct = new IvanhoeAction(currAct.getType(), 
                currAct.getDocumentVersionID(), this.roleName, this.roleID, currAct.getId(),
                currAct.getOffset(), content, currAct.getDate());

        final DocumentVersion docVersion =
        	discourseField.getDocumentVersionManager().getDocumentVersion(fullAct.getDocumentVersionID());
        docVersion.addActionID(fullAct.getId());
        return fullAct;
    }

    /**
     * Get a count of all actions in the current move. This includes both active
     * and cached actions
     */
    public int getActionCount()
    {
        return this.actions.size() + this.cachedActions.size();
    }

    /**
     * Get the action with the specified ID
     */
    public IvanhoeAction getAction(String actionId)
    {
        // try the base-class get method
        IvanhoeAction act = super.getAction(actionId);

        // if an ation wasnt found, search the cached actions
        if (act == null)
        {
            IvanhoeAction testAct;
            Iterator itr = this.cachedActions.iterator();
            while (itr.hasNext())
            {
                testAct = (IvanhoeAction) itr.next();
                if (testAct.getId().equals(actionId))
                {
                    act = testAct;
                    break;
                }
            }
        }

        return act;
    }

    /**
     * handle messages from the server. Current move listens for move submission
     * responses
     */
    public void handleMessage(Message msg)
    {
        if (msg.getType().equals(MessageType.MOVE_RESPONSE))
        {
            handleSubmitResponse((MoveResponseMsg) msg);
        }
        else if (msg.getType().equals(MessageType.DOCUMENT_ERROR))
        {
            handleDocumentError((DocumentErrorMsg) msg);
        }
    }

    /**
     * Handle nbotification that am attempt to add a document failed
     * 
     * @param msg
     */
    private void handleDocumentError(DocumentErrorMsg msg)
    {
        // fire noty and remove action that added the document
        StatusEventMgr.fireErrorMsg(msg.getErrorMessage());
        for (Iterator itr = this.actions.iterator(); itr.hasNext();)
        {
            IvanhoeAction act = (IvanhoeAction) itr.next();
            DocumentVersion docVersion = dvManager.getDocumentVersion(act);
            
            if (act.getType().equals(ActionType.ADD_DOC_ACTION)
                    && docVersion.getDocumentTitle().equals(
                            msg.getDocumentInfo().getTitle()))
            {
                SimpleLogger
                        .logInfo("removing action that attempted to add bad document"
                                + msg.getDocumentInfo().getTitle());
                removeAction(act.getId());
                break;
            }
        }
    }

    /**
     * Copy all restored data into current move structure.
     * 
     * @param restMsg
     */
    public void restoreMove(Move savedState)
    {
        // populate move data
        this.id = savedState.getId();
        this.roleID = savedState.getRoleID();
        this.roleName = savedState.getRoleName();
        this.description = savedState.getDescription();
        this.startDate = savedState.getStartDate();
        this.submissionDate = savedState.getSubmissionDate();

        // copy all actions locally
        Iterator itr = savedState.getActions();

        while (itr.hasNext())
        {
            this.cachedActions.add(itr.next());
        }

        fireCurrentMoveChanged(null);
    }

    /**
     * Converts cached actions for a given document version into active actions.
     * A list of converted actions is returned
     * 
     * @return
     */
    public List activateCachedActions(DocumentVersion docVersion)
    {
        Vector result = new Vector();

        for (Iterator itr = this.cachedActions.iterator(); itr.hasNext();)
        {
            IvanhoeAction act;
            act = (IvanhoeAction) itr.next();
            DocumentVersion actionDocVersion = dvManager.getDocumentVersion(act);
            
            if (actionDocVersion.equals(docVersion))
            {
                result.add(act);

                // move act from cache to active
                CurrentAction currAct = new CurrentAction(this.discourseField,
                        actionDocVersion.getID(), act.getId(), act.getType(),
                        act.getDate());
                this.actions.add(currAct);
            }
        }

        // purge acts that are now active
        for (Iterator itr = result.iterator(); itr.hasNext();)
        {
            this.cachedActions.remove(itr.next());
        }

        return result;
    }

    /**
     * Determine if the specified document was effected by an action during the
     * current move.
     * 
     * @param doc
     *           The document to test.
     * @return true if the document was effected, false if not.
     */
    public boolean currentMovedTouched(IvanhoeDocument doc)
    {
        for (Iterator i = this.actions.iterator(); i.hasNext();)
        {
            IvanhoeAction act = (IvanhoeAction) i.next();
            DocumentVersion docVersion = dvManager.getDocumentVersion(act);
            
            if (docVersion.getDocumentTitle().equals(doc.getTitle()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Handle message containing move submisson results. If successful, mark all
     * the action tags as submitted
     * 
     * @param msg
     */
    private void handleSubmitResponse(MoveResponseMsg msg)
    {
        if (msg.isSuccess())
        {
            // once a move is successful, close all open windows
            Workspace.instance.closeAllWindows();
            this.reset();

            SimpleLogger.logInfo("Move submit successful, resetting current move");
        }
    }

    /**
     * Generate a summary report
     * 
     * @return HTML formatted report
     */
    public String generateSummaryReport()
    {
        return generateSummaryReport(null, null);
    }

    /**
     * Generate a summary report
     * 
     * @param inspirationList
     *           A list of moves that have inspired the current move
     * @return HTML formatted report
     */
    public String generateSummaryReport(List list)
    {
        return generateSummaryReport(null, list);
    }

    /**
     * Generate a summary report
     * 
     * @param category
     *           Metadata category for the move
     * @param inspirationList
     *           A list of moves that have inspired the current move
     * @return HTML formatted report
     */
    public String generateSummaryReport(Category category, List inspirationList)
    {
        StringBuffer txt = new StringBuffer();

        if (startDate != null)
        {
            txt.append("<html><body style=font-size:12><b>Start Date: </b>")
                    .append(IvanhoeDateFormat.format(getStartDate()));
            txt.append("<br>");
        }

        if (getSubmissionDate() != null)
        {
            txt.append("<b>Publish Date: </b>").append(
                    IvanhoeDateFormat.format(getSubmissionDate())).append(
                    "<br>");
        }

        // add sub-nodes for each action the the move
        IvanhoeAction act;

        Vector allActs = new Vector();
        allActs.addAll(this.actions);
        allActs.addAll(this.cachedActions);
        Iterator itr = allActs.iterator();

        if (itr.hasNext())
        {
            txt.append("<br><hr><b>Actions</b><hr><ol>");
        }

        while (itr.hasNext())
        {
            act = (IvanhoeAction) itr.next();
            DocumentVersion docVersion = dvManager.getDocumentVersion(act);
            txt.append("<li>").append(act.toHtml(docVersion)).append("</li>");
        }

        // list of metadata categories for the move TODO
        if (category != null)
        {
            txt.append("<li>").append("designated move as <b>").append(
                    category.getName()).append("</b></li>");
        }

        // list of moves that inspired the current move
        if (inspirationList != null)
        {
            for (Iterator i = inspirationList.iterator(); i.hasNext();)
            {
                MoveListItem moveItem = (MoveListItem) i.next();
                txt.append("<li>").append("move inspired by ").append(
                        moveItem.toString()).append("</li>");
            }

        }

        return txt.toString();
    }

    /**
     * @return
     */
    public boolean isStarted()
    {
        return (super.isStarted() && this.getActionCount() > 0);
    }

    public void addListener(ICurrentMoveListener listener)
    {
        listeners.add(listener);
    }
    
    public boolean removeListener(ICurrentMoveListener listener)
    {
        return listeners.remove(listener);
    }

    private void fireCurrentMoveChanged(IvanhoeAction action)
    {
        for (Iterator i = listeners.iterator(); i.hasNext();)
        {
            ICurrentMoveListener listener = (ICurrentMoveListener) (i.next());
            listener.currentMoveChanged(this, action);
        }
    }

    /**
     * @return Returns the currentRole.
     */
    public Role getCurrentRole()
    {
        return currentRole;
    }
}