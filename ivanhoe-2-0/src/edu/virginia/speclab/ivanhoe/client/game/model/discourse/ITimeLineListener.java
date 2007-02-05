/*
 * Created on Jun 18, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;

/**
 * @author Nick
 */
public interface ITimeLineListener
{
    public void moveAddedToHistory( MoveEvent event );
    public void addNewDocument( DocumentInfo docInfo );
    public void removeDocument(DocumentInfo docInfo);
}
