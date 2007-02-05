/*
 * Created on Mar 2, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author lfoster
 *
 * Abstract message used to pass chunks of binary data 
 * between client & server
 */
public abstract class DataMsg extends Message
{
   private int chunkSize;
   private boolean complete;
   private byte[] data;
   public static final int MAX_DATA_SIZE = 512;
   
   public DataMsg(MessageType msgType)
   {
      super(msgType);
      this.data = new byte[MAX_DATA_SIZE];
      this.complete = false;
      this.chunkSize = MAX_DATA_SIZE;
   }
   
   public boolean isComplete()
   {
      return this.complete;
   }
   
   public void setComplete()
   {
      this.complete = true;
   }
   
   public void setBytes(byte[] bytes, int len)
   {
      this.chunkSize = len;
      this.data = new byte[len];
      this.data = bytes;
   }
   
   public void setBufferSize(int bytesRead)
   {
      this.chunkSize = Math.max(0,bytesRead);
   } 
   
   public int getBufferSize()
   {
      return  this.chunkSize;
   }
   
   public byte[] getDataBufer()
   {
      return data;
   }
}
