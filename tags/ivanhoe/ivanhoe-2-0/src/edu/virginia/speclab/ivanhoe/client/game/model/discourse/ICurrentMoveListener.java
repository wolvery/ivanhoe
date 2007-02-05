/*
 * Created on Oct 8, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse;

import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;

/**
 * @author benc
 */
public interface ICurrentMoveListener 
{
	public void currentMoveChanged(CurrentMove currentMove, IvanhoeAction action); 
}
