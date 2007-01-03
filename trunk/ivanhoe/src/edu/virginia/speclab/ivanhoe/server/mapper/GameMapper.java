/*
 * Created on Jun 28, 2004
 *
 * GameMapper
 */
package edu.virginia.speclab.ivanhoe.server.mapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.*;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author lfoster
 *
 * Manage mapping of GameInfo <-> DB
 */
public class GameMapper
{
   /**
    * create a new game
    * @param newInfo
    */
   public int create(GameInfo newInfo) throws MapperException
   {
      PreparedStatement stmt = null;

      Integer id = new Integer(-1);
      
      try
      {
         // get a new id for the game
         id = KeySequence.instance.getNewKey("game");
         
         // grab creator info
         User player = UserMapper.getByName(newInfo.getCreator());
         
         // insert the info
         StringBuffer sql = new StringBuffer();
         sql.append("INSERT INTO game");
         sql.append(" (id, fk_creator_id, name, description, objectives, restricted, archived, startDocWeight)");
         sql.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         stmt.setInt(1, id.intValue());
         stmt.setInt(2, player.getId());
         stmt.setString(3, newInfo.getName());
         stmt.setString(4, newInfo.getDescription());
         stmt.setString(5, newInfo.getObjectives());
         stmt.setBoolean(6, newInfo.isRestricted());
         stmt.setBoolean(7, newInfo.isArchived());
         stmt.setInt(8, newInfo.getStartDocWeight());
         stmt.executeUpdate();
      }
      catch (Exception e)
      {
         SimpleLogger.logError("Unable to create game: ", e);
         throw new MapperException("Unable to create game: " + e.toString());
      }
      finally
      {
         DBManager.instance.close(stmt);
      }
      
      return id.intValue();
   }
   
   /**
    * Add an accesslist to a game
    * @param gameName
    * @param players
    * @throws MapperException
    */
   public void addAccessList(String gameName, List players) throws MapperException
   {
      // lookup game info for gameName
      GameInfo gameInfo = null;
      try
      {
         gameInfo = get(gameName);
      }
      catch (MapperException e)
      {
         throw new MapperException("Invalid gamename " +  gameName + " passed to addAccessList");
      }
      
      // loop thru the player list and add each one to the game access list
      for (Iterator itr = players.iterator(); itr.hasNext();)
      {
         // grab player info
         String name = (String)itr.next();
         SimpleLogger.logInfo("Adding " + name + " to access list for " + gameInfo.getName());
         
         PreparedStatement stmt = null;
         try
         {  
            // lookup the player
            User player = UserMapper.getByName(name);
            
            // add player to access list
            StringBuffer sql = new StringBuffer();
            sql.append("INSERT INTO player_game (");
            sql.append("fk_player_id, fk_game_id)");
            sql.append(" VALUES (?, ?);");
            stmt = DBManager.instance.getConnection().prepareStatement(
               sql.toString());
            stmt.setInt(1, player.getId());
            stmt.setInt(2, gameInfo.getId());
            stmt.executeUpdate();
         }
         catch (Exception e)
         {
            SimpleLogger.logError("Unable to add  [" + name+ "]: ", e);
         }
         finally
         {
            DBManager.instance.close(stmt);
         }
      }
   }
   
   /**
    * Get game info by ID
    * @param id
    * @return
    * @throws MapperException
    */
   public GameInfo get(int id) throws MapperException
   {
      GameInfo info = null;
      PreparedStatement stmt = null;
      ResultSet results = null;
      
      Set participants = getParticipants(id);
      
      try
      {
         String sqlCommand = "SELECT * FROM game WHERE id=?";         
         stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
         stmt.setInt(1,id);
         results = stmt.executeQuery();
         
         if (results.first())
         {            
            User user = UserMapper.get( results.getInt("fk_creator_id"));
            info = new GameInfo(
               results.getInt("id"), results.getString("name"),
               user.getUserName(), 
               results.getString("description"),
               results.getString("objectives"),
               results.getBoolean("restricted"), 
               results.getBoolean("archived"),
			   results.getBoolean("retired"),
			   results.getInt("startDocWeight"),
               participants);
         }
      }
      catch (SQLException e)
      {            
         throw new MapperException("Get GameInfo for id [" + 
            id + "] failed", e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);  
      }
      
      return info;
   }
   
