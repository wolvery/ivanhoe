/*
 * Created on Jan 11, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.server.mapper.converter.migrations;

import java.sql.Statement;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.mapper.converter.IvanhoeDataConverter;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author Nick
 *
 * Add the summary field to the bookmarks table
 */
public class AddRefSummary extends IvanhoeDataConverter
{
    private void performConversion()
    {
        try 
        {
            setUp();            
            addNewField();    
            addHistoryEntry("added summary to bookmarks table","1.3");
            tearDown();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    public void addNewField() throws MapperException
    {
        SimpleLogger.logInfo("Adding new field...");
        Statement stmt = null;
	
        try
        {
            stmt = DBManager.instance.getConnection().createStatement();
            
            String command = "ALTER TABLE bookmarks ADD COLUMN summary TEXT NOT NULL";
            stmt.addBatch(command);
            
            stmt.executeBatch();
	   }
	   catch (Exception e)
	   {
	      throw new MapperException("Unable to add field: "+e);
	   }
	   finally
	   {
	      DBManager.instance.close(stmt);
	   }
    }
    
    public static void main( String args[] )
    {
        System.setProperty("IVANHOE_DIR","res");
        AddRefSummary converter = new AddRefSummary(); 
        converter.performConversion();
    }
}
