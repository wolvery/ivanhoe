/*
 * Created on Jan 7, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.lobby.newgame;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.cache.DocumentFilter;
import edu.virginia.speclab.ivanhoe.client.game.model.cache.DocumentImporter;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.document.DocumentInfoDialog;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IWizardStepPanel2;
import edu.virginia.speclab.ivanhoe.shared.IvanhoeException;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.message.DeleteDocumentMsg;

/**
 * @author Nick
 *
 */
class DocumentPanel extends JPanel implements ActionListener, IWizardStepPanel2
{
	private List documentList;
	private DocumentImporter importer;
	private JList docListDisplay;
	private DefaultListModel docListModel;
	private JButton addDocBtn;
	private JButton delDocBtn;
    private JFrame parentFrame;
	
	private final String panelText = "Select the starting documents for the game.";
    
    public DocumentPanel( String workingDirectory, JFrame parentFrame, DiscourseField discourseField )
    {
        
		// create document members
		this.documentList = new ArrayList();
		this.importer = new DocumentImporter(workingDirectory, discourseField);
        this.parentFrame = parentFrame;

		setLayout( new BorderLayout() );		
		
		JLabel info = new JLabel(panelText);
		info.setFont(IvanhoeUIConstants.SMALL_FONT);
		
		this.docListModel = new DefaultListModel();
		this.docListDisplay = new JList(this.docListModel);
		JScrollPane sp = new JScrollPane(this.docListDisplay);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		add(sp, BorderLayout.CENTER);
		add(info, BorderLayout.NORTH);
		
		JPanel boo = new JPanel();
		boo.setLayout(new BoxLayout(boo, BoxLayout.Y_AXIS));
		this.delDocBtn = new JButton("remove");
		this.delDocBtn.setFont(IvanhoeUIConstants.SMALL_FONT);
		this.delDocBtn.addActionListener(this);		
		this.addDocBtn = new JButton("add...");
		this.addDocBtn.setFont(IvanhoeUIConstants.SMALL_FONT);
		this.addDocBtn.addActionListener(this);
		boo.add(Box.createVerticalGlue());
		boo.add(this.addDocBtn);
		boo.add(this.delDocBtn);
		add(boo, BorderLayout.EAST);
		
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardStepPanel#panelSelected()
     */
    public void panelSelected()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
		if (e.getSource().equals(this.addDocBtn))
		{
			handleAddDocument();
		}
		else if (e.getSource().equals(this.delDocBtn))
		{
			handleDeleteDocument();
		}
    }
    
    /**
	 * add a new document to the list
	 */
	private void handleAddDocument()
	{
		JFileChooser dlg = new JFileChooser(System.getProperty("user.dir"));
		dlg.addChoosableFileFilter(
				new DocumentFilter(".gif", "GIF Images (*.gif)"));
		dlg.addChoosableFileFilter(
				new DocumentFilter(".jpg", "JPEG Images (*.jpg)"));
		dlg.addChoosableFileFilter(
				new DocumentFilter(".txt", "Text Documents (*.txt)"));
//		dlg.addChoosableFileFilter(
//				new DocumentFilter(".html", "HTML Documents (*.html)"));
		
		/**
		 * The following instruction fails on Mac OS X 10.2.x,
		 * which has a Java bug.  Later version of Mac OS work fine.
		 */
		if (System.getProperty("mrj.version") == null)
		{
			dlg.setAcceptAllFileFilterUsed(false);
		}
		
		int result = dlg.showDialog(parentFrame, "Add Document");
		if (result == JFileChooser.APPROVE_OPTION )
		{
            String selectedFile = dlg.getSelectedFile().getName();
			DocumentInfoDialog infoDlg = DocumentInfoDialog.createDialog( parentFrame, selectedFile );
			Workspace.centerWindow(infoDlg);
			infoDlg.show();
			
			// grab new document stuff
			File newFile = dlg.getSelectedFile();
			
			int contributorID = Role.GAME_CREATOR_ROLE_ID;
			
			DocumentInfo docInfo = new DocumentInfo(infoDlg.getFileName(),
					infoDlg.getDocumentTitle(), infoDlg.getAuthor(), infoDlg.getSource(),
					Ivanhoe.getProxy().getUserName(), contributorID,
					infoDlg.getCreateDate());
			docInfo.setStartingDocument(true);
			docInfo.setPublishedDocument(true);
            
            if( !isUniqueTitle(docInfo.getTitle()) )
            {
                JOptionPane.showMessageDialog(this, 
                        "<html><b>A document with this title already exists.</b><br>", 
                        "Cannot Add Document, Duplicate Title.", JOptionPane.ERROR_MESSAGE);
            }
            else if ( !isUniqueFilename(docInfo.getFileName()))
            {
                JOptionPane.showMessageDialog(this, 
                        "<html><b>A document with this filename already "
                        + "exists.  If you still want to add this document, "
                        + "change the filename and resubmit.</b><br>", 
                        "Cannot Add Document, Duplicate Filename.", JOptionPane.ERROR_MESSAGE);
                
            }
            else
            {
                importFile( docInfo, newFile );
            }
		}
	}
    
