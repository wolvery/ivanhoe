/*
 * Created on Jul 1, 2004
 *
 * RefreshLobbyMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * Message sent from client->server to request a refreshed lobby list
 */
public class RefreshLobbyMsg extends Message
{
   public RefreshLobbyMsg()
   {
      super(MessageType.REFRESH_LOBBY);
   }
}
