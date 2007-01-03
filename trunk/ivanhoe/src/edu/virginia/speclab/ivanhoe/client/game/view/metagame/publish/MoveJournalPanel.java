/*
 * Created on Jan 18, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.publish;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.Journal;
import edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay.JournalPanel;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardStepPanel;


class MoveJournalPanel extends WizardStepPanel
{
    private JournalPanel journalPanel;
    
    public MoveJournalPanel( MovePublishWizard wizard, Journal journal )
    {
        super(wizard);
        
        setLayout(new BorderLayout());
        journalPanel = new JournalPanel(journal);
        journalPanel.setEditable(false);
        add(journalPanel, BorderLayout.CENTER);

        JLabel label = new JLabel("journal (private)");
        label.setFont(IvanhoeUIConstants.BOLD_FONT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(label, BorderLayout.NORTH);
        
        JLabel msg = new JLabel("Update your journal with any notes on this move." );
        msg.setFont(IvanhoeUIConstants.SMALL_FONT);
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(msg, BorderLayout.SOUTH);
    }
    
    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardStepPanel#panelSelected()
     */
    public void panelSelected()
    {
        journalPanel.setEditable(true);
    }
    
    public void save()
    {
        journalPanel.save();
    }
}