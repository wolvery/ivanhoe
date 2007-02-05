/*
 * Created on Mar 16, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author benc
 */
public class CreateGameCancelMsg extends Message
{
    private final String gameName;
    
    public CreateGameCancelMsg(String gameName)
    {
        super(MessageType.CREATE_GAME_CANCEL);
        this.gameName = gameName;
    }
    
    public String getGameName()
    {
        return gameName;
    }

}
