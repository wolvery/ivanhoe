/*
 * Created on Nov 10, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.shared.data;

import java.io.Serializable;

/**
 * @author Nick
 *
 * Encapsulates a link tag. Link tags are used to mark up links in IvanhoeDocument objects. Link
 * tags store the information necessary to follow the link.  
 * 
 */
public class LinkTag implements Serializable
{
	private int linkType;
    private String backLinkID;
	private int documentVersionID;
    
    // storage for external link data
    private String data;
    
    // DB Link types
    public static final int INTERNAL_LINK_ID = 1;
//    public static final int URL_LINK_ID = 2;
//    public static final int BIBLIOGRAPHIC_LINK_ID = 3;  // not used
    public static final int COMMENTARY_LINK_ID = 4;
    
    /**
     * Create an internal link tag with a back link reference.
     * @param documentVersionID The ID of the link destination. 
     * @param backLinkId The id of the reciporical link, null if there isn't one.
     */
    public LinkTag( int documentVersionID, String backLinkId )
    {
		this.documentVersionID = documentVersionID;
		this.linkType = INTERNAL_LINK_ID;
        this.backLinkID = backLinkId;
    }

    /**
     * Create a commentary or external link tag. 
     * @param data The data associated with this link.
     */
    public LinkTag( String data )
    {
		this.linkType = COMMENTARY_LINK_ID;
        this.data = data;
    }
    
    /**
     * @return Returns the backLinkID.
     */
    public String getBackLinkID()
    {
        return backLinkID;
    }

    /**
     * Get the string representation of this link tag. Only valid for commentary and urls.
     * @return Returns the linkTag.string
     */
    public String getTagData()
    {
        return data;
    }
  
	public int getDocumentVersionID() 
	{
		return documentVersionID;
	}

	public int getLinkType() {
		return linkType;
	}
	
	
}
