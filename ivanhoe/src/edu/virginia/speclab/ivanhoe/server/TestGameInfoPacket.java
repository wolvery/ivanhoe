/*
 * Created on Jun 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.server;

import java.util.HashSet;
import java.util.Set;

import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;
import junit.framework.TestCase;

/**
 * @author lfoster
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestGameInfoPacket extends TestCase
{
   public void testStreaming()
   {
      int id = 57;
      String name = "TestName";
      String creator = "admin";
      String desc = "test description";
      String obj = "test objectives";
      String bogusServerName = "FakeServer";
      int port = 8080;
      int count = 500;
      boolean restricted = true;
      boolean archived = false;
      int startDocWeight = 1337;
      Set participants = new HashSet();
      participants.add("fakePlayer");
      GameInfo info = new GameInfo(id,name,creator,desc,obj,restricted,false,true,startDocWeight,participants);
      GameInfoPacket gi = new GameInfoPacket(info,bogusServerName,port,count);
      
      // Removed the old streaming version.  Do we need to unit test serializibility?
      
      
      assertTrue("Should be valid", gi.isValid());
      assertEquals("ID should be " + id, id, gi.getInfo().getId());
      assertEquals("Name should be " + name, name, gi.getInfo().getName());
      assertEquals("Restricted should be " + restricted, restricted, gi.getInfo().isRestricted());
      assertEquals("Archived should be " + archived, archived, gi.getInfo().isArchived());
      assertEquals("StartDocWeight should be " + startDocWeight, startDocWeight, gi.getInfo().getStartDocWeight());
      assertEquals("GameServer Name should be " + bogusServerName, 
         bogusServerName, gi.getGameServerName());
      assertEquals("Port should be " + port, port, gi.getPort());
      assertEquals("Count should be " + count, count, gi.getPlayerCount());
      assertEquals("Description should be " + desc, desc, gi.getInfo().getDescription());
      assertEquals("Objectives should be " + obj, obj, gi.getInfo().getObjectives());
      
      Set totalParts = new HashSet(participants);
      totalParts.add(creator);
      Set resultingParts = gi.getInfo().getParticipants();
      boolean partsEqual 
              = totalParts.containsAll(resultingParts) && totalParts.size() == resultingParts.size();
      
      assertTrue("Participants should be ["+totalParts+"]",partsEqual);
   }
}
