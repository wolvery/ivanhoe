/*
 * Created on Sep 21, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTimeline;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DocumentVersionManager;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.ITimeLineListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeDateFormat;
import edu.virginia.speclab.ivanhoe.shared.data.Move;

/**
 * @author Nick
 */
public class RoleHistoryTreeModel extends DefaultTreeModel implements ITimeLineListener
{
    private final DocumentVersionManager dvManager;
    private final DefaultMutableTreeNode roleListRootNode;
    private final HashMap roleNodeMap;
           
    private RoleHistoryTreeModel( DefaultMutableTreeNode roleListRootNode, DiscourseField discourseField )
    {
        super(roleListRootNode);

        this.dvManager = discourseField.getDocumentVersionManager();
        this.roleListRootNode = roleListRootNode;
        this.roleNodeMap = new HashMap(5);
        
        DiscourseFieldTimeline timeline = discourseField.getDiscourseFieldTimeline();

        LinkedList moveList = timeline.getTimeline();
  
        for( Iterator i = moveList.iterator(); i.hasNext(); )
        {
            MoveEvent event = (MoveEvent) i.next();
            addMoveToHistoryDisplay(event.getMove());
        }        
        
        timeline.addListener(this);
    }
    
    public static RoleHistoryTreeModel createRoleHistoryTreeModel( DiscourseField discourseField )
    {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Roles");
        return new RoleHistoryTreeModel(rootNode,discourseField);
    }
    
    /**
     * Add move to the history of the appropriate player
     * @param move
     */
    private void addMoveToHistoryDisplay(Move move)
    {
       if( move == null ) return;
       
       SimpleLogger.logInfo("History panel got move update for " + move.getRoleName());

       // find the tree node for the moving player
       String name = move.getRoleName();
       DefaultMutableTreeNode roleNode =
          (DefaultMutableTreeNode)this.roleNodeMap.get(name);

       // If we can't find the node, add it
       if (roleNode == null)
       {
          roleNode = new RoleNode(name, false);
          this.roleNodeMap.put(name, roleNode);
          roleListRootNode.add(roleNode);
       }

       // create the node representing the move
       int moveCnt = roleNode.getChildCount() + 1;
       String date = IvanhoeDateFormat.format(move.getSubmissionDate());
       String lbl = "Move #" + moveCnt + " on " + date;
       MoveNode moveNode = new MoveNode(lbl,move, false); 
          
       // add sub-nodes for each action the the move     
       IvanhoeAction act;
       Iterator itr = move.getActions();
       ActionNode actionNode;
       String content;
       while (itr.hasNext())
       {
          act = (IvanhoeAction)itr.next();
          content = act.toHtml(dvManager.getDocumentVersion(act));
          actionNode = new ActionNode(content, act);
          moveNode.add(actionNode);
       }
       
       // add the node and reload the tree
       roleNode.add(moveNode);
       reload();
    }
    
    public RoleNode getRoleNode( String roleName )
    {
        return (RoleNode)this.roleNodeMap.get(roleName);
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.model.discourse.ITimeLineListener#moveAddedToHistory(edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent)
     */
    public void moveAddedToHistory(MoveEvent event)
    {
        addMoveToHistoryDisplay(event.getMove());        
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.model.discourse.ITimeLineListener#addNewDocument(edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo)
     */
    public void addNewDocument(DocumentInfo docInfo)
    {
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.model.discourse.ITimeLineListener#removeDocument(edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo)
     */
    public void removeDocument(DocumentInfo docInfo)
    {
    }
}
