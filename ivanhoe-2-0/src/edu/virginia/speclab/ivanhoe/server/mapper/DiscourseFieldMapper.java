/*
 * Created on Jan 9, 2004
 *
 * DiscourseFieldMapper
 */
package edu.virginia.speclab.ivanhoe.server.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.List;
import java.util.Vector;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.shared.IvanhoeException;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author lfoster
 * 
 * Handles read/write/update for DiscourseField <->DB
 */
public class DiscourseFieldMapper
{
   /**
    * Add a starting document to the discourseField
    * @param gameId Id of game
    * @param docTitle title of document to add
    * @throws MapperException
    */
   public void addStartingDocument(final String gameName, final String docTitle, final int docID)
      throws MapperException
   {
      PreparedStatement stmt = null;

      try
      {  
         // Get game info
         SimpleLogger.logInfo("Looking up info for game ["+gameName+"]");
         GameMapper gameMapper  = new GameMapper();
         GameInfo gameInfo = gameMapper.get(gameName);
         if (gameInfo == null)
         {
            throw new MapperException("Invalid game name " + gameName);
         }
         
         // get document info
         SimpleLogger.logInfo("Looking up info for document "+docTitle);
         
         // insert Ids into discourseField table
         StringBuffer sql = new StringBuffer();
         sql.append("INSERT INTO discourse_field (");
         sql.append("fk_game_id, fk_document_id, starting_doc, published_doc) ");
         sql.append(" values (?,?,?,?);");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         stmt.setInt(1, gameInfo.getId());
         stmt.setInt(2, docID);
         
         // starting and published
         stmt.setBoolean(3, true);
         stmt.setBoolean(4, true);
         
         stmt.executeUpdate();
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Unable to add doc to discourse field: " + e);
         throw new MapperException("Unable to add document ["+docTitle+ 
            "] to discourse field");
      }
      finally
      {
         DBManager.instance.close(stmt);
      }
   }
   
   /**
    * Insert a new Document into the Pending Document table. It will be commited
    * to the Discourse Field when the move that added it is submitted.
    * @param obj DocumentInfo for the new document
    */
   public void addPendingDocument(final String gameName, final String docTitle, final int docID) 
      throws MapperException
   {
      PreparedStatement stmt = null;

      try
      {  
         // Get game info
         SimpleLogger.logInfo("Looking up info for game " + gameName);
         GameMapper gameMapper  = new GameMapper();
         GameInfo gameInfo = gameMapper.get(gameName);
         if (gameInfo == null)
         {
            throw new MapperException("Invalid game name " + gameName);
         }
         
         // get document info
         SimpleLogger.logInfo("Looking up info for pending document " + docTitle);
         
         // insert Ids into discourseField table
         StringBuffer sql = new StringBuffer();
         sql.append("insert into discourse_field (");
         sql.append("fk_game_id, fk_document_id, starting_doc, published_doc) ");
         sql.append(" values (?,?,?,?);");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         stmt.setInt(1, gameInfo.getId());
         stmt.setInt(2, docID);
         
         // pendind documens are not starting nor published
         stmt.setBoolean(3, false);
         stmt.setBoolean(4, false);
         
         stmt.executeUpdate();
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Unable to add pending doc to discourse field: " + e);
         throw new MapperException("Unable to add pending document " + docTitle + 
            " to discourse field");
      }
      finally
      {
         DBManager.instance.close(stmt);
      }
   }

   /**
    * Get a vector of DocumentInfo objects for all the documents available in
    * this game (not including pending additions)
    * @return Vector of DocumentInfo
    */
   public Vector getDocumentList(final int gameId)
   {
      Vector list = new Vector();
      PreparedStatement pstmt = null;
      ResultSet results = null;

      try
      {
         StringBuffer sql = new StringBuffer();
         sql.append("select * from discourse_field ");
         sql.append("where published_doc = 1 and fk_game_id = ?");
         pstmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         pstmt.setInt(1,gameId);
         results = pstmt.executeQuery();
         
         while (results.next())
         {
            int docId = results.getInt("fk_document_id");
            DocumentInfo di = DocumentMapper.getDocumentInfo(docId);
            if (di != null)
            {
               di.setPublishedDocument( results.getBoolean("published_doc"));
               di.setStartingDocument( results.getBoolean("starting_doc"));
               list.add( di );
            }
            else
            {
                SimpleLogger.logError("Problem looking up document "+docId,
                        new IvanhoeException("Could not find document for documentID "+docId));
            }
         }
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Unable to get DF Doc list " + e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(pstmt);
      }

      return list;
   }
   
