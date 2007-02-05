/*
 * Created on Dec 16, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.gameaction;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;


/**
 * Action implemnation for EXIT
 */
public class ExitAction extends javax.swing.AbstractAction
{
   public ExitAction()
   {
      super("exit",
         ResourceHelper.instance.getIcon("res/icons/exit.jpg"));
      putValue(Action.SHORT_DESCRIPTION,"exit ivanhoe");
   }

   public void actionPerformed(ActionEvent arg0)
   {
      SwingUtilities.invokeLater( new Runnable()
         {
            public void run()
            {
                Workspace.instance.exitGame(true);
            }
         });
   }
}