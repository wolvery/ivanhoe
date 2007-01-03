/*
 * Created on Mar 24, 2004
 *
 * DeleteView
 */
package edu.virginia.speclab.ivanhoe.client.game.model.document;

import java.awt.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

/**
 * @author lfoster
 *
 * Inline View extension that can corrrectly render ivanhoe
 * specific action tags
 */
public class IvanhoeInlineView extends InlineView
{
   private Style deleteStyle;
   private Style linkStyle;
   private Style addStyle;

   public IvanhoeInlineView(Element elem)
   {
      super(elem);
      getIvanhoeStyles();
   }

   protected void getIvanhoeStyles()
   {
      StyleSheet sheet = getStyleSheet();
      deleteStyle = sheet.getRule("delete");
      linkStyle = sheet.getRule("ilink");
      addStyle = sheet.getRule("add");
   }
   
   public float getPreferredSpan(int axis)
   {
      // if delete tags are toggled off in the document,
      // and this element contains a delete tag set its
      // preferred span to zero. This causes it to disappear
      // and the surrounding text to collapse and fill the void
      IvanhoeDocument doc = (IvanhoeDocument)this.getDocument();
      if (doc.areDeleteTagsVisible() == false && 
         getElement().getAttributes().isDefined(IvanhoeTag.DELETE))
      {
         return 0;
      }
      
      return super.getPreferredSpan(axis);
   }

   /**
    * Paints the view.
    */
   public void paint(Graphics g, Shape a)
   {
      Color bg = null;
      Color fg = null;
      Font font = null;
      
      // are Ivanhoe tags present?
      AttributeSet atts = getElement().getAttributes();
      if (atts.isDefined(IvanhoeTag.DELETE) == false &&
          atts.isDefined(IvanhoeTag.ILINK) == false &&
          atts.isDefined(IvanhoeTag.ADD) == false)
      {
         // no... just render as basic InlineView
         super.paint(g,a);
         return;
      }
      else
      {
         // pick styles based on tags present
         // delete is the dominant style
         if (atts.isDefined(IvanhoeTag.DELETE))
         {
            bg = getStyleSheet().getBackground(deleteStyle);
            fg = getStyleSheet().getForeground(deleteStyle);
            font = getStyleSheet().getFont(deleteStyle);
         }
         else if (atts.isDefined(IvanhoeTag.ILINK))
         {
            bg = getStyleSheet().getBackground(linkStyle);
            fg = getStyleSheet().getForeground(linkStyle);
            font = getStyleSheet().getFont(linkStyle); 
         }
         else
         {
            bg = getStyleSheet().getBackground(addStyle);
            fg = getStyleSheet().getForeground(addStyle);
            font = getStyleSheet().getFont(addStyle); 
         }
      }
      
      // grab the rectangle that needs to be painted
      Rectangle alloc =
         (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();

      // fill in the background color
      if (bg != null) 
      {
         g.setColor(bg);
         g.fillRect(alloc.x, alloc.y, alloc.width, alloc.height);
      }

      // get start and end text offsets
      int p0 = getStartOffset();
      int p1 = getEndOffset();
      
      // paint the correct highlighter over this range
      Component c = getContainer();
      if (c instanceof JTextComponent) 
      {
         JTextComponent tc = (JTextComponent) c;
         Highlighter h = tc.getHighlighter();
         if (h instanceof LayeredHighlighter) 
         {
            ((LayeredHighlighter)h).paintLayeredHighlights
               (g, p0, p1, a, tc, this);
         }
      }
      
      // render the text
      g.setColor(fg);
      g.setFont(font);
      getGlyphPainter().paint(this, g, a, p0, p1);
      
      // calculate x coords
      View parent = getParent();
      if ((parent != null) && (parent.getEndOffset() == p1)) 
      {
         // strip whitespace on end
         Segment s = getText(p0, p1);
         while ((s.count > 0) && 
                (Character.isWhitespace(s.array[s.count-1]))) 
         {
            p1 -= 1;
            s.count -= 1;
         }
         s = null;
      }
      
      int x0 = alloc.x;
      int p = getStartOffset();
      if (p != p0)
      {
         x0 += (int) getGlyphPainter().getSpan(
            this, p, p0, getTabExpander(), x0);
      }
      int x1 = x0 + (int) getGlyphPainter().getSpan(
         this, p0, p1, getTabExpander(), x0);
         
      // calculate y coordinate
      int d = (int) getGlyphPainter().getDescent(this);
      int h = alloc.height - d; 
      int y = alloc.y + h;
      
      // strikethru deletes
      if (atts.isDefined(IvanhoeTag.DELETE))
      {
         g.setColor(fg);
         g.drawLine(x0, y-3, x1, y-3);
      }
      
      // and underline links
      if (atts.isDefined(IvanhoeTag.ILINK))
      {
         g.setColor(fg.darker().darker());
         IvanhoeDocument doc = (IvanhoeDocument)getDocument();
         if (doc.areLinksUnderlined())
         {
            String anchorId = doc.getTagId(IvanhoeTag.ILINK, getElement());
            y+=1;
            g.drawLine(x0, y, x1, y);
            if (doc.getLinkManager().isOneToMany(anchorId))
            {
               g.drawLine(x0, y+2, x1, y+2);
            }
         }
      }
   }
}
