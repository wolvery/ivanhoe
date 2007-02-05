/*
 * Created on Jun 28, 2004
 *
 * Lobby
 */
package edu.virginia.speclab.ivanhoe.client.lobby;

import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.blist.*;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;
import edu.virginia.speclab.ivanhoe.shared.message.GameInfoMsg;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;

/**
 * @author benc
 * @author lfoster
 * 
 * Model for ivanhoe lobby
 */
public class Lobby extends AbstractTableModel implements IMessageHandler
{
    public static final BCollectionFilter restrictedFilterOut = BCollectionAlgorithms.invertFilter(new RestrictedFilter());
    public static final BCollectionFilter archivedFilterOut = BCollectionAlgorithms.invertFilter(new ArchivedFilter());
    
    private TableSorter tableSorter;
    private final BList entries;
    private BList filteredEntries;
    private final BCollection listeners;
    private BCollectionFilter entryFilter;
    protected String columnNames[] = { "game", "creator", "online", "properties" };

    /**
     * Construct the lobby model
     * 
     */
    public Lobby()
    {
        this.entries = new BArrayList();
        this.filteredEntries = new BArrayList();
        this.listeners = new BHashSet();
        this.tableSorter = null;

        // register the model to handle game info messages
        Ivanhoe.getProxy().registerMsgHandler(MessageType.GAME_INFO, this);
    }

    public void addListener(LobbyListener listener)
    {
        listeners.add(listener);
    }
    
    public void removeListener(LobbyListener listener)
    {
        listeners.remove(listener);
    }
    
    public void removeAllListeners()
    {
        listeners.clear();
    }
    
    public void setTableSorter(TableSorter sorter)
    {
        this.tableSorter = sorter;
    }
    
    /**
     * Get number of game entries
     */
    public int getRowCount()
    {
        return this.filteredEntries.size();
    }

    /**
     * Get columns
     */
    public int getColumnCount()
    {
        return this.columnNames.length;
    }

    /**
     * Get the name of a column
     */
    public String getColumnName(int column)
    {
        return columnNames[column];
    }

    /**
     * Get name of game at specified row
     * 
     * @param row
     * @return
     */
    public String getGameName(int row)
    {
        LobbyRec rec = (LobbyRec) this.filteredEntries.get(getEntryLookup(row));
        return rec.info.getName();
    }
    
    private String getPropertiesDescription(LobbyRec rec)
    {
        GameInfo info = rec.info;
        StringBuffer propertiesDesc = new StringBuffer();
        
        if (info.isRestricted())
        {
            propertiesDesc.append("restricted, ");
        }
        if (info.isArchived())
        {
            propertiesDesc.append("archived, ");
        }
        
        String propertiesStr = propertiesDesc.toString();
        int lastComma = propertiesStr.lastIndexOf(',');
        if (lastComma > 0)
        {
            propertiesStr = propertiesStr.substring(0, lastComma);  
        }
        return propertiesStr;
    }

    /**
     * Get host of game at specified row
     * 
     * @param row
     * @return
     */
    public String getGameHost(int row)
    {
        LobbyRec rec = (LobbyRec) this.filteredEntries.get(getEntryLookup(row));
        return rec.host;
    }

    /**
     * Get description of game at specified row
     * 
     * @param row
     * @return
     */
    public String getGameDescription(int row)
    {
        LobbyRec rec = (LobbyRec) this.filteredEntries.get(getEntryLookup(row));
        return rec.info.getDescription();
    }

    /**
     * Get port of game at specified row
     * 
     * @param row
     * @return
     */
    public int getGamePort(int row)
    {
        LobbyRec rec = (LobbyRec) this.filteredEntries.get(getEntryLookup(row));
        return rec.port;
    }

    /**
     * Get info of game at specified row
     * 
     * @param row
     * @return GameInfo
     */
    public GameInfo getGameInfo(int row)
    {
        LobbyRec rec = (LobbyRec) this.filteredEntries.get(getEntryLookup(row));
        return rec.info;
    }

