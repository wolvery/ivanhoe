/*
 * Created on Jan 21, 2004
 *
 * Admin
 */
package edu.virginia.speclab.ivanhoe.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Properties;

import javax.swing.WindowConstants;

import edu.virginia.speclab.ivanhoe.client.pregame.NewAccountDialog;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;


/**
 * @author lfoster
 *
 * The admin module for remotely monitoring & controlling
 * an Ivanhoe game server
 */
public class Admin implements Runnable
{
   private String ipAddr;
   private int port;
   private Socket serverSocket;
   private PrintWriter  commandWriter;
   private BufferedReader inputReader;
   private Thread runner;
   private final boolean useGui;
   private boolean quit;
   
   private static final String PROPERTIES_FILE = System.getProperty("IVANHOE_DIR",".")+"/ivanhoe.properties";
   
   private final static int GAME_PORT_MOD = 1;
   
   public static void main(String[] args)
   {
      Properties props = loadIvanhoeProperties();
      boolean gui = false;
      InputStream input = System.in;
      char serverTypeChar = '\0';
      String ipAddress = props.getProperty("lobbyHost", "");
      int port = Integer.parseInt(props.getProperty("lobbyPort", "0"));
      
      SimpleLogger.initConsoleLogging();
      
      for (int i=0; i<args.length; ++i)
      {
          if (args[i].equals("-gui"))
          {
              gui = true;
          }
          else if (args[i].equals("-ip"))
          {
              ipAddress = args[++i];
          }
          else if (args[i].equals("-port"))
          {
              try
              {
                  port = Integer.parseInt(args[++i]);
              }
              catch (NumberFormatException nfe)
              {
                  SimpleLogger.logError(nfe.getMessage());
              }
          }
          else if (args[i].equals("-servertype"))
          {
              String serverType = args[++i];
              if (serverType.compareToIgnoreCase("g") == 0 ||
                      serverType.compareToIgnoreCase("game") == 0)
              {
                  serverTypeChar = 'g';
              }
              else if (serverType.compareToIgnoreCase("l") == 0 ||
                      serverType.compareToIgnoreCase("lobby") == 0)
              {
                  serverTypeChar = 'l';
              }
              else
              {
                  SimpleLogger.logError("Invalid servertype supplied");
              }
          }
          else if (args[i].equals("-batch"))
          {
              String inputFilename = args[++i];
              File inputFile = new File(inputFilename);
              try
              {
                  input = new FileInputStream(inputFile);
              }
              catch (IOException ioe)
              {
                  SimpleLogger.logError("Error reading from ["+inputFilename+"]", ioe);
              }
          }
          else
          {
              SimpleLogger.logInfo("Unrecognized argument \""+args[i]+"\"");
          }
      }
      
      Admin admin = new Admin(gui, input);
      if (admin.connect(ipAddress, port, serverTypeChar))
      {
         try
         {
             admin.acceptCommands();
         }
         catch (Exception e)
         {
             SimpleLogger.logError(e.getMessage());
         }
      }

      
      System.out.println("Exiting Ivanhoe admin tool");
   }
   
   /**
    * Loop until admin is exited. Accepts commands on std in and
    * transmits them to the server
    */
   private void acceptCommands() throws IOException
   {
      // at present, the only gui interface is for new account creation
      if (useGui)
      {
         this.inputReader.close();
         NewAccountDialog dlg = new NewAccountDialog(null, 
                    null, ipAddr, port);
         dlg.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         dlg.show();
         
         return;
      }
      
      String command;
      this.quit = false;
      while (this.quit == false)
      {
         System.out.print(">");
         command = getUserInput();
         if (command == null || command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("q"))
         {
            quit = true;
         }
         else if (command.equalsIgnoreCase("help"))
         {
            System.out.println("Help: ");
            System.out.println("      say       [msg]       - broadcast message to all users");
            System.out.println("      kick      [user]      - disconnects a given user");
            System.out.println("      kickall               - disconnects all users");
            System.out.println("      shutdown              - shuts down the server");
            System.out.println("      info                  - get info about active games");
            System.out.println("      start     [game name] - start the specified game");
            System.out.println("      stop      [game name] - stop the specified game");
            System.out.println("      retire    [game name] - stop game, mark as retired, but leave in db");
            System.out.println("      delete    [game name] - stop game and remove from db");
            System.out.println("      sleep     [millisec]  - stop admin client for specified number of milliseconds");
            System.out.println("      quit / q              - quit the admin tool");
         }
         else if (command.startsWith("sleep "))
         {
             String subCommands[] = command.split(" ", 2);
             if (subCommands.length >= 2)
             {
                 int sleepLength = Integer.parseInt(subCommands[1]);
                 try
                 {
                     Object mutex = new Object();
                     System.out.println("Sleeping.");
                     synchronized (mutex)
                     {
                         mutex.wait(sleepLength);
                     }
                 }
                 catch (InterruptedException ie) { }
             }
         }
         else
         {
            this.commandWriter.println(command);
            this.commandWriter.flush();
         }
      }
      
      // close socket and stuff
      try
      {
         this.commandWriter.close();
         this.serverSocket.close();
      }
      
      catch (Exception e)
      {
         System.err.println("Unable to disco cleanly; " + e);
      }
   }

