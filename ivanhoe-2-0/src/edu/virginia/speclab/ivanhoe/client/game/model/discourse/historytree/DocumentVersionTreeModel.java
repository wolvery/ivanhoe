/*
 * Created on Jun 23, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.ITimeLineListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.shared.blist.BArrayList;
import edu.virginia.speclab.ivanhoe.shared.blist.BCollection;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.Move;

/**
 * @author benc
 */
public class DocumentVersionTreeModel extends DefaultTreeModel implements
        ITimeLineListener
{
    private final DiscourseField discourseField;
    private final DefaultMutableTreeNode docListRootNode;
    private final BArrayList documentVersions;
    private final Map docVersionNodeMap;
    private final Map docNodeMap;
    
    private DocumentVersionTreeModel(DefaultMutableTreeNode docListRootNode, DiscourseField discourseField)
    {
        super(docListRootNode);
        this.discourseField = discourseField;
        this.docListRootNode = docListRootNode;
        this.documentVersions = new BArrayList();
        this.docVersionNodeMap = new HashMap();
        this.docNodeMap = new HashMap();
        
        discourseField.getDiscourseFieldTimeline().addListener(this);
        List moveList = discourseField.getDiscourseFieldTimeline().getTimeline();

        for (Iterator i = moveList.iterator(); i.hasNext();)
        {
            MoveEvent event = (MoveEvent) i.next();
            addMoveToHistoryDisplay(event.getMove());
        }
    }
    
    private void addMoveToHistoryDisplay(Move move)
    {
        if (move == null) return;

        BCollection allDocumentVersions = discourseField.getDocumentVersionManager().getDocumentVersions(move);
        for (Iterator i=allDocumentVersions.iterator(); i.hasNext(); )
        {
            DocumentVersion docVersion = (DocumentVersion)i.next();
            
            if (!documentVersions.contains(docVersion) )
            {
                DocumentNode documentNode;
                Integer docVersionID = new Integer(docVersion.getID());
                Integer docVersionParentID = new Integer(docVersion.getParentID());
                DocumentVersionNode docVersionNode = new DocumentVersionNode(discourseField,docVersion,true);
                DocumentVersionNode parent;
                
                if ( docNodeMap.containsKey(docVersion.getDocumentTitle()) ) 
                {
                    documentNode = (DocumentNode) docNodeMap.get(docVersion.getDocumentTitle());
                }
                else
                {
                    documentNode = new DocumentNode(docVersion.getDocumentTitle(), true);
                    docNodeMap.put(docVersion.getDocumentTitle(), documentNode);
                    docListRootNode.add(documentNode);
                }
                
                if (docVersionNodeMap.containsKey(docVersionParentID))
                {
                    parent = (DocumentVersionNode)docVersionNodeMap.get(docVersionParentID);
                    parent.add(docVersionNode);
                }
                else
                {
                    // This could also happen if the moves are passed in the wrong order
                    documentNode.add(docVersionNode);
                }
                
                documentVersions.add(docVersion);
                docVersionNodeMap.put(docVersionID, docVersionNode);
            }
        }
        
        reload();
    }

    public void moveAddedToHistory(MoveEvent event)
    {
        addMoveToHistoryDisplay(event.getMove());
    }

    public void addNewDocument(DocumentInfo docInfo) {}

    public void removeDocument(DocumentInfo docInfo) {}
    
    public static DocumentVersionTreeModel createDocumentVersionTreeModel( DiscourseField discourseField )
    {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(
                "Documents");
        return new DocumentVersionTreeModel(treeNode, discourseField);
    }
}
