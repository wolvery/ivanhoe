/*
 * Created on Feb 10, 2004
 *
 * A bean-like object that encapsulates an IvanhoeDocument and an offset, used 
 * by a SearchResults object.
 * 
 */
package edu.virginia.speclab.ivanhoe.client.game.view.search;

import edu.virginia.speclab.ivanhoe.client.game.model.document.IvanhoeDocument;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;

/**
 * @author Nathan Piazza
 * 
 * To change this generated comment go to Window>Preferences>Java>Code
 * Generation>Code and Comments
 */
public class SearchResult
{
   private IvanhoeDocument document;
   private int offset;

   public SearchResult(IvanhoeDocument doc, int offset)
   {
      this.document = doc;
      this.offset = offset;
   }
   
   public IvanhoeDocument getDocument()
   {
      return this.document;
   }

   public DocumentVersion getDocumentVersion()
   {
      return this.document.getVersion();
   }

   public int getOffset()
   {
      return this.offset;
   }
}