   /**
    * Get game info by name
    * @param id
    * @return
    * @throws MapperException
    */
   public GameInfo get(String gameName) throws MapperException
   {
      GameInfo info = null;
      PreparedStatement stmt = null;
      ResultSet results = null;
      Set participants = getParticipants(gameName);
      
      try
      {
         StringBuffer sql = new StringBuffer();
         sql.append("SELECT * FROM game ");
         sql.append("WHERE name = ?");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         stmt.setString(1, gameName);
         results = stmt.executeQuery();;
         if (results.first())
         {
            User user = UserMapper.get( results.getInt("fk_creator_id"));
            info = new GameInfo(
               results.getInt("id"), results.getString("name"),
               user.getUserName(), 
               results.getString("description"),
               results.getString("objectives"),
               results.getBoolean("restricted"),
               results.getBoolean("archived"),
			   results.getBoolean("retired"),
			   results.getInt("startDocWeight"),
               participants);
         }
      }
      catch (SQLException e)
      {            
         throw new MapperException("Get GameInfo for [" + 
            gameName + "] failed", e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);  
      }
      
      return info;
   }
   
   /**
    * @return Returns a list of gameInfo for all games in DB
    */
   public List getGames()
   {
      List games = new ArrayList();
      PreparedStatement stmt = null;
      ResultSet results = null;
      
      try
      {
         StringBuffer sql = new StringBuffer();
         sql.append("SELECT * FROM game;");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         results = stmt.executeQuery();
         
         while (results.next())
         {
            final User user = UserMapper.get( results.getInt("fk_creator_id"));
            final String userName = user.getUserName();
            final int gameID = results.getInt("id");
            final Set participants = getParticipants(gameID);
            
            games.add(new GameInfo(
               gameID,
               results.getString("name"),
               userName, 
               results.getString("description"),
               results.getString("objectives"),
               results.getBoolean("restricted"),
               results.getBoolean("archived"),
			   results.getBoolean("retired"),
			   results.getInt("startDocWeight"),
               participants));
         }
      }
      catch (SQLException e)
      {            
         SimpleLogger.logError("GetGames failed: ", e);
      }
      catch (MapperException e2)
      {
         SimpleLogger.logError("Unable to get creator for game in getGames: ", e2);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);  
      }
      
