/*
 * Created on May 5, 2005
 */
package edu.virginia.speclab.ivanhoe.server.mapper.converter.migrations;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;

import edu.virginia.speclab.ivanhoe.server.mapper.converter.IvanhoeDataConverter;
import edu.virginia.speclab.ivanhoe.server.mapper.converter.IvanhoeDataConverter2;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.blist.BLinkedList;
import edu.virginia.speclab.ivanhoe.shared.blist.BList;
import edu.virginia.speclab.ivanhoe.shared.blist.BCollectionReducer;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author benc
 */
public final class ServerGameSQLMigrator extends IvanhoeDataConverter2
{
    private final String srcDb;
    private final Map srcIdOffsets, dstIdOffsets, fkOffsets;
    private static final String[] primaryTableNames =
            {
                "action",
                "bookmarks",
                "category",
                "discussion",
                "document",
                "game",
                "move",
                "player",
                "role"
            };

    private static final String[] secondaryTableNames =
            {            
                "action_document",
                "discourse_field",
                "document_image",
                "link_target",
                "move_action",
                "move_inspiration",
                "player_game",
                "player_game_role"
            };
    
    public ServerGameSQLMigrator(String srcDb) throws SQLException
    {
        super(1500);
        this.srcDb = srcDb;
        srcIdOffsets = getKeyOffsets(srcDb);
        dstIdOffsets = getKeyOffsets();
        fkOffsets = getFkRefs(dstIdOffsets);
    }
    
    public void migratePrimaryTable(String tblName) throws SQLException
    {   
        migrateSecondaryTable(tblName);
        
        startSQLOperations();
        
        String updateSql = genUpdateSQLStr(tblName);
        System.out.println("SQL: ["+updateSql+"]");
        pstmt = DBManager.instance.getConnection().prepareStatement(updateSql);
        pstmt.execute();
        
        endSQLOperations();
    }

    public void migrateSecondaryTable(String tblName) throws SQLException
    {
        startSQLOperations();
        
        String selectStr = genInsertSelectSQLStr(tblName);
        String insertSql = "INSERT INTO "+tblName+" SELECT "+selectStr
                +" FROM "+srcDb+"."+tblName;
        System.out.println("SQL: ["+insertSql+"]");
        pstmt = DBManager.instance.getConnection().prepareStatement(insertSql);
        pstmt.execute();
        endSQLOperations();
    }
    
    public void consolidatePlayerTable() throws SQLException
    {
        String selectSql = 
            "SELECT p1.playername,p1.id FROM player AS p1, player AS p2"
                    +" WHERE p1.playername=p2.playername AND p1.id!=p2.id"; 
        
        startSQLOperations();
        pstmt = DBManager.instance.getConnection().prepareStatement(selectSql);
        pstmt.execute();
        results = pstmt.getResultSet();
        
        LinkedHashMap conflicts = new LinkedHashMap();
        while (results.next())
        {
            String playerName = results.getString("playername");
            Integer id = new Integer(results.getInt("id"));
            
            BList ids;
            if (conflicts.containsKey(playerName))
            {
                ids = (BList) conflicts.get(playerName);
            }
            else
            {
                ids = new BLinkedList();
                conflicts.put(playerName, ids);
            }
            
            ids.add(id);
        }
        endSQLOperations();
        
        for (Iterator i = conflicts.keySet().iterator(); i.hasNext(); )
        {
            String playerName = (String) i.next();
            BList ids = (BList) conflicts.get(playerName);
            Integer smallestId = (Integer) Collections.min(ids);
            ids.remove(smallestId);
            
            if (!ids.isEmpty())
            {
                startSQLOperations();
                String deleteSql = "DELETE FROM player WHERE "
                        +ids.reduce(new SQLOrEqualsWhereDefGenerator("id"));
                
                System.out.println("SQL: ["+deleteSql+"]");
                pstmt = DBManager.instance.getConnection().prepareStatement(deleteSql);
                pstmt.execute();
                endSQLOperations();
    
                updatePlayerRelations("game", "fk_creator_id", ids, smallestId);
                updatePlayerRelations("player_game", "fk_player_id", ids, smallestId);
                updatePlayerRelations("player_game_role", "fk_player_id", ids, smallestId);
            }
        }
    }

