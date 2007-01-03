/*
 * Created on Dec 11, 2003
 */
package edu.virginia.speclab.ivanhoe.client.game.model.document;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTML.Tag;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.shared.GuidGenerator;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.ActionType;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.Link;
import edu.virginia.speclab.ivanhoe.shared.data.LinkTag;
import edu.virginia.speclab.ivanhoe.shared.data.LinkType;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Duane Gran (dmg2n@virginia.edu)
 *
 * Tests for IvanhoeDocument class
 */
public class TestIvanhoeDocument extends TestCase
{   
   /**
    * Mock class to hold reveal internal workings of a class that might implement
    * the IDocumentActionListener interface.
    */
   private class MockDocumentAction implements IDocumentActionListener
   {
      public String addedId = null;
      public String deletedId = null;
      
      public void actionAdded(DocumentVersion version, String actionId, Tag tag)
      {
         this.addedId = actionId;
      }
      
      public void actionDeleted(String actionId)
      {
         this.deletedId = actionId;
      }
   }

   public TestIvanhoeDocument(String name)
   {
      super(name);
   }

   public void testCreateLink() throws IOException, BadLocationException
   {
       DiscourseField discourseField = new DiscourseField(null);
       String docStr = "<html><body>blah blah</body></html>";
       IvanhoeEditorKit kit = new IvanhoeEditorKit(discourseField);
       IvanhoeDocument doc = kit.createDocumentFromString(docStr);
       doc.setDocumentInfo( createTestDocInfo("testCreateLink"));
       
       String linkId = GuidGenerator.generateID();
       Link testLink = new Link(linkId, LinkType.COMMENT,
               new LinkTag("test comment"), "test comment label");
       
       doc.createLink(1, 5, testLink);
       
       assertNull(
               "There should not be a link returned for a random GUID",
               doc.getLinkManager().getLink(GuidGenerator.generateID()));
       assertTrue(
               "Link requested via actionId should be equal to link entered",
               testLink.equals(doc.getLinkManager().getLink(linkId)));
       assertTrue(
               "The action content should be equal to \"blah\"",
               "blah".equals(doc.getActionContent(linkId, ActionType.LINK_ACTION)));
   }
   
   public void testInsertImage() throws IOException, BadLocationException
   {
       String docStr = "<html> <head></head> <body></body> </html>";
       IvanhoeEditorKit kit = new IvanhoeEditorKit(new DiscourseField(null));
       IvanhoeDocument doc = kit.createDocumentFromString(docStr);
       doc.setDocumentInfo( createTestDocInfo("testInsertImage") );
       
       String guid0 = GuidGenerator.generateID();
       String guid1 = GuidGenerator.generateID();
       String guid2 = GuidGenerator.generateID();
       
       doc.insertImage(guid0, 0, "dummyImage0.jpg", true);
       doc.insertImage(guid1, 1, "dummyImage1.jpg", true);
       doc.insertImage(guid2, 3, "dummyImage2.jpg", true);

       boolean image0RunElement = doc.getElement(guid0) instanceof HTMLDocument.RunElement;
       boolean image1RunElement = doc.getElement(guid1) instanceof HTMLDocument.RunElement;
       boolean image2RunElement = doc.getElement(guid2) instanceof HTMLDocument.RunElement;
       
       boolean image0Removed = doc.removeTag(IvanhoeTag.ADD, guid0);
       boolean image1Removed = doc.removeTag(IvanhoeTag.ADD, guid1);
       boolean image2Removed = doc.removeTag(IvanhoeTag.ADD, guid2);
       
       assertTrue(
               "Image elements should be of type HTMLDocument.RunElement",
               image1RunElement 
               && (!image0Removed || image0RunElement)
               && (!image2Removed || image2RunElement)
               );
       assertFalse(
               "Image at offset 0 should not have been added to file",
               image0Removed);
       assertTrue(
               "Image at offset 1 should have been inserted with proper guid",
               image1Removed);
       assertFalse(
               "Image at offset 3 should not have been added to file",
               image2Removed);
   }
   
