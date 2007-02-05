/*
 * Created on Oct 9, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.shared;

/**
 * @author lfoster
 *
 * Interface that will be notified when a proxy disconnects
 */
public interface IDisconnectListener
{
   public void notifyDisconnect(AbstractProxy proxy);
}
