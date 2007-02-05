/*
 * Created on Jan 9, 2004
 *
 * DocumentInfo
 */
package edu.virginia.speclab.ivanhoe.shared.data;

import java.io.Serializable;
import java.util.Date;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;

/**
 * @author lfoster
 *
 * Encapsulation of document metadata
 */
public class DocumentInfo implements Serializable
{
   private final Integer id;
   private final String fileName;
   private final String displayName;
   private final String author;
   private final String source;
   private final String contributor;
   private final int contributorID;
   private final Date createTime;
   private int documentLength;
   private boolean publishedDocument;
   private boolean startingDocument;
   
   /**
    * Constructor for document info when an ID is not yet known
    * @param fileName
    * @param displayName
    * @param author
    * @param provenance
    * @param publicationDate
    * @param contributor
    * @param createDate
    * @param startDoc
    * @param published
    */
   public DocumentInfo(String fileName, String displayName, String author,
      String provenance, String contributor, int contributorID,
      Date createDate)
   {
      this(0, fileName, displayName, author, provenance, contributor, contributorID, createDate);
   }

   /**
    * Construct a document info object
    * @param id
    * @param fileName
    * @param displayName
    * @param author
    * @param provenance
    * @param contributor
    * @param createDate
    * @param startDoc
    * @param published
    */
   public DocumentInfo(int id, String fileName, String displayName, String author,
      String provenance, String contributor, int contributorID,
      Date createDate, boolean startDoc, boolean published )
   {
      this.id = new Integer(id);
      this.fileName = fileName;
      this.displayName = displayName;
      this.author = author;
      this.source = provenance;
      this.contributor = contributor;
      this.contributorID = contributorID;
      this.startingDocument = startDoc;
      this.createTime = createDate;
      this.publishedDocument = published;
   }
   
   /**
    * Construct a document info object
    * @param id
    * @param fileName
    * @param displayName
    * @param author
    * @param provenance
    * @param publicationDate
    * @param contributor
    * @param createDate
    */
   public DocumentInfo(int id, String fileName, String displayName, String author,
      String provenance, String contributor, int contributorID,
      Date createDate )
   {
      this(id, fileName, displayName, author, provenance, 
         contributor, contributorID, createDate, false, false);
   }
   
   public static String createTitleFromFileName( String fileName )
   {
       // is this creation of a new file?
       String defaultName = "";
       if (fileName != null)
       {
          // make the default doc title the same as the file name minus ext
          defaultName = fileName;
          int pos = fileName.indexOf('.');
          if (pos > -1 )
          {
             defaultName = fileName.substring(0,pos);
          }
       }
       
       return defaultName;
  }
   
   /**
    * @return Returns the filename for the document
    */
   public static String createFileNameFromTitle( String title )
   {      
       return title.replaceAll("[[^a-z]&&[^A-Z]&&[^0-9]]","") + ".html";
   }

   
   /**
    * Copy constructor
    * @param src
    */
   public DocumentInfo(DocumentInfo src)
   {
      this(src.getId().intValue(), src.getFileName(), src.getTitle(), src.getAuthor(),
         src.getSource(), src.getContributor(), src.getContributorID(), 
         src.getCreateTime(), src.isStartingDocument(), src.isPublishedDocument());
   }
   
   public Integer getId()
   {
      return this.id;
   }
   
   public String getAuthor()
   {
      return author;
   }

   public String getTitle()
   {
      return displayName;
   }

   public String getFileName()
   {
      return fileName;
   }

   public String getSource()
   {
      return source;
   }

   public String toString()
   {
      return getTitle();
   }
   
   public String getContributor()
   {
      return this.contributor;
   }

   /**
    * @return Returns the documentLength.
    */
   public int getDocumentLength()
   {
       return documentLength;
   }
   
   /**
    * @param documentLength The documentLength to set.
    */
   public void setDocumentLength(int documentLength)
   {
       this.documentLength = documentLength;
   }
   
	/**
	 * @return Returns the createTime.
	 */
	public Date getCreateTime()
	{
	    if( createTime == null )
	    {
	        SimpleLogger.logError("Attempted to access uninitialized create time");
	        return null;
	    }
	    
	    return createTime;
	}

   /**
    * @return Returns the startingDocument.
    */
   public boolean isStartingDocument()
   {
       return startingDocument;
   }
   
   /**
    * @return Returns the publishedDocument.
    */
   public boolean isPublishedDocument()
   {
       return publishedDocument;
   }
   
   /**
    * @param publishedDocument The publishedDocument to set.
    */
   public void setPublishedDocument(boolean publishedDocument)
   {
       this.publishedDocument = publishedDocument;
   }
   
   public void setStartingDocument(boolean starting)
   {
      this.startingDocument = starting;
   }
/**
 * @return Returns the contributorID.
 */
public int getContributorID()
{
    return contributorID;
}
}
