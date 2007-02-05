/*
 * Created on Sep 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree;

import java.util.Date;

import edu.virginia.speclab.ivanhoe.shared.data.Move;


public class MoveNode extends DocumentStateNode
{
  private Move move;

  public MoveNode(String label, Move move, boolean showDocStates )
  {
     super(label, showDocStates, true);
     this.move = move;
  }

  public Move getMove()
  {
      return move;
  }

  public String getPlayerName()
  {
      if( move == null ) return null;
      return move.getRoleName();
  }

  public Date getDate()
  {
     if( move == null ) return null;
     return move.getSubmissionDate();
  }

  public String getNarrative()
  {
     if( move == null ) return null;
     return move.getDescription();
  }

  public int getNumStates()
  {
      return 1;
  }
}