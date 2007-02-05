/*
 * Created on Jun 28, 2004
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
public class ColorAction extends AbstractAction
{
	public ColorAction(String text, Icon icon)
	{
		super(text, icon);
		putValue(Action.SHORT_DESCRIPTION, text);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		Workspace.instance.openColorsWindow();
	}
}
