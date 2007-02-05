/*
 * Created on Jun 28, 2004
 *
 * GameInfo
 */
package edu.virginia.speclab.ivanhoe.shared.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author lfoster
 *
 * Data to describe an IvanhoeGame
 */
public class GameInfo implements Serializable
{
   private final int id;
   private final String name;
   private final String creator;
   private final String description;
   private final String objectives;
   private final boolean restricted;
   private final boolean archived;
   private final boolean retired;
   private final int startDocWeight;
   private final Set participants;
   
   /**
    * GameInfo constructor
    * 
    * @param id
    *       game ID
    * @param name
    *       name of the game
    * @param creator
    *       name of player who created the game
    * @param description
    * @param objectives
    * @param restrictedAccess
    *       whether this game is restricted to the participants 
    * @param archived
    *       whether or not the game is archived (non-playable)
    * @param retired
    *       whether or not the game is retired (not-visible or playable) 
    * @param startDocWeight
    *       the weight that initial document wedges have (really has no
    *       business being here)
    * @param participants
    *       set of players involved in the game.  This is players who've played
    *       in a public game and players who're invited to restricted/private
    *       games.
    */
   public GameInfo(int id, String name, String creator, String description, 
      String objectives, boolean restrictedAccess, boolean archived, boolean retired,
      int startDocWeight, Set participants)
   {
      this.id = id;
      this.name = name;
      this.creator = creator;
      this.description = description;
      this.objectives = objectives;
      this.restricted = restrictedAccess;
      this.archived = archived;
      this.retired = retired;
      this.startDocWeight = startDocWeight;
      this.participants = participants != null ? participants : new HashSet();
      
      this.participants.add(creator);
   }
   
   /**
    * @param name
    * @param creator
    * @param desription
    * @param objectives
    * @param restrictedAccess
    * @param startDocWeight
    */
   public GameInfo(String name, String creator, String description, 
           String objectives, boolean restrictedAccess, boolean archived, int startDocWeight)
   {
      this(0, name, creator, description, objectives, restrictedAccess, archived, false, startDocWeight, null);
   }
   
   /**
    * Copy constructor
    * @param that Source gameInfo instance
    */
   public GameInfo(GameInfo that)
   {
      this(that.getId(), that.getName(), 
         that.getCreator(), that.getDescription(), that.getObjectives(),
         that.isRestricted(), that.isArchived(), that.isRetired(), that.getStartDocWeight(), that.getParticipants());
   }
   
   /**
    * @return Returns the creator.
    */
   public String getCreator()
   {
      return creator;
   }
   
   /**
    * @return Returns the desription.
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * @return Returns the desription.
    */
   public String getObjectives()
   {
      return objectives;
   }
   
   /**
    * @return Returns the id.
    */
   public int getId()
   {
      return id;
   }
   
   /**
    * @return Returns the name.
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * @return is this a restricted-access game
    */
   public boolean isRestricted()
   {
      return this.restricted;
   }
   
   public boolean isArchived()
   {
       return this.archived;
   }
   
   /**
    * @return is this a retired game
    */
   public boolean isRetired()
   {
      return this.retired;
   }
   
    /**
	 * @return Returns the startDocWeight.
	 */
	public int getStartDocWeight() 
	{
		return startDocWeight;
	}
    
    public Set getParticipants()
    {
        return participants;
    }
   
   public String toString()
   {
      if (isRestricted())
      {
         return "Restricted-access game '" + getName() + "' created by " + getCreator();
      }
      else
      {
         return "Public game '" + getName() + "' created by " + getCreator();
      }
   }
}
