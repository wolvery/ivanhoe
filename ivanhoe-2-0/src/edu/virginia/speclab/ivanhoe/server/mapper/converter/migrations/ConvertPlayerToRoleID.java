/*
 * Created on Nov 22, 2004
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
 * This class was written to convert the database from using player IDs to identify actors
 * in the game world to using role IDs.
 *
 */
public class ConvertPlayerToRoleID extends IvanhoeDataConverter
{    
    private void performConversion()
    {
        try 
        {
            setUp();
            addNewColumns();
            convertMoveTable();
            convertDocumentTable();
            convertActionDocumentTable();
            removeOldColumns();
            addHistoryEntry("convert player to role id","1.1");
            tearDown();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    

    /**
     * @throws MapperException
     * 
     */
    private void removeOldColumns() throws MapperException
    {
        SimpleLogger.logInfo("Removing old columns...");
        Statement stmt = null;
	
        try
        {
            stmt = DBManager.instance.getConnection().createStatement();
            stmt.addBatch("ALTER TABLE move DROP COLUMN fk_player_id");              
            stmt.addBatch("ALTER TABLE action_document DROP COLUMN fk_owner_id");
            stmt.executeBatch();
	   }
	   catch (Exception e)
	   {
	      throw new MapperException("Unable to remove columns: "+e);
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
    private void convertActionDocumentTable() throws MapperException
    {
        SimpleLogger.logInfo("Converting action_document table...");
        PreparedStatement stmt = null, stmt2 = null;
        ResultSet results = null;
        
        try 
        {
            // get all the player ids
            String sqlCommand = "SELECT * FROM action_document";
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            results = stmt.executeQuery();

            // look up the role id and update the role id column
            while( results.next() )
            {
                int documentID = results.getInt("fk_document_id");
                int playerID = results.getInt("fk_owner_id");                
                int roleID = lookupRoleID(playerID,lookupGameID(documentID));
                
                String sqlCommand2 = "UPDATE action_document SET fk_role_id = ? WHERE fk_owner_id = ?";
                stmt2 = DBManager.instance.getConnection().prepareStatement(sqlCommand2);
                stmt2.setInt(1,roleID);
                stmt2.setInt(2,playerID);
                stmt2.executeUpdate();
            }
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error converting action_document table: "+e);            
        }
        finally
        {
            DBManager.instance.close(stmt);
            DBManager.instance.close(stmt2);
            DBManager.instance.close(results);
        }        
    }
    
    /**
     * @throws MapperException
     *  
     */
    private void convertDocumentTable() throws MapperException
    {
        SimpleLogger.logInfo("Converting document table...");
        PreparedStatement stmt = null, stmt2 = null;
        ResultSet results = null;
        
        try 
        {
            // get all the player ids
            String sqlCommand = "SELECT * FROM document";
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            results = stmt.executeQuery();

            // look up the role id and update the role id column
            while( results.next() )
            {
                int documentID = results.getInt("id");
                int playerID = results.getInt("fk_contributor_id");                
                int roleID = lookupRoleID(playerID,lookupGameID(documentID));
                
                String sqlCommand2 = "UPDATE document SET fk_contributor_id = ? WHERE fk_contributor_id = ?";
                stmt2 = DBManager.instance.getConnection().prepareStatement(sqlCommand2);
                stmt2.setInt(1,roleID);
                stmt2.setInt(2,playerID);
                stmt2.executeUpdate();
            }
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error converting document table: "+e);            
        }
        finally
        {
            DBManager.instance.close(stmt);
            DBManager.instance.close(stmt2);
            DBManager.instance.close(results);
        }        
    }
    
    /**
     * @throws MapperException
     * 
     */
    private void convertMoveTable() throws MapperException
    {
        SimpleLogger.logInfo("Converting move table...");
        PreparedStatement stmt = null, stmt2 = null;
        ResultSet results = null;
        
        try 
        {
            // get all the player ids
            String sqlCommand = "SELECT * FROM move";
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            results = stmt.executeQuery();

            // look up the role id and update the role id column
            while( results.next() )
            {
                int playerID = results.getInt("fk_player_id");
                int gameID = results.getInt("fk_game_id");
                int roleID = lookupRoleID(playerID,gameID);
                
                String sqlCommand2 = "UPDATE move SET fk_role_id = ? WHERE fk_player_id = ?";
                stmt2 = DBManager.instance.getConnection().prepareStatement(sqlCommand2);
                stmt2.setInt(1,roleID);
                stmt2.setInt(2,playerID);
                stmt2.executeUpdate();
            }
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error converting move table: "+e);            
        }
        finally
        {
            DBManager.instance.close(stmt);
            DBManager.instance.close(stmt2);
            DBManager.instance.close(results);
        }        
    }


    /**
     * @throws MapperException
     * 
     */
    private void addNewColumns() throws MapperException
    {
        SimpleLogger.logInfo("Adding new columns...");
        Statement stmt = null;
	
        try
        {
            stmt = DBManager.instance.getConnection().createStatement();
            stmt.addBatch("ALTER TABLE move ADD COLUMN fk_role_id int NOT NULL references role(id)");  
            stmt.addBatch("ALTER TABLE action_document ADD COLUMN fk_role_id int NOT NULL references role(id)");
            stmt.executeBatch();
	   }
	   catch (Exception e)
	   {
	      throw new MapperException("Unable to add columns: "+e);
	   }
	   finally
	   {
	      DBManager.instance.close(stmt);
	   }
    }


    public static void main(String[] args)
    {        
        ConvertPlayerToRoleID converter = new ConvertPlayerToRoleID(); 
        converter.performConversion();
    }
}
