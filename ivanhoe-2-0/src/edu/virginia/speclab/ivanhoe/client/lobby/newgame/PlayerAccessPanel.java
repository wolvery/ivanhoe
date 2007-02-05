/*
 * Created on Jan 12, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.lobby.newgame;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.PlayerListMsg;
import edu.virginia.speclab.ivanhoe.shared.message.PlayerListRequestMsg;
import edu.virginia.speclab.ivanhoe.shared.message.ServerErrorMsg;

/**
 * @author Nick
 *
 */
class PlayerAccessPanel extends JPanel implements ActionListener, IMessageHandler
{
    private JCheckBox restrictedBox;
    private JList availablePlayers;
    private DefaultListModel availablePlayerModel;
    private JList gamePlayers;
    private DefaultListModel gamePlayerModel;
    private JButton addPlayerBtn;
    private JButton removePlayerBtn;
    private boolean valid;
    
    public PlayerAccessPanel()
    {
       valid = false;
       
       setLayout(new BorderLayout());
       add( Box.createHorizontalStrut(5), BorderLayout.EAST);
       add( Box.createHorizontalStrut(5), BorderLayout.WEST);

       this.restrictedBox = new JCheckBox("Restrict player access to this game?");
       this.restrictedBox.setFont(IvanhoeUIConstants.SMALL_FONT);
       this.restrictedBox.setBorder( new EmptyBorder(2,0,10,0));
       this.restrictedBox.setSelected(false);
       this.restrictedBox.addActionListener( new ActionListener() {
	        public void actionPerformed(ActionEvent e)
	        {
	            handleToggleRestrictAccess();
	        } 
       });
       
       JPanel pnl = new JPanel(new BorderLayout());
       pnl.add( createPlayerPanel(), BorderLayout.CENTER);
       pnl.add(this.restrictedBox, BorderLayout.NORTH);
       
       add(pnl, BorderLayout.CENTER);
       
       Ivanhoe.getProxy().registerMsgHandler(MessageType.SERVER_ERROR, this);
       Ivanhoe.getProxy().registerMsgHandler(MessageType.PLAYER_LIST, this);
       Ivanhoe.getProxy().sendMessage( 
          new PlayerListRequestMsg(PlayerListRequestMsg.ALL_PLAYERS));
    }
    
    private void handleToggleRestrictAccess()
    {
        // if de-selected, clear the restricted list.
        if( this.restrictedBox.isSelected() == false )
        {
            gamePlayerModel.clear();
        }
        
        // toggle the arrrows
        this.addPlayerBtn.setEnabled( !addPlayerBtn.isEnabled() );
        this.removePlayerBtn.setEnabled( !removePlayerBtn.isEnabled() ); 
        this.availablePlayers.setEnabled( !availablePlayers.isEnabled() );
    }
    
    /**
     * create UI for player / game setup
     * @return
     */
    private JPanel createPlayerPanel()
    {
       JPanel pnl = new JPanel( );
       pnl.setLayout( new BoxLayout(pnl, BoxLayout.X_AXIS));
       
       this.availablePlayerModel = new DefaultListModel();
       this.availablePlayers =  new JList(this.availablePlayerModel);
       availablePlayers.setFixedCellWidth(150);
       JScrollPane availSp = new JScrollPane(this.availablePlayers);
       availSp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
       availSp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
       JPanel p = new JPanel(new BorderLayout());
       p.add(availSp, BorderLayout.CENTER);
       TitledBorder pBorder = new TitledBorder(new EmptyBorder(1,1,1,1),"Available Players");
       pBorder.setTitleFont( IvanhoeUIConstants.SMALL_FONT );
       p.setBorder(pBorder);
       pnl.add(p);
       this.availablePlayers.setEnabled(false);
       
       JPanel b = new JPanel();
       b.setLayout( new BoxLayout(b, BoxLayout.Y_AXIS));
       this.addPlayerBtn = new JButton( ResourceHelper.instance.getIcon("res/icons/rightArrow.gif"));
       this.addPlayerBtn.setToolTipText("Add selected player to game access list");       
       this.addPlayerBtn.addActionListener(this);
       this.addPlayerBtn.setEnabled(false);
       this.removePlayerBtn = new JButton( ResourceHelper.instance.getIcon("res/icons/leftArrow.gif"));
       this.removePlayerBtn.setToolTipText("Remove selected player from game access list");
       this.removePlayerBtn.addActionListener(this);
       this.removePlayerBtn.setEnabled(false);
       b.add(this.addPlayerBtn);
       b.add(this.removePlayerBtn);
       pnl.add(b);
       
       this.gamePlayerModel = new DefaultListModel();
       this.gamePlayers =  new JList(this.gamePlayerModel);
       gamePlayers.setFixedCellWidth(150);
       JScrollPane gameSp = new JScrollPane(this.gamePlayers);
       gameSp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
       gameSp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
       JPanel p2 = new JPanel(new BorderLayout());
       p2.add(gameSp, BorderLayout.CENTER);
       TitledBorder p2Border = new TitledBorder(new EmptyBorder(1,1,1,1),"Allowed Players");       
       p2Border.setTitleFont( IvanhoeUIConstants.SMALL_FONT );
       p2.setBorder(p2Border);
       pnl.add(p2);
       
       return pnl;
    }
    
