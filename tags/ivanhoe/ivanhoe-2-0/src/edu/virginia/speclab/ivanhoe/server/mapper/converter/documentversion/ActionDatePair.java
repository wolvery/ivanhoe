package edu.virginia.speclab.ivanhoe.server.mapper.converter.documentversion;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

class ActionDatePair
{
    private int actionID;
    private Date versionDate;
    
    public ActionDatePair( int actionID, Date versionDate )
    {
        this.actionID = actionID;
        this.versionDate = versionDate;
    }

    public int getActionID()
    {
        return actionID;
    }
    

    public Date getVersionDate()
    {
        return versionDate;
    }
    
    public String toString()
    {
        return "("+actionID+","+versionDate+")";
    }
    
    public void write() throws MapperException
    {
        SimpleLogger.logInfo(toString());
        
        PreparedStatement pstmt = null;
        
        // write the action version
        try
        {
           StringBuffer sql = new StringBuffer();
           sql.append("UPDATE action_document SET version_date=? WHERE fk_action_id=?");
           
           pstmt = DBManager.instance.getConnection().prepareStatement(
              sql.toString());

           pstmt.setString(1, DBManager.formatDate(versionDate) );
           pstmt.setInt(2, actionID );
           
           int rows = pstmt.executeUpdate();
           if (rows != 1)
           {
               SimpleLogger.logError("Updated "+rows+" rows in action_document");
           }
        }
        catch (SQLException e)
        {
           throw new MapperException("update of action_document table failed: " + e);
        }
        finally
        {
           DBManager.instance.close(pstmt); 
        }        
    }

}
