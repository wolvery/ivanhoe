/*
 * Created on Dec 16, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;

/**
 * @author benc
 */
public class ActionSelection
{
    private DiscourseFieldNavigator navigator;
    private LinkedList listeners, pendingListeners, pendingRemovals;
    private boolean selectionChanging;
    
    private IvanhoeAction selectedAction;
    
    ActionSelection(DiscourseFieldNavigator navigator)
    {
        this.navigator = navigator;
        listeners = new LinkedList();
        pendingListeners = new LinkedList();
        pendingRemovals = new LinkedList();
    }
    
    public void addListener(IActionSelectionListener listener)
    {
        List operatingList = selectionChanging ? pendingListeners : listeners;
        if (!operatingList.contains(listener)) operatingList.add(listener);
    }
    
    public boolean removeListener(IActionSelectionListener listener)
    {
        if (selectionChanging)
        {
            return listeners.remove(listener);
        } else {
            boolean willBeRemoved = listeners.contains(listener);
            pendingRemovals.add(listener);
            return willBeRemoved;
        }
        
    }
    
    public void changeSelection(String actionId)
    {
        this.changeSelection(navigator.getDiscourseField().getDiscourseFieldTimeline().getAction(actionId));
    }
    
    public void changeSelection(IvanhoeAction action)
    {
        selectionChanging = true;
        selectedAction = action;
        
        for ( Iterator i=listeners.iterator(); i.hasNext(); )
        {
            ((IActionSelectionListener)i.next()).actionSelectionChanged(selectedAction);
        }
        selectionChanging = false;
        
        
        /* Now make all the changes that resulted from the actionSelection 
         * changing that would've resulted in ConcurrentModificationExceptions.
         * Note that we do not respect order here, and could theoretically have
         * a memory leak if an object is added and then removed within one
         * call of changeSelection().
         */
        
        if (!pendingRemovals.isEmpty())
        {
            listeners.removeAll(pendingRemovals);
            pendingRemovals.clear();
        }
        
        if (!pendingListeners.isEmpty())
        {
            listeners.addAll(pendingListeners);
            pendingListeners.clear();
        }
    }
    
    public IvanhoeAction getSelectedAction()
    {
        return selectedAction;
    }
}
