package edu.virginia.speclab.ivanhoe.server.mapper.converter.documentversion;

import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.blist.BCollection;
import edu.virginia.speclab.ivanhoe.shared.blist.BCollectionFilter;
import edu.virginia.speclab.ivanhoe.shared.blist.BLinkedList;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

class DocumentVersionManager
{
    private BLinkedList actionVersionSet;
    private BLinkedList documentVersionSet;
    private HashSet startingDocuments;
    
    public DocumentVersionManager()
    {        
        actionVersionSet = new BLinkedList();
        documentVersionSet = new BLinkedList();
        startingDocuments = new HashSet();
    }
    
    private void processStartingDocumentVersions() throws MapperException, SQLException
    {
        PreparedStatement pstmt = null;
        ResultSet results = null;
        
        try 
        {
            // get all the player ids
            String sqlCommand = "SELECT d.id,d.add_date FROM document AS d, discourse_field AS df WHERE d.id=df.fk_document_id AND df.starting_doc=1";             
            pstmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            results = pstmt.executeQuery();

            while( results.next() )
            {                
                int documentID = results.getInt("id");
                Date submitDate = DBManager.parseDate(results.getString("add_date"));
                processVersion( -1, documentID, submitDate, true );
                startingDocuments.add( new Integer(documentID) );
            }
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error creating versions for starting documents: "+e);            
        }
        finally 
        {
            if (results != null) results.close();
            if (pstmt != null) pstmt.close();    
        }        
    }
        
    private void processMove( int moveID, int roleID, Date submitDate ) throws MapperException, SQLException
    {
        PreparedStatement pstmt = null;
        ResultSet results = null;
        
        try 
        {
            // skip link actions, these never create a new document version and 
            // are processed later
            String sqlCommand = 
                    "SELECT ma.fk_action_id,ad.fk_document_id"
                    +" FROM action AS a, move_action AS ma, action_document AS ad"
                    +" WHERE ma.fk_action_id=ad.fk_action_id AND ma.fk_action_id=a.id AND a.fk_type!=3 AND fk_type!=4 AND ma.fk_move_id="+moveID;            
            pstmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            results = pstmt.executeQuery();

            // iterate through the move's action list
            while( results.next() )
            {                
                int actionID = results.getInt("fk_action_id");
                int documentID = results.getInt("fk_document_id");
				
                DocumentVersion version = processVersion( roleID, documentID, submitDate, false );
                ActionVersion actionVersion = new ActionVersion(actionID,version.getId());
                actionVersionSet.add(actionVersion);
                writeActionVersion(actionVersion);
            }
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error converting discussion table: "+e);            
        }
        finally 
        {
            if (results != null) results.close();
            if (pstmt != null) pstmt.close();    
        }        
        
    }
    
    private class ActionVersion
    {
        private int actionID;
        private int versionID;
        
        public ActionVersion( int actionID, int versionID )
        {
            this.actionID = actionID;
            this.versionID = versionID;
        }

        public int getActionID()
        {
            return actionID;
        }
        

        public int getVersionID()
        {
            return versionID;
        }
    }
    
    private void writeActionVersion( ActionVersion actionVersion ) throws MapperException
    {
        System.out.println("action_version: "+actionVersion.getActionID()+","+actionVersion.getVersionID());
        
        PreparedStatement pstmt = null;
        
        // write the action version
        try
        {
           StringBuffer sql = new StringBuffer();
           sql.append("insert into action_version ");
           sql.append(" values (?, ?)");
           
           pstmt = DBManager.instance.getConnection().prepareStatement(
              sql.toString());

           pstmt.setInt(1, actionVersion.getActionID());
           pstmt.setInt(2, actionVersion.getVersionID());
           pstmt.executeUpdate();   
        }
        catch (SQLException e)
        {
           throw new MapperException("insert action_version failed: " + e);
        }
        finally
        {
           DBManager.instance.close(pstmt); 
        }        
    }

