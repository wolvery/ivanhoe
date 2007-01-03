/*
 * Created on Jul 14, 2004
 *
 * ChatNotifier
 */
package edu.virginia.speclab.ivanhoe.client.game.view.chat;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;

/**
 * @author lfoster
 *
 * This class provides a visual cue to player that chat msgs are available
 */
public class ChatNotifier extends JPanel implements ActionListener, IMessageHandler
{
   private JLabel chatIcon;
   private boolean ready;
   private boolean blink;
   private long lastBlinkTime;
   private final int blinkDelay = 1000;
   
   public ChatNotifier()
   {
      super();
      setOpaque(false);
      setVisible(true);
      
      setLayout(new BorderLayout());
      this.chatIcon = new JLabel(ResourceHelper.instance.getIcon("res/icons/phone.jpg"));
      add(this.chatIcon, BorderLayout.CENTER);
      this.chatIcon.setVisible(false);
      
      setSize(30,30);
      
      this.addMouseListener( new MouseAdapter()
         {
            public void mouseClicked(MouseEvent e)
            {
               if (ChatNotifier.this.blink == true)
               {
                  Workspace.instance.openChatWindow();
               }
            }
            
            public void mouseEntered(MouseEvent e)
            {
               if (ChatNotifier.this.blink)
               {
                  ChatNotifier.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
               }
            }
            
            public void mouseExited(MouseEvent e)
            {
               if (ChatNotifier.this.blink)
               {
                  ChatNotifier.this.setCursor(Cursor.getDefaultCursor());                  
               }
            }
         });
      
      // int
      this.lastBlinkTime = System.currentTimeMillis();
      
      // register for relevant messages
      Ivanhoe.registerGameMsgHandler(MessageType.READY, this);
      Ivanhoe.registerGameMsgHandler(MessageType.CHAT, this);
   }

   /**
    * Handle UI events!
    */
   public void actionPerformed(ActionEvent e)
   {
      if (e.getSource() instanceof Timer)
      {
         if (this.blink == true)
         {
            long currTime = System.currentTimeMillis();
            if (currTime -this.lastBlinkTime >= this.blinkDelay)
            {
               if (Workspace.instance.isChatVisible())
               {
                  this.chatIcon.setVisible(false);
                  this.blink = false;
               }
               else
               {
                  this.chatIcon.setVisible( !this.chatIcon.isVisible());
                  this.lastBlinkTime = currTime;
               }
            }
         }
      }
   }

   /**
    * Handle ready & chat msgs
    */
   public void handleMessage(Message msg)
   {
      if (msg.getType().equals(MessageType.READY))
      {
         SimpleLogger.logInfo("ChatNotifier is ready");
         this.ready = true;
      }
      else if (msg.getType().equals(MessageType.CHAT))
      {
         if (this.ready == true && Workspace.instance.isChatVisible() == false)
         {
            if (msg.getSender().equalsIgnoreCase("system") == false)
            {
               this.blink = true;
            }
         }
      }
   }
}
