/*
 * Created on Mar 10, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

/**
 * @author benc
 */
public interface IWizardStepPanel2
{
    public void nextStep();
    public void prevStep();
    public void steppedInto();
    public void cancel();
    public void finish();
}