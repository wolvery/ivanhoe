/*
 * Created on Dec 22, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors;

/**
 * @author benc
 */
public interface IColorPairViewListener
{
    public void handleOk(ColorPairView dispatcher);
    public void handleCancel(ColorPairView dispatcher);
    public void handleApply(ColorPairView dispatcher);
}
