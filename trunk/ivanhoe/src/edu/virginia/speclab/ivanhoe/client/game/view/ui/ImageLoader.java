/*
 * Created on Feb 14, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

/**
 * ImageLoader
 * http://wiki.java.net/bin/view/Games/LoadingSpritesWithImageIO
 * @author ScottWPalmer 
 */
public class ImageLoader {
	final GraphicsConfiguration gc;
	
	public ImageLoader(GraphicsConfiguration gc) {
		if (gc == null) {
			gc =
				GraphicsEnvironment
					.getLocalGraphicsEnvironment()
					.getDefaultScreenDevice()
					.getDefaultConfiguration();
		}
		this.gc = gc;
	}
   
   public BufferedImage createImage( int width, int height )
   {      
      return gc.createCompatibleImage( width, height );
   }

	public BufferedImage loadImage(String resource) {
		try {

			File f = new File(resource);
			BufferedImage src = javax.imageio.ImageIO.read(f);
			//Images returned from ImageIO are currently (java version <= 1.4.2) NOT managed images
			//Therefor, we copy it into a ManagedImage
			BufferedImage dst =
				gc.createCompatibleImage(
					src.getWidth(),
					src.getHeight(),
					src.getColorModel().getTransparency());
			// Setting transparency
			Graphics2D g2d = dst.createGraphics();
			g2d.setComposite(AlphaComposite.Src);
			// Copy image
			g2d.drawImage(src, 0, 0, null);
			g2d.dispose();
//			SimpleLogger.logInfo("Loading Image:"+resource+" Buffer properties = "+dst.toString());
			return dst;
		} catch (java.io.IOException ioe) {
			return null;
		}
	}
}