    private void importFile( DocumentInfo docInfo, File newFile )
    {
        try
        {
            // import it
            File importedFile = this.importer.importDocument(docInfo, newFile);
            
            // add it to lists
            this.documentList.add(new NewDocumentRecord(importedFile, docInfo));
            this.docListModel.addElement(docInfo.getTitle());
        }
        catch (IvanhoeException e)
        {
            JOptionPane.showMessageDialog(this, 
                    "<html><b>Unable to add document to game</b><br>Reason: " + e.toString(), 
                    "Create Game Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean isUniqueTitle( String title )
    {
        for( Iterator i = this.documentList.iterator(); i.hasNext(); )
        {
            NewDocumentRecord docRecord = (NewDocumentRecord) i.next();
            
            if( docRecord.getDocumentInfo().getTitle().equals(title) == true )
            {
                // this title is already on the list
                return false;
            }            
        }
        
        return true;        
    }
    
    private boolean isUniqueFilename( String filename )
    {
        for (Iterator i=documentList.iterator(); i.hasNext(); )
        {
            NewDocumentRecord docRecord = (NewDocumentRecord) i.next();
            
            if( docRecord.getDocumentInfo().getFileName().equals(filename) == true )
            {
                // this title is already on the list
                return false;
            }            
        }
        return true;
    }

	/**
	 * Delete a document from the document list
	 */
	private void handleDeleteDocument()
	{
		int idx = this.docListDisplay.getSelectedIndex();
		if (idx > -1 )
		{
			String selName = (String)this.docListModel.elementAt(idx);
			for (Iterator itr = this.documentList.iterator(); itr.hasNext(); )
			{
			    NewDocumentRecord rec = (NewDocumentRecord)itr.next();
				if (rec.getDocumentInfo().getTitle().equals(selName))
				{
					SimpleLogger.logInfo("Removing document " + selName + " from game");
					
					// remove  doc from lists
					this.documentList.remove(rec);
					this.docListModel.removeElement(selName);
					
					// notify server & delete file
					Ivanhoe.getProxy().sendMessage( new DeleteDocumentMsg(rec.getDocumentInfo()) );
					rec.getSourceFile().delete(); 
					break;
				}
			}
		}
	}    
	/**
	 * @return Returns the documentList.
	 */
	public List getDocumentList()
	{
	    return documentList;
	}
	
	public void removeDocument( String docName )
	{
	    docListModel.removeElement(docName);
	}
	
	public int getStartingDocPriv()
	{
	    //return Integer.parseInt(this.startDocPrivilege.getText());
	    return 1;
	}

    public void nextStep() {}
    public void prevStep()
    {
        documentList.clear();
        docListModel.clear();
    }

    public void steppedInto() {}
    public void cancel() {}
    public void finish()
    {
        // TODO: don't upload anything 'til you go to create the game, then
        // upload from here and throw up a progress bar.
    }

}
