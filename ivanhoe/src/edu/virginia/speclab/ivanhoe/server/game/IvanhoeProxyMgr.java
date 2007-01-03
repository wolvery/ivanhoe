/*
 * Created on Dec 21, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.server.game;

import edu.virginia.speclab.ivanhoe.server.ProxyMgr;
import edu.virginia.speclab.ivanhoe.shared.AbstractProxy;
import edu.virginia.speclab.ivanhoe.shared.IDisconnectListener;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.message.RoleLeftMsg;

/**
 * @author Nick
 * 
 * Ivanhoe game specific functionality for the proxy manager.
 */
public class IvanhoeProxyMgr extends ProxyMgr implements IDisconnectListener
{
    public void notifyDisconnect( AbstractProxy proxy )
    {
        if( proxy instanceof UserProxy )
        {
            UserProxy userProxy = (UserProxy) proxy;
            RoleLeftMsg roleLeftMessage = new RoleLeftMsg();
            
            Role role = userProxy.getCurrentRole();            
            if( role != null )
            {
                roleLeftMessage.setRoleName(role.getName());
                broadcastMessage(roleLeftMessage);
            }            
        }    
        
        super.notifyDisconnect(proxy);
    }
}
