/*
 * Created on May 31, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.blist.*;
import edu.virginia.speclab.ivanhoe.shared.blist.lambda.TagToActionTransform;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;
import edu.virginia.speclab.ivanhoe.shared.data.Move;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.message.DocumentVersionMsg;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.MoveResponseMsg;

/**
 * @author benc
 */
public class DocumentVersionManager implements IMessageHandler
{
    private final DiscourseField discourseField;
    private final BSet documentVersions;
    private final BList listeners;
    private final int STARTING_CURRENT_DOCUMENT_VERSION_ID = -2;
    private int currentDocumentVersionID;
    private boolean initialized;
    
    public DocumentVersionManager(DiscourseField discourseField)
    {
        this.discourseField = discourseField;
        documentVersions = new BHashSet();
        this.listeners = new BArrayList();
        
        currentDocumentVersionID = STARTING_CURRENT_DOCUMENT_VERSION_ID;
        
        if (Ivanhoe.getProxy() != null)
        {
            Ivanhoe.registerGameMsgHandler(MessageType.DOCUMENT_VERSION, this);
            Ivanhoe.registerGameMsgHandler(MessageType.MOVE_RESPONSE, this);
        }
        else
        {
            SimpleLogger.logInfo("Could not register DocumentVersionManager with proxy."
                    +"  This is an error if not used in a unit test.");
        }
    }
    
    public void addListener(IDocumentVersionManagerListener listener)
    {
        listeners.add(listener);
    }
    
    public boolean removeListener(IDocumentVersionManagerListener listener)
    {
        return listeners.remove(listener);
    }
    
    private void fireVersionAdded(DocumentVersion version)
    {
        for (Iterator i=listeners.iterator(); i.hasNext(); )
        {
            final IDocumentVersionManagerListener listener =
                    (IDocumentVersionManagerListener) i.next();
            listener.documentVersionAdded(version);
        }
    }

    private void fireVersionRemoved(DocumentVersion version)
    {
        for (Iterator i=listeners.iterator(); i.hasNext(); )
        {
            final IDocumentVersionManagerListener listener =
                    (IDocumentVersionManagerListener) i.next();
            listener.documentVersionRemoved(version);
        }
    }
    
    private int getNextCurrentDocumentVersionID()
    {
        return currentDocumentVersionID--;
    }
    
    public void removeCurrentDocumentVersions()
    {
        BCollection currentDocumentVersions = documentVersions.filter(new CurrentDocumentVersionFilter());
        
        discourseField.removeDocumentVersions(currentDocumentVersions);
        
        for (Iterator i=currentDocumentVersions.iterator(); i.hasNext(); )
        {
            DocumentVersion dv = (DocumentVersion) i.next();
            removeDocumentVersion(dv);
            fireVersionRemoved(dv);
        }
    }
    
    public DocumentVersion createNewDocumentVersion(DocumentInfo docInfo)
    {
        String docTitle = docInfo.getTitle();
        String roleName = docInfo.getContributor();
        int roleID = docInfo.getContributorID();
        Date date = docInfo.getCreateTime();
        
        DocumentVersion newDocumentVersion = new DocumentVersion(
                getNextCurrentDocumentVersionID(), docTitle, roleName, roleID,
                date, DocumentVersion.NO_PARENT_ID, false);
        
        documentVersions.add(newDocumentVersion);
        fireVersionAdded(newDocumentVersion);
        return newDocumentVersion;
    }
    
    public DocumentVersion createChildDocumentVersion(
            DocumentVersion parentVersion, String roleName, int roleID,
            Date date)
    {
        DocumentVersion newDocumentVersion = new DocumentVersion(
                getNextCurrentDocumentVersionID(),
                parentVersion.getDocumentTitle(),
                roleName, roleID, date, parentVersion.getID(), false);
        newDocumentVersion.setParentVersion(parentVersion);
        
        documentVersions.add(newDocumentVersion);
        fireVersionAdded(newDocumentVersion);
        return newDocumentVersion;
    }
    
    private void addDocumentVersion( DocumentVersion version )
    {
        if( documentVersions.contains(version) )
        {
            new RuntimeException( "Attempted to add a version that is already present: "+version);
        }

        documentVersions.add(version);    
    }
    
    public BCollection getDocumentVersions( DocumentInfo docInfo )
    {
        return documentVersions.filter(new DocumentInfoFilter(docInfo));
    }
    
    public BCollection getDocumentVersions( Move move )
    {
        BHashSet documentVersionSet = new BHashSet();
        BList actions = move.getActionList();
        
        for( Iterator i = documentVersions.iterator(); i.hasNext(); )
        {
            DocumentVersion documentVersion = (DocumentVersion) i.next();
            
            for( Iterator j = actions.iterator(); j.hasNext(); )
            {
                IvanhoeAction action = (IvanhoeAction) j.next();
                
                if( documentVersion.getActionIDs().contains(action.getId()) )
                {
                    documentVersionSet.add(documentVersion);
                    break;
                }
            }
        }
        
        return documentVersionSet;
    }

