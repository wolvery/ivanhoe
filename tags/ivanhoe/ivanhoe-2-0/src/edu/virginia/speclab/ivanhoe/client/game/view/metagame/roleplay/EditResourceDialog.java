/*
 * Created on Jan 5, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.IReferenceResourceListener;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.shared.data.ReferenceResource;


class EditResourceDialog extends JDialog implements IReferenceResourceListener
{
    private ReferenceResource reference;
    private boolean ok = false; 
    
    JTextField bookmarkLabel, bookmarkURL;
    JTextArea summaryField;

    public EditResourceDialog()
    {
        this( new ReferenceResource() );
    }
    
    public EditResourceDialog( ReferenceResource reference ) 
    {            
        this.reference = reference;
        
        setTitle("edit resource");
        setModal(true);
        setResizable(false);
        setSize(300,200);
        
        getContentPane().setLayout( new BoxLayout( getContentPane(), BoxLayout.Y_AXIS ));
        
        JPanel bookmarkPanel = createBookmarkPanel();        
        JPanel buttonPanel = createButtonPanel();                
        
        getContentPane().add(bookmarkPanel);
        getContentPane().add(buttonPanel);
        
        Workspace.instance.centerWindowOnWorkspace(this);
    }
    
    private JPanel createBookmarkPanel()
    {
        JPanel labelPnl = new JPanel();
        labelPnl.setLayout(new BorderLayout());
        labelPnl.setBorder(new EmptyBorder(5,5,5,5));
        JLabel labelLabel = new JLabel("title:");        
        labelLabel.setFont(IvanhoeUIConstants.BOLD_FONT);        
        labelPnl.add(labelLabel, BorderLayout.WEST);
        
        this.bookmarkLabel = new JTextField(20);
        this.bookmarkLabel.setText(reference.getLabel());
        bookmarkLabel.setText(reference.getLabel());
        bookmarkLabel.setFont(IvanhoeUIConstants.SMALL_FONT);
        labelPnl.add(this.bookmarkLabel);

        // url panel - SOUTH
        JPanel urlPnl = new JPanel();                   
        JLabel urlLabel = new JLabel("url:");
        urlPnl.setLayout(new BorderLayout());
        urlPnl.setBorder(new EmptyBorder(5,5,5,5));
        urlLabel.setFont(IvanhoeUIConstants.BOLD_FONT);
        urlPnl.add(urlLabel, BorderLayout.WEST);
        this.bookmarkURL = new JTextField(20);
        
        
        if( reference.getUrl() == null )
        {
            this.bookmarkURL.setText("http://");    
        }
        else
        {
            this.bookmarkURL.setText(reference.getUrl());    
        }
        
        this.bookmarkURL.setFont(IvanhoeUIConstants.SMALL_FONT);
        urlPnl.add(this.bookmarkURL);

        JPanel summaryPnl = new JPanel();
        summaryPnl.setLayout( new BorderLayout() );
        JLabel summaryLabel = new JLabel("summary:");
        summaryLabel.setFont(IvanhoeUIConstants.BOLD_FONT);        
        summaryField = new JTextArea();
        summaryField.setText(this.reference.getSummary());
        summaryField.setBorder( LineBorder.createGrayLineBorder() );
        summaryField.setFont(IvanhoeUIConstants.SMALL_FONT);
        summaryField.setWrapStyleWord(true);
        summaryField.setLineWrap(true);        
        summaryPnl.add(summaryLabel, BorderLayout.NORTH );
        summaryPnl.add(summaryField, BorderLayout.CENTER);
        summaryPnl.setBorder( new EmptyBorder(5,5,5,5) );
                

        JPanel bookmarkPanel = new JPanel();
        bookmarkPanel.setLayout(new BorderLayout());

        JPanel northPnl = new JPanel();
        northPnl.setLayout(new BorderLayout());
        
        northPnl.add(labelPnl, BorderLayout.NORTH);
        northPnl.add(urlPnl, BorderLayout.SOUTH);
        bookmarkPanel.add(northPnl, BorderLayout.NORTH);
        bookmarkPanel.add(summaryPnl, BorderLayout.CENTER);
        
        return bookmarkPanel;
    }
    
    private JPanel createButtonPanel()
    {
       JPanel btns = new JPanel();
       
       JButton okButton = new JButton("ok");
       okButton.setFont(IvanhoeUIConstants.SMALL_FONT);

       okButton.addActionListener( new ActionListener() {
           public void actionPerformed(ActionEvent e)
           {
               handleOK();
           } });
       
       btns.add(okButton);

       JButton cancelButton = new JButton("cancel");
       cancelButton.setFont(IvanhoeUIConstants.SMALL_FONT);
       cancelButton.addActionListener( new ActionListener() {
           public void actionPerformed(ActionEvent e)
           {
               handleCancel();
           } });
       btns.add(cancelButton);
       
       return btns;
    }
    
    // test this dialog
    public static void main( String args[] )
    {
        Ivanhoe.installLookNFeel();
        EditResourceDialog dialog = new EditResourceDialog();
        dialog.show();
        System.exit(0);
    }

    private void handleCancel()
    {
        ok = false;
        dispose();
    }
    
    private void handleOK()
    {
        ok = true;
        reference.setLabel(this.bookmarkLabel.getText());
        reference.setUrl(this.bookmarkURL.getText());
        reference.setSummary(this.summaryField.getText());
        dispose();
    }
    
    public ReferenceResource getReferenceResource()
    {
        return reference;
    }
    
    /**
     * @return Returns the ok.
     */
    public boolean isOk()
    {
        return ok;
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.model.metagame.IReferenceResourceListener#resourceChanged()
     */
    public void resourceChanged()
    {
        // if the resource list changed, disregard this edit
        handleCancel();        
    }
}