/*
 * Created on Mar 11, 2004
 *
 * ScholiaPanel
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame;

import java.awt.BorderLayout;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.DefaultEditorKit;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.Discussion;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeStyleInternalFrame;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.data.DiscussionEntry;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.DiscussionEntryMsg;
import edu.virginia.speclab.ivanhoe.shared.message.DiscussionResponseMsg;

/**
 * @author lfoster
 *
 * UI for in-gamme discussion board
 */
public class DiscussionWindow extends IvanhoeStyleInternalFrame implements ActionListener
{
   private JTable discussionTable;
   private Discussion model;
   private JEditorPane msgDisplay;
   private JButton newBtn;
   private JButton respondBtn;
   private int currSelection;
   
   public DiscussionWindow( Discussion discussion, boolean enabled )
   {
      super("discussion");

      this.model = discussion;
      this.currSelection = -1;
      this.getContentPane().setLayout(new BorderLayout(0, 0));

      createBrowsingPanel(enabled);
   }

   private void createBrowsingPanel(boolean enabled)
   {      
      this.discussionTable = new JTable(this.model);
      this.discussionTable.setColumnSelectionAllowed(false);
      this.discussionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      //Ask to be notified of selection changes.
      ListSelectionModel rowSM = this.discussionTable.getSelectionModel();
      rowSM.addListSelectionListener(new ListSelectionListener() 
         {
            public void valueChanged(ListSelectionEvent e) 
            {
               //Ignore extra messages.
               if (e.getValueIsAdjusting() == false)
               {
                  ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                  if (lsm.isSelectionEmpty() == false)  
                  {
                     int selectedRow = lsm.getMinSelectionIndex();
                     handleEntrySelected(selectedRow);
                  }
                  else
                  {
                     clearDetails();
                  }
               }
            }
         });

      this.discussionTable.getColumnModel().getColumn(0).setPreferredWidth(100);
      this.discussionTable.getColumnModel().getColumn(1).setPreferredWidth(50);
      this.discussionTable.getColumnModel().getColumn(2).setPreferredWidth(120);
      this.discussionTable.setFont(IvanhoeUIConstants.SMALL_FONT);

      JScrollPane listSp = new JScrollPane(this.discussionTable);
      listSp.setVerticalScrollBarPolicy(
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      listSp.setHorizontalScrollBarPolicy(
         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      listSp.setBorder(new TitledBorder(
            new EtchedBorder(),
            "entries",
            TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION,
            IvanhoeUIConstants.SMALL_FONT));

      this.msgDisplay = new JEditorPane();
      this.msgDisplay.setContentType("text/html");
      this.msgDisplay.setEditable(false);
      this.msgDisplay.setMargin(new Insets(2,8,2,8));
      JScrollPane displaySp = new JScrollPane(this.msgDisplay);
      displaySp.setVerticalScrollBarPolicy(
         JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      displaySp.setBorder(new TitledBorder(
            new EtchedBorder(),
            "message",
            TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION,
            IvanhoeUIConstants.SMALL_FONT));

      JSplitPane listSplit =
         new JSplitPane(JSplitPane.VERTICAL_SPLIT, listSp, displaySp);
      listSplit.setDividerLocation(300);
      

      JPanel controls = new JPanel();
      this.newBtn = new JButton("Create New Message");
      this.newBtn.setFont(IvanhoeUIConstants.SMALL_FONT);
      this.newBtn.addActionListener(this);
      this.newBtn.setEnabled(enabled);
      this.respondBtn = new JButton("Respond to Current Message");
      this.respondBtn.setFont(IvanhoeUIConstants.SMALL_FONT);
      this.respondBtn.setEnabled(false);
      this.respondBtn.addActionListener(this);
      this.respondBtn.setEnabled(enabled);
      controls.add(this.newBtn);
      controls.add(this.respondBtn);
      
      getContentPane().setLayout( new BorderLayout() );
      getContentPane().add(listSplit, BorderLayout.CENTER);
      getContentPane().add(controls, BorderLayout.SOUTH);
   }

   protected void clearDetails()
   {
      this.msgDisplay.setText("");
      this.respondBtn.setEnabled(false);
      this.currSelection = -1;
   }

   protected void handleEntrySelected(int row)
   {
      DiscussionEntry ent = this.model.getContent(row);
      
      // look up the name of the speaker
      RoleManager roleManager = model.getRoleManager();
      String speaker = roleManager.getRole(ent.getRoleID()).getName();

      StringBuffer out = new StringBuffer();
      out.append("<strong>Speaker:</strong> ").append(speaker);
      out.append("<br>");
      out.append("<strong>Date:</strong> ").append(ent.getPostingDate());
      out.append("<br>");
      out.append("<strong>Message:</strong> ").append(ent.getTitle());
      out.append("<hr>");
      String msg = ent.getMessage();
//      msg.replaceAll("<p>","");
//      msg.replaceAll("</p>", "<br>");
      out.append(msg);
      this.msgDisplay.setText(out.toString());
      this.respondBtn.setEnabled(true);
      this.currSelection = row;
   }

   public void actionPerformed(ActionEvent e)
   {
      RoleManager roleManager = model.getRoleManager();
      Role currentRole = roleManager.getCurrentRole();
      
      if (e.getSource().equals(this.newBtn))
      {
         
         EntryDialog ed = new EntryDialog(currentRole.getId());
         ed.show();
      }
      else if (e.getSource().equals(this.respondBtn))
      {
         if (this.currSelection > -1)
         {
            DiscussionEntry ent = this.model.getContent(this.currSelection);
            EntryDialog ed = new EntryDialog(currentRole.getId(),ent);
            ed.show();
         }
      }
   }
   
   private static class EntryDialog extends JDialog 
      implements ActionListener, IMessageHandler
   {
      private JButton submitBtn;
      private JButton cancelBtn;
      private JEditorPane entryTxt;
      private JTextField titleTxt;
      private int parentEntryId;
      private int currentRoleID;
      
      public EntryDialog( int currentRoleID )
      {
         super();
         this.setTitle("New Message");
                 
         setSize(300,275);
         Workspace.instance.centerWindowOnWorkspace(this);
         
         this.currentRoleID = currentRoleID;
         this.parentEntryId = -1;
         createEditPanel();
         
         Ivanhoe.registerGameMsgHandler(MessageType.DISCUSSION_RESPONSE,
            this);
      }
      
      public EntryDialog( int currentRoleID, DiscussionEntry respTo)
      {
         super();
         this.setTitle("New Response");
         setSize(300,275);         
         Workspace.instance.centerWindowOnWorkspace(this);
         
         this.currentRoleID = currentRoleID;
         this.parentEntryId = respTo.getId();
         createEditPanel();
   
         String title;
         if (respTo.getTitle().indexOf("RE:") == -1)
         {
            title = "RE: " + respTo.getTitle();
            if (title.length() > 50)
            {
               title = title.substring(0,46) + "...";
            }
         }
         else
         {
            title = respTo.getTitle();
         }
         
         this.titleTxt.setText(title);
         this.titleTxt.setEditable(false);
         
         
         Ivanhoe.registerGameMsgHandler(MessageType.DISCUSSION_RESPONSE,
            this);
      }
      
      private void createEditPanel()
      {
         this.getContentPane().setLayout(new BorderLayout( ));
         
         JPanel main = new JPanel( new BorderLayout() );
         JPanel headerPnl = new JPanel( new BorderLayout() );
         JLabel lbl1 = new JLabel("Message: ");
         lbl1.setFont(IvanhoeUIConstants.SMALL_FONT);
         headerPnl.add( lbl1, BorderLayout.NORTH);  
         this.titleTxt = new JTextField();
         headerPnl.add(this.titleTxt, BorderLayout.CENTER);  
         main.add( headerPnl, BorderLayout.NORTH);
      
         JPanel entryPnl = new JPanel( new BorderLayout() );
         JLabel lbl = new JLabel("Content: ");
         lbl.setFont(IvanhoeUIConstants.SMALL_FONT);
         entryPnl.add( lbl, BorderLayout.NORTH);
         this.entryTxt = new JEditorPane();
         this.entryTxt.setEnabled(true);
         this.entryTxt.setContentType("text/html");
         this.entryTxt.setMargin( new Insets(2,8,2,8));
         JScrollPane sp = new JScrollPane( this.entryTxt );
         sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         entryPnl.add( sp, BorderLayout.CENTER );
         main.add( entryPnl, BorderLayout.CENTER); 
         
         JPanel controls = new JPanel();
         this.submitBtn = new JButton("Submit");
         this.submitBtn.setFont(IvanhoeUIConstants.SMALL_FONT);
         this.submitBtn.addActionListener(this);
         this.cancelBtn = new JButton("Cancel");
         this.cancelBtn.setFont(IvanhoeUIConstants.SMALL_FONT);
         this.cancelBtn.addActionListener(this);
         controls.add(this.cancelBtn);
         controls.add(this.submitBtn); 
         this.getContentPane().add( controls, BorderLayout.SOUTH);  
         
         // Create the document toolbar
         JToolBar toolbar = new JToolBar();
         
         // clipboard stuff
         Action act = this.entryTxt.getActionMap().get(DefaultEditorKit.cutAction);
         act.putValue(AbstractAction.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/smallcut.gif"));
         addToolbarButton(toolbar, act);
         act =this.entryTxt.getActionMap().get(DefaultEditorKit.copyAction);
         act.putValue(AbstractAction.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/smallcopy.gif"));
         addToolbarButton(toolbar, act);
         act =this.entryTxt.getActionMap().get(DefaultEditorKit.pasteAction);
         act.putValue(AbstractAction.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/smallpaste.gif"));
         addToolbarButton(toolbar, act);
         toolbar.addSeparator();
         
         // styles
         act = this.entryTxt.getActionMap().get("font-bold");
         act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/bold.gif"));
         addToolbarButton(toolbar, act);
         act = this.entryTxt.getActionMap().get("font-italic");
         act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/italic.gif"));
         addToolbarButton(toolbar, act);
         act = this.entryTxt.getActionMap().get("font-underline");
         act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/underline.gif"));
         addToolbarButton(toolbar, act);
         toolbar.addSeparator();
         
         // justify
         act = this.entryTxt.getActionMap().get("left-justify");
         act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/leftJustify.gif"));
         addToolbarButton(toolbar, act);
         act = this.entryTxt.getActionMap().get("center-justify");
         act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/centerJustify.gif"));
         addToolbarButton(toolbar, act);
         act = this.entryTxt.getActionMap().get("right-justify");
         act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/rightJustify.gif"));
         addToolbarButton(toolbar, act);
         toolbar.setFloatable(false);
         
         getContentPane().add(toolbar, BorderLayout.NORTH);
         getContentPane().add(main,BorderLayout.CENTER);
         getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.EAST);
         getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.WEST); 
      }
      
      private void addToolbarButton(JToolBar bar, Action act)
      {
         JButton btn = new JButton(act);
         btn.setText(null);
         bar.add(btn);
      }
      
      public void actionPerformed(ActionEvent e)
      {
         if (e.getSource().equals(this.cancelBtn))
         {
            dispose();
         }
         else
         {
            this.submitBtn.setEnabled(false);
            String title = this.titleTxt.getText();
            String body = this.entryTxt.getText();
            if (title.length() > 50)
            {
                Ivanhoe.showErrorMessage(
                  "Discussion title too long; please limit to 50 characters");
               this.submitBtn.setEnabled(true);
               return;
            }
            if (title.length() > 0 && body.length() > 0)
            {
               DiscussionEntry entry = new DiscussionEntry(
                  title, currentRoleID, Ivanhoe.getDate(),
                  body, this.parentEntryId);
               DiscussionEntryMsg msg = new DiscussionEntryMsg(entry);
               Ivanhoe.getProxy().sendMessage( msg );
            }
            else
            {
               Ivanhoe.showErrorMessage("Missing data for discussion entry");
            }
         }
      }

      public void handleMessage(Message msg)
      {
         if (msg.getType().equals(MessageType.DISCUSSION_RESPONSE))
         {
            DiscussionResponseMsg resp = (DiscussionResponseMsg)msg;
            if (resp.isSuccess())
            {
               dispose();
            }
            else
            {
               Ivanhoe.showErrorMessage(resp.getMessage());
               this.submitBtn.setEnabled(true);
            }
         }
      }
   }
}
