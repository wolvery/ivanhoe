/*
 * Created on Aug 1, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.view.stemma;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.IDocumentVersionManagerListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.IRoleListener;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.DiscourseFieldNavigator;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.IDiscourseFieldNavigatorListener;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.blist.BArrayList;
import edu.virginia.speclab.ivanhoe.shared.blist.BLinkedList;
import edu.virginia.speclab.ivanhoe.shared.blist.BList;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.Role;

/**
 * This class is the tree model behind the whole Stemma view.  As such, it is
 * the main interface to the stemma view package.
 * 
 * @author benc
 */
public class StemmaManager implements
        TreeModel, IRoleListener {
    
    private final DiscourseField discourseField;
    private final DiscourseFieldNavigator navigator;
    private final NavigatorListener navigatorListener;
    private final DocumentVersionManagerListener dvManagerListener;
    private final DocumentInfo documentInfo;
    private final HashMap nodes;
    private final BList treeListeners;
    private TreePath selectedPath;
    
    public StemmaManager(DocumentInfo documentInfo, DiscourseField discourseField, DiscourseFieldNavigator navigator) {
        this.discourseField = discourseField;
        this.navigator = navigator;
        this.navigatorListener = new NavigatorListener();
        this.dvManagerListener = new DocumentVersionManagerListener();
        this.documentInfo = documentInfo;
        this.nodes = new HashMap();
        this.treeListeners = new BArrayList();
        
        navigator.addListener(navigatorListener);
        discourseField.getDocumentVersionManager().addListener(dvManagerListener);
        
        // listen for color updates
        discourseField.getRoleManager().addRoleListener(this);
    }

    public StemmaNode addDocumentVersion( DocumentVersion documentVersion ) {
        if (documentVersion == null) {
            throw new IllegalArgumentException("documentVersion must not be null");
        }
        
        Integer parentKey = new Integer(documentVersion.getParentID());
        Integer key = new Integer(documentVersion.getID());
        
        boolean nodeExists = false;
        if (nodes.containsKey(key)) {
            nodeExists = true;
            
            StemmaNode sNode = (StemmaNode) nodes.get(key);
            if (sNode.getDocumentVersion().equals(documentVersion)) {
                return sNode;
            }
        }

        final StemmaNode parentNode;
        if (nodes.containsKey(parentKey)) {
            parentNode = (StemmaNode) nodes.get(parentKey);
        } else {
            parentNode = null;
        }
        
        final int versionRank = discourseField.getDiscourseFieldTimeline().getTick(documentVersion);
        Role versionRole = discourseField.getRoleManager().getRole(documentVersion.getRoleID());
        if (versionRole == null) {
            versionRole = Role.GAME_CREATOR_ROLE;
        }
        StemmaNode sNode = new StemmaNode(documentVersion, versionRank, parentNode,
                versionRole.getFillPaint(), versionRole.getStrokePaint());
        
        nodes.put(key, sNode);
        
        if (nodeExists) {
            fireTreeNodesChanged(sNode, getPathToRoot(sNode));
        } else {
            fireTreeNodesInserted(sNode, getPathToRoot(sNode));
        }
        
        return sNode;
    }
    
    public boolean removeDocumentVersion(DocumentVersion version) {
    	final BList nodeKeys = new BArrayList(nodes.keySet());
        for (Iterator i=nodeKeys.iterator(); i.hasNext(); ) {
            final Integer dvID = (Integer) i.next();
            if (version.getID() == dvID.intValue()) {
            	final StemmaNode removedNode = ((StemmaNode) nodes.get(dvID));
            	final StemmaNode removedNodeParent = removedNode.getParentNode();
            	
                if (removedNodeParent == null) {
                    // Can't remove the root from the stemma.  Just close the window
                    return false;
                }
                
                removedNodeParent.remove(removedNode);
                nodes.remove(dvID);
                fireTreeNodesChanged(removedNodeParent, getPathToRoot(removedNodeParent));
                return true;
            }
        }
        
        return false;
    }

    public StemmaNode setSelectedNode(int documentVersionID) {
        Integer keyID = new Integer(documentVersionID);
        if (nodes.containsKey(keyID)) {
            return setSelectedNode((StemmaNode) nodes.get(keyID));
        } else {
            return setSelectedNode(null);
        }

    }
    
    StemmaNode setSelectedNode(StemmaNode node) {
        if (node == getSelectedNode()) return node;
        
        setSelectedPath(selectedPath, false);
        this.selectedPath = this.getPathToRoot(node);
        setSelectedPath(selectedPath, true);
        return node;
    }
    
    private void setSelectedPath(TreePath path, boolean selected) {
        if (path != null) {
            for (Iterator i=(new BArrayList(path.getPath())).iterator(); i.hasNext(); ) {
                StemmaNode sNode = (StemmaNode) i.next();
                sNode.setSelected(selected);
            }
        }
    }
    
    StemmaNode getSelectedNode() {
        if (selectedPath != null) {
            return (StemmaNode) this.selectedPath.getLastPathComponent();
        } else {
            return null;
        }
        
    }
    
    public DocumentVersion getSelectedDocumentVersion() {
        StemmaNode node = getSelectedNode();
        DocumentVersion version = null;
        if (node != null) {
            version = node.getDocumentVersion();
        }
        
        return version;
    }
    
    public Object getRoot() {
        for (Iterator i=nodes.values().iterator(); i.hasNext(); )
        {
            StemmaNode gNode = (StemmaNode) i.next();
            if (gNode.isRootNode())
            {
                return gNode;
            }
        }
        
        RuntimeException e = new RuntimeException("The stemma is not a tree!  Failing getRoot().");
        SimpleLogger.logError("No root node in: "+this.nodes.values(), e);
        throw e;
    }

    public int getChildCount(Object parent) {
        if (!(parent instanceof StemmaNode))
        {
            throw new IllegalArgumentException();
        }
        
        return ((StemmaNode) parent).getChildren().size();
    }

    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }

    public void addTreeModelListener(TreeModelListener l) {
        treeListeners.add(l);
    }
    
    public void removeTreeModelListener(TreeModelListener l) {
        treeListeners.remove(l);
    }

    public Object getChild(Object parent, int index) {
        if (!(parent instanceof StemmaNode))
        {
            throw new IllegalArgumentException();
        }
        
        return ((StemmaNode) parent).getChildren().get(index);
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (!(parent instanceof StemmaNode))
        {
            throw new IllegalArgumentException();
        }
        
        return ((StemmaNode) parent).getChildren().indexOf(child);
    }
    
    TreePath getPathToRoot(Object treeNode) {
        if (! (treeNode instanceof StemmaNode) ) {
            if (treeNode != null) {
                SimpleLogger.logInfo("Cannot get path from non-StemmaNode to the root");
            }
            return null;
        }
        
        BLinkedList nodeList = new BLinkedList();
        nodeList.addFirst(treeNode);
        final Object rootNode = getRoot();
        
        while (nodeList.getLast() != rootNode) {
            StemmaNode lastPathComponent = (StemmaNode) nodeList.getLast();
            StemmaNode lastPathParent = lastPathComponent.getParentNode();
            if (lastPathParent == null) {
                throw new RuntimeException("Internal error while getting path to root");
            }
            nodeList.addLast(lastPathParent);
        }
        
        Collections.reverse(nodeList);
        return new TreePath(nodeList.toArray());
        
    }
    
    public final DocumentInfo getDocumentInfo()
    {
        return documentInfo;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        if (!path.getLastPathComponent().equals(newValue)) {
            fireTreeNodesChanged(newValue, path);
        }
    }

    private void fireTreeNodesChanged(Object sourceNode, TreePath path) {
        TreeModelEvent event = new TreeModelEvent(sourceNode, path);
        for (Iterator i=treeListeners.iterator(); i.hasNext(); ) {
            TreeModelListener listener = (TreeModelListener)i.next();
            listener.treeNodesChanged(event);
        }
    }
    
    private void fireTreeNodesInserted(Object newNode, TreePath path) {
        TreeModelEvent event = new TreeModelEvent(newNode, path);
        for (Iterator i=treeListeners.iterator(); i.hasNext(); ) {
            TreeModelListener listener = (TreeModelListener)i.next();
            listener.treeNodesChanged(event);
        }
    }

    /**
     * @param activatedNode
     */
    void activateNode(StemmaNode activatedNode) {
        Workspace.instance.openEditor(activatedNode.getDocumentVersion());
    }
    
    public void roleChanged()
    {
        Set nodeSet = nodes.entrySet();
        
        for( Iterator i = nodeSet.iterator(); i.hasNext(); )
        {
            Map.Entry nodeEntry = (Map.Entry) i.next();
            StemmaNode node = (StemmaNode) nodeEntry.getValue();
            
            final int roleID = node.getDocumentVersion().getRoleID(); 
            final Role role = discourseField.getRoleManager().getRole(roleID);
            
            node.setFillColor(role.getFillPaint());
            node.setStrokeColor(role.getStrokePaint());
        }
    }
    
    public void stemmaClosing() {
        navigator.removeListener(navigatorListener);
        discourseField.getDocumentVersionManager().removeListener(dvManagerListener);
    }
    
    private class DocumentVersionManagerListener implements
            IDocumentVersionManagerListener {

        public void documentVersionAdded(DocumentVersion version)
        {
            if (documentInfo.getTitle().equals( version.getDocumentTitle() ) )
            {
                StemmaManager.this.addDocumentVersion(version);
            }
        }

        public void documentVersionRemoved(DocumentVersion version)
        {
            if (documentInfo.getTitle().equals( version.getDocumentTitle() ) )
            {
                StemmaManager.this.removeDocumentVersion(version);
            }
        }
    }

    private class NavigatorListener implements
            IDiscourseFieldNavigatorListener { 
     
        public void selectedRoleChanged(String selectedRoleName, MoveEvent currentEvent) {
            for (Iterator i = nodes.values().iterator(); i.hasNext(); ) {
                StemmaNode node = (StemmaNode) i.next();
                boolean highlighted = (node.getDocumentVersion().getRoleName().equals(selectedRoleName));
                node.setHighlighted(highlighted);
            }
        }
    
        public void roleAdded(String roleName) {}
        public void roleRemoved(String roleName) {}
    }
}
