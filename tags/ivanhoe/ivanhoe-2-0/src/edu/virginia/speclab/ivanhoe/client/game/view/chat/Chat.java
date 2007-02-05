/*
 * Created on Jul 9, 2004
 *
 * Chat
 */
package edu.virginia.speclab.ivanhoe.client.game.view.chat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.IRoleOnlineListener;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeStyleInternalFrame;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.message.*;


/**
 * @author lfoster
 *
 * This is the chat client for Ivanhoe. Handles chat in lobby & game.
 * Also shows public chat & private chat
 */
public class Chat extends IvanhoeStyleInternalFrame implements IMessageHandler, IRoleOnlineListener, ActionListener
{
   private JTabbedPane chatTabs;
   private ChatPanel publicChat;
   private DefaultListModel playerListModel;
   private JList playerList;
   private JButton privateChatBtn;
   private RoleManager roleManager;
   
   public Chat( RoleManager roleManager )
   {
      super("chat");

      this.roleManager = roleManager;
      roleManager.addOnlineListener(this);
      
      setSize(500,300);
      setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
      setVisible(false);
      
      createUI();
      
      initRoleList();
      
      // let the public channel know who the player is speaking as
      publicChat.setCurrentPlayer(roleManager.getCurrentRole().getName());
      
      // init message handlers
      Ivanhoe.registerGameMsgHandler(MessageType.CHAT, this);
   }
   
   private void createUI()
   {
       getContentPane().setLayout(new BorderLayout() );
       
       // create chat tabs
       this.chatTabs = new JTabbedPane();
       this.publicChat = new ChatPanel();
       this.chatTabs.add( this.publicChat, "game messages");
       this.chatTabs.setFont(IvanhoeUIConstants.SMALL_FONT);
       
       // create userList
       this.playerListModel = new DefaultListModel();
       this.playerList = new JList(this.playerListModel);
       JScrollPane sp = new JScrollPane(this.playerList);
       sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
       sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
       JPanel playerPanel = new JPanel(new BorderLayout());
       playerPanel.setBorder(
          new TitledBorder(
             new EtchedBorder(),
             "roles",
             TitledBorder.LEFT,
             TitledBorder.DEFAULT_POSITION,
             IvanhoeUIConstants.SMALL_FONT));
       this.privateChatBtn = new JButton("private chat");
       this.privateChatBtn.setFont(IvanhoeUIConstants.SMALL_FONT);
       this.privateChatBtn.addActionListener(this);
       playerPanel.add(sp, BorderLayout.CENTER);
       playerPanel.add(this.privateChatBtn, BorderLayout.SOUTH);
     
       // join list and tabs on a splitter
       JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
          this.chatTabs, playerPanel);

       // add components to window
       getContentPane().add(splitter, BorderLayout.CENTER);
       splitter.setDividerLocation(330);
       getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.EAST);
       getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.WEST);      
       getContentPane().add(Box.createVerticalStrut(10), BorderLayout.NORTH);      
       getContentPane().add(Box.createVerticalStrut(10), BorderLayout.SOUTH);      
   }
   
   /**
    * Requests focus for the input text field on the chat client.  This can
    * only be called successfully when the component is made visible.
    */
   public void inputRequestFocus()
   {
      this.publicChat.chatEntry.requestFocus();
   }
   
   /**
    * Handle messages from server
    */
   public void handleMessage(Message msg)
   {
      if (msg.getType().equals(MessageType.CHAT))
      {
         handleChat((ChatMsg)msg);
      }
   }

   /**
    * Chat notification.
    * Display public or private chat. Create private session if necessary
    * @param chat
    * @return
    */
   private void handleChat(ChatMsg chat)
   {
      if (chat.isPrivate() )
      {
         ChatPanel destChat = null;
         
         Role currentRole = roleManager.getCurrentRole();
         
         // is this an echo of a private message sent by this player?
         if (currentRole != null && chat.getSender().equals(currentRole.getName()))
         {
            int idx  = this.chatTabs.indexOfTab(chat.getTargetPlayer());
            if (idx > -1)
            {
               destChat = (ChatPanel)this.chatTabs.getComponentAt(idx);
            }
         }
         else
         {
            // this is a private chat from someone else
            int idx = this.chatTabs.indexOfTab(chat.getSender());
            if (idx > -1)
            {
               // grab existing session
               destChat = (ChatPanel)this.chatTabs.getComponentAt(idx);
            }
            else
            {
               // create a new session
               destChat = new ChatPanel(chat.getSender());
               destChat.setCurrentPlayer(roleManager.getCurrentRole().getName());
               this.chatTabs.add(destChat, chat.getSender());
            }
         }
         
         if (destChat!= null)
         {
            destChat.displayMessage(chat.getSpeakerRole(), chat.getChatText());
            this.chatTabs.setSelectedComponent(destChat);
         }
      }
      else
      {
         // just display the public chat
         this.publicChat.displayMessage(chat.getSpeakerRole(), chat.getChatText());
      }
   }

   /**
    * populate the player list with this list from the server
    * @param msg
    */
   private void initRoleList()
   {
      List onlineRoles = roleManager.getOnlineRoles();
       
      for (Iterator itr = onlineRoles.iterator(); itr.hasNext(); )
      {
         Role role = (Role) itr.next();          
         this.playerListModel.addElement(role.getName());
      }
   }

   /**
    * Handle UI actions
    */
   public void actionPerformed(ActionEvent e)
   {
      if (e.getSource().equals(this.privateChatBtn))
      {
         handleStartChat();
      }
   }

   /**
    * Start a priate chat with the selected player
    */
   private void handleStartChat()
   {
      int index = this.playerList.getSelectedIndex();
      if (index > -1)
      {
         String chatTgt = (String)this.playerListModel.get(index);
         ChatPanel newPnl = new ChatPanel(chatTgt);
         newPnl.setCurrentPlayer(roleManager.getCurrentRole().getName());
         
         this.chatTabs.add(newPnl, chatTgt);
         this.chatTabs.setSelectedComponent(newPnl);
         SimpleLogger.logInfo("Created new chat session with " + chatTgt);
      }
      else
      {
         Ivanhoe.showErrorMessage("Chat Error", 
            "Select a player to chat with before clicking the private chat button");
      }
   }
   
    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.model.metagame.IRoleOnlineListener#roleArrived(java.lang.String)
     */
    public void roleArrived(String name)
    {
       // add the name to the player list, and stick a chat message in the
       // public message panel
       this.playerListModel.addElement(name);
//       String disp = name + " has joined the game";
//       this.publicChat.displayMessage("System", disp);
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.model.metagame.IRoleOnlineListener#roleLeft(java.lang.String)
     */
    public void roleLeft(String name)
    {
   //     String txt = name + " has left the game";
        this.playerListModel.removeElement(name);
        int tabIndex = this.chatTabs.indexOfTab(name);
        if (tabIndex > -1)
        {
           this.chatTabs.remove(tabIndex);
        }

   //     this.publicChat.displayMessage("System", txt);   
    }
	
}
