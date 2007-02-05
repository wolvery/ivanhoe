/*
 * Created on Jan 4, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.util.PropertiesManager;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;

/**
 * @author Nick
 *  
 */
public class PlaySpacePanel extends JPanel
{
    private final PropertiesManager propertiesManager;
    private JTextArea objectiveDisplay;
    private final GameInfo gameInfo;

    public PlaySpacePanel( GameInfo gameInfo, PropertiesManager propertiesManager )
    {
        this.gameInfo = gameInfo;
        this.propertiesManager = propertiesManager;
        
        initUI();
        if( gameInfo != null ) 
            objectiveDisplay.setText( gameInfo.getObjectives());
    }
    
    private void initUI()
    {
        setLayout(new BorderLayout());
        
        JPanel objectivePanel = new JPanel( new BorderLayout() );
                
        this.objectiveDisplay = new JTextArea();
        this.objectiveDisplay.setEditable(false);
        this.objectiveDisplay.setWrapStyleWord(true);
        this.objectiveDisplay.setLineWrap(true);
        this.objectiveDisplay.setMargin(new Insets(2, 8, 2, 8));
        JScrollPane objSp = new JScrollPane(this.objectiveDisplay);
        objSp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        objectivePanel.add(objSp, BorderLayout.CENTER);        

        WelcomeMessageCheckMark checkMark = new WelcomeMessageCheckMark();
        checkMark.setBorder(new EmptyBorder(2, 10, 2, 0));

        add(getTitleBox(), BorderLayout.NORTH);
        add(objectivePanel, BorderLayout.CENTER);
        add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        add(checkMark, BorderLayout.SOUTH);
        setBorder( new EmptyBorder( 10,10,10,10 ));
    }
  
    private JPanel getTitleBox()
    {
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.LINE_AXIS));
        JLabel titleLabel = new JLabel("game title: ");
        JLabel title = new JLabel(gameInfo.getName());
        titlePanel.add(titleLabel);
        titlePanel.add(title);
        return titlePanel;
    }
    
    private class WelcomeMessageCheckMark extends JCheckBox implements ActionListener
    {       
        public WelcomeMessageCheckMark()
        {
            super("display welcome message when entering game.");
            this.addActionListener(this);
            this.setFont(IvanhoeUIConstants.TINY_FONT);
            
            // set the check according to the current setting in properties
            String welcomeString = propertiesManager.getProperty("welcome_message");           
            boolean welcomeFlag = Boolean.valueOf(welcomeString).booleanValue();           
            this.setSelected(welcomeFlag);           
        }

     /* (non-Javadoc)
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     public void actionPerformed(ActionEvent e)
     {
         propertiesManager.setProperty("welcome_message", this.isSelected() );
     }       
    }
}