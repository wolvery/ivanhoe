/*
 * Created on May 18, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist.lambda;

import java.io.File;

import edu.virginia.speclab.ivanhoe.shared.blist.BCollectionTransform;

/**
 * This class takes a list of filenames and generates a list of files, with an
 * optional prefix path. 
 * 
 * @author benc
 */
public class NamesToFilesTransform implements BCollectionTransform
{
    private final String path;
    
    /**
     * The default constructor that uses no prefix for the filenames
     */
    public NamesToFilesTransform()
    {
        this.path = "";
    }
    
    /**
     * The constructor that takes a path prefix
     * 
     * @param path
     *      The path under which the files will be created
     */
    public NamesToFilesTransform(String path)
    {
        this.path = path;
    }
    
    public Object transform(Object o)
    {
        return new File(path + File.separator + (String)o);
    }
}
