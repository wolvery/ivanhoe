/*
 * Created on May 21, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.gameaction;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.CurrentMove;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.ICurrentMoveListener;
import edu.virginia.speclab.ivanhoe.client.game.view.IvanhoeFrame;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;

/**
 * @author lfoster
 */
public class RemoveAllAction extends AbstractAction
    implements ICurrentMoveListener
{
   private DiscourseField discourseField;
   private IvanhoeFrame ivanhoeFrame;
   
   public RemoveAllAction( DiscourseField discourseField, IvanhoeFrame frame )
   {
      super("discard move");
      this.discourseField = discourseField;
      this.ivanhoeFrame = frame;
      putValue(Action.SHORT_DESCRIPTION, "discard move");
      
      // Set initial value
      currentMoveChanged(discourseField.getCurrentMove(), null);
   }
  
   public void actionPerformed(ActionEvent arg0)
   {
      removeAll();
   }
   
   /**
    * Removes all actions in the current move, with appropriate dialog and
    * confirmation.
    */
   public void removeAll()
   {
      if (discourseField.getCurrentMove().getActionCount() > 0)
      {
         int resp = JOptionPane.showConfirmDialog(ivanhoeFrame, 
            "This will discard all actions in your current move. Continue?", "Confirm Discard",
            JOptionPane.OK_CANCEL_OPTION);
         if (resp == JOptionPane.OK_OPTION)
         {
            discourseField.getCurrentMove().removeAllActions();
            discourseField.getDocumentVersionManager().removeCurrentDocumentVersions();
            
            JOptionPane.showMessageDialog(ivanhoeFrame, 
               "All actions in your current move have been removed.", "Success", 
               JOptionPane.INFORMATION_MESSAGE);
         }
         else
         {
            JOptionPane.showMessageDialog(ivanhoeFrame, 
               "Nothing discarded", "Canceled", // bad dialog!
               JOptionPane.INFORMATION_MESSAGE);
         }
      }
      else
      {
         JOptionPane.showMessageDialog(ivanhoeFrame, 
            "You do not have any actions in your current move.", "No Actions", 
            JOptionPane.INFORMATION_MESSAGE);
      }
   }


    public void currentMoveChanged(CurrentMove currentMove, IvanhoeAction action)
    {
        Icon icon;
        if (currentMove != null && currentMove.isStarted())
        {
            icon = ResourceHelper.instance.getIcon("res/icons/trash.jpg");
        }
        else
        {
            icon = ResourceHelper.instance.getIcon("res/icons/trash-disabled.jpg");
        }
    
        putValue(Action.SMALL_ICON, icon);   
    }

}
