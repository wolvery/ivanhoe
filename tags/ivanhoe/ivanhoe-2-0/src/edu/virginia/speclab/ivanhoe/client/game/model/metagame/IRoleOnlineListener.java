/*
 * Created on Dec 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.client.game.model.metagame;

/**
 * @author Nick
 *
 * Listens for the online status of player roles.
 */
public interface IRoleOnlineListener
{
    public void roleArrived( String name );
    public void roleLeft( String name );
}
