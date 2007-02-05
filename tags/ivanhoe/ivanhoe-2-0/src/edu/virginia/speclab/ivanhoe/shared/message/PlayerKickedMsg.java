/*
 * Created on Apr 19, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author benc
 */
public class PlayerKickedMsg extends Message
{
    private final String reason; 
    public PlayerKickedMsg(String reason)
    {
        super(MessageType.PLAYER_KICKED);
        this.reason = reason;
    }
    public String getReason()
    {
        return this.reason;
    }
}
