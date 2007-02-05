/*
 * Created on Jan 7, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.lobby.newgame;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.shared.AbstractProxy;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.message.CreateGameResponseMsg;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.RefreshLobbyMsg;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardDialog2;

/**
 * @author Nick
 *
 */
public class NewGameWizard extends WizardDialog2
        implements INewGameTransactionListener, IMessageHandler
{
    private NamePanel namePanel;
    private DescriptionPanel descriptionPanel;
    private DocumentPanel documentPanel;
    private RolePanel rolePanel;
    private PlayerAccessPanel playerAccessPanel;
    
    private NewGameTransaction newGameTransaction; 
    
    public NewGameWizard( JFrame parentFrame, String workingDirectory, AbstractProxy proxy )
    {
        super(parentFrame,"create new game");

        namePanel = new NamePanel(proxy);
        descriptionPanel = new DescriptionPanel();
        documentPanel = new DocumentPanel(workingDirectory,parentFrame, null);
        rolePanel = new RolePanel();
        playerAccessPanel = new PlayerAccessPanel();        
        FinishedPanel finishedPanel = new FinishedPanel();
        
        // add all the steps!                
        addStepPanel(namePanel);
        addStepPanel(documentPanel);        
        addStepPanel(descriptionPanel);
        addStepPanel(rolePanel);
        addStepPanel(playerAccessPanel);
        addStepPanel(finishedPanel);

        setSize(400,350);

        this.setFinishButtonText("create");
        //this.setWizardCanFinish(false);        
    }
    
    // test this dialog
    public static void main( String args[] )
    {
        Ivanhoe.initLogging();
        Ivanhoe.installLookNFeel();        
        NewGameWizard newGameWizard = new NewGameWizard(null,"",null);
        newGameWizard.startWizard();
        System.exit(0);
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardDialog#wizardCancelled()
     */
    protected void wizardCancelled() 
    {
        dispose();
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardDialog#wizardFinished()
     */
    protected void wizardFinished()
    {
        this.lockMovement(true);
        
        String description = descriptionPanel.getDescription();
        String objective = rolePanel.getObjective();
        String name = namePanel.getGameName();
        List docList = documentPanel.getDocumentList();
        int startingPriv = 1; 
        boolean restrictedAccess = playerAccessPanel.isRestricted();
        List accessList = playerAccessPanel.getAccessList();
        List categoryList = new LinkedList();
                
        newGameTransaction = new NewGameTransaction( name, startingPriv, description, objective, 
                									 docList, restrictedAccess, 
                									 accessList, categoryList, this );
        
        Ivanhoe.getProxy().sendMessage(new RefreshLobbyMsg());
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.lobby.newgame.INewGameTransactionListener#createFailed(int)
     */
    public void createFailed(int errorCode)
    {
        switch(errorCode)
        {
        	case NewGameTransaction.DESCRIPTION_BLANK:
        	    reportError("Please enter a description for this game.");
        	    break;
        	
        	case NewGameTransaction.NAME_BLANK:
        	    reportError("Please enter a name for this game.");
        	    break;
        	
        	case NewGameTransaction.DOC_PRIV_OUT_OF_RANGE:
        	    reportError("Privilege in visualization for starting documents must " +
    					     "be a number greater than 0.  Please enter a valid number.");
        	    break;
        	
        	case NewGameTransaction.GAME_NAME_TOO_LONG:
        	    reportError("Please enter a name that is less than 50 characters.");
        	    break;
        	
        	case NewGameTransaction.HOST_ERROR:
        	    reportError(newGameTransaction.getErrorText()); 
        	    break;
        	    
        	case NewGameTransaction.DOCUMENT_ERROR:
        	    String docName = newGameTransaction.getErrorText();
        		reportError( "Document Missing: "+docName );
        		documentPanel.removeDocument(docName);
        	    break;
                
            case NewGameTransaction.GAME_NAME_HAS_INVALID_CHARACTERS:
                reportError("Game names cannot contain question marks");
                break;
        }
        
        this.lockMovement(false);
    }
    
    private void reportError( String error )
    {
        JOptionPane.showMessageDialog(this,error,"Create Game Error", JOptionPane.ERROR_MESSAGE);
        SimpleLogger.logError(error);
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.lobby.newgame.INewGameTransactionListener#createSuccess()
     */
    public void createSuccess()
    {
        dispose();
    }

    public void handleMessage(Message msg)
    {
        if (msg.getType().equals(MessageType.CREATE_GAME_RESPONSE))
        {
            CreateGameResponseMsg cgrMsg = (CreateGameResponseMsg)msg;
            if (!cgrMsg.isSuccess())
            {
                reportError(cgrMsg.getMessage());
                this.wizardCancelled();
            }
        }
    }

}
