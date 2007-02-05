/*
 * Created on Jan 24, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.lobby.newgame;

import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.BoxLayout;
import javax.swing.Box;

import edu.virginia.speclab.ivanhoe.client.game.view.ui.IWizardStepPanel2;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.AbstractProxy;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.message.CreateGameCancelMsg;
import edu.virginia.speclab.ivanhoe.shared.message.CreateGameStartMsg;

/**
 * @author Nick
 *
 */
public class NamePanel extends JPanel implements IWizardStepPanel2
{
    private final JTextField textField;
    private final AbstractProxy proxy;
    private boolean createGameStarted;
    
    public NamePanel(AbstractProxy proxy)
    {
        super();
        this.proxy = proxy;
        this.createGameStarted = false;
        
        setSize(329, 215);
        SpringLayout springLayout = new SpringLayout();
        setLayout(springLayout);
        final JLabel ivanhoePicture = new JLabel(ResourceHelper.instance.getIcon("res/images/ivan.gif"));
        ivanhoePicture.setBackground(new Color(255, 255, 255));
        add(ivanhoePicture);
        springLayout.putConstraint(SpringLayout.EAST, ivanhoePicture, 77, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.SOUTH, ivanhoePicture, 148, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.NORTH, ivanhoePicture, 5, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, ivanhoePicture, 6, SpringLayout.WEST, this);
        final JLabel titleLabel = new JLabel();
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 16));
        titleLabel.setText("Create a New Game");
        add(titleLabel);
        springLayout.putConstraint(SpringLayout.SOUTH, titleLabel, 29, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.EAST, titleLabel, 295, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.NORTH, titleLabel, 7, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, titleLabel, 96, SpringLayout.WEST, this);
        final JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Verdana", Font.PLAIN, 10));
        textArea.setOpaque(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        add(textArea);
        springLayout.putConstraint(SpringLayout.SOUTH, textArea, 101, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.EAST, textArea, 327, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.NORTH, textArea, 44, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, textArea, 98, SpringLayout.WEST, this);
        textArea.setText("This wizard will guide you through the process of creating a new game.");
        final JTextArea textArea_1 = new JTextArea();
        textArea_1.setEditable(false);
        textArea_1.setFont(new Font("Verdana", Font.PLAIN, 10));
        textArea_1.setOpaque(false);
        textArea_1.setLineWrap(true);
        textArea_1.setWrapStyleWord(true);
        add(textArea_1);
        springLayout.putConstraint(SpringLayout.SOUTH, textArea_1, 150, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.EAST, textArea_1, 327, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.NORTH, textArea_1, 94, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, textArea_1, 96, SpringLayout.WEST, this);
        textArea_1.setText("The first step is to give your game a unique name. Please enter a name below and press next.");
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        add(panel);
        springLayout.putConstraint(SpringLayout.SOUTH, panel, 203, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.EAST, panel, 323, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.NORTH, panel, 178, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, panel, 95, SpringLayout.WEST, this);
        final JLabel nameLabel = new JLabel();
        panel.add(nameLabel);
        nameLabel.setText("game name:");
        panel.add(Box.createHorizontalStrut(5));
        textField = new JTextField();
        panel.add(textField);
        //
    }
    
    public String getGameName()
    {
        return textField.getText();
    }

    public void nextStep()
    {
        if (proxy != null)
        {
            proxy.sendMessage(new CreateGameStartMsg(getGameName()));
            createGameStarted = true;
        }
        else
        {
            SimpleLogger.logError("NamePanel has null proxy and cannot send CreateGameStartMsg");
        }
    }
    
    public void prevStep() {}
    public void cancel()
    {
        if (proxy != null)
        {
            if (createGameStarted)
            {
                proxy.sendMessage(new CreateGameCancelMsg(getGameName()));
                createGameStarted = false;
            }
        }
        else
        {
            SimpleLogger.logError("NamePanel has null proxy and cannot send CreateGameCancelMsg");
        }
    }
    public void finish() {}

    public void steppedInto()
    {
        if (proxy != null)
        {
            if (createGameStarted)
            {
                proxy.sendMessage(new CreateGameCancelMsg(getGameName()));
                createGameStarted = false;
            }
        }
    }
}
