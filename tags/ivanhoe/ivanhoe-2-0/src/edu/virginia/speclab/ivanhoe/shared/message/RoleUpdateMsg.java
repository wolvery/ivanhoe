/*
 * Created on Jul 20, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import java.awt.Color;

import edu.virginia.speclab.ivanhoe.shared.data.Role;

/**
 * @author Nick
 * 
 * Sends an updated role object to the server.
 */
public class RoleUpdateMsg extends Message
{
    private final int id;
    private final String name;
    private final Color strokePaint, fillPaint;
    private final String description;
    private final String objectives;
    private final boolean writePermission;
    
    public RoleUpdateMsg(Role r)
    {
       super(MessageType.ROLE_UPDATE);
       
       this.id = r.getId();
       this.name = r.getName();
       this.strokePaint = r.getStrokePaint();
       this.fillPaint = r.getFillPaint();
       this.description = r.getDescription();
       this.objectives = r.getObjectives();
       this.writePermission = r.hasWritePermission();
    }
    
    public Role getRole()
    {
        return new Role(id, name, description, objectives, strokePaint, fillPaint, writePermission);
    }    
 }
