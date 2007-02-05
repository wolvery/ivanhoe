/*
 * Created on Mar 17, 2005
 */
package edu.virginia.speclab.ivanhoe.server.mapper.converter.migrations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.blist.*;
import edu.virginia.speclab.ivanhoe.shared.blist.lambda.NamesToFilesTransform;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author benc
 */
public class RestructureDocumentDirectories
{
    private static final String IVANHOE_DIR = System.getProperty("IVANHOE_DIR",".");
    private static final String PROPERTIES_FILE = IVANHOE_DIR+File.separator+"ivanhoe.properties";
    private static boolean dbWasInit = false;
    
    private RestructureDocumentDirectories() {}

    public static void main( String args[] )
    {
        SimpleLogger.initConsoleLogging();
        
        Properties ivanProperties = loadIvanhoeProperties();
        
        if (ivanProperties == null)
        {
            return;
        }
        
        String dbHost = ivanProperties.getProperty("dbHost");
        String dbUser = ivanProperties.getProperty("dbUser");
        String dbPass = ivanProperties.getProperty("dbPass");
        String dbName = ivanProperties.getProperty("dbName");
        String dfRoot = ivanProperties.getProperty("discourseFieldRoot");

        if (
                dbHost == null ||
                dbUser == null ||
                dbPass == null ||
                dbName == null ||
                dfRoot == null )
        {
            SimpleLogger.logError("Required property missing from properties file.");
            shutdown();
            return;
        }
                
        dfRoot = IVANHOE_DIR+File.separator+dfRoot;
        String docExts[] = {"htm", "html", "HTM", "HTML"};
        String imgExts[] = {"jpg", "gif",  "JPG", "GIF"};
        BCollection docFileList = getFilesByExtensions(dfRoot, docExts);
        BCollection imgFileList = getFilesByExtensions(dfRoot, imgExts);
        
        dbWasInit = DBManager.instance.connect(dbHost, dbUser, dbPass, dbName);
        
        if (!dbWasInit)
        {
            SimpleLogger.logError("Could not make DB connection.");
            shutdown();
        }
        
        BCollectionTransform gamesForDocFilesTransform = new BCollectionTransform()
        {
            public Object transform(Object o)
            {
                return getGameNameForDocFile((File)o, DBManager.instance.getConnection());
            }
        };
        BCollectionTransform gamesForImgFilesTransform = new BCollectionTransform()
        {
            public Object transform(Object o)
            {
                return getGameNameForImgFile((File)o, DBManager.instance.getConnection());
            }
        };
        
        BCollection docGamesList = (BList)docFileList.transform(gamesForDocFilesTransform);
        BCollection imgGamesList = imgFileList.transform(gamesForImgFilesTransform);
        
        BList fileList = new BArrayList(docFileList.size() + imgFileList.size());
        fileList.addAll(docFileList);
        fileList.addAll(imgFileList);
        
        BList gamesList = new BArrayList(docGamesList.size() + imgGamesList.size());
        gamesList.addAll(docGamesList);
        gamesList.addAll(imgGamesList);
        
        for (int i=0; i<fileList.size(); ++i)
        {
            String gameName = (String)gamesList.get(i);
            if (gameName == null)
            {
                // No game for this document.
                continue;
            }
            
            File docFile = (File)fileList.get(i);
            File gameDir = new File(dfRoot+File.separator+gameName);
            
            boolean directoryExists;
            
            if (!gameDir.exists())
            {
                directoryExists = gameDir.mkdir();
            }
            else
            {
                directoryExists = gameDir.isDirectory();
            }
            
            if (!directoryExists)
            {
                SimpleLogger.logError("Could not create directory ["+gameDir
                        +"]; skipping this game's files");
                continue;
            }
            
            File destFile = new File(gameDir.getPath() + File.separator + docFile.getName());
            
            boolean fileMoved = docFile.renameTo(destFile);
            
            if (!fileMoved)
            {
                SimpleLogger.logError("Could not move file ["+docFile+"] to ["
                        +destFile+"]");
                continue;
            }
        }
        
        shutdown();
        return;
    }

