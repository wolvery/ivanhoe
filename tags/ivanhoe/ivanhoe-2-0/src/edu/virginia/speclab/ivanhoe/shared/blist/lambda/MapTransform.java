/*
 * Created on Jul 11, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist.lambda;

import java.util.Map;

import edu.virginia.speclab.ivanhoe.shared.blist.BCollectionTransform;

/**
 * @author benc
 */
public class MapTransform implements BCollectionTransform
{
    private final Map map;
    private final boolean disallowAbsentKeys;
    
    public MapTransform(Map map)
    {
        this.map = map;
        this.disallowAbsentKeys = false;
    }
    
    public MapTransform(Map map, boolean disallowAbsentKeys)
    {
        this.map = map;
        this.disallowAbsentKeys = disallowAbsentKeys; 
    }
    
    public Object transform(Object o)
    {
        Object out = map.get(o);
        if (disallowAbsentKeys && out == null)
        {
            throw new RuntimeException("Map cannot translate key ["+o+"]");
        }
        return out;
    }

}
