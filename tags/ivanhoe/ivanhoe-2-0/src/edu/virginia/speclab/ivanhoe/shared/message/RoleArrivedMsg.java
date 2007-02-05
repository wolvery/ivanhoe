/*
 * Created on Oct 13, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author lfoster
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class RoleArrivedMsg extends Message
{
   private String roleName;
   
   public RoleArrivedMsg()
   {
      super(MessageType.ROLE_ARRIVED);
   }
   
   public String getRoleName()
   {
      return roleName;
   }

   public void setRoleName(String string)
   {
      roleName = string;
   }
   
   public String toString()
   {
      return super.toString() + " Role [" + this.roleName + "]";
   }
}
