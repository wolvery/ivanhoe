/*
 * Created on Oct 9, 2003
 *
 * IAuthProxy
 */
package edu.virginia.speclab.ivanhoe.server;

import edu.virginia.speclab.ivanhoe.shared.AbstractProxy;

/**
 * @author lfoster
 *
 * This interface is called when a proxy has successfully passed 
 * authorizarions
 */
public interface IAuthenticationListener
{
    public void userAuthorized(String userName, AbstractProxy authProxy);
}
