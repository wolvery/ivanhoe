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

class RolePanel extends AbstractRolePanel
{
    private RoleChooser chooser;
    private Role role;
    
    public RolePanel(RoleManager manager, Role role, ActionListener okButtonListener)
    {
        super(manager, false, okButtonListener);
        this.role = role;
        
        initUI();
        
        this.namePanel.add(new JLabel(role.getName()));
        this.namePanel.add(Box.createGlue());
        
        updateRoleSelection();
    }
    
    protected void updateRoleSelection()
    {
        if( roleDescription != null )
        {
            roleDescription.setText(role.getDescription());
        }
        
        if( roleObjective != null )
        {
            roleObjective.setText(role.getObjectives());
        }
        
        colorPreviewPanel.setColors(
                new ColorPair(role.getStrokePaint(), role.getFillPaint()));
	}
    
    protected void acceptRole()
    {
        if (chooser != null) chooser.fireRoleChosen(this.role);
        
    }
    
    public void setChoiceListener(RoleChooser listener)
    {
        chooser = listener;
    }

    protected ColorPair getRoleColors()
    {
        return new ColorPair(role.getStrokePaint(), role.getFillPaint());
    }

    protected boolean roleMatches(String roleName)
    {
        return role.getName().equals(roleName);
    }

    protected String getRoleName()
    {
        return role.getName();
    }
}