/*
 * Created on May 25, 2005
 *
 * LobbyPanel
 */
package edu.virginia.speclab.ivanhoe.client.lobby;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;

/**
 * @author benc
 */
public class LobbyPanel
        extends JPanel
        implements ActionListener
{
    private final LobbyDialog lobbyDialog;
    private final Lobby model;
    private JTable gameTable;
    private JTextArea descriptionField;
    private JButton joinButton;
    private JPanel buttonBox;
    private JPanel bottomPanel;
    private JPanel descriptionPanel;
    private JScrollPane gameTableScrollPane;
    private JSplitPane gameSplitPane;

    // selected game data
    private boolean selectionValid;

    public LobbyPanel(LobbyDialog parent)
    {        
        this.lobbyDialog = parent;
        this.model = new Lobby();

        this.setLayout(new BorderLayout());
        
        this.add(getGameSplitPane(), BorderLayout.CENTER);
        this.add(getButtonBox(), BorderLayout.SOUTH);
    }
    
    private JPanel getButtonBox()
    {
        if (this.buttonBox == null)
        {
            this.buttonBox = new JPanel(new BorderLayout());
            this.buttonBox.setBorder(new EmptyBorder(3, 0, 0, 0));
            this.buttonBox.add(getJoinButton(), BorderLayout.EAST);
        }
        return this.buttonBox;
    }
    
    private JSplitPane getGameSplitPane()
    {
        if (this.gameSplitPane == null)
        {
            this.gameSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
            this.gameSplitPane.setTopComponent(getGameTableScrollPane());
            this.gameSplitPane.setBottomComponent(getBottomPanel());
            this.gameSplitPane.resetToPreferredSizes();
        }
        
        return this.gameSplitPane;
    }
    
    private JButton getJoinButton()
    {
        if (this.joinButton == null)
        {
            this.joinButton = new JButton("join game");
            this.joinButton.setFont(IvanhoeUIConstants.BOLD_FONT);
            this.joinButton.addActionListener(this);
        }
        return this.joinButton;
    }

    protected JPanel getBottomPanel()
    {
        if (this.bottomPanel == null)
        {
            this.bottomPanel = new JPanel(new BorderLayout());
            this.bottomPanel.add(getDescriptionPanel(), BorderLayout.CENTER);
        }
        
        return this.bottomPanel;
    }
    
    
    private JPanel getDescriptionPanel()
    {
        if (this.descriptionPanel == null)
        {
            this.descriptionPanel = new JPanel();
            this.descriptionPanel.setLayout(new BorderLayout());
            TitledBorder descriptionBorder = new TitledBorder("game description");
            descriptionBorder.setTitleFont(IvanhoeUIConstants.SMALL_FONT);
            this.descriptionPanel.setBorder(descriptionBorder);
            this.descriptionPanel.add(getDescriptionField(), BorderLayout.CENTER);
            this.descriptionPanel.setMinimumSize(new Dimension(300,100));
        }

        return this.descriptionPanel;
    }

    private JTextArea getDescriptionField()
    {
        if (descriptionField == null)
        {
            descriptionField = new JTextArea();
            descriptionField.setFont(IvanhoeUIConstants.SMALL_FONT);
            descriptionField.setMinimumSize(new Dimension(300, 100));
            descriptionField.setEditable(false);
            descriptionField.setLineWrap(true);
            descriptionField.setWrapStyleWord(true);
            descriptionField.setBackground(this.descriptionPanel.getBackground());
        }
        return descriptionField;
    }

    
    private void setDisplay(String description)
    {
        descriptionField.setText(description);
    }

    /**
     * Clear the currently selected game
     */
    protected void clearSelection()
    {
        this.selectionValid = false;
        lobbyDialog.selectedGame = "";
        lobbyDialog.selectedGameHost = "";
        lobbyDialog.selectedGamePort = 0;
        lobbyDialog.selectedGameInfo = null;
        setDisplay("");
    }

    /**
     * @param selectedRow
     */
    protected void handleEntrySelected(int selectedRow)
    {
        this.selectionValid = true;
        lobbyDialog.selectedGame = this.model.getGameName(selectedRow);
        lobbyDialog.selectedGameHost = this.model.getGameHost(selectedRow);
        lobbyDialog.selectedGamePort = this.model.getGamePort(selectedRow);
        lobbyDialog.selectedGameInfo = this.model.getGameInfo(selectedRow);
        setDisplay(this.model.getGameDescription(selectedRow));
    }

    private JScrollPane getGameTableScrollPane()
    {
        if (this.gameTableScrollPane == null)
        {
//          Ask to be notified of selection changes.
            ListSelectionModel rowSM = getGameTable().getSelectionModel();
            rowSM.addListSelectionListener(new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent e)
                {
                    // Ignore extra messages.
                    if (e.getValueIsAdjusting() == false)
                    {
                        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                        if (lsm.isSelectionEmpty() == false)
                        {
                            int selectedRow = lsm.getMinSelectionIndex();
                            handleEntrySelected(selectedRow);
                        }
                        else
                        {
                            clearSelection();
                        }
                    }
                }
            });

            // handle double clicks on list
            getGameTable().addMouseListener(new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    if (e.getClickCount() == 2)
                    {
                        joinGame();
                    }
                }
            });

            this.gameTableScrollPane = new JScrollPane(getGameTable());
            this.gameTableScrollPane
                    .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            this.gameTableScrollPane
                    .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        }

         return this.gameTableScrollPane;    
    }
    
    private JTable getGameTable()
    {
        if (this.gameTable == null)
        {
            TableSorter sorter = new TableSorter(this.model);
            this.gameTable = new JTable(sorter);
            sorter.setTableHeader(gameTable.getTableHeader());
            
            // Setup the default sorting state
            sorter.setSortingStatus(2,TableSorter.DESCENDING);
            sorter.setSortingStatus(0,TableSorter.ASCENDING);
            sorter.setSortingStatus(3,TableSorter.ASCENDING);
            sorter.setSortingStatus(1,TableSorter.ASCENDING);
            
            this.model.setTableSorter(sorter);
            
            this.gameTable.setColumnSelectionAllowed(false);
            this.gameTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            this.gameTable.setFont(IvanhoeUIConstants.SMALL_FONT);
    
            this.gameTable.getColumnModel().getColumn(0).setPreferredWidth(200);
            this.gameTable.getColumnModel().getColumn(1).setPreferredWidth(70);
            this.gameTable.getColumnModel().getColumn(2).setPreferredWidth(40);
            this.gameTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        }

        return gameTable;
    }
    
    protected void joinGame()
    {
        if (this.selectionValid)
        {
            lobbyDialog.joinGame();
        }
        else
        {
            Ivanhoe.showErrorMessage("Join Error",
                    "Please select a game from the list before clicking Join.");
            return;
        }
        
        
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource().equals(this.joinButton))
        {
            this.joinGame();
        }
    }
    public Lobby getModel()
    {
        return model;
    }
}