    /**
     * get table value
     */
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        LobbyRec rec = (LobbyRec) this.filteredEntries.get(rowIndex);
        Object val = null;
        if (rec != null)
        {
            switch (columnIndex)
            {
            case 0: // title
                val = rec.info.getName();
                break;
            case 1: // creator
                val = rec.info.getCreator();
                break;
            case 2: // count
                val = new Integer(rec.cnt);
                break;
            case 3: // properties description
                val = getPropertiesDescription(rec);
                break;
            }
        }
        return val;
    }

    /**
     * remove all content from the model
     */
    public void clear()
    {
        this.entries.clear();
        this.filteredEntries.clear();
        fireTableDataChanged();
    }

    /**
     * Handle GameInfo message from lobby server
     */
    public void handleMessage(Message msg)
    {
        if (!msg.getType().equals(MessageType.GAME_INFO))
        {
            return;
        }

        boolean dataChanged = false;

        GameInfoMsg infoMsg = (GameInfoMsg) msg;

        if (infoMsg.getInfo().isRetired())
        {
            for (Iterator i = entries.iterator(); i.hasNext();)
            {
                LobbyRec rec = (LobbyRec) (i.next());
                if (rec.host.equals(infoMsg.getHost())
                        && rec.port == infoMsg.getPort())
                {
                    i.remove();
                    dataChanged = true;
                    break;
                }
            }
        }
        else
        {
            boolean replacedOldEntry = false;
            for (int i = 0; i < entries.size(); ++i)
            {
                LobbyRec rec = (LobbyRec) (entries.get(i));
                if (rec.host.equals(infoMsg.getHost())
                        && rec.port == infoMsg.getPort())
                {
                    LobbyRec newRec = new LobbyRec(infoMsg.getInfo(), infoMsg
                            .getHost(), infoMsg.getPort(), infoMsg
                            .getPlayerCnt());

                    entries.set(i, newRec);
                    replacedOldEntry = true;
                    dataChanged = true;
                    break;
                }
            }

            if (!replacedOldEntry)
            {
                LobbyRec newRec = new LobbyRec(infoMsg.getInfo(), infoMsg
                        .getHost(), infoMsg.getPort(), infoMsg.getPlayerCnt());
                entries.add(newRec);
                int entryRowIndex = entries.size() - 1;
                fireTableRowsUpdated(entryRowIndex, entryRowIndex);
                dataChanged = true;
            }

        }

        if (dataChanged)
        {
            if (entryFilter != null)
            {
                filteredEntries = (BList)entries.filter(entryFilter);
            }
            else
            {
                filteredEntries = entries;
            }
            // notify listers that the data has changed
            fireTableDataChanged();
        }
    }

    private int getEntryLookup(int row)
    {
        return tableSorter == null ? row : tableSorter.modelIndex(row);
    }
    
    private static class LobbyRec
    {
        private GameInfo info;
        private String host;
        private int port;
        private int cnt;

        public LobbyRec(GameInfo info, String host, int port, int cnt)
        {
            this.info = info;
            this.host = host;
            this.port = port;
            this.cnt = cnt;
        }
    }
    public void setEntryFilter(BCollectionFilter entryFilter)
    {
        this.entryFilter = entryFilter;
        if (entryFilter != null)
        {
            filteredEntries = (BList)entries.filter(entryFilter);
        }
        else
        {
            filteredEntries = entries;
        }
        fireTableDataChanged();
    }
    
    public void fireTableDataChanged()
    {
        super.fireTableDataChanged();
        fireLobbyChanged();
    }
    
    private void fireLobbyChanged()
    {
        for (Iterator i=listeners.iterator(); i.hasNext(); )
        {
            ((LobbyListener)i.next()).lobbyChanged(this);
        }
    }
    
    private static class RestrictedFilter implements BCollectionFilter
    {
        public boolean accept(Object o)
        {
            GameInfo info = ((LobbyRec)o).info;
            return info.isRestricted();
        }
    }
    
    private static class ArchivedFilter implements BCollectionFilter
    {
        public boolean accept(Object o)
        {
            GameInfo info = ((LobbyRec)o).info;
            return info.isArchived();
        }
    }
    
    public static class ParticipantFilter implements BCollectionFilter
    {
        private final String participant;
        
        public ParticipantFilter(String participant)
        {
            this.participant = participant;
        }
        
        public boolean accept(Object o)
        {
            GameInfo info = ((LobbyRec)o).info;
            return info.getParticipants().contains(participant);
        }
    }
}
