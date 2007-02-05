/*
 * Created on Jul 13, 2004
 *
 * NewAccountDialog
 */
package edu.virginia.speclab.ivanhoe.client.pregame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import edu.virginia.speclab.ivanhoe.shared.data.User;
import edu.virginia.speclab.ivanhoe.shared.message.CreateAccountMsg;

/**
 * @author lfoster
 *
 * This dialog is used to create a new ivanhoe account
 */
public class NewAccountDialog extends JDialog 
   implements ActionListener
{
   private JTextField playerNameEdit;
   private JPasswordField newPasswordEdit;
   private JPasswordField passwordConfirmEdit;
   private JTextField firstNameEdit;
   private JTextField lastNameEdit;
   private JTextField emailEdit;
   private JTextField affiliationEdit;
   private JButton createBtn;
   private JButton cancelBtn;
   private String authHost;
   private int authPort;
   
   public NewAccountDialog(String user, String password, String authHost, int authPort)
   {
      super();
      setTitle("create new account");
      setResizable(false);
      setModal(true);
      
      this.authHost = authHost;
      this.authPort = authPort;
      
      getContentPane().setLayout(new BorderLayout());
      JPanel center = new JPanel(new BorderLayout());
      JPanel labelPanel = new JPanel( new GridLayout(7,1));
      labelPanel.add(new JLabel("player name: ", SwingConstants.RIGHT));
      labelPanel.add(new JLabel("password: ", SwingConstants.RIGHT));
      labelPanel.add(new JLabel("confirm password: ", SwingConstants.RIGHT));
      labelPanel.add(new JLabel("first name: ", SwingConstants.RIGHT));
      labelPanel.add(new JLabel("last name: ", SwingConstants.RIGHT));
      labelPanel.add(new JLabel("e-mail: ", SwingConstants.RIGHT));
      labelPanel.add(new JLabel("affiliation (optional): ", SwingConstants.RIGHT));
      center.add(labelPanel, BorderLayout.WEST);
      
      // create editor fields
      JPanel editPnl = new JPanel(new GridLayout(7,1));
      this.playerNameEdit = new JTextField(user);
      editPnl.add(this.playerNameEdit);
      this.newPasswordEdit = new JPasswordField(password);
      editPnl.add(this.newPasswordEdit);
      this.passwordConfirmEdit = new JPasswordField();
      editPnl.add(this.passwordConfirmEdit);
      this.firstNameEdit = new JTextField();
      editPnl.add(this.firstNameEdit);
      this.lastNameEdit = new JTextField();
      editPnl.add(this.lastNameEdit);
      this.emailEdit = new JTextField();
      editPnl.add(this.emailEdit);
      this.affiliationEdit = new JTextField();
      editPnl.add(this.affiliationEdit);
      center.add(editPnl, BorderLayout.CENTER);
      
      // create control panel
      JPanel ctl = new JPanel();
      this.cancelBtn = new JButton("cancel");
      this.cancelBtn.addActionListener(this);
      ctl.add(this.cancelBtn);
      this.createBtn = new JButton("create account");
      this.createBtn.addActionListener(this);
      ctl.add(createBtn);
      
      getContentPane().add(center, BorderLayout.CENTER);
      getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.EAST);
      getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.WEST);
      getContentPane().add(Box.createVerticalStrut(10), BorderLayout.NORTH);
      getContentPane().add(ctl, BorderLayout.SOUTH);
      pack();
      setSize(380, getHeight());
   }

   /**
    * Handle Create/Cancel
    */
   public void actionPerformed(ActionEvent e)
   {
      if (e.getSource().equals(this.cancelBtn))
      {
         this.dispose();
      }
      else if (e.getSource().equals(this.createBtn))
      {
         createAccount();
      }
   }
   
   /**
    * Use info in fields to validate & create account
    */
   void createAccount()
   {
      String playerName, passWd, fname, lname, email;
      playerName = this.playerNameEdit.getText();
      passWd = new String(this.newPasswordEdit.getPassword());
      fname = this.firstNameEdit.getText();
      lname = this.lastNameEdit.getText();
      email = this.emailEdit.getText();
      
      boolean missingError = false;
      String msg ="<html><b>Unable to create account.</b><br><br>" +
            "The following errors were detected:<ul>";
      if (playerName.length() < 1)
      {
         missingError = true;
         msg += "<li>Missing Player Name</li>";
      }
      else if (playerName.length() > 20)
      {
         missingError = true;
         msg += "<li>Player Name exceeds 20 characters</li>";
      }
      
      if (passWd.length() < 1)
      {
         missingError = true;
         msg += "<li>Password</li>";
      }
      if (fname.length() < 1)
      {
         missingError = true;
         msg += "<li>First Name</li>";
      }
      else if (fname.length() > 20)
      {
         missingError = true;
         msg += "<li>First Name exceeds 20 characters</li>";
      }
      
      if (lname.length() < 1)
      {
         missingError = true;
         msg += "<li>Last Name</li>";
      }
      else if (lname.length() > 20)
      {
         missingError = true;
         msg += "<li>Last Name exceeds 20 characters</li>";
      }
      
      if (email.length() < 1)
      {
         missingError = true;
         msg += "<li>E-Mail</li>";
      }
      else if (email.length() > 100)
      {
         missingError = true;
         msg += "<li>EMail exceeds 100 characters</li>";
      }
      
      if (missingError)
      {
         JOptionPane.showMessageDialog(null, msg, "Account Creation Error",
                 JOptionPane.ERROR_MESSAGE);
         return;
      }
      
      String valPw = new String(this.passwordConfirmEdit.getPassword());
      if (passWd.equals(valPw) == false)
      {
          JOptionPane.showMessageDialog(null, "<html><b>Unable to create account.</b><br><br>" +
                  "Password fields did not match", "Account Creation Error",
                  JOptionPane.ERROR_MESSAGE);
         return;
      }
      
      // open a connection & send create data
      User newUserData = new User(playerName, passWd, lname, fname, email, 
         this.affiliationEdit.getText());
      CreatorProxy proxy = new CreatorProxy();
      if (proxy.connect(this.authHost, this.authPort))
      {
         proxy.setEnabled(true);
         proxy.sendMessage(new CreateAccountMsg(newUserData));
         JOptionPane.showMessageDialog(this, 
            "Your account creation request has been submitted. " +
            "You will receive an email confirming account creation shortly.", 
            "Account Request Sent", JOptionPane.INFORMATION_MESSAGE);
         return;
      }
      else
      {
         JOptionPane.showMessageDialog(null, 
                 "Unable to connect to server to create account. Try again later.",
                 "Account Creation Failed", JOptionPane.ERROR_MESSAGE);
      }    
   }
}
