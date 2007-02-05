/*
 * Created on Mar 11, 2004
 *
 * Discussion
 */
package edu.virginia.speclab.ivanhoe.client.game.model.metagame;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.data.DiscussionEntry;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeDateFormat;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.DiscussionEntryMsg;

/**
 * @author lfoster
 *
 * Client-side model for managing discussion data
 */
public class Discussion extends AbstractTableModel implements IMessageHandler
{
   private RoleManager roleManager;
   
   private Vector entries;
   protected String columnNames[] = 
      {
         "Message", "Speaker", "Date"
      };
   
   public Discussion( RoleManager roleManager )
   {
      this.roleManager = roleManager;
      this.entries = new Vector();
      
      // register the model to handle discussion messages
      Ivanhoe.registerGameMsgHandler(MessageType.DISCUSSION_ENTRY, this);
   }
   
   /**
    * Listen for new Scholia entry messages from the server.
    * Add ech entry to a list, and notify listeners
    */
   public void handleMessage(Message msg)
   {
      if  (msg.getType().equals(MessageType.DISCUSSION_ENTRY))
      {
         DiscussionEntryMsg sm = (DiscussionEntryMsg)msg;
         
         // clone the msg data, amd stick it in the managed list
         DiscussionEntry newEntry = new DiscussionEntry(sm.getEntry());
         this.entries.add(newEntry);
         
         // notify listers that the data has changed
         fireTableDataChanged();
      }
   }

   /**
    * Get number of discussion entries
    */
   public int getRowCount()
   {
      return this.entries.size();
   }

   /**
    * Get the number of colums to display
    */
   public int getColumnCount()
   {
      // only 3 cols: title, player and date
      // TODO maybe display 4 cols... type
      return 3;
   }
   
   public String getColumnName(int column)
   {
      return columnNames[column];      
   }

   public Object getValueAt(int rowIndex, int columnIndex)
   {
      DiscussionEntry entry = (DiscussionEntry)this.entries.get(rowIndex);
      Object val = null;
      if (entry != null)
      {
         switch (columnIndex)
         {
            case 0:  // title
               val = entry.getTitle();
               break;
            case 1:  // player
               val = roleManager.getRole(entry.getRoleID()).getName();
               break;
            case 2:  // date
               val = IvanhoeDateFormat.format(entry.getPostingDate());
               break;
         }
      }
      return val;
   }
   
   /**
    * Get the entry at the specified row
    * @param row
    * @return
    */
   public DiscussionEntry getContent(int row)
   {
      return (DiscussionEntry)this.entries.get(row);
   }

	/**
	 * @return Returns the roleManager.
	 */
	public RoleManager getRoleManager()
	{
	    return roleManager;
	}
}
