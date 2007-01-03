/*
 * Created on Jan 4, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.server.mapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.shared.data.ReferenceResource;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author Nick
 *
 */
public class ReferenceResourceMapper
{
    
   public static List getReferences( int gameID )
      throws MapperException
   {
      LinkedList references = new LinkedList();
      
      PreparedStatement stmt = null;
      ResultSet results = null;

      try
      {
         String sqlCommand = "select * from bookmarks where fk_game_id=?";
         stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
         stmt.setInt(1,gameID);
         results = stmt.executeQuery();
         
         while( results.next() )
         {
             String label = results.getString("label");
             String url = results.getString("url");
             String summary = results.getString("summary");
             
             ReferenceResource bookmark = new ReferenceResource( label, url, summary );
             references.add(bookmark);
         }
         
      }
      catch (SQLException e)
      {
         throw new MapperException("Error looking up resource references for game: "
            + gameID + ": " + e.toString());
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);
      }

      return references;
   }

   public static void updateReferences( List references, int gameID ) throws MapperException
   {
      PreparedStatement stmt = null, stmt2 = null;

      try
      {
         // remove old bookmarks
         String sql = "DELETE FROM bookmarks WHERE fk_game_id = ?";
         stmt = DBManager.instance.getConnection().prepareStatement(sql.toString());
         stmt.setInt(1,gameID);
         stmt.executeUpdate();
    
         // add new references         
         for( Iterator i = references.iterator(); i.hasNext(); )
         {
             ReferenceResource reference = (ReferenceResource) i.next();
             String label = reference.getLabel();
             String url = reference.getUrl();
             String summary = reference.getSummary();
             
             StringBuffer sql2 = new StringBuffer();
             sql2.append("INSERT INTO bookmarks (");
             sql2.append("id, fk_game_id, label, url, summary)");
             sql2.append(" VALUES (?, ?, ?, ?, ?);");

             stmt2 = DBManager.instance.getConnection().prepareStatement(sql2.toString());
             
             Integer id = KeySequence.instance.getNewKey("bookmarks");

             stmt2.setInt(1, id.intValue());
             stmt2.setInt(2, gameID);
             stmt2.setString(3, label);
             stmt2.setString(4, url);
             stmt2.setString(5, summary);
             stmt2.executeUpdate();
         }
      }
      catch (Exception e)
      {
         throw new MapperException("Unable to update resource references for game " + gameID
            + ": " + e.toString());
      }
      finally
      {
         DBManager.instance.close(stmt);
         DBManager.instance.close(stmt2);
      }
   }
}