    public void initDocumentVersions()
    {
        for( Iterator i = documentVersions.iterator(); i.hasNext(); )
        {
            DocumentVersion version = (DocumentVersion) i.next();
            
            int parentVersionID = version.getParentID();
            
            if( parentVersionID != DocumentVersion.NO_PARENT_ID )
            {
                DocumentVersion parentVersion = getDocumentVersion( parentVersionID );
                version.setParentVersion(parentVersion);
            }
        }
        
        initialized = true;
    }
    
    final public DocumentVersion getDocumentVersion( IvanhoeAction action )
    {
        return (action == null ? null : getDocumentVersion(action.getDocumentVersionID()));
    }
    
    public DocumentVersion getDocumentVersion( int versionID )
    {
		if( versionID == DocumentVersion.NO_PARENT_ID )
        {
            return null;
        }
        
        for( Iterator i = documentVersions.iterator(); i.hasNext(); )
        {
            DocumentVersion version = (DocumentVersion) i.next();
            
            if( version.getID() == versionID )
            {
                return version;
            }
        }
        
        return null;
    }
  
    /**
     * @param docTitle
     * @param roleID
     * @param moveDate
     * @return Returns the latest document version for <code>docTitle</code>
     * in <code>playerName</code> discourse field at date <code>moveDate</code>.
     * If no moves have been made in the document, its initial version is
     * returned
     * @deprecated
     *      There is no longer a single version given these arguments
     */
    public DocumentVersion findDocumentVersion(String docTitle, int roleID,  Date moveDate)
    {
       DocumentVersion latestDocumentVersion = null;
       
       for (Iterator i=documentVersions.iterator(); i.hasNext(); )
       {
           DocumentVersion dv = (DocumentVersion)(i.next());
		   
           if( dv.getDocumentTitle().equals(docTitle)
			   && dv.getRoleID() == roleID)
           {
               if (dv.getDate().equals(moveDate))
               {
                   return dv;
               }
               
			   // if we can't find the version requested, return the 
			   // latest version. 
               if (latestDocumentVersion == null
                   || dv.getDate().after(latestDocumentVersion.getDate()) )
               {
				   latestDocumentVersion = dv;
               }
           }
       }
	   
	   if (latestDocumentVersion == null)
       {
           // this player doesn't have a version of this document, so return the root version
           latestDocumentVersion = findRootDocumentVersion( docTitle );
           
           // there is no root version, this is an error
           if( latestDocumentVersion == null )
           {
               throw new RuntimeException("Unable to find document: "+docTitle);    
           }           
       }
       
       return latestDocumentVersion;
    }
    
    /**
     * Find the root document version with this title
     * @param title
     *      Title of the document.  Document must exist
     * @return
     *      The document version
     * @deprecated
     *      Would've been deleted, but it's used in another deprecated method.
     *      This doesn't always work properly, and it's currently unnecessary.
     */
    private DocumentVersion findRootDocumentVersion( String title )
    {
        for (Iterator i=documentVersions.iterator(); i.hasNext(); )
        {
            DocumentVersion version = (DocumentVersion)i.next();
            if ( version.getDocumentTitle().equals(title) 
                    && version.getParentID() == DocumentVersion.NO_PARENT_ID )
            {
                return version;
            }
        }
        
        throw new RuntimeException("Cannot find root version of document:"+title);        
    }
    
    /**
     * @param playerName
     * @return Returns lates version for for <code>docTitle</code>
     * in <code>playerName</code> discourse field. If no moves have been 
     * made in the document, its initial version is returned
     * @deprecated
     *      There is no longer a single cannonical last version for a role
     */
    public DocumentVersion findLatestVersion(String docTitle, int roleID )
    {
       return findDocumentVersion(docTitle, roleID, Ivanhoe.getDate());
    }

    public void handleMessage(Message msg)
    {
        if( msg instanceof DocumentVersionMsg )
        {
            DocumentVersionMsg dvMsg = (DocumentVersionMsg) msg;
            DocumentVersion version = dvMsg.getVersion();
            SimpleLogger.logInfo("DocumentVersion ["+version.getID()+"] added to DocumentVersionManager");
            
            if( initialized )
			{
				// set the parent of this version
                version.setParentVersion( getDocumentVersion(version.getParentID()) );
				
				// now find all of the version's children and point them at this document version object
                // (Is this needed? --b.c)
				Collection collection = getChildren(version);				
				for( Iterator i = collection.iterator(); i.hasNext(); )
				{
					DocumentVersion child = (DocumentVersion) i.next();
					child.setParentVersion(version);
				}
			}
            
            addDocumentVersion(version);
        }
        else if (msg instanceof MoveResponseMsg )
        {
            MoveResponseMsg mrMsg = (MoveResponseMsg)msg;
            if (mrMsg.isSuccess())
            {
                this.removeCurrentDocumentVersions();
            }
        }
    }

