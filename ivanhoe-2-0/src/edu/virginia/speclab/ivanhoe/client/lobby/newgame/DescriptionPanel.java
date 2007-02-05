/*
 * Created on Jan 7, 2005
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
class DescriptionPanel extends JPanel
{
    private JTextArea description;
    
    private final String panelText1 = "Provide a general description of the game.";

    public DescriptionPanel()
    {
        super();
        
       	setLayout(new BorderLayout());
       	       	
       	this.description = new JTextArea();
    	
    	this.description.setLineWrap(true);
    	this.description.setWrapStyleWord(true);
    	this.description.setMargin(new Insets(1,8,1,1));
    	JScrollPane sp  = new JScrollPane(this.description);
    	JPanel dp = new JPanel(new BorderLayout());
    	
    	dp.add(sp, BorderLayout.CENTER);
    	
    	JLabel info = new JLabel(panelText1);
    	info.setFont(IvanhoeUIConstants.SMALL_FONT);
    	
    	add(info, BorderLayout.NORTH );
    	add(dp, BorderLayout.CENTER );
    }
    
    public String getDescription()
    {
    	String desc = "";

    	try
		{
			Document desDoc = this.description.getDocument();
			desc = desDoc.getText(0,desDoc.getLength());
		}
		catch (BadLocationException e){}
		
		return desc;
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardStepPanel#panelSelected()
     */
    public void panelSelected()
    {
        // TODO Auto-generated method stub

    }

}
