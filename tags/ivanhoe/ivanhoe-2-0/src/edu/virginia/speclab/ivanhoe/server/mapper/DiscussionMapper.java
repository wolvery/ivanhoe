/*
 * Created on Mar 11, 2004
 *
 * DiscussionMapper
 */
package edu.virginia.speclab.ivanhoe.server.mapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.exception.SequenceException;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DiscussionEntry;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author lfoster
 *
 * Maps DiscussionEntry data to/from the database
 */
public class DiscussionMapper
{
   private int gameId;
     
   /**
    * Constructs an instance of the ScoliaMapper for the given game
    * @param gameId
    */ 
   public DiscussionMapper(int gameId)
   {
      this.gameId = gameId;
   }

   /**
    * Insert a new ScholiaEntry into the DB
    */
   public boolean insert(DiscussionEntry entry)
   {
      // attempt to get an ID for the new entry
      Integer id;
      try
      {
         id  = KeySequence.instance.getNewKey("discussion");
      }
      catch (SequenceException e1)
      {
         SimpleLogger.logError("Unable to get unique ID for new discussion");
         return false;
      }

      PreparedStatement pstmt = null;
      boolean success = false;
      
      // write the action record
      try
      {
         StringBuffer sql = new StringBuffer();
         sql.append("insert into discussion (");
         sql.append("id, fk_role_id, fk_game_id, title, ");
         sql.append("message, post_date, parent_id)");
         sql.append(" values (?, ?, ?, ?, ?, ?, ?);");
         
         pstmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());

         pstmt.setInt(1, id.intValue());
         pstmt.setInt(2, entry.getRoleID() );
         pstmt.setInt(3, this.gameId);
         pstmt.setString(4, entry.getTitle());
         pstmt.setString(5, entry.getMessage());
         pstmt.setString(6, DBManager.formatDate(entry.getPostingDate()) );
         pstmt.setInt(7, entry.getParentId() );

         pstmt.executeUpdate();
         success = true; 
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Insert of discussion failed: " + e);
      }
      finally
      {
         DBManager.instance.close(pstmt);  
      }
      
      return success;
   }

   /**
    * Get the ScholiaEntry with the matching ID
    */
   public DiscussionEntry get(int id) throws MapperException
   {
      DiscussionEntry entry = null;
      
      PreparedStatement stmt = null;
      ResultSet results = null;
      
      try
      {         
         int roleID;
         String title, msg;
         Date postDate;
         
         String sqlCommand = "select * from discussion where fk_game_id = ?";
         stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
         stmt.setInt(1,this.gameId);
         results = stmt.executeQuery();

         if (results.first())
         {
            roleID = results.getInt("fk_role_id");  
            title = results.getString("title");
            msg = results.getString("msg");
            postDate = DBManager.parseDate(results.getString("post_date"));
//            parentId = results.getInt("parent_id"); 
            
            // add a move to the history
            entry =  new DiscussionEntry(id, title, roleID, postDate, msg);   
         }
         
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Unable to discussion " + e);
         throw new MapperException("Unable to get discussion list", e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);
      }
      
      return entry;
   }
   
   /**
    * Get a list of all discussion entries
    * @return List containing all discussion for a game
    */
   public List getAllEntries() throws MapperException
   {
      ArrayList list = new ArrayList();
      PreparedStatement stmt = null;
      ResultSet results = null;
      
      try
      {         
         int id, roleId;
         String title, msg;
         Date postDate;
         
         String sqlCommand = "select * from discussion where fk_game_id = ?";
         stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
         stmt.setInt(1,this.gameId);
         results = stmt.executeQuery();

         while (results.next())
         {
            id = results.getInt("id");
            roleId = results.getInt("fk_role_id");  
            title = results.getString("title");
            msg = results.getString("message");
            postDate = DBManager.parseDate(results.getString("post_date"));
 //           parentId = results.getInt("parent_id"); 
            
            // add a move to the history
            list.add( new DiscussionEntry(id, title, roleId, postDate, msg) );   
         }
         
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Unable to discussion " + e);
         throw new MapperException("Unable to get discussion list", e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);
      }
      
      return list;
   }
}
