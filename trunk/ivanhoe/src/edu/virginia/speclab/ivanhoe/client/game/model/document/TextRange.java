/*
 * Created on May 5, 2004
 *
 * TextRange
 */
package edu.virginia.speclab.ivanhoe.client.game.model.document;

import javax.swing.text.Element;


/**
 * TextRange is a helper class that encapsulates
 * the data for marking a range of text (start and end pos)
 * It also provides methods to check if ranges are equal and to 
 * find overlap
 * Its important to note that the start and end numbers represent
 * the space between characters; ie start 0 in the text 'test' is
 * just before the 't'. This means ranges such as 1-10 and 10-15 
 * do NOT overlap, but are adjacent
 */
public class TextRange
{
   public int start, end;
   
   public TextRange(Element e)
   {
      start = e.getStartOffset();
      end = e.getEndOffset();
   }
   
   public TextRange(int s, int e) 
   {
      start = s;
      end = e;
   }
   
   public boolean equals(TextRange that)
   {
      return (this.start == that.start && this.end == that.end);
   }
   
   public boolean overlaps(TextRange that)
   {
      if ( this.end <= that.start || this.start >= that.end )
      {
         return false;
      }
      
      return true;
   }
   
   public TextRange getOverlap(TextRange that)
   {
      // same range?
      if (this.equals(that))
         return that;
      
      if (this.overlaps(that))
      {
         int overlapStart = Math.max(this.start, that.start);
         int overlapEnd = Math.min(this.end, that.end);
         return new TextRange(overlapStart, overlapEnd); 
      }
      
      // no overlap
      return new TextRange(0,0);
   }
   
   public int getLength()
   {
      return end - start;
   }
}