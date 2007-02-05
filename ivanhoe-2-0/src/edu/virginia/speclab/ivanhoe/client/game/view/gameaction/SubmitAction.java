/*
 * Created on Mar 15, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.gameaction;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.IvanhoeGame;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.CurrentMove;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.ICurrentMoveListener;
import edu.virginia.speclab.ivanhoe.client.game.view.IvanhoeFrame;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.metagame.publish.MovePublishWizard;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;
import edu.virginia.speclab.ivanhoe.shared.data.Move;


/**
 * @author Nathan Piazza
 */
public class SubmitAction 
	extends AbstractAction implements ICurrentMoveListener
{  
    private IvanhoeGame ivanhoeGame;
    private IvanhoeFrame ivanhoeFrame;
    private boolean enabled;
    
	public SubmitAction( IvanhoeGame game, IvanhoeFrame frame )
	{
		super("post move");
		this.ivanhoeGame = game;
        this.ivanhoeFrame = frame;
        this.updateWritability();
		putValue(Action.SHORT_DESCRIPTION, "post move");
	}
	
	public void actionPerformed(ActionEvent e)
    {
        CurrentMove currentMove = ivanhoeGame.getDiscourseField()
                .getCurrentMove();
        if (!enabled)
        {
            String explaination;
            String shortExplaination;
            if (!ivanhoeGame.isWritable())
            {
                explaination = "You do not currently have permission to modify"
                        + " this game.  This could be because it is a restricted"
                        + " game or because the game has been archived.";
                shortExplaination = "game not writable";
            }
            else if (currentMove == null || currentMove.getActionCount() <= 0)
            {
                explaination = "There is no move currently under way.";
                shortExplaination = "currentMove empty or null";
            }
            else
            {
                explaination = "Move submission is not possible at this time."
                        + "  Try again later or contact the technical administrator.";
                shortExplaination = "reason unknown";
            }
            Ivanhoe.showErrorMessage("Move submission not possible",
                    explaination);
            SimpleLogger
                    .logInfo("Move submission failed: " + shortExplaination);
            return;
        }
        
        Workspace.instance.closeAllWindows();

        MovePublishWizard wizard = new MovePublishWizard(ivanhoeFrame,
                currentMove, ivanhoeGame.getJournal(), ivanhoeGame
                        .getCategoryManager(), ivanhoeGame.getDiscourseField()
                        .getDiscourseFieldTimeline());
        wizard.startWizard();
    }

    public void currentMoveChanged(CurrentMove currentMove, IvanhoeAction action)
    {
        updateWritability();
    }
    
    private void updateWritability()
    {
        Icon icon;
        Move currentMove = ivanhoeGame.getDiscourseField().getCurrentMove();
        if (currentMove != null && currentMove.isStarted() && ivanhoeGame.isWritable())
        {
            enabled = true;
            icon = ResourceHelper.instance.getIcon("res/icons/publish.jpg");
        }
        else
        {
            enabled = false;
            icon = ResourceHelper.instance.getIcon("res/icons/publish-disabled.jpg");
        }
        
        putValue(Action.SMALL_ICON, icon);   
    }
}