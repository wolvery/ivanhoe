/*
 * Created on Jan 19, 2004
 *
 * HistoryPanel
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator.history;

import java.awt.Point;
import java.util.Iterator;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.tree.DefaultTreeModel;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.DiscourseFieldNavigator;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeStyleInternalFrame;
import edu.virginia.speclab.ivanhoe.shared.blist.BArrayList;
import edu.virginia.speclab.ivanhoe.shared.blist.BList;


/**
 * @author lfoster
 *
 * List the DF Move history
 */
public class DiscourseHistoryWindow extends IvanhoeStyleInternalFrame implements ChangeListener    
{
   private final BList historyViews;
   private final JTabbedPane tabPane;
   private final DiscourseFieldNavigator dfNavigator;
   
   private static final int HISTORY_WINDOW_START_X = 70;
   private static final int HISTORY_WINDOW_START_Y = 10;
   
   private static Point windowLocation = new Point(HISTORY_WINDOW_START_X,HISTORY_WINDOW_START_Y);
   
   public DiscourseHistoryWindow(DiscourseFieldNavigator navigator)
   {
      super("history log");

      this.dfNavigator = navigator;
      this.historyViews = new BArrayList();
      this.tabPane = new JTabbedPane();
      tabPane.setTabPlacement(JTabbedPane.BOTTOM);
      tabPane.setFont(IvanhoeUIConstants.BOLD_FONT);
      getContentPane().add(tabPane);

      setLocation(windowLocation);
      
      // listen for editor close events
      addInternalFrameListener(new InternalFrameAdapter()
      {
         public void internalFrameClosing(InternalFrameEvent e)
         {             
            handleClose();
         }         
      });
      
      tabPane.addChangeListener(this); 
   }

   public void addHistoryView(String name, DefaultTreeModel historyTreeModel)
   {
       DiscourseHistoryView historyView = new DiscourseHistoryView(historyTreeModel,dfNavigator,dfNavigator.getRoleManager());
       dfNavigator.addListener(historyView);
       historyViews.add(historyView);
       tabPane.addTab(name,historyView);
   }
   
   public void setLocation( Point location )
   {
       super.setLocation(location);
       windowLocation = location;
   }
   
   public void setLocation( int x, int y )
   {
       super.setLocation(x,y);
       windowLocation.x = x;
       windowLocation.y = y;
   }
   
   private void handleClose()
   {
       Point location = getLocation();
       windowLocation = location;
       
       for (Iterator i=historyViews.iterator(); i.hasNext(); )
       {
           DiscourseHistoryView historyView = (DiscourseHistoryView)i.next();
           historyView.handleClose();
       }
   }

	public void stateChanged(ChangeEvent arg0)
	{
	    //TODO make it so switching from player tab to move tab updates the selected move.
//	    if( tabPane.getSelectedComponent() instanceof DiscourseHistoryView )
//	    {
//	        DiscourseHistoryView view = (DiscourseHistoryView) tabPane.getSelectedComponent();
//	        
//	    }	     
	}
}
