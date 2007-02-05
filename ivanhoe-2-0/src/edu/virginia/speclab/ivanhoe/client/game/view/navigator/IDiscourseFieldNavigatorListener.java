/*
 * Created on Aug 25, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;

/**
 * @author Nick
 */
public interface IDiscourseFieldNavigatorListener
{
    public void selectedRoleChanged( String selectedRoleName, MoveEvent currentEvent );
    public void roleAdded( String roleName );
    public void roleRemoved( String roleName );
}
