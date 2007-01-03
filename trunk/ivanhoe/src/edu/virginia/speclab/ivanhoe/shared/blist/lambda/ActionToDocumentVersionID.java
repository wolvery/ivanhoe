package edu.virginia.speclab.ivanhoe.shared.blist.lambda;

import edu.virginia.speclab.ivanhoe.shared.blist.BCollectionTransform;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;

public class ActionToDocumentVersionID implements BCollectionTransform
{
    public Object transform(Object o)
    {
        return new Integer(((IvanhoeAction)o).getDocumentVersionID());
    }

}
