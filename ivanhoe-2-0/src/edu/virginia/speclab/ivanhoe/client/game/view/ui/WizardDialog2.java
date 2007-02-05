/*
 * Created on Jan 21, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

import javax.swing.JDialog;
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.Box;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.border.EmptyBorder;

/**
 * @author Nick
 *
 */
public abstract class WizardDialog2 extends JDialog
{
    private int currentStep, numberOfSteps;
    
    private JPanel stepPanel;
    private CardLayout stepPanelLayout; 
    private JButton finishButton;
    private JButton cancelButton;
    private JButton nextButton;
    private JButton backButton;

    private boolean wizardCanFinish;
    
    public WizardDialog2( JFrame frame,  String title )
    {
        super(frame);
        setModal(true);
        setTitle(title);
        createUI();        
    }
    
    /**
     * 
     */
    private void createUI()
    {        
        getContentPane().setLayout(new BorderLayout());
        setBounds(100, 100, 500, 490);
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.add(Box.createGlue());
        backButton = new JButton();
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pressBack();
            }
        });
        backButton.setFont(new Font("Verdana", Font.PLAIN, 10));
        buttonPanel.add(backButton);
        backButton.setText("< back");
        nextButton = new JButton();
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pressNext();
            }
        });
        nextButton.setFont(new Font("Verdana", Font.PLAIN, 10));
        buttonPanel.add(nextButton);
        nextButton.setText("next >");
        finishButton = new JButton();
        finishButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pressFinish();
            }
        });
        finishButton.setFont(new Font("Verdana", Font.PLAIN, 10));
        buttonPanel.add(finishButton);
        finishButton.setText("finish");
        cancelButton = new JButton();
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pressCancel();
            }
        });
        cancelButton.setFont(new Font("Verdana", Font.PLAIN, 10));
        buttonPanel.add(cancelButton);
        cancelButton.setText("cancel");
        
        stepPanel = new JPanel();
        stepPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        stepPanelLayout = new CardLayout();
        stepPanel.setOpaque(false);
        stepPanel.setLayout(stepPanelLayout);
        getContentPane().add(stepPanel, BorderLayout.CENTER);        

    }
    
    
    /**
     * @param currentStep The currentStep to set.
     */
    public void setCurrentStep(int step)
    {
        if( step > 0 && step <= numberOfSteps )
        {
            String stepStr = Integer.toString(step);            
            stepPanelLayout.show(stepPanel,stepStr);
            currentStep = step;
            adjustStepButtonStates();
        }
    }
    
    public void startWizard()
    {
        setCurrentStep(1);        
        centerDialog();
        this.show();
    }
    
    public void addStepPanel( JPanel panel )
    {                  
        String step = Integer.toString(++numberOfSteps);
        stepPanel.add(panel,step);
    }
    
    protected void adjustStepButtonStates()
    {
        if( this.currentStep == 1 )
        {
            this.backButton.setEnabled(false);
        }
        else
        {
            this.backButton.setEnabled(true);
        }    
        
        if( this.currentStep == numberOfSteps )
        {
            this.nextButton.setVisible(false);
            this.finishButton.setVisible(true);
        }
        else
        {
            this.nextButton.setVisible(true);
            this.finishButton.setVisible(false);
        }
    }
    
    private void centerDialog()
    {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension dim = tk.getScreenSize();
        int screenHeight = dim.height;
        int screenWidth = dim.width;

        setLocation( (screenWidth - getWidth()) / 2,
                     (screenHeight - getHeight()) / 2 - 50);        
    }
    
    public void lockMovement( boolean lock )
    {
        this.backButton.setEnabled(!lock);
        this.nextButton.setEnabled(!lock);
        this.cancelButton.setEnabled(!lock);
        this.finishButton.setEnabled(!lock);
    }
    
    public void setFinishButtonText( String text )
    {
        finishButton.setText(text);
    }
          
    /**
     * @param wizardCanFinish The wizardCanFinish to set.
     */
    public void setWizardCanFinish(boolean wizardCanFinish)
    {
        this.wizardCanFinish = wizardCanFinish;
        this.finishButton.setEnabled(wizardCanFinish);
    }
    
    private void pressBack()
    {
        Object step;
        
        step = stepPanel.getComponent(currentStep-1);
        if (step instanceof IWizardStepPanel2)
        {
            ((IWizardStepPanel2)step).prevStep();
        }
        
        step = stepPanel.getComponent(currentStep-2);
        if (step instanceof IWizardStepPanel2)
        {
            ((IWizardStepPanel2)step).steppedInto();
        }
        
        setCurrentStep(currentStep-1);
    }
    
    private void pressNext()
    {
        Object step;
        
        step = stepPanel.getComponent(currentStep-1);
        if (step instanceof IWizardStepPanel2)
        {
            ((IWizardStepPanel2)step).nextStep();
        }
        
        step = stepPanel.getComponent(currentStep);
        if (step instanceof IWizardStepPanel2)
        {
            ((IWizardStepPanel2)step).steppedInto();
        }
        
        setCurrentStep(currentStep+1);
    }
    
    private void pressFinish()
    {
        for (int i=0; i < stepPanel.getComponentCount(); ++i)
        {
            Object step = stepPanel.getComponent(i);
            if (step instanceof IWizardStepPanel2)
            {
                ((IWizardStepPanel2)step).finish();
            }
        }
        wizardFinished();
    }
    
    private void pressCancel()
    {
        for (int i=0; i < stepPanel.getComponentCount(); ++i)
        {
            Object step = stepPanel.getComponent(i);
            if (step instanceof IWizardStepPanel2)
            {
                ((IWizardStepPanel2)step).cancel();
            }
        }
        wizardCancelled();
    }
    
    protected abstract void wizardCancelled();
    protected abstract void wizardFinished();

    /**
     * @return Returns the currentStep.
     */
    public int getCurrentStep()
    {
        return currentStep;
    }
    
    /**
     * @return Returns the wizardCanFinish.
     */
    public boolean wizardCanFinish()
    {
        return wizardCanFinish;
    }
    
}
