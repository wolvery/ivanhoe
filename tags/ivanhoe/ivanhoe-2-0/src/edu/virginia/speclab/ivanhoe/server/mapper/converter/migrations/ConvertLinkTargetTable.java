package edu.virginia.speclab.ivanhoe.server.mapper.converter.migrations;
/*
 * Created on Nov 23, 2004
 *
 */

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.mapper.converter.IvanhoeDataConverter;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.LinkTag;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;


/**
 * @author Nick
 * 
 *  Convert the link_target table from role,doc,date to document version id for identifying
 *  the link target.
 * 
 */
public class ConvertLinkTargetTable extends IvanhoeDataConverter
{
    private int corrupt;
    
    private void performConversion()
    {
        try 
        {
            setUp();
            addColumn();
            processLinkTargets();
			removeCorruptEntries();
			removeColumns();
            addHistoryEntry("drop action_document, convert link_target to use version ids","1.8");
            tearDown();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    private void removeColumns() throws MapperException 
	{
	    SimpleLogger.logInfo("Removing obsolete columns in link_target.");
        Statement stmt = null;
    
        try
        {
            stmt = DBManager.instance.getConnection().createStatement();
            stmt.addBatch("ALTER TABLE link_target DROP COLUMN fk_role_id");
            stmt.addBatch("ALTER TABLE link_target DROP COLUMN fk_document_id");
            stmt.addBatch("ALTER TABLE link_target DROP COLUMN document_version_date");
			stmt.addBatch("ALTER TABLE link_target DROP COLUMN current_move");
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

	private void removeCorruptEntries() 
	{
        PreparedStatement pstmt = null;
        ResultSet results = null;
        
        try
        {
           String sql = "select fk_action_id from link_target where fk_link_type=1 and fk_document_version_id=0";
           pstmt = DBManager.instance.getConnection().prepareStatement(sql);
           results = pstmt.executeQuery();

           while(results.next())
           {              
              int actionID = results.getInt("fk_action_id");
              removeEntry(actionID);
           }
        }
        catch (SQLException e)
        {
           SimpleLogger.logError("Unable to retrieve link target " + e);
        } 
		catch (MapperException e) 
		{
			SimpleLogger.logError("Unable to retrieve link target " + e);		
		} 
        finally
        {
           DBManager.instance.close(results);
           DBManager.instance.close(pstmt);
        }
	}

	private void removeEntry(int actionID) throws MapperException 
	{
	    SimpleLogger.logInfo("Removing corrupt entry: "+actionID);
        Statement stmt = null;
    
        try
        {
            stmt = DBManager.instance.getConnection().createStatement();
            stmt.addBatch("delete from action where id="+actionID);
            stmt.addBatch("delete from link_target where fk_action_id="+actionID);  
			stmt.addBatch("delete from move_action where fk_action_id="+actionID);
			stmt.addBatch("delete from action_version where fk_action_id="+actionID);
            stmt.executeBatch();
       }
       catch (Exception e)
       {
          throw new MapperException("Unable to remove entry: "+e);
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
    private void addColumn() throws MapperException
    {
        SimpleLogger.logInfo("Adding new columns...");
        Statement stmt = null;
    
        try
        {
            stmt = DBManager.instance.getConnection().createStatement();
            //stmt.addBatch("drop table action_document");
            stmt.addBatch("ALTER TABLE link_target ADD COLUMN fk_document_version_id int NOT NULL references document_version(id)");  
            stmt.executeBatch();
       }
       catch (Exception e)
       {
          throw new MapperException("Unable to add column: "+e);
       }
       finally
       {
          DBManager.instance.close(stmt);
       }
    }
    
    private int getDocumentVersionID( int roleID, int docID, Date date ) throws MapperException
    {
        PreparedStatement pstmt = null;
        ResultSet results = null;        
        VersionDatePair correctVersion = null;
        
        try 
        {
            String sqlCommand = "SELECT id,date FROM document_version WHERE fk_role_id=? AND fk_document_id=?";            
            pstmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            pstmt.setInt(1,roleID);
            pstmt.setInt(2,docID);
            
            results = pstmt.executeQuery();

            TreeMap versionMap = new TreeMap();
            
            // collect all of the candidate document verions
            while( results.next() )  
            {
                int versionID = results.getInt("id");
                Date documentVersionDate = DBManager.parseDate(results.getString("date"));
                versionMap.put( documentVersionDate, new VersionDatePair( versionID, documentVersionDate ));
            }
                        
			// walk the sorted list looking for the latest version before the link_target date
            for( Iterator i = versionMap.entrySet().iterator(); i.hasNext(); )
            {
				Map.Entry mapEntry = (Map.Entry) i.next();
				VersionDatePair version = (VersionDatePair) mapEntry.getValue();
                			                
				if( correctVersion == null ) correctVersion = version;				
				else if( version.getDate().compareTo(date) <= 0 )
                {
					correctVersion = version;
                }
            }
            
        }
        catch (SQLException e) 
        {
            throw new MapperException("Error converting discussion table: "+e);            
        }
        finally 
        {
            DBManager.instance.close(results);
            DBManager.instance.close(pstmt);      
        }
        
        if( correctVersion != null ) return correctVersion.getId();
        else return -1;             
    }

    private void processLinkTargets()
    {
        PreparedStatement pstmt = null;
        ResultSet results = null;
        
        try
        {
           String sql = "select * from link_target";
           pstmt = DBManager.instance.getConnection().prepareStatement(sql);
           results = pstmt.executeQuery();

           while(results.next())
           {              
              int actionID = results.getInt("fk_action_id");
              int linkTypeId = results.getInt("fk_link_type");              
              int documentID = results.getInt("fk_document_id");
              int roleID = results.getInt("fk_role_id");
              Date date = DBManager.parseDate(results.getString("document_version_date")); 
              
              if( linkTypeId == LinkTag.INTERNAL_LINK_ID )
              {
                  int documentVersionID = getDocumentVersionID(roleID,documentID,date);
                  
                  // one of the three pieces of info are bad. 
                  if( documentVersionID == -1 )
                  {
                      corrupt++;
                  }
                  else
                  {
                      // write the new document_version id to the table
                      updateLinkTarget( actionID, documentVersionID );
                  }
              }
           }
           
           SimpleLogger.logInfo(corrupt+" indeterminate entries.");
        }
        catch (SQLException e)
        {
           SimpleLogger.logError("Unable to retrieve link target " + e);
        } 
        catch (MapperException e)
        {
            SimpleLogger.logError("Unable to retrieve link target " + e);
            
        } 
        finally
        {
           DBManager.instance.close(results);
           DBManager.instance.close(pstmt);
        }
    }
   
    private void updateLinkTarget(int actionID, int documentVersionID) throws MapperException
    {
        SimpleLogger.logInfo("Converting link_target for action id: "+actionID);
        PreparedStatement stmt = null;
            
        try
        {
            String sqlCommand = "UPDATE link_target SET fk_document_version_id = ? WHERE fk_action_id = ?";
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            stmt.setInt(1,documentVersionID);
            stmt.setInt(2,actionID);
            stmt.executeUpdate();
        } 
        catch (SQLException e) 
        {
            throw new MapperException("Unable to update link_target for action id: "+e);        
        }
        finally
        {
            DBManager.instance.close(stmt);
        }        
    }

    public static void main(String[] args)
    {        
        ConvertLinkTargetTable converter = new ConvertLinkTargetTable(); 
        converter.performConversion();
    }
    
    private class VersionDatePair
    {
        private int id;
        private Date date;
        
        public VersionDatePair( int id, Date date )
        {
            this.id = id;
            this.date = date;
        }

        private Date getDate()
        {
            return date;
        }
        

        private int getId()
        {
            return id;
        }
    }
}
