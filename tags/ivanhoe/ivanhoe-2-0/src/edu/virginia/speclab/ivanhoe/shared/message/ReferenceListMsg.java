/*
 * Created on Jan 4, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import java.util.LinkedList;
import java.util.List;

import edu.virginia.speclab.ivanhoe.shared.data.ReferenceResource;

/**
 * @author Nick
 *
 */
public class ReferenceListMsg extends Message
{
    private List resources;
    
    public ReferenceListMsg()
    {
        super(MessageType.REFERENCE_LIST);
        resources = new LinkedList();
    }
    
    public void addResource( ReferenceResource bookmark )
    {
        resources.add(bookmark);
    }
    
    public List getResources()
    {
        return resources;
    }
    
}
