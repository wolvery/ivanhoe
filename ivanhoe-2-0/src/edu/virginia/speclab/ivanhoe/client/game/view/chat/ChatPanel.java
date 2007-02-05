/*
 * Created on Oct 3, 2003
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.chat;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.shared.message.*;

/**
 * @author lfoster
 *
 */
public class ChatPanel extends JPanel 
   implements ActionListener
{
   private JTextPane chatDisplay;
   protected JTextField chatEntry;
   private JButton closeChat;
   private boolean privateChat;
   private String currentPlayer,targetPlayer;
   
   /**
    * Construct the public chat panel
    */
   public ChatPanel()
   {
      super();
      
      this.privateChat = false;
      this.targetPlayer = "";
      
      initUI();
   }
   
   /**
    * Construct a private chat session with the specified player
    * @param player
    */
   public ChatPanel(String targetPlayer)
   {
      super();
      
      this.privateChat = true;
      this.targetPlayer = targetPlayer;

      initUI();
   }

   private void initUI()
   {
      this.setLayout( new BorderLayout() );
      this.chatDisplay = new JTextPane();
      this.chatDisplay.setEditable(false);
      
      //Initialize some styles.
      Style def = StyleContext.getDefaultStyleContext().
         getStyle(StyleContext.DEFAULT_STYLE);
      Style s = chatDisplay.addStyle("bold_blue", def);
      StyleConstants.setBold(s, true);
      StyleConstants.setForeground(s, Color.BLUE );
      Style s2 = chatDisplay.addStyle("bold_red", def);
      StyleConstants.setBold(s2, true);
      StyleConstants.setForeground(s2, Color.RED );
      Style s3 = chatDisplay.addStyle("bold", def);
      StyleConstants.setBold(s3, true);
      
      JScrollPane sp = new JScrollPane( this.chatDisplay );
      sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      this.add(sp, BorderLayout.CENTER);
      
      JPanel pnl = new JPanel();
      pnl.setLayout( new BoxLayout(pnl, BoxLayout.X_AXIS) );
      this.chatEntry = new JTextField();
      this.chatEntry.addKeyListener( new KeyAdapter()
         {
            public void keyPressed(KeyEvent evt)
            {
               if (evt.getKeyChar() == '\n' ||
                   evt.getKeyChar() == '\r' )
               {
                  sendChat();
               }
            }   
         });
         
      pnl.add(Box.createHorizontalStrut(10));
      pnl.add(this.chatEntry);
      
      JButton sendBtn = new JButton("send");
      sendBtn.addMouseListener(new MouseAdapter()
         {
            public void mouseClicked(MouseEvent evt)
            {
               sendChat();
            }
         });
      pnl.add(sendBtn);
      
      if (this.privateChat == true)
      {
         this.closeChat = new JButton("Close");
         this.closeChat.setFont(IvanhoeUIConstants.SMALL_FONT);
         this.closeChat.addActionListener(this);
         pnl.add(this.closeChat);
      }
      else
      {
         pnl.add(Box.createHorizontalStrut(10));
      }
      
      this.add(pnl, BorderLayout.SOUTH);
   }
   
   public boolean displayMessage(String sender, String txt)
   {
      Document doc = chatDisplay.getDocument();
   
      try
      {
         Style style;
         if (currentPlayer != null && sender.equals(currentPlayer))
         {
            style = chatDisplay.getStyle("bold_red");
         }
         else
         {
            style = chatDisplay.getStyle("bold_blue") ;  
         }
         
         doc.insertString(doc.getLength(), sender,style);
         doc.insertString(doc.getLength(), ": "+txt + "\n",null);
         chatDisplay.setSelectionStart(doc.getLength());
         return true;
      }
      catch (Exception e)
      {
         System.err.println("Unable to display chat: " + e);
         return false;
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      if (evt.getSource().equals(closeChat))
      {
         this.getParent().remove(this);
         this.chatEntry.requestFocus();
      }
   }

   private void sendChat()
   {
      String chat = this.chatEntry.getText();
      if (chat.length() > 0)
      {         
         ChatMsg msg = null;
         if (this.privateChat)
         {
            msg = new ChatMsg( this.currentPlayer, this.targetPlayer,chatEntry.getText());
         }
         else
         {
            msg = new ChatMsg( this.currentPlayer, chatEntry.getText());
         }
         Ivanhoe.getProxy().sendMessage(msg);
      }
      
      this.chatEntry.setText("");  
      this.chatEntry.requestFocus();
   }
   
	/**
	 * @param currentPlayer The currentPlayer to set.
	 */
	public void setCurrentPlayer(String currentPlayer)
	{
	    this.currentPlayer = currentPlayer;
	}
}
