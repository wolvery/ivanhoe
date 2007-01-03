/*
 * Created on Feb 22, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import edu.virginia.speclab.ivanhoe.shared.blist.BCollectionReducer;

/**
 * @author benc
 */
public abstract class DocumentStateNode extends DefaultMutableTreeNode
{
    private boolean docStatesVisible;
    private boolean usingHTML;
    
    public DocumentStateNode(String label, boolean showDocStates, boolean useHTML)
    {
        super(label);
        this.docStatesVisible = showDocStates;
        this.usingHTML = useHTML;
        
        if (label == null)
        {
            throw new IllegalArgumentException("Cannot create a DocumentStateNode with a null label");
        }
    }

    public int getNumStates()
    {
        int numStates = 0;
        for (Enumeration children = children(); children.hasMoreElements();)
        {
            Object child = children.nextElement();
            if (child instanceof DocumentStateNode)
            {
                DocumentStateNode docStateNode = (DocumentStateNode)child;
                numStates += docStateNode.getNumStates();
            }
        }
        
        return numStates;
    }
    
    public String toString()
    {
        String retString;
        String stateString = "";
        
        if (docStatesVisible)
        {
            int numStates = getNumStates();
            stateString = "("+numStates+" version"
                    + (numStates == 1 ? "" : "s") + ")";
        }
        
        if (usingHTML)
        {
            retString = "<html><body>"+getUserObject()+" "+stateString+"</body></html>";
        }
        else
        {
            retString = getUserObject()+" "+stateString;
        }
        
        return retString;
    }

    public boolean areDocStatesVisible()
    {
        return docStatesVisible;
    }
    public void setdocStatesVisible(boolean docStatesVisible)
    {
        this.docStatesVisible = docStatesVisible;
    }
    public boolean isUsingHTML()
    {
        return usingHTML;
    }
    public void setUsingHTML(boolean usingHTML)
    {
        this.usingHTML = usingHTML;
    }
    
    /**
     * Takes a list of DocumentStateNodes and returns an Integer representing
     * the number of states in them.
     * 
     * @author benc
     */
    static class NumberOfStatesReducer implements BCollectionReducer
    {
        int numVersions;
        
        public boolean reduce(Object o)
        {
            if (o instanceof DocumentStateNode)
            {
                numVersions += ((DocumentStateNode)o).getNumStates();
            }
            return false;
        }

        public Object getReduction()
        {
            return new Integer(numVersions);
        }
        
    }
}
