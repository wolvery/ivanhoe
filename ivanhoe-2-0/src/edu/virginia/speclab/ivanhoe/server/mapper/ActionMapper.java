/*
 * Created on Dec 17, 2003
 *
 * ActionMapper
 */
package edu.virginia.speclab.ivanhoe.server.mapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.exception.SequenceException;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;

import edu.virginia.speclab.ivanhoe.shared.data.*;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author lfoster
 *
 * Mapper for ivanhoe AbstractActions
 */
public class ActionMapper
{  
   // DB Action types
   public static final int ADD_ACTION = 1;
   public static final int DELETE_ACTION = 2;
   public static final int LINK_ACTION = 3;
   public static final int ADD_DOC_ACTION = 4;
   public static final int IMAGE_ACTION = 5;
   
   /**
    * Insert an action into the database
    *  
    * @param moveID
    *       The containing moveID
    * @param absAct
    *       The action to be inserted
    * @param publish
    *       Whether this action is part of a move being published
    * @return
    *       Whether the insert worked
    */
   public static boolean insert(int moveID, IvanhoeAction absAct, boolean publish)
   {
      if (moveID < 0)
      {
         SimpleLogger.logError("Invalid MoveID for action insert");
         return false;
      }

      // attempt to get an ID for the new action
      Integer id;
      try
      {
         id  = KeySequence.instance.getNewKey("action");
      }
      catch (SequenceException e1)
      {
         SimpleLogger.logError("Unable to get unique ID for new action");
         return false;
      }
      
      // FIXME: this is entirely redundant
      // write the data for action based on action type
      if (absAct.getType().equals(ActionType.ADD_ACTION))
      {
         writeActionData( id.intValue(), ActionMapper.ADD_ACTION,absAct );
      }
      if (absAct.getType().equals(ActionType.ADD_DOC_ACTION))
      {
         writeActionData( id.intValue(), ActionMapper.ADD_DOC_ACTION,absAct );
      }
      else if (absAct.getType().equals(ActionType.DELETE_ACTION))
      {
         writeActionData( id.intValue(), ActionMapper.DELETE_ACTION,absAct );
      }
      else if (absAct.getType().equals(ActionType.LINK_ACTION))
      {
         writeActionData( id.intValue(), ActionMapper.LINK_ACTION,absAct );
      }
      else if (absAct.getType().equals(ActionType.IMAGE_ACTION))
      {
         writeActionData( id.intValue(), ActionMapper.IMAGE_ACTION,absAct );
      }
         
      // write the move-action link data 
      boolean success = writeActionMapping(moveID, id.intValue());
      
      if (success)
      {
          success = DocumentVersionMapper
                  .mapActionToDocumentVersion(id.intValue(), absAct.getDocumentVersionID());
      }
      
      return success;
   }

