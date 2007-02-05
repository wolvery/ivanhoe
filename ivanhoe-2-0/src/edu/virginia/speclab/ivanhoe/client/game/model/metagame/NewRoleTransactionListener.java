/*
 * Created on Dec 20, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.model.metagame;

import edu.virginia.speclab.ivanhoe.shared.data.Role;

/**
 * @author Nick
 * 
 * Listens to the new role creation process.
 */
public interface NewRoleTransactionListener
{
    public void newRoleCreated( Role role );
    public void newRoleNotCreated( String reason );
}
