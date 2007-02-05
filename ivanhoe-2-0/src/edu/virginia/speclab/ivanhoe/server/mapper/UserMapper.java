/*
 * Created on Oct 7, 2003
 *
 * UserMapper
 */
package edu.virginia.speclab.ivanhoe.server.mapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.shared.Encryption;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.User;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author lfoster
 *
 * Handles read/write/update for player data <-> DB
 */

//TODO: make this class static, it has no state

public class UserMapper
{  

   public static User getUserForRole( int roleID ) throws MapperException
   {
       User user = null;
       PreparedStatement stmt = null;
       ResultSet results = null;
       
       try
       {
          String sqlCommand = "SELECT fk_player_id FROM player_game_role WHERE fk_role_id=?";
          stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
          stmt.setInt(1,roleID);
          results = stmt.executeQuery();
          
          if (results.first())
          {
              int userID = results.getInt("fk_player_id");
              user = get(userID);
          }
       }
       catch (SQLException e)
       {            
          throw new MapperException("UserMapper::get user for role id [" + 
             roleID + "] failed", e);
       }
       finally
       {
          DBManager.instance.close(results);
          DBManager.instance.close(stmt);  
       }
       
       return user;
   }
   

   public static User get(int id) throws MapperException
   {
      User user = null;
      PreparedStatement stmt = null;
      ResultSet results = null;
      
      try
      {
         String sqlCommand = "SELECT * FROM player WHERE id=?";
         stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
         stmt.setInt(1,id);
         results = stmt.executeQuery();
         
         if (results.first())
         {
            user = new User(
               results.getInt("id"),
               results.getString("playername"),
               results.getString("password"),
               results.getString("lname"),
               results.getString("fname"),
               results.getString("email"),
               results.getString("affiliation"),
               results.getBoolean("new_game_permission")
            );
         }
      }
      catch (SQLException e)
      {            
         throw new MapperException("UserMapper::get for id [" + 
            id + "] failed", e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);  
      }
      
      return user;
   }
   
   public static User get(String name) throws MapperException
   {
      final User user;
      PreparedStatement stmt = null;
      ResultSet results = null;
      
      try
      {
         String sqlCommand = "SELECT * FROM player WHERE playername=?";
         stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
         stmt.setString(1,name);
         results = stmt.executeQuery();
         
         if (results.first())
         {
            user = new User(
               results.getInt("id"),
               results.getString("playername"),
               results.getString("password"),
               results.getString("lname"),
               results.getString("fname"),
               results.getString("email"),
               results.getString("affiliation"),
               results.getBoolean("new_game_permission")
            );
         }
         else
         {
            user = null;
         }
      }
      catch (SQLException e)
      {            
         throw new MapperException("UserMapper::get for playername [" + 
            name + "] failed", e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);  
      }
      
      return user;
   }
   
   /**
    * Get a list of all users authorized for a specific game of ivanhoe
    * @param gameId the ID of the desired game 
    * @return List of Strings containing all players in the game  
    * @throws MapperException
    */
   public static List getAllUserNames(int gameId) throws MapperException
   {
      List list = new ArrayList();
      PreparedStatement stmt = null;
      ResultSet results = null;
      
      try
      {
         String sqlCommand = "SELECT playername FROM player,player_game " +
            				 "WHERE fk_player_id = player.id AND fk_game_id = ?"; 
         stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
         stmt.setInt(1,gameId);
         results = stmt.executeQuery();
         
         while (results.next())
         {
             String playerName = results.getString("playername"); 
             if( !list.contains(playerName))
             {
                 list.add(playerName);
             }
         }
      }
      catch (SQLException e)
      {            
         SimpleLogger.logError("getAllUserNames for " + gameId + " failed: "+ e);
         throw new MapperException("Unable to get game user list: " + e.toString());
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);  
      }
      
