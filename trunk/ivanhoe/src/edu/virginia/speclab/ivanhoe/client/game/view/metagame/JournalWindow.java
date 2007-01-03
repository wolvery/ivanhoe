/*
 * Created on Jan 2, 2004
 *
 * JournalPanel
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.html.HTMLEditorKit;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.Journal;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeStyleInternalFrame;
import edu.virginia.speclab.ivanhoe.client.util.PropertiesManager;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;
import edu.virginia.speclab.ivanhoe.shared.data.Role;

/**
 * @author lfoster
 *
 * Simple panel that allows free form text entry for a player journal
 */
public class JournalWindow extends IvanhoeStyleInternalFrame 
{
   private Journal journal;
   private PropertiesManager propertiesManager;
   private RoleManager roleManager;
   
   private JEditorPane journalDisplay;
   private JTextArea objectiveDisplay;
   private JTextArea roleObjectiveEdit;
   private JTextArea roleDescriptionEdit;
   
   
   private class WelcomeMessageCheckMark extends JCheckBox implements ActionListener
   {       
       public WelcomeMessageCheckMark()
       {
           super("display welcome message when entering game.");
           this.addActionListener(this);
           this.setFont(IvanhoeUIConstants.TINY_FONT);
           
           // set the check according to the current setting in properties
           String welcomeString = propertiesManager.getProperty("welcome_message");           
           boolean welcomeFlag = Boolean.valueOf(welcomeString).booleanValue();           
           this.setSelected(welcomeFlag);           
       }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        propertiesManager.setProperty("welcome_message", this.isSelected() );
    }       
   }
      
   public JournalWindow( Journal journal, GameInfo gameInfo, PropertiesManager propertiesManager, RoleManager roleManager )
   {
      super("role journal");

      this.journal = journal;
      this.propertiesManager = propertiesManager;      
      this.roleManager = roleManager;
      
      // create roleplaying panel
      JPanel rolePlayPnl = new JPanel( new GridLayout(2,1));
      rolePlayPnl.add(createObjectivePanel());
      rolePlayPnl.add(createRolePanel());
     
      // create journal panel
      JPanel journalPnl = createJournalPanel();

      // populate the panels
      if( gameInfo != null ) 
          objectiveDisplay.setText( gameInfo.getObjectives());
      
      populateRoleInfo(roleManager.getCurrentRole());
      
      //TODO handle race if journal not yet loaded with callback
      URL journalURL = journal.getJournalFile();
      if(  journalURL != null )
      {
          try
          {
              journalDisplay.setPage(journalURL);    
          }
          catch( IOException e )
          {
              SimpleLogger.logError("Unable to load journal from temp file: "+journalURL.toString());
          }
      }
      else
      {
          SimpleLogger.logInfo("No previous journal loaded.");
      }

      // Layout panels
      getContentPane().removeAll();
      getContentPane().setLayout(new GridLayout(2,1));
      getContentPane().add(rolePlayPnl);
      getContentPane().add(journalPnl);
      
      // listen for editor close events
      addInternalFrameListener(new InternalFrameAdapter()
      {
         public void internalFrameClosing(InternalFrameEvent e)
         {
            handleClose();
         }         
      });      
   }
   
   private void handleClose()
   {
       // send updated role to the server
       Role currentRole = roleManager.getCurrentRole();
       currentRole.setObjectives(roleObjectiveEdit.getText());
       currentRole.setDescription(roleDescriptionEdit.getText());
       roleManager.sendRoleToServer(currentRole);
       
       // update journal
       journal.save( journalDisplay );
   }
   
   private JPanel createObjectivePanel()
   {
      JPanel objPnl = new JPanel( new BorderLayout());
      objPnl.setBorder(new TitledBorder(
         new EmptyBorder(0,0,0,0),
         "Rules and Objectives",
         TitledBorder.LEFT,
         TitledBorder.DEFAULT_POSITION,
         IvanhoeUIConstants.BOLD_FONT));
      this.objectiveDisplay = new JTextArea();
      this.objectiveDisplay.setEditable(false);
      this.objectiveDisplay.setWrapStyleWord(true);
      this.objectiveDisplay.setLineWrap(true);
      this.objectiveDisplay.setMargin(new Insets(2, 8, 2, 8));
      JScrollPane objSp = new JScrollPane(this.objectiveDisplay);
      objSp.setVerticalScrollBarPolicy(
         JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      objPnl.add(objSp, BorderLayout.CENTER);
      objPnl.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
      
      WelcomeMessageCheckMark checkMark = new WelcomeMessageCheckMark();
      checkMark.setBorder(new EmptyBorder(2,10,2,0));
      objPnl.add(checkMark,BorderLayout.SOUTH);
      
      return objPnl;
   }
   
   private JPanel createRolePanel()
   {
      JPanel rolePanel = new JPanel(new BorderLayout());
      rolePanel.setBorder(new TitledBorder(
         new EmptyBorder(0,0,0,0),
         "Role Information",
         TitledBorder.LEFT,
         TitledBorder.DEFAULT_POSITION,
         IvanhoeUIConstants.BOLD_FONT));
      JPanel cow = new JPanel(new BorderLayout());
      
      // create role name line
      JLabel roleName = new JLabel(
         "Role Name: " + roleManager.getCurrentRole().getName());
      roleName.setFont(IvanhoeUIConstants.SMALL_FONT);
      
      // create role desc
      JPanel descPnl = new JPanel( new BorderLayout());
      JLabel l2 = new JLabel("Description:");
      l2.setFont(IvanhoeUIConstants.SMALL_FONT);
      descPnl.add(l2, BorderLayout.NORTH);
      this.roleDescriptionEdit = new JTextArea();
      this.roleDescriptionEdit.setWrapStyleWord(true);
      this.roleDescriptionEdit.setLineWrap(true);
      this.roleDescriptionEdit.setFont(IvanhoeUIConstants.SMALL_FONT);
      JScrollPane descSp = new JScrollPane(this.roleDescriptionEdit);
      descSp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      descSp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      descPnl.add(descSp, BorderLayout.CENTER);
      
      // create role objectives
      JPanel objPnl = new JPanel( new BorderLayout());
      JLabel l3 = new JLabel("Objectives:");
      l3.setFont(IvanhoeUIConstants.SMALL_FONT);
      objPnl.add(l3, BorderLayout.NORTH);
      this.roleObjectiveEdit = new JTextArea();
      this.roleObjectiveEdit.setFont(IvanhoeUIConstants.SMALL_FONT);
      this.roleObjectiveEdit.setWrapStyleWord(true);
      this.roleObjectiveEdit.setLineWrap(true);
      JScrollPane objSp = new JScrollPane(this.roleObjectiveEdit);
      objSp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      objSp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      objPnl.add(objSp, BorderLayout.CENTER);
      
      // glom obj & desc into one panel
      JPanel glomfulness = new JPanel (new GridLayout(2,1,5,5));
      glomfulness.add(descPnl);
      glomfulness.add(objPnl);
      
      // add role stuff to content
      cow.add(roleName, BorderLayout.NORTH);
      cow.add(glomfulness, BorderLayout.CENTER);
      
      rolePanel.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
      rolePanel.add(cow, BorderLayout.CENTER);
      
      return rolePanel;
   }
   
   private JPanel createJournalPanel()
   {
      JPanel journalPnl = new JPanel( new BorderLayout() );
      journalPnl.setBorder(new TitledBorder(
         new EmptyBorder(0,0,0,0),
         "Roleplay Journal",
         TitledBorder.LEFT,
         TitledBorder.DEFAULT_POSITION,
         IvanhoeUIConstants.BOLD_FONT));
      JPanel contentPnl = new JPanel(new BorderLayout());
      
      // create main journal display
      this.journalDisplay = new JEditorPane();
      this.journalDisplay.setEditorKit( new HTMLEditorKit());
      this.journalDisplay.setMargin(new Insets(2, 8, 2, 8));
      JScrollPane journalSp = new JScrollPane(this.journalDisplay);
      journalSp.setVerticalScrollBarPolicy(
         JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      
      // Create the document toolbar
      JToolBar toolbar = new JToolBar();
      toolbar.setBackground(IvanhoeUIConstants.DARK_GRAY);
      
      // clipboard stuff
      Action act = this.journalDisplay.getActionMap().get(DefaultEditorKit.cutAction);
      act.putValue(AbstractAction.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/smallcut.gif"));
      addToolbarButton(toolbar, act);
      act =this.journalDisplay.getActionMap().get(DefaultEditorKit.copyAction);
      act.putValue(AbstractAction.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/smallcopy.gif"));
      addToolbarButton(toolbar, act);
      act =this.journalDisplay.getActionMap().get(DefaultEditorKit.pasteAction);
      act.putValue(AbstractAction.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/smallpaste.gif"));
      addToolbarButton(toolbar, act);
      toolbar.addSeparator();
      
      // styles
      act = this.journalDisplay.getActionMap().get("font-bold");
      act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/bold.gif"));
      addToolbarButton(toolbar, act);
      act = this.journalDisplay.getActionMap().get("font-italic");
      act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/italic.gif"));
      addToolbarButton(toolbar, act);
      act = this.journalDisplay.getActionMap().get("font-underline");
      act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/underline.gif"));
      addToolbarButton(toolbar, act);
      toolbar.addSeparator();
      
      // justify
      act = this.journalDisplay.getActionMap().get("left-justify");
      act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/leftJustify.gif"));
      addToolbarButton(toolbar, act);
      act = this.journalDisplay.getActionMap().get("center-justify");
      act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/centerJustify.gif"));
      addToolbarButton(toolbar, act);
      act = this.journalDisplay.getActionMap().get("right-justify");
      act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/rightJustify.gif"));
      addToolbarButton(toolbar, act);
      toolbar.setFloatable(false);
      
      contentPnl.add(journalSp, BorderLayout.CENTER);
      contentPnl.add(toolbar, BorderLayout.NORTH);
      
      journalPnl.add(contentPnl, BorderLayout.CENTER);
      journalPnl.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
      return journalPnl;
   }
   
   private void addToolbarButton(JToolBar bar, Action act)
   {
      JButton btn = new JButton(act);
      btn.setText(null);
      btn.setSize(17,17);
      btn.setOpaque(false);
      btn.setBorder(new EmptyBorder(3,3,3,3));
      bar.add(btn);
   }
   
   /**
    * @param role
    */
   private void populateRoleInfo(Role roleInfo)
   {
      this.roleDescriptionEdit.setText(roleInfo.getDescription());
      this.roleObjectiveEdit.setText(roleInfo.getObjectives());
   }

}
