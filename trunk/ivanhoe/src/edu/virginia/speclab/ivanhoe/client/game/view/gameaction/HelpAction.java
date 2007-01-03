/*
 * Created on Mar 15, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.gameaction;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.jnlp.*;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;

/**
 * @author Nathan Piazza
 */
public class HelpAction extends AbstractAction 
{
   /**
    * Constructor for use in GUI context, where there is an icon and a tooltip
    * @param text Text for the tooltip
    * @param icon Icon for the action
    */
   public HelpAction(String text, Icon icon)
   {
      super(text,icon);
      putValue(Action.SHORT_DESCRIPTION, text);
   }
	
   /**
    * Basic constructor for non GUI use
    */
   public HelpAction()
   {
   }

   public void actionPerformed(ActionEvent e)
	{
       BasicService bs;
       URL helpUrl = null;
      try
      {
         bs = (BasicService) ServiceManager.lookup( "javax.jnlp.BasicService" );
         helpUrl = new URL("http://www.patacriticism.org/ivanhoe/help/");
         if (bs.showDocument(helpUrl));
         {
          
         }
      } 
      catch (UnavailableServiceException e1)
      {
         SimpleLogger.logError("Connection problem trying to open browser to help page");
      } 
      catch (MalformedURLException e2)
      {
         SimpleLogger.logError("Problem with URL for help action: " + helpUrl);
      }      
	}

	/**
	 * Loads the help files for the application.  This makes use of
	 * a classloader trick to pull the resource out of resources.jar.
	 * This hack is employed so that everything works when deployed
	 * with Java Web Start.  For all the gory details, see:
	 * 
	 * http://rachel.sourceforge.net
	 */
   
   /*
    * Commented out for the time being
    * 
	private void setupHelp()
	{
	   String helpHS = "res/help/IvanhoeHelp.hs";

	   try 
	   {                 
		  URL hsURL = ResourceHelper.instance.getUrl(helpHS);         
		  hs = new HelpSet(null, hsURL);    
	   }
	   catch (Exception ee) 
	   {       
		  // Say what the exception really is       
		  SimpleLogger.logError( "HelpSet1 " + ee.getMessage());       
		  SimpleLogger.logError("HelpSet2 "+ helpHS +" not found");          
	   }
      
	   hb = hs.createHelpBroker(); 
	   hb.setDisplayed(true);
	}
   */


}
