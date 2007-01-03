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
public class AddStepTable extends IvanhoeDataConverter
{

    private void performConversion()
    {
        try 
        {
            setUp();            
            addNewTable();    
            addHistoryEntry("added the step table","1.9");
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
            
            String command = "CREATE TABLE step (" +
                    		 "id int NOT NULL default 0, " +
                    		 "fk_action_id int NOT NULL references action(id), " +
                    		 "description text default \"\"," +
                    		 "PRIMARY KEY (id) );";		           
            stmt.addBatch(command);
            
            String command2 = "INSERT INTO keyspace VALUES (\"step\",1)";
            stmt.addBatch(command2);
            
            String command3 = "ALTER TABLE action ADD COLUMN fk_step_id INT references step(id)";
            stmt.addBatch(command3);
            
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
        AddStepTable converter = new AddStepTable(); 
        converter.performConversion();
    }

}
