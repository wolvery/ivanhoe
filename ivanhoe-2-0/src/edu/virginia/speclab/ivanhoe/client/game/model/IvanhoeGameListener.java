/*
 * Created on Dec 15, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.model;

import edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay.IRoleChooserListener;
import edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay.RoleChooser;

/**
 * @author Nick
 * 
 *  Listens to IvanhoeGame for when the user has entered a game.
 *
 */
public interface IvanhoeGameListener
{
    public RoleChooser chooseRole();
    public void startGame();
    public void addRoleChooserListener(IRoleChooserListener listener);
    public boolean removeRoleChooserListener(IRoleChooserListener listener);
}
