/*
 * Created on Dec 9, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.publish;

import java.awt.Dimension;
import java.util.LinkedList;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.CurrentMove;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTimeline;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.CategoryManager;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.Journal;
import edu.virginia.speclab.ivanhoe.client.game.view.IvanhoeFrame;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardDialog;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.Category;

/**
 * @author Nick
 *  
 */
public class MovePublishWizard extends WizardDialog 
{
    private CurrentMove currentMove;
    //private MoveTitlePanel moveTitlePanel;
    private MoveRationalePanel moveRationalePanel;
    private MoveJournalPanel moveJournalPanel;
    private MoveInspirationPanel moveInspirationPanel;
    
    // test this dialog
    public static void main( String agrs[] )
    {
        // init system
        Ivanhoe.initLogging();
        Ivanhoe.installLookNFeel();
        
        // create empty data
        DiscourseField df = new DiscourseField(null);
        CurrentMove move = new CurrentMove(df);
        Journal journal = new Journal();
        
        LinkedList catList = new LinkedList();
        for( int i=1; i<5; i++ )
        {
            Category cat = new Category(i,"item"+i,"description"+i);
            catList.add(cat);
        }
        
        // load with test data
        CategoryManager categoryManager = new CategoryManager(catList);
        
        DiscourseFieldTimeline timeline = df.getDiscourseFieldTimeline();
        
        // start wizard
        MovePublishWizard wizard = new MovePublishWizard(null,move,journal,categoryManager,timeline);
        wizard.startWizard();
        System.exit(0);
    }
    
    public MovePublishWizard( IvanhoeFrame frame, CurrentMove currentMove, Journal journal, CategoryManager categoryManager, DiscourseFieldTimeline timeline )
    {
        super( frame, "move publication", new Dimension(400,400));

        this.currentMove = currentMove;
        
        //moveTitlePanel = new MoveTitlePanel(this);
        moveRationalePanel = new MoveRationalePanel(this,currentMove);       
        moveJournalPanel = new MoveJournalPanel(this,journal);
        moveInspirationPanel = new MoveInspirationPanel(this,timeline);
        MoveSummaryPanel moveSummaryPanel = new MoveSummaryPanel(this, currentMove, moveInspirationPanel);
        
        //this.addStepPanel(moveTitlePanel);
        this.addStepPanel(moveJournalPanel);
        // removed this step since we aren't going to do the move centric views
        //this.addStepPanel(moveInspirationPanel);
        this.addStepPanel(moveRationalePanel);                                
        this.addStepPanel(moveSummaryPanel);
                
        this.setFinishButtonText("Publish");
        
        if( currentMove.getDescription().length() == 0 )
        {
        	this.setWizardCanFinish(false);	
        }
        
        setResizable(false);
    }
         
    protected void wizardFinished()
    {
        try
        {
            // obtain the rationale text 
    	    String narrative = moveRationalePanel.getText();
    	    SimpleLogger.logInfo("rationale: " + narrative);	    
    
    	    // save the journal
    	    moveJournalPanel.save();
    	    
            // publish the move
    	    currentMove.setDescription( narrative );
    	    currentMove.setInpiration( moveInspirationPanel.getInspirations() );
    	    
            if( currentMove.submit() == false )
            {
                Ivanhoe.showErrorMessage("Error submitting move, please email ivanhoe@nines.org.");                
            }
            
    	    dispose();
        }
        catch( Exception e )
        {        
            // if something bad happens, let us know
            Ivanhoe.sendExceptionToHost(e);            
        }
    }

    protected void wizardCancelled()
    {
	    // save the journal
        moveJournalPanel.save();
        dispose();
    }

}