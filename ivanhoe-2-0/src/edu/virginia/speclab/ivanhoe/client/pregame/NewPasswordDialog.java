/*
 * Created on Nov 17, 2004
 */
package edu.virginia.speclab.ivanhoe.client.pregame;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.shared.AbstractProxy;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.User;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.PassResetMsg;
import edu.virginia.speclab.ivanhoe.shared.message.PassResetResponseMsg;
import edu.virginia.speclab.ivanhoe.shared.message.UserLookupMsg;
import edu.virginia.speclab.ivanhoe.shared.message.UserLookupResponseMsg;

/**
 * @author dgran
 * 
 * In the event that a player has forgotten his or her password, this interface
 * helps to reset it.
 */
public class NewPasswordDialog extends JDialog implements ActionListener
{
   private String authHost;
   private int authPort;
   private JTextField userId;
   private JButton searchBtn;

   public NewPasswordDialog(String authHost, int authPort)
   {
      super();
      setTitle("forgotten password");
      setResizable(false);
      setModal(true);

      this.authHost = authHost;
      this.authPort = authPort;

      getContentPane().setLayout(new BorderLayout());
      JPanel infoPane = new JPanel();
      String instructions = "Please enter either your email address or"
            + "your account id to have the password reset";
      JLabel instPnl = new JLabel(instructions);
      infoPane.add(instPnl);

      JPanel labelPanel = new JPanel(new GridLayout(1, 2));
      this.userId = new JTextField();
      this.userId.addActionListener(this);
      labelPanel.add(this.userId);
      this.searchBtn = new JButton("reset password");
      this.searchBtn.addActionListener(this);
      labelPanel.add(this.searchBtn);

      getContentPane().add(infoPane, BorderLayout.NORTH);
      getContentPane().add(labelPanel, BorderLayout.CENTER);

      pack();
      setSize(380, getHeight());
      Workspace.centerWindow(this);
   }

   /**
    * Handle GUI request to reset the password
    */
   public void actionPerformed(ActionEvent e)
   {
      if (e.getSource().equals(this.searchBtn)
            || e.getSource().equals(this.userId))
      {
         lookupAccount(this.userId.getText());
      }
   }

   /**
    * Sends a message to the server to find all accounts that match an id
    * 
    * @param idText
    *           A username or an email address in the system
    */
   private void lookupAccount(String idText)
   {
      SimpleLogger.logInfo("Looking up " + idText + " for password reset");

      UserLookupProxy proxy = new UserLookupProxy();
      if (proxy.connect(this.authHost, this.authPort))
      {
         proxy.setEnabled(true);
         proxy.sendMessage(new UserLookupMsg(idText));
      }
      else
      {
         Ivanhoe.showErrorMessage("user lookup failed",
               "User account lookup failed (couldn't connect to host)");
      }
   }

   /**
    * Once a response has been received from the server, this method resets the
    * password.
    * 
    * @param users
    *           An array of User objects
    */
   private void handleLookupResponse(List users)
   {
      SimpleLogger.logInfo("Got back " + users.size() + " user lookup matches");

      /**
       * In the case of an email address search, there may be more than one
       * match. In this case, prompt the user to select the correct player id.
       */
      if (users.size() > 1)
      {
         // TODO - prompt for correct user name
      }
      else if (users.size() == 1)
      {
         User user = (User) users.get(0);
         SimpleLogger.logInfo("resetting password for " + user.getUserName());
         resetPassword(user);
      }
      else
      {
         JOptionPane
               .showMessageDialog(
                     this,
                     "Could not find the user in the Ivanhoe game. "
                           + "Please check spelling and capitalization, or email ivanhoe@nines.org "
                           + "for assistance.", "account not found",
                     JOptionPane.INFORMATION_MESSAGE);
      }
   }
   
   /**
    * Displays a dialog to the user regarding the status of the password
    * reset request.
    * @param success True if the password was reset
    */
   private void handlePasswordResetResponse(boolean success)
   {
      if (success)
      {
         JOptionPane
               .showMessageDialog(
                     this,
                     "Password reset successfully. "
                           + "You will receive email shortly with the new password.", "password reset",
                     JOptionPane.INFORMATION_MESSAGE);
      }
      else
      {
         Ivanhoe.showErrorMessage("password reset failed",
               "Password reset failed.  Please email ivanhoe@nines.org for assistance.");
      }
      
      this.dispose();
   }

   /**
    * Requests a new password from the GPW system and sends it to the server
    * @param user A User object for the user who will get a new password
    */
   private void resetPassword(User user)
   {
      // send reset request to server, using crypt password
      UserLookupProxy proxy = new UserLookupProxy();
      if (proxy.connect(this.authHost, this.authPort))
      {
         proxy.setEnabled(true);
         proxy.sendMessage(new PassResetMsg(user));
      }
      else
      {
         Ivanhoe.showErrorMessage("password reset failed",
               "Password reset failed (couldn't connect to host)");
      }
   }

   private class UserLookupProxy extends AbstractProxy
   {
      public void receiveMessage(Message msg)
      {
         if (msg.getType().equals(MessageType.USER_LOOKUP_RESPONSE))
         {
            List users = ((UserLookupResponseMsg) msg).getUsers();
            handleLookupResponse(users);
         }
         else if (msg.getType().equals(MessageType.PASS_RESET_RESPONSE))
         {
            boolean success = ((PassResetResponseMsg) msg).passwordResetSuccessfully();
            handlePasswordResetResponse(success);
         }
      }

      public String getID()
      {
         return "UserLookupProxy";
      }
   }

}