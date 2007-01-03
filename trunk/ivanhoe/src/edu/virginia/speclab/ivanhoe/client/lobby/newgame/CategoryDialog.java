/*
 * Created on Jan 25, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.lobby.newgame;

import javax.swing.JDialog;
import javax.swing.SpringLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.Box;
import java.awt.Font;
import javax.swing.border.LineBorder;

import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.ILengthReportingListener;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.LengthReportingTextField;
import edu.virginia.speclab.ivanhoe.shared.data.Category;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author Nick
 *
 */
public class CategoryDialog extends JDialog
{
    private boolean ok;
    private Category category;
    
    private JTextArea descriptionField;
    private LengthReportingTextField nameField;
    private SpringLayout springLayout;
    private JButton cancelButton;
    private JButton okButton;
    
    // listens for changes in the length of the name field
    private class ToggleOKButton implements ILengthReportingListener
    {
        /* (non-Javadoc)
         * @see edu.virginia.speclab.ivanhoe.client.game.view.ui.ILengthReportingListener#lengthChanged(int)
         */
        public void lengthChanged(int length)
        {
            checkEntryLength(length);
        }
    }
    
   
    public CategoryDialog( Category oldCategory )
    {
        super();
        setResizable(false);

        createUI();

        this.category = new Category();
        
        if( oldCategory != null )
        {
            category.setName(oldCategory.getName());
            category.setDescription(oldCategory.getDescription());
            nameField.setText(oldCategory.getName());
            descriptionField.setText(oldCategory.getDescription());
        }
        
        checkEntryLength( nameField.getText().length() );
        
        Workspace.centerWindow(this);
    }
    
    private void createUI()
    {
        setTitle("create new category");
        springLayout = new SpringLayout();
        getContentPane().setLayout(springLayout);
        setModal(true);
        setName("");
        setBounds(100, 100, 393, 275);
        final JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
        getContentPane().add(namePanel);
        springLayout.putConstraint(SpringLayout.SOUTH, namePanel, 41, SpringLayout.NORTH, getContentPane());
        springLayout.putConstraint(SpringLayout.NORTH, namePanel, 21, SpringLayout.NORTH, getContentPane());
        springLayout.putConstraint(SpringLayout.EAST, namePanel, 363, SpringLayout.WEST, getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, namePanel, 17, SpringLayout.WEST, getContentPane());
        final JLabel label = new JLabel();
        label.setFont(new Font("Verdana", Font.PLAIN, 10));
        namePanel.add(label);
        label.setText("name");
        namePanel.add(Box.createHorizontalStrut(5));
        nameField = new LengthReportingTextField(15);
        nameField.setLengthReportingListener(new ToggleOKButton());
        nameField.setFont(new Font("Verdana", Font.PLAIN, 10));
        namePanel.add(nameField);
        final JLabel label_1 = new JLabel();
        label_1.setFont(new Font("Verdana", Font.PLAIN, 10));
        getContentPane().add(label_1);
        springLayout.putConstraint(SpringLayout.SOUTH, label_1, 65, SpringLayout.NORTH, getContentPane());
        springLayout.putConstraint(SpringLayout.EAST, label_1, 129, SpringLayout.WEST, getContentPane());
        springLayout.putConstraint(SpringLayout.NORTH, label_1, 50, SpringLayout.NORTH, getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, label_1, 15, SpringLayout.WEST, getContentPane());
        label_1.setText("description");
        descriptionField = new JTextArea();
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        descriptionField.setBorder(new LineBorder(Color.GRAY, 1, false));
        descriptionField.setFont(new Font("Verdana", Font.PLAIN, 10));
        getContentPane().add(descriptionField);
        springLayout.putConstraint(SpringLayout.SOUTH, descriptionField, 182, SpringLayout.NORTH, getContentPane());
        springLayout.putConstraint(SpringLayout.EAST, descriptionField, 364, SpringLayout.WEST, getContentPane());
        springLayout.putConstraint(SpringLayout.NORTH, descriptionField, 66, SpringLayout.NORTH, getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, descriptionField, 14, SpringLayout.WEST, getContentPane());
        final JPanel buttonPanel = new JPanel();
        getContentPane().add(buttonPanel);
        springLayout.putConstraint(SpringLayout.SOUTH, buttonPanel, 230, SpringLayout.NORTH, getContentPane());
        springLayout.putConstraint(SpringLayout.EAST, buttonPanel, 377, SpringLayout.WEST, getContentPane());
        springLayout.putConstraint(SpringLayout.NORTH, buttonPanel, 196, SpringLayout.NORTH, getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, buttonPanel, 235, SpringLayout.WEST, getContentPane());
        okButton = new JButton();
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleOK();
            }
        });
        okButton.setFont(new Font("Verdana", Font.PLAIN, 10));
        buttonPanel.add(okButton);
        okButton.setText("ok");
        cancelButton = new JButton();
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleCancel();
            }
        });
        cancelButton.setFont(new Font("Verdana", Font.PLAIN, 10));
        buttonPanel.add(cancelButton);
        cancelButton.setText("cancel");
        //
    }
    
    
    public boolean isOK()
    {
        return ok;
    }
    
    private void handleOK() 
    {
        ok = true;
        dispose();
    }
    
    private void handleCancel() 
    {
        ok = false;
        dispose();
    }
    
    /**
     * @return Returns the category.
     */
    public Category getCategory()
    {
        category.setName( nameField.getText() );
        category.setDescription( descriptionField.getText() );
        
        return category;
    }
    
    private void checkEntryLength( int count )
    {
        if( okButton != null )
        {
            if( count > 0 )
            {
                okButton.setEnabled(true);
            }
            else
            {
                okButton.setEnabled(false);
            }
        }
    }
}
