//
// SimpleLogger
// Utility class to perform simple logging
// Author: Lou Foster
// Date  : 10/01/03
//

package edu.virginia.speclab.ivanhoe.shared;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;

public final class SimpleLogger
{
   private static final Logger logger = Logger.getLogger("SimpleLogger");
   private static boolean simpleOutEnabled = false;
   private static boolean logToConsole = false;
   
   public static void initConsoleLogging()
   {  
      ConsoleAppender ca = new ConsoleAppender(
         new PatternLayout("%d{E MMM dd, HH:mm:ss} [%p] - %m\n"));
      BasicConfigurator.configure( ca ); 
      logToConsole = true;
   }
   
   public static void initFileLogging(String fileName) throws IOException
   {
      // purge old log on startup
      File logFile = new File(fileName);
      if (logFile.exists())
      {
         SimpleDateFormat dateFormat = 
            new SimpleDateFormat ("MMddhhmm");
         Date date = new Date();
         String newName = fileName + "-" + dateFormat.format(date);
         logFile.renameTo(new File(newName));
//         logFile.delete();
      }
      
      FileAppender fa = new FileAppender(
         new PatternLayout("%d{E MMM dd, HH:mm:ss} [%p] - %m\n"), fileName);
      BasicConfigurator.configure( fa );
      logToConsole = false;
   }
   
   private SimpleLogger()
   {
   }
   
   public static void logError(String errorMsg, Exception e)
   {
      logger.error(errorMsg, e);
      
      toConsole(errorMsg, System.err, e);
   }

   public static void logError(String errorMsg)
   {
      logger.error(errorMsg);
      
      toConsole(errorMsg, System.err, null);
   }
   
   public static void logInfo(String infoMsg)
   {
      logger.info(infoMsg);
      
      toConsole(infoMsg, System.out, null);
   }
   
   /**
    * Sends error message to a PrintStream, typically System.out or System.err
    * provided that the simpleOutEnabled property is set to TRUE.
    * 
    * @param message text of the error message
    * @param out A PrintStream for output
    * @param e An Exception object for printing the entire stack trace.  If 
    *          null no stack trace is printed.
    */
   protected static void toConsole(String message, PrintStream out, Exception e)
   {
      if (simpleOutEnabled && logToConsole == false)
      {
         out.println(message);
         
         if (e != null)
         {
            e.printStackTrace(out);
         }
      }
   }

   public static void setSimpleConsoleOutputEnabled(boolean b)
   {
      SimpleLogger.simpleOutEnabled = b;
   }
} 
