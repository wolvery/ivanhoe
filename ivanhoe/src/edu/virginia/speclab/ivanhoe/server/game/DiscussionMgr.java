/*
 * Created on Mar 11, 2004
 *
 * Scholia
 */
package edu.virginia.speclab.ivanhoe.server.game;

import java.util.Iterator;
import java.util.List;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.mapper.DiscussionMapper;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DiscussionEntry;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.DiscussionEntryMsg;
import edu.virginia.speclab.ivanhoe.shared.message.DiscussionResponseMsg;

/**
 * @author lfoster
 *
 * Server-side discussion. Handles all discussion messages and 
 * db mapping
 */
public class DiscussionMgr implements IMessageHandler
{  
   private IvanhoeGame game;
   
   public DiscussionMgr(IvanhoeGame game)
   {
      this.game = game;
      SimpleLogger.logInfo("Discussion created  for " + game.getInfo().getName());
   }
   
   public void sendEntryToPlayer(UserProxy player)
   {
      SimpleLogger.logInfo("Sending discussion to " + player.getID());
      DiscussionMapper mapper = new DiscussionMapper(this.game.getId());
         
      try
      {
         List entries = mapper.getAllEntries();
         Iterator itr = entries.iterator();
         while (itr.hasNext())
         {
            player.sendMessage( 
               new DiscussionEntryMsg( (DiscussionEntry)itr.next()));  
         }
      }
      catch (MapperException e)
      {
         SimpleLogger.logError("Unable to get discussion entries: " + e);
      }
   }

   public void handleMessage(Message msg)
   {
      if (msg.getType().equals( MessageType.DISCUSSION_ENTRY))
      {
         handleEntry( (DiscussionEntryMsg)msg );
      }
   }

   private void handleEntry(DiscussionEntryMsg msg)
   {
      if (this.game.getInfo().isArchived())
      {
          SimpleLogger.logInfo("["+msg.getSender()
                  +"] tried to post discussion entry without proper permissions");
          DiscussionResponseMsg resp = new DiscussionResponseMsg(false,
                  "You do not have permission to post to the discussion in this game.");
          this.game.sendMessageTo(msg.getSender(), resp);
          return;
      }
      
      DiscussionMapper mapper = new DiscussionMapper( this.game.getId() );
      if (mapper.insert( msg.getEntry() ))
      {
         SimpleLogger.logInfo("Successful discussion entry from " + msg.getSender());
         DiscussionResponseMsg resp = new DiscussionResponseMsg(true, "Success");
         this.game.sendMessageTo( msg.getSender(), resp);
         this.game.broadcastMessage( msg );
      } 
      else
      {
         SimpleLogger.logInfo("Failed discussion entry from " + msg.getSender());
         DiscussionResponseMsg resp = new DiscussionResponseMsg(false, 
            "Unable to access database; try again later.");
         this.game.sendMessageTo( msg.getSender(), resp);
      }
   }
}
