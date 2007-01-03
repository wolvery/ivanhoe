/*
 * Created on Dec 21, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.server.mapper.converter.migrations;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.mapper.converter.IvanhoeDataConverter;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author Nick
 * 
 * 
 */
public class ConvertDiscussionData extends IvanhoeDataConverter
{
    public static void main( String args[] )
    {
        ConvertDiscussionData converter = new ConvertDiscussionData(); 
        converter.performConversion();
    }

    /**
     * 
     */
    private void performConversion()
    {
        try 
        {
            setUp();            
            addNewColumn();    
            processPlayerID();
            removeOldColumn();
            addHistoryEntry("convert discussion data from player id to role id","1.2");
            tearDown();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    private void addNewColumn() throws MapperException
    {
        SimpleLogger.logInfo("Adding new column...");
        Statement stmt = null;
	
        try
        {
            stmt = DBManager.instance.getConnection().createStatement();
            stmt.addBatch("ALTER TABLE discussion ADD COLUMN fk_role_id int NOT NULL references role(id)");  
            stmt.executeBatch();
	   }
	   catch (Exception e)
	   {
	      throw new MapperException("Unable to add column: "+e);
	   }
	   finally
	   {
	      DBManager.instance.close(stmt);
	   }
    }
    
    /**
     * @throws MapperException
     * 
     */
    private void removeOldColumn() throws MapperException
    {
        SimpleLogger.logInfo("Removing old column...");
        Statement stmt = null;
	
        try
        {
            stmt = DBManager.instance.getConnection().createStatement();
            stmt.addBatch("ALTER TABLE discussion DROP COLUMN fk_player_id");                          
            stmt.executeBatch();
	   }
	   catch (Exception e)
	   {
	      throw new MapperException("Unable to remove column: "+e);
	   }
	   finally
	   {
	      DBManager.instance.close(stmt);
	   }                
    }

    /**
     * Convert the player ID column to role IDs 
     * @throws MapperException
     */
    private void processPlayerID() throws MapperException
    {
        SimpleLogger.logInfo("Converting discussion table...");
        PreparedStatement stmt = null, stmt2 = null;
        ResultSet results = null;
        
        try 
        {
            // get all the player ids
            String sqlCommand = "SELECT * FROM discussion";
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            results = stmt.executeQuery();

            // look up the role id and update the role id column
            while( results.next() )
            {
                int entryID = results.getInt("id");
                int playerID = results.getInt("fk_player_id");
                int gameID = results.getInt("fk_game_id");
                int roleID = lookupRoleID(playerID,gameID);
                
                String sqlCommand2 = "UPDATE discussion SET fk_role_id = ? WHERE id = ?";
                stmt2 = DBManager.instance.getConnection().prepareStatement(sqlCommand2);
                stmt2.setInt(1,roleID);
                stmt2.setInt(2,entryID);
                stmt2.executeUpdate();
            }
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error converting discussion table: "+e);            
        }
        finally
        {
            DBManager.instance.close(stmt);
            DBManager.instance.close(stmt2);
            DBManager.instance.close(results);
        }        
    }
    

}
