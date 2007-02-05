/*
 * Created on Oct 9, 2003
 *
 * CommEndpoint
 */
package edu.virginia.speclab.ivanhoe.shared;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import edu.virginia.speclab.ivanhoe.shared.message.Message;

/**
 * CommEndpoint
 * @author lfoster
 * @version
 *
 * This class is a collection of the three elements that make up a 
 * network communications endpoint. The elements are: socket, out stream
 * and input stream.
 */
final public class CommEndpoint
{
   private Socket              socket;
   private ObjectOutputStream  output;
   private ObjectInputStream   input;
   private volatile boolean    connected;
   
   /**
    * Construct a comm endpoint to the specifed host/port
    * @param host
    * @param port
    * @throws IOException
    */
   public CommEndpoint(String host, int port) throws IOException
   {
      this.socket = new Socket(host, port); 
//      this.socket.setSoTimeout(5000);
      this.output = new ObjectOutputStream( this.socket.getOutputStream());
      this.output.flush();
      this.input = new ObjectInputStream( this.socket.getInputStream() );
      this.connected = true;
   }
   
   /**
    * Construct a commEndpoint based on an existing socket connection
    * @param socket
    * @throws IOException
    */
   public CommEndpoint(Socket socket)
   {
      this.socket = socket;
      try
      {
         //this.socket.setSoTimeout(5000);
         this.output = new ObjectOutputStream( this.socket.getOutputStream());
         this.output.flush();
         this.input = new ObjectInputStream( this.socket.getInputStream() );
         this.connected = true;
      }
      catch (Exception e)
      {
         SimpleLogger.logError("Unable to create comEndpoint " + e);
         this.connected = false;
      }
   }
   
   /**
    * Write a message to the connection
    * @param msg
    * @throws IOException
    */
   public synchronized void sendMessage(Message msg) throws IOException
   {      
      if (this.output == null)
      {
         SimpleLogger.logError("CommEndpoint Error - Attempt to sendMessage with null output");
      }
      
      this.output.writeObject(msg);
      this.output.flush();
   }
   
   /**
    * Request a message from the connection. This
    * method will block until a message is available
    * @return Next available message
    * @throws IOException
    */
   final public Message getMessage() throws IOException
   {     
      Object objIn = new Object();
      try
      {
         objIn = this.input.readObject();
         if (objIn instanceof Message)
         {
            return (Message)objIn;
         }
      }
      catch (ClassNotFoundException e)
      {
         SimpleLogger.logError("CommEndpoint read object of invalid type; returning null", e);
      }
      
      return null;
   }
   
   /**
    * Close the socket and in/out streams
    * @throws IOException
    */
   public synchronized void disconnect()
   {
      // close the input stream
      try
      {
         if (this.input != null)
         {
            this.input.close();
            this.input = null;
         }
      }
      catch (IOException e)
      {
          SimpleLogger.logError("Error closing input stream.");
      }
      
      // close the output stream
      try
      {
         if (this.output != null)
         {
            this.output.close();
            this.output = null;
         }
      }
      catch (IOException e)
      {
          SimpleLogger.logError("Error closing output stream.");
      }
      
      // close the socket
      try
      {
         if (this.socket != null)
         {
            this.socket.close();
            this.socket = null;            
         }
      }
      catch (IOException e)
      {
          SimpleLogger.logError("Error closing socket.");
      }
      
         
      // flag as disconnected      
      this.connected = false;
      SimpleLogger.logInfo("CommEndPoint disconnected.");
   }
   
   /**
    * Check the status of the connection
    * @return
    */
   public boolean isConnected()
   {
      return this.connected;    
   }
}
