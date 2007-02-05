/*
 * Created on Nov 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.server.mapper.converter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.game.IvanhoeServer;
import edu.virginia.speclab.ivanhoe.server.mapper.UserMapper;
import edu.virginia.speclab.ivanhoe.shared.data.User;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author Nick
 *
 * This class provides a useful framework for performing migrations of legacy databases to newer versions of the database.
 * 
 */
public class IvanhoeDataConverter
{
    private Properties properties; 
    
    
    protected int lookupDocumentID( String documentName ) throws MapperException
    {
        int docID = -1;
        PreparedStatement stmt = null;
        ResultSet results = null;
        
        try 
        {
            String sqlCommand = "SELECT id FROM document WHERE title=?";
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            stmt.setString(1,documentName);            
            results = stmt.executeQuery();
            
            if( results.next() )
            {
                docID = results.getInt("id");
            }            
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error looking up document id: "+e);            
        }
        finally
        {
            DBManager.instance.close(stmt);
            DBManager.instance.close(results);
        }
        
        return docID;        
    }
    
    protected void addHistoryEntry( String historyInfo, String versionInfo ) throws MapperException
    {
        PreparedStatement pstmt = null;
        
        try
        {
           String sqlCommand = "insert into db_history (entry_date, entry, host_version) values (?, ?, ?)";           
           pstmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);

           pstmt.setString(1, DBManager.formatDate(new Date()) );
           pstmt.setString(2, historyInfo );
           pstmt.setString(3, versionInfo );
           
           pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new MapperException("Insert of history entry failed: " + e);
        }
        finally
        {
           DBManager.instance.close(pstmt);  
        }        
    }
   
    protected int lookupRoleID( int playerID, int gameID ) throws MapperException
    {
        int roleID = -1;
        PreparedStatement stmt = null;
        ResultSet results = null;
        
        try 
        {
            String sqlCommand = "SELECT fk_role_id FROM player_game_role "+
            					"WHERE fk_player_id=? AND fk_game_id=?";
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            stmt.setInt(1,playerID);
            stmt.setInt(2,gameID);
            results = stmt.executeQuery();
            
            if( results.next() )
            {
                roleID = results.getInt("fk_role_id");
            }            
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error looking up role id: "+e);            
        }
        finally
        {
            DBManager.instance.close(stmt);
            DBManager.instance.close(results);
        }
        
        return roleID;
    }
    
    protected int lookupRoleID( int moveID ) throws MapperException
    {
        int roleID = -1;
        PreparedStatement stmt = null;
        ResultSet results = null;
        
        try 
        {
            String sqlCommand = "SELECT * FROM move WHERE id=?";
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            stmt.setInt(1,moveID);            
            results = stmt.executeQuery();
            
            if( results.next() )
            {
                roleID = results.getInt("fk_role_id");
            }            
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error looking up role id: "+e);            
        }
        finally
        {
            DBManager.instance.close(stmt);
            DBManager.instance.close(results);
        }
        
        return roleID;        
    }
    
    
    protected int lookupMoveID( int actionID ) throws MapperException
    {
        int moveID = -1;
        PreparedStatement stmt = null;
        ResultSet results = null;
        
        try 
        {
            String sqlCommand = "SELECT * FROM move_action WHERE fk_action_id=?";
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            stmt.setInt(1,actionID);            
            results = stmt.executeQuery();
            
            if( results.next() )
            {
                moveID = results.getInt("fk_move_id");
            }            
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error looking up move id: "+e);            
        }
        finally
        {
            DBManager.instance.close(stmt);
            DBManager.instance.close(results);
        }
        
        return moveID;        
    }
    
    protected int lookupRoleID( String playerName, String title, int gameID ) throws MapperException
    {            
        User user = UserMapper.getByName(playerName);
		int userID = user.getId();
		
		return lookupRoleID(userID,gameID);			
    }

    protected int lookupGameID( int documentID ) throws MapperException
    {
        int gameID = -1;
        PreparedStatement stmt = null;
        ResultSet results = null;
        
        try 
        {
            String sqlCommand = "SELECT * FROM discourse_field WHERE fk_document_id=?";
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            stmt.setInt(1,documentID);            
            results = stmt.executeQuery();
            
            if( results.next() )
            {
                gameID = results.getInt("fk_game_id");
            }            
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error looking up game id: "+e);            
        }
        finally
        {
            DBManager.instance.close(stmt);
            DBManager.instance.close(results);
        }
        
        return gameID;        
    }

    public void setUp()
    {
        IvanhoeServer.initLogging(true);
        
        properties = IvanhoeServer.loadIvanhoeProperties();
        
        // connect DB
        String dbNameProp = properties.getProperty("dbName");
        String hostProp = properties.getProperty("dbHost");
        String userProp = properties.getProperty("dbUser");
        String passProp = properties.getProperty("dbPass");
        if (DBManager.instance.connect(
              hostProp, userProp, passProp, dbNameProp) == false)
        {
           throw new RuntimeException("Unable to connect DB");
        }
    }

    public void tearDown() throws Exception
    {
        DBManager.instance.disconnect();
    }
    
    public final Properties getProperties()
    {
        return properties;
    }
}
