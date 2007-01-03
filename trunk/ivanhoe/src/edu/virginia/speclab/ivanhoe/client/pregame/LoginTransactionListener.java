/*
 * Created on Dec 16, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.pregame;

/**
 * @author Nick
 * 
 * Listens for a successful login attempt.
 */
public interface LoginTransactionListener
{
    public void loginSuccessful();   
    public void failedConnection();
    public void failedAuthorization();
}
