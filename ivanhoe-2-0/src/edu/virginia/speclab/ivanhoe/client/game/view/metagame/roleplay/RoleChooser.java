/*
 * Created on Dec 17, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.NewRoleTransactionListener;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.IColorModelUpdateListener;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeStyleInternalFrame;
import edu.virginia.speclab.ivanhoe.shared.data.Role;

/**
 * @author Nick
 * @author benc
 * Allows the player to select a role or choose to create a new
 * role.
 */
public class RoleChooser extends IvanhoeStyleInternalFrame implements ActionListener, NewRoleTransactionListener
{
    private RoleManager roleManager;
    private JTabbedPane roleTabPane;
    private Role selectedRole;
    private LinkedList listeners;
    private boolean roleSelected;

    public RoleChooser(RoleManager roleManager)
    {        
        super("select a role");
        this.roleManager = roleManager;
        this.listeners = new LinkedList();
        this.roleSelected = false;

        this.getContentPane().add(createUI());
        
        InternalFrameListener closeListener = new InternalFrameListener()
        {
            public void internalFrameActivated(InternalFrameEvent e) {}
            public void internalFrameClosed(InternalFrameEvent e) 
            {
                for (int i=0; i < roleTabPane.getTabCount(); ++i)
                {
                    ((AbstractRolePanel)roleTabPane.getComponentAt(i)).removeListeners();
                }
            }
            public void internalFrameClosing(InternalFrameEvent e) {}
            public void internalFrameDeactivated(InternalFrameEvent e) {}
            public void internalFrameDeiconified(InternalFrameEvent e) {}
            public void internalFrameIconified(InternalFrameEvent e) {}
            public void internalFrameOpened(InternalFrameEvent e) {}
        };
        
        this.addInternalFrameListener(closeListener);
        
        this.setResizable(false);
        this.pack();
    }
    
    private JComponent createUI()
    {
        roleTabPane = new JTabbedPane();
        roleTabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        roleTabPane.setBorder(new EmptyBorder(0,0,0,0));

        List roleList = roleManager.getCurrentPlayerRoleList();
        for (Iterator i=roleList.iterator(); i.hasNext(); )
        {
            Role role = (Role)i.next();
            RolePanel rolePanel = new RolePanel(this.roleManager, role, this);
            rolePanel.setChoiceListener(this);
            roleTabPane.addTab(role.getName(), rolePanel);
        }
        roleTabPane.addTab(NewRolePanel.TITLE, new NewRolePanel(this.roleManager, this, this));
   
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new LineBorder(Color.GRAY, 1));
        mainPanel.add(roleTabPane);
        
        JPanel spacerPanel = new JPanel();
        spacerPanel.setBorder(new EmptyBorder(0,0,0,0));
        spacerPanel.add(mainPanel);
       
        
        return spacerPanel;
    }

    /**
     * @return Returns the selectedRole.
     */
    public Role getSelectedRole()
    {
        return selectedRole;
    }
    
    public void actionPerformed(ActionEvent actEvent)
    {
        if (AbstractRolePanel.USE_ROLE_TEXT.equals(actEvent.getActionCommand()))
        {
            this.roleSelected = true;
            this.dispose();
        }
        else if (NewRolePanel.NEW_ROLE_TEXT.equals(actEvent.getActionCommand()))
        {
            this.setEnabled(false);
        }
    }
    
    public void newRoleCreated(Role newRole)
    {
        this.setEnabled(true);
        int insertPoint = roleTabPane.getTabCount() - 1;
        
        roleTabPane.remove(insertPoint);
        
        RolePanel rolePanel = new RolePanel(roleManager, newRole, this);
        rolePanel.setChoiceListener(this);
        
        roleTabPane.insertTab(newRole.getName(), null,
                rolePanel,
                null, insertPoint);
        roleTabPane.setSelectedIndex(insertPoint);
        
        roleTabPane.addTab(NewRolePanel.TITLE,
                new NewRolePanel(roleManager, this, this));
    }
    
    public void newRoleNotCreated(String roleMessage)
    {
        this.setEnabled(true);
        Ivanhoe.showErrorMessage("Problem creating role", roleMessage);
    }
    
    public void addListener(IRoleChooserListener listener)
    {
        if (!listeners.contains(listener)) listeners.add(listener);
    }
    
    public boolean removeListener(IRoleChooserListener listener)
    {
        return listeners.remove(listener);
    }
    
    public void fireRoleChosen(Role chosenRole)
    {
        for (Iterator i=listeners.iterator(); i.hasNext(); )
        {
            ((IRoleChooserListener)i.next()).roleChosen(chosenRole);
        }
    }
    
    public boolean isRoleSelected()
    {
        return roleSelected;
    }
    
    public void removeColorUpdateListeners()
    {
        for (int i=0; i < roleTabPane.getTabCount(); ++i)
        {
            this.roleManager.removeColorUpdateListener(
                    (IColorModelUpdateListener)roleTabPane.getComponentAt(i));
        }
    }
}