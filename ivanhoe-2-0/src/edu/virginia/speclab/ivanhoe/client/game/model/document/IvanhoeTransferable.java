/*
 * Created on Mar 5, 2004
 *
 * IvanhoeSelection
 */
package edu.virginia.speclab.ivanhoe.client.game.model.document;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * @author lfoster
 *
 * IvanhoeTransferable is an implementation of Transferrable that allows
 * custom Ivanhoe data to be passed using the system clipboard. It
 * supports only one data flavor - IvanhoeClipboard
 */
public class IvanhoeTransferable implements Transferable
{
   private IvanhoeClipboard data;
   public static final int IVANHOE_FLAVOR = 0;
   public static final int STRING_FLAVOR = 1;
   

   /** the data flavor of this transferable */
   private static final DataFlavor[] flavors =
      { new DataFlavor(IvanhoeClipboard.class, "IvanhoeText"),
        DataFlavor.stringFlavor};

   /** a data flavor for transferables processed by this component */
   public static final DataFlavor ivanhoeFlavor =
      new DataFlavor(IvanhoeClipboard.class, "IvanhoeText");

   public IvanhoeTransferable(IvanhoeClipboard ivanClip)
   {
      this.data = ivanClip;
   }

   public DataFlavor[] getTransferDataFlavors()
   {
      return (DataFlavor[])flavors.clone();
   }

   public boolean isDataFlavorSupported(DataFlavor flavor)
   {
      boolean supported = false;
      for (int i = 0; i < flavors.length; i++)
      {
         if (flavors[i].equals(flavor))
         {
            supported = true;
            break;
         }
      }
      return supported;
   }

   public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException
   {
      if (flavor.equals(flavors[IVANHOE_FLAVOR]))
      {
         return data;
      }
      else if (flavor.equals(flavors[STRING_FLAVOR]))
      {
         return data.toString();
      }
      else
      {
         throw new UnsupportedFlavorException(flavor);
      }
   }
}
