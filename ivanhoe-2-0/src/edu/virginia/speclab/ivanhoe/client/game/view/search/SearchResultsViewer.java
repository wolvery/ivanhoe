/*
 * Created on Nov 24, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 *
 */

package edu.virginia.speclab.ivanhoe.client.game.view.search;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.document.*;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.document.DocumentEditor;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;

import javax.swing.JPanel;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTML.Tag;

import java.awt.event.*;
import java.util.*;

/**
 * @author Nathan Piazza
 * 
 * To change this generated comment go to Window>Preferences>Java>Code
 * Generation>Code and Comments
 */
public class SearchResultsViewer extends JPanel implements IDocumentActionListener

{
   private JTable resultsTable;
   private JScrollPane sp;
   private SearchResultsTableModel model;
   private boolean resultsValid = false, searchInit = false;

   private java.util.List searchResults = null;
   private String searchString = null;

   public SearchResultsViewer()
   {
      initUI();
   }

   private void initSearch(java.util.List docList, String string)
   {
      if (docList == null)
         return;

      if (searchInit == false)
      {
         Iterator docIter = docList.iterator();
         while (docIter.hasNext())
         {
            ((IvanhoeDocument) docIter.next()).addActionListener(this);
         }

         this.searchString = string;
         searchInit = true;
      }
   }

   /**
    * Handles a click on some search result
    */
   private void handleSearchClick(SearchResult DOPair, String searchResult)
   {
      DocumentVersion docVersion = DOPair.getDocumentVersion();
      DocumentEditor openedEditor = Workspace.instance.openEditor(docVersion);
      openedEditor.highlightText(DOPair.getOffset(), searchResult.length());
   }

   private void initUI()
   {
      this.setValid(true);

      this.model = new SearchResultsTableModel(this);

      resultsTable = new JTable(model);
      resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      resultsTable.setFont(IvanhoeUIConstants.LARGE_FONT);

      // add a listener for results clicks
      MouseAdapter rowMouseListener = new MouseAdapter()
      {
         public void mouseClicked(MouseEvent e)
         {
            if (isValid())
            {
               if (e.getClickCount() == 2)
               {
                  int row = resultsTable.rowAtPoint(e.getPoint());
                  SearchResult clickedResult = model.getSearchResult(row);
                  handleSearchClick(clickedResult, model.getSearchString());
               }
            }
            else
            {
                Ivanhoe.showErrorMessage("Clicked Search Result Not Valid");
            }
         }
      };
      resultsTable.addMouseListener(rowMouseListener);

      resultsTable.getColumnModel().getColumn(0).setPreferredWidth(150);
      resultsTable.getColumnModel().getColumn(1).setPreferredWidth(350);

      sp = new JScrollPane(resultsTable);
      sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      sp
         .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      setLayout(new BorderLayout());
      add(sp, BorderLayout.CENTER);
      add(new JPanel(), BorderLayout.WEST);
      add(new JPanel(), BorderLayout.EAST);
      add(new JPanel(), BorderLayout.SOUTH);
      add(new JPanel(), BorderLayout.NORTH);
   }

   public void search(java.util.List docList, String searchTxt)
   {
      this.searchString = searchTxt;
      generateResults(docList, searchTxt);
      this.model.updateTableModel(this.searchResults, this.searchString);
   }

   private void generateResults(java.util.List docList, String searchTxt)
   {
      initSearch(docList, searchTxt);

      if (searchTxt != null && searchTxt.length() > 0
         && !(searchTxt.equals(" ")))
      {
         ArrayList results = new ArrayList();
         int found = 0;
         String docText = "No Document Text";
         SearchResult DOPair = null;

         // search each document for the specified text. if found, save a the
         // doc
         // version info along with the offset
         for (Iterator docIter = docList.iterator(); docIter.hasNext();)
         {
            IvanhoeDocument theDocument = (IvanhoeDocument) docIter.next();
            int offset = 0;
            try
            {
               docText = theDocument.getText(offset, theDocument.getLength());
               while (offset < docText.length())
               {
                  found = theDocument.search(offset, searchTxt, false);
                  if (found != -1)
                  {
                     DOPair = new SearchResult(theDocument, found);
                     results.add(DOPair);
                     offset = found + searchTxt.length();
                  }
                  else
                  {
                     offset = docText.length();
                  }
               }
            }

            catch (BadLocationException ble)
            {
               SimpleLogger
                  .logError("Error performing search of the discourse field.");
            }
         }
         this.searchResults = results;
         this.setValid(true);
      }
   }

//   public void actionPerformed(ActionEvent evt)
//   {
//      if (evt.getSource().equals(this.updateResults))
//      {
//         this.generateResults(this.allDocuments, this.searchString);
//         this.model.updateTableModel(this.searchResults, this.searchString);
//         resultsTable.getColumnModel().getColumn(0).setPreferredWidth(150);
//         resultsTable.getColumnModel().getColumn(1).setPreferredWidth(350);
//
//      }
//   }

   public void staleResults()
   {
      if (this.isValid())
      {
         this.setValid(false);
         model.fireTableDataChanged();
         model.fireTableStructureChanged();
      }
   }

   public boolean isValid()
   {
      return this.resultsValid;
   }

   public void setValid(boolean valid)
   {
      this.resultsValid = valid;
   }

   /*
    * (non-Javadoc)
    * @see edu.virginia.speclab.ivanhoe.client.model.event.IDocumentActionListener#actionAdded(java.lang.String,
    *      java.lang.String,
    *      edu.virginia.speclab.ivanhoe.client.model.document.IvanhoeTag)
    */
   public void actionAdded(DocumentVersion version, String actionId, Tag type)
   {
      this.staleResults();
   }

   /*
    * (non-Javadoc)
    * @see edu.virginia.speclab.ivanhoe.client.model.event.IDocumentActionListener#actionDeleted(java.lang.String)
    */
   public void actionDeleted(String actionId)
   {
      this.staleResults();
   }

   /**
    * 
    */
   public void reset()
   {
      this.model.reset();
   }
}