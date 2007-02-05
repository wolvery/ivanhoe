/*
 * Created on Jul 19, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.shared.data;

import java.awt.Color;
import java.io.Serializable;

/**
 * This class holds all the role-specific information, such as colors,
 * description, role name, icon, etc.
 * 
 * @author benc
 */
public class Role implements Serializable
{
   public static final Color DEFAULT_STROKE_PAINT = Color.YELLOW;
   public static final Color DEFAULT_FILL_PAINT = Color.BLUE;

   private final int id;
   private String name;
   private Color strokePaint, fillPaint;
   private String description;
   private String objectives;
   private boolean writePermission;
   public static final int GAME_CREATOR_ROLE_ID = -1;
   public static final String GAME_CREATOR_ROLE_NAME = "";
   public static final Role GAME_CREATOR_ROLE = new Role(Role.GAME_CREATOR_ROLE_ID, Role.GAME_CREATOR_ROLE_NAME, false);
   
   public Role(int id, String roleName, String desc, String obj,
      Color strokeColor, Color fillColor, boolean writePermission)
   {
      this.id = id;
      this.name = roleName;
      this.description = desc;
      this.objectives = obj;
      this.strokePaint = strokeColor;
      this.fillPaint = fillColor;
      this.writePermission = writePermission;
   }
   
   public Role(int id, String roleName, boolean writePermission)
   {
      this(id, roleName, "", "", Role.DEFAULT_STROKE_PAINT, Role.DEFAULT_FILL_PAINT, true);
   }

   public Color getFillPaint()
   {
      return this.fillPaint;
   }

   public void setFillPaint(Color fillPaint)
   {
      this.fillPaint = fillPaint;
   }

   public Color getStrokePaint()
   {
      return strokePaint;
   }

   public void setStrokePaint(Color strokePaint)
   {
      this.strokePaint = strokePaint;
   }

   public String getName()
   {
      return name;
   }

   /**
    * @return Returns the id.
    */
   public int getId()
   {
      return this.id;
   }
   
   /**
    * @return Returns the description.
    */
   public String getDescription()
   {
      return this.description;
   }
   
   /**
    * @return Returns the description.
    */
   public String getObjectives()
   {
      return this.objectives;
   }
   
   /**
    * Sets the description.
    */
   public void setDescription(String desc)
   {
      this.description = desc;
   }
   
   /**
    * Sets the objectives.
    */
   public void setObjectives(String obj)
   {
      this.objectives = obj;
   }

   public boolean hasWritePermission()
   {
       return writePermission;
   }
   
   public void setWritePermission(boolean writePermission)
   {
       this.writePermission = writePermission; 
   }
}