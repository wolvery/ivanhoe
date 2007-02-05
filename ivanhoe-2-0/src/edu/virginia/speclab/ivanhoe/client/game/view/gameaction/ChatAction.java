/*
 * Created on Jul 9, 2004
 *
 * ChatAction
 */
package edu.virginia.speclab.ivanhoe.client.game.view.gameaction;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;

/**
 * @author lfoster
 *
 * This is the action used to bring up the ivanhoe chat client
 */
public class ChatAction extends AbstractAction
{
   public ChatAction(String text, Icon icon)
   {
      super(text, icon);
      putValue(Action.SHORT_DESCRIPTION, text);
   }
   
   /**
    * show the chat
    */
   public void actionPerformed(ActionEvent e)
   {
      Workspace.instance.openChatWindow();
   }

}
