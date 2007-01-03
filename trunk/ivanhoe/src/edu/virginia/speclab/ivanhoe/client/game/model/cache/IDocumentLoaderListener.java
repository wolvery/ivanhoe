/*
 * Created on Mar 17, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.virginia.speclab.ivanhoe.client.game.model.cache;

import edu.virginia.speclab.ivanhoe.client.game.model.document.IvanhoeDocument;

/**
 * @author lfoster
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface IDocumentLoaderListener
{
   public void documentLoaded(IvanhoeDocument document);
   public void documentLoadError( String name, String errorMessage );
}
