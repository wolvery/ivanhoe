/*
 * Created on Jan 13, 2005
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
 */
public class AddMoveMetadata extends IvanhoeDataConverter
{

    private void performConversion()
    {
        try 
        {
            setUp();            
            addNewTable();    
            addHistoryEntry( "added move_inspiration and category tables "+
                    		 "and title, inspiration, and category to move","1.4" );
            tearDown();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    public void addNewTable() throws MapperException
    {
        SimpleLogger.logInfo("Adding new tables...");
        Statement stmt = null;
	
        try
        {
            stmt = DBManager.instance.getConnection().createStatement();
            
            String command = "CREATE TABLE category ( " +
            				 "id int NOT NULL default 0," +
            				 "name varchar(255) default \"\"," +   
            				 "description text not null," +
            				 "fk_game_id int NOT NULL references game(id)," +
            				 "PRIMARY KEY  (id));";
            
            stmt.addBatch(command);

            String command2 = "CREATE TABLE move_inspiration (" +
               				  "inspired_id int NOT NULL references move(id)," +
               				  "inspirational_id int NOT NULL references move(id)," +
               				  "PRIMARY KEY(inspired_id, inspirational_id));" ;
            stmt.addBatch(command2);
            
            String command3 = "ALTER TABLE move ADD COLUMN title TEXT";
            stmt.addBatch(command3);

            String command4 = "ALTER TABLE move ADD COLUMN fk_category_id INT "+
            				  "references category(id)";
            stmt.addBatch(command4);

            String command5 = "INSERT INTO keyspace VALUES (\"category\",1)";            
            stmt.addBatch(command5);
            
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
        System.setProperty("IVANHOE_DIR","res");
        AddMoveMetadata converter = new AddMoveMetadata(); 
        converter.performConversion();
    }
}
