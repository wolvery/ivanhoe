/*
 * Created on Dec 8, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.test;

import edu.virginia.speclab.ivanhoe.client.util.PropertiesManager;
import junit.framework.TestCase;

/**
 * @author Nick
 *
 */
public class TestPropertiesManager extends TestCase
{
    protected PropertiesManager propertiesManager;
    
    protected void setUp() throws Exception
    {
        super.setUp();
        propertiesManager = new PropertiesManager();     
        propertiesManager.setProperty("host", "localhost");
    }
    
    public void testGetProperty()
    {
        assertNotNull( propertiesManager.getProperty("host") );        
    }

    /*
     * Class under test for void setProperty(String, int)
     */
    public void testSetPropertyStringint()
    {
        propertiesManager.setProperty("TestNum",5);
        int testNum = Integer.valueOf(propertiesManager.getProperty("TestNum")).intValue();        
        assertTrue( testNum == 5 );
        propertiesManager.resetProperty("TestNum");
        String test = propertiesManager.getProperty("TestNum");
        assertNull(test);
    }

    /*
     * Class under test for void setProperty(String, boolean)
     */
    public void testSetPropertyStringboolean()
    {
        propertiesManager.setProperty("TestBool",true);
        boolean testBool= Boolean.valueOf(propertiesManager.getProperty("TestBool")).booleanValue();        
        assertTrue( testBool );
        propertiesManager.resetProperty("TestBool");
        String test = propertiesManager.getProperty("TestBool");
        assertNull(test);
    }

    /*
     * Class under test for void setProperty(String, String)
     */
    public void testSetPropertyStringString()
    {
        propertiesManager.setProperty("TestString","test");
        String testString = propertiesManager.getProperty("TestString");        
        assertTrue( testString.equals("test") );
        propertiesManager.resetProperty("TestString");
        String test = propertiesManager.getProperty("TestString");
        assertNull(test);
        
        String localHost = propertiesManager.getProperty("host");
        assertNotNull(localHost);
        propertiesManager.resetProperty("host");
        String defaultHost = propertiesManager.getProperty("host");
        assertNotNull(defaultHost);
        
        propertiesManager.setProperty("host","testname");
        String testName = propertiesManager.getProperty("host");
        assertTrue( testName.equals("testname") );
        
        propertiesManager.resetProperty("host");
        String testName2 = propertiesManager.getProperty("host");
        assertTrue( defaultHost.equals(testName2) );
        
        propertiesManager.setProperty("host",localHost);
    }    

    /*
     * Class under test for void resetProperty()
     */
    public void testResetProperty()
    {
        propertiesManager.setProperty("TestString","test");
        String testString = propertiesManager.getProperty("TestString");        
        assertTrue( testString.equals("test") );
        propertiesManager.resetProperty("TestString");
        String testReset = propertiesManager.getProperty("TestString");
        assertNull(testReset);
    }

}
