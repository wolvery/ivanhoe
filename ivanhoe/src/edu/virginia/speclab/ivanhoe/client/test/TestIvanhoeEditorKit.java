/*
 * Created on Dec 12, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.virginia.speclab.ivanhoe.client.test;

import java.io.IOException;

import javax.swing.text.BadLocationException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Duane Gran (dmg2n@virginia.edu)
 *
 * Tests for IvanhoeEditorKit class
 */
public class TestIvanhoeEditorKit extends TestCase
{
   public TestIvanhoeEditorKit(String name)
   {
      super(name);
   }
   
   /**
    * Affirm that the editor kit can create a document from a String
    */
   public void testCreateDocumentFromString()
      throws IOException, BadLocationException
   {

   }

   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(new TestSuite(TestIvanhoeEditorKit.class));
   }
}
