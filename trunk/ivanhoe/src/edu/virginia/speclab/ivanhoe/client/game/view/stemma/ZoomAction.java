/*
 * Created on Aug 10, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.view.stemma;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;


/**
 * @author benc
 */
class ZoomAction extends
        AbstractAction {

    private final StemmaView stemmaView;
    private final int clicks;
    
    /**
     * Action to zoom in or out one button press
     * 
     * @param text
     *      Action text
     * @param icon
     *      Icon for the button
     * @param clicks
     *      Number of clicks to zoom in, may be negative
     * @param stemmaView
     *      The StemmaView to zoom
     */
    ZoomAction(String text, Icon icon, int clicks, StemmaView stemmaView) {
        super(text, icon);
        this.stemmaView = stemmaView;
        this.clicks = clicks;
        putValue(Action.SHORT_DESCRIPTION, text);
    }
    
    public void actionPerformed(ActionEvent e) {
        stemmaView.discreteZoom(clicks);
    }
}
