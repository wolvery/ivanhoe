/*
 * Created on Jan 4, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.shared.data;

import java.io.Serializable;

/**
 * @author Nick
 * 
 */

public class ReferenceResource implements Serializable
{
    private String label;
    private String url;
    private String summary;
    
    public ReferenceResource()
    {
    }

    public ReferenceResource(String label, String url, String summary )
    {
        this.label = label;
        this.url = url;
        this.summary = summary;
        
    }
    
    public ReferenceResource getCopy()
    {
        return new ReferenceResource(label,url,summary);
    }

    /**
     * @return Returns the label.
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * @return Returns the url.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param label The label to set.
     */
    public void setLabel(String label)
    {
        this.label = label;
    }
    /**
     * @param url The url to set.
     */
    public void setUrl(String url)
    {
        this.url = url;
    }
    /**
     * @return Returns the summary.
     */
    public String getSummary()
    {
        return summary;
    }
    /**
     * @param summary The summary to set.
     */
    public void setSummary(String summary)
    {
        this.summary = summary;
    }
}