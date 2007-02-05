/*
 * Created on Jan 9, 2004
 */

package edu.virginia.speclab.ivanhoe.client.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;

import rachel.url.RachelUrlFactory;

import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
 
/**
 * @author dmg2n@virginia.edu
 * 
 * Utility class to make it simpler to access resources (help file, icons,
 * images, properties, etc) in the resources.jar file. This should hopefully
 * make the pains of deploying via JNLP a little easier.
 */
public class ResourceHelper
{
	static
	{
		URL.setURLStreamHandlerFactory(new RachelUrlFactory());
	}

	public static final ResourceHelper instance = new ResourceHelper();
	
	/**
	 * Returns the Icon associated with the name from the resources. The resouce
	 * should be in the path. If the file isn't found, it will be associated
	 * with the bogus "imageNotFound.gif" rather than halting.
	 * 
	 * @param name
	 *            Name of the icon file i.e., /path/to/help16.gif
	 * @return An ImageIcon resource for the file.
	 */
	public ImageIcon getIcon(String name)
	{
		ImageIcon icon = getIconJnlp(name);
//		SimpleLogger.logInfo("Loaded Icon: "+name);
		return icon;
	}

//	private static ImageIcon getIconFile(String name)
//	{
//		if (name != null)
//		{
//			return new ImageIcon(name);
//		}
//
//		return new ImageIcon("notFound.gif");
//	}

	private ImageIcon getIconJnlp(String name)
	{
		ImageIcon icon = new ImageIcon("imageNotFound.gif");
		if (name != null)
		{
			try
			{
				// Get current classloader 
				ClassLoader cl = this.getClass().getClassLoader(); 
				URL url = cl.getResource(name);
				
				if (url == null)
				{
				    SimpleLogger.logError("Cannot find icon resource \""+name+"\"");
				    return null;
				}
				
				InputStream is = url.openStream();

				if (is != null)
				{
					icon = new ImageIcon(url);
				}
			} catch (IOException e)
			{
				SimpleLogger.logError("Could not open image file: " + name);
			}
		}

		return icon;
	}

	/**
	 * Returns the Image associated with the name from the resources. The
	 * resouce should be in the path. If the file isn't found, a null reference
	 * will be returned.
	 * 
	 * @param name
	 *            Name of the image file i.e., /path/to/help16.gif
	 * @return An Image resource for the file.
	 */
	public	static Image getImage(String name)
	{
		Image image = getImageJnlp(name);
//		SimpleLogger.logInfo("Loaded Image: "+name);
		return image;
	}

//	private static Image getImageFile(String name)
//	{
//		Image image = null;
//
//		if (name != null)
//		{
//			Toolkit toolkit = Toolkit.getDefaultToolkit();
//			image = toolkit.getImage(name);
//		}
//
//		return image;
//	}
 

	private static Image getImageJnlp(String name)
	{
		Image image = null;

		if (name != null)
		{
			try
			{
				URL url = new URL("class://ResourceAnchor/" + name);
				InputStream is = url.openStream();

				if (is != null)
				{
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					image = toolkit.getImage(url);
				}
			} catch (IOException e)
			{
				SimpleLogger.logError("Could not open image file: " + name);
			}
		}

		return image;
	}

	/**
	 * Retreives a Properties object for a specified file in the resources.jar
	 * 
	 * @param name path to file
	 * @return A Properties object
	 */
	public Properties getProperties(String name)
	{
		return getPropertiesJnlp(name);		
	}
	
//	private static Properties getPropertiesFile(String name)
//	{
//		Properties props = new Properties();
//
//		try
//		{
//			InputStream is = new FileInputStream(name);
//			props.load(is);
//		} catch (IOException e)
//		{
//			SimpleLogger
//					.logError("Could not load property file from resource: "
//							+ name);
//		}
//
//		return props;
//	}

	private Properties getPropertiesJnlp(String name)
	{
		Properties props = new Properties();

		try
		{
			// reference property file as part of resources.jar
			ClassLoader cl = this.getClass().getClassLoader();
			URL propURL = cl.getResource(name);
			
			if (propURL == null)
			{
			    SimpleLogger.logError("Cannot find properties resource \""+name+"\"");
			    return null;
			}
			
			InputStream is = propURL.openStream();
			props.load(is);
		} catch (IOException e)
		{
			SimpleLogger
					.logError("Could not load property file from resource: "
							+ name);
		}

		return props;
	}

	/**
	 * Retreives an input stream for a specified file in the resources.jar
	 * 
	 * @param name
	 *            path to file
	 * @return An InputStream
	 */

	public static InputStream getInputStream(String name)
	{
		InputStream stream = getInputStreamJnlp(name); 
//		SimpleLogger.logInfo("Loaded Stream: "+name);
		return stream;
	}
	
//	private static InputStream getInputStreamFile(String name)
//	{
//		InputStream is = null;
//
//		try
//		{
//			is = new FileInputStream(name);
//		} catch (IOException e)
//		{
//			SimpleLogger.logError("Could not load file from resource: " + name);
//		}
//
//		return is;
//	}

	private static InputStream getInputStreamJnlp(String name)
	{
		InputStream is = null;

		try
		{
			// reference url as part of resources.jar
			URL isURL = new URL("class://ResourceAnchor/" + name);
			is = isURL.openStream();
		} catch (IOException e)
		{
			SimpleLogger.logError("Could not load file from resource: " + name);
		}

		return is;
	}

	/**
	 * Retrieves a URL for a resource in the res directory
	 * 
	 * @param name
	 *            path to file, ex - res/icon/open.gif
	 * @return A URL representing the resource on the filesystem
	 */
	public URL getUrl(String name)
	{		
		URL url = getUrlJnlp(name);
//		SimpleLogger.logInfo("Loaded URL: "+name);
		return url;
	}
	
//	private static URL getUrlFile(String name)
//	{
//		URL url = null;
//
//		if (name != null)
//		{
//			try
//			{
//				url = new URL("file:///" + System.getProperty("user.dir") + "/"
//						+ name);
//			} catch (IOException e)
//			{
//				SimpleLogger.logError("Could not create URL from resource: "
//						+ name);
//			}
//		}
//
//		return url;
//	}

	private URL getUrlJnlp(String name)
	{
		URL url = null;

		if (name != null)
		{
			ClassLoader cl = this.getClass().getClassLoader(); 
			url = cl.getResource(name);				
		}

		return url;
	}
}

