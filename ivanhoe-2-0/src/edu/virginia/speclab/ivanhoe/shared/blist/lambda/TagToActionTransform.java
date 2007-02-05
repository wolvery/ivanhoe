/*
 * Created on Oct 5, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist.lambda;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTimeline;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.blist.BCollectionTransform;

/**
 * @author benc
 */
public class TagToActionTransform implements BCollectionTransform
{
    private final DiscourseField discourseField;
    private final DiscourseFieldTimeline timeline;
    
    public TagToActionTransform(DiscourseField discourseField)
    {
        this.discourseField = discourseField;
        this.timeline = discourseField.getDiscourseFieldTimeline();
    }
    
    public Object transform(Object o)
    {
        final String actionTag = (String) o;
        
        Object actionObj = timeline.getAction( actionTag );
        if (actionObj == null)
        {
            actionObj = discourseField.getCurrentMove().getAction( actionTag );
            if (actionObj == null)
            {
                SimpleLogger.logError("Could not find action for ["+o+"]",
                        new Exception());
            }
        }
        
        return actionObj;
    }

}
