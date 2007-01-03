/*
 * Created on Dec 20, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author Nick
 *
 */
public class SelectRoleMsg extends Message
{
    private int roleID;
    
    public SelectRoleMsg( int roleID )
    {
       super(MessageType.SELECT_ROLE);
       this.roleID = roleID;
    }
    
    public String toString()
    {
       return super.toString() + " Select Role ID [" + roleID + "]";
    }
    
    /**
     * @return Returns the roleID.
     */
    public int getRoleID()
    {
        return roleID;
    }
}
