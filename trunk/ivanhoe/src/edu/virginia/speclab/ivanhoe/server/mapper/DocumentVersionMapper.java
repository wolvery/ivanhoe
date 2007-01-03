/*
 * Created on May 31, 2005
 */
package edu.virginia.speclab.ivanhoe.server.mapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.exception.SequenceException;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.blist.*;
import edu.virginia.speclab.ivanhoe.shared.blist.lambda.ActionTypeFilter;
import edu.virginia.speclab.ivanhoe.shared.data.ActionType;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;
import edu.virginia.speclab.ivanhoe.shared.data.Move;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author benc
 */
public class DocumentVersionMapper
{
    public static boolean mapActionToDocumentVersion( int actionID, int versionID )
    {
        boolean success = false;
        PreparedStatement pstmt = null;
        
        try {
            String sql = 
                    "INSERT INTO action_version ("
                    +"fk_action_id, fk_document_version_id )"
                    +" VALUES (?, ?);";

            pstmt = DBManager.instance.getConnection().prepareStatement(sql);

            pstmt.setInt(1, actionID );
            pstmt.setInt(2, versionID );
            pstmt.executeUpdate();
            
            success = true;
        } catch (SQLException e) {
            SimpleLogger.logError("Insert Failed: " + e);
        } finally {
            DBManager.instance.close(pstmt);
        }
        
        return success;
    }
    
    public static void writeDocumentVersion( DocumentVersion version, int gameID )
    {
        PreparedStatement pstmt = null;
        
        try {
            StringBuffer sql = new StringBuffer();
            sql.append("insert into document_version (");
            sql.append("id, fk_document_id, fk_role_id, date, parent_id, published )");
            sql.append(" values (?, ?, ?, ?, ?, ?);");

            pstmt = DBManager.instance.getConnection().prepareStatement(
                    sql.toString());

            DocumentInfo docInfo = DocumentMapper.getDocumentInfo(version.getDocumentTitle(),gameID);

            pstmt.setInt(1, version.getID());
            pstmt.setInt(2, docInfo.getId().intValue() );
            pstmt.setInt(3, version.getRoleID());
            pstmt.setString(4, DBManager.formatDate(version.getDate()));
            pstmt.setInt(5, version.getParentID());
            pstmt.setInt(6, (version.isPublished() ? 1 : 0) );
            pstmt.executeUpdate();
        } catch (SQLException e) {
            SimpleLogger.logError("Insert Failed: " + e);
        } finally {
            DBManager.instance.close(pstmt);
        }
    }
   
    public static int getDocumentVersionID( int actionID ) throws MapperException
    {
        PreparedStatement pstmt = null;
        ResultSet results = null;
        
        int versionID = DocumentVersion.INVALID_ID;
        
        try 
        {
            String sqlCommand = "SELECT fk_document_version_id FROM action_version WHERE fk_action_id=?";            
            pstmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            pstmt.setInt(1,actionID);
            results = pstmt.executeQuery();

            while( results.next() )
            {                
                versionID = results.getInt("fk_document_version_id");
            }
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error converting discussion table: "+e);            
        }
        finally 
        {
            DBManager.instance.close(results);
            DBManager.instance.close(pstmt);      
        }
        
        return versionID;        
    }
    
    public static DocumentVersion getDocumentVersion( int versionID ) throws MapperException
    {
        PreparedStatement pstmt = null;
        ResultSet results = null;
        
        DocumentVersion version = null;
        
        try 
        {
            String sqlCommand = "SELECT * FROM document_version WHERE id=?";            
            pstmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            pstmt.setInt(1,versionID);
            results = pstmt.executeQuery();

            while( results.next() )
            {                
                int documentID = results.getInt("fk_document_id");
                int roleID = results.getInt("fk_role_id");
                int parentID = results.getInt("parent_id");
                Date date = DBManager.parseDate(results.getString("date"));
                boolean published = (results.getInt("published")==1);
                
                DocumentInfo docInfo = DocumentMapper.getDocumentInfo(documentID);
                Role role = RoleMapper.get(roleID);
                
                if( docInfo != null && role != null )
                {                    
                    version = new DocumentVersion(versionID, docInfo.getTitle(), role.getName(), roleID, date, parentID,published);
                    addActionsToVersion(version);
                }
            }
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error converting discussion table: "+e);            
        }
        finally 
        {
            DBManager.instance.close(results);
            DBManager.instance.close(pstmt);      
        }
        
        return version;
    }