    private void processLinks() throws MapperException, SQLException
    {
        SimpleLogger.logInfo("Processing links...");
        
        BLinkedList actionDatePairs = new BLinkedList();   
        
        PreparedStatement pstmt = null;        
        ResultSet results = null;
        
        try 
        {
            // get all the player ids
            String sqlCommand = "SELECT ad.* FROM action_document AS ad, action AS a WHERE a.fk_type=3 AND a.id=ad.fk_action_id";              
            pstmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            results = pstmt.executeQuery();

            while( results.next() )
            {                
                int documentID = results.getInt("fk_document_id");
                int actionID = results.getInt("fk_action_id");
                int roleID = results.getInt("fk_role_id");
                Date versionDate = DBManager.parseDate(results.getString("version_date"));

                // if this move create a new document version, use that document version
                DocumentVersion version = processLinkInNewVersion( documentID, actionID, roleID );
                
                // if it hasn't, we need to find one that is close
                if( version == null )
                {
                    // see if the version to which this link refers has already been 
                    // discovered.
                    SimpleLogger.logInfo("Lookup existing version...");
                    version = lookupVersion(roleID,documentID,versionDate);

                    if( version == null )
                    {
                        // otherwise, use an existing document verison
                        version = processLinkInOldVersion( documentID, roleID, versionDate );
                        if( version != null ) actionDatePairs.add( new ActionDatePair( actionID, version.getDate() ));
                    }
                }
                else
                {
                    actionDatePairs.add( new ActionDatePair( actionID, version.getDate() ));
                }
                
                
                if( version != null )                
                {
                    // add this action to the version_action map
                    ActionVersion actionVersion = new ActionVersion(actionID,version.getId());
                    actionVersionSet.add(actionVersion);
                    writeActionVersion(actionVersion);                    
                }
                else
                {
                    // abort!
                    throw new RuntimeException("unable to find version:"+documentID+","+actionID+","+roleID+","+versionDate);
                }
            }
            
            writeActionDatePairs( actionDatePairs );
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error converting discussion table: "+e);            
        }
        finally 
        {
            if (results != null) results.close();
            if (pstmt != null) pstmt.close();    
        }            
    }
    
    private void writeActionDatePairs(Collection actionDatePairs) throws MapperException
    {
        SimpleLogger.logInfo("Writing action/version date pairs...");
        
        for( Iterator i = actionDatePairs.iterator(); i.hasNext(); )
        {
            ActionDatePair actionDatePair = (ActionDatePair) i.next();
            actionDatePair.write();
        }        
    }
   
    public void convertTables() throws MapperException, SQLException
    {
        SimpleLogger.logInfo("Converting version dates...");
        
        PreparedStatement pstmt = null;
        ResultSet results = null;
        
        // first, take care of the initial document versions
        //     step 1: initial documents
        processStartingDocumentVersions();
        //     step 2: added documents
        processAddedDocumentVersions();
        
        // create doc versions to mimic the functionality of the legacy system
        createDummyDocumentVersions();
        
        try 
        {
            // get all the player ids
            String sqlCommand = "SELECT id,fk_role_id,submit_date,start_date FROM move";
            pstmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            results = pstmt.executeQuery();

            // go through all of moves in the DB and figure out which ones resulted in new
            // document versions and add the document versions to the list
            while( results.next() )
            {
                int ID = results.getInt("id");
                int roleID =     results.getInt("fk_role_id");
				String submitDateString = results.getString("submit_date");
				Date submitDate = null; 
				if( submitDateString != null ) submitDate = DBManager.parseDate(submitDateString);                  
				Date startDate = DBManager.parseDate(results.getString("start_date"));
				if( submitDate == null )
				{
					// this move has not yet been submitted, use the start date of the
					// move instead
					submitDate = startDate;
				}
                processMove( ID, roleID, submitDate );
            }

            // now go through all of the link actions and figure out which document
            // versions they are reffering to
            processLinks();
             
            // set the parent ids for document versions
            updateLineage();
            
            // set the published flag
            updatePublishFlag();
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error converting discussion table: "+e);            
        }
        finally 
        {
            if (results != null) results.close();
            if (pstmt != null) pstmt.close();    
        }
        
    }
    private void updatePublishFlag() throws MapperException, SQLException
    {
        PreparedStatement pstmt = null;
        ResultSet results = null;
        
        try 
        {
            // get all the player ids
            String sqlCommand = "select action_version.*, document_version.published from action_version, document_version where action_version.fk_document_version_id=document_version.id";             
            pstmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            results = pstmt.executeQuery();
            
            while( results.next() )
            {                
                int actionID = results.getInt("fk_action_id");
                int versionID = results.getInt("fk_document_version_id");
                boolean published = (results.getInt("published")==1)?true:false;
                
                if( !published && isPublished(actionID) )
                {
                    writePublishStatus( versionID, true );    
                }
            }
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error creating versions for starting documents: "+e);            
        }
        finally 
        {
            if (results != null) results.close();
            if (pstmt != null) pstmt.close();    
        }        
    }
    
