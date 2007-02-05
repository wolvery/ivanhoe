/*
 * Created on Oct 25, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

/**
 * @author Duane Gran (dmg2n@virginia.edu)
 * 
 * Class restricts the input for JFC/Swing text components to a certain
 * character limit. Example use:
 * 
 * int columns = 50;
 * JTextField field = new JTextField("initial text");
 * field.addKeyListener(new TextLimit(columns));
 * 
 * This class won't prevent a text component from being over populated via
 * the setText method.
 */

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.text.*;

public class TextLimit implements KeyListener
{
   /** character limit for the text component */
   private int limit = 0;

   public TextLimit(int limit)
   {
      this.limit = limit;
   }

   /*
    * happens for return and backspace, but not for tab. Safe to ignore.
    */
   public void keyTyped(KeyEvent ke)
   {
   }

   /*
    * prefer to catch on release, ignore
    */
   public void keyPressed(KeyEvent ke)
   {     
   }

   /*
    * Upon key release, checks if text exceeds the limit and imposes said limit
    */
   public void keyReleased(KeyEvent ke)
   {
      JTextComponent tf = (JTextComponent) ke.getSource();
      String s = tf.getText();
      if (limit > 0 && s.length() > limit)
      {
         java.awt.Toolkit.getDefaultToolkit().beep();
         tf.setText(s.substring(0, limit));
      }
   }
}