    public boolean verifyMigribility() throws SQLException
    {
        boolean migrible = super.verifyMigribility(); 
        if (!migrible)
        {
            return false;
        }
        
        String sqlStr;
        
        startSQLOperations();
        sqlStr = 
            "SELECT p1.playername FROM player AS p1,"+srcDb+".player AS p2"
            +" WHERE p1.playername = p2.playername";
        pstmt = DBManager.instance.getConnection().prepareStatement(sqlStr);
        pstmt.execute();
        results = pstmt.getResultSet();
        
        while (results.next())
        {
            String conflictingPlayerName = results.getString("playername");
            SimpleLogger.logInfo("Both databases contain player ["+conflictingPlayerName+"].  Merging.");
        }
        endSQLOperations();
        
        
        startSQLOperations();
        sqlStr = 
            "SELECT g1.name FROM game AS g1,"+srcDb+".game AS g2 WHERE g1.name = g2.name";
        pstmt = DBManager.instance.getConnection().prepareStatement(sqlStr);
        pstmt.execute();
        results = pstmt.getResultSet();
        
        while (results.next())
        {
            migrible = false;
            String conflictingGameName = results.getString("name");
            SimpleLogger.logError("Cannot merge game.  Both databases contain game ["+conflictingGameName+"]");
        }
        endSQLOperations();
        
        return migrible;
    }
    
    private String genInsertSelectSQLStr(String tblName) throws SQLException
    {
        StringBuffer sqlBuf = new StringBuffer();
        BList columns = getTableColumns(tblName);
        
        boolean firstElement = true;
        final Integer idOffset = (Integer) dstIdOffsets.get(tblName);
        
        for (ListIterator i=columns.listIterator(); i.hasNext(); )
        {
            String colName = (String)i.next();
            
            if (!firstElement)
            {
                sqlBuf.append(", ");
            }
            
            if (colName.equals("id"))
            {
                sqlBuf.append(colName+"+"+idOffset.toString());
            }
            else if (fkOffsets.containsKey(colName))
            {
                Integer keyOffset = (Integer) fkOffsets.get(colName);
                sqlBuf.append(colName+"+"+keyOffset.toString());
            }
            else
            {
                sqlBuf.append(colName);
            }
            
            firstElement = false;
        }
        
        return sqlBuf.toString();
    }
    
    private String genUpdateSQLStr(String tblName)
    {
        Integer srcIdOffset = (Integer) srcIdOffsets.get(tblName);
        return "UPDATE keyspace SET next_value=next_value+"
                +srcIdOffset.toString()+" WHERE tablename=\""+tblName+"\"";
    }
    
    private static Map getKeyOffsets() throws SQLException
    {
        return getKeyOffsets("");
    }
    
    private static Map getKeyOffsets(String db) throws SQLException
    {
        pstmt = null;
        results = null;
        Map maxKeys = new HashMap();
        
        if (db.length() > 0)
        {
            db = db + ".";
        }
        
        pstmt = DBManager.instance.getConnection().prepareStatement("SELECT * FROM "+db+"keyspace");
        pstmt.execute();
        results = pstmt.getResultSet();

        results.first();
        do
        {
            String tablename = results.getString("tablename");
            Integer offset = new Integer(results.getInt("next_value"));
            maxKeys.put(tablename, offset);
        } while (results.next());
        
        return maxKeys;
    }
    
