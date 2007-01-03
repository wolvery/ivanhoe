/*
 * Created on Feb 10, 2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.client.game.view.search;

import javax.swing.table.AbstractTableModel;
import javax.swing.text.*;

/**
 * @author Nathan F. Piazza
 * 
 * Models the content of a search results table.
 */
public class SearchResultsTableModel extends AbstractTableModel
{
   private java.util.List searchResults = null;
   private String searchString = null;
   private SearchResultsViewer viewer;

   public SearchResultsTableModel(SearchResultsViewer view)
   {
      viewer = view;
   }

   public SearchResultsTableModel(java.util.List searchResults,
      String searchString, SearchResultsViewer viewer)
   {

      this.searchString = searchString;
      this.searchResults = searchResults;
      this.viewer = viewer;
   }

   protected String columnNames[] = { "document", "context" };

   public String getColumnName(int column)
   {
      if (viewer.isValid())
      {
         return columnNames[column];
      }
      else
      {
         return " ";
      }
   }

   public int getRowCount()
   {
      int size;
      if (viewer.isValid())
      {
         if (searchResults == null || this.searchResults.size() < 1)
         {
            size = 1;
         }
         else
         {
            size = this.searchResults.size();
         }
      }
      else
      {
         size = 1;
      }
      return size;
   }

   public int getColumnCount()
   {
      if (viewer.isValid())
      {
         return columnNames.length;
      }
      else
      {
         return 1;
      }
   }

   public Object getValueAt(int rowIndex, int columnIndex)
   {
      String notFound = "search string not found";
      if (this.viewer.isValid())
      {
         if (this.searchResults == null || this.searchResults.isEmpty())
         {
            return notFound;
         }
         SearchResult doPair = (SearchResult) searchResults.get(rowIndex);

         // doc title column
         if (columnIndex == 0)
         {
            return doPair.getDocumentVersion().getDocumentTitle();
         }

         int adjustedOffset = (doPair.getOffset() - 25);
         adjustedOffset = Math.max(adjustedOffset, 0);
         int adjustedEnd = adjustedOffset + this.searchString.length() + 50;
         adjustedEnd = Math.min(adjustedEnd, doPair.getDocument().getLength());

         try
         {
            String dat = doPair.getDocument().getText(adjustedOffset,
               adjustedEnd - adjustedOffset);
            String tagIn = "<b><i color=\"#007700\">";
            String tagOut = "</i></b>";
            String lcDat = dat.toLowerCase();
            int pos = lcDat.indexOf(searchString.toLowerCase());
            if (pos == -1)
            {
               return ("<html><body><b><i> Error: Document/Offset mismatch </i></b></body></html>");
            }
            return "<html><body>..." + dat.substring(0, pos) + tagIn
               + dat.substring(pos, pos + searchString.length()) + tagOut
               + dat.substring(pos + searchString.length())
               + "...</body></html>";

         }
         catch (BadLocationException ble)
         {
            System.err.println(ble.fillInStackTrace());
         }
      }
      else
      {
         return "<html><body><b><i>Search Results Have Changed: Click \"search\" to Refresh.</i></b></body></html>";
      }
      return notFound;
   }

   public SearchResult getSearchResult(int row)
   {
      return (SearchResult) this.searchResults.get(row);
   }

   public String getSearchString()
   {
      return this.searchString;
   }

   public void updateTableModel(java.util.List results, String string)
   {
      this.searchResults = results;
      this.searchString = string;
      fireTableDataChanged();
      fireTableStructureChanged();
   }

   /**
    * 
    */
   public void reset()
   {
      if (this.searchResults != null)
      {
         this.searchResults.clear();
         fireTableDataChanged();
      }
   }
}