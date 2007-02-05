/*
 * Created on Jan 3, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.model.metagame;

import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.ColorPair;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.CustomColors;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.IColorModelUpdateListener;
import edu.virginia.speclab.ivanhoe.shared.data.Role;

/**
 * @author benc
 */
public class RoleColorsModel implements IColorModelUpdateListener
{
    private RoleManager roleManager;
    private Role role;
    
    public RoleColorsModel(Role role, RoleManager roleManager)
    {
        this.roleManager = roleManager;
        this.role = role;
    }
  
    public void updateColorModel( String roleName, ColorPair colors, int arcType )
    {
        if (role.getName().equals(roleName))
        {
	        if (arcType == CustomColors.PLAYER_CIRCLE)
	        {
	            // update the player circle colors and send them to server 
	            role.setStrokePaint(colors.strokeColor);
	            role.setFillPaint(colors.fillColor);
	            roleManager.sendRoleToServer(role);
	        }
	        else
	        {
	            // update the non-playercircle colors
	            CustomColors customColors = Workspace.instance.getNavigator().getCustomColors();
	            customColors.setArcColors(colors, arcType);
	            customColors.saveColors();
	        }
        }
    }
}
