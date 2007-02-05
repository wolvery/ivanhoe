/*
 * Created on Dec 20, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.model.metagame;

import java.awt.Color;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.NewRoleRequest;
import edu.virginia.speclab.ivanhoe.shared.message.NewRoleResponse;

/**
 * @author Nick
 *
 * Handles the transaction with the server for new role creation.
 */
public class NewRoleTransaction implements IMessageHandler
{
    private String description, objective; 
	private Color strokeColor, fillColor;
	
    private NewRoleTransactionListener listener;
    private RoleManager roleManager;
    
    public NewRoleTransaction(String roleName, String description, String objective, 
            			   Color strokeColor, Color fillColor, 
            			   RoleManager roleManager, NewRoleTransactionListener listener )
    {
        this.description = description;
        this.objective = objective;
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
        
        this.listener = listener;
        this.roleManager = roleManager;
        
        Ivanhoe.registerGameMsgHandler(MessageType.NEW_ROLE_RESPONSE,this);
        sendNewRoleRequest(roleName);
    }
    
    private void sendNewRoleRequest(String roleName)
    {
        NewRoleRequest newRoleRequest = new NewRoleRequest(roleName);
        Ivanhoe.getProxy().sendMessage(newRoleRequest);        
    }
    
    private void fireNewRoleCreated( Role role )
    {
        if( listener != null )
        {
            listener.newRoleCreated(role);
        }
    }
    
    private void fireNewRoleNotCreated( String reason )
    {
        if( listener != null )
        {
            listener.newRoleNotCreated(reason);
        }
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.shared.IMessageHandler#handleMessage(edu.virginia.speclab.ivanhoe.shared.message.Message)
     */
    public void handleMessage(Message msg)
    {
        // recieve the new role response message
        if( msg.getType().equals(MessageType.NEW_ROLE_RESPONSE) )
        {
            // got our response, unregister so we don't recieve future responses
            Ivanhoe.getProxy().unregisterMsgHandler(MessageType.NEW_ROLE_RESPONSE,this);
            NewRoleResponse newRoleResponse = (NewRoleResponse) msg;

            Role role = newRoleResponse.getRole(); 
            if( role != null )
            {
                processNewRole(role);
            }
            else
            {
                // report an unsuccessful creation attempt
                fireNewRoleNotCreated(newRoleResponse.getResponse());
            }
        }
    }

    /**
     * @param role
     * Populate the newly created role object and send it to the appropriate keepers.
     */
    private void processNewRole(Role role)
    {
        // populate the object
        role.setDescription(description);
        role.setObjectives(objective);
        role.setFillPaint(fillColor);
        role.setStrokePaint(strokeColor);
        
        // add the role to the local role manager
        roleManager.addRole(Ivanhoe.getProxy().getUserName(),role);
        
        // update the server with the complete role information            
        roleManager.sendRoleToServer(role);

        // tell the listener the new role has been created
        fireNewRoleCreated(role);        
    }

}
