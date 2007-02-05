/*
 * Created on May 5, 2005
 */
package edu.virginia.speclab.ivanhoe.server.mapper.converter.migrations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.virginia.speclab.ivanhoe.server.mapper.converter.IvanhoeDataConverter;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.blist.*;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author benc
 */
public final class SQLDataVerifier
{
    private final Map columnDependancies;
    private static final String[] tableNames1500 =
            {
                // primary tables
                "action",
                "bookmarks",
                "category",
                "discussion",
                // document is allowed to have references to a non-existant contributor -1
                "document",
                "game",
                "move",
                "player",
                "role",
                
                // secondary tables
                "action_document",
                "discourse_field",
                "document_image",
                // link_target is allowed to have references to a non-existant id 0
                "link_target",
                "move_action",
                "move_inspiration",
                "player_game",
                "player_game_role"
            };
    
    private static final String[] tableNames1600 =
    {
        // primary tables
        "action",
        "bookmarks",
        "category",
        "discussion",
        // document is allowed to have references to a non-existant contributor -1
        "document",
        "document_version",
        "game",
        "move",
        "player",
        "role",
        
        // secondary tables
        "action_version",
        "discourse_field",
        "document_image",
        // link_target is allowed to have references to a non-existant id 0
        "link_target",
        "move_action",
        "move_inspiration",
        "player_game",
        "player_game_role"
        
        // Moved "action_document" data to "action_version"
    };
    
    private final int dbVersion; 
    private static PreparedStatement pstmt;
    private static ResultSet results;
    private static boolean sqlDataInUse = false;
    
    // DO NOT CHANGE THIS NUMBER UNLESS YOU'VE VERIFIED THIS CLASS STILL WORKS!
    private static final Integer SUPPORTED_DB_VERSION_NUMBERS[] = 
            {
                new Integer(1500),
                new Integer(1600),
                new Integer(1700),
                new Integer(1800)
            };
    
    public SQLDataVerifier() throws SQLException
    {
        dbVersion = getDBVersion();
        Map fkReferenceColumns = findFkReferenceColumns(getFkRefs(dbVersion));
        columnDependancies = getOtherRefs(dbVersion);
        columnDependancies.putAll(fkReferenceColumns);
    }
    
    /**
     * @param fkReferenceColumns
     * @return
     */
    private Map findFkReferenceColumns(Map unqualifiedFkReferenceColumns) throws SQLException
    {
        BCollection allTables = getTableNames(dbVersion);
        Map qualifiedFks = new HashMap(unqualifiedFkReferenceColumns.size() * 3); // just a rule of thumb capacity
        Set keySet = unqualifiedFkReferenceColumns.keySet(); 
        
        for (Iterator i=allTables.iterator(); i.hasNext(); )
        {
            String tableName = (String)i.next();
            BList columns = getTableColumns(tableName);
            for (Iterator j=columns.iterator(); j.hasNext(); )
            {
                String columnName = (String)j.next();
                if (keySet.contains(columnName))
                {
                    qualifiedFks.put(
                            new ColumnReference(tableName, columnName),
                            unqualifiedFkReferenceColumns.get(columnName));
                }
            }
        }
        
        return qualifiedFks;
    }

    public boolean verifiable() throws SQLException
    {
        boolean verifiable = true;
        if (! Arrays.asList(SUPPORTED_DB_VERSION_NUMBERS).contains(new Integer(dbVersion)))
        {
            SimpleLogger.logError("Error: database has version ["+dbVersion/1000.0 
                    +"] which is unsupported by this command");
            verifiable = false;
        }
        return verifiable;
    }
    
    public boolean verifyData(PrintStream out, PrintStream records) throws SQLException
    {
        boolean clean = true;
        BLinkedList complexities = new BLinkedList();
        
        for (Iterator i=columnDependancies.keySet().iterator(); i.hasNext(); )
        {
            ColumnReference srcColRef = (ColumnReference) i.next();
            ColumnReference dstColRef = (ColumnReference) columnDependancies.get(srcColRef);
            
            Integer recordsAffected = getVerifierComplexity(srcColRef, dstColRef);
            complexities.addLast(recordsAffected);
        }
        
        int complexityComputed = 0;
        Integer totalComplexity = (Integer) complexities.reduce(new IntSumReducer());
        
        for (Iterator i=columnDependancies.keySet().iterator(); i.hasNext(); )
        {
            ColumnReference srcColRef = (ColumnReference) i.next();
            ColumnReference dstColRef = (ColumnReference) columnDependancies.get(srcColRef);
            Integer stepComplexity = (Integer) complexities.removeFirst();
            
            double percentage = 100.0*(complexityComputed / totalComplexity.doubleValue());
            percentage = Math.floor(percentage * 10) / 10;
            
            out.println("("+percentage+"% of rows processed)"
                    +" Checking ["+srcColRef+"] against ["+dstColRef+"]: "+stepComplexity+" rows");
            
            findOrphans(srcColRef, dstColRef, records);
            
            complexityComputed += stepComplexity.intValue();
        }
        
        return clean;
    }
    
