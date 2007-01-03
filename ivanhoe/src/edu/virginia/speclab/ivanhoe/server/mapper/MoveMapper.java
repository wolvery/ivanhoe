/*
 * Created on Dec 16, 2003
 *
 * MoveMapper
 */
package edu.virginia.speclab.ivanhoe.server.mapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.exception.SequenceException;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;
import edu.virginia.speclab.ivanhoe.shared.data.Move;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.data.User;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author lfoster
 * 
 * Mapper class to handle read/write of move data to DB
 */
public class MoveMapper
{
    private Map documentVersionIDMapping;
    
    private static Move createMove(int moveId, int roleId, int categoryId, Date startDate, Date subDate, String desc)
            throws MapperException    
    {
        Move move;
        List inspirations, actions;
        Role role = RoleMapper.get(roleId);
        String roleName = role.getName();

        actions = ActionMapper.getActionsForMove(moveId, roleName, roleId);

        inspirations = getInspirationIDs(moveId);
        
        SimpleLogger.logInfo("Creating move for id:"+moveId+"");
        
        // add a move to the history
        move = new Move(moveId, roleId, roleName, startDate,
                subDate, desc, actions, categoryId, inspirations );
        return move;
    }
   
    
    /**
     * @param gameId
     * @return returns a list of players that have made a move in
     *         <code>gameId</code>
     */
    public static List getActivePlayers(int gameId)
    {
        List list = new ArrayList();

        PreparedStatement pstmt = null;
        ResultSet results = null;

        try {            
            String sql = "select distinct fk_role_id from move where fk_game_id = ?";
            pstmt = DBManager.instance.getConnection().prepareStatement(sql);
            pstmt.setInt(1, gameId);
            results = pstmt.executeQuery();
            while (results.next()) {
                User player = UserMapper.getUserForRole(results.getInt("fk_role_id")); 
                if( !list.contains(player.getUserName()))
                {
                    list.add(player.getUserName());
                }
            }
        } catch (SQLException e) {
            SimpleLogger
                    .logError("Unable to get a list of players that made a move in game "
                            + gameId);
        } catch (MapperException e) {
            SimpleLogger.logError("Unable to find player " + e);
        } finally {
            DBManager.instance.close(results);
            DBManager.instance.close(pstmt);
        }
        return list;
    }

    /**
     * Insert a move into the database
     * 
     * @param gameID
     *      the ID of the game containing the move
     * @param move
     *      the move to be inserted
     * @param documentVersionOriginsMap
     *      an Integer-to-Integer map of DocumentVersion IDs in the actions to
     *      new server-side DocumentVersion IDs  
     * @param publish
     *      whether this move is being published or not
     * @return
     *      the new move ID or -1 if it failed
     * @throws MapperException
     *      if there's a problem with the database transactions
     */
    public Move insert(int gameID, Move move, Map documentVersionOriginsMap, boolean publish)
            throws MapperException
    {
        // attempt to get an ID for the new record
        // if move does not already have an id
        Integer id;
        
        if (move.getId() > -1) {
            // grab id from prior entry in move table
            id = new Integer(move.getId());
            SimpleLogger.logInfo("Updating moveID " + id);

            // purge any existing versions of this move
            remove(move);
        } else {
            try {
                id = KeySequence.instance.getNewKey("move");
            } catch (SequenceException e1) {
                SimpleLogger.logError("Unable to get unique ID for new move");
                return null;
            }
        }

        if( publish ) 
        {
            DocumentVersionMapper.publishDocumentVersions( move.getSubmissionDate(), documentVersionOriginsMap.keySet() );
        }
        
        documentVersionIDMapping =
            DocumentVersionMapper.generateDocumentVersions(move, documentVersionOriginsMap, gameID, publish);
        
        // add the move & all its actions to the DB
        if (writeMoveData(gameID, move, id)) {
            if( !writeActions(move.getActions(), documentVersionIDMapping, id.intValue(), publish) )
            {
                return null;
            }
        }
        
        return MoveMapper.getMove(id.intValue());
    }

    private static boolean writeActions(
            Iterator actionItr,
            Map documentVersionIDMapper,
            int moveId,
            boolean publish)
    {        
        boolean success = true;
        while (actionItr.hasNext()) {
            final IvanhoeAction rawAct = (IvanhoeAction) actionItr.next();
            final IvanhoeAction act = new IvanhoeAction(rawAct, documentVersionIDMapper);
            
            // if anything has a problem, abort the insert
            if (ActionMapper.insert(moveId, act, publish) == false) {
                success = false;
                break;
            }
        }
        return success;
    }
    