   private static void writeActionData(int id, int dbActType, IvanhoeAction action)
   {
      PreparedStatement pstmt = null;
      
      // write the action record
      try
      {
         StringBuffer sql = new StringBuffer();
         sql.append("insert into action (");
         sql.append("id, fk_type, tag_id, ");
         sql.append("date, offset, ");
         sql.append("data, fk_adopted_from_id)");
         sql.append(" values (?, ?, ?, ?, ?, ?, ?);");
         
         pstmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());

         pstmt.setInt(1, id);
         pstmt.setInt(2, dbActType);
         pstmt.setString(3, action.getId());
         pstmt.setString(4, DBManager.formatDate(action.getDate()) );
         pstmt.setInt(5, action.getOffset() );
         
         if (action.getType().equals(ActionType.LINK_ACTION))
         {
            Link link = (Link)action.getContent();
            pstmt.setString(6, link.getAnchorText());
         }
         else
         {
            pstmt.setString(6, action.getContent().toString());
         }

         // fk_adopted_from_id is depricated
         pstmt.setNull(7,java.sql.Types.INTEGER);
         
         pstmt.executeUpdate();   
         
         // if this act is a link, write the link data
         if (action.getType().equals(ActionType.LINK_ACTION))
         {
            Link link = (Link)action.getContent();
            writeLinkTarget(id, link); 
         }
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Insert of link action failed: " + e);
      }
      finally
      {
         DBManager.instance.close(pstmt); 
      }      
   }

   private static void writeLinkTarget(int id, Link tgt)
   {
      PreparedStatement pstmt = null;
      
      // write the link target record
      try
      {
         StringBuffer sql = new StringBuffer();
         sql.append("insert into link_target (");
         sql.append("fk_action_id, fk_link_type, label, data, " );
         sql.append("link_id, fk_document_version_id )" );
         sql.append(" values (?, ?, ?, ?, ?, ?);");
         
         pstmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());
            
         LinkTag linkTag = tgt.getLinkTag();

		 pstmt.setInt(1, id);
         pstmt.setInt(2, getLinkTypeID(tgt.getType()));            
         pstmt.setString(3, tgt.getLabel());
         pstmt.setString(4, linkTag.getTagData());	         
         pstmt.setString(5, linkTag.getBackLinkID());
		 pstmt.setInt(6, linkTag.getDocumentVersionID());

         pstmt.executeUpdate();

      }
      catch (Exception e)
      {
         SimpleLogger.logError("Insert of link target id:"+id+" failed", e);
      }
      finally
      {
         DBManager.instance.close(pstmt); 
      }
      
   }
   
   private static int getLinkTypeID( LinkType linkType )
   {
       if (linkType.equals(LinkType.COMMENT))
       {
          return LinkTag.COMMENTARY_LINK_ID;
       }
       else if (linkType.equals(LinkType.INTERNAL))
       {
          return LinkTag.INTERNAL_LINK_ID;
       }
       
       return -1;       
   }

    /**
    * This method populates the move_action link table
    * It defines relationship between moves->actions
    * @param actionId ID of newly added action
    * @return
    */
   private static boolean writeActionMapping(int moveId, int actionId)
   {
      PreparedStatement pstmt = null;
      boolean success = false;
     
      try
      {
         StringBuffer sql = new StringBuffer();
         sql.append("insert into move_action (");
         sql.append("fk_move_id, fk_action_id) values (?,?); ");
        
         pstmt = DBManager.instance.getConnection().prepareStatement(
            sql.toString());

         pstmt.setInt(1, moveId);
         pstmt.setInt(2, actionId);
         
         pstmt.executeUpdate();
         success = true; 
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Insert of move_action data failed: " + e);
      }
      finally
      {
         DBManager.instance.close(pstmt);  
      }
      
      if( success == false )
      {
          SimpleLogger.logError("Insert of move_action data unsucessful.");
      }
      
      return success;
   }
   
   /**
    * Get a list of all actions associate with a particular moveId
    * @param tgtMoveId
    * @return
 * @throws MapperException
    */
   public static List getActionsForMove(int tgtMoveId, String roleName, int roleID)
           throws MapperException
   {
      ArrayList actions = new ArrayList();
      PreparedStatement pstmt = null;
      ResultSet results = null;
      try
      {
         String sql = 
            "select * from move_action,action where " +
            "move_action.fk_action_id = action.id and " +
            "move_action.fk_move_id = ?";
         pstmt = DBManager.instance.getConnection().prepareStatement(sql);
         pstmt.setInt(1,tgtMoveId);
         results = pstmt.executeQuery();
         String tagId;
         Date date;
         String data;
         int actionId;
         int offset,actType;
         while (results.next())
         {
            actionId = results.getInt("id");
            actType = results.getInt("fk_type");
            tagId = results.getString("tag_id") ;
            offset = results.getInt("offset");
            date = DBManager.parseDate(results.getString("date"));
            data = results.getString("data");
			        
            // get doc info for the actions documentId
            int docVersionID = DocumentVersionMapper.getDocumentVersionID(actionId);
            
            if (docVersionID != DocumentVersion.INVALID_ID)
            {               
               // add the correct action type to the list
               IvanhoeAction act = null;
               Object content = null;
               ActionType type = null;
               if (actType == ActionMapper.ADD_ACTION)
               {
                  type = ActionType.ADD_ACTION;
                  content = data;
               }
               else if (actType == ActionMapper.DELETE_ACTION)
               {
                  type = ActionType.DELETE_ACTION;
                  content = data;
               }
               else if (actType == ActionMapper.IMAGE_ACTION)
               {
                  type = ActionType.IMAGE_ACTION;
                  content = data;
               }
               else if (actType == ActionMapper.LINK_ACTION)
               {
                  Link tgt = getLinkTarget(actionId, tagId);
                  if (tgt != null)
                  {    
                      tgt.setAnchorText(data);
                  }
                  else
                  {
                      SimpleLogger.logError("Cannot find entry in link_target"+
                              " table for action "+actionId+".  Action skipped");
                      continue;
                  }
                  
                  content = tgt;
                  type = ActionType.LINK_ACTION;
               }
               else if (actType == ADD_DOC_ACTION)
               {
                  int documentVersionID = DocumentVersionMapper.getDocumentVersionID(actionId);
                  DocumentVersion documentVersion = DocumentVersionMapper.getDocumentVersion(documentVersionID);
                  
                  DocumentInfo di = DocumentMapper.getDocumentInfo(
                          documentVersion.getDocumentTitle(),
                          RoleMapper.getGameContainingRole(documentVersion.getRoleID()));
                  if (di == null)
                  {
                     SimpleLogger.logError("Unable to restore document add for " +
                             documentVersion.getDocumentTitle());
                     continue;
                  }
                  content = di;
                  type = ActionType.ADD_DOC_ACTION;
               }
               
               act = new IvanhoeAction(type, docVersionID, roleName, roleID, tagId, 
                  offset, content, date); 
               
               // add this action to its document version             
               actions.add(act);
            }
         } 
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Unable to retrieve move history " + e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(pstmt);
      }
      return actions;
   }

   private static Link getLinkTarget(int actionId, String linkId) throws MapperException
   {
      PreparedStatement pstmt = null;
      ResultSet results = null;
      Link target = null;
      
      try
      {
         String sql = "select * from link_target where fk_action_id = ?";
         pstmt = DBManager.instance.getConnection().prepareStatement(sql);
         pstmt.setInt(1, actionId);
         results = pstmt.executeQuery();

         if (results.first())
         {
            int linkTypeId = results.getInt("fk_link_type");
            String label = results.getString("label");
            String data = results.getString("data");
            int documentVersionID = results.getInt("fk_document_version_id");
                       
            LinkTag linkTag;
            
            switch (linkTypeId)
            {
               case LinkTag.INTERNAL_LINK_ID:                  
                  final String backLinkID = results.getString("link_id");
                  linkTag  = new LinkTag( documentVersionID, backLinkID );
                  target = new Link(linkId, LinkType.INTERNAL, linkTag, label);
                  break;
               case LinkTag.COMMENTARY_LINK_ID:
                  linkTag  = new LinkTag( data );
                  target = new Link(linkId, LinkType.COMMENT, linkTag, label);
                  break;
            }
         }
      }
      catch (SQLException e)
      {         
         throw new MapperException("Unable to retrieve link target " + e); 
      } 
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(pstmt);
      }
      
      return target;
   }

   public static int getActionID(String guid) throws MapperException
   {
      int id = 0;
      PreparedStatement stmt = null;
      ResultSet results = null;
      
      try
      {
         String sql = "select * from action where tag_id=?";
         stmt = DBManager.instance.getConnection().prepareStatement(sql);
         stmt.setString(1,guid);
         results = stmt.executeQuery();
         if (results.first())
         {
            id = results.getInt("id");  
         }
      }
      catch (SQLException e)
      {            
         throw new MapperException("ActionMapper::getActionID for guid [" + 
            guid + "] failed", e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);  
      }
      
      return id;
   }

   /**
    * Remove all actions associated with the given move
    */
   public static void removeActionsForMove(int tgtMoveId)
   {
      PreparedStatement stmt = null;
      PreparedStatement stmt2 = null;
      PreparedStatement stmt3 = null;
      PreparedStatement stmt4 = null;
      ResultSet results = null;
      try
      {
         // remove each action and document_action mapping
         
         String sqlCommand = "select * from move_action where fk_move_id = ?";
         stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
         stmt.setInt(1,tgtMoveId);
         results = stmt.executeQuery();

         while (results.next())
         {
            int actionId = results.getInt("fk_action_id");
            
            String sqlCommand2 = "delete from action where id = ?"; 
            stmt2 = DBManager.instance.getConnection().prepareStatement(sqlCommand2);
            stmt2.setInt(1,actionId);
            stmt2.executeUpdate();
            
            removeLinkTarget( actionId );
         }
         
         // and now remove mapping from move -> actions
         String sqlCommand4 = "delete from move_action where fk_move_id = ?";
         stmt4 = DBManager.instance.getConnection().prepareStatement(sqlCommand4);            
         stmt4.setInt(1,tgtMoveId);            
         stmt4.executeUpdate();
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Unable to retrieve move history " + e);
      }
      finally
      {
         DBManager.instance.close(results);
         DBManager.instance.close(stmt);
         DBManager.instance.close(stmt2);
         DBManager.instance.close(stmt3);
         DBManager.instance.close(stmt4);
      }
      
   }
   
   private static void removeLinkTarget( int actionId )
   {
       PreparedStatement stmt = null;
       
       try
       {
         // remove each action and document_action mapping
         String sqlCommand = "delete from link_target where fk_action_id = ?";
         stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
         stmt.setInt(1,actionId);
         stmt.executeUpdate();
       }
       catch (SQLException e)
       {
          SimpleLogger.logError("Unable to retrieve move history " + e);
       }
       finally
       {
          DBManager.instance.close(stmt);
       }
   }
   
   
}
