/*
 * Created on May 25, 2005
 */
package edu.virginia.speclab.ivanhoe.client.lobby;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;

/**
 * @author benc
 */
public class PersonalLobbyPanel extends LobbyPanel
{
    public PersonalLobbyPanel(LobbyDialog parent)
    {
        super(parent);
        this.getModel().setEntryFilter(new Lobby.ParticipantFilter(Ivanhoe.getProxy().getUserName()));
    }
}
