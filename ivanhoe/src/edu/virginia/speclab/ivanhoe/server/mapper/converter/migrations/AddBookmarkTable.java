/*
 * Created on Jan 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AddBookmarkTable extends IvanhoeDataConverter
{

    private void performConversion()
    {
        try 
        {
            setUp();            
            addNewTable();    
            addHistoryEntry("added the bookmarks table","1.2");
            tearDown();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    public void addNewTable() throws MapperException
    {
        SimpleLogger.logInfo("Adding new table...");
        Statement stmt = null;
	
        try
        {
            stmt = DBManager.instance.getConnection().createStatement();
            
            String command = "CREATE TABLE bookmarks (" +
                    		 "id int NOT NULL default 0, " +
                    		 "fk_game_id int NOT NULL references game(id), " +
                    		 "label varchar(255) default \"\"," +
                    		 "url varchar(255) default \"\"," +
                    		 "PRIMARY KEY (id) );";		           
            stmt.addBatch(command);
            
            String command2 = "INSERT INTO keyspace VALUES (\"bookmarks\",1)";
            stmt.addBatch(command2);
            
            stmt.executeBatch();
	   }
	   catch (Exception e)
	   {
	      throw new MapperException("Unable to add table: "+e);
	   }
	   finally
	   {
	      DBManager.instance.close(stmt);
	   }
    }
    
    public static void main( String args[] )
    {
        AddBookmarkTable converter = new AddBookmarkTable(); 
        converter.performConversion();
    }

}
