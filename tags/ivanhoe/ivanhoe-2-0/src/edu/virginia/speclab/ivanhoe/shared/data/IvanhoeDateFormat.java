/*
 * Created on Nov 10, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.shared.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;

/**
 * @author Nick
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IvanhoeDateFormat
{
    // standard format for Ivanhoe date display
    private static final SimpleDateFormat dateFormat =
       new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    
    /**
     * @param strDate
     * @return Returns a Date parsed from the string
     */
    public static Date parseDate(String strDate)
    {
       try
       {
          return dateFormat.parse(strDate);
       }
       catch (ParseException e)
       {
          try
          {
             return DateFormat.getDateInstance().parse(strDate);
          }
          catch (ParseException e1)
          {
             SimpleLogger.logError("Bad date string: " + strDate);
          }
       }
       
       return null;
//       return new Date();
    }
    
    /**
     * Return a date in the ivanhoe standard string format
     * @param date
     * @return
     */
    public static String format(Date date)
    {
       return dateFormat.format(date);
    }

}