    private boolean isPublished(int actionID) throws MapperException, SQLException
    {
        PreparedStatement pstmt = null;
        ResultSet results = null;
        
        boolean published = false;
        
        try 
        {
            String sqlCommand = "SELECT move.submit_date FROM move_action,move WHERE move_action.fk_action_id=? AND move_action.fk_move_id=move.id";             
            pstmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            pstmt.setInt(1,actionID);
            results = pstmt.executeQuery();

            // move to the first result and retrieve it
            results.next();            
            Date submitDate = results.getDate("submit_date");
            published = ( submitDate != null ); 
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error looking up publish status: "+e);            
        }
        finally 
        {
            if (results != null) results.close();
            if (pstmt != null) pstmt.close();    
        }     
        
        return published;
    }
        

    private void writePublishStatus(int versionID, boolean published) throws MapperException
    {
        System.out.println("Writing publish status for document_version: "+versionID);
        
           PreparedStatement pstmt = null;
           
           // write the action record
           try
           {
              String sql = "UPDATE document_version SET published=? WHERE id=?";
              pstmt = DBManager.instance.getConnection().prepareStatement(sql);
              
              pstmt.setInt(1, (published?1:0));
              pstmt.setInt(2, versionID);
              int rows = pstmt.executeUpdate();
              
              if (rows != 1)
              {
                  throw new MapperException("Updated "+rows+" rows instead of just one");
              }
           }
           catch (SQLException e)
           {
              throw new MapperException("Update of document_version failed: " + e);
           }
           finally
           {
              DBManager.instance.close(pstmt); 
           }        
    }


    /**
     * 
     */
    private void processAddedDocumentVersions() throws MapperException, SQLException
    {
        PreparedStatement pstmt = null;
        ResultSet results = null;
        
        try 
        {
            // Get all the documents that were created by an add doc action
            pstmt = DBManager.instance.getConnection().prepareStatement(
                    "SELECT d.id,d.add_date,d.fk_contributor_id,a.id"
                    +"    FROM"
                    +"        action AS a,"
                    +"        action_document AS ad,"
                    +"        document AS d"
                    +"    WHERE"
                    +"        a.fk_type=4" // add document type
                    +"        AND"
                    +"        a.id=ad.fk_action_id"
                    +"        AND"
                    +"        ad.fk_document_id=d.id");
            results = pstmt.executeQuery();

            while( results.next() )
            {                
                int documentID = results.getInt("d.id");
                Date submitDate = DBManager.parseDate(results.getString("d.add_date"));
                int contributorID = results.getInt("d.fk_contributor_id");
                int actionID = results.getInt("a.id");
                
                // Now populate this data structure with those created documents
                DocumentVersion newDocumentVersion = processVersion( contributorID, documentID, submitDate, false );
                
                // And associate the add action with the original dummy document version
                ActionVersion addActionVersion = new ActionVersion(actionID, newDocumentVersion.getId());
                actionVersionSet.add(addActionVersion);
                writeActionVersion(addActionVersion);
                
            }
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error creating versions for added documents: "+e);            
        }
        finally 
        {
            if (results != null) results.close();
            if (pstmt != null) pstmt.close();    
        }
    }

