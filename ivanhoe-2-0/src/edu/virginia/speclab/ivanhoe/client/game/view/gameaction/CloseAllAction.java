/*
 * Created on Mar 15, 2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.client.game.view.gameaction;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;

/**
 * @author Nathan Piazza
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CloseAllAction extends AbstractAction {
	
	
	public CloseAllAction(String text, Icon icon)
	{
		super(text,icon);
		putValue(Action.SHORT_DESCRIPTION,"Close All Windows");

	}
	
	public void actionPerformed(ActionEvent e)
	{
		Workspace.instance.closeAllWindows();
	}

}
