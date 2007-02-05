/*
 * Created on Feb 18, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTimeline;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.ITimeLineListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.shared.blist.*;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.Move;

/**
 * @author benc
 */
public class DocumentHistoryTreeModel extends DefaultTreeModel implements
        ITimeLineListener
{
    private DiscourseField discourseField;
    private DefaultMutableTreeNode docListRootNode;
    private HashMap docNodeMap;

    /**
     * @param root
     */
    private DocumentHistoryTreeModel(DefaultMutableTreeNode docListRootNode, 
            DiscourseField discourseField )
    {
        super(docListRootNode);

        this.discourseField = discourseField;
        this.docListRootNode = docListRootNode;
        this.docNodeMap = new HashMap();

        DiscourseFieldTimeline dfTimeline = discourseField.getDiscourseFieldTimeline();
        LinkedList moveList = dfTimeline.getTimeline();

        for (Iterator i = moveList.iterator(); i.hasNext();)
        {
            MoveEvent event = (MoveEvent) i.next();
            addMoveToHistoryDisplay(event.getMove());
        }

        dfTimeline.addListener(this);
    }

    public static DocumentHistoryTreeModel createDocumentHistoryTreeModel( DiscourseField discourseField )
    {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(
                "Documents");
        return new DocumentHistoryTreeModel(treeNode, discourseField);
    }

    /**
     * Add move to the history of the appropriate player
     * 
     * @param move
     */
    private void addMoveToHistoryDisplay(Move move)
    {
        if (move == null) return;

        BCollection documentVersions = discourseField.getDocumentVersionManager().getDocumentVersions(move);
        for (Iterator i=documentVersions.iterator(); i.hasNext(); )
        {
            DocumentVersion docVersion = (DocumentVersion)i.next();
            DocumentNode documentNode;
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
            
            BCollection existingDocVersions = BCollectionAlgorithms.list(documentNode.children())
                    .inplaceTransform(new BCollectionTransform() {
                            public Object transform(Object o)
                            {
                                return ((DocumentVersionNode) o).getDocumentVersion();
                            }
                    });
            
            if (!existingDocVersions.contains(docVersion) )
            {
                DocumentVersionNode docVersionNode = new DocumentVersionNode(discourseField,docVersion,false);
                documentNode.add(docVersionNode);
            }
        }
        
        reload();
    }

    public void moveAddedToHistory(MoveEvent event)
    {
        addMoveToHistoryDisplay(event.getMove());
    }

    public void addNewDocument(DocumentInfo docInfo)
    {
    }

    public void removeDocument(DocumentInfo docInfo)
    {
    }
}
