/*
 * Created on Sep 29, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator.history;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree.ActionNode;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree.RoleHistoryTreeModel;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree.MoveNode;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree.RoleNode;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.DiscourseFieldNavigator;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.IActionSelectionListener;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.IDiscourseFieldNavigatorListener;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;
import edu.virginia.speclab.ivanhoe.shared.data.Move;
import edu.virginia.speclab.ivanhoe.shared.data.Role;


public class DiscourseHistoryView extends JPanel
	implements TreeSelectionListener, IDiscourseFieldNavigatorListener, IActionSelectionListener 
{
       private DiscourseFieldNavigator discourseFieldNavigator;
       private RoleManager roleManager;
       
       private DefaultTreeModel model;
       private JTree inspector;
       private JEditorPane narrative;
       
       private boolean selfModifyFlag;
              
       private static final Icon placeHolderIcon = ResourceHelper.instance.getIcon("res/icons/place-holder.gif");
       
       private class TreeNodeClickHandler extends MouseAdapter
       {
          private final DiscourseHistoryView window;

        /**
         * @param window
         */
        public TreeNodeClickHandler(DiscourseHistoryView window)
        {
            this.window = window;
        }

        public void mousePressed(MouseEvent evt)
          {
             if ( (evt.getSource() instanceof JTree) &&
                  (evt.getClickCount() > 0) )
             {
                this.window.handleClick(evt);
             }
          }
       }
       
       private class CellRenderer extends JLabel implements TreeCellRenderer
	   {
           public CellRenderer()
           {
	            setFont(IvanhoeUIConstants.SMALL_FONT);
	            setBackground(IvanhoeUIConstants.HIGHLIGHTED_TEXT);	            
           }
           
	        /*
	         * (non-Javadoc)
	         * 
	         * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree,
	         *      java.lang.Object, boolean, boolean, boolean, int, boolean)
	         */
	        public Component getTreeCellRendererComponent(JTree tree, Object node,
	                boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	        {
            	setText(node.toString());            	            	
           	    setIcon(placeHolderIcon);

           	    if( selected == true ) 
            	{
            	    setOpaque(true);            	    
            	}
            	else
            	{
            	    setOpaque(false);            	    
            	}
            	            
	            return this;
	        }
	
	    }
    
       public DiscourseHistoryView(DefaultTreeModel treeModel, DiscourseFieldNavigator navigator, RoleManager roleManager )
       {
           this.model = treeModel;
           
           // listen for changes to the selected role
           if( navigator != null )
           {
	           this.discourseFieldNavigator = navigator;
	           this.discourseFieldNavigator.addListener(this);    
	           this.discourseFieldNavigator.getActionSelection().addListener(this);
           }
           
           this.roleManager = roleManager;           

           setBorder(null);
           
           inspector = createHistoryTree(treeModel); 
           
           JScrollPane sp = new JScrollPane(inspector);
           sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
           sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

           // init the move narrative display
           narrative = new JEditorPane();
           narrative.setContentType("text/html");
           narrative.setEditable(false);
           narrative.setMargin( new Insets(10,10,10,10));
           narrative.setFont(IvanhoeUIConstants.SMALL_FONT);
           
           JScrollPane nsp = new JScrollPane(narrative);
           nsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
           nsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
           nsp.setBorder(new TitledBorder(
                 new EtchedBorder(),
                 "move rationale",
                 TitledBorder.LEFT,
                 TitledBorder.DEFAULT_POSITION,
                 IvanhoeUIConstants.SMALL_FONT));
           
           JSplitPane split =
              new JSplitPane(
                 JSplitPane.VERTICAL_SPLIT,
                 sp, nsp);
           split.setDividerLocation(0.80);
           split.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
           
           setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));           
           add(split);
       }
       
       public void handleClose()
       {           
           if( discourseFieldNavigator != null )
           {
               discourseFieldNavigator.removeListener(this);
               discourseFieldNavigator.getActionSelection().removeListener(this);
           }
       }
       
       private JTree createHistoryTree( DefaultTreeModel treeModel )
       {
           if( treeModel == null ) return null;
           
           JTree tree = new JTree(treeModel);
           tree.setFont(IvanhoeUIConstants.SMALL_FONT);
           tree.setShowsRootHandles(true);
           tree.addTreeSelectionListener( this );
           tree.getSelectionModel().setSelectionMode(
              TreeSelectionModel.SINGLE_TREE_SELECTION);
           tree.putClientProperty("JTree.lineStyle", "Angled");
           tree.putClientProperty(JTree.ROOT_VISIBLE_PROPERTY, Boolean.FALSE);
           tree.setCellRenderer( new CellRenderer() );

           // listen for double clicking on the tree   
           tree.addMouseListener(new TreeNodeClickHandler(this));
           
           return tree;
       }

       public void valueChanged(TreeSelectionEvent e)
       {
       	  // don't propogate state changes when this flag is set
       	  if( selfModifyFlag == true ) return;
       	  
       	  selfModifyFlag = true;
       	  
          Object selection = this.inspector.getLastSelectedPathComponent();
          if (selection instanceof ActionNode)
          {
             ActionNode node = (ActionNode)selection;
             TreeNode parent = node.getParent();
             
             if (parent instanceof MoveNode)
             {
                 MoveNode parentMoveNode = (MoveNode)parent;
                 selectMoveEvent(parentMoveNode);
             }
//             else if (parent instanceof DocumentVersionNode)
//             {
//                 DocumentVersionNode parentDocVersion = (DocumentVersionNode)parent;
//                 // Do we need to do anything with this?
//             }
          }
          else if (selection instanceof MoveNode)
          {
             MoveNode move = (MoveNode)selection;
             selectMoveEvent(move);
          }
          else if( selection instanceof RoleNode )
          {
              if( this.discourseFieldNavigator != null && roleManager != null )
              {
                  RoleNode userNode = (RoleNode)selection;
                  Role role = roleManager.getRole(userNode.getRoleName());
                  this.discourseFieldNavigator.setSelectedRole(role);                  
              }
          }
          
          selfModifyFlag = false;   	  
       }
       
       // handle a request to look up the history of 
       // a given node in the history tree.   
       private void selectAction(ActionNode selectedNode)
       {
          IvanhoeAction nodeAction = selectedNode.getAction();
          Move move = discourseFieldNavigator.getDiscourseField().getContainingMove(nodeAction);
          narrative.setText(move.getDescription());
          selectPath(new TreePath(selectedNode.getPath()));
       }
       
       private void selectPath(TreePath selectionPath)
       {
           // select the specified node and scroll to it
           selfModifyFlag = true;
           this.inspector.setSelectionPath(selectionPath);
           selfModifyFlag = false;
      	   this.inspector.scrollPathToVisible(selectionPath); 
       }
       
       private void handleClick(MouseEvent evt)
       {
          TreePath path = inspector.getSelectionPath();
          Rectangle bounds = inspector.getPathBounds(path);

          // if the double click target is the currently selected node
          if (bounds != null && bounds.contains(evt.getPoint()) == true)
          {
             Object selection = path.getLastPathComponent();

             // And the selected node is a action node
             if ( discourseFieldNavigator != null && selection instanceof ActionNode)
             {
                 ActionNode actionNode = (ActionNode)selection;
                 this.discourseFieldNavigator.getActionSelection()
                 		.changeSelection(actionNode.getAction());
         
                if (evt.getClickCount() == 2) 
                {
                    // Then select the action for display
                    selectAction(actionNode);
                    IvanhoeAction act = actionNode.getAction();
                    DocumentVersion docVersion = 
                            discourseFieldNavigator.getDiscourseField().getDocumentVersionManager()
                                    .getDocumentVersion(act);
                    
                    Workspace.instance.openEditor(docVersion);
                }
             }
          }
       }
       
   	/* (non-Javadoc)
   	 * @see edu.virginia.speclab.ivanhoe.client.navigator.IDiscourseFieldNavigatorListener#selectedRoleChanged()
   	 */
   	public void selectedRoleChanged(String selectedRole, MoveEvent currentEvent )
   	{	    
   		if( selfModifyFlag == true )
   			return;
   		
   		// only applies to the role history tree
   		if( this.model instanceof RoleHistoryTreeModel == false )
   		    return;
   		
   		RoleHistoryTreeModel roleTreeModel = (RoleHistoryTreeModel) this.model;
   		
   	    RoleNode roleNode = roleTreeModel.getRoleNode(selectedRole);	    

   	    if( roleNode == null ) 
   	    	return;
   	    
   	    TreePath selectionPath = null;
   	    
   	    if( currentEvent != null )
   	    {
   	        Enumeration childNodes = roleNode.children();	        
   	        while( childNodes.hasMoreElements() )
   	        {
   	            MoveNode moveNode = (MoveNode)childNodes.nextElement();
   	            
   	            if( moveNode.getMove() == currentEvent.getMove() )
   	            {
   	                /* We don't actually set the move selected because that
   	                 * a) would result in an infinite loop, though that's
   	                 *    fixable, and
   	                 * b) would move the timeline, which is not what we want
   	                 *    when you simply select a role.
   	                 */
   	                selectionPath = new TreePath(moveNode.getPath());
   	                this.narrative.setText(moveNode.getNarrative());
   	                break;
   	            }	            
   	        }
   	    }
   	    
   	    if( selectionPath == null )	    
   	    {
   	        selectionPath = new TreePath(roleNode.getPath());
   	    }
   	    
   	    selectPath(selectionPath);	    
   	}
   	
   	private void selectMoveEvent( MoveNode node )
   	{
   	    this.narrative.setText(node.getNarrative());
        
   	    if( this.discourseFieldNavigator != null && roleManager != null )
   	    {
	   	    DiscourseField discourseField = this.discourseFieldNavigator.getDiscourseField();
	        MoveEvent moveEvent = discourseField.getDiscourseFieldTimeline().lookupEvent(node.getMove());
	        this.discourseFieldNavigator.getDiscourseFieldTime().setMoveEvent(moveEvent);
	        
	        Role role = roleManager.getRole(moveEvent.getMove().getRoleID());
	        this.discourseFieldNavigator.setSelectedRole(role);
   	    }
    }
    
    public void actionSelectionChanged(IvanhoeAction action)
    {
        findActionInTree(model, model.getRoot(), action);
    }
    
    private boolean findActionInTree(TreeModel tree, Object node, IvanhoeAction action)
    {
        boolean success = false;
        for (int i=0; i<tree.getChildCount(node); ++i)
        {
            Object childNode = tree.getChild(node, i);
            
            if (childNode instanceof ActionNode && ((ActionNode)childNode).getAction().equals(action))
            {
                ActionNode actionNode = (ActionNode)childNode;
                this.selectAction(actionNode);
                success = true;
            }
            else
            {
	            success = findActionInTree(tree, childNode, action);
            }
            
            if (success)
            {
                break;
            }
        }
        return success;
    }
    
    public void roleAdded(String roleName) {}
    public void roleRemoved(String roleName) {}
}