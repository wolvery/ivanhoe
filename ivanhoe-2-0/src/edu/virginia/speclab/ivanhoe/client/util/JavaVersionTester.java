/*
 * Created on Mar 1, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.util;

import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;

/**
 * @author Nick
 *
 * Tests the version of the running JRE
 */
public class JavaVersionTester
{
    public static boolean isVersionJavaFiveOrHigher()
    {
        String version = System.getProperty("java.version");
        
        if( version != null )
        {
            CharSequence majorVersionNumberString = version.subSequence(2,3);
            int majorVersionNumber = Integer.parseInt(majorVersionNumberString.toString());
            return ( majorVersionNumber >= 5 ); 
        }   
        else 
        {
            SimpleLogger.logError("Unable to detect java version, assuming java version < 1.5.");
            return false;        
        }
    }
    
    public static void main( String args[] )
    {
        System.out.println(isVersionJavaFiveOrHigher());
    }

}
