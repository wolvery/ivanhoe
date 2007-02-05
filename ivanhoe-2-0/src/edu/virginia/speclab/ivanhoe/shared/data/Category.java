/*
 * Created on Jan 13, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.shared.data;

import java.io.Serializable;

/**
 * @author Nick
 *
 */
public class Category implements Serializable 
{
    private int id;
    private String name, description;
    
    public Category()
    {
        // create blank category object
    }
    
    public Category( int id, String name, String description )
    {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription()
    {
        return description;
    }
    /**
     * @param description The description to set.
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String toString()
    {
        return name;
    }
    /**
     * @return Returns the id.
     */
    public int getID()
    {
        return id;
    }
}
