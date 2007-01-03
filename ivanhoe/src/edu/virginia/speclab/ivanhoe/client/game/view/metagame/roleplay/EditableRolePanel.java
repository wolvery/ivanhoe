/*
 * Created on Jan 6, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay;

import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;

import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.ColorPair;
import edu.virginia.speclab.ivanhoe.shared.data.Role;



/**
 * @author benc
 */

class EditableRolePanel extends AbstractRolePanel
{
    private Role role; 
    
    public EditableRolePanel(RoleManager manager, Role role, ActionListener okButtonListener)
    {
        super(manager, true, okButtonListener);
        this.role = role;
        
        initUI();
        
        this.buttonPanel.setVisible(false);
        
        this.roleDescription.setText(role.getDescription());
        this.roleObjective.setText(role.getObjectives());
        this.roleDescription.setEditable(true);
        this.roleObjective.setEditable(true);
        
        this.namePanel.add(new JLabel(role.getName()));
        this.namePanel.add(Box.createGlue());
    }
    
    protected void acceptRole()
    {
        role.setDescription(this.roleDescription.getText());
        role.setObjectives(this.roleObjective.getText());
        manager.sendRoleToServer(role);
    }

    protected ColorPair getRoleColors()
    {
        return new ColorPair(role.getStrokePaint(), role.getFillPaint());
    }

    protected boolean roleMatches(String roleName)
    {
        return role.getName().equals(roleName);
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay.AbstractRolePanel#getRoleName()
     */
    protected String getRoleName()
    {
        return role.getName();
    }
}