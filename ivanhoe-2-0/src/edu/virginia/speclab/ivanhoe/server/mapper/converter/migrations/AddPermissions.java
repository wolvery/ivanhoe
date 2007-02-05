/*
 * Created on Apr 1, 2005
 */
package edu.virginia.speclab.ivanhoe.server.mapper.converter.migrations;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.virginia.speclab.ivanhoe.server.mapper.converter.IvanhoeDataConverter;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author benc
 */
public class AddPermissions extends IvanhoeDataConverter
{
    private void performConversion()
    {
        try 
        {
            setUp();            
            
            addBoolColumn("game", "archived", false, "objectives");
            addBoolColumn("game", "private", false, "restricted");
            
            addBoolColumn("role", "write_permission", false);
            updateBoolColumn("role", "write_permission", true);
            
            addBoolColumn("player", "new_game_permission", false);
            addBoolColumn("player", "new_role_permission", false);
            addBoolColumn("player", "write_permission", false);
            addBoolColumn("player", "admin", false);
            
            addHistoryEntry("Added 'archived' field to game table","1.5");
            addHistoryEntry("Added 'private' field to game table","1.5");
            addHistoryEntry("Added 'write_permission' field to role table","1.5");
            addHistoryEntry("Added 'new_game_permission' field to player table","1.5");
            addHistoryEntry("Added 'new_role_permission' field to player table","1.5");
            addHistoryEntry("Added 'write_permission' field to player table","1.5");
            addHistoryEntry("Added 'admin' field to player table","1.5");
            tearDown();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    private void addBoolColumn(String table, String column, boolean defaultVal)
    {
        addBoolColumn(table, column, defaultVal, null);
    }
    
    private void addBoolColumn(String table, String column, boolean defaultVal, String afterCol)
    {
        String sql = "ALTER TABLE "+table+" ADD COLUMN "+column+" TINYINT(1) DEFAULT ?";
        
        if (afterCol != null)
        {
            sql += " AFTER "+afterCol;
        }
        
        PreparedStatement pstmt = null;
        
        try
        {
            pstmt = DBManager.instance.getConnection().prepareStatement(sql.toString());
            
            pstmt.setBoolean(1, defaultVal);
            
            SimpleLogger.logInfo("Executing SQL statement ["+pstmt.toString()+"]");
            pstmt.execute();
        }
        catch (SQLException e)
        {
           throw new RuntimeException("Unable to ALTER table ["+table+"]: "+e);
        }
        finally
        {
           DBManager.instance.close(pstmt);
        }
    }
    
    private void updateBoolColumn(String table, String column, boolean value)
    {
        String sql = "UPDATE "+table+" SET "+column+" = ?";
        
        PreparedStatement pstmt = null;
        
        try
        {
            pstmt = DBManager.instance.getConnection().prepareStatement(sql.toString());
            
            pstmt.setBoolean(1, value);
            
            SimpleLogger.logInfo("Executing SQL statement ["+pstmt.toString()+"]");
            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
           throw new RuntimeException("Unable to UPDATE table ["+table+"]: "+e);
        }
        finally
        {
           DBManager.instance.close(pstmt);
        }
    }
    
    public static void main(String[] args)
    {
        AddPermissions converter = new AddPermissions(); 
        converter.performConversion();
    }
}
