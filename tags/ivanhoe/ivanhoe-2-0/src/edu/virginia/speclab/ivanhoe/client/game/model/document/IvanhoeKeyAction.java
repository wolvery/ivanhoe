/*
 * Created on Nov 19, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.client.game.model.document;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import javax.swing.text.TextAction;

import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;

/**
 * IvanhoeKeyAction
 * @author lfoster
 *
 * Specialized action that is installed as the default for the
 * DocumentEditor pane. It is used to catch keyboard events
 * and generate ivanhoe addition/deletion tags and actions
 */
public class IvanhoeKeyAction extends TextAction
{
   /**
    * Constructs the IvanhoeKeyAction
    */
   public IvanhoeKeyAction()
   {
      super("IvanhoeKeyAction");
   }

   /**
    * Called when the user has typed a key. This method will
    * determine if the key was delete or addition, and generate
    * the specialized tags
    */
   public void actionPerformed(ActionEvent e)
   {
      JEditorPane pane = (JEditorPane)e.getSource();
      IvanhoeDocument doc = (IvanhoeDocument)pane.getDocument();
      if ( doc.isReadOnly() || this.isEnabled() == false)
      {
         Toolkit.getDefaultToolkit().beep();
         return;
      }
      
      // ignore control modifed keystrokes
      if ( (e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK)
      {
         return;   	
      }
      
      // ignore meta modifed keystrokes
      if ( (e.getModifiers() & ActionEvent.META_MASK) == ActionEvent.META_MASK)
      {
         return;     
      }
         
      int startPos = pane.getSelectionStart();
      int endPos = pane.getSelectionEnd();
      
      String cmd = e.getActionCommand();
      if (cmd.equals("\b"))
      {
         // Backspace pressed, move the caret left one slot and mar
         if (startPos == endPos)
         {
            doc.deleteText(startPos - 1, endPos);
            pane.setCaretPosition(startPos - 1);
         }
         else
         {
            doc.deleteText(startPos, endPos);
            pane.setCaretPosition(startPos);
         }
      }
      else if (cmd.charAt(0) == 127) // delete key
      {
         // Delete pressed
         if (startPos == endPos)
         {
            int len1 = doc.getLength();
            
            // update position with move dot... setCaretPos cause extra delete
            doc.deleteText(startPos, startPos + 1);
            if (len1 == doc.getLength())
            {
               try
               {
                   pane.setCaretPosition(startPos+1);
               }
               catch (RuntimeException re)
               {
                   SimpleLogger.logError("Delete key caused failure during setting caret position",
                           re);
               }
            }
         }
         else
         {
            doc.deleteText(startPos, endPos);
            int caretPos = Math.min(endPos, doc.getLength());
            pane.setCaretPosition(caretPos);
         }
      }
      else
      {
         if (startPos != endPos)
         {
            doc.deleteText(startPos, endPos);
            pane.setCaretPosition(startPos);
         }

         doc.addNewText(startPos, cmd);
      }
   }
}