    private static BCollection getFilesByExtensions(String dfDirectoryName, String extensions[])
    {
        BCollection fileList = null;
        
        try
        {
            File dfDirectory = new File(dfDirectoryName);
            
            NamesToFilesTransform namesToFilesTransform = new NamesToFilesTransform(dfDirectoryName);
            
            BCollectionFilter fileFilter = new BCollectionFilter() 
            {
                public boolean accept(Object o)
                {
                    return !((File)o).isDirectory();
                }
            };
            
            MultiExtensionFilter filenameFilter = new MultiExtensionFilter(extensions);
            
            BArrayList filenameList = 
                new BArrayList( dfDirectory.list(filenameFilter) );
            fileList = filenameList.transform(namesToFilesTransform);
            fileList = fileList.filter(fileFilter);    
        }
        catch (Exception ioe)
        {
            SimpleLogger.logError("Error finding file in directory ["+dfDirectoryName+"]",
                    ioe);
            return null;
        }
        
        return fileList;
    }
    
    private static String getGameNameForDocFile(File documentFile, Connection dbConnection)
    {
        String filename = documentFile.getName();
        PreparedStatement stmt = null;
        ResultSet results = null;
        String gameName = null;
        
        try
        {
            stmt = dbConnection.prepareStatement(
                    "SELECT t3.name FROM "
                    + "document AS t1, "
                    + "discourse_field AS t2, " 
                    + "game AS t3 "
                    + "WHERE t1.file_name = ? " 
                    + "AND t1.id = t2.fk_document_id "
                    + "AND t2.fk_game_id = t3.id"
                    );
            stmt.setString(1, filename);
            results = stmt.executeQuery();
            
            if (results.first())
            {
                gameName = results.getString(1);
            }
            else
            {
                SimpleLogger.logInfo("File ["+filename+"] belonged to no game in the database");
            }
        }
        catch (SQLException sqle)
        {
            SimpleLogger.logError("Error while looking up document", sqle);
        }
        finally
        {
            DBManager.instance.close(results);
            DBManager.instance.close(stmt);
        }
        
        return gameName;
    }
    
    private static String getGameNameForImgFile(File imgFile, Connection dbConnection)
    {
        String filename = imgFile.getName();
        PreparedStatement stmt = null;
        ResultSet results = null;
        String gameName = null;
        
        try
        {
            stmt = dbConnection.prepareStatement(
                    "SELECT t3.name FROM "
                    + "document_image AS t1, "
                    + "discourse_field AS t2, " 
                    + "game AS t3 "
                    + "WHERE t1.file_name = ? " 
                    + "AND t1.fk_document_id = t2.fk_document_id "
                    + "AND t2.fk_game_id = t3.id"
                    );
            stmt.setString(1, filename);
            results = stmt.executeQuery();
            
            if (results.first())
            {
                gameName = results.getString(1);
            }
            else
            {
                SimpleLogger.logInfo("File ["+filename+"] belonged to no game in the database");
            }
        }
        catch (SQLException sqle)
        {
            SimpleLogger.logError("Error while looking up document", sqle);
        }
        finally
        {
            DBManager.instance.close(results);
            DBManager.instance.close(stmt);
        }
        
        return gameName;
    }
    
    private static void shutdown()
    {
        if (dbWasInit) DBManager.instance.disconnect();
    }
    
    private static Properties loadIvanhoeProperties()
    {
        Properties props = null;

        // Open the server proprties file 
        try
        {
           File f = new File(PROPERTIES_FILE);
           FileInputStream is = new FileInputStream(f);
           if (is == null)
           {
              SimpleLogger.logError("Unable to open properties file");
              return null;
           }
           else
           {
              props = new Properties();
              props.load(is);
           }
        }
        catch (IOException e1)
        {
           SimpleLogger.logError(
              "Unable to load properties file: " + e1.toString());
           return null;
        }

        return props;
    }
    
    private static class MultiExtensionFilter implements FilenameFilter 
    {
        private final String extensions[]; 
        public MultiExtensionFilter(String extensions[])
        {
            this.extensions = extensions;
        }
        
        public boolean accept(File dir, String filename)
        {
            for (int i=0; i<extensions.length; ++i)
            {
                if (filename.endsWith("." + extensions[i]))
                {
                    return true;
                }
            }
            return false;
        }
    }
}
