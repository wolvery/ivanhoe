// Author: Lou Foster
// Typesafe enumeration of all message types supported by ivanhoe
// Date  : 10/01/03 
//
package edu.virginia.speclab.ivanhoe.shared.message;

import java.io.*;

public class MessageType implements Serializable
{
   private final String name;
   
   private MessageType(String name)
   {
      this.name = name;
   }
   
   public String toString()
   {
      return name;
   }
   
   public boolean equals(MessageType that)
   {
      return ( name.equals(that.toString()) );
   }

   public static final MessageType UNDEFINED            = new MessageType("Undefined");
   public static final MessageType LOGIN                = new MessageType("Login");
   public static final MessageType LOGOUT               = new MessageType("Logout");
   public static final MessageType LOGIN_RESPONSE       = new MessageType("LoginResponse");
   public static final MessageType REFRESH_LOBBY        = new MessageType("RefreshLobby");
   public static final MessageType CREATE_ACCOUNT       = new MessageType("CreateAccount");
   public static final MessageType CREATE_GAME          = new MessageType("CreateGame");
   public static final MessageType CREATE_GAME_START    = new MessageType("CreateGameStart");
   public static final MessageType CREATE_GAME_CANCEL   = new MessageType("CreateGameCancel");
   public static final MessageType CREATE_GAME_RESPONSE = new MessageType("CreateGameResponse");   
   public static final MessageType GAME_INFO            = new MessageType("GameInfo");
   public static final MessageType USER_ONLINE          = new MessageType("UserOnline");
   public static final MessageType ROLE_ARRIVED         = new MessageType("RoleArrived");
   public static final MessageType DELETE_DOCUMENT      = new MessageType("DeleteDocument");
   public static final MessageType DOCUMENT_INFO        = new MessageType("DocumentInfo");
   public static final MessageType DOCUMENT_DATA        = new MessageType("DocumentData");
   public static final MessageType DOCUMENT_ERROR       = new MessageType("DocumentError");
   public static final MessageType IMAGE_DATA           = new MessageType("ImageData");
   public static final MessageType DOCUMENT_COMPLETE    = new MessageType("DocumentComplete");
   public static final MessageType DOCUMENT_REQUEST     = new MessageType("DocumentRequest");
   public static final MessageType DOCUMENT_CHANGED     = new MessageType("DocumentChanged");
   public static final MessageType READY                = new MessageType("Ready");
   public static final MessageType SAVE                 = new MessageType("Save");
   public static final MessageType CANCEL_MOVE          = new MessageType("CancelMove");
   public static final MessageType SAVE_RESPONSE        = new MessageType("SaveResponse");
   public static final MessageType RESTORE              = new MessageType("Restore");
   public static final MessageType PLAYER_LIST_REQUEST  = new MessageType("PlayerListRequest");
   public static final MessageType PLAYER_LIST          = new MessageType("PlayerList");
   public static final MessageType ROLE_LEFT            = new MessageType("RoleLeft");
   public static final MessageType CHAT                 = new MessageType("Chat");
   public static final MessageType MOVE                 = new MessageType("Move");
   public static final MessageType MOVE_SUBMIT          = new MessageType("MoveSubmit");
   public static final MessageType MOVE_RESPONSE        = new MessageType("MoveResponse");
   public static final MessageType SERVER_ERROR         = new MessageType("ServerError");
   public static final MessageType JOURNAL_DATA         = new MessageType("JournalData");
   public static final MessageType DISCUSSION_ENTRY     = new MessageType("DiscussionEntry");
   public static final MessageType DISCUSSION_RESPONSE  = new MessageType("DiscussionResponse");
   public static final MessageType ROLE		 		 	= new MessageType("Role");
   public static final MessageType ROLE_UPDATE		    = new MessageType("RoleUpdate");
   public static final MessageType USER_LOOKUP          = new MessageType("UserLookup");
   public static final MessageType USER_LOOKUP_RESPONSE = new MessageType("UserLookupResults");
   public static final MessageType PASS_RESET           = new MessageType("PasswordReset");
   public static final MessageType PASS_RESET_RESPONSE  = new MessageType("PasswordResetResponse");
   public static final MessageType NEW_ACCOUNT_QUERY    = new MessageType("NewAccountQuery");
   public static final MessageType NEW_ACCOUNT_QUERY_RESPONSE = new MessageType("NewAccountQueryResponse");
   public static final MessageType NEW_ROLE_REQUEST	    = new MessageType("NewRoleRequest");
   public static final MessageType NEW_ROLE_RESPONSE	= new MessageType("NewRoleResponse");
   public static final MessageType SELECT_ROLE			= new MessageType("SelectRole");
   public static final MessageType REFERENCE_LIST		= new MessageType("ReferenceList");
   public static final MessageType CATEGORY_LIST		= new MessageType("CategoryList");
   public static final MessageType CLIENT_ERROR         = new MessageType("ClientError");
   public static final MessageType TIME_SYNCH           = new MessageType("TimeSynch");
   public static final MessageType PLAYER_KICKED        = new MessageType("PlayerKicked");
   public static final MessageType DOCUMENT_VERSION     = new MessageType("DocumentVersion");
   
}
