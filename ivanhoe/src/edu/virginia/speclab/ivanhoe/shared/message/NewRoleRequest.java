/*
 * Created on Dec 20, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author Nick
 *
 * Sent to the server to request a new role be created
 */
public class NewRoleRequest extends Message
{
    private String roleName;
    
    public NewRoleRequest( String roleName )
    {
       super(MessageType.NEW_ROLE_REQUEST);
       this.roleName = roleName;
    }
    
    public String toString()
    {
       return super.toString() + " New Role Request [" + roleName + "]";
    }
    
    /**
     * @return Returns the roleName.
     */
    public String getRoleName()
    {
        return roleName;
    }
}
