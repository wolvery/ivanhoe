/*
 * Created on Nov 18, 2003
 * LinkType
 */
package edu.virginia.speclab.ivanhoe.shared.data;

import java.io.Serializable;

/**
 * LinkType
 * @author lfoster
 *
 * Typeseafe enum of available types pf links
 */
public class LinkType implements Serializable
{
   private final String name;
   
   private LinkType(String name)
   {
      this.name = name;
   }
   
   public String toString()
   {
      return name;
   }
   
   public boolean equals(LinkType that)
   {
      return ( name.equals(that.toString()) );
   }
   
   public static final LinkType INTERNAL = new LinkType("Document Link");
   public static final LinkType URL = new LinkType("Web Link");
   public static final LinkType COMMENT = new LinkType("Commentary");
}
