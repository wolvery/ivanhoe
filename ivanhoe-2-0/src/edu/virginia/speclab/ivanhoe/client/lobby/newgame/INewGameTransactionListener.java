/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.client.lobby.newgame;

/**
 * @author Nick
 * 
 * Notifies the listener of the outcome of a new game transaction.
 */
public interface INewGameTransactionListener
{
    public void createFailed( int errorCode );
    public void createSuccess();
}
