/*
 * Created on Sep 21, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.gameaction;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;

/**
 * @author Nick
 */
public class RoleAction extends AbstractAction
{

    public RoleAction(String text, Icon icon)
    {
		super(text,icon);
		putValue(Action.SHORT_DESCRIPTION, text);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0)
    {
        Workspace.instance.openRoleWindow();
    }

}
