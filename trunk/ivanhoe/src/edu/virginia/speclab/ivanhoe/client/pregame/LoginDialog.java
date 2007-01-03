//
// LoginDialog
// Dialog to handle client login
// Author: Lou Foster
// Date : 10/02/03
//
package edu.virginia.speclab.ivanhoe.client.pregame;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.*;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.view.gameaction.HelpAction;
import edu.virginia.speclab.ivanhoe.client.util.InfiniteProgressPanel;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.AbstractProxy;
import edu.virginia.speclab.ivanhoe.shared.Encryption;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.NewAccountQueryMsg;
import edu.virginia.speclab.ivanhoe.shared.message.NewAccountQueryResponseMsg;

public final class LoginDialog extends JDialog implements ActionListener
{
    private boolean allowNewAccounts;
    private boolean loginSuccess;
    
    private JButton loginBtn;
    private JButton exitBtn;
    private JButton newBtn,helpBtn,forgotBtn;
    
    private JTextField user;
    private JLabel status;
    private JPasswordField password;
    
    private String lobbyHost;
    private int lobbyPort;
    
    private InfiniteProgressPanel glassPane;
    private HelpAction helpAction = new HelpAction();
	private GameInfo gameInfo;

    public LoginDialog(String lobbyHost, int lobbyPort)
    {
        this.lobbyHost = lobbyHost;
        this.lobbyPort = lobbyPort;
        
        this.glassPane = new InfiniteProgressPanel(2000);
        this.setGlassPane(glassPane);
        
        createUI();
        
        this.password.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt)
            {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) 
                {
                    handleLogin();
                }
            }
        });

        new NewAccountQuery(lobbyHost,lobbyPort);
        this.user.requestFocus();
    }
    
    
    /**
     * Checks for any invalid characters in the input fields of the login dialog
     * that may interfere with the database back end.
     * 
     * @return boolean value of true if the input is valid
     */
    private boolean validateInput( String userName, String passwordText )
    {
        boolean validInput = true;
        String[] textfields = new String[4];
        textfields[0] = this.lobbyHost;
        textfields[1] = Integer.toString(this.lobbyPort);
        textfields[2] = userName;
        textfields[3] = passwordText;

        for (int i = 0; i < textfields.length; i++) {
            if (textfields[i].indexOf('/') != -1
                    || textfields[i].indexOf('\\') != -1) {
                validInput = false;
            }
        }

        return validInput;
    }
    private void createUI()
    {
        setTitle("Welcome to Ivanhoe!");
        setModal(true);
        setSize(200, 100);
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        EmptyBorder emptyBorder = new EmptyBorder(5,5,5,5);
        topPanel.setBorder(emptyBorder);

        // control panel 
        JPanel controlPanel = createControlPanel();        

        // picture panel
        Box pictureBox = Box.createVerticalBox();               
        JLabel picture =  new JLabel(ResourceHelper.instance.getIcon("res/images/ivan.gif"));        
        pictureBox.add(Box.createRigidArea(new Dimension(0,5)));
        pictureBox.add(picture);
        pictureBox.add(Box.createGlue());
                
        topPanel.add(pictureBox,BorderLayout.WEST);
        topPanel.add(controlPanel,BorderLayout.EAST);
        
        setContentPane(topPanel);
        pack();
        setResizable(false);
    }
    
    private JPanel createControlPanel()
    {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
                        
        JPanel loginPanel = createLoginPanel();
        JPanel buttonPanel = createButtonPanel();
        
        controlPanel.add(loginPanel);
        controlPanel.add(buttonPanel);
        
        return controlPanel;
    }

    private JPanel createLoginPanel()
    {
        // create login panel - NORTH
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BorderLayout());
        
        EmptyBorder emptyBorder = new EmptyBorder(5,5,5,5);
        EmptyBorder emptyBorder2 = new EmptyBorder(5,5,5,5);
        CompoundBorder compoundBorder = new CompoundBorder(LineBorder.createGrayLineBorder(),emptyBorder2);
        loginPanel.setBorder(new CompoundBorder(emptyBorder,compoundBorder));
        
        JPanel identityPanel = createIdentityPanel();
        JPanel loginButtonPanel = createLoginButtonPanel();
        
        loginPanel.add(identityPanel,BorderLayout.NORTH);
        loginPanel.add(loginButtonPanel,BorderLayout.SOUTH);
        
        return loginPanel;
    }

    private JPanel createIdentityPanel()
    {
        // identity panel - NORTH
        JPanel identityPanel = new JPanel();
        identityPanel.setLayout(new BorderLayout());

        // username panel - NORTH
        JPanel namePnl = new JPanel();
        JLabel nameLabel = new JLabel("user name:");
        nameLabel.setFont(IvanhoeUIConstants.BOLD_FONT);
        namePnl.add(nameLabel);
        this.user = new JTextField(20);
        user.setFont(IvanhoeUIConstants.BOLD_FONT);
        namePnl.add(this.user);

        // password panel - SOUTH
        JPanel pwPnl = new JPanel();        
        JLabel passwordLabel = new JLabel("password: ");
        passwordLabel.setFont(IvanhoeUIConstants.BOLD_FONT);
        pwPnl.add(passwordLabel);
        this.password = new JPasswordField(20);
        this.password.setFont(IvanhoeUIConstants.BOLD_FONT);
        pwPnl.add(this.password);

        identityPanel.add(namePnl, BorderLayout.NORTH);
        identityPanel.add(pwPnl, BorderLayout.SOUTH);
        
        return identityPanel;
    }

    private JPanel createLoginButtonPanel()
    {
        // login button panel - SOUTH
        JPanel loginButtonPanel = new JPanel();
        loginButtonPanel.setLayout(new BorderLayout());

        JPanel statusPnl = new JPanel();
        JLabel statusLabel = new JLabel("status: ");
        statusLabel.setFont(IvanhoeUIConstants.BOLD_FONT);
        statusPnl.add(statusLabel);
        this.status = new JLabel("Not Connected");
        this.status.setFont(IvanhoeUIConstants.BOLD_FONT);
        statusPnl.add(status);
        loginButtonPanel.add(statusPnl);

        this.loginBtn = new JButton("login");
        this.loginBtn.setFont(IvanhoeUIConstants.BOLD_FONT);
        this.loginBtn.addActionListener(this);
        loginButtonPanel.add(this.loginBtn, BorderLayout.EAST );
        
        return loginButtonPanel;
    }
    
    private JPanel createButtonPanel()
    {
        // button panel - SOUTH
        JPanel btnPnl = new JPanel();
        btnPnl.setLayout(new BorderLayout());
        EmptyBorder emptyBorder = new EmptyBorder(5,5,5,5);
        btnPnl.setBorder(emptyBorder);

        this.newBtn = new JButton("new account");
        this.newBtn.setFont(IvanhoeUIConstants.BOLD_FONT);
        this.newBtn.addActionListener(this);
        this.newBtn.setEnabled(false);
        btnPnl.add(this.newBtn, BorderLayout.WEST);
        
        this.forgotBtn = new JButton("forgot password");
        this.forgotBtn.setFont(IvanhoeUIConstants.BOLD_FONT);
        this.forgotBtn.addActionListener(this);
        
        this.helpBtn = new JButton("help");
        this.helpBtn.setFont(IvanhoeUIConstants.BOLD_FONT);
        this.helpBtn.addActionListener(helpAction);
        
        JPanel centerButtons = new JPanel(new BorderLayout());
        centerButtons.add(this.forgotBtn, BorderLayout.WEST);
        centerButtons.add(this.helpBtn, BorderLayout.EAST);
               
        btnPnl.add(centerButtons, BorderLayout.CENTER);

        this.exitBtn = new JButton("exit");
        this.exitBtn.setFont(IvanhoeUIConstants.BOLD_FONT);
        this.exitBtn.addActionListener(this);
        btnPnl.add(this.exitBtn, BorderLayout.EAST);
        
        return btnPnl;
    }

    /**
     * Implementation of method for ActionListener interface. It simply handles
     * the login or cancel buttons.
     */
    public void actionPerformed(ActionEvent evt)
    {
        if (evt.getSource() == this.exitBtn) 
        {
            dispose();
        } 
        else if (evt.getSource() == this.loginBtn) 
        {
            handleLogin();
        } 
        else if (evt.getSource().equals(this.newBtn)) 
        {
            handleNewAccounts();
        } 
        else if (evt.getSource().equals(this.forgotBtn)) 
        {
            forgotPassword();
        }
    }
    
    private void handleNewAccounts()
    {
        if (allowNewAccounts)
        {
            String userName = user.getText();
            String clearPassword = new String(password.getPassword());
            NewAccountDialog dlg = new NewAccountDialog(userName, clearPassword, this.lobbyHost, this.lobbyPort);
            dlg.show();
        }
        else
        {
            JOptionPane.showMessageDialog(null, 
                    "Please email ivanhoe@nines.org to request an account.", "New Account",
                    JOptionPane.INFORMATION_MESSAGE);
        }       
    }
    
    private void handleLogin()
    {
        // display a progress indicator while login is handled
        SwingUtilities.invokeLater(new Runnable() 
        {
             public void run() 
             {
                loginBtn.setEnabled(false);
                glassPane.start();
                Thread performer = new Thread(new Runnable() 
                {
                   public void run() 
                   {
//                      glassPane.wasteSomeTime(loginBtn, 500);
                       String userName = user.getText();
                       String clearPassword = new String(password.getPassword());
                       new LoginAttempt(userName,clearPassword);
                   }
                }, "Performer");
                performer.start();
             }
         });
        
    }
    
    private void forgotPassword()
    {
        NewPasswordDialog npd = new NewPasswordDialog(this.lobbyHost, this.lobbyPort);
        npd.show();
    }
    
    private void loginSuccess(GameInfo gameInfo)
    {
    	this.gameInfo = gameInfo;
        this.loginSuccess = true;
        dispose();        
    }
    
    private void allowNewAccounts( boolean value )
    {
        allowNewAccounts = value;
        this.newBtn.setEnabled(allowNewAccounts);
    }
    
    /**
     * @return Returns true is login was successful
     */
    public boolean isLoginSuccessful()
    {
        return this.loginSuccess;
    }
 
    
    private class NewAccountQuery implements IMessageHandler
    {
        private AbstractProxy provisionalProxy;

        public NewAccountQuery( String host, int port )
        {
            provisionalProxy = new CreatorProxy();

            boolean success = provisionalProxy.connect(lobbyHost, lobbyPort);
            
            if (success) 
            {
                provisionalProxy.registerMsgHandler(MessageType.NEW_ACCOUNT_QUERY_RESPONSE, this);
                provisionalProxy.setEnabled(true);
                provisionalProxy.sendMessage(new NewAccountQueryMsg());
            }
        }
        
        public void handleMessage(Message msg)
        {
            if (msg.getType().equals(MessageType.NEW_ACCOUNT_QUERY_RESPONSE))
            {
                NewAccountQueryResponseMsg naqrMsg = (NewAccountQueryResponseMsg)msg;
                provisionalProxy.unregisterMsgHandler(MessageType.NEW_ACCOUNT_QUERY_RESPONSE, this);
                provisionalProxy.disconnect();                
                allowNewAccounts( naqrMsg.getNewAccountsAllowed() );
            }
        }
    }
    
    private class LoginAttempt implements LoginTransactionListener
    {
        private LoginTransaction login;
        
        public LoginAttempt( String userName, String clearPassword )
        {
            login = new LoginTransaction(lobbyHost,lobbyPort);
            login.setListener(this);
        
            if ( validateInput(userName,clearPassword) ) 
            {
                status.setText("Authenticating...");
                String encryptedPassword = Encryption.createMD5HashCode(clearPassword);
                login.login(userName,encryptedPassword);            
            } 
            else 
            {
                status.setText("Invalid login.");
            }
            
            glassPane.stop();
            loginBtn.setEnabled(true);
        }

        /* (non-Javadoc)
         * @see edu.virginia.speclab.ivanhoe.client.player.ILoginListener#failedConnection()
         */
        public void failedConnection()
        {
            status.setText("Unable to connect.");
        }

        /* (non-Javadoc)
         * @see edu.virginia.speclab.ivanhoe.client.player.ILoginListener#failedAuthorization()
         */
        public void failedAuthorization()
        {
            status.setText("Invalid login.");
        }

        /* (non-Javadoc)
         * @see edu.virginia.speclab.ivanhoe.client.player.ILoginListener#loginSuccessful()
         */
        public void loginSuccessful()
        {
            loginSuccess(login.getGameInfo());
        }        
    }

	public GameInfo getGameInfo() {
		return gameInfo;
	}
    
}