/*
 * Created on Oct 7, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.shared.data;

import java.io.Serializable;

/**
 * @author lfoster
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class User implements Serializable
{
   private final int id;
   private final String userName;
   private final String lastName;
   private final String firstName;
   private final String email;
   private final String affiliation;
   private final String password;
   private boolean newGamePermission;
   private boolean newRolePermission;
   private boolean writePermission;
   private boolean admin;
   
   public User(String name, String passwd, String lastName, String firstName,
      String email, String affiliation)
   {
      this(0, name, passwd, lastName, firstName, email, affiliation, false);
   }
   
   public User(int id, String name, String passwd, String lastName, String firstName,
      String email, String affiliation, boolean newGamePermission)
   {
      this.id = id;
      this.userName = name;
      this.lastName = lastName;
      this.firstName = firstName;
      this.email = email;
      this.affiliation = affiliation;
      this.password = passwd;
      this.newGamePermission = newGamePermission;
   }
   
   public User(User that)
   {
      this(that.getId(), that.getUserName(), that.getPassword(), that.getLastName(),
         that.getFirstName(), that.getEmail(), that.getAffiliation(), that.getNewGamePermission());
   }
   
   public int getId()
   {
      return this.id;
   }
   
   public String getEmail()
   {
      return email;
   }

   public String getFirstName()
   {
      return firstName;
   }

   public String getLastName()
   {
      return lastName;
   }

   public String getUserName()
   {
      return userName;
   }
   
   public String getPassword()
   {
      return this.password;
   }
   
   public String getAffiliation()
   {
      return this.affiliation;
   }
   
   public String toString()
   {
      return "User: userName[" + this.userName + "], " +
                   "lastName[" + this.lastName + "], " +                   "firstName[" + this.firstName + "], " + 
                   "email[" + this.email + "], " +
                   "affiliation[" + this.affiliation + "], " +
                   "new_game_permission[" + this.newGamePermission + "]";
   }
   
   public boolean getNewRolePermission()
   {
       return newRolePermission;
   }
   
   public boolean isAdmin()
   {
       return admin;
   }
   
   public boolean getWritePermission()
   {
       return writePermission;
   }
    
   public boolean getNewGamePermission()
   {
       return newGamePermission;
   }
   public void setNewGamePermission(boolean newGamePermission)
   {
       this.newGamePermission = newGamePermission;
   }
   
   public void setNewRolePermission(boolean newRolePermission)
   {
       this.newRolePermission = newRolePermission;
   }
   
   public void setWritePermission(boolean writePermission)
   {
       this.writePermission = writePermission;
   }
   
   public void setAdmin(boolean admin)
   {
       this.admin = admin;
   }
}
