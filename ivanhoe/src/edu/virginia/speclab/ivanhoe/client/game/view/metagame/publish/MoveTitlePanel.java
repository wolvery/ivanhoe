/*
 * Created on Jan 12, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.publish;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardDialog;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardStepPanel;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;

/**
 * @author Nick
 *
 */
class MoveTitlePanel extends WizardStepPanel
{
    private JTextField titleEdit;
    
    private final String panelTitle = "Publish Your Move";
    private final String panelText1 = "This wizard will guide you through the process of "+
    						 		  "publishing your move.";
    private final String panelText2 = "The first step is to title your move. "+
    								  "Please enter a name below and press next.";
    
    public MoveTitlePanel( WizardDialog wizard )
    {
        super(wizard);
    	
        setLayout( new BorderLayout() );
        JPanel textPanel = createTextPanel();
        Box pictureBox = createPicturePanel();
        add( textPanel, BorderLayout.EAST );
        add( pictureBox, BorderLayout.WEST );
    }
    
    private Box createPicturePanel()
    {
        Box pictureBox = Box.createVerticalBox();               
        JLabel picture =  new JLabel(ResourceHelper.instance.getIcon("res/images/ivan.gif"));        
        picture.setBorder( new EmptyBorder(0,0,0,5));
        pictureBox.add(picture);

        return pictureBox;
    }
    
    private JPanel createTextPanel()
    {
        JPanel textPanel = new JPanel();

        JTextArea titleLabel = new JTextArea(panelTitle);
        titleLabel.setEditable(false);
        titleLabel.setFont(IvanhoeUIConstants.LARGE_FONT);
        titleLabel.setWrapStyleWord(true);
        titleLabel.setLineWrap(true);
        titleLabel.setBackground(textPanel.getBackground());
        titleLabel.setSize( new Dimension( 100, 10 ));
        titleLabel.setLocation(0,10); 
        
        JTextArea text1Label = new JTextArea(panelText1);
        text1Label.setEditable(false);
        text1Label.setFont(IvanhoeUIConstants.SMALL_FONT);
        text1Label.setWrapStyleWord(true);
        text1Label.setLineWrap(true);
        text1Label.setBackground(textPanel.getBackground());
        text1Label.setSize( new Dimension( 100, 10 ));
        titleLabel.setLocation(0,20); 
        
        JTextArea text2Label = new JTextArea(panelText2);
        text2Label.setEditable(false);
        text2Label.setFont(IvanhoeUIConstants.SMALL_FONT);
        text2Label.setWrapStyleWord(true);
        text2Label.setLineWrap(true);
        text2Label.setBackground(textPanel.getBackground());
        text2Label.setSize( new Dimension( 100, 10 ));
        titleLabel.setLocation(0,30);
        
        textPanel.setLayout( null );
        textPanel.add( titleLabel );
        textPanel.add( text1Label );
        textPanel.add( text2Label );
        textPanel.add( createTitlePanel() );
        textPanel.setSize( new Dimension( 100,100 ));
        
        
        return textPanel;
    }

    private JPanel createTitlePanel()
    {            
        JLabel titleLabel = new JLabel("move title:");
        titleLabel.setFont(IvanhoeUIConstants.BOLD_FONT);
		this.titleEdit = new JTextField(15);
		this.titleEdit.setFont(IvanhoeUIConstants.SMALL_FONT);
		
        JPanel titleFieldPanel = new JPanel();
        titleFieldPanel.setBorder(new EmptyBorder(10,0,0,0));
        titleFieldPanel.setLayout(new FlowLayout());
        titleFieldPanel.add(titleLabel);
		titleFieldPanel.add(this.titleEdit);
		
		return titleFieldPanel;
    }
    
    public String getMoveTitle()
    {
        return titleEdit.getText();
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardStepPanel#panelSelected()
     */
    public void panelSelected()
    {
        // TODO Auto-generated method stub
        
    }
}
