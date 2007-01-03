/*
 * Created on Feb 22, 2005
 */
package edu.virginia.speclab.ivanhoe.shared;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author dgran
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestSimpleLogger extends TestCase
{
   private class MockPrintStream extends PrintStream
   {
      public String exceptionText = null;

      public MockPrintStream(OutputStream arg0)
      {
         super(arg0);
      }
      
      /**
       * Capture calls to e.printStackTrace(out) to a String
       */
      public void println(String x)
      {
         exceptionText += x + "\n";
      }
      
   }
   
   public TestSimpleLogger(String name)
   {
      super(name);
   }
   
   /**
    * Create an exception, pass to SimpleLogger and confirm that it sends the exception
    * to an OutputStream.  We use the MockPrintStream to confirm that the exception
    * is passed along.
    */
   public void testToConsole()
   {
      // initialize with an OutputStream that goes no where
      MockPrintStream mock = new MockPrintStream(new ByteArrayOutputStream());
            
      try
      {
         // create an excception
         Object a = null;
         a.toString();
      }
      catch (Exception e)
      {
         // pass along exception to the SimpleLogger
         SimpleLogger.setSimpleConsoleOutputEnabled(true);
         SimpleLogger.toConsole("sample error message", mock, e);
         
         // the property mock.exceptionText should be populated with a stack trace, not null
         assertNotNull("text of exception should not be null", mock.exceptionText);
         
         // a sentinel term in the stack trace is "Test" from our test case
         assertTrue("", mock.exceptionText.indexOf("Test") != -1);
      }
   }

   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(new TestSuite(TestSimpleLogger.class));
   }
}
