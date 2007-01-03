/*
 * Created on Apr 28, 2004
 *
 * PopoutMenu
 */
package edu.virginia.speclab.ivanhoe.client.game.view.gameaction;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*;
import javax.swing.*;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.IvanhoeGame;
import edu.virginia.speclab.ivanhoe.client.game.view.IvanhoeFrame;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.BlackBox;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeButton;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;

/**
 * @author lfoster
 *
 * An animated toolbar containing all global ivanhoe action
 */
public class GameActionToolbar extends BlackBox
{
   private MouseAdapter mouseAdapter;
   
   private class Seperator extends Component
   {
       public Seperator()
       {
           int toolbarHeight = GameActionToolbar.this.getHeight();
           setSize(14,toolbarHeight);                      
       }
       
       public void paint( Graphics g )
       {  
           int toolbarHeight = GameActionToolbar.this.getHeight();
           g.setColor(IvanhoeUIConstants.DARKEST_GRAY);
           g.drawLine(0,10,0,toolbarHeight-10);           
       }
       
   }

   public GameActionToolbar( IvanhoeGame ivanhoeGame, IvanhoeFrame frame )
   {      
      super(BoxLayout.X_AXIS);

      // add all the buttons
      
      addButton(new ExitAction());

      addSeperator();

      AddDocAction addDocAction = new AddDocAction(ivanhoeGame.getDiscourseField(),
              									   ivanhoeGame.getRoleManager(), 
                                                   frame );
      ivanhoeGame.getDiscourseField().addPermissionListener(addDocAction);
      addDocAction.writePermissionsChanged(ivanhoeGame.getDiscourseField().isWritable());
      addButton(addDocAction);

      SearchAction searchAction = new SearchAction( "search",ResourceHelper.instance.getIcon("res/icons/search.jpg"));         
      addButton(searchAction);

      HistoryAction historyAction = new HistoryAction( "history log", ResourceHelper.instance.getIcon("res/icons/log.jpg"));
      addButton(historyAction);

      addSeperator();

      RoleAction roleAction = new RoleAction( "roleplay", ResourceHelper.instance.getIcon("res/icons/mask.jpg"));
      addButton(roleAction);

      ColorAction colorAction = new ColorAction( "color preferences", ResourceHelper.instance.getIcon("res/icons/colors.jpg"));
      addButton(colorAction);

      addSeperator();

      ChatAction chat = new ChatAction("chat", ResourceHelper.instance.getIcon("res/icons/phone.jpg"));
      addButton(chat);

      DiscussionAction discussionAction = new DiscussionAction( "discussion", ResourceHelper.instance.getIcon("res/icons/forum.jpg"));
      addButton(discussionAction);
     
      addSeperator();

      SubmitAction submitAction = new SubmitAction(ivanhoeGame, frame);
      ivanhoeGame.getDiscourseField().addCurrentMoveListener(submitAction);
      addButton(submitAction);

      RemoveAllAction trashAction = new RemoveAllAction(ivanhoeGame.getDiscourseField(), frame);
      ivanhoeGame.getDiscourseField().addCurrentMoveListener(trashAction);
      addButton(trashAction); 

      addSeperator();

      HelpAction helpAction = new HelpAction("help",ResourceHelper.instance.getIcon("res/icons/help.jpg"));
      addButton(helpAction);

//      add(Box.createRigidArea(new Dimension(1,15)));
//      
      // create a mouse adapter for all menu components
      this.mouseAdapter = new MouseAdapter()
      {
         public void mouseEntered(MouseEvent e)
         {
         	//Workspace.instance.getNavigator().getDocumentAreaManager().clearHighlight();
            //TODO implement roll over
         }
         
         public void mouseExited(MouseEvent e)
         {

         }
      };
      
      Dimension size = getPreferredSize();
      setSize(size.width,size.height);
      
      this.addMouseListener( this.mouseAdapter );
   }
   
   private void addSeperator()
   {       
       add(new Seperator());
   }
   
   private void addButton(Action act)
   {
      IvanhoeButton btn = new IvanhoeButton(act);
      add(btn);
      btn.addMouseListener( this.mouseAdapter );
   }
   
}