    // find an exact match in the document version list
    private DocumentVersion lookupVersion(int roleID, int documentID, Date versionDate)
    {
        for( Iterator i = documentVersionSet.iterator(); i.hasNext(); )
        {
            DocumentVersion version = (DocumentVersion) i.next();
            
            if( version.getRoleID() == roleID && 
                version.getDate().equals(versionDate) && 
                version.getDocumentID() == documentID )
            {
                return version;
            }
        }
        
        return null;        
    }
    
    private Collection getDocumentList(Collection documentVersions)
    {
        HashSet documentIDs = new HashSet();
        for (Iterator i=documentVersions.iterator(); i.hasNext(); )
        {
            DocumentVersion version = (DocumentVersion)i.next();
            documentIDs.add(new Integer(version.getDocumentID()));
        }
        return documentIDs;
    }
    
    private Collection getRoleIDs(Collection documentVersions)
    {
        HashSet roleIDs = new HashSet();
        for (Iterator i=documentVersions.iterator(); i.hasNext(); )
        {
            DocumentVersion version = (DocumentVersion)i.next();
            roleIDs.add(new Integer(version.getRoleID()));
        }
        return roleIDs;
    }

    private void updateLineage() throws MapperException
    {
        Collection documentIDs = getDocumentList(documentVersionSet);
        
        for( Iterator i = documentIDs.iterator(); i.hasNext(); )
        {
            // For each document, create a list of only those document versions affecting that document
            Integer documentID = (Integer) i.next();
            BLinkedList docFilteredSet = 
                (BLinkedList)documentVersionSet.filter(new DocumentFilter(documentID.intValue()));
             
            Collection roleIDs = getRoleIDs(docFilteredSet);
            
            for (Iterator j=roleIDs.iterator(); j.hasNext(); )
            {
                // For each role in this list, create a list of document versions created by the role
                Integer roleID = (Integer)j.next();
                BLinkedList roleFilteredSet = 
                    (BLinkedList)docFilteredSet.filter(new RoleFilter(roleID.intValue()));
                
                // Sort this list of document versions in submission order
                Collections.sort(roleFilteredSet, new Comparator(){
                    public int compare(Object l, Object r)
                    {
                        DocumentVersion docL = (DocumentVersion)l;
                        DocumentVersion docR = (DocumentVersion)r;
                        return docL.getDate().compareTo(docR.getDate());
                    }
                });
                
                // Go through in chronological order and set the parents
                DocumentVersion previousVersion = null;
                for (Iterator k=roleFilteredSet.iterator(); k.hasNext(); )
                {
                    DocumentVersion currentVersion = (DocumentVersion)k.next();
                    if (previousVersion != null)
                    {
                        currentVersion.setParentID(previousVersion.getId());
                        updateDocumentVersion(currentVersion);
                    }
                    
                    previousVersion = currentVersion;
                }
            }            
        }
    }
    
    private DocumentVersion processVersion( int roleID, int documentID, Date submitDate, boolean published ) throws MapperException
    {
        DocumentVersion documentVersion = lookupVersion(roleID, documentID, submitDate);
        
        if( documentVersion == null )
        {
            // create a new document version, add it to the list and write it to the DB
            documentVersion = new DocumentVersion(documentID,roleID,submitDate,published);
            documentVersionSet.add(documentVersion);
            documentVersion.write();
        }
        
        return documentVersion;
    }
    
