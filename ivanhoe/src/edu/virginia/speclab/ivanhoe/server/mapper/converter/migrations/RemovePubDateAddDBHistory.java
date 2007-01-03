/*
 * Created on Nov 30, 2004
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
 */
public class RemovePubDateAddDBHistory extends IvanhoeDataConverter
{
    private void performConversion()
    {
        try 
        {
            setUp();
            removePubDate();
            addDBHistory();
            addHistoryEntry("removed document.publication_date, added db_history table","1.1");
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
    private void addDBHistory() throws MapperException
    {
        SimpleLogger.logInfo("Adding new column...");
        Statement stmt = null;
	
        try
        {
            stmt = DBManager.instance.getConnection().createStatement();
            stmt.addBatch("CREATE TABLE db_history (" +
                          "entry_date varchar(20)," +
            		      "entry varchar(50)," +
            		      "host_version varchar(20))" );             
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

    /**
     * @throws MapperException
     * 
     */
    private void removePubDate() throws MapperException
    {
        SimpleLogger.logInfo("Removing old columns...");
        Statement stmt = null;
	
        try
        {
            stmt = DBManager.instance.getConnection().createStatement();
            stmt.addBatch("ALTER TABLE document DROP COLUMN publication_date");                          
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

    public static void main(String[] args)
    {        
        RemovePubDateAddDBHistory converter = new RemovePubDateAddDBHistory(); 
        converter.performConversion();
    }}
