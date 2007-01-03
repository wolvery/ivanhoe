package edu.virginia.speclab.ivanhoe.server.mapper.converter.documentversion;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

// This class stores the document version information
class DocumentVersion
{
    private final int id;
    private final int documentID;
    private final int roleID;
    private final Date date;
    private int parentID;
    private boolean published;
    
    private static int idCounter = 1;
    
    public DocumentVersion( int documentID, int roleID, Date date, boolean published )
    {
        this.id = idCounter++;
        this.documentID = documentID;
        this.roleID = roleID;
        this.date = date;
        this.published = published;
    }

    public Date getDate()
    {
        return date;
    }
    

    public int getDocumentID()
    {
        return documentID;
    }
    

    public int getId()
    {
        return id;
    }

    public int getRoleID()
    {
        return roleID;
    }
    
    public String toString()
    {
        return id+","+documentID+","+roleID+","+DBManager.formatDate(date);
    }

    public int getParentID()
    {
        return parentID;
    }
    

    public void setParentID(int parentID)
    {
        if (parentID == this.id)
        {
            throw new RuntimeException("DocumentVersion cannot be its own parent");
        }
        this.parentID = parentID;
    }
    
    public void write() throws MapperException
    {
        System.out.println(toString());
     
        PreparedStatement pstmt = null;
        
        // write the action record
        try
        {  
           pstmt = DBManager.instance.getConnection().prepareStatement(
                   "INSERT INTO document_version VALUES (?, ?, ?, ?, ?, ?)");

           pstmt.setInt(1, id);
           pstmt.setInt(2, documentID);
           pstmt.setInt(3, roleID);
           pstmt.setString(4, DBManager.formatDate(date) );
           pstmt.setInt(5, parentID );
           pstmt.setInt(6, (published?1:0) );
           pstmt.executeUpdate();   
        }
        catch (SQLException e)
        {
           throw new MapperException("Insert of link action failed: " + e);
        }
        finally
        {
           DBManager.instance.close(pstmt); 
        }        
    }
}