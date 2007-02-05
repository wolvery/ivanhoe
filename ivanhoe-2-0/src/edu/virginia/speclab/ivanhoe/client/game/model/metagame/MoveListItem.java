/*
 * Created on Jan 25, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.model.metagame;

import edu.virginia.speclab.ivanhoe.shared.data.Move;

/**
 * @author Nick Laiacona
 */
public class MoveListItem
{
   private boolean selected;

   private int number;

   private Move move;

   public MoveListItem(Move move, int number)
   {
      this.move = move;
      this.number = number;
   }

   public String toString()
   {
      return "Move #" + number + " by " + move.getRoleName();
   }

   /**
    * @return Returns the description.
    */
   public String getDescription()
   {
      return move.getDescription();
   }

   public Move getMove()
   {
      return move;
   }

   /**
    * @return Returns the selected.
    */
   public boolean isSelected()
   {
      return selected;
   }

   /**
    * @param selected
    *           The selected to set.
    */
   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }
}