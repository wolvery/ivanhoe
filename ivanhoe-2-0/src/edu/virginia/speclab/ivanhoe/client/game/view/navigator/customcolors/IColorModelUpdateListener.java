/*
 * Created on Jan 3, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors;

/**
 * @author benc
 */
public interface IColorModelUpdateListener
{
    // update the listener's model
    public void updateColorModel( String roleName, ColorPair colors, int arcType );
}

