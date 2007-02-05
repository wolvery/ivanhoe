/*
 * Created on Oct 8, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.virginia.speclab.ivanhoe.server.mapper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.virginia.speclab.ivanhoe.server.exception.SequenceException;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author Duane Gran (dmg2n@virginia.edu)
 *
 * Simplifies the acquisition of the next primary key from a database
 */
public class KeySequence
{
   public static final KeySequence instance = new KeySequence();
   /**
   	* Retrieves the next primary key for a given table.  This is adatabase neutral
   	* approach where keys are pulled from a lookup table and then
   	* incremented.
   	*
   	* @param table The table name for acquiring a new primary key
   	*
   	* @throws SequenceException If a primary key for the table cannot be found
   	* @throws SQLException For general database errors
   	*
   	* @return An <code>Integer</code> representing the primary key
   	*/

   public synchronized Integer getNewKey(String table)
      throws SequenceException
   {
      Integer newKey = null;
      String sql =
         "SELECT * FROM keyspace WHERE tablename='" + table + "'";
      Statement stmt = null;
      ResultSet rset = null;
      Connection conn = DBManager.instance.getConnection();

      try
      {
         stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                                     ResultSet.CONCUR_UPDATABLE);
         rset = stmt.executeQuery(sql);

         if (rset.next())
         {
            newKey = new Integer(rset.getInt("next_value"));

            // update the key value in the database
            int newKeyValue = newKey.intValue();
            newKeyValue++;

            rset.updateInt("next_value", newKeyValue);
            rset.updateRow();
            
            conn.commit();
         }
         else
         {
            throw new SequenceException("No key available for table: " + table);
         }
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("KeySequence DB exception: " + e);
         throw new SequenceException("Database Problem", e);
      }
      finally
      {
      	 DBManager.instance.close(stmt);
      	 DBManager.instance.close(rset);
      }

      return newKey;
   }
}
