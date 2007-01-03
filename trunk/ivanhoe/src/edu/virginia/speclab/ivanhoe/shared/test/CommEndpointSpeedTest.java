package edu.virginia.speclab.ivanhoe.shared.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import edu.virginia.speclab.ivanhoe.shared.CommEndpoint;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.RoleMsg;


public class CommEndpointSpeedTest
{
    private long startTime;
    
    private static int NUMBER_OF_MESSAGES = 50;
    
    public CommEndpointSpeedTest()
    {
        ConnectionManager connectionManager = new ConnectionManager();
        connectionManager.start();
        
        CommEndpoint client = null;
        
        try
        {
            SimpleLogger.logInfo("connecting to server");
            client = new CommEndpoint("localhost", 4000);
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        startTime = System.currentTimeMillis();
        for( int i=0; i < NUMBER_OF_MESSAGES; i++ )
        {            
            sendMessage(client);
        }
        
    }
    
    private void sendMessage( CommEndpoint client )
    {
        Message a = new RoleMsg("test", new Role(1,"blah",false), false);

        try
        {
            client.sendMessage(a);
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private class MessageProcessor extends Thread
    {
        private CommEndpoint commEndPoint;
        
        public MessageProcessor( CommEndpoint commEndPoint )
        {
            this.commEndPoint = commEndPoint;
        }
        
        public void run()
        {
            
            SimpleLogger.logInfo("starting message processing");
            
            int count = 0;
            while(count < NUMBER_OF_MESSAGES)
            {
                try
                {
                    Message message = commEndPoint.getMessage();
                    count++;
                } 
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            SimpleLogger.logInfo( NUMBER_OF_MESSAGES+" messages recieved in "+ duration+"ms");
        }
    }
    
    private class ConnectionManager extends Thread
    {
        public void run()
        {
            try
            {
                SimpleLogger.logInfo("starting server socket");
                ServerSocket socket = new ServerSocket(4000);
                Socket clientSocket = socket.accept();
                SimpleLogger.logInfo("client connection accepted");
                MessageProcessor processor = new MessageProcessor( new CommEndpoint(clientSocket) );
                processor.start();
            } 
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
    }
    
    public static void main( String args[] )
    {
        SimpleLogger.initConsoleLogging();
        CommEndpointSpeedTest commTest = new CommEndpointSpeedTest();        
    }
    

}
