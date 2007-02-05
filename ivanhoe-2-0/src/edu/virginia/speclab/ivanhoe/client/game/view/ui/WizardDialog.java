/*
 * Created on Oct 15, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;

/**
 * @author Nick
 *
 */
public abstract class WizardDialog extends JDialog
{
    private boolean wizardCanFinish;
    private int currentStep;
    private LinkedList stepPanelList;
    private JButton backButton, nextButton, finishButton, cancelButton;
    private JLayeredPane stepPanelBox;
    private Dimension stepArea;
    
    public WizardDialog( JFrame frame, String title, Dimension stepArea )
    {
        super(frame);
        this.stepArea = stepArea;
        setResizable(true);
        setTitle(title);
        setModal(true);
        stepPanelList = new LinkedList();
        createUI();                
    }
    
    private void createUI()
    {
        this.getContentPane().setLayout(new BorderLayout());

        stepPanelBox = new JLayeredPane();
        stepPanelBox.setPreferredSize(stepArea);        
        this.getContentPane().add(stepPanelBox,BorderLayout.CENTER);
        
        JPanel buttonPanel = createButtonPanel();        
        this.getContentPane().add(buttonPanel,BorderLayout.SOUTH);
    }
    
    private JPanel createButtonPanel()
    {
        JPanel buttonSet = new JPanel();
        buttonSet.setBorder(new EmptyBorder(5,5,5,5));
        backButton = new JButton("< Back");
        backButton.setFont(IvanhoeUIConstants.SMALL_FONT);
        buttonSet.add(backButton);
        nextButton = new JButton("Next >");
        nextButton.setFont(IvanhoeUIConstants.SMALL_FONT);
        buttonSet.add(nextButton);
        finishButton = new JButton("Finish");
        finishButton.setFont(IvanhoeUIConstants.SMALL_FONT);
        buttonSet.add(finishButton);
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(IvanhoeUIConstants.SMALL_FONT);
        buttonSet.add(cancelButton);
        
        backButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                pressBack();
            }
        });
        
        nextButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                pressNext();
            }
        });

        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                pressCancel();
            }
        });

        finishButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                pressFinish();
            }
        });
        
        return buttonSet;
    }
    
    public void setFinishButtonText( String text )
    {
        finishButton.setText(text);
    }
    
    private void pressBack()
    {
        setCurrentStep(currentStep-1);
    }
    
    private void pressNext()
    {
        setCurrentStep(currentStep+1);
    }
    
    private void pressFinish()
    {
        wizardFinished();
    }
    
    private void pressCancel()
    {
        wizardCancelled();
    }
    
    protected abstract void wizardCancelled();
    protected abstract void wizardFinished();
    
    public void addStepPanel( WizardStepPanel stepPanel )
    {        
        stepPanelList.add(stepPanel);        
        stepPanelBox.add(stepPanel, JLayeredPane.DEFAULT_LAYER );
        stepPanel.setBounds(0,0,stepArea.width,stepArea.height);
    }
    
    /**
     * @return Returns the wizardCanFinish.
     */
    public boolean wizardCanFinish()
    {
        return wizardCanFinish;
    }
    
    /**
     * @param wizardCanFinish The wizardCanFinish to set.
     */
    public void setWizardCanFinish(boolean wizardCanFinish)
    {
        this.wizardCanFinish = wizardCanFinish;
        this.finishButton.setEnabled(wizardCanFinish);
    }
    
    public void lockMovement( boolean lock )
    {
        this.backButton.setEnabled(!lock);
        this.nextButton.setEnabled(!lock);
        this.cancelButton.setEnabled(!lock);
        this.finishButton.setEnabled(!lock);
    }
    
    public void startWizard()
    {
        if( stepPanelList.size() > 0 )
        {
            setCurrentStep(1);
            pack();
            centerDialog();
            this.show();
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
    
    /**
     * @return Returns the currentStep.
     */
    public int getCurrentStep()
    {
        return currentStep;
    }
    
    /**
     * @param currentStep The currentStep to set.
     */
    public void setCurrentStep(int step)
    {
        if( step > 0 && step <= stepPanelList.size() )
        {                        
            hidePanels();
            WizardStepPanel stepPanel = (WizardStepPanel) stepPanelList.get(step-1);
            stepPanel.setVisible(true);
            this.stepPanelBox.moveToFront(stepPanel);            
            this.currentStep = step;            
            adjustStepButtonStates();
            stepPanel.panelSelected();
        }
    }
    
    private void hidePanels()
    {
        for( Iterator i = stepPanelList.iterator(); i.hasNext(); )
        {
            WizardStepPanel panel = (WizardStepPanel) i.next();
            panel.setVisible(false);            
        }
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
        
        if( this.currentStep == stepPanelList.size() )
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
    
}
