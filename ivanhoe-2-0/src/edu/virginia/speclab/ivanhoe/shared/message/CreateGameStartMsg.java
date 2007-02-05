/*
 * Created on Mar 4, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author benc
 */
public class CreateGameStartMsg extends Message
{
    private final String gameName;
    
    public CreateGameStartMsg(String gameName)
    {
        super(MessageType.CREATE_GAME_START);
        this.gameName = gameName;
    }

    public String getGameName()
    {
        return gameName;
    }
}
