/*
 * Created on Jul 7, 2005
 */
package edu.virginia.speclab.ivanhoe.server.mapper.converter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * This class adds some functionality that is used in some data converters.
 * These are provided seperately because including them in the base class
 * would be problematic, as many children do not use the start/end
 * SQLOperations methods, and these are not useful if they're not used
 * consistantly.
 * 
 * @author benc
 */
public abstract class IvanhoeDataConverter2
        extends IvanhoeDataConverter
{
    private static boolean sqlDataInUse = false;
    protected static PreparedStatement pstmt;
    protected static ResultSet results;

    protected final int SUPPORTED_DB_VERSION_NUMBER;

    protected IvanhoeDataConverter2()
    {
        SUPPORTED_DB_VERSION_NUMBER = Integer.MIN_VALUE;
    }
    
    protected IvanhoeDataConverter2(int supportedDBVersionNumber)
    {
        SUPPORTED_DB_VERSION_NUMBER = supportedDBVersionNumber;
    }
    
    protected static final int getDBVersion() throws SQLException
    {
        int dbVersion = 0;
        String sqlStr = "SELECT host_version FROM db_history ORDER BY host_version DESC LIMIT 1";
        
        startSQLOperations();
        pstmt = DBManager.instance.getConnection().prepareStatement(sqlStr);
        pstmt.execute();
        
        results = pstmt.getResultSet();
        results.next();
        dbVersion = (int)(1000 * Double.parseDouble(results.getString("host_version")));
        endSQLOperations();
        
        return dbVersion;
    }

    public boolean verifyMigribility()
            throws SQLException
    {
        if (SUPPORTED_DB_VERSION_NUMBER == Integer.MIN_VALUE)
        {
            throw new RuntimeException("IvanhoeDataConverter2 not initialized with a database version number");
        }
        
        int dbVersion = getDBVersion();
        if (dbVersion != SUPPORTED_DB_VERSION_NUMBER)
        {
            SimpleLogger.logError("Error: database has version ["+dbVersion/1000.0 
                    +"], but this command only works on version ["
                    +SUPPORTED_DB_VERSION_NUMBER/1000.0+"]");
            return false;
        }
        
        return true;
    }


    
    protected static final void startSQLOperations()
    {
        if (sqlDataInUse)
        {
            throw new RuntimeException("Tried to start new work with the SQL data objects already in use");
        }
        
        sqlDataInUse = true;
        pstmt = null;
        results = null;
    }

    protected static final void endSQLOperations()
    {
        try
        {
            if (results != null) results.close();
            if (pstmt != null) pstmt.close();
        }
        catch (SQLException sqle)
        {
            SimpleLogger.logError("Error closing SQL statement", sqle);
        }
        finally
        {
            sqlDataInUse = false;
        }
    }

}
