/*
 * Created on Dec 4, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.shared.data;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import edu.virginia.speclab.ivanhoe.shared.blist.*;
import edu.virginia.speclab.ivanhoe.shared.blist.lambda.ActionToDocumentVersionID;

/**
 * @author lfoster
 *
 * Manage the set of actions ahich make up a move
 * Registerd with each DF doc to handle action events
 */
public class Move implements Serializable, Comparable
{
   protected int id;
   protected BList actions;
   protected int roleID;
   protected String roleName;
   protected Date startDate;
   protected Date submissionDate;
   protected String description;
   protected int category;
   protected List inspirations;
   
   /**
    * Constructor for an unstarted move
    * @param userName
    * @param startDate
    */
   public Move() 
   {
      this(-1, -1, null, null, null, "", new BArrayList(),0,null );
   }
   
   /**
    * Constructor for an unpublished move that contains some actions
    * @param id
    * @param roleID
    * @param roleName
    * @param startDate
    * @param actionList
    * @param category
    * @param inspirationList
    * @param documentVersions
    *       DocumentVersions created by this move
    */
   public Move(int id, int roleID, String roleName, Date startDate, List actionList, int category, List inspirationList )
   {
      this(id, roleID, roleName, startDate, null, "", actionList, category, inspirationList );
   }
   

   /**
    * Fully specified constructor
    * @param id
    * @param roleID
    * @param roleName
    * @param startDate
    * @param subDate
    * @param desc
    * @param actionList
    * @param category
    * @param inspirationList
    * @param documentVersions
    */
   public Move(int id, int roleID, String roleName, Date startDate, Date subDate, String desc,
      List actionList, int category, List inspirationList )
   {
      // clone the passed actions list
      this.actions = new BArrayList();
      for (Iterator itr = actionList.iterator();itr.hasNext();)
      {
         this.actions.add( new IvanhoeAction((IvanhoeAction)itr.next()) );
      }
      
      this.roleName = roleName;
      if (startDate != null)
         this.startDate = (Date)startDate.clone();
      if (subDate != null)
         this.submissionDate = (Date)subDate.clone();
      this.description = desc;
      this.id = id;
      this.roleID = roleID;
      this.category = category;      
      this.inspirations = inspirationList;
   }
   
   /**
    * Copy constructor
    * @param source
    */
   public Move(Move src)
   {
      this(src.getId(), src.getRoleID(), src.getRoleName(), src.getStartDate(), src.getSubmissionDate(),
         src.getDescription(), src.actions, src.getCategory(), src.getInspirations() );
   }
   
   public IvanhoeAction getAction(BCollectionReducer finder)
   {
       return (IvanhoeAction)this.actions.reduce(finder);
   }
   
   public BList getActions(BCollectionFilter filter)
   {
       return (BList)this.actions.filter(filter);
   }
   
   /**
    * Get an action by ActionId
    * @param actionId
    * @return Action or null if not found
    */
   public IvanhoeAction getAction(String actionId)
   {
      IvanhoeAction act = null;
      IvanhoeAction testAct;
      Iterator itr = this.actions.iterator();
      while (itr.hasNext())
      {
         testAct = (IvanhoeAction)itr.next();
         if (testAct.getId().equals(actionId))
         {
            act = testAct;
            break;
         }
      }
      return act; 
   }
   
   /**
    * Check if the given ID is an action in this move
    * @param string
    * @return
    */
   public boolean containsAction(String actId)
   {
      boolean found = false;
      
      // check in active action list
      Iterator itr = this.actions.iterator();
      while (itr.hasNext())
      {
         if ( ((IvanhoeAction)itr.next()).getId().equals(actId) )
         {
            found = true;
            break;
         }
      }
      return found;
   }
   
   public int getId()
   {
      return this.id;
   }
   
   /**
    * get an iterator to the actions that make up this move
    */
   public Iterator getActions()
   {
      return this.actions.iterator();
   }

   /**
    * Obtain a list of the actions that make up this move.
    */
   public BList getActionList()
   {
      return this.actions;
   }
   
   /**
	* get the number of actions 
	*/
   public int getActionCount()
   {
	  return this.actions.size();
   }

   
   /**
    * Get the description of the move
    * @return
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * Get the date on which this move was started
    * @return
    */
   public Date getStartDate()
   {
      return startDate;
   }

   /**
    * get the date on which this move  was submitted
    * @return
    */
   public Date getSubmissionDate()
   {
      return submissionDate;
   }

   /**
    * Get the name of the user that submitted this move
    * @return
    */
   public String getRoleName()
   {       
      return this.roleName;
   }
   
   /**
    * @return Returns true if a move has been started
    */
   public boolean isStarted()
   {
      return (getStartDate() != null);
   }
   
   /**
    * @return Returns true if a move has been published
    */
   public boolean isPublished()
   {
      return (getSubmissionDate() != null);
   }
   
   public String toString()
   {
      StringBuffer buf = new StringBuffer();
      buf.append("  * UserName [").append(this.roleName).append("]\n");
      buf.append("  * StartDate [").append(this.startDate).append("]\n");
      buf.append("  * SubmitDate [").append(this.submissionDate).append("]\n");
      buf.append("  * Description [").append(this.description).append("]\n");
      buf.append("  * Actions: \n");
      Iterator itr = this.actions.iterator();
      int cnt = 1;
      while (itr.hasNext())
      {
         buf.append("  * Action #").append(cnt++).append(": ");
         buf.append(itr.next().toString()).append("\n");   
      }
      
      return buf.toString();
   }

	/**
	 * @return Returns the userID.
	 */
	public int getRoleID()
	{
	    return roleID;
	}
	
	/**
	 * @return Returns the category.
	 */
	public int getCategory()
	{
	    return category;
	}
	
	/**
	 * @return Returns the inspirations.
	 */
	public List getInspirations()
	{
	    return inspirations;
	}
  
    public BSet getDocumentVersionIDs()
    {
        return new BHashSet(actions.transform(new ActionToDocumentVersionID()));
    }
    
    public int compareTo(Object o)
    {
        Move move = (Move) o;        
        return this.submissionDate.compareTo(move.getSubmissionDate());
    }
}