    private void createDummyDocumentVersions() throws MapperException, SQLException
    {
        PreparedStatement pstmt = null;
        ResultSet results = null;
        
        try 
        {
            // SELECT all sets of moves and documents in the same game.  Specifically,
            //  * the role contributing the document (d.fk_contributor_id)
            //  * the role making the move (m.fk_role_id)
            //  * the document id number (d.id)
            //  * and the document add date (d.add_date)
            pstmt = DBManager.instance.getConnection().prepareStatement(
                    "SELECT d.fk_contributor_id,m.fk_role_id,d.id,d.add_date"
                    + " FROM discourse_field AS df, move AS m, document AS d"
                    + " WHERE m.fk_game_id=df.fk_game_id AND df.fk_document_id=d.id");
            results = pstmt.executeQuery();

            while( results.next() )
            {                
                int contributorID = results.getInt("fk_contributor_id");
                int roleID = results.getInt("fk_role_id");
                int documentID = results.getInt("id");
                Date submitDate = DBManager.parseDate(results.getString("add_date"));
                
                DocumentVersion parentVersion = null ;
                if( startingDocuments.contains( new Integer(documentID ) ) )
                {
                    parentVersion = lookupVersion( -1, documentID, submitDate );    
                }
                else
                {
                    parentVersion = lookupVersion( contributorID, documentID, submitDate );
                }
                
                DocumentVersion currentVersion = processVersion( roleID, documentID, submitDate, true );
                
                if (parentVersion == currentVersion)
                {
                    // This occurs when the parent and the child occur at the
                    // same moment.  This is never valid.
                }
                else if( parentVersion != null )
                {
                    currentVersion.setParentID(parentVersion.getId());
                    updateDocumentVersion(currentVersion);
                }
                else
                {
                    SimpleLogger.logInfo("No parent found for ["+currentVersion.getId()+"]");
                }
            }
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error converting discussion table: "+e);            
        }
        finally 
        {
            if (results != null) results.close();
            if (pstmt != null) pstmt.close();    
        }
    }
    
    
    private DocumentVersion processLinkInNewVersion( int documentID, int actionID, int roleID ) throws MapperException, SQLException
    {
        SimpleLogger.logInfo("Processing link in new version...");
        
        DocumentVersion documentVersion = null;
        PreparedStatement pstmt = null;        
        ResultSet results = null;
        
        try 
        {
            
            String sqlCommand = "SELECT m.submit_date FROM move_action AS ma, move AS m WHERE ma.fk_move_id=m.id AND ma.fk_action_id="+actionID+
                                " AND m.submit_date IS NOT NULL";              
            pstmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            results = pstmt.executeQuery();

            while( results.next() )
            {
                Date versionDate = DBManager.parseDate(results.getString("submit_date")); 
                documentVersion = lookupVersion( roleID, documentID, versionDate );
                
                // found it, done.
                if( documentVersion != null ) break;
            }
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error converting discussion table: "+e);            
        }
        finally 
        {
            if (results != null) results.close();
            if (pstmt != null) pstmt.close();    
        }            
        
        return documentVersion;
    }

    
    private DocumentVersion processLinkInOldVersion( int documentID, int roleID, Date versionDate )
    {
        SimpleLogger.logInfo("Processing link in old version...");
        
        DocumentVersion prevVersion = null;
        for( Iterator i = documentVersionSet.iterator(); i.hasNext(); )
        {             
            DocumentVersion version = (DocumentVersion) i.next();
            
            if( version.getRoleID() == roleID &&                 
                version.getDocumentID() == documentID ) 
            {
                if( prevVersion == null || prevVersion.getDate().before(version.getDate()) ) 
                {
                    prevVersion = version;
                }
            }
        }
        
        return prevVersion;
    }

    private void updateDocumentVersion(DocumentVersion version) throws MapperException
    {
        System.out.println("Updating document_version: "+version);
     
        PreparedStatement pstmt = null;
        
        // write the action record
        try
        {
           String sql = "UPDATE document_version SET parent_id=? WHERE id=?";
           pstmt = DBManager.instance.getConnection().prepareStatement(sql);
           
           pstmt.setInt(1, version.getParentID());
           pstmt.setInt(2, version.getId());
           int rows = pstmt.executeUpdate();
           
           if (rows != 1)
           {
               throw new MapperException("Updated "+rows+" rows instead of just one");
           }
        }
        catch (SQLException e)
        {
           throw new MapperException("Update of document_version failed: " + e);
        }
        finally
        {
           DBManager.instance.close(pstmt); 
        }
    }

