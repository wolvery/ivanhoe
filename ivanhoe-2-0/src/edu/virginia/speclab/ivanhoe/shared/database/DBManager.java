/*
 * Created on Oct 7, 2003
 */
package edu.virginia.speclab.ivanhoe.shared.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.shared.*;

/**
 * @author lfoster
 * @author dmg2n@virginia.edu
 *
 * Singleton class to handle database connections and related resources
 */
public class DBManager
{
   private Connection   dbConnection;
   private String		dbName;
   private String       user;
   private String       pass;
   private String       host;
   private boolean      connected;   
   
   public static final DBManager   instance = new DBManager();
   
   private static final SimpleDateFormat dateFormat =
      new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
   
   /**
    * Check if the database connection is active
    * 
    * @return Boolean true if the database is connected
    */
   public boolean isConnected()
   {
      return this.connected;
   }
   
   /**
    * Retrieves the current active database connection or re-establishes
    * the connection if it is lost.
    * 
    * @return A java.sql.Connection object
    */
   public Connection getConnection()
   {
      try
      {
         if (this.dbConnection.isClosed())
         {
            SimpleLogger.logInfo("re-establishing database connection");
            connect(host, user, pass, dbName);
         }
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("unable to recreate database connection");
      }
      
      return this.dbConnection;
   }
   
   /**
    * Helper method to close a ResultSet
    * @param rs ResultSet to be closed
    */
   public void close(ResultSet rs)
   {
      try
      {
         if (rs != null)
            rs.close();
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Unable to close resultSet " + e);
      }
   }
   
   /**
    * Helper method to close a Statement
    * @param stmt Statement to be closed
    */
   public void close(Statement stmt)
   {
      try
      {
         if (stmt != null)
            stmt.close();
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Unable to close statement " + e);
      }
   }
   
   /**
    * Establishes a connection to the database
    * 
    * @param dbHost Hostname for the database
    * @param dbUser Login for the database
    * @param dbPass Password for the login
    * @return Boolean true if the connection and login succeeds
    */
   public boolean connect(String dbHost, String dbUser, String dbPass, String databaseName )
   {
         String param = "?user=" + dbUser + "&password=" + dbPass + 
         "&autoReconnect=true" + "&useUnicode=true&characterEncoding=UTF8";
         String connectionString = "jdbc:mysql://" + dbHost + "/" + databaseName + param;
         this.user = dbUser;
         this.pass = dbPass;
         this.host = dbHost;
         this.dbName = databaseName;
         
         return openDBConnection( connectionString );
   }
   
   /**
    * Connect anonymously to the specified database 
    * @param dbHost
    * @return
    */
   public boolean connect(String dbHost, String databaseName )
   {
       String param = "?autoReconnect=true";
       String connectionString = "jdbc:mysql://" + dbHost + "/" + databaseName + param;
       this.user = "";
       this.pass = "";
       this.host = "";
       this.dbName = databaseName;
       
       return openDBConnection( connectionString );              
   }
   
   private boolean openDBConnection( String connectionString )
   {
       try
       {
          SimpleLogger.logInfo("Attempting to make DB connection...");
          SimpleLogger.logInfo("Creating db connection to: " + connectionString);
          Class.forName("com.mysql.jdbc.Driver").newInstance();
          DriverManager.setLoginTimeout(3);
          this.dbConnection = DriverManager.getConnection(connectionString);             
          this.dbConnection.setAutoCommit(false);
          this.connected = true;
       }
       catch (Exception e)
       {
          SimpleLogger.logError("Unable to make DB connection", e);
          return false;
       }
       
       return true;
   }
      
   /**
    * This method wipes the existing database and replaces it with a blank one or 
    * creates a new database if none exists. Use with caution!
    * @throws MapperException
    */
   public void initializeDatabase( String scriptPath ) throws MapperException
    {
        Connection connection = getConnection();
        Statement statement = null;
        FileReader reader = null;

        try 
        {
            statement = connection.createStatement();
            
            // open the setup script
            File setupScript = new File(scriptPath);
            reader = new FileReader(setupScript);
            
            SimpleLogger.logInfo("Initializing database...");
            
            // load the contents of the setup script into a batch command.
            String line = "";            
            
            while( reader.ready() )
            {
                char c = (char) reader.read();               
                line += c;

                // watch for semicolons for statement endings
                if( c == ';')
                {
                    // Check that it's not JUST a semicolon
                    if (line.length() > 1)
                    {
//                        SimpleLogger.logInfo(line);
                        statement.addBatch(line);
                    }
                    line = "";
                }
            }

            // close the file
            reader.close();
            
            // execute the setup
            statement.executeBatch();

        }
        catch (SQLException e) 
        {
            throw new MapperException("Error initializing database: "+ e );
        } 
        catch (FileNotFoundException e) 
        {
            throw new MapperException("Error loading database setup script." );        
        } 
        catch (IOException e) 
        {
            throw new MapperException("Error reading database setup script." );
        }
        finally
        {
            close(statement);            
        }

    }
   
   /**
    * Disconnect and reconnect to the database using the same information
    * @return Whether the reconnection was successful
    */
   public boolean reconnect()
   {
       disconnect();
       return connect(host, user, pass, dbName);
   }
   
   /**
    * Closes the connection to the database
    */
   public void disconnect()
   {
      SimpleLogger.logInfo("Disconnecting DBManager..");
      try
      {
         this.dbConnection.close();
         SimpleLogger.logInfo("DBManager disconnected");
      }
      catch (SQLException e)
      {
         SimpleLogger.logError("Error closing DB: " + e);
      }
      finally
      {
          this.connected = false;
      }
   }
   
   /**
    * Return a date in the ivanhoe standard string format
    * @param date
    * @return
    */
   public static String formatDate(Date date)
   {
      return DBManager.dateFormat.format(date);
   }
   
   public static Date parseDate(String dateStr)
   {
      Date parsedDate = null;
      try
      {
         parsedDate = DBManager.dateFormat.parse(dateStr);
      }
      catch (ParseException e)
      {
         SimpleLogger.logError("Unable to parse date " + dateStr + ". Using NOW");
         parsedDate = new Date();
      }
      return parsedDate;
   }
}
