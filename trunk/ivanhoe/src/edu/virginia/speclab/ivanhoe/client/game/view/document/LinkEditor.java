package edu.virginia.speclab.ivanhoe.client.game.view.document;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.BadLocationException;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.cache.IDocumentLoaderListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.document.IvanhoeDocument;
import edu.virginia.speclab.ivanhoe.client.game.model.document.TextRange;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeStyleInternalFrame;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.GuidGenerator;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.Link;
import edu.virginia.speclab.ivanhoe.shared.data.LinkTag;
import edu.virginia.speclab.ivanhoe.shared.data.LinkType;

public class LinkEditor extends IvanhoeStyleInternalFrame 
{
    private DiscourseField discourseField;
    private Workspace workspace;
    
    private Link linkSource;
    private IvanhoeDocument targetDocument;
    private int targetStart, targetEnd;
    private IvanhoeDocument sourceDocument;
    private int sourceStart, sourceEnd;
    private final Collection loadedDocuments;

    private LinkStatePanel linkStatePanel;
    private RationalePanel rationalePanel;
    private SummaryPanel summaryPanel;
    private ButtonPanel buttonPanel;
    
    private static Icon selectLinkIcon = ResourceHelper.instance.getIcon("res/icons/link.select.gif");
    private static Icon passageLinkIcon = ResourceHelper.instance.getIcon("res/icons/link.passage.gif");
    private static Icon documentLinkIcon = ResourceHelper.instance.getIcon("res/icons/link.document.gif");
    
    private static final int SELECT_LINK = 0;
    private static final int PASSAGE_LINK = 1;
    private static final int DOCUMENT_LINK = 2;
    
    public LinkEditor( Workspace workspace, DiscourseField discourseField, IvanhoeDocument sourceDocument, int start, int end )
    {
        super("link editor");

        this.discourseField = discourseField;
        this.workspace = workspace;
        
        this.sourceDocument = sourceDocument;
        this.sourceStart = start;
        this.sourceEnd = end;

        loadedDocuments = new ArrayList(2);
        linkStatePanel = new LinkStatePanel();
        rationalePanel = new RationalePanel();
        summaryPanel = new SummaryPanel(sourceDocument,start,end);
        buttonPanel = new ButtonPanel();
        
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BorderLayout());
        middlePanel.add(rationalePanel,BorderLayout.NORTH);
        middlePanel.add(summaryPanel,BorderLayout.CENTER);
        
        JPanel containingPanel = new JPanel();
        containingPanel.setLayout(new BorderLayout());
        containingPanel.add(linkStatePanel,BorderLayout.NORTH);
        containingPanel.add(middlePanel,BorderLayout.CENTER);
        containingPanel.add(buttonPanel,BorderLayout.SOUTH);
        containingPanel.setBorder( new EmptyBorder(5,5,5,5));
        
        getContentPane().setLayout( new BorderLayout() );
        getContentPane().add(containingPanel,BorderLayout.CENTER);
        
        // listen for editor close events        
        addInternalFrameListener( new InternalFrameAdapter()
           {
              public void internalFrameClosing(InternalFrameEvent e)
              {
                  cancelLink();
              }
        });
        
        setSize(365,300);
        setVisible(true);
        
        // Keep the source document open
        discourseField.requestDocument(sourceDocument.getVersion(), new DocumentLoaderDelegate());
        
