/*
 * Created on Jun 20, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree;

import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DocumentVersionManager;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.blist.*;
import edu.virginia.speclab.ivanhoe.shared.blist.lambda.ActionTypeFilter;
import edu.virginia.speclab.ivanhoe.shared.blist.lambda.CastableFilter;
import edu.virginia.speclab.ivanhoe.shared.blist.lambda.ChainFilter;
import edu.virginia.speclab.ivanhoe.shared.data.ActionType;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;

/**
 * @author benc
 */
public class DocumentVersionNode extends DocumentStateNode
{
    private final DocumentVersion documentVersion;
    private final DocumentVersionManager dvManager;
    
    /**
     * ctor
     * 
     * @param discourseField
     * @param documentVersion
     *      DocumentVersion this node represents
     * @param showVersions
     *      Display the number of versions represented by this node
     */
    public DocumentVersionNode(
            DiscourseField discourseField,
            DocumentVersion documentVersion, 
            boolean showVersions)
    {
        super(getDocumentVersionLabel(documentVersion), showVersions, true);
        this.documentVersion = documentVersion;
        this.dvManager = discourseField.getDocumentVersionManager();
        generateChildren(discourseField);
    }

    private void generateChildren( DiscourseField discourseField )
    {
        BCollectionFilter marksFilter = new ActionTypeFilter(ActionType.LINK_ACTION);
        BCollectionFilter docAddsFilter = new ActionTypeFilter(ActionType.ADD_DOC_ACTION);
        BCollectionFilter editsFilter = new ChainFilter()
                .addFilter(BCollectionAlgorithms.invertFilter(docAddsFilter))
                .addFilter(BCollectionAlgorithms.invertFilter(marksFilter));
        
        
                // Now we do the same thing three times over
        
        // First, we do document adds
        BCollection actionTags = documentVersion.getActionIDs();
        BCollection actions = discourseField.convertTagsToActions(actionTags);
        BCollection validActions = actions.filter(new CastableFilter(IvanhoeAction.class));
        
        if (actions.size() != validActions.size()) {
            SimpleLogger.logError("action tags "+actionTags+
                    " could not all be converted into actions.");
        }
        
        BCollection docAddActions = validActions.filter(docAddsFilter); 
        if (!docAddActions.isEmpty())
        {
            DefaultMutableTreeNode docAddsNode = new ActionGroupNode("document adds");
            
            for (Iterator i=docAddActions.iterator(); i.hasNext(); )
            {
                IvanhoeAction act = (IvanhoeAction) i.next();
                docAddsNode.add(new ActionNode(act.toHtml(dvManager.getDocumentVersion(act)), act));
            }
            this.add(docAddsNode);
        }
        
        // Second, we do edits
        BCollection editActions = actions.filter(editsFilter);
        if (!editActions.isEmpty())
        {
            DefaultMutableTreeNode editsNode = new ActionGroupNode("edit actions");
    
            for (Iterator i=editActions.iterator(); i.hasNext(); )
            {
                IvanhoeAction act = (IvanhoeAction) i.next();
                editsNode.add(new ActionNode(act.toHtml(dvManager.getDocumentVersion(act)), act));
            }
            
            this.add(editsNode);
        }
        
        // Last, we do links
        BCollection markActions = actions.filter(marksFilter);
        if (!markActions.isEmpty())
        {
            DefaultMutableTreeNode marksNode = new ActionGroupNode("markings");
    
            
            for (Iterator i=markActions.iterator(); i.hasNext(); )
            {
                IvanhoeAction act = (IvanhoeAction) i.next();
                marksNode.add(new ActionNode(act.toHtml(dvManager.getDocumentVersion(act)), act));
            }
            
            this.add(marksNode);
        }
    }
    
    public DocumentVersion getDocumentVersion()
    {
        return documentVersion;
    }
    
    public int getNumStates()
    {
        Object numStates =
                BCollectionAlgorithms.list(this.children())
                .reduce(new DocumentStateNode.NumberOfStatesReducer());
        
        return 1 + ((Integer) numStates).intValue();
    }
    
    public static String getDocumentVersionLabel(DocumentVersion documentVersion)
    {
        boolean debug = Boolean.valueOf(Workspace.instance.getPropertiesManager().getProperty("debug")).booleanValue();
        String idString = (debug ? "(ID="+documentVersion.getID()+", parent="+documentVersion.getParentID()+") " : "");
        
        return  idString+"<b>"+documentVersion.getRoleName()+"</b>'s version from "
                +documentVersion.getDate();
    }
    
    private static class ActionGroupNode extends DefaultMutableTreeNode
    {
        public ActionGroupNode(String label)
        {
            super(label);
        }
        
        public String toString()
        {
            return "<html><body><b>"+this.getUserObject()+"</b></body></html>";
        }
    }
}
