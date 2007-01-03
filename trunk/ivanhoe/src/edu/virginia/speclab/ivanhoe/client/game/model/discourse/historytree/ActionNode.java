/*
 * Created on Sep 21, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree;

import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;


/**
    * A node in the move history view tree. 
    * @author Nick Laiacona
    */
public class ActionNode extends DocumentStateNode
   {
      private IvanhoeAction action;

      /**
         * Construct a new node for the move history tree. 
         * @param move The <code>Move</code> associated with this node.
         */
      public ActionNode(String label, IvanhoeAction act)
      {
         super(label, false, true);
         this.action = act;
      }

      /**
       * Get the move associated with this node in the history tree.
       * @return
       */
      public IvanhoeAction getAction()
      {
         return this.action;
      }
      
      public int getNumStates()
      {
          return 0;
      }
   }