    private static void writeInspirationData( int moveID, List inspirations ) throws MapperException
    {
        if( inspirations == null ) return;
        
        PreparedStatement stmt = null;

        try
        {
            for( Iterator i = inspirations.iterator(); i.hasNext(); )
            {
                Integer inspirationID = (Integer) i.next();
                
                StringBuffer sql = new StringBuffer();
                sql.append("INSERT INTO move_inspiration (");
                sql.append("inspired_id, inspirational_id)");
                sql.append(" VALUES (?, ?);");

                stmt = DBManager.instance.getConnection().prepareStatement(sql.toString());

                stmt.setInt(1, moveID );
                stmt.setInt(2, inspirationID.intValue());
                stmt.executeUpdate();                
            }
        }
        catch (Exception e)
        {
           throw new MapperException("Unable to update inspirations for move " + moveID
              + ": " + e.toString());
        }
        finally
        {
           DBManager.instance.close(stmt);
        }        
    }

    private static boolean writeMoveData(int gameId, Move move, Integer id)
    {
        PreparedStatement pstmt = null;
        boolean success = false;
        try {
            StringBuffer sql = new StringBuffer();
            sql.append("insert into move (");
            sql.append("id, fk_game_id, fk_role_id, description, ");
            sql.append("start_date, submit_date, fk_category_id) ");
            sql.append(" values (?, ?, ?, ?, ?, ?, ?);");

            pstmt = DBManager.instance.getConnection().prepareStatement(
                    sql.toString());

            pstmt.setInt(1, id.intValue());
            pstmt.setInt(2, gameId);
            pstmt.setInt(3, move.getRoleID());
            pstmt.setString(4, move.getDescription());
            pstmt.setString(5, DBManager.formatDate(move.getStartDate()));            
            pstmt.setInt(7, move.getCategory());
            
            // in-progress moves will have a null submission date
            // make sure the column is set to null
            if (move.getSubmissionDate() != null) {
                pstmt.setString(6, DBManager.formatDate(move.getSubmissionDate()));
            } else {
                pstmt.setNull(6, java.sql.Types.DATE);
            }
            pstmt.executeUpdate();
            
            writeInspirationData( id.intValue(), move.getInspirations() );
            
            success = true;
        } catch (SQLException e) {
            SimpleLogger.logError("Insert Failed: " + e);
        } catch (MapperException e) {
            SimpleLogger.logError("Inspiration insert failed: " + e);            
        } finally {
            DBManager.instance.close(pstmt);
        }

        return success;
    }

    /**
     * Updates a saved move
     * 
     * @param gameId
     *      game containing the move
     * @param move
     *      the move to be updated
     * @param dvOrigins
     *      a Map from the newly created DocumentVersion IDs to their parents
     * @param publish
     *      whether this move should be published
     * @return
     *      true if the update was successful
     * @throws MapperException
     */
    public boolean update(int gameId, Move move, Map dvOrigins, boolean publish )
            throws MapperException
    {
        if (remove(move)) {
            if( insert(gameId, move, dvOrigins, publish) != null )
                return true;
        }

        return false;
    }

    public static boolean cancelPendingMove(int gameId, int roleId)
    {
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        ResultSet results = null;
        try {

            String sqlCommand = "select id from move where fk_game_id = ?" + 
            " and fk_role_id = ?" + 
            " and submit_date IS NULL";            
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            stmt.setInt(1,gameId);
            stmt.setInt(2,roleId);             
            results = stmt.executeQuery();

            // anything pending?
            if (results.first()) {
                SimpleLogger.logInfo("Removing pending move");
                int pendingMoveId = results.getInt("id");
                
                String sqlCommand2 = "delete from move where id = ?";
                stmt2 = DBManager.instance.getConnection().prepareStatement(sqlCommand2);
                stmt2.setInt(1,pendingMoveId);
                stmt2.executeUpdate();
                
                ActionMapper.removeActionsForMove(pendingMoveId);
                
            } else {
                SimpleLogger.logInfo("No pending move to remove");
            }
        } catch (Exception e) {
            SimpleLogger.logError("Unable to remove pending move for role id: "
                    + roleId + ": " + e);
            return false;
        } finally {
            DBManager.instance.close(results);
            DBManager.instance.close(stmt);
            DBManager.instance.close(stmt2);
        }
        return true;
    }
    