   /**
    * Constructs the admin tool and an buffered reader for
    * commands typed to std in, or presents a gui interface
    */
   private Admin(boolean gui, InputStream input)
   {       
      useGui = gui;
      this.inputReader = new BufferedReader(new InputStreamReader(input));
   }
   
   /**
    * Connect to the server
    * @return
    */
   private boolean connect(String ipAddress, int portNumber, char serverType)
   {
      if (ipAddress != null && ipAddress.length() > 0)
      {
          this.ipAddr = ipAddress;
      }
      else
      {
	      // collect connect info
	      System.out.print("Enter Game Server Address [localhost]: ");
	      this.ipAddr = getUserInput();
	      if ("".equals(this.ipAddr))
	      {
	         this.ipAddr = "localhost";
	      }
      }
      
      if (portNumber > 0)
      {
          this.port = portNumber;
      }
      else
      {
	      System.out.print("Enter Base Ivanhoe Port [4000]: ");
	      String strPort = getUserInput();
	      if ("".equals(strPort))
	      {
	         strPort = "4000";
	      }
	      this.port = Integer.parseInt(strPort);
      }
      
      this.port += GAME_PORT_MOD;
         
      System.out.println("Connecting to " + this.ipAddr + ":" + 
         this.port + "...");    
      try
      {
         this.serverSocket = new Socket(this.ipAddr,this. port);
         this.serverSocket.setSoTimeout(1000);
         this.commandWriter = new PrintWriter( this.serverSocket.getOutputStream());
         System.out.println("Connected.");
         
         // start listening for responses
         this.runner = new Thread(this);
         this.runner.start();
         
         return true;
      }
      catch (Exception e)
      {
         System.err.println("Unable to connect: " + e);
      }
      
      return false;
   }

   /**
    * Await input from user
    * @return
    */
   private String getUserInput()
   {
      String data = "";
      
      try
      {
         data = inputReader.readLine();
      }
      catch (IOException e)
      {
         System.err.println("Error reading input");
      }
      
      return data;
   }
   
   public void run()
   {
      // set up reading
      BufferedReader reader = null;
      try
      {
          if (!this.serverSocket.isConnected())
          {
              this.wait(100);
          }
          
          reader = new BufferedReader(
                  new InputStreamReader(this.serverSocket.getInputStream()));
      }
      catch (Exception e)
      {
         System.err.println("Unable to read on socket " + e);
         return;
      }

      // await input from server... just print it when it comes
      while (this.serverSocket.isConnected())
      {
         String line;
         try
         {
            line = reader.readLine();
            if (line == null)
            {
               // connection dropped
               this.quit = true;
               System.out.println("Connection terminated");
               break;
            }
            else
            {
               System.out.print(line + "\n>");
            }
         }
         catch (SocketTimeoutException ste){}
         catch (IOException e)
         {
            this.quit = true;
            System.out.println("Connection terminated");
            break;
         }
      }
      
      System.out.println("Admin tool exiting");
      System.exit(0);
   }
   
   public static Properties loadIvanhoeProperties()
   {
       Properties props = null;

       // Open the server proprties file 
       try
       {
          File f = new File(PROPERTIES_FILE);
          FileInputStream is = new FileInputStream(f);
          if (is == null)
          {
             SimpleLogger.logError("Unable to open properties file");
             return null;
          }
          else
          {
             props = new Properties();
             props.load(is);
          }
       }
       catch (IOException e1)
       {
          SimpleLogger.logInfo(
             "Unable to find properties file");
          
          return new Properties();
       }

       return props;
   }

}
