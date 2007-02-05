/*
 * Created on Jun 21, 2004
 *
 * Messenger
 */
package edu.virginia.speclab.ivanhoe.server.game;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.mapper.DocumentVersionMapper;
import edu.virginia.speclab.ivanhoe.server.mapper.MoveMapper;
import edu.virginia.speclab.ivanhoe.server.mapper.UserMapper;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;
import edu.virginia.speclab.ivanhoe.shared.data.User;
import edu.virginia.speclab.ivanhoe.shared.message.MoveMsg;

/**
 * @author lfoster
 * 
 * Messegner is in charge of sending email notifications to ivanhoe game players
 */
public class Messenger
{
   private String mailHost;
   private String mailFromAddress;
   private boolean enabled;
   private IvanhoeGame game;
   private String serverName;

   /**
    * Construct a messager for the specified game
    * @param game
    */
   public Messenger(String serverName, IvanhoeGame game)
   {
      this.game = game;
      this.serverName = serverName;
   }
   
   /**
    * Configure the messenger for sending mails
    * 
    * @param mailHost
    * @param fromAddress
    */
   public void configure(boolean isEnabled, String host, String from)
   {
      this.enabled = isEnabled;
      this.mailHost = host;
      this.mailFromAddress = from;
      
      if (this.enabled)
      {
         SimpleLogger.logInfo("Email notification is ENABLED");
      }
      else
      {
         SimpleLogger.logInfo("Email notification is DISABLED");
      }
   }
   
   /**
    * Send out move notifaction emails to all players in the game
    * @param submittingPlayer
    */
   public void sendMoveNotification(MoveMsg move)
   { 
      String message = generateMoveEmail( move);
      
      try
      {   
         List recipientNames = null;
         if (this.game.getInfo().isRestricted())
         {
            SimpleLogger.logInfo("Sending email notification of move by "
               + move.getSender() + " to all authorized players");            
            recipientNames = UserMapper.getAllUserNames(this.game.getGameId());
         }
         else
         {
            SimpleLogger.logInfo("Sending email notification of move by "
               + move.getSender() + " to active players");
            
            recipientNames = MoveMapper.getActivePlayers(this.game.getGameId());
         }
         
         for (Iterator itr = recipientNames.iterator();itr.hasNext();)
         {
            String toName = (String)itr.next();
            sendMail(toName, message);
         }
      }
      catch (MapperException e)
      {
         SimpleLogger.logError("Unable to get list of players for email noty: " + e.toString());
      }
   }

   /**
    * @param move
    * @return
    */
   private String generateMoveEmail(MoveMsg move)
   {
      StringBuffer message = new StringBuffer();
      message.append("Player \"").append(move.getMove().getRoleName());
      message.append("\" has published a move in the Ivanhoe game \""); 
      message.append(this.game.getInfo().getName()).append("\" running on server \"");
      message.append(this.serverName).append("\".\n\n");
      message.append("Narrative:\n");
      String desc = move.getMove().getDescription().replaceAll("\n"," ");
      // no longer HTML
//      int p1 = htmlDesc.indexOf("<body>");
//      int p2 = htmlDesc.indexOf("</body>");
//      message.append(htmlDesc.substring(p1+6,p2)).append("\n\n");   
      message.append( desc + "\n\n");
      message.append("Action Summary:\n");
      
      int num = 1;
      for (Iterator i=move.getMove().getActions(); i.hasNext();)
      {
         final IvanhoeAction act = (IvanhoeAction)i.next();
         final int docVersionID = act.getDocumentVersionID();
         try
         {
             final DocumentVersion docVersion =
                     DocumentVersionMapper.getDocumentVersion(docVersionID);
             message.append("   ").append(num++).append(". ").append(act.toString(docVersion)).append("\n");
         }
         catch (MapperException me)
         {
             SimpleLogger.logError("Error finding document version ["+docVersionID+"]", me);
         }
      }
      
      return message.toString();
   }
   
   /**
    * Send an email with the body <code>message</code> to the specified player
    * @param playerName The player name to receive the email
    * @param message The text of the message
    */
   public void sendMail(String playerName, String message)
   {
      sendMail(playerName, message, "Ivanhoe Notification");
   }

   /**
    * Send an email with the body <code>message</code> to the specified player
    * @param playerName The player name to receive the email
    * @param message The text of the message
    * @param subject Subject line of the email
    */
   public void sendMail(String playerName, String message, String subject)
   {
      if (this.enabled == false)
      {
         SimpleLogger.logInfo("Mail is not enabled");
         return;
      }
      
      User player = null;
      try
      {
         player = UserMapper.getByName( playerName );
      }
      catch (MapperException e)
      {
         SimpleLogger.logInfo("Unable to locate mail target: " + playerName);
         return;
      }
      
      // create some properties and get the default Session
      Properties props = new Properties();
      props.put("mail.smtp.host", this.mailHost);
      Session session = Session.getInstance(props, null);

      SimpleLogger.logInfo("Sending mail to " 
         + player.getUserName() + " - " + player.getEmail());
      
      try
      {
         // create a message
         Message msg = new MimeMessage(session);
         msg.setFrom(new InternetAddress(this.mailFromAddress));
         InternetAddress address = new InternetAddress(player.getEmail());
         msg.setRecipient(Message.RecipientType.TO, address);
         msg.setSubject(subject);
         msg.setSentDate(new Date());
         msg.setText(message);
         
         // send it
         Transport.send(msg);
      }
      catch (MessagingException mex)
      {
         SimpleLogger.logError("Unable to send email notification: " + mex.getMessage());
      }
   }
}