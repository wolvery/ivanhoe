/*
 * Created on Dec 9, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * @author Nick
 *
 * Each step in a wizard dialog is displayed using a WizardStepPanel derived class. 
 */
public class WizardStepPanel extends JPanel
{
    protected WizardDialog wizardDialog;
    
    public WizardStepPanel( WizardDialog wizDialog )
    {
        this.wizardDialog = wizDialog;
        setBorder(new EmptyBorder(10,10,10,10));
    }
    
    public void panelSelected() 
    {
        // do nothing
    }    

}
