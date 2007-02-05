/*
 * Created on Sep 24, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * @author Nick
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IvanhoeButton extends JButton
{
    public static final int BTN_SIZE = 32;

    public IvanhoeButton( String text, Icon icon)
    {
        super(icon);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setText(null);
        setToolTipText(text);
        setSize(BTN_SIZE,BTN_SIZE);        
    }

    public IvanhoeButton( Action act )
    {
        super(act);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setText(null);
        setSize(BTN_SIZE,BTN_SIZE);        
    }
}
