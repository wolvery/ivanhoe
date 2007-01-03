//
// Ivanhoe
// Main for Ivanhoe game client
// Author: Lou Foster
// Date  : 10/01/03 
//
package edu.virginia.speclab.ivanhoe.client;

import javax.swing.*;
import javax.swing.border.*;

import net.roydesign.mac.MRJAdapter;

import edu.virginia.speclab.ivanhoe.client.game.view.IvanhoeFrame;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.network.ClientProxy;
import edu.virginia.speclab.ivanhoe.client.network.GameProxy;
import edu.virginia.speclab.ivanhoe.client.network.LobbyProxy;
import edu.virginia.speclab.ivanhoe.client.pregame.LoginDialog;
import edu.virginia.speclab.ivanhoe.client.pregame.LoginTransaction;
import edu.virginia.speclab.ivanhoe.client.pregame.LoginTransactionListener;
import edu.virginia.speclab.ivanhoe.client.util.PropertiesManager;
import edu.virginia.speclab.ivanhoe.client.util.ServerSynchronizedClock;
import edu.virginia.speclab.ivanhoe.shared.Encryption;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.Semaphore;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;
import edu.virginia.speclab.ivanhoe.shared.message.ClientErrorMsg;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class Ivanhoe 
{
   private PropertiesManager propertiesManager;
      
   // data from properties file
   private static String systemBrowser;
   private String        rootDirectory;
   private String        gameHost;
   private int           gamePort;
   private static ClientProxy   proxy;
   
   private static ServerSynchronizedClock ivanhoeClock;
   
   
   /**
    * Construct Ivanhoe game. Loads properties, creates base work dirs
    * and present user with login dialog
    */
   private Ivanhoe()
   {
       initLogging();
       
       // init look and feel stuff
       if (System.getProperty("mrj.version") == null)
       {
          installLookNFeel();
       }
       else
       {
          SimpleLogger.logInfo("Installing apple quit handler");
          MRJAdapter.addQuitApplicationListener(new ActionListener()
             {       
                public void actionPerformed(ActionEvent e)
                {       
                   SimpleLogger.logInfo("Handling apple quit action");
                   if( Workspace.instance != null )
                   {
                       Workspace.instance.exitGame(true);
                   }
                   else
                   {
                       Ivanhoe.shutdown();
                   }
                }
             });
       }
       
       SimpleLogger.logInfo("Adjusting UI properties");
       UIManager.put("InternalFrame.titleFont", 
          IvanhoeUIConstants.SMALL_FONT );
       UIManager.put("InternalFrame.border", 
          LineBorder.createGrayLineBorder() );
  
       // customize settings
       ToolTipManager.sharedInstance().setInitialDelay(100);
       ToolTipManager.sharedInstance().setDismissDelay(60*1000);
       ToolTipManager.sharedInstance().setReshowDelay(100);       
       
   }
   
   public static void initLogging()
   {
	   initLogging(false);
   }
   
   public static void initLogging( boolean console )
   {
	   if( console )
	   {
		   SimpleLogger.setSimpleConsoleOutputEnabled(true);
		   SimpleLogger.initConsoleLogging();
	   }
	   else
	   {
	       try
	       {
	          // turn on file logging, but also show simple console
	          SimpleLogger.initFileLogging("IvanhoeClient.log");
	          SimpleLogger.setSimpleConsoleOutputEnabled(true);
	       }
	       catch (IOException e)
	       {
	          System.err.println("Unable to init file logging, use console");
	          SimpleLogger.initConsoleLogging();
	       }       
	   }
	   
   }
   
   public static void installLookNFeel()
   {
       SimpleLogger.logInfo("Installing custom look and feel");
       try
       {
          UIManager.setLookAndFeel(
             new com.incors.plaf.kunststoff.KunststoffLookAndFeel());
          
       }
       catch (UnsupportedLookAndFeelException e)
       {
       }
   }
   
   private void startApplication( String userName, String password )
   {
       // init game properties from file
       if (loadProperties() == false)
       {
          SimpleLogger.logError("Invalid or incomplete properties data. Exiting");
          shutdown();
       }
       
       AutoLogin autoLog = new AutoLogin(userName,password);
       
       if( autoLog.isSuccessful() )
       {
           login(autoLog.getGameInfo());
       }
       else
       {
           shutdown();
       }
   }
   
   private void login( GameInfo gameInfo )
   {
       // create root work dir
       if (createDirectory(this.rootDirectory) )
       {
           // encapsulates game execution so it can be run on the swing thread
           SwingUtilities.invokeLater( new GameRunner(gameInfo) );
       }
       else
       {
          SimpleLogger.logError("Unable to create directory " + this.rootDirectory);
          JOptionPane.showMessageDialog(null,
             "Unable to create local working directories! Game will now exit.", 
             "Startup Error", JOptionPane.ERROR_MESSAGE);
          shutdown();
       }
   }
   
   /**
    * Create a directory at the spacified path
    * @param directoryName
    * @return
    */
   public static boolean createDirectory(String directoryName)
   {
      SimpleLogger.logInfo("Creating directory [" + directoryName + "]...");
      File dir = new File(directoryName);
      if (dir.exists() == false)
      {
         if (dir.mkdirs() == true)
         {
            SimpleLogger.logInfo("Created directory");
            return true;
         }
      }
      else
      {
         SimpleLogger.logInfo("Directory exists");
         return true;
      }
      
      SimpleLogger.logInfo("Unable to create directory");
      return false;
   }
   
   /**
    * Immediately halts execution. Be sure to save data first!    
    * 
    **/
   public static void shutdown()
   {
       // close connection to server
       if (proxy != null)
       {
           proxy.close();
       }
       
       SimpleLogger.logInfo("Terminating application.");
       System.exit(0);
   }
   
 
   
   /**
    * Initialize client data from properties file
    * @return
    */
   private boolean loadProperties()
   {
      propertiesManager = new PropertiesManager();

      Ivanhoe.systemBrowser = propertiesManager.getProperty("browser");
      this.gameHost = propertiesManager.getProperty("host");
      this.gamePort = Integer.parseInt(propertiesManager.getProperty("port"));
      this.rootDirectory = System.getProperty("user.home") + File.separator + propertiesManager.getProperty("workingDir");
      
      // check for bad properties data
      if( Ivanhoe.systemBrowser == null || 
          this.gameHost == null ||
          this.gamePort <= 0    ||
          this.rootDirectory == null   )
      {
          return false;
      }
      
      return true;
   }
   
   /**
    * Static access to configured browser
    * @return
    */
   public static String getBrowser()
   {
      return Ivanhoe.systemBrowser;
   }
   
   /**
    * @return Returns the corrected date based on the server start time stamp
    */
   public static Date getDate()
   {
      return Ivanhoe.ivanhoeClock.getDate();
   }
      
   /**
    * Show an error dialog with the specified title & message
    * @param title
    * @param msg
    */
   public static void showErrorMessage(String title, String msg)
   {
      JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
   }
   
   public static void sendErrorMessageToHost( String message )
   {
       ClientErrorMsg msg = new ClientErrorMsg(message);
       Ivanhoe.getProxy().sendMessage(msg);
   }
   
   public static void sendExceptionToHost( Exception e )
   {
       String stackTrace = e.toString() + ":\n";
       StackTraceElement[] trace = e.getStackTrace();
       for( int i=0; i<trace.length; i++ )
       {
           stackTrace += trace[i] + "\n";
       }
       
       sendErrorMessageToHost( stackTrace );       
   }


   /**
    * Show an error dialog with the specified message with title "Error"
    * @param msg
    */
   public static void showErrorMessage(String msg)
   {
      JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
   }

   /**
    * Convert Graphics to Graphics2D and setup standard rendering hints
    * @param g
    * @return Graphics2D
    */
   public static Graphics2D getGraphics2D(Graphics g)
   {
      Graphics2D g2d = (Graphics2D)g;
      g2d.setRenderingHint(
         RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
      g2d.setRenderingHint(
         RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
      return g2d;
   }
   
   public static Color addAlphaToColor( Color opaque, int alpha )
   {        
       return new Color(opaque.getRed(),opaque.getGreen(),opaque.getBlue(),alpha);       
   }
     
   /**
    * MAIN for Ivanhoe Game Client.
    * Init logging, setup UI statics and create app
    * @param args
    */
   public static void main(String[] args)
   {
      // create an instance of the application
      Ivanhoe ivanhoe = new Ivanhoe();
      
      // start the application
      if( args.length == 2 ) {
          ivanhoe.startApplication(args[0],args[1]);    	  
      }
   }
  
	/**
	 * @return Returns the propertiesManager.
	 */
	public PropertiesManager getPropertiesManager()
	{
	    return propertiesManager;
	}
	
	/**
	 * @return Returns the rootDirectory.
	 */
	public String getRootDirectory()
	{
	    return rootDirectory;
	}
	
	/**
	 * @return Returns the lobbyHost.
	 */
	public String getLobbyHost()
	{
	    return gameHost;
	}
	/**
	 * @return Returns the lobbyPort.
	 */
	public int getLobbyPort()
	{
	    return gamePort;
	}
    
    public static ClientProxy getProxy()
    {
        return proxy;
    }
    
    public static void setProxy(ClientProxy newProxy)
    {
        if (newProxy == null)
        {
            throw new IllegalArgumentException("newProxy should not be null");
        }
        
        ClientProxy oldProxy = proxy;
        proxy = newProxy;
        
        if (oldProxy != null)
        {
            oldProxy.close();
        }
    }
    
    public static void registerGameMsgHandler(MessageType msgType, IMessageHandler handler)
    {
        if (proxy == null)
        {
            throw new RuntimeException("Cannot register game message handler when there is no proxy");
        }
        
        if (msgType == null || handler == null)
        {
            throw new IllegalArgumentException("Cannot register game message handler with null argument");
        }
        
        if (proxy instanceof GameProxy)
        {
            proxy.registerMsgHandler(msgType, handler);
        }
        //TODO remove lobby proxy ref
        else if (proxy instanceof LobbyProxy)
        {
            SimpleLogger.logInfo("Deferring message handling registration until GameProxy is the default");
            ((LobbyProxy)proxy).registerGameMsgHandler(msgType, handler);
        }
        else
        {
            throw new RuntimeException("Trying to register game message handler with proxy of type " 
                    + proxy.getClass().getName());
        }
    }
    
    /**
     * @param ivanhoeClock The ivanhoeClock to set.
     */
    public static void setIvanhoeClock(ServerSynchronizedClock ivanhoeClock)
    {
        Ivanhoe.ivanhoeClock = ivanhoeClock;
    }
    
    private class GameRunner implements Runnable
    {
    	private GameInfo gameInfo;

		public GameRunner( GameInfo gameInfo )
    	{
    		this.gameInfo = gameInfo;
    	}
    	
        public void run()
        {
            new IvanhoeFrame(Ivanhoe.this,gameInfo);
        }
   };
    
    private class AutoLogin implements LoginTransactionListener 
    {
    	private LoginTransaction loginTransaction; 
        private boolean success;
        private Semaphore semaphore;
        
        public AutoLogin( String username, String clearPassword )
        {
            success = false;
            semaphore = new Semaphore();
            
            loginTransaction = new LoginTransaction(gameHost,gamePort);
            loginTransaction.setListener(this);
            
            String userName = username;
            String encryptedPassword = Encryption.createMD5HashCode(clearPassword);            
            loginTransaction.login(userName,encryptedPassword);            

            // wait here till semaphore is raised by callback
            semaphore.waitHere();
        }
        
        public GameInfo getGameInfo()
        {
        	return loginTransaction.getGameInfo();
        }

        public boolean isSuccessful()
        {
            return success;
        }

        public void loginSuccessful()
        {
            success = true;
            semaphore.proceed();
        }

        public void failedConnection()
        {
            success = false;
            semaphore.proceed();
        }

        public void failedAuthorization()
        {
            success = false;   
            semaphore.proceed();
        }
    }
}