      return games;
   }

   public boolean archiveGame(String gameName)
   {
       PreparedStatement stmt = null;
       boolean success = false;
       try
       {
           String sql = "UPDATE game SET archived = 1 WHERE name = ?";
           stmt = DBManager.instance.getConnection().prepareStatement(sql);
           stmt.setString(1, gameName);
           stmt.executeUpdate();
           success = true;
       }
       catch (SQLException sqle)
       {
           SimpleLogger.logError("Unable to archive game ["+gameName+"]", sqle);
       }
       finally
       {
           DBManager.instance.close(stmt);
       }
       
       return success;
   }
   
   /**
    * @param gameName
    * @return
    */
   public boolean retireGame(String gameName)
   {
      PreparedStatement stmt = null;
      boolean success = false;
      try
      {
         // insert the info
         StringBuffer sql = new StringBuffer();
         sql.append("update game set retired = 1 where name = ?");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         stmt.setString(1, gameName);
         stmt.executeUpdate();
         success = true;
      }
      catch (Exception e)
      {
         SimpleLogger.logError("Unable to retire game ["+gameName+"]", e);
         success = false;
      }
      finally
      {
         DBManager.instance.close(stmt);
      }
      
      return success;
   }

   /**
    * @param gameName
    * @return
    */
   public boolean deleteGame(String gameName)
   {
      PreparedStatement stmt = null;
      boolean success = false;
      try
      {
         GameInfo gameInfo = get(gameName);
         
         StringBuffer sql = new StringBuffer();
         sql.append("delete from game where name = ?");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         stmt.setString(1, gameName);
         stmt.executeUpdate();
         
         sql = new StringBuffer();
         sql.append("delete from player_game where fk_game_id = ?");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         stmt.setInt(1, gameInfo.getId());
         stmt.executeUpdate();
         
         sql = new StringBuffer();
         sql.append("delete from player_game_role where fk_game_id = ?");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         stmt.setInt(1, gameInfo.getId());
         stmt.executeUpdate();
         
         sql = new StringBuffer();
         sql.append("delete from discourse_field where fk_game_id = ?");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         stmt.setInt(1, gameInfo.getId());
         stmt.executeUpdate();
         
         sql = new StringBuffer();
         sql.append("delete from discussion where fk_game_id = ?");
         stmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
         stmt.setInt(1, gameInfo.getId());
         stmt.executeUpdate();
         
         // kill all moves         
         List moves = MoveMapper.getMoveHistory(gameInfo.getId());
         for (Iterator itr = moves.iterator(); itr.hasNext();)
         {
            MoveMapper.remove((Move)itr.next());
         }
         
         success = true;
      }
      catch (Exception e)
      {
         SimpleLogger.logError("Unable to retire game: ", e);
         success = false;
      }
      finally
      {
         DBManager.instance.close(stmt);
      }
      
      return success;
   }
   
   public Set getParticipants(int gameId)
   {
       Set participants = new HashSet();
       PreparedStatement pstmt = null;
       ResultSet results = null;
       
       try
       {
           String sqlStr = 
               "SELECT DISTINCT p.playername"
                   +" FROM player AS p"
                   +" JOIN player_game_role AS pgr ON p.id=pgr.fk_player_id"
                   +" JOIN game AS g ON pgr.fk_game_id=g.id"
                   +" WHERE g.restricted=0"
                       +" AND pgr.fk_game_id=?"
               +" UNION"
               +" SELECT DISTINCT p.playername"
                   +" FROM player AS p"
                   +" JOIN player_game AS pg ON p.id=pg.fk_player_id"
                   +" JOIN game AS g ON pg.fk_game_id=g.id"
                   +" WHERE g.restricted=1"
                       +" AND pg.fk_game_id=?";
           
           pstmt = DBManager.instance.getConnection().prepareStatement(sqlStr);
           pstmt.setInt(1,gameId);
           pstmt.setInt(2,gameId);
           
           pstmt.execute();
           results = pstmt.getResultSet();
           
           while (results.next())
           {
               participants.add(results.getString(1));
           }
       }
       catch (SQLException sqle)
       {
           SimpleLogger.logError("Error getting participants for ["+gameId+"]", sqle);
       }
       finally
       {
           if (pstmt != null) DBManager.instance.close(pstmt);
           if (results != null) DBManager.instance.close(results);
       }
       return participants;
   }
   
   public Set getParticipants(String gameName)
   {
       Set participants = new HashSet();
       PreparedStatement pstmt = null;
       ResultSet results = null;
       
       try
       {
           
           String sqlStr = 
               "SELECT DISTINCT p.playername"
                   +" FROM player AS p"
                   +" JOIN player_game_role AS pgr ON p.id=pgr.fk_player_id"
                   +" JOIN game AS g ON pgr.fk_game_id=g.id"
                   +" WHERE g.restricted=0"
                       +" AND g.name=?"
               +" UNION"
               +" SELECT DISTINCT p.playername"
                   +" FROM player AS p"
                   +" JOIN player_game AS pg ON p.id=pg.fk_player_id"
                   +" JOIN game AS g ON pg.fk_game_id=g.id"
                   +" WHERE g.restricted=1"
                       +" AND g.name=?";

           pstmt = DBManager.instance.getConnection().prepareStatement(sqlStr);
           pstmt.setString(1,gameName);
           pstmt.setString(2,gameName);
           
           pstmt.execute();
           results = pstmt.getResultSet();
           
           while (results.next())
           {
               participants.add(results.getString(1));
           }
       }
       catch (SQLException sqle)
       {
           SimpleLogger.logError("Error getting participants for ["+gameName+"]", sqle);
       }
       finally
       {
           if (pstmt != null) DBManager.instance.close(pstmt);
           if (results != null) DBManager.instance.close(results);
       }
       return participants;
   }
}
