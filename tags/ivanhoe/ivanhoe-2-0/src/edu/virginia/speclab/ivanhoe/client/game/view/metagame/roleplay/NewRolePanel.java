/*
 * Created on Jan 6, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import edu.virginia.speclab.ivanhoe.client.game.model.metagame.NewRoleColorsModel;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.NewRoleTransaction;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.NewRoleTransactionListener;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.ColorPair;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.IColorModelUpdateListener;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.TextLimit;

/**
 * @author benc
 */

class NewRolePanel extends AbstractRolePanel
{
    public static final String NEW_ROLE_TEXT = "create this role";
    public static final String TITLE = "<create new role>";
    private static final int MAX_ROLE_NAME_SIZE = 50;
    
    protected JTextField nameInput;
    protected NewRoleTransactionListener newRoleListener;
    protected NewRoleColorsModel newRoleColorsModel;
    
    public NewRolePanel(RoleManager manager, ActionListener okButtonListener,
            NewRoleTransactionListener newRoleListener)
    {
        super(manager, true, okButtonListener);
        this.newRoleListener = newRoleListener;
        this.newRoleColorsModel = new NewRoleColorsModel();
        
        initUI();

        manager.addColorUpdateListener(getColorModelUpdateListener());
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                Workspace.instance.getNavigator().getRoleManager()
                		.removeColorUpdateListener(getColorModelUpdateListener());
            }
        });
        

        
        this.okButton.setText(NEW_ROLE_TEXT);
        nameInput = new JTextField(MAX_ROLE_NAME_SIZE);
        nameInput.setBorder(new LineBorder(Color.GRAY, 1));
        nameInput.addKeyListener(new TextLimit(MAX_ROLE_NAME_SIZE));
        
        this.namePanel.add(nameInput);
    }
    
    protected void acceptRole()
    {
        ColorPair newPCColors = newRoleColorsModel.getPlayerCircleColors();
        new NewRoleTransaction(
                nameInput.getText(),
                super.roleDescription.getText(),
                super.roleObjective.getText(), 
    			newPCColors.strokeColor, newPCColors.fillColor,
    			this.manager,newRoleListener);

    }
    
    protected IColorModelUpdateListener getColorModelUpdateListener()
    {
        return newRoleColorsModel;
    }
    
    protected ColorPair getRoleColors()
    {
        return newRoleColorsModel.getPlayerCircleColors();
    }

    protected boolean roleMatches(String roleName)
    {
        return (roleName == null);
    }

    protected String getRoleName()
    {
        return null;
    }
    
    protected void removeListeners()
    {
        manager.removeColorUpdateListener(getColorModelUpdateListener());
        super.removeListeners();
    }
}