    public void printDocumentVersionTrees(PrintStream out)
    {
        HashMap docVersionLineage = new HashMap();
        
        out.println("digraph DocumentVersions {");
        
        for (Iterator i=documentVersionSet.iterator(); i.hasNext(); )
        {
            DocumentVersion docVer = (DocumentVersion)i.next();
            Integer docParentID = new Integer(docVer.getParentID());
            BCollection childVersions;
            if (!docVersionLineage.containsKey(docParentID))
            {
                childVersions = new BLinkedList();
                docVersionLineage.put(docParentID, childVersions);
            }
            else
            {
                childVersions = (BCollection)docVersionLineage.get(docParentID);
            }
            
            childVersions.add(docVer);
        }
        
        Collection rootDocVersionIDs = (Collection)docVersionLineage.get(new Integer(0));
        for (Iterator i=rootDocVersionIDs.iterator(); i.hasNext(); )
        {
            DocumentVersion rootDocVers = (DocumentVersion)i.next();
            out.println(" subgraph cluster_doc"+rootDocVers.getDocumentID()+" {");
            out.println("  style = filled;");
            out.println("  color = lightgrey;");
            out.println("  node [style=filled,color=white];");
            out.println();
            printDocumentVersionTreeConnections(rootDocVers.getId(), docVersionLineage, out, new HashSet());
            out.println();
            out.println("  label = \"doc"+rootDocVers.getDocumentID()+"\";");
            out.println(" }");
            out.println();
        }
        
        for (Iterator i=rootDocVersionIDs.iterator(); i.hasNext(); )
        {
            DocumentVersion rootDocVers = (DocumentVersion)i.next();
            out.println("  node"+rootDocVers.getId()+"[label = \""+rootDocVers.getId()+" (ROOT) : by "+rootDocVers.getRoleID()+"\"];");
            printDocumentVersionTreeNodes(rootDocVers.getId(), docVersionLineage, out, new HashSet());
        }
        
        out.println("}");
    }
    
    private static void printDocumentVersionTreeNodes(
            final int docVersionID, final Map docVersionLineage, PrintStream out, final Set visitedNodes)
    {
        BCollection docVersions = (BCollection)docVersionLineage.get(new Integer(docVersionID));
        if (docVersions == null)
        {
            docVersions = new BLinkedList();
        }
        
        
        for (Iterator j=docVersions.iterator(); j.hasNext(); )
        {
            DocumentVersion docVersion = (DocumentVersion)j.next();
            
            if (visitedNodes.contains(docVersion))
            {
                throw new RuntimeException("DocumentVersion graph contains a cycle!  This data is corrupt.");
            }
            else
            {
                visitedNodes.add(docVersion);
            }
            
            out.println("  node"+docVersion.getId()+" [label = \""+docVersion.getId()+" : by "+docVersion.getRoleID()+"\"];");
            printDocumentVersionTreeNodes(docVersion.getId(), docVersionLineage, out, visitedNodes);
        }
    }
    
    private static void printDocumentVersionTreeConnections(
            final int docVersionID, final Map docVersionLineage, PrintStream out, final Set visitedNodes)
    {
        BCollection docVersions = (BCollection)docVersionLineage.get(new Integer(docVersionID));
        if (docVersions == null)
        {
            docVersions = new BLinkedList();
        }
        
        for (Iterator j=docVersions.iterator(); j.hasNext(); )
        {
            DocumentVersion docVersion = (DocumentVersion)j.next();
            
            if (visitedNodes.contains(docVersion))
            {
                throw new RuntimeException("DocumentVersion graph contains a cycle!  This data is corrupt.");
            }
            else
            {
                visitedNodes.add(docVersion);
            }
            
            out.println("  node"+docVersion.getId()+" -> node"+docVersion.getParentID()+";");
            printDocumentVersionTreeConnections(docVersion.getId(), docVersionLineage, out, visitedNodes);
        }
    }
    
    private static class RoleFilter implements BCollectionFilter
    {
        private final int roleID;
        public RoleFilter(int roleID)
        {
            this.roleID = roleID;
        }
        
        public boolean accept(Object o)
        {
            return ((DocumentVersion)o).getRoleID() == roleID;
        }
    }
    
    private static class DocumentFilter implements BCollectionFilter
    {
        private final int documentID;
        public DocumentFilter(int documentID)
        {
            this.documentID = documentID;
        }
        
        public boolean accept(Object o)
        {
            return ((DocumentVersion)o).getDocumentID() == documentID;
        }
    }
}

