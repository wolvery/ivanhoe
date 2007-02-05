/*
 * Created on Jun 20, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist.lambda;

import edu.virginia.speclab.ivanhoe.shared.blist.BCollectionFilter;
import edu.virginia.speclab.ivanhoe.shared.data.ActionType;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;


public class ActionTypeFilter implements BCollectionFilter
{
    private final ActionType type;
    
    public ActionTypeFilter(ActionType type)
    {
        this.type = type;
    }
    
    public boolean accept(Object o)
    {
        return ((IvanhoeAction) o).getType().equals(type);
    }
}