    private static Map getFkRefs(Map refIds)
    {
        Map fkKeyIds = new HashMap();
        
        fkKeyIds.put("fk_action_id", refIds.get("action"));
        fkKeyIds.put("fk_contributor_id", refIds.get("role"));
        fkKeyIds.put("fk_creator_id", refIds.get("player"));
        fkKeyIds.put("fk_document_id", refIds.get("document"));
        fkKeyIds.put("fk_game_id", refIds.get("game"));
        fkKeyIds.put("fk_move_id", refIds.get("move"));
        fkKeyIds.put("fk_player_id", refIds.get("player"));
        fkKeyIds.put("fk_role_id", refIds.get("role"));
        
        fkKeyIds.put("inspired_id", refIds.get("move"));
        fkKeyIds.put("inspirational_id", refIds.get("move"));
        fkKeyIds.put("parent_id", refIds.get("discussion"));
        
        return fkKeyIds;
    }

    private static BList getTableColumns(String tblName) throws SQLException
    {
        BList columns = null;
        pstmt = null;
        results = null;

        String tblDescSql = "DESC "+tblName;
        pstmt = DBManager.instance.getConnection().prepareStatement(tblDescSql);
        pstmt.execute();
        
        results = pstmt.getResultSet();
        columns = new BLinkedList();
        while (results.next())
        {
            columns.add(results.getString("Field"));
        }
        
        return columns;
    }
        
    private static void updatePlayerRelations(String tblName, String columnName, BList fromValues, Object toValue)
            throws SQLException
    {
        StringBuffer sqlBuf = new StringBuffer();
        sqlBuf.append("UPDATE "+tblName+" SET "+columnName+"="+toValue.toString()+" WHERE ");
        sqlBuf.append( fromValues.reduce( new SQLOrEqualsWhereDefGenerator(columnName) ) );

        startSQLOperations();
        
        System.out.println("SQL: ["+sqlBuf.toString()+"]");
        pstmt = DBManager.instance.getConnection().prepareStatement(sqlBuf.toString());
        pstmt.execute();
        
        endSQLOperations();
    }
    
    private static class SQLOrEqualsWhereDefGenerator implements BCollectionReducer
    {
        private final StringBuffer sqlBuf;
        private final String columnName;
        
        public SQLOrEqualsWhereDefGenerator(String columnName)
        {
            sqlBuf = new StringBuffer();
            this.columnName = columnName;
        }
        
        public boolean reduce(Object o)
        {
            if (sqlBuf.length() != 0)
            {
                sqlBuf.append(" OR ");
            }
            
            sqlBuf.append(columnName+"="+o.toString());
            return false;
        }

        public Object getReduction()
        {
            return sqlBuf.toString();
        }
    }
    
    public static void main(String[] argv)
    {
        
        IvanhoeDataConverter idc = new IvanhoeDataConverter();
        String tableName = "no table selected";

        try
        {
            idc.setUp();
            
            String sourceDb;
            if (argv.length > 0)
            {
                sourceDb = argv[0];
            }
            else
            {
                throw new RuntimeException("Program requires database argument");
            }

            ServerGameSQLMigrator migrator = new ServerGameSQLMigrator(sourceDb);
         
            if (migrator.verifyMigribility())
            {
                SimpleLogger.logInfo("Game id offset from source: ["
                        +migrator.dstIdOffsets.get("game")+"]");
                
                for (int i=0; i<primaryTableNames.length; ++i)
                {
                    tableName = primaryTableNames[i];
                    migrator.migratePrimaryTable(tableName);
                }
                
                for (int i=0; i<secondaryTableNames.length; ++i)
                {
                    tableName = secondaryTableNames[i];
                    migrator.migrateSecondaryTable(tableName);
                }
                
                migrator.consolidatePlayerTable();
            }
        }
        catch (SQLException sqle)
        {
            SimpleLogger.logError("Error migrating table ["+tableName+"]", sqle);
        }
        catch (Exception e)
        {
            SimpleLogger.logError("Error in migrator", e);
        }
        finally
        {
            endSQLOperations();
            
            try
            {
                idc.tearDown();
            }
            catch (Exception e)
            {
                SimpleLogger.logError("Error tearing down IvanhoeDataConverter", e);
            }
        }
    }
}