    /**
     * Check if this game is open to all players or has a restricted
     * access list
     * @return
     */
    public boolean isRestricted()
    {
       return this.restrictedBox.isSelected();
    }
    
    /**
     * Get the list of players designated for game access
     * @return
     */
    public List getAccessList()
    {
       List accessList = new ArrayList();
       for (Enumeration enumerator = this.gamePlayerModel.elements(); enumerator.hasMoreElements();)
       {
          accessList.add( enumerator.nextElement() );
       }
       return accessList;
    }

    /**
     * handle UI button clicks
     */
    public void actionPerformed(ActionEvent e)
    {
       if (e.getSource().equals(this.addPlayerBtn))
       {
          handleAddPlayers();
       }
       else if (e.getSource().equals(this.removePlayerBtn))
       {
          handleRemovePlayers();
       }
    }
    
    /**
     * Check if the results of this dialog are valid. Contents
     * are valid when the user clicks OK.
     */
    public boolean isValid()
    {
       return this.valid;
    }

    /**
     * Remove selected player(s) from the game access list
     */
    private void handleRemovePlayers()
    {    
       while (true)
       {
          int idx = this.gamePlayers.getSelectedIndex();
          if (idx > -1)
          {
             Object selObj = this.gamePlayerModel.elementAt(idx);
             this.gamePlayerModel.remove(idx);
             this.availablePlayerModel.addElement(selObj);
             sortList(this.availablePlayerModel);
          }
          else
          {
             break;
          }
       }
       
    }

    /**
     * add selected player(s) to the game access list
     */
    private void handleAddPlayers()
    {
       while (true)
       {
          int idx = this.availablePlayers.getSelectedIndex();
          if (idx > -1)
          {
             this.gamePlayerModel.addElement( this.availablePlayerModel.elementAt(idx));
             sortList(this.gamePlayerModel);
             this.availablePlayerModel.remove(idx);
          }
          else
          {
             break;
          }
       }
    }
    
    private void sortList(DefaultListModel lm)
    {
       String item1, item2;
       for (int i = 0; i < lm.size(); i++)
       {
          for (int j = i + 1; j <  lm.size(); j++)
          {
             item1 = (String)lm.elementAt(i);
             item2 = (String)lm.elementAt(j);
             if ( item1.compareToIgnoreCase(item2) > 0)
             {
                lm.setElementAt(item2, i);
                lm.setElementAt(item1, j);
             }
          }
       }
    }

    /**
     * handle messages from server
     */
    public void handleMessage(Message msg)
    {
       if (msg.getType().equals(MessageType.PLAYER_LIST))
       {
          PlayerListMsg listMsg = (PlayerListMsg)msg;
          for (Iterator itr = listMsg.getNames().iterator(); itr.hasNext();)
          {
             this.availablePlayerModel.addElement(itr.next());
          }
          sortList(this.availablePlayerModel);
          Ivanhoe.getProxy().unregisterMsgHandler(MessageType.PLAYER_LIST, this);
       }
       else if (msg.getType().equals(MessageType.SERVER_ERROR))
       {
          ServerErrorMsg err = (ServerErrorMsg)msg;
          JOptionPane.showMessageDialog(this, 
             err.getErrorTxt(), 
             "Create Game Error", JOptionPane.ERROR_MESSAGE);
       }
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardStepPanel#panelSelected()
     */
    public void panelSelected()
    {
        // TODO Auto-generated method stub

    }

}
