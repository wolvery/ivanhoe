/*
 * Created on Jul 19, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.server.mapper;

import java.awt.Color;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author Nick
 * 
 */
public class RoleMapper
{
   public static boolean hasRole(int gameID, int playerID)
      throws MapperException
   {
      PreparedStatement stmt = null;
      boolean roleFound = false;
      ResultSet results = null;

      try
      {
         String sqlCommand = "select * from player_game_role where fk_player_id=?" + 
         					 " and fk_game_id=?";
         stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
         stmt.setInt(1,playerID);
         stmt.setInt(2,gameID);
         results = stmt.executeQuery();
         
         roleFound = results.first();
      }
      catch (SQLException e)
      {
         throw new MapperException("Error looking up role for player: "
            + playerID + ": " + e.toString());
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);
      }

      return roleFound;
   }

   /**
    * Test if this role name is unique to the specified game. 
    * @param name Name to test
    * @param gameID Game to test
    * @return true if unique, false if not
    */
   public static boolean isNameUnique( String name, int gameID ) throws MapperException
   {
       PreparedStatement stmt = null, stmt2 = null;
       ResultSet results = null, results2 = null;

       try
       {
          // first find if there is a role by this name in the database
          String sqlCommand = "select id from role where name=?";
          stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
          stmt.setString(1,name);          
          results = stmt.executeQuery();
          
          if( results != null )
          {
              while( results.next() )
              {
                  int roleID = results.getInt("id");
                  
                  String sqlCommand2 = "select * from player_game_role where fk_game_id=? and " +
                  					   "fk_role_id = ?";
                  stmt2 = DBManager.instance.getConnection().prepareStatement(sqlCommand2);
                  stmt2.setInt(1,gameID);          
                  stmt2.setInt(2,roleID);
                  results2 = stmt2.executeQuery();
                 
                  // check if this role is in the current game, if so, return false
                  if( results2.next() )
                  {
                      return false;
                  }                  
              }
          }
          
          
       }
       catch (SQLException e)
       {
          throw new MapperException("Error checking name uniqueness: "
             + name + ": " + e.toString());
       }
       finally
       {
          DBManager.instance.close(results);
          DBManager.instance.close(results2);
          DBManager.instance.close(stmt);
          DBManager.instance.close(stmt2);
       }

       return true;
   }
   
   public static void updateRole(Role role) throws MapperException
   {
      PreparedStatement stmt = null;

      try
      {
         // insert the role info
         StringBuffer sql = new StringBuffer();
         sql.append("UPDATE role SET name = ?, ");
         sql.append(" description = ?, ");
         sql.append(" objectives = ?, ");
         sql.append(" stroke_rgb = ?, ");
         sql.append(" fill_rgb = ?");
         sql.append(" where id = ?");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());

         stmt.setString(1, role.getName());
         stmt.setString(2, role.getDescription());
         stmt.setString(3, role.getObjectives());
         stmt.setInt(4, role.getStrokePaint().getRGB());
         stmt.setInt(5, role.getFillPaint().getRGB());
         stmt.setInt(6, role.getId());
         stmt.executeUpdate();
      }
      catch (Exception e)
      {
         throw new MapperException("Unable to update role " + role.getName()
            + ": " + e.toString());
      }
      finally
      {
         DBManager.instance.close(stmt);
      }
   }

   public static Role newRole(String roleName, int playerID, int gameID, boolean writePermission)
      throws MapperException
   {
      Role role = null;
      PreparedStatement stmt = null, stmt2 = null;

      try
      {
         // get a new id for the role
         Integer id = KeySequence.instance.getNewKey("role");

         role = new Role(id.intValue(), roleName, writePermission);

         // insert the role info
         StringBuffer sql = new StringBuffer();
         sql.append("insert into role (");
         sql.append("id, name, description, objectives,");
         sql.append(" stroke_rgb, fill_rgb, write_permission)");;
         sql.append(" values (?, ?, ?, ?, ?, ?, ?);");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());

         stmt.setInt(1, role.getId());
         stmt.setString(2, role.getName());
         stmt.setString(3, "");
         stmt.setString(4, "");
         stmt.setInt(5, role.getStrokePaint().getRGB());
         stmt.setInt(6, role.getFillPaint().getRGB());
         stmt.setBoolean(7, role.hasWritePermission());
         stmt.executeUpdate();

         // associate role, player and game
         StringBuffer sql2 = new StringBuffer();
         sql2.append("insert into player_game_role (");
         sql2.append("fk_player_id, fk_game_id, fk_role_id )");
         sql2.append(" values (?, ?, ?);");
         stmt2 = DBManager.instance.getConnection().prepareStatement(
            sql2.toString());
         stmt2.setInt(1, playerID);
         stmt2.setInt(2, gameID);
         stmt2.setInt(3, id.intValue());
         stmt2.executeUpdate();
      }
      catch (Exception e)
      {
         throw new MapperException("Unable to create new role "
            + role.getName() + ": " + e.toString());
      }
      finally
      {
         DBManager.instance.close(stmt);
         DBManager.instance.close(stmt2);
      }

      return role;
   }

   /**
    * Retrieves the role with the given id.
    */
   public static Role get(int id) throws MapperException
   {
      // special case for creator role
      if( id == Role.GAME_CREATOR_ROLE_ID ) 
      {
          return Role.GAME_CREATOR_ROLE;
      }
      
      Role role = null;
      PreparedStatement stmt = null;
      ResultSet results = null;

      try
      {
         String sqlCommand = "SELECT * FROM role WHERE id=?";
         stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
         stmt.setInt(1,id);
         results = stmt.executeQuery();
         
         if (results.first())
         {
            String roleName = results.getString("name");
            int strokeRGB = results.getInt("stroke_rgb");
            int fillRGB = results.getInt("fill_rgb");
            String desc = results.getString("description");
            String obj = results.getString("objectives");
            boolean writePermission = results.getBoolean("write_permission");
            role = new Role(id, roleName, desc, obj, 
               new Color(strokeRGB), new Color(fillRGB), writePermission);
         }
      }
      catch (SQLException e)
      {
         throw new MapperException(
            "RoleMapper::get for id [" + id + "] failed", e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);
      }

      return role;
   }
 	/**
	 * Get the list of roles that are assoicated with the specified game.
	 * @param id The id of the game to look up.
	 * @return A list of Role objects.
	 * @throws MapperException
	 */
	public static List getGameRoles(int gameID) throws MapperException
	{	     
	     PreparedStatement stmt = null;
	     ResultSet results = null;
	     LinkedList roleList = null;
	      
	     try
	     {
	         String sqlCommand = "select fk_role_id from player_game_role where fk_game_id=?";
	         stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
	         stmt.setInt(1,gameID);	         
	         results = stmt.executeQuery();
	         
	         roleList = new LinkedList();	         
	         	         
	         while( results.next() )
	         {            
	            int roleID = results.getInt("fk_role_id");
	            Role role = get(roleID);
	            roleList.add(role);
	         } 
	      }
	      catch (Exception e)
	      {
	         throw new MapperException("Unable to get role for gameID["+ gameID + "]", e);
	      }
	      finally
	      {
	         DBManager.instance.close(results);
	         DBManager.instance.close(stmt);
	      }

	      return roleList;	
	}

	public static int getGameContainingRole(int roleID) throws MapperException
    {
	    PreparedStatement stmt = null;
        ResultSet results = null;
        int gameID = -1;
        
        try
        {
            String sqlCmd = "SELECT fk_game_id FROM player_game_role WHERE "
                + "fk_role_id = ?";
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCmd);
            stmt.setInt(1,roleID);
            results = stmt.executeQuery();
            
            if (results.next())
            {
                gameID = results.getInt("fk_game_id");
            }
        }
        catch (Exception e)
        {
            throw new MapperException("Unable to get gameID for roleID["+roleID+"]", e);
        }
        finally
        {
            DBManager.instance.close(results);
            DBManager.instance.close(stmt);
        }
        
        return gameID;
    }
}