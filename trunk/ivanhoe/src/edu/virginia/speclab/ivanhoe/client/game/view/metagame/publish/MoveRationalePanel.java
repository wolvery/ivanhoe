/*
 * Created on Jan 18, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.publish;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.CurrentMove;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardStepPanel;

/**
 * @author Nick
 *
 */
class MoveRationalePanel extends WizardStepPanel  
{
    private JTextArea narrativeTxt;
    
    public MoveRationalePanel( MovePublishWizard wizard, CurrentMove currentMove )
    {
        super(wizard);
        
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
                    
        // create editable narrative entry pane
        narrativeTxt = new JTextArea();
        narrativeTxt.setText(currentMove.getDescription());
        narrativeTxt.setWrapStyleWord(true);
        narrativeTxt.setLineWrap(true);
        narrativeTxt.setFont(IvanhoeUIConstants.SMALL_FONT);
        narrativeTxt.setMargin(new Insets(10, 10, 10, 10));
        narrativeTxt.setPreferredSize(new Dimension(200,200));
        narrativeTxt.setAlignmentX(Component.LEFT_ALIGNMENT);
        narrativeTxt.setEditable(false);
        
        // listen for editing 
        narrativeTxt.addKeyListener( new KeyAdapter() {
            public void keyTyped(KeyEvent e)
            {
                checkEntryLength();
            }                
        });
        
        JScrollPane sp = new JScrollPane(narrativeTxt);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel l1 = new JLabel("rationale (public)");
        l1.setFont(IvanhoeUIConstants.BOLD_FONT);  
        l1.setAlignmentX(Component.LEFT_ALIGNMENT);

        // bottom panel with instructions and buttons
        JLabel msg = new JLabel("Now enter a rationale for your move." );
        msg.setFont(IvanhoeUIConstants.SMALL_FONT);
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        add(l1);
        add(sp);
        add(msg);
    }
    
    // make the finish button appear when the user types some text, otherwise hide it.
    private void checkEntryLength()
    {
        String text = narrativeTxt.getText();
        if( text != null && text.length() > 0 )
        {
            this.wizardDialog.setWizardCanFinish(true);
        }
        else
        {
            this.wizardDialog.setWizardCanFinish(false);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardStepPanel#panelSelected()
     */
    public void panelSelected()
    {
        narrativeTxt.requestFocus();
        narrativeTxt.setEditable(true);
    }
    
    public String getText()
    {
        return narrativeTxt.getText();
    }

}