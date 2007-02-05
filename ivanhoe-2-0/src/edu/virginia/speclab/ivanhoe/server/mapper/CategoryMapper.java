/*
 * Created on Jan 14, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.server.mapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.shared.data.Category;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author Nick
 *
 */
public class CategoryMapper
{
    public static List getCategories( int gameID )
       throws MapperException
    {
       LinkedList categoryList = new LinkedList();
       
       PreparedStatement stmt = null;
       ResultSet results = null;

       try
       {
          String sqlCommand = "select * from category where fk_game_id=?";
          stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
          stmt.setInt(1,gameID);
          results = stmt.executeQuery();
          
          while( results.next() )
          {
              int id = results.getInt("id");
              String name = results.getString("name");
              String description = results.getString("description");              
              Category category = new Category(id,name,description);
              categoryList.add(category);
          }
          
       }
       catch (SQLException e)
       {
          throw new MapperException("Error looking up categories for game: "
             + gameID + ": " + e.toString());
       }
       finally
       {
          DBManager.instance.close(results);
          DBManager.instance.close(stmt);
       }

       return categoryList;
    }

    public static void addCategory( Category category, int gameID ) throws MapperException
    {
       PreparedStatement stmt = null;

       try
       {
          StringBuffer sql = new StringBuffer();
          sql.append("INSERT INTO category (");
          sql.append("id, name, description, fk_game_id)");
          sql.append(" VALUES (?, ?, ?, ?);");

          stmt = DBManager.instance.getConnection().prepareStatement(sql.toString());
          
          Integer id = KeySequence.instance.getNewKey("category");

          stmt.setInt(1, id.intValue());
          stmt.setString(2, category.getName());
          stmt.setString(3, category.getDescription());
          stmt.setInt(4, gameID);
          stmt.executeUpdate();
       }
       catch (Exception e)
       {
          throw new MapperException("Unable to update categories for game " + gameID
             + ": " + e.toString());
       }
       finally
       {
          DBManager.instance.close(stmt);
       }
    }

}
