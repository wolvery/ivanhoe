/*
 * Created on Jan 18, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.publish;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.CurrentMove;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardStepPanel;


class MoveSummaryPanel extends WizardStepPanel
{
    private CurrentMove currentMove;
    private MoveInspirationPanel moveInspirationPanel;
    private JEditorPane moveSummary;
    
    public MoveSummaryPanel( MovePublishWizard wizard, CurrentMove currentMove, 
    						 MoveInspirationPanel moveInspirationPanel )
    {
        super(wizard);
        
        this.currentMove = currentMove;
        this.moveInspirationPanel = moveInspirationPanel;
        
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

        // create move summary display
        moveSummary = new JEditorPane();
        moveSummary.setEditable(false);
        moveSummary.setContentType("text/html");
        moveSummary.setMargin(new Insets(10, 10, 10, 10));
        moveSummary.setPreferredSize(new Dimension(200,200));
        moveSummary.setAlignmentX(Component.LEFT_ALIGNMENT);
        moveSummary.setBackground(this.getBackground());
        
        

        JScrollPane sp = new JScrollPane(moveSummary);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        	
        // panel label
        JLabel label = new JLabel("summary");
        label.setFont(IvanhoeUIConstants.BOLD_FONT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        
        // bottom panel with instructions and buttons
        JLabel msg = new JLabel("Here is a list of actions in your move." );
                 			    
        msg.setFont(IvanhoeUIConstants.SMALL_FONT);
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // add components to panel
        add(label);
        add(sp);
        add(msg);
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardStepPanel#panelSelected()
     */
    public void panelSelected()
    {
        String summaryText = currentMove.generateSummaryReport(moveInspirationPanel.getSelectedMoveItems());
        moveSummary.setText(summaryText);
    }
}