    public static Move getPendingMove( int gameId, int roleId )
    {
        Move move = null;

        PreparedStatement stmt = null;
        ResultSet results = null;
        try 
        {           
            String sqlCommand = "select * from move where fk_game_id = ?" + 
            " and fk_role_id = ?" + 
            " and submit_date IS NULL"; 
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            stmt.setInt(1,gameId);
            stmt.setInt(2,roleId);
            results = stmt.executeQuery();

            if (results.first()) {
                int moveId = results.getInt("id");
                String desc = results.getString("description");
                Date startDate = DBManager.parseDate(results
                        .getString("start_date"));
                
                Role role = RoleMapper.get(roleId);
                String roleName = role.getName();
                
                List actions = ActionMapper.getActionsForMove(moveId, roleName, roleId);

                move = new Move(moveId, roleId, roleName, startDate, null, desc, actions, 0, null );
            }
        } 
        catch (SQLException e) 
        {
            SimpleLogger.logError("Unable to retrieve move history", e);
        } 
        catch (MapperException e) 
        {
            SimpleLogger.logError("Unable to find user for role", e);            
        } 
        finally 
        {
            DBManager.instance.close(results);
            DBManager.instance.close(stmt);
        }

        return move;
    }
    
    public static List getInspirationIDs(int moveID)
    {
        ArrayList inspirationList = new ArrayList();

        PreparedStatement stmt = null;
        ResultSet results = null;
        try 
        {            
            String sqlCommand = "select * from move_inspiration where inspired_id = ?";
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            stmt.setInt(1,moveID);
            results = stmt.executeQuery();
            
            while (results.next()) 
            {
                int inspirationId = results.getInt("inspirational_id");

                // add a move to the history
                inspirationList.add( new Integer( inspirationId ) );
            }

        } 
        catch (SQLException e) 
        {
            SimpleLogger.logError("Unable to retrieve inspiration list: " + e);
        }
        finally 
        {
            DBManager.instance.close(results);
            DBManager.instance.close(stmt);
        }
        
        return inspirationList;                
    }

    /**
     * Get a list of all previously submitted moves for a game
     * 
     * @return
     */
    public static List getMoveHistory(int gameId) throws MapperException
    {
        ArrayList history = new ArrayList();

        PreparedStatement stmt = null;
        ResultSet results = null;
        try 
        {            
            int moveId, roleId, categoryId;
            String desc;
            Date startDate, subDate;
            
            String sqlCommand = "select * from move where fk_game_id = ?" +
                				" and submit_date IS NOT NULL";
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            stmt.setInt(1,gameId);
            results = stmt.executeQuery();
            
            while (results.next()) {
                moveId = results.getInt("id");
                roleId = results.getInt("fk_role_id");
                desc = results.getString("description");
                categoryId = results.getInt("fk_category_id");

                startDate = DBManager
                        .parseDate(results.getString("start_date"));
                subDate = DBManager.parseDate(results.getString("submit_date"));
                
                history.add(createMove(moveId, roleId, categoryId, startDate, subDate, desc));
            }

        } catch (SQLException e) {
            SimpleLogger.logError("Unable to retrieve move history " + e);
        } finally {
            DBManager.instance.close(results);
            DBManager.instance.close(stmt);
        }
        return history;
    }

    public static Move getMove(int moveID) throws MapperException
    {
        Move move = null;

        PreparedStatement pstmt = null;
        ResultSet results = null;
        
        try
        {            
            int roleID, categoryID;
            String desc;
            Date startDate, subDate;
            
            pstmt = DBManager.instance.getConnection().prepareStatement(
                    "SELECT * FROM move WHERE id = ?");
            pstmt.setInt(1,moveID);
            results = pstmt.executeQuery();
            
            results.next();
            roleID = results.getInt("fk_role_id");
            desc = results.getString("description");
            categoryID = results.getInt("fk_category_id");

            startDate = DBManager
                    .parseDate(results.getString("start_date"));
            subDate = results.getDate("submit_date");
            
            move = createMove(moveID, roleID, categoryID, startDate, subDate, desc);


        } catch (SQLException e) {
            SimpleLogger.logError("Unable to retrieve move history " + e);
        } finally {
            DBManager.instance.close(results);
            DBManager.instance.close(pstmt);
        }
        return move;
    }
    
    public static boolean remove(Move move)
    {
        PreparedStatement stmt = null;
        try 
        {
            String sqlCommand = "delete from move where id = ?";
            stmt = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            stmt.setInt(1,move.getId());
            stmt.executeUpdate();            
            ActionMapper.removeActionsForMove(move.getId());
        } catch (SQLException e) {
            SimpleLogger.logError("Unable to remove moveID "+move.getId(), e);
        } finally {
            DBManager.instance.close(stmt);
        }
        return true;
    }

    public Map getDocumentVersionIDMapping()
    {
        return documentVersionIDMapping;
    }
    
    
}