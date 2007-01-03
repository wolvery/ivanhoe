/*
 * Created on May 5, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist.lambda;

import edu.virginia.speclab.ivanhoe.shared.blist.BCollectionFilter;

/**
 * This class is used to filter a collection of strings matching a pattern.
 * This filter removes objects that are not strings.
 * 
 * @author benc
 */
public class StringFilter implements BCollectionFilter
{
    private final String matchString;
    private final boolean useRegExp;
    
    /**
     * Filter out all entries that do not .equal(matchString)
     * 
     * @param matchString
     *      The string to match against
     */
    public StringFilter(String matchString)
    {
        this.matchString = matchString;
        this.useRegExp = false;
    }
    
    /**
     * Filter out all strings that do not match matchString, either via .equals() or .match()
     * 
     * @param matchString
     *      The string or pattern to match against
     * @param useRegExp
     *      Whether to use regular expressions to match or to simply compare equality
     */
    public StringFilter(String matchString, boolean useRegExp)
    {
        this.matchString = matchString;
        this.useRegExp = useRegExp;
    }
    
    public boolean accept(Object o)
    {
        try
        {
            String element = (String)o;
            return useRegExp ? element.matches(matchString) : element.equals(matchString);  
        }
        catch (ClassCastException cce)
        {
            // Pass through anything that's not a string            
            return true;
        }
    }

}
