/*
 * Created on Dec 20, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.Role;

/**
 * @author Nick
 *
 */
public class NewRoleResponse extends Message
{
    private Role role;
    private String response;
    
    public NewRoleResponse( Role role )
    {
       super(MessageType.NEW_ROLE_RESPONSE);
       this.role = role;
       this.response = null;
    }
    
    public NewRoleResponse( String response )
    {
       super(MessageType.NEW_ROLE_RESPONSE);
       this.role = null;
       this.response = response;
    }

    public String toString()
    {
       String roleName = "*BLANK*";
           
       if( role != null )
       {
           roleName = this.role.getName();
       }
       
       return super.toString() + " New Role Response [" + roleName + "]";
    }
    
    /**
     * @return Returns the role.
     */
    public Role getRole()
    {
        return role;
    }
    /**
     * @return Returns the response.
     */
    public String getResponse()
    {
        return response;
    }
}
