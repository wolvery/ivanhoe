/*
 * Created on May 25, 2005
 */
package edu.virginia.speclab.ivanhoe.client.lobby;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.shared.blist.lambda.ChainFilter;

/**
 * @author benc
 */
public class FilteredLobbyPanel
        extends LobbyPanel
        implements ItemListener
{
    private ChainFilter lobbyFilter;
    private JPanel filterPanel;
    private JCheckBox archivedFilterCheckBox;
    private JCheckBox restrictedFilterCheckBox;
    
    public FilteredLobbyPanel(LobbyDialog parent)
    {
        super(parent);
        this.lobbyFilter = new ChainFilter();
        
        getBottomPanel().add(getFilterPanel(), BorderLayout.SOUTH);
        
        getArchivedFilterCheckBox().addItemListener(this);
        getRestrictedFilterCheckBox().addItemListener(this);
    }
    
    protected JPanel getFilterPanel()
    {
        if (this.filterPanel == null)
        {
            this.filterPanel = new JPanel();
            TitledBorder border = new TitledBorder("filter out");
            border.setTitleFont(IvanhoeUIConstants.SMALL_FONT);
            this.filterPanel.setBorder(border);
            this.filterPanel.add(getArchivedFilterCheckBox());
            this.filterPanel.add(getRestrictedFilterCheckBox());
        }
        return this.filterPanel;
    }
    
    protected JCheckBox getArchivedFilterCheckBox()
    {
        if (this.archivedFilterCheckBox == null)
        {
            this.archivedFilterCheckBox = new JCheckBox("archived games");
        }
        return this.archivedFilterCheckBox;
    }
    
    protected JCheckBox getRestrictedFilterCheckBox()
    {
        if (this.restrictedFilterCheckBox == null)
        {
            this.restrictedFilterCheckBox = new JCheckBox("restricted games");
        }
        return this.restrictedFilterCheckBox;
    }
    
    public void itemStateChanged(ItemEvent e)
    {
        if (e.getSource().equals(getRestrictedFilterCheckBox()))
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                lobbyFilter.addFilter(Lobby.restrictedFilterOut);
            }
            else
            {
                lobbyFilter.removeFilter(Lobby.restrictedFilterOut);
            }
        }
        else if (e.getSource().equals(getArchivedFilterCheckBox()))
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                lobbyFilter.addFilter(Lobby.archivedFilterOut);
            }
            else
            {
                lobbyFilter.removeFilter(Lobby.archivedFilterOut);
            }
        }
        
        getModel().setEntryFilter(this.lobbyFilter);
    }
}