    /**
     * This method finds all children of this DocumentVersion.  It's not
     * terribly performant, but if it becomes a bottleneck, caching could
     * do a lot of good.
     * 
     * @param documentVersion
     *          DocumentVersion to find children of
     * @return
     *          List of children
     */
    public BCollection getChildren(DocumentVersion documentVersion)
    {
        return documentVersions.filter(new DocumentVersionChildFilter(documentVersion));
    }
    
    /**
     * This method figures out if a DocumentVersion has any children in the
     * current move in progress.  This is not performant.
     * 
     * @param documentVersion
     * @return
     */
    public boolean hasCurrentChild(DocumentVersion documentVersion)
    {
        return getCurrentChild(documentVersion) != null;
    }
    
    public DocumentVersion getCurrentChild(DocumentVersion documentVersion)
    {
        BCollection children = documentVersions
            .filter(new CurrentDocumentVersionFilter())
            .inplaceFilter(new DocumentVersionChildFilter(documentVersion));
        
        if (children.isEmpty())
        {
            return null;
        }
        else if (children.size() == 1)
        {
            return (DocumentVersion)children.iterator().next();
        }
        else
        {
            throw new RuntimeException("DocumentVersion contains two different revisions in same move.");
        }
    }

    /**
     * @return
     */
    public Map getCurrentDocumentVersionOrigins(CurrentMove move)
    {
        BCollection currentDocumentVersions =
                documentVersions.filter(new CurrentDocumentVersionFilter());
        Map originMap = new HashMap();
        
        for (Iterator i=currentDocumentVersions.iterator(); i.hasNext(); )
        {
            DocumentVersion currentDV = (DocumentVersion)i.next();
            originMap.put(
                    new Integer(currentDV.getID()        ),
                    new Integer(currentDV.getParentID()  ));
        }
        
        return originMap;
    }
    
    public static class DocumentVersionChildFilter implements BCollectionFilter
    {
        private final int parentID;
        public DocumentVersionChildFilter(DocumentVersion documentVersion)
        {
            this.parentID = documentVersion.getID();
        }

        public boolean accept(Object o)
        {
            DocumentVersion documentVersion = (DocumentVersion)o;
            return documentVersion.getParentID() == parentID;
        }
    }
    
    public static class CurrentDocumentVersionFilter implements BCollectionFilter
    {
        public boolean accept(Object o)
        {
            return !((DocumentVersion) o).isPublished();
        }
    }
    
    public static class DocumentInfoFilter implements BCollectionFilter
    {
        private final DocumentInfo docInfo;
        
        public DocumentInfoFilter(DocumentInfo docInfo)
        {
            this.docInfo = docInfo;
        }
        
        public boolean accept(Object o)
        {
            DocumentVersion dv = (DocumentVersion) o;
            return dv.getDocumentTitle().equals(docInfo.getTitle());
        }
        
    }

    public boolean isStartingDocumentVersion(int docVersionID)
    {
        final DocumentVersion docVersion = getDocumentVersion(docVersionID);
        return (docVersion.getRoleID() == Role.GAME_CREATOR_ROLE_ID);
    }

    /**
     * @param documentVersion
     * @return
     */
    public BCollection getActions(DocumentVersion version)
    {
        BCollection actions = new BLinkedList(); 
        DocumentVersion dv = version;
        while (dv != null)
        {
            actions.addAll(dv.getActionIDs());
            dv = dv.getParentVersion();
        }
        
        // Change action tags to action objects
        actions.inplaceTransform( new TagToActionTransform(discourseField) );
        
        // Filter out nulls
        actions.inplaceFilter(
                new BCollectionFilter() {
                    public boolean accept(Object o) {
                        return o != null;
                    }
                });
        
        return actions;
    }

    boolean removeDocumentVersion(DocumentVersion docVersion)
    {
        boolean removed = false;
        if (docVersion.isPublished())
        {
            SimpleLogger.logError("doc version ["+docVersion+
                    "] is published and cannot be removed");
        }
        else if ( !this.documentVersions.contains(docVersion) )
        {
            SimpleLogger.logError("doc version ["+docVersion+
                    "] is not in DocumentVersionManager so cannot be removed");
        }
        else
        {
        	SimpleLogger.logInfo("DocumentVersion being removed: " + docVersion);
            this.documentVersions.remove(docVersion);
            fireVersionRemoved(docVersion);
            removed = true;
        }
        
        return removed;
    }
}
