/*
 * Created on Mar 5, 2004
 *
 * IvanhoeClipboard
 */
package edu.virginia.speclab.ivanhoe.client.game.model.document;

import java.util.Collection;
import java.util.Iterator;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;

/**
 * @author lfoster
 *
 * IvanhoeClipboard is the DataFlavor class used by IvanhoeSelection. 
 * It allows custom IvanhoeContent including link actions, 
 * and source player to be transferred via the system clipboard
 */
public class IvanhoeClipboard
{
   private String txtContent;
   private Collection copiedActions;
   private DocumentVersion sourceVersion;
   
   IvanhoeClipboard(DocumentVersion sourceVersion, String txt, Collection acts)
   {
      this.sourceVersion = sourceVersion;
      this.txtContent = txt;
      this.copiedActions = acts;
   }

   public String getSourcePlayer()
   {
      return this.sourceVersion.getRoleName();
   }
   
   public DocumentVersion getSourceVersion()
   {
      return this.sourceVersion;
   }
   
   public Iterator getCopiedActions()
   {
      return this.copiedActions.iterator();
   }
   
   public String getCopiedText()
   {
      return this.txtContent;
   }
   
   public String toString()
   {
      return getCopiedText();
   }
}