    /**
     * @param srcColRef
     * @param dstColRef
     * @param records
     *      Output
     */
    private void findOrphans(ColumnReference srcColRef, ColumnReference dstColRef, PrintStream records)
            throws SQLException
    {
        String selectSQL = getSelectAbsenceString(srcColRef, dstColRef);
        
        boolean headerPrinted = false;
        
        startSQLOperations();
        pstmt = DBManager.instance.getConnection().prepareStatement(selectSQL);
        pstmt.execute();
        results = pstmt.getResultSet();
        
        while (results.next())
        {
            if (!headerPrinted)
            {
                records.println("Checking ["+srcColRef+"] against ["+dstColRef+"]:");
                headerPrinted = true;
            }
            
            records.println(srcColRef + " orphaned: " + results.getInt(srcColRef.getColumn()) );
        }
        endSQLOperations();

        if (headerPrinted)
        {
            records.println();
        }
    }

    /**
     * @param srcColRef
     * @param dstColRef
     * @return
     */
    private Integer getVerifierComplexity(ColumnReference srcColRef, ColumnReference dstColRef) throws SQLException
    {
        BList rows = new BLinkedList();
        String explainSQL = "EXPLAIN "+getSelectAbsenceString(srcColRef, dstColRef);
        
        SimpleLogger.logInfo("About to execute ["+explainSQL+"]");
        
        startSQLOperations();
        pstmt = DBManager.instance.getConnection().prepareStatement(explainSQL);
        pstmt.execute();
        results = pstmt.getResultSet();
        
        while (results.next())
        {
            rows.add( new Integer(results.getInt("rows")) );
        }
        endSQLOperations();
        
        return (Integer) rows.reduce(new IntProductReducer());
    }

    /**
     * @param srcColRef
     * @param dstColRef
     * @return
     */
    private String getSelectAbsenceString(ColumnReference srcColRef, ColumnReference dstColRef)
    {
        String srcTbl = srcColRef.getTable();
        String dstTbl = dstColRef.getTable();
        String srcCol = srcColRef.getColumn();
        String dstCol = dstColRef.getColumn();
        String srcWhereClause = srcColRef.getWhereClause().replaceAll(srcColRef.getTable(), "a");
        String dstWhereClause = dstColRef.getWhereClause().replaceAll(dstColRef.getTable(), "b");
        
        return "SELECT a.* FROM "+srcTbl+" AS a"
                +" LEFT JOIN "+dstTbl+" AS b ON a."+srcCol+"=b."+dstCol
                +" WHERE b."+dstCol+" IS NULL"
                +" "+srcWhereClause+" "+dstWhereClause;
    }

    private static int getDBVersion() throws SQLException
    {
        int dbVersion = 0;
        String sqlStr = "SELECT host_version FROM db_history";
        
        startSQLOperations();
        pstmt = DBManager.instance.getConnection().prepareStatement(sqlStr);
        pstmt.execute();
        results = pstmt.getResultSet();
        
        while (results.next())
        {
             int ver = (int)(1000 * Double.parseDouble(results.getString("host_version")));
             dbVersion = Math.max(ver, dbVersion);  
        }
        endSQLOperations();
        
        return dbVersion;
    }
    
    private static Map getFkRefs(int db_version)
    {
        Map fkKeyReferences = new HashMap();
        
        switch (db_version)
        {
        case 1600:
            fkKeyReferences.put("fk_document_version_id", new ColumnReference("document_version"));
        case 1500:
            fkKeyReferences.put("fk_action_id", new ColumnReference("action"));
            fkKeyReferences.put("fk_contributor_id", new ColumnReference("role"));
            fkKeyReferences.put("fk_creator_id", new ColumnReference("player"));
            fkKeyReferences.put("fk_document_id", new ColumnReference("document"));
            fkKeyReferences.put("fk_game_id", new ColumnReference("game"));
            fkKeyReferences.put("fk_move_id", new ColumnReference("move"));
            fkKeyReferences.put("fk_player_id", new ColumnReference("player"));
            fkKeyReferences.put("fk_role_id", new ColumnReference("role"));
            break;
        default:
        }
        
        return fkKeyReferences;
    }
    
