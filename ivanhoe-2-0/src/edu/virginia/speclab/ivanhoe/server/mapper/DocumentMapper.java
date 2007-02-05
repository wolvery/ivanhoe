/*
 * Created on Jul 2, 2004
 *
 * DocumentMapper
 */
package edu.virginia.speclab.ivanhoe.server.mapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author lfoster
 *
 * Manage mapping from document info  <-> document table
 */
public class DocumentMapper
{

   /**
    * Add a new document record to the DB
    * @param newDocInfo
    * @return new document id
    * @throws MapperException
    */
   public static int add(final DocumentInfo newDocInfo) throws MapperException
   {
      PreparedStatement stmt = null;
      Integer id = new Integer(-1);
      
      try
      {
         // get a new id for the document
         id = KeySequence.instance.getNewKey("document");
         
         // insert the document info
         StringBuffer sql = new StringBuffer();
         sql.append("insert into document (");
         sql.append("id, file_name, title, author, ");
         sql.append(" publication_date, provenance, length, add_date, ");
         sql.append(" fk_contributor_id)");
         sql.append(" values (?, ?, ?, ?, ?, ?, ?, ?, ?);");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         stmt.setInt(1, id.intValue());
         stmt.setString(2, newDocInfo.getFileName());
         stmt.setString(3, newDocInfo.getTitle());
         stmt.setString(4, newDocInfo.getAuthor());
         //TODO remove publication date from the database schema 
         stmt.setString(5, "*deprecate*");
         stmt.setString(6, newDocInfo.getSource());
         stmt.setInt(7, newDocInfo.getDocumentLength());
         stmt.setString(8, DBManager.formatDate(newDocInfo.getCreateTime()) );
         stmt.setInt(9, newDocInfo.getContributorID());

         stmt.executeUpdate();
      }
      catch (Exception e)
      {
         SimpleLogger.logError("Unable to add document " + newDocInfo.getTitle() + 
            ": " + e.toString());
         throw new MapperException("Unable to add document " + newDocInfo.getTitle() + ": " + e.toString());
      }
      finally
      {
         DBManager.instance.close(stmt);
      }
      return id.intValue();
   }
   
   /**
    * Add a supporting image file to the document
    * @param parentInfo
    * @param imageFileName
    */
   public static void addImage(final DocumentInfo parentInfo, final String imageFileName)
      throws MapperException
   {
       // get a doc info from the database which has a valid id for this document
       DocumentInfo parentDBInfo = getDocumentInfo(parentInfo.getId().intValue());
       
       // the doc is not in the database
       if( parentDBInfo == null )
       {
           throw new MapperException("Parent document for image " + imageFileName + " not found, unable to add it to DF.");
       }

      int parentId = parentDBInfo.getId().intValue();
      PreparedStatement stmt = null;

      try
      {
         // insert the image
         StringBuffer sql = new StringBuffer();
         sql.append("insert into document_image (");
         sql.append("fk_document_id, file_name) ");
         sql.append("values (?, ?);");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         stmt.setInt(1, parentId);
         stmt.setString(2, imageFileName);
         stmt.executeUpdate();
      }
      catch (Exception e)
      {
         SimpleLogger.logError("Unable to add image to DB: " + e);
         throw new MapperException("Unable to add image to DB", e);
      }
      finally
      {
         DBManager.instance.close(stmt);
      }
   }
   
   /**
    * Get a list of supporting image filenames for the given document
    * @param documentId
    * @return
    */
   public static List getDocumentImages(final int documentId) throws MapperException
   {
      List list = new ArrayList();
      PreparedStatement stmt = null;
      ResultSet results = null;
      try
      {
         // insert the image
         StringBuffer sql = new StringBuffer();
         sql.append("select file_name from document_image ");
         sql.append("where fk_document_id = ?");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         stmt.setInt(1, documentId);
         results = stmt.executeQuery();
         while (results.next())
         {
            list.add( results.getString("file_name"));
         }
      }
      catch (Exception e)
      {
         SimpleLogger.logError("Unable to get images for document " + documentId + e);
         throw new MapperException("Unable to get images for document " + documentId, e);
      }
      finally
      {
         DBManager.instance.close(stmt);
      }
      return list;
   }   
   
   /**
    * Get documentInfo for the given document title
    * @return DocumentInfo
    */
   public static DocumentInfo getDocumentInfo(final String docTitle, final int gameId)
   {
      DocumentInfo info = null;
      PreparedStatement pstmt = null;
      ResultSet results = null;

      try
      {
         StringBuffer sql = new StringBuffer();
         sql.append("SELECT t1.* FROM document AS t1, discourse_field AS t2 "
                 + "WHERE t1.title = ? "
                 + "AND t2.fk_game_id = ? "
                 + "AND t1.id = t2.fk_document_id");
         pstmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         pstmt.setString(1, docTitle);
         pstmt.setInt(2, gameId);
         results = pstmt.executeQuery();
         if (results.first())
         {
            info = extractDocInfo(results);
         }
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Unable to get Document Info " + e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(pstmt);
      }

      return info;
   }
   
