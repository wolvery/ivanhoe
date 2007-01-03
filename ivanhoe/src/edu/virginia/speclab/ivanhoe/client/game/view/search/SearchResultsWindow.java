/*
 * Created on May 18, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.search;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import edu.virginia.speclab.ivanhoe.client.game.model.cache.IDocumentLoaderListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.document.IvanhoeDocument;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeStyleInternalFrame;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.Role;

/**
 * 
 * @author Nick Laiacona
 */
public class SearchResultsWindow extends IvanhoeStyleInternalFrame implements
   ActionListener, IDocumentLoaderListener

{
   private DiscourseField discourseField;
   private Role currentRole; 
   private SearchResultsViewer searchResultsViewer;
   private JTextField searchTxt;
   private JButton searchBtn;
   private List requestedDocuments, discourseFieldList;
   private String discourseFieldSearchTxt;
   private boolean discourseFieldSearchReady;

   public SearchResultsWindow( DiscourseField discourseField, Role currentRole )
   {
      super("search");      
      this.discourseField = discourseField;
      this.discourseFieldList = new ArrayList();
      this.requestedDocuments = new ArrayList();
      this.discourseFieldSearchReady = false;
      this.currentRole = currentRole;
      
      createUI();
   }

   /**
    * Create all of the UI components needed for the DocumentEditor
    */
   private void createUI()
   {
      // Create the document toolbar
      JToolBar toolbar = new JToolBar();

      this.searchTxt = new JTextField();
      this.searchTxt.addActionListener(this);
      this.searchTxt.setFocusCycleRoot(true);
      toolbar.add(this.searchTxt);
      
      this.searchBtn = new JButton("search");
      this.searchBtn.addActionListener(this);
      toolbar.add(this.searchBtn);
      
      toolbar.add(Box.createHorizontalGlue());

      toolbar.setFloatable(false);
      this.getContentPane().add(toolbar, BorderLayout.NORTH);

      // search results
      this.searchResultsViewer = new SearchResultsViewer();
      getContentPane().add(this.searchResultsViewer, BorderLayout.CENTER);

      setSize(Workspace.instance.getWidth()/2+30, Workspace.instance.getHeight()/3);
      setVisible(true);

      // listen for editor close events
      addInternalFrameListener(new InternalFrameAdapter()
      {
         public void internalFrameClosing(InternalFrameEvent e)
         {
            handleClose();
         }
         
         public void internalFrameActivated(InternalFrameEvent e)
         {
            SearchResultsWindow.this.searchTxt.requestFocus();
         }
      });
   }

   /**
    * Close the search window... close all docs in list 
    */
   protected void handleClose()
   {
      SimpleLogger.logInfo("Closing search window and all related documents");
      
      // notify DF that all docs held open in the search window can now
      // be closed
      for (Iterator itr = this.discourseFieldList.iterator(); itr.hasNext();)
      {
         discourseField.removeReference( (IvanhoeDocument)itr.next());
      }
   }

   /*
    * (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed(ActionEvent e)
   {
      if (e.getSource().equals(this.searchTxt) ||
          e.getSource().equals(searchBtn))
      {
         requestDiscourseFieldSearch(this.searchTxt.getText());
      }
   }

   // request a search of the discourse field for a particular string
   private void requestDiscourseFieldSearch(String searchString)
   {
      SimpleLogger.logInfo("Requesting documents for search ...");
      
      // clear out the previous request info   
      this.requestedDocuments.clear();
      List documentList = discourseField.getDocumentInfoList();
      this.discourseFieldSearchTxt = searchString;
      this.searchResultsViewer.reset();

      // iterate through the list of documents in the discourse
      // field and request those that have not already been opened
      for (Iterator i = documentList.iterator(); i.hasNext();)
      {
         // TODO: fix this.  I'm pretty sure this doesn't really do the job by getting the latest versions
         DocumentInfo docInfo = (DocumentInfo) i.next();
         if ( isAvailable(docInfo.getTitle()) == false)
         {
            SimpleLogger.logInfo("Requesting document for search: " + docInfo.getTitle());
            this.requestedDocuments.add(docInfo.getTitle());
            
            DocumentVersion latestVersion = discourseField.getDocumentVersionManager()
                    .findLatestVersion(docInfo.getTitle(), currentRole.getId() );
            discourseField.requestDocument(latestVersion, this);
         }
         else
         {
            SimpleLogger.logInfo(docInfo.getTitle() + " is already available");
         }
      }

      // all documents have been requested, we are ready to search
      // once we have recieved them.
      this.discourseFieldSearchReady = true;

      // check and see if we got all the documents before we knew
      // we were ready.
      if (this.requestedDocuments.isEmpty() == true)
      {
         searchDiscourseField();
      }
   }

   /**
    * @param docInfo
    * @return Returns TRUE if the doc is already available in search's DF list
    */
   private boolean isAvailable(String docTitle)
   {
      boolean available = false;
      for (Iterator itr = this.discourseFieldList.iterator(); itr.hasNext();)
      {
         IvanhoeDocument doc = (IvanhoeDocument)itr.next();
         if (doc.getTitle().equals(docTitle))
         {
            available = true;
            break;
         }
      }
      return available;
   }

   /*
    * (non-Javadoc)
    * @see edu.virginia.speclab.ivanhoe.client.model.event.IDocumentLoaderListener#documentLoaded(edu.virginia.speclab.ivanhoe.client.model.document.IvanhoeDocument)
    */
   public void documentLoaded(IvanhoeDocument document)
   {
      this.discourseFieldList.add(document);
      this.requestedDocuments.remove(document.getTitle());

      if (this.requestedDocuments.isEmpty() == true)
      {
         searchDiscourseField();
      }
   }

   private synchronized void searchDiscourseField()
   {
      if (this.discourseFieldSearchReady == true)
      {
         SimpleLogger.logInfo("Searching discourse field for string: "
            + this.discourseFieldSearchTxt);
         this.searchResultsViewer.search(this.discourseFieldList, this.discourseFieldSearchTxt);
         this.discourseFieldSearchReady = false;
      }
   }

   /**
    * Document unable to load
    */
   public void documentLoadError(String docTitle, String errorMessage)
   {
      this.requestedDocuments.remove(docTitle);
      if (this.requestedDocuments.isEmpty() == true)
      {
         searchDiscourseField();
      }
   }

}