   public void testDeleteText() throws IOException, BadLocationException
   {
       String docStr = "<html><body>blah blah</body></html>";
       IvanhoeEditorKit kit = new IvanhoeEditorKit(new DiscourseField(null));
       IvanhoeDocument doc = kit.createDocumentFromString(docStr);
       doc.setDocumentInfo( createTestDocInfo("testDeleteText") );
       
       boolean earlyDeletion = doc.deleteText(-4, -1);
       boolean lateDeletion = doc.deleteText(64, 128);
       boolean validDeletion = doc.deleteText(1,6);
       
       assertFalse(
               "Deleting before the beginning of the text should be invalid",
               earlyDeletion);
       assertFalse(
               "Deleting after the end of the text should be invalid",
               lateDeletion);
       assertTrue(
               "Deleting characters 1-6 should be valid",
               validDeletion);
       
   }
   
   public void testHasCurrentTag() throws IOException, BadLocationException
   {
      String docStr =
         "<html><body><add id=\"a\" new=\"true\">blah blah</add></body></html>";
      IvanhoeEditorKit kit = new IvanhoeEditorKit(new DiscourseField(null));
      IvanhoeDocument doc = kit.createDocumentFromString(docStr);
      doc.setDocumentInfo( createTestDocInfo("Test"));
      
      Element rootElem = doc.getCharacterElement(1);

      assertTrue(
         "The ADD tag should be in the sample document",
         doc.hasCurrentTag(rootElem, IvanhoeTag.ADD));
   }
   
   private DocumentInfo createTestDocInfo(String title)
   {
      return new DocumentInfo(title+".html", title, "Unknown", "Unknown", "System", 1, new Date());
   }

   /**
    * Confirm behavoir of search method, which returns an index into the content
    * (not counting the tags).
    */
   public void testSearch() throws IOException, BadLocationException
   {
      String docStr =
         "<html><body><add id=\"a\" new=\"true\">first second third</add></body></html>";
      IvanhoeEditorKit kit = new IvanhoeEditorKit(new DiscourseField(null));
      IvanhoeDocument doc = kit.createDocumentFromString(docStr);
      doc.setDocumentInfo( createTestDocInfo("Test"));

      // search on element content should fail
      int pos = doc.search(0, "body", true);
      assertEquals("search should search content, not tags", -1, pos);

      // search on content of add tag should suceed
      pos = doc.search(0, "second", true);
      assertEquals("position for found content should be 7", 7, pos);

      // search on bugus content should fail
      pos = doc.search(0, "not_there", true);
      assertEquals("position for unfound content should be -1", -1, pos);
   }

   /**
    * Confirms behavior of getSpannedElements method, which populates a Vector of
    * Elements based on a span of text.
    */
   public void testSpannedElements() throws IOException, BadLocationException
   {
      String docStr = "<p>one<b>two</b>three</p>";
      IvanhoeEditorKit kit = new IvanhoeEditorKit(new DiscourseField(null));
      IvanhoeDocument doc = kit.createDocumentFromString(docStr);
      doc.setDocumentInfo( createTestDocInfo("Test"));
      
      // count from within first p tag
      Collection elements = doc.getSpannedElements(1, doc.getLength());
      assertEquals(
         "Count from within first p tag should be 3",
         3,
         elements.size());

      // start from first index
      elements = doc.getSpannedElements(0, doc.getLength());
      assertEquals(
         "Count of first through n-1 chars should be 4",
         4,
         elements.size());

      // pass a bogus start index
      elements = doc.getSpannedElements(-1, doc.getLength());
      assertEquals(
         "Count with bogus start index should still be correct",
         4,
         elements.size());

      // pass a bogus end index
      elements = doc.getSpannedElements(0, doc.getLength() + 1);
      assertEquals("Count of all elements should be 4", 5, elements.size());

      // pass a bogus end index
      elements = doc.getSpannedElements(0, doc.getLength() + 2);
      assertEquals(
         "Count with bogus end index should still be correct",
         5,
         elements.size());
   }

   /**
    * The getElementByID method won't find IvanhoeTag elements and it won't
    * find elements that normally don't have an ID (like B).
    */
   public void testElementByID() throws IOException, BadLocationException
   {
      String docStr =
         "<p id='a'><add id='c'>one</add><p id='b'>two</p><b id='d'>three</b></p>";
      IvanhoeEditorKit kit = new IvanhoeEditorKit(new DiscourseField(null));
      IvanhoeDocument doc = kit.createDocumentFromString(docStr);
      doc.setDocumentInfo( createTestDocInfo("Test"));
      
      Element p1 = doc.getElement("a");
      Element p2 = doc.getElement("b");
      Element z = doc.getElement("z");
      Element add = doc.getElement("c");
      Element bold = doc.getElement("d");

      // standard tests
      assertNotNull("P element of id 'a' should be found", p1);
      assertNotNull("Nested P element of id 'b' should be found", p2);
      assertNull("Non-existent id should yield null Element", z);
      assertNotNull("IvnahoeTag.ADD should be found in document", add);
      assertNotNull("Bold tag should be found in document", bold);
   }

