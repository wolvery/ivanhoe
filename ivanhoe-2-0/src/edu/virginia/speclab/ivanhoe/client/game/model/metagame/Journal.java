/*
 * Created on Sep 22, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.client.game.model.metagame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.text.Document;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.message.DataMsg;
import edu.virginia.speclab.ivanhoe.shared.message.DocumentDataMsg;
import edu.virginia.speclab.ivanhoe.shared.message.JournalDataMsg;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;

public class Journal implements IMessageHandler
{
    private File tempFile;
    private boolean journalReady;
    
    public Journal()
    {
        // Register for journal data, game info and role
        Ivanhoe.registerGameMsgHandler(MessageType.JOURNAL_DATA, this);

        // init files
        this.tempFile = new File("temp-journal.dat");
        if (this.tempFile.exists())
           this.tempFile.delete();           
    }
    
    public URL getJournalFile()
    {
        if( journalReady == true )
        {
            try
            {
                return tempFile.toURL();
            }
            catch( MalformedURLException e )
            {
                return null;
            }
        }
        else return null;
    }
    
    public void handleMessage(Message msg)
    {   
       if (msg.getType().equals(MessageType.JOURNAL_DATA))
       {
          JournalDataMsg jd = (JournalDataMsg) msg;
          assembleDocumentData(this.tempFile, jd);
          if (jd.isComplete())
          {
             journalReady = true;
          }
       }
    }
    
    /**
     * Piece together chunks of data in the dataMsg into a complete file
     * @param targetFile Destination file
     * @param dataMsg Data Chunks
     */
    private void assembleDocumentData(File targetFile, DataMsg dataMsg)
    {
       // re-assemble file in local filesystem
       FileWriter writer = null;
       try
       {
          // write the chunk of data to a local file
          writer = new FileWriter(targetFile, true);

          String s = new String(dataMsg.getDataBufer());
          writer.write(s, 0, dataMsg.getBufferSize());
       }
       catch (IOException e)
       {
          SimpleLogger.logError(
             "Unable to receive doc data for ["
                + targetFile.getName()
                + "]: "
                + e);
       }
       finally
       {
          if (writer != null)
          {
             try
             {
                writer.flush();
                writer.close();
             }
             catch (IOException e2)
             {
             }
          }
       }
    }
    
    /**
     * Send the journal file to the server for saving
     */
    public void save( JEditorPane journalDisplay )
    {
        // write journal out as local html file
        FileWriter writer = null;
        try {
            // write the chunk of data to a local file
            writer = new FileWriter(this.tempFile, false);
            Document journalDoc = journalDisplay.getDocument();
            // If we don't add 1 to the length, this will truncate one
            // terminating whitespace character or element.  This is important
            // because if we lose all <p> tags, then the HTMLEditorKit will no
            // longer insert them, and there will be no way to actually add
            // newlines.
            journalDisplay.getEditorKit().write(writer,
                    journalDoc, 0, journalDoc.getLength() + 1);
        } catch (Exception e) {
            Ivanhoe.sendErrorMessageToHost("Unable write local journal file.");
            return;
        }
        
        try 
        {
            writer.close();
        } 
        catch (IOException e1) 
        {
            Ivanhoe.sendErrorMessageToHost("Error writing local journal file");
            return;
        }
        
        // stream this file down to server
        FileInputStream source = null;
        try {
            source = new FileInputStream(this.tempFile);
            JournalDataMsg dataMsg;
            boolean done = false;
            int bytesRead;
            while (!done) {
                dataMsg = new JournalDataMsg();
                bytesRead = source.read(dataMsg.getDataBufer());
                if (bytesRead == -1
                        || bytesRead < DocumentDataMsg.MAX_DATA_SIZE) {
                    dataMsg.setComplete();
                    done = true;
                }

                dataMsg.setBufferSize(bytesRead);

                // send the info to the server
                Ivanhoe.getProxy().sendMessage(dataMsg);
                // pretend the server responded with this journal entry
                Ivanhoe.getProxy().receiveMessage(dataMsg);
            }
        } catch (IOException e) {
            Ivanhoe.sendErrorMessageToHost("Error sending journal to server: " + e);            
        } finally {
            if (source != null) {
                try {
                    source.close();                   
                } catch (IOException e1) {
                }
            }
        }
    }

    
}
