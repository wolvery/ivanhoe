/*
 * Created on Nov 25, 2003
 *
 * IDocumentActionListener
 */
package edu.virginia.speclab.ivanhoe.client.game.model.document;

import javax.swing.text.html.HTML.Tag;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;

/**
 * @author lfoster
 *
 * Interface to handle additonand deletion of ivanhoe tags
 */
public interface IDocumentActionListener
{
   void actionAdded(DocumentVersion version, String actionId, Tag tag);
   void actionDeleted(String actionId);
}