    private static void addActionsToVersion(DocumentVersion version) throws MapperException
    {
        PreparedStatement pstmt = null;
        ResultSet results = null;
        
        try 
        {
            String sqlCommand = "SELECT a.tag_id FROM action_version AS av, action AS a WHERE av.fk_document_version_id=? and av.fk_action_id=a.id";            
            pstmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            pstmt.setInt(1,version.getID());
            results = pstmt.executeQuery();

            while( results.next() )
            {                
                String actionTagID = results.getString("tag_id");
                version.addActionID(actionTagID);
            }
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error converting discussion table: "+e);            
        }
        finally 
        {
            DBManager.instance.close(results);
            DBManager.instance.close(pstmt);      
        }
    }

    /**
     * 
     * @param gameID
     * @param currentRoleID A value of -1 returns only globally visible verions.
     * @return
     * @throws MapperException
     */
    public static List getDocumentVersions(int gameID, int currentRoleID ) throws MapperException
    {
        PreparedStatement pstmt = null;
        ResultSet results = null;
        
        LinkedList documentVersionList = new LinkedList();
        
        try 
        {
            String sqlCommand = "SELECT dv.* FROM document_version as dv, discourse_field as df  WHERE df.fk_game_id=? and df.fk_document_id=dv.fk_document_id";            
            pstmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            pstmt.setInt(1,gameID);
            results = pstmt.executeQuery();

            while( results.next() )
            {                
                int versionID = results.getInt("id");
                int documentID = results.getInt("fk_document_id");
                int roleID = results.getInt("fk_role_id");
                int parentID = results.getInt("parent_id");
                boolean published = (results.getInt("published")==1);
                Date date = DBManager.parseDate(results.getString("date"));
                
                DocumentInfo docInfo = DocumentMapper.getDocumentInfo(documentID);
                Role role = RoleMapper.get(roleID);
                
                // if we are able to look up the document and role, and this is 
                // either a published version or an unpublished version of the 
                // current player, send it.           
                if( docInfo != null && role != null && 
                    (published || (roleID == currentRoleID) ))
                {                    
                    DocumentVersion version = new DocumentVersion(versionID, docInfo.getTitle(), role.getName(), roleID, date, parentID, published );
                    addActionsToVersion(version);
                    documentVersionList.add(version);
                }
            }
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error converting discussion table: "+e);            
        }
        finally 
        {
            DBManager.instance.close(results);
            DBManager.instance.close(pstmt);      
        }
        
        return documentVersionList;        
    }

    /**
     * This method takes a move, generates its missing document versions and
     * alters the actions to point to the new DocumentVersion IDs.
     * 
     * @param move
     *          The move containing actions which may contain document versions
     *          that need to be created
     * @param documentVersionOriginsMap
     *          The Integer to Integer Map that maps new document versions to
     *          their parents
     * @param gameID
     *          The game for which this method generates document versions
     * @param publish
     *          Whether this is part of a published move or a saved move
     * @throws MapperException
     *          When new document versions can't be created, e.g. if
     *          documentVersionOriginsMap contains bad data
     */
    public static Map generateDocumentVersions(Move move, Map documentVersionOriginsMap, int gameID, boolean publish)
            throws MapperException
    {
        HashMap documentVersionIDMapping = new HashMap();
        
        // First we deal with added document actions
        BCollection addDocActions = move.getActionList()
                .filter(new ActionTypeFilter(ActionType.ADD_DOC_ACTION));
        
        for (Iterator i=addDocActions.iterator(); i.hasNext(); )
        {
            IvanhoeAction act = (IvanhoeAction)i.next();
            
            // We see if a document version with this ID already exists
            // (saved move)
            DocumentVersion thisVersion = getDocumentVersion(act.getDocumentVersionID());
            
            if ( thisVersion == null)
            {
                // If its ID isn't already here, create it
                thisVersion = createFirstVersion(
                        ((DocumentInfo)act.getContent()).getTitle(),
                        move, gameID, publish);
            }
            else
            {
                // This is where we'd update the document version, but there's
                // nothing updatable in add document actions at this point, and
                // the publish flag is getting set later.
            }
            
            documentVersionIDMapping.put(
                    new Integer(act.getDocumentVersionID()  ),
                    new Integer(thisVersion.getID()         ));
              
        }
        
        // Now we deal with revisions
        for (Iterator i=documentVersionOriginsMap.keySet().iterator(); i.hasNext(); )
        {
            Integer temporaryVersionID = ((Integer) i.next());
              
            if (temporaryVersionID.intValue() < 0)
            {
                Integer parentID = (Integer) documentVersionOriginsMap.get(temporaryVersionID);
                if (parentID == null)
                {
                    SimpleLogger.logError("DocumentVersion ID ["+temporaryVersionID+"] has no mapping.  Skipping it.");
                    break;
                }
                
                // Make sure the document version isn't already in the ID mapping
                if ( !documentVersionIDMapping.keySet().contains(temporaryVersionID) )
                {
                    DocumentVersion thisVersion =
                        createChildVersion(parentID.intValue(), move, gameID, publish);
                    
                    documentVersionIDMapping.put(temporaryVersionID, new Integer(thisVersion.getID()));
                } 
            }                    
        }
          
        return documentVersionIDMapping;
    }
    
