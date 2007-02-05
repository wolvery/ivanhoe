/*
 * Created on Jun 28, 2004
 *
 * LobbyDialog
 */
package edu.virginia.speclab.ivanhoe.client.lobby;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.lobby.newgame.NewGameWizard;
import edu.virginia.speclab.ivanhoe.shared.AbstractProxy;
import edu.virginia.speclab.ivanhoe.shared.IDisconnectListener;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;

/**
 * @author benc
 * @author lfoster
 * 
 * Dialog containing lobby panels and interacting with the lobby
 */
public class LobbyDialog
        extends JDialog
        implements ActionListener, IDisconnectListener, LobbyListener
{
    private boolean joinedGame;
    protected String selectedGame;
    protected String selectedGameHost;
    protected int selectedGamePort;
    protected GameInfo selectedGameInfo;
    
    // UI
    private JButton newGameButton;
    private JButton exitButton;
    private JPanel buttonPanel;
    private JPanel titlePanel;
    private JTabbedPane lobbyTabs;
    private JPanel personalGames;
    private JPanel filteredGames;
    private String workDir;
    private JFrame parentFrame;
    private LobbyPanel personalLobbyPanel, filteredLobbyPanel;
    private boolean displayPersonalPanel;

    /**
     * Construct the lobby dialog
     */
    public LobbyDialog( JFrame frame, String workDir)
    {
        super(frame);
        setTitle("IVANHOE Playspaces");
        this.workDir = workDir;
        this.parentFrame = frame;
        this.displayPersonalPanel = false;
        
        getPersonalLobbyPanel().getModel().addListener(this);
        
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                Ivanhoe.shutdown();
            }
        });

        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(getTitlePanel(), BorderLayout.NORTH);
        getContentPane().add(getLobbyTabs(), BorderLayout.CENTER);
        getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);
        getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.WEST);

        setSize(500, 400);
        setResizable(true);
        setModal(true);

        this.pack();
        
        // go online in the lobby
        Ivanhoe.getProxy().registerDisconnectHandler(this);
        Ivanhoe.getProxy().goOnline();
    }

    /**
     * handle UI button clicks
     */
    public void actionPerformed(ActionEvent event)
    {
        if (event.getSource().equals(this.exitButton))
        {
            Ivanhoe.shutdown();
        }
        else if (event.getSource().equals(this.newGameButton))
        {
            NewGameWizard gameWizard = new NewGameWizard(parentFrame,this.workDir,Ivanhoe.getProxy());
            Ivanhoe.getProxy().registerMsgHandler(MessageType.CREATE_GAME_RESPONSE,gameWizard);
            gameWizard.startWizard();
            Ivanhoe.getProxy().unregisterMsgHandler(MessageType.CREATE_GAME_RESPONSE,gameWizard);
        }
    }

    protected void joinGame()
    {
        // set joined flag
        this.joinedGame = true;

        // put the dialog in a wait state
        getGlassPane().setVisible(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // close dialog
        Ivanhoe.getProxy().unregisterDisconnectHandler(this);
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                dispose();
            }
        });
    }

    /**
     * handle disconnects
     */
    public void notifyDisconnect(AbstractProxy proxy)
    {
        SimpleLogger.logInfo("Lost connection with server; terminating lobby");
        Ivanhoe.getProxy().unregisterDisconnectHandler(this);

        Ivanhoe
                .showErrorMessage("Lost Connection",
                        "You have lost your connetion with the Lobby. Ivanhoe will now shut down.");

        dispose();
        Ivanhoe.shutdown();
    }

    /**
     * @return Returns the selectedGame.
     */
    public String getSelectedGame()
    {
        return selectedGame;
    }

    /**
     * @return Returns the selectedGameHost.
     */
    public String getSelectedGameHost()
    {
        return selectedGameHost;
    }

    /**
     * @return Returns the selectedGameInfo.
     */
    public GameInfo getSelectedGameInfo()
    {
        return selectedGameInfo;
    }

    /**
     * @return Returns the selectedGamePort.
     */
    public int getSelectedGamePort()
    {
        return selectedGamePort;
    }

    /**
     * @return Returns the joinedGame.
     */
    public boolean isJoinedGame()
    {
        return joinedGame;
    }
    
    private JTabbedPane getLobbyTabs()
    {
        if (lobbyTabs == null)
        {
            lobbyTabs = new JTabbedPane();
            // Don't add the personal games tab 'til it's populated
            lobbyTabs.addTab("all games", getFilteredGames());
        }
        return lobbyTabs;
    }
    
    private JPanel getPersonalGames()
    {
        if (personalGames == null)
        {
            personalGames = new JPanel();
            personalGames.setLayout(new BorderLayout());
            personalGames.add(getPersonalLobbyPanel(), BorderLayout.CENTER);
        }
        return personalGames;
    }
    
    private LobbyPanel getPersonalLobbyPanel()
    {
        if (personalLobbyPanel == null)
        {
            personalLobbyPanel = new PersonalLobbyPanel(this);
        }
        return personalLobbyPanel;
    }
    
    private JPanel getFilteredGames() {
        if (filteredGames == null) {
            filteredGames = new JPanel();
            filteredGames.setLayout(new BorderLayout());
            filteredGames.add(getFilteredLobbyPanel(), BorderLayout.CENTER);
        }
        return filteredGames;
    }
    
    private LobbyPanel getFilteredLobbyPanel()
    {
        if (filteredLobbyPanel == null)
        {
            filteredLobbyPanel = new FilteredLobbyPanel(this);
        }
        return filteredLobbyPanel;
    }
    
    private JPanel getButtonPanel()
    {
        if (buttonPanel == null)
        {
            buttonPanel = new JPanel();
            buttonPanel.setLayout(new BorderLayout());
            buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            buttonPanel.add(getNewGameButton(), BorderLayout.WEST);
            buttonPanel.add(getExitButton(), BorderLayout.EAST);
        }
        return buttonPanel;
    }

    private JPanel getTitlePanel()
    {
        if (this.titlePanel == null)
        {
            this.titlePanel = new JPanel();
            JLabel title = new JLabel("IVANHOE playspaces");
            title.setFont(IvanhoeUIConstants.LARGE_FONT.deriveFont(Font.BOLD, 22.0f));
            this.titlePanel.add(title);
        }
        
        return this.titlePanel;
    }
    
    private JButton getNewGameButton()
    {
        if (this.newGameButton == null)
        {
            this.newGameButton = new JButton("new game");
            this.newGameButton.setFont(IvanhoeUIConstants.BOLD_FONT);
            this.newGameButton.addActionListener(this);
        }
        return this.newGameButton;
    }
    
    private JButton getExitButton()
    {
        if (this.exitButton == null)
        {
            this.exitButton = new JButton("exit");
            this.exitButton.setFont(IvanhoeUIConstants.BOLD_FONT);
            this.exitButton.addActionListener(this);
        }
        return this.exitButton;
    }

    public void lobbyChanged(Lobby changedLobby)
    {
        if (changedLobby == getPersonalLobbyPanel().getModel()
                && changedLobby.getRowCount() > 0)
        {
            if (!displayPersonalPanel)
            {
                displayPersonalPanel = true;
                getLobbyTabs().addTab("your games", getPersonalGames());
                getLobbyTabs().setSelectedIndex(getLobbyTabs().getTabCount() - 1);
            }
        }
    }
}
