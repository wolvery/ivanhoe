package edu.virginia.speclab.ivanhoe.server.mapper.converter.documentversion;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.mapper.RoleMapper;
import edu.virginia.speclab.ivanhoe.server.mapper.converter.IvanhoeDataConverter2;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * This class converts from DB version 1.5 -> 1.6, which is a major change in the 
 * way document versions are represented in the database. This change is made to 
 * support branching document stemmas.
 * 
 * @author Nick
 *
 */
public class CreateDocumentVersionAndVersionActionTables extends IvanhoeDataConverter2
{
    public CreateDocumentVersionAndVersionActionTables()
    {
        super(1500);
    }
    
    private void performConversion()
    {
        try 
        {
            setUp();
            if (verifyMigribility() == false)
            {
                throw new RuntimeException("Database not migratable");
            }
            addTables();
            
            DocumentVersionManager documentVersionManager = new DocumentVersionManager();
            documentVersionManager.convertTables();
            
            addHistoryEntry( "convert action document version dates","1.6" );
            
            PrintStream dotStream = new PrintStream(new FileOutputStream("documentversions.dot"));
            documentVersionManager.printDocumentVersionTrees(dotStream);
            dotStream.close();
            
            tearDown();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    private void addTables() throws MapperException
    {
        SimpleLogger.logInfo("Adding new tables...");
        Statement stmt = null;
    
        try
        {
            stmt = DBManager.instance.getConnection().createStatement();
            
            String command = "CREATE TABLE document_version ( id INT NOT NULL default 0, fk_document_id int NOT NULL REFERENCES document(id), fk_role_id INT NOT NULL REFERENCES role(id), date DATETIME NOT NULL, parent_id INT REFERENCES document_version(id), published tinyint(1) default '0', PRIMARY KEY(id))";                 
            stmt.addBatch(command);
            
            String command2 = "CREATE TABLE action_version ( fk_action_id INT NOT NULL REFERENCES action(id), fk_document_version_id INT NOT NULL REFERENCES document_version(id))";
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
        CreateDocumentVersionAndVersionActionTables converter = new CreateDocumentVersionAndVersionActionTables(); 
        converter.performConversion();
    }

    public static DocumentInfo getDocumentInfo(final int id)
    {
       DocumentInfo info = null;
       PreparedStatement pstmt = null;
       ResultSet results = null;

       try
       {
          StringBuffer sql = new StringBuffer();
          sql.append("select * from document ");
          sql.append("where document.id = ").append(id);
          pstmt = DBManager.instance.getConnection().prepareStatement(
             sql.toString());
          results = pstmt.executeQuery();
          if (results.first())
          {
             info = extractDocInfo(results);
          }
       }
       catch (SQLException e)
       {
          SimpleLogger.logError("Unable to get DF Doc list " + e);
       }
       finally
       {
          DBManager.instance.close(results);
          DBManager.instance.close(pstmt);
       }

       return info;
    }
    
    /**
     * Helper method to extract doc info from a valid resultSet
     * @param results
     * @return
     * @throws SQLException
     */
    private static DocumentInfo extractDocInfo(ResultSet results) throws SQLException
    {
       int id = results.getInt("id");
       String file = results.getString("file_name");
       String title = results.getString("title");
       String author = results.getString("author");
       String provenance = results.getString("provenance");
       int length = Integer.parseInt(results.getString("length"));
       Date addDate = DBManager.parseDate(results.getString("add_date"));
       int contributorId = results.getInt("fk_contributor_id");
       String contributor = "";
       if (contributorId > 0)
       {         
          try
          {
             Role role = RoleMapper.get(contributorId);
             contributor = role.getName(); 
          }
          catch (MapperException e1)
          {
             SimpleLogger.logError("Unable to get contributor; Leaving as System"); 
          }
       }
       DocumentInfo info = new DocumentInfo(id, file, title, author, provenance, contributor, contributorId, addDate);
       info.setDocumentLength(length);
       return info;
    }

}