      return list;
   }
   
   /**
    * Get a list of all users register to play ivanhoe
    * @return List of Strings containing login names of all the players
    * @throws MapperException
    */
   public static List getAllUserNames() throws MapperException
   {
      List list = new ArrayList();
      Statement stmt = null;
      ResultSet results = null;
      
      try
      {
         stmt = DBManager.instance.getConnection().createStatement();
         results = stmt.executeQuery("SELECT playername FROM player" );
         while (results.next())
         {
            list.add(results.getString("playername"));
         }
      }
      catch (SQLException e)
      {            
         SimpleLogger.logError("getAllUserNames failed: "+ e);
         throw new MapperException("Unable to get user list: " + e.toString());
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);  
      }
      
      return list;
   }
   
   /**
    * Get a list of all user IDs registered to play ivanhoe
    * @return List of Integers containing IDs of all users in DB
    * @throws MapperException
    */
   public static List getAllUserIDs() throws MapperException
   {
      List list = new ArrayList();
      Statement stmt = null;
      ResultSet results = null;
      
      try
      {
         stmt = DBManager.instance.getConnection().createStatement();
         results = stmt.executeQuery("SELECT id FROM player" );
         while (results.next())
         {
            list.add(new Integer(results.getInt("id")));
         }
      }
      catch (SQLException e)
      {            
         SimpleLogger.logError("getAllUserIDs failed: "+ e);
         throw new MapperException("Unable to get user list: " + e.toString());
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);  
      }
      
      return list;
   }

   
   /**
    * Finds all users in the game with the given email address
    * @param email Email address of the user to find
    * @return A List containing all the User objects founds
    * @throws MapperException
    */
   public static List getByEmail(String email) throws MapperException
   {
      List list = new ArrayList();
      User user = null;
      Statement stmt = null;
      ResultSet results = null;
      
      try
      {
         String sql = "SELECT * FROM player WHERE email='" + email + "'";
         stmt = DBManager.instance.getConnection().createStatement();
         results = stmt.executeQuery(sql);
         while (results.next())
         {
            user = new User(
               results.getInt("id"),
               results.getString("playername"),
               results.getString("password"),
               results.getString("lname"),
               results.getString("fname"),
               results.getString("email"),
               results.getString("affiliation"),
               results.getBoolean("new_game_permission")
            );
            
            list.add(user);
         }
      }
      catch (SQLException e)
      {            
         throw new MapperException("Unable to get info for user [" + 
            email + "] failed", e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);  
      }
      
      return list;
   }
   
   public static User getByName(String userName) throws MapperException
   {      
      User user = null;
      PreparedStatement stmt = null;
      ResultSet results = null;
      
      try
      {
         StringBuffer sql = new StringBuffer("SELECT * FROM player WHERE playername=?");          
         stmt = DBManager.instance.getConnection().prepareStatement(sql.toString());
         stmt.setString(1, userName );
         results = stmt.executeQuery();

         if (results.first())
         {
            user = new User(
               results.getInt("id"),
               results.getString("playername"),
               results.getString("password"),
               results.getString("lname"),
               results.getString("fname"),
               results.getString("email"),
               results.getString("affiliation"),
               results.getBoolean("new_game_permission")
            );
         }
      }
      catch (SQLException e)
      {            
         throw new MapperException("Unable to get info for user [" + 
            userName + "] failed", e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);  
      }
      
      return user;
   }
   
   /**
    * Resets the password for a given user.  The new password should
    * be set in the User object.
    * @param newUserData A User object containing the encrypted password
    * @throws MapperException
    */
   public static void resetPassword(User newUserData) throws MapperException
   {
      PreparedStatement stmt = null;

      try
      {
         // insert the document info
         StringBuffer sql = new StringBuffer();
         sql.append("UPDATE player SET password=? ");
         sql.append("WHERE id=?");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         stmt.setString(1, newUserData.getPassword());
         stmt.setInt(2, newUserData.getId());

         stmt.executeUpdate();
      }
      catch (Exception e)
      {
         throw new MapperException("Unable to update password for " + newUserData.getUserName() + 
            ": " + e.toString());
      }
      finally
      {
         DBManager.instance.close(stmt);
      }
   }

   /**
    * Create a new account
    * @param newUserData
    */
   public static boolean createAccount(User newUserData) throws MapperException
   {
      PreparedStatement stmt = null;

      final User existingUser = get(newUserData.getUserName());
      if (existingUser == null)
      {
          try
          {
             // get a new id for the document
             Integer id = KeySequence.instance.getNewKey("player");
             
             // insert the document info
             String sql = "INSERT INTO player " +
                    "(id, playername, password, fname, lname, email, " +
                    "affiliation, new_game_permission, new_role_permission, write_permission, admin)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
             stmt = DBManager.instance.getConnection().prepareStatement(
                sql);
             int i=0;
             stmt.setInt(++i, id.intValue());
             stmt.setString(++i, newUserData.getUserName());
             stmt.setString(++i, Encryption.createMD5HashCode(newUserData.getPassword()));
             stmt.setString(++i, newUserData.getFirstName());
             stmt.setString(++i, newUserData.getLastName());
             stmt.setString(++i, newUserData.getEmail());
             stmt.setString(++i, newUserData.getAffiliation());
             stmt.setBoolean(++i, newUserData.getNewGamePermission());
             stmt.setBoolean(++i, newUserData.getNewRolePermission());
             stmt.setBoolean(++i, newUserData.getWritePermission());
             stmt.setBoolean(++i, newUserData.isAdmin());
             stmt.executeUpdate();
          }
          catch (Exception e)
          {
             throw new MapperException("Unable to create new account " + newUserData.getUserName() + 
                ": " + e.toString());
          }
          finally
          {
             DBManager.instance.close(stmt);
          }

          return true;
      }
      else
      {
          return false;
      }
   }
}
