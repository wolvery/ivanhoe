/*
 * Created on May 13, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.Move;

/**
 * 
 * @author Nick Laiacona
 */
public interface IDiscourseFieldListener
{
    public void moveAddedToHistory( Move move );
    public void addNewDocument( DocumentInfo docInfo );
    public void removeDocument(DocumentInfo docInfo);

}
