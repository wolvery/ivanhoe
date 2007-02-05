/*
 * Created on Jan 4, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay;

import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import edu.virginia.speclab.ivanhoe.client.game.model.metagame.ReferenceResourceManager;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.Journal;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeStyleInternalFrame;
import edu.virginia.speclab.ivanhoe.client.util.PropertiesManager;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;

/**
 * @author Nick
 *
 */

public class RolePlayWindow extends IvanhoeStyleInternalFrame 
{
    private JournalPanel journalPanel;
    private AbstractRolePanel rolePanel;
    private PlaySpacePanel playSpacePanel;
    private ResourcesPanel resourcesPanel;
    
    public RolePlayWindow( Journal journal, GameInfo gameInfo, PropertiesManager propertiesManager, 
            		       RoleManager roleManager, ReferenceResourceManager bookmarkManager )
    {
        super("roleplay");
        
        journalPanel = new JournalPanel(journal);
        journalPanel.setBorder( new EmptyBorder( 10,10,10,10 ));
        rolePanel = new EditableRolePanel(roleManager, roleManager.getCurrentRole(), null);
        playSpacePanel = new PlaySpacePanel(gameInfo,propertiesManager);
        resourcesPanel = new ResourcesPanel(bookmarkManager);
        
        JTabbedPane tabPane = new JTabbedPane();
        
        tabPane.addTab("role",rolePanel);
        tabPane.addTab("journal",journalPanel);
        tabPane.addTab("resources",resourcesPanel);
        tabPane.addTab("playspace",playSpacePanel);
        
        getContentPane().add(tabPane);
        
        // listen for editor close events
        addInternalFrameListener(new InternalFrameAdapter()
        {
           public void internalFrameClosing(InternalFrameEvent e)
           {
              handleClose();
           }         
        });      

    }
    
    public void save()
    {
        // send updated role to the server
        rolePanel.acceptRole();
        
        // update journal
        journalPanel.save();               
    }
    
    private void handleClose()    
    {
        save();
    }

}
