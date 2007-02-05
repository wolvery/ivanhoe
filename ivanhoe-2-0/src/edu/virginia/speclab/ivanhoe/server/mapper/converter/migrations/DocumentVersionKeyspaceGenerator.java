/*
 * Created on Jul 7, 2005
 */
package edu.virginia.speclab.ivanhoe.server.mapper.converter.migrations;

import java.sql.SQLException;

import edu.virginia.speclab.ivanhoe.server.mapper.converter.IvanhoeDataConverter2;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author benc
 */
public class DocumentVersionKeyspaceGenerator extends IvanhoeDataConverter2
{
    protected DocumentVersionKeyspaceGenerator()
    {
        super(1600);
    }
    
    private void addDocumentVersionKeyspace()
            throws SQLException
    {
        int dvID;
        
        // Get the max used DocumentVersion ID
        startSQLOperations();
        pstmt = DBManager.instance.getConnection().prepareStatement(
                "SELECT id FROM document_version ORDER BY id DESC LIMIT 1");
        pstmt.execute();
        results = pstmt.getResultSet();
        results.next();
        dvID = results.getInt("id");
        endSQLOperations();
        
        // Write the first DocumentVersion ID to the keyspace row
        startSQLOperations();
        pstmt = DBManager.instance.getConnection().prepareStatement(
                "INSERT INTO keyspace VALUES (?,?)");
        pstmt.setString(1, "document_version");
        pstmt.setInt(2, dvID + 1);
        pstmt.execute();
        endSQLOperations();
    }
    
    public static void main(String argv[])
    {
        DocumentVersionKeyspaceGenerator dvKeyspaceGen
                = new DocumentVersionKeyspaceGenerator();

        try
        {
            dvKeyspaceGen.setUp();
            
            if (!dvKeyspaceGen.verifyMigribility())
            {
                throw new SQLException("Database is not the required version.");
            }
            
            dvKeyspaceGen.addDocumentVersionKeyspace();
            dvKeyspaceGen.addHistoryEntry("Add document_version entry in keyspace table", "1.7");
        }
        catch (Exception e)
        {
            SimpleLogger.logError("Problem generating the document_version keyspace", e);
        }
        finally
        {
            try
            {
                dvKeyspaceGen.tearDown();
            }
            catch (Exception e)
            {
                SimpleLogger.logError("Error tearing down the data converter", e);
            }
        }
    }
}
