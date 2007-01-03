/*
 * Created on Jan 12, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.lobby.newgame;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;

/**
 * @author Nick
 *
 */
class RolePanel extends JPanel
{
	private JTextArea objectives;

	private final String panelText = "Describe any special rules and objectives.";
	    
    public RolePanel()
    {
		setLayout(new BorderLayout());		
		
		JLabel info = new JLabel(panelText);
		info.setFont(IvanhoeUIConstants.SMALL_FONT);
				
		this.objectives = new JTextArea();
		this.objectives.setWrapStyleWord(true);
		this.objectives.setLineWrap(true);
		this.objectives.setMargin(new Insets(1, 8, 1, 1));
		JScrollPane sp  = new JScrollPane(this.objectives);
		JPanel dp = new JPanel(new BorderLayout());
		dp.add(sp, BorderLayout.CENTER);
		
		add(info, BorderLayout.NORTH);
		add(dp, BorderLayout.CENTER);
        
    }

	public String getObjective()
	{
		String obj = "";
		try
		{
			Document objDoc = this.objectives.getDocument();
			obj = objDoc.getText(0, objDoc.getLength());
		}
		catch (BadLocationException e){}

		return obj;
	}

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardStepPanel#panelSelected()
     */
    public void panelSelected()
    {
        // TODO Auto-generated method stub

    }

}
