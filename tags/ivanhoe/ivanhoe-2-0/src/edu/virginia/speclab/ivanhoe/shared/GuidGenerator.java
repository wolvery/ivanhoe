/*
 * Created on Dec 8, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.shared;

import java.rmi.dgc.VMID;

/**
 * @author lfoster
 *
 * Simple class that generates GUIDs using VMID
 */
public final class GuidGenerator
{
   public static String generateID()
   {
      VMID id = new VMID();
      return id.toString();   
   }
}
