/*
 * Created on Nov 23, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.server.mapper.converter.migrations;


/*
 * XXX: This code was made incompatible with recent changes, but no data exists in this format to 
 * my knowledge. - Nick
 */

//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.Date;
//
//import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
//import edu.virginia.speclab.ivanhoe.server.mapper.DiscourseFieldMapper;
//import edu.virginia.speclab.ivanhoe.server.mapper.RoleMapper;
//import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
//import edu.virginia.speclab.ivanhoe.shared.data.LinkTag;
//import edu.virginia.speclab.ivanhoe.shared.data.Role;
//import edu.virginia.speclab.ivanhoe.shared.database.DBManager;
//import edu.virginia.speclab.ivanhoe.shared.database.IvanhoeDataConverter;


/**
 * @author Nick
 * 
 * Replace link_target.data field in DB with a set of field for the data.
 * 
 * Example of old data string format:
 * "Letter from JB to SF|lacan|2004-10-18 13:24:09#b3ef3bc3d121547a:152e961:ffad3a3cf3:-7fe7"
 * 
 */
//public class ConvertLinkTagStringToField extends IvanhoeDataConverter
//{
//    private void performConversion()
//    {
//        try 
//        {
//            setUp();            
//            addNewColumns();    
//            processLinkTags();
//            addHistoryEntry("convert link tag string to field","1.1");
//            tearDown();
//        } 
//        catch (Exception e) 
//        {
//            e.printStackTrace();
//        }
//    }
//    
//    /**
//     * 
//     */
//    private void processLinkTags()
//    {
//        PreparedStatement pstmt = null;
//        ResultSet results = null;
//        
//        try
//        {
//           String sql = "select * from link_target";
//           pstmt = DBManager.instance.getConnection().prepareStatement(sql);
//           results = pstmt.executeQuery();
//
//           while(results.next())
//           {              
//              int actionId = results.getInt("fk_action_id");
//              int linkTypeId = results.getInt("fk_link_type");
//              String data = results.getString("data");
//              int documentID = results.getInt("fk_document_id");
//              
//              DiscourseFieldMapper dfMapper = new DiscourseFieldMapper();
//              int gameID = dfMapper.getGameContainingDocument(documentID);
//              if( linkTypeId == LinkTag.INTERNAL_LINK_ID )
//              {
//                  SimpleLogger.logInfo("Processing link tag for action id:"+actionId);
//
//	              LinkTag linkTag = parseInternalLink(data, gameID);
//	              
//	              if( linkTag == null )
//	              {
//		              // must be a pre-M5 database, internal link has only title
//	                  linkTag = parseOldInternalLink(actionId,data);
//	              }
//	              
//	              convertDocumentID(actionId,linkTag);
//	              convertRoleID(actionId,linkTag);
//	              convertDate(actionId,linkTag);
//	              convertLinkID(actionId,linkTag);
//	              addCurrentMoveFlag(actionId,linkTag);
//	              removeData(actionId);
//              }
//           }
//        }
//        catch (SQLException e)
//        {
//           SimpleLogger.logError("Unable to retrieve link target " + e);
//        } 
//        catch (MapperException e) 
//        {
//            SimpleLogger.logError("Error performing conversion:" + e);
//        }
//        finally
//        {
//           DBManager.instance.close(results);
//           DBManager.instance.close(pstmt);
//        }
//    }
//
//    /**
//     * Fill in the link tag the best we can, by deducing who made this link.
//     * @param actionId
//     * @param data
//     */
//    private LinkTag parseOldInternalLink(int actionID, String dataString )
//    {
//        LinkTag linkTag = null;
//        
//        // split on '|' OR '#'
//		String[] linkTokens = dataString.split("\\||#");
//
//		String title = linkTokens[0];
//		String linkID = null;
//		
//		if( linkTokens.length > 1)
//		{
//		    linkID = linkTokens[1];
//		}
//        
//        try 
//        {            
//            int moveID = lookupMoveID( actionID );
//            int roleID = lookupRoleID( moveID );
//            Role role = RoleMapper.get(roleID);
//            linkTag = new LinkTag( title, role.getName(), roleID, new Date(), linkID, false );
//        } 
//        catch (MapperException e) 
//        {
//            SimpleLogger.logError("Error performing conversion:" + e);
//        }
//        
//        return linkTag;
//    }
//
//    /**
//     * @param actionId
//     * @param linkTag
//     * @throws MapperException
//     */
//    private void convertLinkID(int actionId, LinkTag linkTag) throws MapperException
//    {
//        SimpleLogger.logInfo("Converting link id...");
//        PreparedStatement stmt = null;
//            
//        try
//        {
//            String linkID = linkTag.getBackLinkID();
//            
//            String sqlCommand = "UPDATE link_target SET link_id = ? WHERE fk_action_id = ?";
//            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
//            stmt.setString(1,linkID);
//            stmt.setInt(2,actionId);
//            stmt.executeUpdate();
//        } 
//        catch (SQLException e) 
//        {
//            throw new MapperException("Unable to update doc id: "+e);        
//        }
//        finally
//        {
//            DBManager.instance.close(stmt);
//        }        
//    }
//
//    /**
//     * @param actionId
//     * @param linkTag
//     * @throws MapperException
//     */
//    private void addCurrentMoveFlag(int actionID, LinkTag linkTag) throws MapperException
//    {
//        SimpleLogger.logInfo("Adding current move flag...");
//        PreparedStatement stmt = null;
//            
//        try
//        {
//            boolean current_move = linkTag.isInternalCurrentMoveLink();
//            
//            String sqlCommand = "UPDATE link_target SET current_move = ? WHERE fk_action_id = ?";
//            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
//            stmt.setBoolean(1,current_move);
//            stmt.setInt(2,actionID);
//            stmt.executeUpdate();
//        } 
//        catch (SQLException e) 
//        {
//            throw new MapperException("Unable to update doc id: "+e);        
//        }
//        finally
//        {
//            DBManager.instance.close(stmt);
//        }        
//    }
//
//    /**
//     * @param linkTag
//     * @throws MapperException
//     * 
//     */
//    private void convertDocumentID( int actionID, LinkTag linkTag) throws MapperException
//    {
//        SimpleLogger.logInfo("Converting document id...");
//        PreparedStatement stmt = null;
//            
//        try
//        {
//            int documentID = lookupDocumentID(linkTag.getTitle());
//            
//            String sqlCommand = "UPDATE link_target SET fk_document_id = ? WHERE fk_action_id = ?";
//            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
//            stmt.setInt(1,documentID);
//            stmt.setInt(2,actionID);
//            stmt.executeUpdate();
//        } 
//        catch (MapperException e) 
//        {
//            throw new MapperException("Unable to look of doc id: "+e);
//        } 
//        catch (SQLException e) 
//        {
//            throw new MapperException("Unable to update doc id: "+e);        
//        }
//        finally
//        {
//            DBManager.instance.close(stmt);
//        }        
//    }
//
//    /**
//     * @throws MapperException
//     * 
//     */
//    private void convertRoleID( int actionID, LinkTag linkTag) throws MapperException
//    {
//        SimpleLogger.logInfo("Converting role id...");
//        PreparedStatement stmt = null;
//            
//        try
//        {
//            int roleID = linkTag.getRoleID();
//            
//            String sqlCommand = "UPDATE link_target SET fk_role_id = ? WHERE fk_action_id = ?";
//            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
//            stmt.setInt(1,roleID);
//            stmt.setInt(2,actionID);
//            stmt.executeUpdate();
//        } 
//        catch (SQLException e) 
//        {
//            throw new MapperException("Unable to update role id: "+e);        
//        }
//        finally
//        {
//            DBManager.instance.close(stmt);
//        }        
//    }
//
//    // parse the string and pull out the data
//    private LinkTag parseInternalLink( String dataString, int gameID ) throws MapperException
//    {
//        
//        // split on '|' OR '#'
//		String[] linkTokens = dataString.split("\\||#");
//
//		if( linkTokens.length >= 3)
//		{
//		    int line = 0;
//		    String title = linkTokens[line++];
//		    String playerName = linkTokens[line++];
//		    
//		    boolean currentMove = false;
//		    
//		    if( linkTokens[line].equals("UNKNOWN"))
//		    {
//		        line++;
//		        currentMove = true;		        
//		    }
//		    
//		    Date date = DBManager.parseDate(linkTokens[line++]);
//		    
//		    String backLinkID = null;
//		    if( line < linkTokens.length )
//		    {
//		        backLinkID = linkTokens[line];        
//		    }
//            
//            int roleID = lookupRoleID( playerName, title, gameID );
//		
//			return new LinkTag( title, playerName, roleID, date, backLinkID, currentMove );
//		}
//		else
//		{
//		    return null;
//		}
//    }
//
//    /**
//     * @throws MapperException
//     * 
//     */
//    private void convertDate( int actionID, LinkTag linkTag) throws MapperException
//    {
//        SimpleLogger.logInfo("Converting date...");
//        PreparedStatement stmt = null;
//            
//        try
//        {
//            Date date = linkTag.getDate();            
//            String sqlCommand = "UPDATE link_target SET document_version_date = ? WHERE fk_action_id = ?";
//            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
//            stmt.setString(1,DBManager.formatDate(date));
//            stmt.setInt(2,actionID);
//            stmt.executeUpdate();
//        } 
//        catch (SQLException e) 
//        {
//            throw new MapperException("Unable to update date: "+e);        
//        }
//        finally
//        {
//            DBManager.instance.close(stmt);
//        }        
//    }
//
//    /**
//     * @throws MapperException
//     * 
//     */
//    private void removeData( int actionID ) throws MapperException
//    {
//        SimpleLogger.logInfo("Removing data column...");
//        PreparedStatement stmt = null;
//	
//       try
//       {
//                        
//            String sqlCommand = "UPDATE link_target SET data = NULL WHERE fk_action_id = ?";
//            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
//            stmt.setInt(1,actionID);            
//            stmt.executeUpdate();
//       }
//	   catch (Exception e)
//	   {
//	      throw new MapperException("Unable to remove data: "+e);
//	   }
//	   finally
//	   {
//	      DBManager.instance.close(stmt);
//	   }        
//    }
//
//    /**
//     * @throws MapperException
//     * 
//     */
//    private void addNewColumns() throws MapperException
//    {
//        SimpleLogger.logInfo("Adding new columns...");
//        Statement stmt = null;
//	
//        try
//        {
//            stmt = DBManager.instance.getConnection().createStatement();
//            stmt.addBatch("ALTER TABLE link_target ADD COLUMN fk_role_id int NOT NULL references role(id)");  
//            stmt.addBatch("ALTER TABLE link_target ADD COLUMN fk_document_id int NOT NULL references document(id)");
//            stmt.addBatch("ALTER TABLE link_target ADD COLUMN current_move BOOL DEFAULT 0");
//            stmt.addBatch("ALTER TABLE link_target ADD COLUMN document_version_date DATETIME DEFAULT NULL");
//            stmt.addBatch("ALTER TABLE link_target ADD COLUMN link_id TEXT");
//            stmt.executeBatch();
//	   }
//	   catch (Exception e)
//	   {
//	      throw new MapperException("Unable to add columns: "+e);
//	   }
//	   finally
//	   {
//	      DBManager.instance.close(stmt);
//	   }
//    }
//   
//    public static void main(String[] args)
//    {        
//        ConvertLinkTagStringToField converter = new ConvertLinkTagStringToField(); 
//        converter.performConversion();
//    }
//}