   /**
    * Corrolary test for ElementByID that makes sure that the tag id
    * returned is in line with expectations.
    */
   public void testGetActionID() throws IOException, BadLocationException
   {
      String docStr =
         "<html><body><p id='a'>one" +
         "<ADD id='b'>two</ADD>three</p></body></html>";
         
      IvanhoeEditorKit kit = new IvanhoeEditorKit(new DiscourseField(null));
      IvanhoeDocument doc = kit.createDocumentFromString(docStr);
      doc.setDocumentInfo( createTestDocInfo("Test"));
      
      Element add = doc.getElement("b");
      assertEquals("Action id should be 'b'", "b", 
         doc.getTagId(IvanhoeTag.ADD,add));
         
      // bad request
      assertEquals(
         "ID attribute should be empty on unfound action",
         "",
         doc.getTagId(IvanhoeTag.ILINK,add));
   }

   /**
    * Confirms that removing an action tag works as expected.
    */
   public void testRemoveAction()
   {
      // TODO remiplement testcase
//      String docStr =
//         "<p>one<ilink id='a' targets='x1' labels='test'>two</ilink>three"+
//            "<ilink id='x1' targets='a' labels='test2'>Dest</ilink></p>";
//      IvanhoeEditorKit kit = new IvanhoeEditorKit();
//      IvanhoeDocument doc = kit.createDocumentFromString(docStr);
//      doc.setDocumentInfo( createTestDocInfo("Test"));
//      
//      
//      // fundamental check for correctness
//      assertTrue("Removing tag should return 'true'", 
//         doc.removeActionTag("a",IvanhoeTag.ILINK));
//
//      // check for phrase 'onetwothree' in output, denoting no ilink tag
//      assertTrue(
//         "Result should have ilink tag stripped",
//         documentToString(kit, doc).indexOf("onetwothree") != -1);
   }

   /**
    * Confirms that the checkForCurrentDeleteTag correctly produces a
    * SimpleAttributeSet with the correct attributes.  It isn't possible to
    * test Ivanhoe specific attributes, like 'new'.
    */
   public void testCheckForCurrentDeleteTag()
      throws IOException, BadLocationException
   {
      String docStr =
         "<p><delete id='z'>one</delete>"
            + "<delete id='b' new='true'>two</delete>"
            + "<delete id='c' new='true'>three</delete></p>";
      IvanhoeEditorKit kit = new IvanhoeEditorKit(new DiscourseField(null));
      IvanhoeDocument doc = kit.createDocumentFromString(docStr);
      doc.setDocumentInfo( createTestDocInfo("Test"));
      
      Collection spannedElements = doc.getSpannedElements(0, doc.getLength() + 1);

      // initial sanity check on number of elements
      assertEquals(
         "Vector of elements should have 5 items",
         5,
         spannedElements.size());

      SimpleAttributeSet sas = doc.checkForCurrentDeleteTag(spannedElements);

      // first delete -- lacks a 'new' attribute
      assertFalse(
         "Should not find delete tags without 'new' attribute",
         sas.containsAttribute(HTML.Attribute.ID, "a"));

      // second delete -- confirm id and value are stored
      assertTrue(
         "ID attribute should be defined",
         sas.isDefined(HTML.Attribute.ID));
      assertTrue(
         "ID value should be 'b'",
         sas.containsAttribute(HTML.Attribute.ID, "b"));

      // third delete -- Should not be found
      assertFalse(
         "Method should only return first delete tag",
         sas.containsAttribute(HTML.Attribute.ID, "c"));
   }
   
   /**
    * The markDeletedRange method in IvanhoeDocument handles a large range of
    * cases where text is deleted from the document.
    */
   public void testNoInfo()
      throws IOException, BadLocationException
   {
      String docStr =
         "<html><body><p id='a'>sample</p></body></html>";
      IvanhoeEditorKit kit = new IvanhoeEditorKit(new DiscourseField(null));
      IvanhoeDocument doc = kit.createDocumentFromString(docStr);
      assertTrue("Title should be Undefined", doc.getTitle().equals("Undefined"));
   }
   
