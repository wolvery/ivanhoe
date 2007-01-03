/*
 * Created on Mar 15, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.gameaction;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;

/**
 * @author Nathan Piazza
 */
public class SearchAction extends AbstractAction 
{
	public SearchAction(String text, Icon icon)
	{
		super(text,icon);
		putValue(Action.SHORT_DESCRIPTION, text);
	}
	
	public void actionPerformed(ActionEvent e) 
	{
        Workspace.instance.openSearchWindow();
	}
}
