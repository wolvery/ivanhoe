/*
 * Created on Jun 30, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.model.metagame;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.ColorPair;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.IColorModelUpdateListener;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.RoleArrivedMsg;
import edu.virginia.speclab.ivanhoe.shared.message.RoleLeftMsg;
import edu.virginia.speclab.ivanhoe.shared.message.RoleMsg;
import edu.virginia.speclab.ivanhoe.shared.message.RoleUpdateMsg;

public class RoleManager implements IMessageHandler
{
   private LinkedList roleListeners, roleOnlineListeners, colorModelUpdateListeners;
   private LinkedList roles, currentPlayerRoles, onlineRoles;
   private Role currentRole;
   private RoleColorsModel roleColorsModel;

   public RoleManager()
   {
      roles = new LinkedList();
      currentPlayerRoles  = new LinkedList();
      onlineRoles = new LinkedList();
      roleListeners = new LinkedList();
      roleOnlineListeners = new LinkedList();
      colorModelUpdateListeners = new LinkedList();
      
      Ivanhoe.registerGameMsgHandler(MessageType.ROLE, this);
      Ivanhoe.registerGameMsgHandler(MessageType.ROLE_ARRIVED, this);
      Ivanhoe.registerGameMsgHandler(MessageType.ROLE_LEFT, this);
      Ivanhoe.registerGameMsgHandler(MessageType.ROLE_UPDATE, this);
   }

   public void addRoleListener(IRoleListener listener)
   {
      roleListeners.add(listener);
   }
   
   public void addOnlineListener( IRoleOnlineListener listener )
   {
      roleOnlineListeners.add(listener);       
   }
   
   private void fireRoleChanged()
   {
      for (Iterator i = roleListeners.iterator(); i.hasNext();)
      {
         IRoleListener listener = (IRoleListener) i.next();
         listener.roleChanged();
      }
   }
   
   private void fireRoleArrived( String name )
   {
       for (Iterator i = roleOnlineListeners.iterator(); i.hasNext();)
       {
          IRoleOnlineListener listener = (IRoleOnlineListener) i.next();
          listener.roleArrived(name);
       }
   }

   private void fireRoleLeft( String name )
   {
       for (Iterator i = roleOnlineListeners.iterator(); i.hasNext();)
       {
          IRoleOnlineListener listener = (IRoleOnlineListener) i.next();
          listener.roleLeft(name);
       }
   }

   public Role getRole( int roleID )
   {
       if (roleID == Role.GAME_CREATOR_ROLE_ID)
       {
           return Role.GAME_CREATOR_ROLE;
       }
       
       for( Iterator i = roles.iterator(); i.hasNext(); )
       {
           Role role = (Role) i.next();
           if( role.getId() == roleID ) 
               return role;           
       }
       
       return null;
   }
   
   public Role getRole( String roleName )
   {
       if (roleName.equals(Role.GAME_CREATOR_ROLE_NAME))
       {
           return Role.GAME_CREATOR_ROLE;
       }
       
       for( Iterator i = roles.iterator(); i.hasNext(); )
       {
           Role role = (Role) i.next();
           if( role.getName().equals(roleName) ) 
               return role;           
       }
       
       return null;      
   }
   
   public void addRole( String player, Role role )
   {
       // if it's a duplicate, replace the old version
       for (Iterator i = roles.iterator(); i.hasNext(); )
       {
           Role r = (Role)i.next();
           if (r.getId() == role.getId())
           {
               i.remove();
               break;
           }
       }
       
       // current player's roles
       if (player.equals(Ivanhoe.getProxy().getUserName()))
       {
          currentPlayerRoles.add(role);
       }        

       roles.add(role);
       SimpleLogger.logInfo("Added Role "+player+" to role manager.");
       fireRoleChanged();
   }


   private void handleRoleMsg(RoleMsg msg)
   {
      Role role = msg.getRole();
      
      addRole(msg.getPlayerName(),role);
      
      if( msg.isOnline() )
      {
          addOnlineRole( role.getName() );
      }      
   }
   
   public void sendRoleToServer( Role role )
   {
      RoleUpdateMsg roleMsg = new RoleUpdateMsg(role);
      Ivanhoe.getProxy().sendMessage(roleMsg);
   }
   
   public void handleMessage(Message msg)
   {
      if (msg.getType().equals(MessageType.ROLE))
      {
         handleRoleMsg((RoleMsg) msg);
      }
      else if (msg.getType().equals(MessageType.ROLE_ARRIVED))
      {
         handleRoleArrivedMsg((RoleArrivedMsg) msg);
      } 
      else if (msg.getType().equals(MessageType.ROLE_LEFT))
      {
         handleRoleLeftMsg((RoleLeftMsg) msg);
      }
      else if (msg.getType().equals(MessageType.ROLE_UPDATE))
      {
          handleRoleUpdateMsg((RoleUpdateMsg) msg);
      }
   }
   
	private void handleRoleUpdateMsg(RoleUpdateMsg msg)
	{
	    final Role updatedRole = msg.getRole();
        
        addRole(msg.getSender(), updatedRole);
        
        final boolean isCurrentRole;
        if (this.currentRole != null)
        {
            isCurrentRole = 
                getCurrentRole().getId() == updatedRole.getId();
            
        }
        else
        {
            isCurrentRole = false;
        }
        
        if (isCurrentRole)
        {
            setCurrentRole(updatedRole);
        }
	}

    /**
	 * @param msg
	 */
	private void handleRoleLeftMsg(RoleLeftMsg msg)
	{
	    String name = msg.getRoleName();
	    
	    for( Iterator i = onlineRoles.iterator(); i.hasNext(); )
	    {
	        Role role = (Role) i.next();
	        
	        if( role.getName().equals(name) )
	        {
	            onlineRoles.remove(role);
	            fireRoleLeft(name);
	            return;
	        }	        
	    }
	}
	
    /**
	 * @param msg
	 */
	private void handleRoleArrivedMsg(RoleArrivedMsg msg)
	{
	    addOnlineRole( msg.getRoleName() );
	}
	
	private void addOnlineRole( String name )
	{
	    for( Iterator i = onlineRoles.iterator(); i.hasNext(); )
	    {
	        Role role = (Role) i.next();
	        
	        if( role.getName().equals(name) )
	        {
	            return;
	        }	        
	    }
	    
	    Role role = getRole(name);
	    
	    if( role != null )
	    {
	        onlineRoles.add(role);
	        fireRoleArrived(name);
	    }
	    else
	    {
	        SimpleLogger.logError("Unable to find role "+name+" in role manager");
	    }	   
	}

    /**
	 * @return Returns the currentRole.
	 */
	public Role getCurrentRole()
	{
	    return currentRole;
	}
	
	public void setCurrentRole(Role currentRole)
	{
	    this.currentRole = currentRole;
	    
	    if (roleColorsModel != null)
	    {
	        removeColorUpdateListener(roleColorsModel);
	    }
	    roleColorsModel = new RoleColorsModel(currentRole, this);
	    addColorUpdateListener(roleColorsModel);
        
        fireRoleChanged();
	}
	
	/**
	 * @return Returns a list of roles owned by this user.
	 */
	public List getCurrentPlayerRoleList()
	{
	    return currentPlayerRoles;
	}
	
	/**
	 * @return Returns the onlineRoles.
	 */
	public LinkedList getOnlineRoles()
	{
	    return onlineRoles;
	}
    
    public void addColorUpdateListener(IColorModelUpdateListener listener)
    {
        if (!colorModelUpdateListeners.contains(listener))
        {
            colorModelUpdateListeners.add(listener);
        }
    }
	
    public boolean removeColorUpdateListener(IColorModelUpdateListener listener)
    {
        return colorModelUpdateListeners.remove(listener);
    }
    
	public void fireColorUpdate(String roleName, ColorPair colors, int arcType)
	{
	    for (Iterator i=colorModelUpdateListeners.iterator(); i.hasNext(); )
		{
		    ((IColorModelUpdateListener)i.next()).updateColorModel(roleName, colors, arcType);
		}
	}

	public RoleColorsModel getRoleColorsModel()
	{
	    return roleColorsModel;
	}

    public LinkedList getRoles()
    {
        return roles;
    }
    
}