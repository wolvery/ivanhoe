/*
 * Created on Oct 6, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;

/**
 * @author benc
 */
public interface IDocumentVersionManagerListener
{
    public void documentVersionAdded(DocumentVersion version);
    public void documentVersionRemoved(DocumentVersion version);
}
