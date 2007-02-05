/*
 * Created on Jan 6, 2004
 *
 * Target
 */
package edu.virginia.speclab.ivanhoe.shared.data;

import java.io.Serializable;


/**
 * Link
 * @author lfoster
 *
 * Collection of the data needed to represent a target of 
 * an ivanhoe link
 */
public class Link implements Serializable
{
   private final String id;
   private final LinkType linkType;
   private final LinkTag linkTag;
   private final String label;
   private String anchorText;
   
   public Link(Link src)
   {
      this.id = src.id;
      this.linkType = src.linkType;
      this.linkTag = src.linkTag;
      this.label = src.label;
      this.anchorText = src.anchorText;
   }
   
   public Link(String actionId,
      LinkType linkType, LinkTag linkTag, String label)
   {
      this.id = actionId;
      this.linkType = linkType;
      this.linkTag = linkTag;
      this.label = label;
   }

   /**
    * Get a short textual description of this linktarget
    * @return
    */
   public String getDescription()
   {
      return (
         this.getType().toString() + ": "             
            + " target '"
            + this.label
            + "'");
   }
   
   /**
    * Get the lable associated with this linktarget
    * @return
    */
   public String getLabel()
   {
      return label;
   }

   /**
    * Get the type of link
    * @return
    */
   public LinkType getType()
   {
      return linkType;
   }

   /**
    * Get the type-specific data contained in this target
    * @return
    */
   public LinkTag getLinkTag()
   {
      return linkTag;
   }
   
   /**
    * Get the actionID that created this linktarget
    * @return
    */
   public String getId()
   {
      return this.id;
   }
   
   public String getAnchorText()
   {
      return this.anchorText;
   }

   public String toString()
   {
      return getLabel();
   }
   
   public void setAnchorText(String txt)
   {
      this.anchorText = txt;
   }
}