    private static Map getOtherRefs(int db_version)
    {
        Map otherRefs = new HashMap();
        
        // References common to all versions
        otherRefs.put(new ColumnReference("move_inspiration","inspired_id"), new ColumnReference("move"));
        otherRefs.put(new ColumnReference("move_inspiration","inspirational_id"), new ColumnReference("move"));
        otherRefs.put(new ColumnReference("discussion","parent_id"), new ColumnReference("discussion"));

        // One to one relationships
        otherRefs.put(new ColumnReference("action"), new ColumnReference("move_action","fk_action_id"));
        otherRefs.put(new ColumnReference("role"), new ColumnReference("player_game_role", "fk_role_id"));
        otherRefs.put(new ColumnReference("action","id","AND action.fk_type=3"), new ColumnReference("link_target","fk_action_id"));


        // each document id should have precisely one entry in discourse_field
        otherRefs.put(new ColumnReference("document"), new ColumnReference("discourse_field","fk_document_id"));

        switch (db_version)
        {
        case 1800:
        case 1700:
        case 1600:
            // One to one relationship between action.id and action_version.fk_action_id
            otherRefs.put(new ColumnReference("action"), new ColumnReference("action_version","fk_action_id"));
            break;
        case 1500:
            // One to one relationship between action.id and action_document.fk_action_id
            otherRefs.put(new ColumnReference("action"), new ColumnReference("action_document","fk_action_id"));
            break;
        default:
            throw new IllegalArgumentException("Database version "+db_version+" not supported");
        }
        
        
        return otherRefs;
    }

    private static BCollection getTableNames(int dbVersion)
    {
        String[] names;
        switch (dbVersion)
        {
        case 1800:
        case 1700:
        case 1600:
            names = tableNames1600;
            break;
        case 1500:
            names = tableNames1500;
            break;
        default:
            throw new IllegalArgumentException("dbVersion "+dbVersion+" not supported by getTableNames()");
        }
        
        return new BArrayList(names);
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
        
    private static void startSQLOperations()
    {
        if (sqlDataInUse)
        {
            throw new RuntimeException("Tried to start new work with the SQL data objects already in use");
        }
        
        sqlDataInUse = true;
        pstmt = null;
        results = null;
    }
    
    private static void endSQLOperations()
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
    
    private static class ColumnReference implements Comparable
    {
        private final String table;
        private final String column;
        private final String whereClause;
        
        public ColumnReference(String table, String column, String whereClause)
        {
            this.table=table;
            this.column=column;
            this.whereClause = whereClause;
        }
        
        public ColumnReference(String table, String column)
        {
            this.table=table;
            this.column=column;
            this.whereClause = "";
        }
        
        public ColumnReference(String table)
        {
            this.table=table;
            this.column="id";
            this.whereClause="";
        }
        
        public String toString()
        {
            String qualifiedColumnRef = table + "." + column;
            if (whereClause.length() != 0)
            {
                return qualifiedColumnRef + " [WHERE... "+whereClause+"]";
            }
            else
            {
                return qualifiedColumnRef;
            }
        }
        
        public boolean equals(Object o)
        {
            return this.compareTo(o) == 0;
        }

        public int compareTo(Object o)
        {
            ColumnReference that = (ColumnReference)o;
            return this.toString().compareTo(that.toString());
        }
        
        public String getTable() { return table; }
        public String getColumn() { return column; }
        public String getWhereClause() { return whereClause; }
    }
    
    private static class IntProductReducer implements BCollectionReducer
    {
        private int total;
    
        public IntProductReducer()
        {
            total = 1;
        }
        
        public boolean reduce(Object o)
        {
            total *= ((Integer) o).intValue();
            return false;
        }

        public Object getReduction()
        {
            return new Integer(total);
        }
    }
    
    private static class IntSumReducer implements BCollectionReducer
    {
        private int sum;
        
        public IntSumReducer()
        {
            sum = 0;
        }
        
        public boolean reduce(Object o)
        {
            sum += ((Integer) o).intValue();
            return false;
        }

        public Object getReduction()
        {
            return new Integer(sum);
        }
    }
    
    public static void main(String[] argv)
    {
        
        IvanhoeDataConverter idc = new IvanhoeDataConverter();

        try
        {
            idc.setUp();

            SQLDataVerifier verifier = new SQLDataVerifier();
            if (verifier.verifiable())
            {
                FileOutputStream recordsOut = new FileOutputStream(new File("sql_orphans.txt"));
                verifier.verifyData(System.out, new PrintStream(recordsOut));
            }
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