        buttonPanel.setOKButtonState(false);
        workspace.enableLinkSelectionBars(this);
    }
    
    /**
     * Cancels any link that may be in progress
     */
    private void cancelLink()
    { 
       if (this.linkSource != null)
       {
          discourseField.getCurrentMove().removeAction(this.linkSource.getId());          
       }
       
       if (sourceDocument != null)
       {
           setDocumentLoadState(sourceDocument.getTitle(), false);
           discourseField.removeReference(sourceDocument);   
       }
       if (targetDocument != null)
       {
           setDocumentLoadState(targetDocument.getTitle(), false);
           discourseField.removeReference(targetDocument);
       }
       
       workspace.disableLinkSelectionBars();
    }
    
    private void makeLink( IvanhoeDocument source, IvanhoeDocument target, String rationale, int start, int end )
    {
        String linkID = GuidGenerator.generateID();           
        LinkTag linkTag = new LinkTag( target.getVersion().getID(), null );
        Link targetLink = new Link(linkID, LinkType.INTERNAL, linkTag, rationale );        
        source.createLink(start, end, targetLink );
    }
    
    private void makeReciprocalLink( IvanhoeDocument documentA, IvanhoeDocument documentB, String rationale, int startA, int endA, int startB, int endB )
    {
        String linkIDA = GuidGenerator.generateID();
        String linkIDB = GuidGenerator.generateID();                   
        
        // link A -> B
        LinkTag linkTagB = new LinkTag( documentB.getVersion().getID(), linkIDA );
        Link targetLinkB = new Link(linkIDB, LinkType.INTERNAL, linkTagB, rationale );        
        documentA.createLink(startA, endA, targetLinkB );

        // link B -> A
        LinkTag linkTagA = new LinkTag( documentA.getVersion().getID(), linkIDB );
        Link targetLinkA = new Link(linkIDA, LinkType.INTERNAL, linkTagA, rationale );        
        documentB.createLink(startB, endB, targetLinkA );
    }
    
    private boolean createLink()
    {
        if ( !isDocumentLoaded(sourceDocument.getTitle())
                || !isDocumentLoaded(targetDocument.getTitle()) )
        {
            SimpleLogger.logError("One of the documents isn't properly loaded.");
            Ivanhoe.showErrorMessage("<html><b>Add link failed</b><br> "+
                    "There was a problem loading the linked documents.<br> "+
                    "Please try again, and contact the software maintainers "+
                    "if this problem persists."+
                    "</html>");
            return false;
        }
        
        if( linkStatePanel.getState() == DOCUMENT_LINK )
        {
            String rationale = rationalePanel.getLinkTitle();
           
            // create a link to the target document
            makeLink( sourceDocument, targetDocument, rationale, sourceStart, sourceEnd );
        }
        else if( linkStatePanel.getState() == PASSAGE_LINK )
        {
            // make sure target range is not inside of or overlapping src
            if ( sourceDocument.getVersion().equals(targetDocument.getVersion()) )
            {
               TextRange srcRange = new TextRange(sourceStart, sourceEnd);
               TextRange destRange = new TextRange(targetStart, targetEnd);
               if (srcRange.overlaps(destRange))
               {
                  Ivanhoe.showErrorMessage("<html><b>Add link failed</b><br>" +
                  "The link destination is contained within the origin.<br>" +
                  "Please select a different link destintion");
                  
                  setDocumentLoadState(sourceDocument.getTitle(), false);
                  discourseField.removeReference(sourceDocument);
                  setDocumentLoadState(targetDocument.getTitle(), false);
                  discourseField.removeReference(targetDocument);
                  return false;
               }
            }
            
            String rationale = rationalePanel.getLinkTitle();

            // make reciporical link
            makeReciprocalLink( sourceDocument, targetDocument, rationale, sourceStart, sourceEnd, targetStart, targetEnd );
        }
        else
        {
            return false;
        }
        
        setDocumentLoadState(sourceDocument.getTitle(), false);
        setDocumentLoadState(targetDocument.getTitle(), false);
        
        discourseField.removeReference(sourceDocument);
        discourseField.removeReference(targetDocument);
        
        this.sourceDocument = null;
        this.targetDocument = null;
        
        return true;
    }
    
    private void linkReady( int type )
    {
        linkStatePanel.setState(type);
        
        workspace.disableLinkSelectionBars();
        buttonPanel.setOKButtonState(true);        
        this.moveToFront();
        rationalePanel.requestFocus();
    }
    
    public void addDocumentLink( IvanhoeDocument targetDocument )
    {               
        // update UI
        linkReady(DOCUMENT_LINK);

        // store info for this link
        this.targetDocument = targetDocument;
        
        // Keep this document open
        discourseField.requestDocument(targetDocument.getVersion(), new DocumentLoaderDelegate());
        
        // update the summary panel
        summaryPanel.updateDocumentTarget(targetDocument);
    }
    
    public void addPassageLink( IvanhoeDocument targetDocument, int targetStart, int targetEnd )
    {
        // update UI
        linkReady(PASSAGE_LINK);

        // store info for this link
        this.targetDocument = targetDocument;
        this.targetStart = targetStart;
        this.targetEnd = targetEnd;
        
        // Keep this document open
        discourseField.requestDocument(targetDocument.getVersion(), new DocumentLoaderDelegate());
        
        // update the summary panel
        summaryPanel.updatePassageTarget(targetDocument,targetStart,targetEnd);        
    }
    
    private void setDocumentLoadState(String title, boolean loaded)
    {
        if (loaded == true)
        {
            loadedDocuments.add(title);
        }
        else
        {
            loadedDocuments.remove(title);
        }
    }
    
    private final boolean isDocumentLoaded(String title)
    {
        return loadedDocuments.contains(title);
    }
    
    private class ButtonPanel extends JPanel implements ActionListener
    {
        private JButton cancelButton, okButton;
        
        public ButtonPanel()
        {
            setLayout(new GridLayout(1,2));
            
            cancelButton = new JButton("cancel");
            cancelButton.setFont(IvanhoeUIConstants.SMALL_FONT);
            cancelButton.addActionListener(this);            
            add(cancelButton);
            
            okButton = new JButton("ok");
            okButton.setFont(IvanhoeUIConstants.SMALL_FONT);
            okButton.addActionListener(this);
            add(okButton);
        }

        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource().equals(this.cancelButton))
            {
                cancelLink();
                closeWindow();
            }
            else if (e.getSource().equals(this.okButton))
            {
                if( createLink() )
                {
                    closeWindow();    
                }                
            }
        }
        
        public void setOKButtonState( boolean state )
        {
            okButton.setEnabled(state);
        }
    }

    private class SummaryPanel extends JPanel
    {
        private JLabel targetSummary;
        
        public SummaryPanel( IvanhoeDocument srcDocument, int start, int end )
        {            
            setLayout(new GridLayout(2,1));
            
            setBorder( new TitledBorder(
                  new EtchedBorder(),
                  "summary",
                  TitledBorder.CENTER,
                  TitledBorder.DEFAULT_POSITION,
                  IvanhoeUIConstants.SMALL_FONT));
         
            String sourceSummary = createSummary(srcDocument,start,end,true);
            
            JLabel sourceLabel = new JLabel(sourceSummary);
            sourceLabel.setFont(IvanhoeUIConstants.SMALL_FONT);
            add(sourceLabel);
            
            targetSummary = new JLabel("<html>Link to <b>?</b>");
            targetSummary.setFont(IvanhoeUIConstants.SMALL_FONT);
            
            add(targetSummary);
        }        

        private String createSummary( IvanhoeDocument srcDocument, int start, int end, boolean source )
        {
            // create html string representing the source of the link
            
            String preposition = source ? "from" : "to";
            String briefSrc;
            String txt = "";
            try
            {
               txt = srcDocument.getText(start, end-start);
            }
            catch (BadLocationException e){}
            if (txt.length() > 25)
            {
               briefSrc = "<html>Link "+preposition+" '<i>" + txt.substring(0,25) + "...</i>'" 
               + " in <b>" + srcDocument.getTitle() + "</b>";
            }
            else
            {
               briefSrc = "<html>Link "+preposition+" '<i>" + txt + "</i>'" 
               + " in <b>" + srcDocument.getTitle() + "</b>";
            }
            
            return briefSrc;            
        }

        public void updateDocumentTarget( IvanhoeDocument targetDocument )
        {
            String summaryText = "<html>Link to <b>" + targetDocument.getTitle() + "</b>";
            targetSummary.setText(summaryText);
        }

        public void updatePassageTarget( IvanhoeDocument targetDocument, int start, int end )
        {
            String summaryText = createSummary( targetDocument, start, end, false );
            targetSummary.setText(summaryText);
        }
        
    }
    
    private class LinkStatePanel extends JPanel
    {
        private int state;
        private JLabel graphicLabel;
        
        public LinkStatePanel()
        {
            setLayout( new BorderLayout() );
            graphicLabel = new JLabel();
            add(graphicLabel, BorderLayout.CENTER );
            setState(SELECT_LINK);
        }
        
        public void setState( int state )
        {
            this.state = state;
            
            switch( state )
            {
                case SELECT_LINK:
                    graphicLabel.setIcon(selectLinkIcon);
                    break;
                case PASSAGE_LINK:
                    graphicLabel.setIcon(passageLinkIcon);
                    break;
                case DOCUMENT_LINK:
                    graphicLabel.setIcon(documentLinkIcon);
                    break;
            }
        }

        public int getState()
        {
            return state;
        }
    }
    
    private class RationalePanel extends JPanel
    {
       private JTextField rationaleEdit;
       
       public RationalePanel()
       {
          setLayout( new GridLayout(2,1) );
          this.rationaleEdit = new JTextField();
          add( new JLabel("Rationale:"));
          add( this.rationaleEdit);          
       }
       
       public String getLinkTitle()
       {
          String text = this.rationaleEdit.getText();
          if( text == null ) return "";
          else return text;
       }
    }
    
    private class DocumentLoaderDelegate implements IDocumentLoaderListener
    {
        public void documentLoaded(IvanhoeDocument document)
        {
            setDocumentLoadState(document.getTitle(), true);
        }

        public void documentLoadError(String name, String errorMessage)
        {
            SimpleLogger.logError("Error while loading ["+name+"] for LinkEditor: "+errorMessage);
            setDocumentLoadState(name, false);
        }
        
    }
}
