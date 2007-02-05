/*
 * Created on Jan 14, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.model.metagame;

import java.util.List;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.message.CategoryListMsg;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;

/**
 * @author Nick
 *
 */
public class CategoryManager implements IMessageHandler
{
    private List categoryList;
    
    public CategoryManager()
    {
        Ivanhoe.registerGameMsgHandler( MessageType.CATEGORY_LIST, this );
    }
    
    public CategoryManager( List testList )
    {
        categoryList = testList;
    }
    
    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.shared.IMessageHandler#handleMessage(edu.virginia.speclab.ivanhoe.shared.message.Message)
     */
    public void handleMessage(Message msg)
    {
        if( msg.getType().equals(MessageType.CATEGORY_LIST) )
        {
            CategoryListMsg catMsg = (CategoryListMsg)msg;
            categoryList = catMsg.getCategoryList();                        
        }        
    }
    /**
     * @return Returns the categoryList.
     */
    public List getCategoryList()
    {
        return categoryList;
    }
    
    public boolean hasCategories()
    {
        if( categoryList != null )
        {
            return !categoryList.isEmpty();
        }
        else
        {
            return false;
        }
    }
}
