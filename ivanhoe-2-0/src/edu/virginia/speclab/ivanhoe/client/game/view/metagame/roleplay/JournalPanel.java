/*
 * Created on Dec 9, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.html.HTMLEditorKit;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.Journal;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;

/**
 * @author Nick
 *
 */
public class JournalPanel extends JPanel
{
    private Journal journal;
    private JEditorPane journalDisplay;
    
    public JournalPanel( Journal journal )
    {
       this.journal = journal;
       createUI();
       loadJournal();       
    }
    
    public void save()
    {
        // update journal
        journal.save( journalDisplay );   
    }
    
    public void setEditable( boolean editable )
    {
        journalDisplay.setEditable(editable);
    }
    
    private void loadJournal()
    {
        //TODO handle race if journal not yet loaded with callback
        URL journalURL = journal.getJournalFile();
        if(  journalURL != null )
        {
            try
            {
                journalDisplay.setPage(journalURL);    
            }
            catch( IOException e )
            {
                SimpleLogger.logError("Unable to load journal from temp file: "+journalURL.toString());
            }
        }
        else
        {
            SimpleLogger.logInfo("No previous journal loaded.");
        }
    }
    
    private void createUI()
    {        
       setLayout( new BorderLayout() );
       JPanel contentPnl = new JPanel(new BorderLayout());
       
       // create main journal display
       this.journalDisplay = new JEditorPane();
       this.journalDisplay.setEditorKit( new HTMLEditorKit());
       this.journalDisplay.setMargin(new Insets(2, 8, 2, 8));
       JScrollPane journalSp = new JScrollPane(this.journalDisplay);
       journalSp.setVerticalScrollBarPolicy(
          JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
       
       // Create the document toolbar
       JToolBar toolbar = new JToolBar();
       toolbar.setBackground(IvanhoeUIConstants.DARK_GRAY);
       
       // clipboard stuff
       Action act = this.journalDisplay.getActionMap().get(DefaultEditorKit.cutAction);
       act.putValue(AbstractAction.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/smallcut.gif"));
       addToolbarButton(toolbar, act);
       act =this.journalDisplay.getActionMap().get(DefaultEditorKit.copyAction);
       act.putValue(AbstractAction.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/smallcopy.gif"));
       addToolbarButton(toolbar, act);
       act =this.journalDisplay.getActionMap().get(DefaultEditorKit.pasteAction);
       act.putValue(AbstractAction.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/smallpaste.gif"));
       addToolbarButton(toolbar, act);
       toolbar.addSeparator();
       
       // styles
       act = this.journalDisplay.getActionMap().get("font-bold");
       act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/bold.gif"));
       addToolbarButton(toolbar, act);
       act = this.journalDisplay.getActionMap().get("font-italic");
       act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/italic.gif"));
       addToolbarButton(toolbar, act);
       act = this.journalDisplay.getActionMap().get("font-underline");
       act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/underline.gif"));
       addToolbarButton(toolbar, act);
       toolbar.addSeparator();
       
       // justify
       act = this.journalDisplay.getActionMap().get("left-justify");
       act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/leftJustify.gif"));
       addToolbarButton(toolbar, act);
       act = this.journalDisplay.getActionMap().get("center-justify");
       act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/centerJustify.gif"));
       addToolbarButton(toolbar, act);
       act = this.journalDisplay.getActionMap().get("right-justify");
       act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/rightJustify.gif"));
       addToolbarButton(toolbar, act);
       toolbar.setFloatable(false);
       
       contentPnl.add(journalSp, BorderLayout.CENTER);
       contentPnl.add(toolbar, BorderLayout.NORTH);
       
       add(contentPnl, BorderLayout.CENTER);
       //add(Box.createHorizontalStrut(10), BorderLayout.WEST);
    }
    
    private void addToolbarButton(JToolBar bar, Action act)
    {
       JButton btn = new JButton(act);
       btn.setText(null);
       btn.setSize(17,17);
       btn.setOpaque(false);
       btn.setBorder(new EmptyBorder(3,3,3,3));
       bar.add(btn);
    }
}