   /**
    * Return a list of all pending documents
    * @param int gameID
    *       The game number in which the documents and role exist
    * @param roleName
    *       The role to whom the documents belong
    * @return
    */
   public List getPendingDocuments(final int gameID, final String roleName)
   {
      Vector list = new Vector();
      PreparedStatement pstmt = null;
      ResultSet results = null;

      try
      {
         StringBuffer sql = new StringBuffer();
         sql.append("SELECT * FROM discourse_field ");
         sql.append("WHERE published_doc = 0 AND fk_game_id = ?");
         pstmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         pstmt.setInt(1,gameID);
         results = pstmt.executeQuery();

         while (results.next())
         {
            int docId = results.getInt("fk_document_id");
            DocumentInfo di = DocumentMapper.getDocumentInfo(docId);
            if (di != null)
            {
               di.setStartingDocument(results.getBoolean("starting_doc"));
               di.setPublishedDocument(results.getBoolean("published_doc"));
               
               //  only add valid, unpublished documents 
               // created by <playerName> to the list
               if (di != null && di.getContributor().equals(roleName))
               {
                  list.add( di );
               }
            }
            else
            {
               SimpleLogger.logError("Request for documentID " + docId + 
                  " failed. Not adding to pending doc list"); 
            }
         }
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Unable to get DF Doc list " + e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(pstmt);
      }

      return list;
   }

   /**
    * @param string
    */
   public void commitDocumentAdds(final int gameId, final String playerName)
   {
      PreparedStatement pstmt = null;
      ResultSet results = null;

      try
      {
         StringBuffer sql = new StringBuffer();
         sql.append("select * from discourse_field ");
         sql.append("where published_doc = 0 ");
         sql.append(" and fk_game_id = ?");
         pstmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE );
         pstmt.setInt(1,gameId);
         results = pstmt.executeQuery();

         while (results.next())
         {
            int docId = results.getInt("fk_document_id");
            DocumentInfo di = DocumentMapper.getDocumentInfo(docId);
            di.setStartingDocument(results.getBoolean("starting_doc"));
            di.setPublishedDocument(results.getBoolean("published_doc"));
            
            //  update published status of docs by the specified player
            if (di != null && di.getContributor().equals(playerName))
            {
               results.updateBoolean("published_doc", true);
               results.updateRow();
            }
         }
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Unable to get DF Doc list " + e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(pstmt);
      }
   }

   /**
    * Delete a document with the matching title within a game
	* @param title Title of document to be deleted
	* @param gameID the ID number of the game from which title is to be deleted
    */
   public void deleteDocument(final String title, final int gameID)
   {
      PreparedStatement stmt = null;

      try
      {
         // delete the document
         DocumentInfo docInfo = DocumentMapper.getDocumentInfo(title, gameID);
         
         if( docInfo == null )
         {
             SimpleLogger.logError("Document "+title+" is not in the DB and therefore cannot be deleted.");
             return;
         }
         else if (docInfo.isPublishedDocument())
         {
             SimpleLogger.logError("Cannot delete published document "+title);
             return;
         }
          
         int id = docInfo.getId().intValue();
         DocumentMapper.delete(id);
         DocumentVersionMapper.deleteDocument(id);
         
         // delete reference to from DF
         String sqlCommand = "delete from discourse_field where fk_document_id = ?";         
         stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
         stmt.setInt(1,id);
         stmt.executeUpdate();            
      }
      catch (Exception e)
      {
         SimpleLogger.logError("Unable to delete document:"+ title+" Reason:"+ e);
      }
      finally
      {
         DBManager.instance.close(stmt);
      }
   }
   
   public int getGameContainingDocument(int documentID)
   {
       PreparedStatement pstmt = null;
       ResultSet results = null;
       int gameID = -1;
       
       try
       {
          StringBuffer sql = new StringBuffer();
          sql.append("SELECT * FROM discourse_field "
                  + "WHERE fk_document_id = ? ");
          pstmt = DBManager.instance.getConnection().prepareStatement(
             sql.toString());
          pstmt.setInt(1, documentID);
          SimpleLogger.logInfo("Looking up game for document "+documentID);
          results = pstmt.executeQuery();
          if (results.first())
          {
             
          }
       }
       catch (SQLException e)
       {
          SimpleLogger.logError("Unable to find game containing document "+documentID, e);
       }
       finally
       {
          DBManager.instance.close(results);
          DBManager.instance.close(pstmt);
       }
       return gameID;
   }

}