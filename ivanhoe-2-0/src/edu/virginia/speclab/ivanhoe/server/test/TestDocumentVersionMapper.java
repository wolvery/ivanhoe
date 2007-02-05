package edu.virginia.speclab.ivanhoe.server.test;

import java.util.Date;
import java.util.HashSet;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.game.IvanhoeServer;
import edu.virginia.speclab.ivanhoe.server.mapper.DiscourseFieldMapper;
import edu.virginia.speclab.ivanhoe.server.mapper.DocumentMapper;
import edu.virginia.speclab.ivanhoe.server.mapper.DocumentVersionMapper;
import edu.virginia.speclab.ivanhoe.server.mapper.GameMapper;
import edu.virginia.speclab.ivanhoe.server.mapper.RoleMapper;
import edu.virginia.speclab.ivanhoe.server.mapper.UserMapper;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;
import edu.virginia.speclab.ivanhoe.shared.data.User;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;
import junit.framework.TestCase;

public class TestDocumentVersionMapper extends TestCase
{

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        IvanhoeServer.initLogging(true);
        
        // connect DB
        if (DBManager.instance.connect("localhost","ivanhoe","gam3","ivanhoe_test") == false )
        {
           SimpleLogger.logError("Unable to connect DB");
           fail();
        }

        String scriptPath = System.getProperty("IVANHOE_DIR",".") + "/scripts/test_setup.sql"; 
        DBManager.instance.initializeDatabase(scriptPath);        
    }
    
    public void testDocumentVersion()
    {
        // test objects        
        Date testDate = new Date(1000);
        
        User userData = new User(1, "creator", "pass", "lastName", "firstName",                
                "email", "affiliation", false);
        
        GameInfo gameInfo = new GameInfo(1, "game","creator","description","objectives", false, false, false, 1, new HashSet());

        DocumentInfo docInfo = new DocumentInfo(1, "file", "name", "author",
                "provenance", "contributor", 1, new Date(), false, true );

        DocumentVersion testVersion = new DocumentVersion( 100, docInfo.getTitle(), "role", 1, testDate, 99, true);

        // mappers
        GameMapper gameMapper = new GameMapper();
        DiscourseFieldMapper dfMapper = new DiscourseFieldMapper();
        
        try
        {
            // setup test
            if (! UserMapper.createAccount(userData))
            {
                fail();
            }
            
            gameMapper.create(gameInfo);
            RoleMapper.newRole("role",1,gameInfo.getId(),true);
            DocumentMapper.add(docInfo);
            dfMapper.addStartingDocument(gameInfo.getName(),docInfo.getTitle(),docInfo.getId().intValue());
            
            // read and write a doc version from the db
            DocumentVersionMapper.writeDocumentVersion(testVersion,gameInfo.getId());
            DocumentVersion resultVersion = DocumentVersionMapper.getDocumentVersion(100);
            
            // test values
            assertNotNull(resultVersion);
            assertEquals(resultVersion.getDocumentTitle(),docInfo.getTitle());
            assertEquals(resultVersion.getRoleID(),1);
            assertEquals(resultVersion.getRoleName(),"role");
            assertTrue(resultVersion.isOwner(1));
            assertTrue(resultVersion.getDate().equals(testDate));
            assertEquals(resultVersion.getParentID(),99);
            
            // test action -> version map
            DocumentVersionMapper.mapActionToDocumentVersion( 1000, 100 );
            int versionID = DocumentVersionMapper.getDocumentVersionID(1000);
            
            assertEquals(versionID,100);
            
        } 
        catch (MapperException e)
        {
            fail();
        }
        
    }
    
    

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        DBManager.instance.disconnect();
    }
}
