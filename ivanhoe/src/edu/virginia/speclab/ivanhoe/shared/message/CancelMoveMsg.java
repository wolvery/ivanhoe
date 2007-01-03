/*
 * Created on Jun 29, 2004
 *
 * CancelMoveMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author lfoster
 *
 * Message used to cancel a pending move
 */
public class CancelMoveMsg extends Message
{
    private int roleID;
   
    /**
     * @return Returns the roleID.
     */
    public int getRoleID()
    {
        return roleID;
    }
   public CancelMoveMsg( int roleID )
   {
      super(MessageType.CANCEL_MOVE);
      this.roleID = roleID;
   }

}
