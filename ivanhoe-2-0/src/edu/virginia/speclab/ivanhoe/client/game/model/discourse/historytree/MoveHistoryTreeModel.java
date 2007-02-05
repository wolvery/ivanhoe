/*
 * Created on Sep 21, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTimeline;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DocumentVersionManager;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.ITimeLineListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;
import edu.virginia.speclab.ivanhoe.shared.data.Move;

/**
 * @author Nick
 */
public class MoveHistoryTreeModel extends DefaultTreeModel implements ITimeLineListener
{
    private final DocumentVersionManager dvManager; 
    private DefaultMutableTreeNode moveListRootNode;
           
    private MoveHistoryTreeModel( DefaultMutableTreeNode moveListRootNode, DiscourseField discourseField )
    {
        super(moveListRootNode);
        this.moveListRootNode = moveListRootNode;
        this.dvManager = discourseField.getDocumentVersionManager();
        
        DiscourseFieldTimeline timeline = discourseField.getDiscourseFieldTimeline();
        
        if( timeline != null )
        {
	        LinkedList moveList = timeline.getTimeline();
	  
	        for( Iterator i = moveList.iterator(); i.hasNext(); )
	        {
	            MoveEvent event = (MoveEvent) i.next();
	            addMoveToHistoryDisplay(event.getMove());
	        }        
	        
	        timeline.addListener(this);
        }
    }
    
    public static MoveHistoryTreeModel createMoveHistoryTreeModel( DiscourseField discourseField )
    {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Player Moves");
        return new MoveHistoryTreeModel(rootNode, discourseField);
    }

    /**
     * Add move to the history of the appropriate player
     * @param move
     */
    private void addMoveToHistoryDisplay(Move move)
    {
       if( move == null ) return;
        
       // create the node representing the move
       int moveCnt = moveListRootNode.getChildCount() + 1;
//       String date = Ivanhoe.format(move.getSubmissionDate());
       String label = "Move #" + moveCnt + " by " + move.getRoleName();

       MoveNode moveNode = new MoveNode(label, move, false);       
          
       // add sub-nodes for each action the the move     
       IvanhoeAction act;
       Iterator itr = move.getActions();
       ActionNode actionNode;
       String content;
       while (itr.hasNext())
       {
          act = (IvanhoeAction)itr.next();
          DocumentVersion version = dvManager.getDocumentVersion(act); 
          content = act.toHtml(version);
          actionNode = new ActionNode(content, act);
          moveNode.add(actionNode);
       }
       
       // add the node and reload the tree
       moveListRootNode.add(moveNode);
       reload();
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
    public void addNewDocument(DocumentInfo docInfo) {}

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.model.discourse.ITimeLineListener#removeDocument(edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo)
     */
    public void removeDocument(DocumentInfo docInfo) { }
    
}
