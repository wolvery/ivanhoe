/*
 * Created on Jan 24, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.lobby.newgame;

import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.JTextArea;

import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;

import java.awt.Font;

/**
 * @author Nick
 *
 */
public class FinishedPanel extends JPanel
{

    private JTextArea textArea_1;
    private JTextArea textArea;
    private SpringLayout springLayout;
    public FinishedPanel()
    {
        super();
        setSize(310, 143);
        springLayout = new SpringLayout();
        setLayout(springLayout);
        final JLabel label = new JLabel(ResourceHelper.instance.getIcon("res/images/ivan.gif"));
        label.setBackground(Color.WHITE);
        add(label);
        springLayout.putConstraint(SpringLayout.EAST, label, 78, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.SOUTH, label, 138, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.NORTH, label, 5, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, label, 5, SpringLayout.WEST, this);
        final JLabel label_1 = new JLabel();
        label_1.setFont(new Font("Verdana", Font.BOLD, 16));
        add(label_1);
        springLayout.putConstraint(SpringLayout.SOUTH, label_1, 27, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.EAST, label_1, 261, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.NORTH, label_1, 6, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, label_1, 89, SpringLayout.WEST, this);
        label_1.setText("Ready to Create");
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setFont(new Font("Verdana", Font.PLAIN, 10));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        add(textArea);
        springLayout.putConstraint(SpringLayout.SOUTH, textArea, 91, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.EAST, textArea, 294, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.NORTH, textArea, 46, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, textArea, 90, SpringLayout.WEST, this);
        textArea.setText("Ivanhoe is now ready to create the game you have designed.");
        textArea_1 = new JTextArea();
        textArea_1.setEditable(false);
        textArea_1.setOpaque(false);
        textArea_1.setLineWrap(true);
        textArea_1.setWrapStyleWord(true);
        textArea_1.setFont(new Font("Verdana", Font.PLAIN, 10));
        add(textArea_1);
        springLayout.putConstraint(SpringLayout.SOUTH, textArea_1, 153, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.EAST, textArea_1, 308, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.NORTH, textArea_1, 98, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, textArea_1, 91, SpringLayout.WEST, this);
        textArea_1.setText("Press create to add this game to the game list.");
        //
    }
}
