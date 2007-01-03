/*
 * Created on Dec 8, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.util.PropertiesManager;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;
import edu.virginia.speclab.ivanhoe.shared.data.Role;

/**
 * @author Nick
 * 
 * This dialog displays the welcome message when a playewr first logs into
 * a game of ivanhoe. It tells the user what role they are currently playing
 * and information about the game. 
 *
 */
public class WelcomeDialog extends JDialog implements ActionListener
{
    private JButton okButton;
    private JCheckBox dontBugCheck;
    private PropertiesManager propertiesManager;
    private Role playerRole;
    
    public WelcomeDialog( JFrame frame, GameInfo gameInfo, Role playerRole, PropertiesManager propertiesManager )
    {                         
        super(frame);
        
        this.propertiesManager = propertiesManager;
        this.playerRole = playerRole;
        
        this.setModal(true);  
        setResizable(true);
        setTitle("game information");
        getContentPane().setLayout( new BorderLayout() );
        
        JPanel controlBar = createControlBar();
        JScrollPane summaryPanel = createSummaryPanel(gameInfo);

        getContentPane().add(summaryPanel,BorderLayout.CENTER);
        getContentPane().add(controlBar, BorderLayout.SOUTH );
        
        setSize(600,500);
        setLocationRelativeTo(null);                       
    }
    
    private JPanel createControlBar()
    {
        JPanel controlBar = new JPanel();
        controlBar.setLayout(new BorderLayout());                
        controlBar.setBorder(new EmptyBorder(5,10,5,10));
        
        okButton = new JButton("continue >");
        okButton.setFont(IvanhoeUIConstants.SMALL_FONT);
        this.okButton.addActionListener(this);
        
        dontBugCheck = new JCheckBox("skip this dialog next time.");
        dontBugCheck.setFont(IvanhoeUIConstants.SMALL_FONT);
        dontBugCheck.setSelected(false);
        
        controlBar.add(dontBugCheck,BorderLayout.WEST);
        controlBar.add(okButton,BorderLayout.EAST);
        
        return controlBar;
    }
    
    private JScrollPane createSummaryPanel(GameInfo gameInfo)
    {
        JEditorPane welcomePane = new JEditorPane();
        welcomePane.setEditable(false);
        welcomePane.setContentType("text/html");
        welcomePane.setMargin(new Insets(10,10,10,10));

        String welcomeMessage = generateWelcomeMessage(gameInfo);
        welcomePane.setText(welcomeMessage);
        
        JScrollPane sp = new JScrollPane( welcomePane );
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        Border bufferBorder = new EmptyBorder(10,10,5,10);
        Border lineBorder = LineBorder.createGrayLineBorder();
        sp.setBorder(new CompoundBorder(bufferBorder,lineBorder));
        
        return sp;
    }
    
    /**
     * Generates the welcome message.
     */
    private String generateWelcomeMessage(GameInfo gameInfo)
    {
       StringBuffer txt = new StringBuffer();
       txt.append("<html><body style=font-size:20 font-family: Verdana><b>Welcome to Ivanhoe!</b>");

       txt.append("<p style=font-size:12 font-family: Verdana>" );
       		
       if ( playerRole != null)
       {
          txt.append("Your are currently playing the role ");
          txt.append("<b>"+playerRole.getName()+"</b> in the playspace ");
          txt.append("<b>"+gameInfo.getName()+"</b>.<br><br>");
       }
           
       txt.append("<u>Playspace Objectives:</u><br>");
       txt.append(gameInfo.getObjectives());
       txt.append("<br><br>");

       txt.append("<u>Your Objective:</u><br>");
       txt.append(playerRole.getObjectives());
       txt.append("<br><br>");
       
       txt.append("</p></body></html>");
       
       return txt.toString();
    }
    
    public void actionPerformed(ActionEvent e)
    {
       if (e.getSource().equals(this.okButton))
       {
             // disable welcome message if selected
           	 if( dontBugCheck.isSelected() )
           	 {
           	     propertiesManager.setProperty("welcome_message",false);
           	 }
           	 
             dispose();
       }
    }
    
}
