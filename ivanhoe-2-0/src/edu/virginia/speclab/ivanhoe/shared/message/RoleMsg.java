/*
 * Created on Jul 20, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.Role;

/**
 * @author Nick
 *
 * Encapsulates information about a role.
 */
public class RoleMsg extends Message
{
    private Role role;
    private String playerName;
    private boolean online;
    
    public RoleMsg(String playerName, Role r, boolean online)
    {
       super(MessageType.ROLE);
       this.role = r;
       this.playerName = playerName;
       this.online = online;
    }
    
    public Role getRole()
    {
       return role;
    }
    
    public String getPlayerName()
    {
       return this.playerName;
    }
    
    /**
     * @return Returns the online.
     */
    public boolean isOnline()
    {
        return online;
    }
 }
