/*
 * Created on Oct 7, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse;

/**
 * @author Nick
 */
public abstract interface IDiscourseFieldTimeListener
{
    public void discourseFieldTickChanged( int tick );
    public void discourseFieldMoveEventChanged( MoveEvent event );
}