   /**
    * Helper method to extract doc info from a valid resultSet
    * @param results
    * @return
    * @throws SQLException
    */
   private static DocumentInfo extractDocInfo(ResultSet results) throws SQLException
   {
      int id = results.getInt("id");
      String file = results.getString("file_name");
      String title = results.getString("title");
      String author = results.getString("author");
      String provenance = results.getString("provenance");
      int length = Integer.parseInt(results.getString("length"));
      Date addDate = DBManager.parseDate(results.getString("add_date"));
      int contributorId = results.getInt("fk_contributor_id");
      String contributor = "";
      if (contributorId > 0)
      {         
         try
         {
            Role role = RoleMapper.get(contributorId);
            contributor = role.getName(); 
         }
         catch (MapperException e1)
         {
            SimpleLogger.logError("Unable to get contributor; Leaving as System"); 
         }
      }
      DocumentInfo info = new DocumentInfo(id, file, title, author, provenance, contributor, contributorId, addDate);
      info.setDocumentLength(length);
      return info;
   }

   /**
    * Get documentInfo for the given document id
    * @return DocumentInfo
    */
   public static DocumentInfo getDocumentInfo(final int id)
   {
      DocumentInfo info = null;
      PreparedStatement pstmt = null;
      ResultSet results = null;

      try
      {
         StringBuffer sql = new StringBuffer();
         sql.append("select * from document ");
         sql.append("where document.id = ").append(id);
         pstmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         results = pstmt.executeQuery();
         if (results.first())
         {
            info = extractDocInfo(results);
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

      return info;
   }

   /**
    * remove doc with the give ID
    * @param title
    */
   public static void delete(final int id)
   {
      PreparedStatement stmt = null;
      PreparedStatement stmt2 = null;
      try
      {
         String sqlCommand = "delete from document where id = ?";
         stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
         stmt.setInt(1,id);
         stmt.executeUpdate();

         String sqlCommand2 = "delete from document_image where fk_document_id = ?";
         stmt2 = DBManager.instance.getConnection().prepareStatement(sqlCommand2);
         stmt2.setInt(1,id);
         stmt2.executeUpdate();
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Error removing documentID " + id + ": " + e.toString());
      }
      finally
      {
         DBManager.instance.close(stmt);
         DBManager.instance.close(stmt2);
      }
   }
   
   public static boolean fileExists(String filename, int gameId)
   {
       PreparedStatement pstmt = null;
       ResultSet results = null;
       boolean exists = false;
       
       try
       {
          StringBuffer sql = new StringBuffer();
          sql.append("SELECT * FROM discourse_field "
                  + "AS t1, document AS t2 "
                  + "WHERE t1.fk_document_id = t2.id "
                  + "AND t2.file_name = ? AND t1.fk_game_id = ?");
          pstmt = DBManager.instance.getConnection().prepareStatement(
             sql.toString());
          pstmt.setString(1, filename);
          pstmt.setInt(2, gameId);
          SimpleLogger.logInfo("Checking existance of document with filename ["
                  +filename+"] in game "+gameId);
          results = pstmt.executeQuery();
          if (results.first())
          {
             SimpleLogger.logInfo("A document with this filename already exists in this game");
             exists = true;
          }
       }
       catch (SQLException e)
       {
          SimpleLogger.logError("Unable to check if file exists " + e);
       }
       finally
       {
          DBManager.instance.close(results);
          DBManager.instance.close(pstmt);
       }
       return exists;
   }
   
   /**
    * Check if a document with this title already exists
    */
   public static boolean exists(String title, int gameId)
   {
      PreparedStatement pstmt = null;
      ResultSet results = null;
      boolean exists = false;
      
      try
      {
         StringBuffer sql = new StringBuffer();
         sql.append("SELECT * FROM discourse_field "
                 + "AS t1, document AS t2 "
                 + "WHERE t1.fk_document_id = t2.id "
                 + "AND t2.title = ? AND t1.fk_game_id = ?");
         pstmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         pstmt.setString(1, title);
         pstmt.setInt(2, gameId);
         SimpleLogger.logInfo("Checking existance of document with title \""
                 +title+"\" in game "+gameId);
         results = pstmt.executeQuery();
         if (results.first())
         {
            SimpleLogger.logInfo("A document with this title already exists in this game");
            exists = true;
         }
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Unable to check if file exists " + e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(pstmt);
      }
      return exists;
   }
}
