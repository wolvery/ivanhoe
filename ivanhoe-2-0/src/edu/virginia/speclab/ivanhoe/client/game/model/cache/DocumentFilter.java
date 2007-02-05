/*
 * Created on Jun 17, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.client.game.model.cache;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * DocumentFilter
 * @author lfoster
 *
 * Filter for document files used when adding a doc to DF
 */
public class DocumentFilter extends FileFilter
{
   private String description;
   private String extension;
   
   public DocumentFilter(String ext, String desc)
   {
	  this.description = desc;
	  this.extension = ext.toLowerCase();
   }
   
   public boolean accept(File f)
   {
	  if (f.isDirectory())
	  {
		 return true;
	  }

	  String name = f.getName().toLowerCase();
	  return (name.endsWith(extension));
   }

   //The description of this filter
   public String getDescription()
   {
	  return this.description;
   }
}