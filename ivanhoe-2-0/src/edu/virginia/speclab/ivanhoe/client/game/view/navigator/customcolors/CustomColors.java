/*
 * Created on Jun 30, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors;

import edu.virginia.speclab.ivanhoe.client.util.PropertiesManager;

/**
 * @author Nick
 * 
 * This is where the color preferences information is accessed for the discourse field 
 * navigator interface. Preferences are stored using the properties maanger. 
 */
public class CustomColors
{
    public static final int PLAYER_CIRCLE = 0;
    public static final int DELETION_ARC = 1;
    public static final int INSERTION_ARC = 2;
    public static final int ANNOTATION_ARC = 3;
    public static final int LINK_ARC = 4;
    
    public static final int FIRST_ARC = 1;
    public static final int LAST_ARC = 4;
    
    private PropertiesManager propertiesManager;
    private ColorPair deletionArcColors;
    private ColorPair insertionArcColors;
    private ColorPair annotationArcColors;
    private ColorPair linkArcColors;

    public CustomColors( PropertiesManager propertiesManager )
    {
        this.propertiesManager = propertiesManager;
    }
    
    /**
     * Loads color preferences from the properties data.
     */
    public void loadColors( PropertiesManager propertiesManager )
    {
       deletionArcColors =  new ColorPair( propertiesManager.getProperty("deletion-arc-stroke-color"),
               							   propertiesManager.getProperty("deletion-arc-fill-color") );
       insertionArcColors =  new ColorPair( propertiesManager.getProperty("insertion-arc-stroke-color"), 
               								propertiesManager.getProperty("insertion-arc-fill-color") );
       annotationArcColors =  new ColorPair( propertiesManager.getProperty("annotation-arc-stroke-color"),
               								propertiesManager.getProperty("annotation-arc-fill-color") );
       linkArcColors =  new ColorPair( propertiesManager.getProperty("link-arc-stroke-color"), 
               						   propertiesManager.getProperty("link-arc-fill-color") );
    }
    
    /**
     * Commits current color preferences to local properties store.
     *
     */
    public void saveColors()
    {       
        propertiesManager.setProperty("deletion-arc-stroke-color", new Integer(deletionArcColors.strokeColor.getRGB()).toString() );
        propertiesManager.setProperty("deletion-arc-fill-color", new Integer(deletionArcColors.fillColor.getRGB()).toString() );
        propertiesManager.setProperty("insertion-arc-stroke-color", new Integer(insertionArcColors.strokeColor.getRGB()).toString() );
        propertiesManager.setProperty("insertion-arc-fill-color", new Integer(insertionArcColors.fillColor.getRGB()).toString() );
        propertiesManager.setProperty("annotation-arc-stroke-color", new Integer(annotationArcColors.strokeColor.getRGB()).toString() );
        propertiesManager.setProperty("annotation-arc-fill-color", new Integer(annotationArcColors.fillColor.getRGB()).toString() );
        propertiesManager.setProperty("link-arc-stroke-color", new Integer(linkArcColors.strokeColor.getRGB()).toString() );
        propertiesManager.setProperty("link-arc-fill-color", new Integer(linkArcColors.fillColor.getRGB()).toString() );
    }
       
    /**
     * @return Returns the annotationArcColors.
     */
    public ColorPair getAnnotationArcColors()
    {
        return annotationArcColors;
    }
    /**
     * @return Returns the deletionArcColors.
     */
    public ColorPair getDeletionArcColors()
    {
        return deletionArcColors;
    }
  
    /**
     * @return Returns the insertionArcColors.
     */
    public ColorPair getInsertionArcColors()
    {
        return insertionArcColors;
    }
    /**
     * @return Returns the linkArcColors.
     */
    public ColorPair getLinkArcColors()
    {
        return linkArcColors;
    }
    
    public ColorPair getArcColors(int arcType)
    {
        switch (arcType)
        {
            case DELETION_ARC:
                return getDeletionArcColors();
            case INSERTION_ARC:
                return getInsertionArcColors();
            case ANNOTATION_ARC:
                return getAnnotationArcColors();
            case LINK_ARC:
                return getLinkArcColors();
            default:
                throw new RuntimeException("CustomColors queried for unsupported arc color type "+arcType);
        }
    }
    
    public void setArcColors(ColorPair colors, int arcType)
    {
        switch (arcType)
        {
            case DELETION_ARC:
                setDeletionArcColors(colors);
                break;
            case INSERTION_ARC:
                setInsertionArcColors(colors);
                break;
            case ANNOTATION_ARC:
                setAnnotationArcColors(colors);
                break;
            case LINK_ARC:
                setLinkArcColors(colors);
                break;
            default:
                throw new RuntimeException("CustomColors queried for unsupported arc color type "+arcType);
        }
    }
    
    public void setAnnotationArcColors(ColorPair annotationArcColors)
    {
        this.annotationArcColors = annotationArcColors;
    }
    
    public void setDeletionArcColors(ColorPair deletionArcColors)
    {
        this.deletionArcColors = deletionArcColors;
    }
    
    public void setInsertionArcColors(ColorPair insertionArcColors)
    {
        this.insertionArcColors = insertionArcColors;
    }
    
    public void setLinkArcColors(ColorPair linkArcColors)
    {
        this.linkArcColors = linkArcColors;
    }
}
