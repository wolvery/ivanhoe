/*
 * Created on Jan 4, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.model.metagame;

import java.awt.Color;

import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.ColorPair;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.CustomColors;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.IColorModelUpdateListener;

/**
 * @author benc
 */
public class NewRoleColorsModel implements IColorModelUpdateListener
{
    private CustomColors customColors;
    private ColorPair playerCircleColors;
    
    
    public NewRoleColorsModel()
    {
        customColors = Workspace.instance.getNavigator().getCustomColors();
        playerCircleColors = new ColorPair(Color.YELLOW, Color.BLUE);
    }
    
    public void updateColorModel(String roleName, ColorPair colors, int arcType)
    {
        if (roleName == null)
        {
	        if (arcType == CustomColors.PLAYER_CIRCLE)
	        {
	            playerCircleColors = colors;
	        }
	        else
	        {
	            customColors.setArcColors(colors, arcType);
	            customColors.saveColors();
	        }
        }
    }
    
    public ColorPair getPlayerCircleColors()
    {
        return playerCircleColors;
    }
}
