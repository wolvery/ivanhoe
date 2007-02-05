/*
 * Created on Jan 10, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.IReferenceResourceListener;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.ReferenceResourceManager;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.shared.data.ReferenceResource;

/**
 * @author Nick
 *
 */
public class ManageResourcesDialog extends JDialog implements IReferenceResourceListener
{
    private ReferenceResourceManager referenceResourceManager;
    private LinkedList referenceList;
    private JList referenceListControl;
    
    public ManageResourcesDialog( ReferenceResourceManager referenceResourceManager )
    {
        this.referenceResourceManager = referenceResourceManager;
        referenceResourceManager.addListener(this);
        
        initReferenceList(referenceResourceManager.getReferences());
        
        setTitle("edit resource list");
        setModal(true);
        
        createUI();
     	setSize(300,300);     	
        Workspace.instance.centerWindowOnWorkspace(this);
    }

    /**
     * @param references
     */
    private void initReferenceList(LinkedList references)
    {
        referenceList = new LinkedList();
        
        for( Iterator i = references.iterator(); i.hasNext(); )
        {
            ReferenceResource resource = (ReferenceResource) i.next();
            ReferenceResource newResource = resource.getCopy();
            referenceList.add(newResource);
        }
    }

    /**
     * 
     */
    private void createUI()
    {
        getContentPane().setLayout(new BorderLayout());
        JPanel centerPanel = createCenterPanel();
        JPanel buttonPanel = createButtonPanel();
        
        getContentPane().add( centerPanel, BorderLayout.CENTER );
        getContentPane().add( buttonPanel, BorderLayout.SOUTH );        
    }
    
    private JPanel createCenterPanel()
    {
        JPanel centerPanel = new JPanel();
        
        centerPanel.setBorder( new EmptyBorder(8,8,8,8) );
        centerPanel.setLayout( new BorderLayout() );
        
        JPanel referenceListPanel = createReferenceListPanel();
        JPanel referenceControlPanel = createReferenceControlPanel();
        
        centerPanel.add(referenceListPanel,BorderLayout.CENTER);
        centerPanel.add(referenceControlPanel, BorderLayout.EAST );
        
        return centerPanel;
    }

    /**
     * 
     */
    private JPanel createButtonPanel()
    {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder( new EmptyBorder(8,8,8,8));
        buttonPanel.setLayout( new BorderLayout() );
        
        JButton okButton = new JButton("ok");
        okButton.setFont(IvanhoeUIConstants.SMALL_FONT);
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                handleOK();
            }
        });
        
        JButton cancelButton = new JButton("cancel");        
        cancelButton.setFont(IvanhoeUIConstants.SMALL_FONT);
        
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                handleCancel();
            }
        });

        buttonPanel.add(okButton,BorderLayout.WEST);
        buttonPanel.add(cancelButton,BorderLayout.EAST);
        
        return buttonPanel;
    }
    
    private void handleOK()
    {
        referenceResourceManager.updateReferenceList(referenceList);        
        dispose();
    }
    
    private void handleCancel()
    {
        dispose();
    }

    /**
     * 
     */
    private JPanel createReferenceControlPanel()
    {
        JPanel referenceControlPanel = new JPanel();
                
        referenceControlPanel.setLayout( new BoxLayout( referenceControlPanel, BoxLayout.Y_AXIS ));
        
        JButton addReferenceButton = new JButton("add...");
        JButton removeReferenceButton = new JButton("remove");
        JButton editReferenceButton = new JButton("edit...");
        
        addReferenceButton.setFont(IvanhoeUIConstants.SMALL_FONT);
        removeReferenceButton.setFont(IvanhoeUIConstants.SMALL_FONT);
        editReferenceButton.setFont(IvanhoeUIConstants.SMALL_FONT);
        
        addReferenceButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                handleAddReference();
            } 
            });

        editReferenceButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                handleEditReference();
            } 
            });
        
        removeReferenceButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                handleRemoveReference();
            } 
            });

        referenceControlPanel.add(addReferenceButton);
        referenceControlPanel.add(editReferenceButton);
        referenceControlPanel.add(removeReferenceButton);

        return referenceControlPanel;
    }
    
    private void handleAddReference()
    {
        EditResourceDialog dialog = new EditResourceDialog();
        
        dialog.show();
        
        if( dialog.isOk() )
        {
            ReferenceResource reference = dialog.getReferenceResource();
            this.referenceList.add(reference);        
        }
        
        populateReferenceList();
    }
                        
    private void handleRemoveReference()
    {
        int selectedIndex = referenceListControl.getSelectedIndex();        
        referenceList.remove(selectedIndex);
        populateReferenceList();
    }
    
    private void handleEditReference()
    {
        int selectedIndex = referenceListControl.getSelectedIndex();
        ReferenceResource reference = (ReferenceResource) referenceList.get(selectedIndex);
        
        EditResourceDialog dialog = new EditResourceDialog(reference);
        
        // edit listens for changes to the list so it can abort if it changes
        referenceResourceManager.addListener(dialog);
        
        dialog.show();
        
        if( dialog.isOk() )
        {
            ReferenceResource newReference = dialog.getReferenceResource();
            reference.setLabel(newReference.getLabel());
            reference.setSummary(newReference.getSummary());
            reference.setUrl(newReference.getUrl());
        }
        
        populateReferenceList();
    }

    private JPanel createReferenceListPanel()
    {
        JPanel referenceListPanel = new JPanel();
        referenceListPanel.setBorder( new EmptyBorder(0,0,0,4));
        referenceListPanel.setLayout( new BorderLayout() );
        
        referenceListControl = new JList();
        referenceListControl.setFont(IvanhoeUIConstants.SMALL_FONT);
        referenceListControl.setBorder(LineBorder.createGrayLineBorder());        
        referenceListControl.addMouseListener( new ListDoubleClickListener() );        
        populateReferenceList();

        referenceListPanel.add(referenceListControl);
        
        return referenceListPanel;
    }
    
    private class ListDoubleClickListener extends MouseAdapter
    {
        public void mouseClicked(MouseEvent e)
        {
            if( e.getClickCount() >= 2 )
            {
                handleEditReference();
            }            
        }
    }
    
    private void populateReferenceList()
    {
        LinkedList labels = new LinkedList();
        
		// populate the list box
		for( Iterator i = this.referenceList.iterator(); i.hasNext(); )
		{
		    ReferenceResource ref = (ReferenceResource) i.next();            
		    labels.add(ref.getLabel());                   
		}
				
		referenceListControl.setListData(labels.toArray());		
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.model.metagame.IReferenceResourceListener#resourceChanged()
     */
    public void resourceChanged()
    {
        // update the reference list 
        initReferenceList(referenceResourceManager.getReferences());     
        populateReferenceList();
    }
   
}