   /**
    * The markDeletedRange method in IvanhoeDocument handles a large range of
    * cases where text is deleted from the document.
    */
   public void testMarkDeleteRange()
      throws IOException, BadLocationException
   {
      String docStr =
         "<body>before<p id='a'>onetwothree</p>after</body>";
      IvanhoeEditorKit kit = new IvanhoeEditorKit(new DiscourseField(null));
      IvanhoeDocument doc = kit.createDocumentFromString(docStr);
      doc.setDocumentInfo( createTestDocInfo("Test"));
      
      doc.markDeletedRange("b", 3, 11, false);

      // delete should be split when overlapping existing P tag
      assertTrue("delete should be split when overlapping existing P tag", 
        documentToString(kit, doc).indexOf("id=\"b\">fore") != -1);
      assertTrue("delete should be split when overlapping existing P tag", 
        documentToString(kit, doc).indexOf("id=\"b\">one</delete") != -1);
      
      // delete should be split three times for full range
      doc = kit.createDocumentFromString(docStr);
      doc.setDocumentInfo( createTestDocInfo("Test"));
      doc.markDeletedRange("b", 1, 25, false);
      assertTrue("delete should be split three times for full range", 
        documentToString(kit, doc).indexOf("id=\"b\">before") != -1);
      assertTrue("delete should be split three times for full range", 
        documentToString(kit, doc).indexOf("id=\"b\">onetwothree") != -1);
      assertTrue("delete should be split three times for full range", 
        documentToString(kit, doc).indexOf("id=\"b\">after</delete") != -1);
      
      // new encompassing delete should span
      docStr =
         "<body>before<delete id='a'>onetwothree</delete>after</body>";
      doc = kit.createDocumentFromString(docStr);
      doc.setDocumentInfo( createTestDocInfo("Test"));
      doc.markDeletedRange("b", 1, 23, true);
      
      assertTrue("new encompassing delete should span", 
        documentToString(kit, doc).indexOf(
          "<delete id=\"b\" new=\"true\">before</delete><delete id=\"a\">onetwothree</delete><delete id=\"b\"") != -1);
      
      // deleting over a new delete should promote inner tag id
      docStr =
         "<body>before<delete id='a' new='true'>onetwothree</delete>after</body>";
      doc = kit.createDocumentFromString(docStr);
      doc.setDocumentInfo( createTestDocInfo("Test"));
      MockDocumentAction mda = new MockDocumentAction();
      doc.addActionListener(mda);
      doc.markDeletedRange("b", 1, 23, true);
      assertTrue("deleting over a new delete should promote inner tag id", 
          documentToString(kit,doc).indexOf(
          "<delete id=\"a\" new=\"true\">beforeonetwothreeafter") != -1);
      
      // No actions should be fired because a tag was just changed
      assertNull("actionAdded event should not be fired", mda.addedId);
      assertNull("actionDeleted event should not be fired", mda.deletedId);
        
      // old additions should be retained by surrounding delete tag  
      docStr =
         "<body>before<add id='a'>onetwothree</add>after</body>";
      doc = kit.createDocumentFromString(docStr);
      doc.setDocumentInfo( createTestDocInfo("Test"));
      mda = new MockDocumentAction();
      doc.addActionListener(mda);
      doc.markDeletedRange("b", 1, 23, false);
      assertTrue("old additions should be retained by surrounding delete tag",
        documentToString(kit, doc).indexOf(
          "<delete id=\"b\">before<add id=\"a\">onetwothree</add>after") != -1);
      
      // confirm that correct actions were
      assertNotNull("actionAdded event should be fired", mda.addedId);
      assertNull("actionDeleted event should not be fired", mda.deletedId);
   }

   /**
    * Utility method to convert state of the document into a String
    */
   private String documentToString(IvanhoeEditorKit kit, IvanhoeDocument doc)
   {
      StringWriter sw = new StringWriter();
      try
      {
         kit.write(sw, doc, 0, doc.getLength());
      }
      catch (Exception e)
      {
         System.err.println("Couldn't dump HTML: " + e);
      }

      return sw.toString();
   }

   public static void main(String[] args)
   {
      SimpleLogger.initConsoleLogging();
      junit.textui.TestRunner.run(new TestSuite(TestIvanhoeDocument.class));
   }
}
