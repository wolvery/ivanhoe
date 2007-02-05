/*
 * Created on Feb 18, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree;

/**
 * @author benc
 */
public class DocumentNode extends DocumentStateNode
{
    private final String documentTitle;

    public DocumentNode(String documentTitle, boolean showDocStates)
    {
        super(documentTitle, showDocStates, true);
        this.documentTitle = documentTitle;
    }

    public String getDocumentTitle()
    {
        return documentTitle;
    }
}
