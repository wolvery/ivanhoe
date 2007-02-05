/*
 * Created on Jan 4, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import java.util.LinkedList;
import java.util.List;

import edu.virginia.speclab.ivanhoe.shared.data.Category;

/**
 * @author Nick
 *
 */
public class CategoryListMsg extends Message
{
    private List categoryList;
    
    public CategoryListMsg()
    {
        super(MessageType.CATEGORY_LIST);
        categoryList = new LinkedList();
    }
    
    public void addResource( Category category )
    {
        categoryList.add(category);
    }
    
    public List getCategoryList()
    {
        return categoryList;
    }
    
}
