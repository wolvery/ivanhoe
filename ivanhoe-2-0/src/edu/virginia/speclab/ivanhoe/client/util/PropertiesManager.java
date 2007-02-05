/*
 * Created on Dec 8, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;

/**
 * @author Nick
 *
 * This class is responsible for retrieving and storing properties data for the entire
 * application. It manages the relationship between the default, read only properties
 * found in the application jar file and the writable properties stored locally. Local
 * properties take precedence, when they exist. 
 * 
 */
public class PropertiesManager
{
    private Properties localProperties, defaultProperties;
    
    private static final String PROPERTIES_FILE = "client.properties";
    private static final String DEFAULT_PROPERTIES_FILE = "res/default.client.properties";
    
    public PropertiesManager()
    {
		loadProperties();
    }
    
    private void loadProperties()
    {
        SimpleLogger.logInfo("Preparing to load client properties.");

        defaultProperties = ResourceHelper.instance.getProperties(DEFAULT_PROPERTIES_FILE);

        if (defaultProperties==null)
        {
           SimpleLogger.logError("Unable to open default properties resource.");
        }
        
        localProperties = new Properties();

		try
		{
			InputStream is = new FileInputStream(PROPERTIES_FILE);
		    localProperties.load(is);
		    is.close();
		    SimpleLogger.logInfo("Loaded client properties from local file.");
		} 
		catch (IOException e)
		{
		    SimpleLogger.logInfo("Client properties local file not found.");
		}
    }
    
    public String getProperty( String propertyName )
    {
        String localValue = null;
        String defaultValue = null;
        
        if( defaultProperties != null )
        {
             defaultValue = defaultProperties.getProperty(propertyName);
        }
        
        if( localProperties != null )
        {
            localValue = localProperties.getProperty(propertyName);
        }

        // return the local value if we have it, otherwise default, otherwise null.
        if( localValue == null )
        {
            // this too might be null, but that's fine
            return defaultValue;
        }
        else
        {
            return localValue;
        }        
    }

    public void setProperty( String propertyName, int propertyValue )
    {
        setProperty(propertyName,Integer.toString(propertyValue));
    }
    
    public void setProperty( String propertyName, boolean propertyValue )
    {
        setProperty(propertyName,Boolean.toString(propertyValue));
    }

    public void setProperty( String propertyName, String propertyValue )
    {
        if( localProperties == null )
        {
            localProperties = new Properties();
        }
        
        localProperties.setProperty(propertyName,propertyValue);
        saveLocalProperties();
    }
    
    /**
     * Resets the property to its default. (Removes local property entry)
     * @param propertyName property to reset
     */
    public void resetProperty( String propertyName )
    {
        localProperties.remove(propertyName);
        saveLocalProperties();
    }

    private void saveLocalProperties()
    {       
    	try
		{
			OutputStream os = new FileOutputStream(PROPERTIES_FILE);
			localProperties.store(os,"client properties");
			os.close();
		} catch (IOException e)
		{
		    SimpleLogger.logError("Unable to client properties!");
		}        
    }
    
    public String toString()
    {
        StringBuffer outString = new StringBuffer();
        
        outString.append("local properties: [");
        outString.append(localProperties != null ? localProperties.toString() : "null");
        outString.append("] default properties: [");
        outString.append(defaultProperties != null ? defaultProperties.toString() : "null");
        outString.append("]");
        
        return outString.toString();
    }
}
