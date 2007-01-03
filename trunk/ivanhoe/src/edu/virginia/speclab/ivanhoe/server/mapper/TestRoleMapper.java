/*
 * Created on Nov 19, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.server.mapper;

import java.awt.Color;
import java.util.List;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.game.IvanhoeServer;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.data.User;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

import junit.framework.TestCase;

/**
 * @author Nick
 *
 * Unit test covering the RoleMapper.
 */
public class TestRoleMapper extends TestCase
{

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        IvanhoeServer.initLogging(true);
        
        // connect DB
        if (DBManager.instance.connect("localhost","test_ivanhoe") == false )
        {
           SimpleLogger.logError("Unable to connect DB");
           fail();
        }

        String scriptPath = System.getProperty("IVANHOE_DIR",".") + "/scripts/test_setup.sql"; 
        DBManager.instance.initializeDatabase(scriptPath);        
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        DBManager.instance.disconnect();
    }

    public void testRole()
    {
        SimpleLogger.logInfo("Testing RoleMapper...");
        
        int gameID = 1;
        String playerName = "test";
        String playerDesc = "this guy";
        String playerObj = "no bugs";
        Color stroke = Color.BLACK;
        Color fill = Color.WHITE;
        boolean writePerm = true;
        
        try 
        {
            User user = new User(playerName,"pass","lastName","firstName","email","affiliation");
            if ( !UserMapper.createAccount(user) )
            {
                fail();
            }
            
            User userDB = UserMapper.getByName(playerName);
            
            Role role = RoleMapper.newRole(playerName,userDB.getId(),gameID,writePerm);
            
            role.setDescription(playerDesc);
            role.setObjectives(playerObj);
            role.setFillPaint(fill);
            role.setStrokePaint(stroke);

            Role newRole = new Role(role.getId(), playerName, playerDesc, playerObj, stroke, fill, writePerm);
            
            RoleMapper.updateRole(newRole);
            
            List roleList = RoleMapper.getGameRoles(gameID);            
            Role roleDB = (Role) roleList.get(0);

            assertTrue( roleDB.getId() == role.getId() );
            assertTrue( roleDB.getName().equals(role.getName()));
            assertTrue( roleDB.getObjectives().equals(role.getObjectives()));
            assertTrue( roleDB.getFillPaint().equals(role.getFillPaint()));
            assertTrue( roleDB.getStrokePaint().equals(role.getStrokePaint()));
            assertTrue( roleDB.hasWritePermission() == writePerm );
            
        } catch (MapperException e) 
        {        
            e.printStackTrace();
            fail();
        }
    }

}