    private static DocumentVersion createChildVersion(int parentID, Move containingMove,
            int gameID, boolean publish)
            throws MapperException
    {
        if (parentID <= 0)
        {
            throw new IllegalArgumentException("parentID ["+parentID+"] is"
                    +" invalid within move "+containingMove); 
        }
        
        DocumentVersion parentVersion = getDocumentVersion(parentID);
        
        // Create new document version
        Integer id;
        try {
            id = KeySequence.instance.getNewKey("document_version");
        } catch (SequenceException e1) {
            throw new RuntimeException(e1);
        }
        
        DocumentVersion documentVersion = new DocumentVersion(
                id.intValue(),
                parentVersion.getDocumentTitle(),
                containingMove.getRoleName(),
                containingMove.getRoleID(),
                (publish ? containingMove.getSubmissionDate() : new Date()),
                parentID,
                publish);
        
        writeDocumentVersion(documentVersion, gameID );
        
        return documentVersion;
    }
    
    private static DocumentVersion createFirstVersion(
            String docTitle, Move containingMove, int gameID, boolean publish)
            throws MapperException
    {
        if (docTitle == null || docTitle.length() == 0)
        {
            throw new IllegalArgumentException("docTitle is invalid"); 
        }
        
        // Create new document version
        Integer id;
        try {
            id = KeySequence.instance.getNewKey("document_version");
        } catch (SequenceException e1) {
            throw new RuntimeException(e1);
        }
        
        DocumentVersion documentVersion = new DocumentVersion(
                id.intValue(),
                docTitle,
                containingMove.getRoleName(),
                containingMove.getRoleID(),
                (publish ? containingMove.getSubmissionDate() : new Date()),
                DocumentVersion.NO_PARENT_ID,
                publish);
        
        writeDocumentVersion(documentVersion, gameID);
        
        return documentVersion;
    }
    
    public static DocumentVersion addStartingDocumentVersion(DocumentInfo docInfo, int gameID)
            throws MapperException
    {
        if (docInfo == null || docInfo.getTitle().length() == 0)
        {
            SimpleLogger.logError("Bad starting document info; cannot create document version");
            return null;
        }
        
        // Create new document version
        Integer id;
        try {
            id = KeySequence.instance.getNewKey("document_version");
        } catch (SequenceException e1) {
            throw new RuntimeException(e1);
        }
        
        DocumentVersion documentVersion = new DocumentVersion(
                id.intValue(),
                docInfo.getTitle(),
                Role.GAME_CREATOR_ROLE_NAME,
                Role.GAME_CREATOR_ROLE_ID,
                docInfo.getCreateTime(),
                DocumentVersion.NO_PARENT_ID,
                true);
        
        writeDocumentVersion(documentVersion, gameID);
        
        return documentVersion;
    }
    
    public static void publishDocumentVersions(Date submissionDate, Set documentVersions)
    {        
        for( Iterator i = documentVersions.iterator(); i.hasNext(); )
        {
            Integer id = (Integer) i.next();
            DocumentVersion version = null;
            
            try
            {
                version = getDocumentVersion(id.intValue());
            } 
            catch (MapperException e1)
            {
                SimpleLogger.logError("Unable to look up document version: "+id.intValue());
            }
            
            if( version != null && !version.isPublished() )
            {
                PreparedStatement pstmt = null;
                
                try 
                {
                    StringBuffer sql = new StringBuffer();
                    sql.append("update document_version set published=1, date=? where id=?");

                    pstmt = DBManager.instance.getConnection().prepareStatement(
                            sql.toString());
                    
                    pstmt.setString(1, DBManager.formatDate(submissionDate) );
                    pstmt.setInt(2, id.intValue());
                    pstmt.executeUpdate();                    
                } 
                catch (SQLException e) 
                {
                    SimpleLogger.logError("Unable to publish document version: " + id, e);
                }
                finally 
                {
                    DBManager.instance.close(pstmt);
                }
            }
        }
    }
    
    public static void deleteDocument(int documentID)
    {
        PreparedStatement pstmt = null;
        
        try {
            final String sql = 
                "DELETE FROM document_version WHERE fk_document_id=?";

            pstmt = DBManager.instance.getConnection().prepareStatement(sql);

            pstmt.setInt(1, documentID);
            pstmt.execute();
        } catch (SQLException e) {
            SimpleLogger.logError("Deletion of versions of document "
                    +documentID+" failed: " + e);
        } finally {
            DBManager.instance.close(pstmt);
        }
    }
}
