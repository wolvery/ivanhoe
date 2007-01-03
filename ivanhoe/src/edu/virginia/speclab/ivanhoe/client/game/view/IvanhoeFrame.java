//
// IvanhoeFrame
// Main JFrame for application
// Author: Lou Foster
// Date  : 6/26/03 
//
package edu.virginia.speclab.ivanhoe.client.game.view;

import javax.swing.*;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.IvanhoeGame;
import edu.virginia.speclab.ivanhoe.client.game.model.IvanhoeGameListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.util.IStatusListener;
import edu.virginia.speclab.ivanhoe.client.game.model.util.StatusEventMgr;
import edu.virginia.speclab.ivanhoe.client.game.view.metagame.SummaryDialog;
import edu.virginia.speclab.ivanhoe.client.game.view.metagame.WelcomeDialog;
import edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay.IRoleChooserListener;
import edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay.RoleChooser;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.DiscourseFieldState;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.time.DiscourseFieldTimeControls;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.time.DiscourseFieldTimeSlider;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.DelayDialog;
import edu.virginia.speclab.ivanhoe.client.util.PropertiesManager;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.*;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;
import edu.virginia.speclab.ivanhoe.shared.message.*;

import java.awt.*;
import java.awt.event.*;

public class IvanhoeFrame extends JFrame implements IStatusListener,
        IMessageHandler, IDisconnectListener, IvanhoeGameListener
{
    private Ivanhoe ivanhoeApplication;
    private IvanhoeGame ivanhoeGame;

    private RoleChooser roleChooser;

    private DiscourseFieldTimeSlider timeSlider;
    private DiscourseFieldTimeControls timeControls;
    
    private DelayDialog statusDlg;
    
     public IvanhoeFrame(Ivanhoe ivanhoe, GameInfo gameInfo)
    {
        super("Ivanhoe");

        this.ivanhoeApplication = ivanhoe;

        setSize(700, 600);
        getContentPane().setBackground(Color.BLACK);
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(ResourceHelper.getImage("res/icons/appicon.gif"));

        setVisible(true);
        
        joinGame(gameInfo);
    }

    /**
     * Join the game
     */
    public void joinGame( GameInfo gameInfo )
    {
        // create the game mode and join the game
        ivanhoeGame = new IvanhoeGame(gameInfo, ivanhoeApplication
                .getPropertiesManager());

        try
        {
            ivanhoeGame.joinGame();
        }
        catch (IvanhoeException e)
        {
            Ivanhoe.showErrorMessage("Unable to connect to game.", e
                    .getMessage());
            Ivanhoe.shutdown();
        }

        // Set UI property
        DiscourseFieldState.setInitialDocumentWeight(gameInfo.getStartDocWeight());

        // initialize the UI
        initUI();

        // tell the host we are ready for data
        // listen for game start events
        ivanhoeGame.setListener(this);

        // register message handlers
        Ivanhoe.registerGameMsgHandler(MessageType.READY, ivanhoeGame);
        Ivanhoe.registerGameMsgHandler(MessageType.SERVER_ERROR, this);
        Ivanhoe.getProxy().registerDisconnectHandler(this);
    }

    /**
     * create the ivanhoe client UI
     */
    private void initUI()
    {
        // TODO: at some point, factor out this singleton
        Workspace.instance = new Workspace(this);
        Workspace.instance.createUI(ivanhoeGame);

        // listen for clicks on the close X
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                if (Workspace.instance != null)
                {
                    Workspace.instance.exitGame(true);
                }
            }
        });
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(Workspace.instance,BorderLayout.CENTER);
        
        this.timeSlider = new DiscourseFieldTimeSlider();
        this.timeControls = new DiscourseFieldTimeControls(timeSlider);
        
        Dimension size = timeControls.getPreferredSize();
        timeControls.setSize(size.width,size.height);
        
        getContentPane().add(timeSlider,BorderLayout.EAST);
        Workspace.instance.add(timeControls, new Integer(0), 0);
        Workspace.instance.addComponentListener(new Resizer());
        
    }
    
    private class Resizer extends ComponentAdapter
    {
        public void componentResized(ComponentEvent e)
        {
            timeControls.setLocation(0,Workspace.instance.getHeight()-timeControls.getHeight());
        }
    }
    

    private void showDelayDialog(String message)
    {
        if (this.statusDlg == null)
        {
            // turn on glass pane to block all mouse activity
            getGlassPane().setVisible(true);

            // show the delay dialog
            this.statusDlg = new DelayDialog(message);
            Workspace.centerWindow(this.statusDlg);
            this.statusDlg.show();
        }
    }

    private void clearDelayDialog()
    {
        if (this.statusDlg != null)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    IvanhoeFrame.this.statusDlg.dispose();
                    IvanhoeFrame.this.statusDlg = null;
                    IvanhoeFrame.this.getGlassPane().setVisible(false);
                }
            });
        }
    }

    /**
     * handle delay messages from an ivanhoe component
     */
    public void delayBeginNotification(String message)
    {
        showDelayDialog(message);
    }

    /**
     * handle delay end messages from an ivanhoe component
     */
    public void delayEndNotification()
    {
        clearDelayDialog();
    }

    /**
     * handle error messages
     */
    public void errorMsgNotification(String message)
    {
        Ivanhoe.showErrorMessage("Error", message);
    }

    /**
     * handle warning messages
     */
    public void warningMsgNotification(String message)
    {
        Ivanhoe.showErrorMessage("Warning", message);
    }

    /**
     * Handle messages from server
     */
    public void handleMessage(Message msg)
    {
        if (msg.getType().equals(MessageType.SERVER_ERROR))
        {
            ServerErrorMsg err = (ServerErrorMsg) msg;

            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, err.getErrorTxt(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            if (err.isFatal())
            {
                Workspace.instance.exitGame(false);
            }
        }
    }

    //TODO remove
    /**
     * Reset the connection to the host, release all game resources and return
     * to the lobby.
     */
    public void restartGame()
    {
        StatusEventMgr.removeListener(this);
        Ivanhoe.getProxy().close();
        if (Workspace.instance != null)
        {
            Workspace.instance.releaseTimer();
            Workspace.instance = null;
        }
        getContentPane().removeAll();

    }

    /**
     * Server has sent a ready message to indicate that it is done with its part
     * of the login sequence. Apply any locally saved actions, and begin play
     */
    public void startGame()
    {
        SimpleLogger.logInfo("IvanhoeFrame.startGame()");
        // register the frame to handle status messages
        StatusEventMgr.addListener(this);

        Workspace.instance.startGame();

        DiscourseField discourseField = ivanhoeGame.getDiscourseField();
        timeSlider.init(discourseField.getDiscourseFieldTimeline(),
                discourseField.getDiscourseFieldTime(), ivanhoeGame
                        .getRoleManager());
        timeControls.init(discourseField.getDiscourseFieldTimeline(),
                discourseField.getDiscourseFieldTime());

        Workspace.instance.getTimer().addActionListener(timeSlider);
        Workspace.instance.setTimeControl(timeSlider);

        // restore the waiting cursor
        clearDelayDialog();

        // obtain the properties manager
        PropertiesManager propertiesManager = ivanhoeGame
                .getPropertiesManager();
        String displayWelcomeMessage = propertiesManager
                .getProperty("welcome_message");

        // display the welcome message if desired
        if (Boolean.valueOf(displayWelcomeMessage) == Boolean.TRUE)
        {
            WelcomeDialog welcomeDialog = new WelcomeDialog(this, this.ivanhoeGame
                    .getGameInfo(), this.ivanhoeGame.getRoleManager()
                    .getCurrentRole(), propertiesManager);
            welcomeDialog.show();
        }

        // display the summary dialog if a move is in progress from previous
        // session
        if (discourseField.getCurrentMove().isStarted())
        {
            String summary = discourseField.getCurrentMove()
                    .generateSummaryReport();
            SummaryDialog dlg = new SummaryDialog(this,summary);
            dlg.show();
        }

        timeSlider.runOpeningSequence();
    }

    public RoleChooser chooseRole()
    {
        // display role chooser dialog
        roleChooser = new RoleChooser(ivanhoeGame.getRoleManager());
        Workspace.instance.centerWindowOnWorkspace(roleChooser);
        return roleChooser;
    }

    /**
     * The client has lost connection with the server; terminate
     */
    public void notifyDisconnect(AbstractProxy proxy)
    {
        SimpleLogger
                .logInfo("Lost connection with server; terminating client");
        Ivanhoe.getProxy().unregisterDisconnectHandler(this);

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JOptionPane
                        .showMessageDialog(
                                IvanhoeFrame.this,
                                "Lost network connection to host, closing application.",
                                "Lost Connection",
                                JOptionPane.ERROR_MESSAGE);

                // close the application
                Ivanhoe.shutdown();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.virginia.speclab.ivanhoe.client.game.model.IvanhoeGameListener#addRoleChooserListener(edu.virginia.speclab.ivanhoe.client.game.view.metagame.IRoleChooserListener)
     */
    public void addRoleChooserListener(IRoleChooserListener listener)
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.virginia.speclab.ivanhoe.client.game.model.IvanhoeGameListener#removeRoleChooserListener(edu.virginia.speclab.ivanhoe.client.game.view.metagame.IRoleChooserListener)
     */
    public boolean removeRoleChooserListener(IRoleChooserListener listener)
    {
        return false;
    }

	public DiscourseFieldTimeSlider getTimeSlider() {
		return timeSlider;
	}
}
