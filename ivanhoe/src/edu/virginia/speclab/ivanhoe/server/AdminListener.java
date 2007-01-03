/*
 * Created on Jan 21, 2004
 *
 * AdminListener
 */
package edu.virginia.speclab.ivanhoe.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import edu.virginia.speclab.ivanhoe.server.game.IvanhoeServer;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;

/**
 * @author lfoster
 *
 * Manage Admin connections & commands
 */
public class AdminListener extends Thread
{
   private boolean             running;
   private ServerSocket        listenSocket;
   private IvanhoeServer       adminTarget;
   private AdminProxy          adminProxy;
   
   public AdminListener (IvanhoeServer admin, int port) throws IOException
   {
      SimpleLogger.logInfo("Creating admin listener at port: " + port);
      this.listenSocket = new ServerSocket(port);
      this.listenSocket.setSoTimeout(5000);
      this.adminTarget = admin;
      this.running = true;
      this.start();
   }
   
   public void stopListening()
   {
      running = false;
      try
      {
         this.interrupt();
         this.join(5000);
         this.listenSocket.close();
      }
      catch (Exception e){}
      
      if (this.adminProxy != null)
      {
         SimpleLogger.logInfo("Closing admin connection");
         this.adminProxy.disconnect();
         this.adminProxy = null;
      }
      
      SimpleLogger.logInfo("AdminAcceptor stopped");
   }
   
   public void run()
   {
      Socket socket = null;
      while (this.running == true)
      {
         try
         {
            socket = this.listenSocket.accept();
            this.adminProxy = new AdminProxy(adminTarget, socket);
            SimpleLogger.logInfo("Got admin connection"); 
         }
         catch (Exception e1) {}
      }
      SimpleLogger.logInfo("Admin listener thread exited");
   }
   
   /**
    * 
    * @author lfoster
    *
    * To change the template for this generated type comment go to
    * Window>Preferences>Java>Code Generation>Code and Comments
    */
   private class AdminProxy extends Thread
   {
      private Socket socket;
      private IvanhoeServer ivanhoeServer;
      private boolean connected;
      private PrintWriter  responseWriter;
      
      public AdminProxy(IvanhoeServer admin, Socket socket)
      {
         this.socket = socket;
         try
         {
            this.responseWriter = new PrintWriter( this.socket.getOutputStream());
         }
         catch (IOException e1){}
         
         this.ivanhoeServer = admin;
         this.connected = true;
         this.start();
      }
      
      public void disconnect()
      {
         try
         {
            this.socket.close();
         }
         catch (IOException e){}
         this.connected = false;
      }

      public boolean isConnected()
      {
         return this.connected;
      }
      
      public void run()
      {
         // set up reading
         BufferedReader reader = null;
         try
         {
            reader =
               new BufferedReader(
                  new InputStreamReader(this.socket.getInputStream()));
         }
         catch (Exception e)
         {
            SimpleLogger.logError("Unable to read on socket " + e);
            return;
         }

         while (isConnected())
         {
            String line;
            try
            {
               line = reader.readLine();
               if (line == null)
               {
                  // connection dropped
                  disconnect();
               }
               else
               {
                  handleCommand(line);
               }
            }
            catch (SocketTimeoutException ste){}
            catch (IOException e)
            {
               disconnect();
            }
         }
      }

      private void handleCommand(String command)
      {
         boolean invalid = false;
         if (command.equalsIgnoreCase("shutdown"))
         {
            this.responseWriter.println("Shutting down...");
            this.responseWriter.flush();
            this.ivanhoeServer.shutdown();
         }
         else
         {
            if (command.startsWith("say "))
            {
               this.ivanhoeServer.systemAnnouncement(command.substring(4));
            }
            else if (command.startsWith("kickall"))
            {
                this.ivanhoeServer.kickAllPlayers();
                this.responseWriter.println("Kicked all players");
                this.responseWriter.flush();
            }
            else if (command.startsWith("kick "))
            {
               if (this.ivanhoeServer.kickPlayer(command.substring(5)))
               {
                  this.responseWriter.println("Player kicked");
                  this.responseWriter.flush();
               }
               else
               {
                  this.responseWriter.println("Unable to kick player");
                  this.responseWriter.flush();
               }
            }
            else if (command.startsWith("retire"))
            {
                this.responseWriter.println("Game retired, shutting down");
                this.responseWriter.flush();
            	this.ivanhoeServer.retireGame();
            }
            else if (command.startsWith("delete"))
            {
               if (this.ivanhoeServer.deleteGame())
               {
                  this.responseWriter.println("Game deleted");
                  this.responseWriter.flush();
               }
               else
               {
                  this.responseWriter.println("Unable to delete game");
                  this.responseWriter.flush();
               }
            }
            else
            {
               invalid = true;
            }
         }
         
         if (invalid)
         {
            this.responseWriter.println("Invalid command");
            this.responseWriter.flush();
         }
      }
   }
}

