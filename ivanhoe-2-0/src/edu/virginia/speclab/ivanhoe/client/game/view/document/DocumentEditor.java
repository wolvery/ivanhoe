/*
 * Created on Oct 14, 2003
 */
package edu.virginia.speclab.ivanhoe.client.game.view.document;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;

import java.awt.event.*;
import java.util.LinkedList;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.cache.DocumentFilter;
import edu.virginia.speclab.ivanhoe.client.game.model.cache.IDocumentLoaderListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DocumentVersionManager;
import edu.virginia.speclab.ivanhoe.client.game.model.document.*;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.IRoleListener;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.ActionSelection;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.IActionSelectionListener;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.*;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.GuidGenerator;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.*;

/**
 * DocumentEditor
 * @author lfoster
 * @author benc
 *
 * Ivanhoe document editor component
 */
public class DocumentEditor
        extends IvanhoeStyleInternalFrame 
        implements
                ActionListener,
                IActionSelectionListener,
                IDocumentLoaderListener,
                IDocumentMouseListener,
                IRoleListener

{
   private IvanhoeDocument    ivanhoeDoc;
   private DocumentVersionBox versionBox;
   private DiscourseField	  discourseField;
   private RoleManager 		  roleManager;
   private AnimatedEditorPane editorPane; 
   private JCheckBox          showDeletes;
   private JButton            dumpHtml;
   private JButton            openRevisionButton;
   private DocumentGutter     gutter;
   private JTextField         searchTxt;
   private JCheckBox          underlineLinks;
   private JButton	          removeDocButton;
   private LinkedList         pendingHighlights;
   
   private LinkSelectionBar   linkBar; 
   private JPanel             barAttachPoint;
   private JButton            linkButton;
private StepEditPanel stepEditPanel;
   
   private static Icon editModeIcon = ResourceHelper.instance.getIcon("res/icons/smallpencil.gif");
   private static Icon stepEditIcon = ResourceHelper.instance.getIcon("res/icons/little-pin.gif");
   private static Icon smallLinkIcon = ResourceHelper.instance.getIcon("res/icons/smalllink.gif");
   
   
   /**
    * 
    * @param discourseField
    * @param version
    * @param roleManager
    */
   public DocumentEditor( DiscourseField discourseField, DocumentVersion version, RoleManager roleManager )
   {
      super(version.getDocumentTitle());
            
      this.versionBox = new DocumentVersionBox(version);
      this.discourseField = discourseField;
      this.roleManager = roleManager;
      
      pendingHighlights = new LinkedList();
      
      // listen for changes to roles to update title bar color
      roleManager.addRoleListener(this);
      
      // update the title to indicate owning player
      String player = version.getRoleName();
      if (player.equals(roleManager.getCurrentRole().getName()) == false)
      {
         if (player.endsWith("s") || player.endsWith("S"))
         {
            setTitle(player + "' " + version.getDocumentTitle());
         }
         else
         {
            setTitle(player + "'s " + version.getDocumentTitle());
         }
      }
      
      createUI(); 
      
      // request the document from the DiscourseField
      this.editorPane.setLoading(true);
      discourseField.requestDocument(version, this);
      
      ActionSelection actionSelection = Workspace.instance.getNavigator().getActionSelection();
      actionSelection.addListener(this);
      this.actionSelectionChanged(actionSelection.getSelectedAction());      
   }
   
   /**
    * Check if a range of text is currently selected.
    * This is useful to make sure a region is selected for
    * the creation of links
    * @return
    */
   public boolean isTextSelected()
   {
      if (this.editorPane.getSelectionStart() != 
          this.editorPane.getSelectionEnd())
      {
         return true;
      }
    
      return false;
   }
      
   public boolean isLoading()
   {
       return this.editorPane.isLoading();
   }
   
   /**
    * Set the editable status of this editor
    * @param editable
    */
   public void setEditable(boolean editable)
   {
      String editorTitle = this.getTitle();
      int lockedPos = editorTitle.indexOf(" - REVIEW COPY");
      if (editable)
      {
         if (lockedPos > -1)
         {
            editorTitle = editorTitle.substring(0, lockedPos);
            this.setTitle( editorTitle );
         }
      }
      else
      {
         if (lockedPos == -1)
         {
            this.setTitle( this.getTitle() + " - REVIEW COPY");
         }
      }
      
    Role role = roleManager.getRole(versionBox.getDocumentVersion().getRoleID());
    Color titleColor = role.getFillPaint();
    this.setTitleColor(titleColor); 
   }

   /**
    * Create all of the UI components needed for the DocumentEditor
    */
   private void createUI()
   {
      // create the editing pane, and associate it with the editor kit
      this.editorPane = new AnimatedEditorPane(this.discourseField);
      this.editorPane.setEnabled(false);
      this.editorPane.addMouseListener( this );
      
      // add listener for when window is opened
      addInternalFrameListener( new WakeUpListener() ); 
      
      // make it scrollable
      JScrollPane sp = new JScrollPane(editorPane);
      sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      sp.setBorder(null);
      this.getContentPane().add(sp, BorderLayout.CENTER);
      
      // add an action gutter 
      this.gutter = new DocumentGutter(this);      
      getContentPane().add(gutter, BorderLayout.EAST);
      
      // create editor control panel and add it to layout
      this.stepEditPanel = new StepEditPanel();
      this.stepEditPanel.setComment(this.discourseField.getCurrentMove().getDescription());
      this.stepEditPanel.add(createControlPanel(), BorderLayout.SOUTH);
      this.getContentPane().add(stepEditPanel, BorderLayout.SOUTH );
      
      DocumentVersion documentVersion = versionBox.getDocumentVersion();

      // create the header panel and add it 
      DocumentInfo docInfo = discourseField.getDocumentInfo(documentVersion.getDocumentTitle());
      JPanel headerPanel = new HeaderPanel(docInfo,false);
      this.getContentPane().add(headerPanel, BorderLayout.NORTH);      
      headerPanel.add(createToolbar(false), BorderLayout.CENTER);
      
      barAttachPoint = new JPanel();
      barAttachPoint.setLayout( new BorderLayout() );
      headerPanel.add(barAttachPoint, BorderLayout.SOUTH);
            
      // create the lock bar if needed and add it to the header panel
      if (documentVersion.isPublished())
      {
          LockBar lockBar = new LockBar(documentVersion);
          lockBar.getUnlockButton().addActionListener(DocumentEditor.this);          
          openRevisionButton = lockBar.getUnlockButton();
          barAttachPoint.add(lockBar,BorderLayout.NORTH);
      }
      
      // listen for editor close events
      addInternalFrameListener( new InternalFrameAdapter()
         {
            public void internalFrameClosing(InternalFrameEvent e)
            {
               handleClose();
            }
      });
      
      this.editorPane.addMouseListener( new MouseAdapter()
         {
            public void mousePressed(MouseEvent e)
            {
               if ( e.isPopupTrigger() )
               {
                  showPopupMenu(e.getPoint().x, e.getPoint().y);
               }
            }
         });
      
      setSize(Workspace.instance.getWidth()/2, Workspace.instance.getHeight()-30);
      setVisible(true);
   }
   
   public void setInfoPanel( InfoPanel panel )
   {
       this.gutter.setInfoPanel(panel);       
   }
   
   /**
    * create a popup menu 
    */
   protected void showPopupMenu(int x, int y)
   {
      JPopupMenu popup = new JPopupMenu();
      if (isTextSelected())
      {
         JMenuItem cutItem = new JMenuItem(IvanhoeEditorKit.getCutAction(this.discourseField));
         cutItem.setText("Cut Text");
         popup.add(cutItem);
         JMenuItem copyItem = new JMenuItem(IvanhoeEditorKit.getCopyAction(this.discourseField));
         copyItem.setText("Copy Text");
         popup.add(copyItem);
         JMenuItem pasteItem = new JMenuItem(IvanhoeEditorKit.getPasteAction());
         pasteItem.setText("Paste Text");
         popup.add(pasteItem);
         popup.add(new LinkAction());
         popup.add(new AnnotateAction());
      }
      else
      {
         JMenuItem pasteItem = new JMenuItem(IvanhoeEditorKit.getPasteAction());
         pasteItem.setText("Paste Text");
         popup.add(pasteItem);
         popup.add(new ImageAction());
      }
      popup.setFont(IvanhoeUIConstants.SMALL_FONT);
      popup.show(this.editorPane, x,y);
   }

   /**
    * The editor is closing. Notify others
    */
   protected void handleClose()
   {
	  this.discourseField.getCurrentMove().setDescription(this.stepEditPanel.getComment());
      this.gutter.handleClose();
      if (this.ivanhoeDoc != null)
      {
          discourseField.removeReference( this.ivanhoeDoc );
      }
   }

   /**
	* @return the document this editor is editing
	*/
	public IvanhoeDocument getDocument()
	{
		return this.ivanhoeDoc;
	}

   /**
    * Create the panel containing all the editor controls
    * @return the control panel
    */
   private JPanel createControlPanel()
   {
      JPanel btnsPnl = new JPanel();
      btnsPnl.setBackground(IvanhoeUIConstants.DARK_GRAY);
      btnsPnl.setLayout(new BoxLayout(btnsPnl, BoxLayout.X_AXIS));
     
      // style toggles      
      this.showDeletes = new JCheckBox("Show Deletes");
      this.showDeletes.setBackground(IvanhoeUIConstants.DARK_GRAY);
      this.showDeletes.setForeground(IvanhoeUIConstants.WHITE);
      this.showDeletes.setFont(IvanhoeUIConstants.SMALL_FONT);
      this.showDeletes.setSelected(true);
      this.showDeletes.addActionListener( this );
      btnsPnl.add(this.showDeletes);
      
      this.underlineLinks = new JCheckBox("Underline Links");
      this.underlineLinks.setBackground(IvanhoeUIConstants.DARK_GRAY);
      this.underlineLinks.setForeground(IvanhoeUIConstants.WHITE);
      this.underlineLinks.setFont(IvanhoeUIConstants.SMALL_FONT);
      this.underlineLinks.setSelected(true);
      this.underlineLinks.addActionListener( this );
      btnsPnl.add(this.underlineLinks);
      
      btnsPnl.add(Box.createHorizontalGlue());
      
      // debugging help
      String debugString = Workspace.instance.getPropertiesManager().getProperty("debug");
      if ((new Boolean(debugString)).booleanValue())
      {
	      dumpHtml = new JButton("dump html");
	      dumpHtml.addActionListener( this);
	      dumpHtml.setVisible(true);
	      btnsPnl.add(dumpHtml);
      }
      
      JToolBar toolbar = new JToolBar();

      removeDocButton = new JButton(new RemoveDocumentAction());
      removeDocButton.setText(null);
      removeDocButton.setBackground(IvanhoeUIConstants.DARK_GRAY);
      removeDocButton.setSize(17,17);
      removeDocButton.setBorder(new EmptyBorder(3,3,3,3));
      removeDocButton.setOpaque(false);
      toolbar.add(removeDocButton);      
      toolbar.addSeparator();  
      
      addToolbarButton(toolbar, gutter.getDeleteActAction());
      addToolbarButton(toolbar, gutter.getDeleteAllAction());
      addToolbarButton(toolbar, gutter.getNextActAction());
      addToolbarButton(toolbar, gutter.getPreviousActAction());
      toolbar.setOpaque(false);

      toolbar.setFloatable(false);          
      btnsPnl.add(toolbar);       
      removeDocButton.setVisible(false);
            
      return btnsPnl;
   }
   
   
   
   /**
    * Debugging aid that dumps the current doc to html on std out
    */
   public void dumpHtml()
   {
      try
      {
//         System.out.println("dump1:");
//         System.out.println(editorPane.getDocument().getText(0,editorPane.getDocument().getLength()));
//         System.out.println("dump2:");
         this.editorPane.getEditorKit().write(
            System.out,this.ivanhoeDoc,0,this.ivanhoeDoc.getLength());
      }
      catch (Exception e)
      {
         System.err.println("Couldn't dump HTML: " + e);
      }
   }

   /**
    * Search for the given text within the document
    * @param searchString The text to find
    */
   public void search(String searchString)
   {
      int pos = this.ivanhoeDoc.search( this.editorPane.getCaretPosition(),
         searchString, true);
      if (pos > -1 )
      {
         highlightText(pos, searchString.length());
         this.editorPane.setCaretPosition(pos + searchString.length());
      }
   }
   
   public void scrollToOffset(int offset)
   {
      this.editorPane.scrollToOffset(offset);
   }

   public void actionPerformed(ActionEvent e)
   {
      if (e.getSource().equals(this.showDeletes))
      {
         this.ivanhoeDoc.setShowDeleteTags( this.showDeletes.isSelected() );
      }
      if (e.getSource().equals(this.underlineLinks))
      {
         this.ivanhoeDoc.setShowUnderlines( this.underlineLinks.isSelected() );
      }
      else if (e.getSource().equals(dumpHtml))
      {
         this.dumpHtml();
      }
      else if (e.getSource().equals(this.searchTxt))
      {
         search( this.searchTxt.getText() );
      }
      else if (e.getSource() instanceof Timer)
      {
         handleTick();
      }
      else if (e.getSource().equals(openRevisionButton))
      {
          this.openRevision();
      }
   }
   
   /**
    * Open a revision of this document, creating it if necessary
    */
    private void openRevision()
    {
        DocumentVersionManager dvManager = discourseField.getDocumentVersionManager();
        DocumentVersion documentVersion = versionBox.getDocumentVersion();
        
        DocumentVersion currentChild = dvManager.getCurrentChild(documentVersion);
        if (currentChild == null)
        {
            Role role = roleManager.getCurrentRole();
            currentChild = dvManager.createChildDocumentVersion(
                    documentVersion, role.getName(),
                    role.getId(), Ivanhoe.getDate());
        }

        Workspace.instance.openEditor(currentChild);
        this.closeWindow();
    }

private void updateControlPanelState()
   {
       if( ivanhoeDoc != null )
       {
           // remove document button (only present for unpublished docs      
           if( discourseField.getDocumentInfo(ivanhoeDoc.getTitle()).isPublishedDocument() == false )
           {
               removeDocButton.setVisible(true);
           }
           else
           {
               removeDocButton.setVisible(false);
           }
       }
   }
   
   /**
    * Handle a timer tick
    */
   private void handleTick()
   {
      this.editorPane.tick();
      
      if (!this.isLoading())
      {
	      // keep gutter selection in sync with the editor pane
	      if (this.editorPane.isHighlightActive() == false)
	      {
	         this.gutter.clearSelection();
	      }
          
          // check for pending highlights
          if (!pendingHighlights.isEmpty())
          {
              IvanhoeAction action = (IvanhoeAction)pendingHighlights.getLast();
              pendingHighlights.removeLast();
              
              if (action == null)
              {
                  DocumentEditor.this.gutter.clearSelection();
              }
              else
              {
	              // highlight the action
	              IvanhoeDocument doc = DocumentEditor.this.ivanhoeDoc;
	              final String id = action.getId();
                  final ActionType type = action.getType();
	
	              int length = 0;
	              if (type.equals(ActionType.IMAGE_ACTION))
	              {
	                 length = 1;
	              }
	              else
	              {
	                 length = doc.getActionContent(id, type).length();
	              }
	              highlightText(doc.getActionOffset(id), length );
	              
	              DocumentEditor.this.gutter.selectAction(id);
              }
          }
      }
   }

   /**
    * Notification that user has moved mouse over an ilink tag
    */
   public void tagEntered(String tagInfo)
   {
      editorPane.setToolTipText(tagInfo);
   }

   /**
    * Notification that user has moved mouse off an ilink tag
    */
   public void tagExited()
   {
	  editorPane.setToolTipText(null);
   }
   
   /**
    * Notification that user has clicked on a tag
    */
   public void linkActivated(Link target)
   {
      if (target.getType().equals(LinkType.INTERNAL))
      {
          activateInternalLink(target);
      }
      else if (target.getType().equals(LinkType.URL))
      {         
         Workspace.instance.openExternalDocument(target.getLinkTag().getTagData());
      }
      else
      {
         IvanhoeAction act = discourseField.lookupAction(target.getId());
         Role role = roleManager.getRole(act.getRoleID());
         this.editorPane.showPop(target,role);
      }
   }

   /**
    * Activate an internal link by jumping to the target location
    * in the current discoursefield
    * @param linkId
    */
   private void activateInternalLink(Link target)
   {
      LinkTag linkTag = target.getLinkTag();
      
      // extract version info from target data
      String tgtId = linkTag.getBackLinkID();
      DocumentVersion tgtDocVersion = 
		  discourseField.getDocumentVersionManager().getDocumentVersion(linkTag.getDocumentVersionID());
      
	  SimpleLogger.logInfo("Activating internal link to " + tgtDocVersion.getDocumentTitle() );
      
      // open a viewer for the target
      DocumentEditor tgtViewer = null;
      tgtViewer = Workspace.instance.openEditor(tgtDocVersion);
      
      // highlight target if there is one
      if (tgtId != null)
      {
         IvanhoeAction tgtAct = discourseField.lookupAction(tgtId);
         tgtViewer.highlightAction(tgtAct);
      }
   }

   /**
    * Adds a link with the specified content, type and label
    * @param start
    * @param end
    * @param type 
    * @param label
    * @param linkTag
    * @return true if link was added, false otherwise
    */
   public void addLink( int start, int end, LinkType type, String label, LinkTag linkTag )
   {
      Link tgt = new Link(GuidGenerator.generateID(), type, linkTag, label);
      this.ivanhoeDoc.createLink(start, end, tgt );
   }

   /**
    * Highlight the given span of text in this document
    * @param startPos
    * @param length
    */
   public void highlightText(int startPos, int length)
   {
      // schedule the task with the swing to be executed when possible 
      SwingUtilities.invokeLater(new TextHighlighter(startPos, length));   
   }

   public void actionSelectionChanged(IvanhoeAction action)
   {
       highlightAction(action);
   }
   
   	/**
   	 * Highlight the given action in the document
   	 * @param action
   	 */
   public void highlightAction(IvanhoeAction action)
   {
      // Add highlight to this queue to be handled later when the document is
      // done loading.
      pendingHighlights.addFirst(action);
   }
   
   /**
    * Clear any highlighted text 
    */
   public void clearHighlights()
   {
      this.editorPane.clearHighlight();
   }

   public void focus()
   {
      this.editorPane.requestFocus();
   }

   public void documentLoaded(IvanhoeDocument document)
   {         
      // schedule the task with the swing to be executed when possible 
      SwingUtilities.invokeLater(new Initializer(document));
   }

   public void documentLoadError(String docTitle, String errorMessage)
   {
      Ivanhoe.showErrorMessage("Document \"" + docTitle 
         + "\" is temporarily unavailable. Notify the game administrator and try again later.");
      doDefaultCloseAction();
   }
   
   /**
    * Runnable object used by invokeLater to load an ivanhoe document
    * into the editor pane in a safe manner, when swing is ready do do it
    */
   private class Initializer implements Runnable
   {
      private IvanhoeDocument document;
      
      public Initializer(IvanhoeDocument doc)
      {
         this.document = doc;
      }
      
      public void run() 
      {  
         // set the doc in the editorpane
	      DocumentEditor.this.editorPane.setDocument(this.document);
	      DocumentEditor.this.editorPane.setEnabled(true);
	      
	      // correctly show the delete tags ckbox
	      DocumentEditor.this.showDeletes.setSelected(this.document.areDeleteTagsVisible());
	      
	      // set editable status
	      if(this.document.isReadOnly() == true)
	      {
	         setEditable(false);
	      }
	      
	      // flag loading as complete
         DocumentEditor.this.ivanhoeDoc = this.document;
         DocumentEditor.this.editorPane.setLoading(false);
         DocumentEditor.this.gutter.initialize();
         updateControlPanelState();
         DocumentEditor.this.repaint();
      }
   }
   
   /**
    * Runnable object used by invokeLater to highlight an area of text
    */
   private class TextHighlighter implements Runnable
   {
      private int start, len;
      
      public TextHighlighter(int start, int len)
      {
         this.start = start;
         this.len = len;
      }
      
      public void run() 
      { 
         int endPos = this.start + this.len;
         
         // make sure end isnt past end of doc
         endPos = Math.min(ivanhoeDoc.getLength(), endPos);
         
         // make sure start is > 0 and < end of doc
         this.start = Math.max(0, this.start);
         this.start = Math.min(ivanhoeDoc.getLength(), this.start);
         
         DocumentEditor.this.editorPane.setCaretPosition(endPos);
         DocumentEditor.this.editorPane.highlightSelection(this.start, this.len);
         DocumentEditor.this.editorPane.scrollToOffset(endPos);
         DocumentEditor.this.editorPane.repaint();
      }
   }
   
   
   /**
    * Action implemnation for Cancel Creation
    */
   private class RemoveDocumentAction extends javax.swing.AbstractAction
   {
      public RemoveDocumentAction()
      {
         super(null,ResourceHelper.instance.getIcon("res/icons/smalltrash.gif"));
         putValue(Action.SHORT_DESCRIPTION,"Remove this document from the discourse field");
      }

      public void actionPerformed(ActionEvent arg0)
      {
         int resp = JOptionPane.showConfirmDialog(null, 
            "Remove this document from the discourse field?", 
            "Confirm Cancel",
            JOptionPane.OK_CANCEL_OPTION);
         if (resp == JOptionPane.OK_OPTION)
         {
             discourseField.getCurrentMove().removeDocument(ivanhoeDoc.getTitle());
         }
      }
   }
   
   // listen for when the window is opened
   private class WakeUpListener extends InternalFrameAdapter
   {
	    public void internalFrameOpened(InternalFrameEvent arg0)
	    {
	        updateControlPanelState();
	    }
   }
      
   /**
    * Action implemnation for local doc searches
    */
   private class LocalSearch extends javax.swing.AbstractAction
   {
      public LocalSearch()
      {
			super("Search",
				ResourceHelper.instance.getIcon("res/icons/smallsearch.gif"));
			putValue(Action.SHORT_DESCRIPTION,"Search");
      }

      public void actionPerformed(ActionEvent arg0)
      {
          DocumentEditor.this.search( DocumentEditor.this.searchTxt.getText());
      }
   }
   
   
   //TODO tie this to edit rationale
   private class StepEditPanel extends JPanel 
   {
	    private JPanel stepPanelTitleBar;
	    private JEditorPane commentPane;
	   
		public StepEditPanel()
		{			
			setBackground(IvanhoeUIConstants.DARKEST_GRAY);
			
			stepPanelTitleBar = new JPanel();
			stepPanelTitleBar.setLayout( new BoxLayout( stepPanelTitleBar, BoxLayout.X_AXIS ));
			
			JLabel title = new JLabel("Move Rationale");
			title.setIcon(editModeIcon);
			title.setFont(IvanhoeUIConstants.SMALL_FONT);
			title.setBackground(IvanhoeUIConstants.DARKEST_GRAY);
		    title.setForeground(IvanhoeUIConstants.BLACK);
		    
		    JLabel thumbIconLabel = new JLabel();
		    thumbIconLabel.setIcon(stepEditIcon);
		    
		    stepPanelTitleBar.add(title);
		    stepPanelTitleBar.add(Box.createHorizontalGlue());
		    stepPanelTitleBar.add(thumbIconLabel);
			
			setLayout(new BorderLayout());
			add(stepPanelTitleBar, BorderLayout.NORTH );
					
			commentPane = new JEditorPane();
			commentPane.setFont(IvanhoeUIConstants.SMALL_FONT);
			
			// make this panel a fifth the size of the editor panel
			int height = (Workspace.instance.getWidth()/2)/5;
			
			commentPane.setPreferredSize(new Dimension(500,height));
			add(commentPane, BorderLayout.CENTER );
		}
		
		public boolean isStepEditPanelVisible()
		{
			return stepPanelTitleBar.isVisible();
		}

		public void setStepEditPanelVisible( boolean state )
		{
			stepPanelTitleBar.setVisible(state);
			commentPane.setVisible(state);
		}

		public String getComment() {
			return this.commentPane.getText();
		}
		
		public void setComment( String comment ) {
			this.commentPane.setText(comment);
		}
   }
   
   
   /**
    * Action implemnation for DocumentInfo
    */
   private class AnnotateAction extends javax.swing.AbstractAction
   {
      public AnnotateAction()
      {
         super("Add Commentary",
            ResourceHelper.instance.getIcon("res/icons/smallcomment.gif"));
         putValue(Action.SHORT_DESCRIPTION,"Add commentary");
      }

      public void actionPerformed(ActionEvent arg0)
      {
         //  and some text is selected
         if (isTextSelected() == false)
         {
            Ivanhoe.showErrorMessage("Comment failed: Select text to comment upon.");
            return;
         }
         
         int start = DocumentEditor.this.editorPane.getSelectionStart();
         int end = DocumentEditor.this.editorPane.getSelectionEnd();
         Workspace.instance.openAnnotationWindow(DocumentEditor.this,start,end);
      }
   }
   
   /**
    * Action implemnation for DocumentInfo
    */
   private class LinkAction extends javax.swing.AbstractAction
   {
      public LinkAction()
      {
         super("Create Link", smallLinkIcon );
         putValue(Action.SHORT_DESCRIPTION,"Create a link");
      }

      public void actionPerformed(ActionEvent arg0)
      {
         //  and some text is selected
         if (isTextSelected() == false)
         {
             Ivanhoe.showErrorMessage("Link failed: No text is selected to link from");
            return;
         }

         IvanhoeDocument doc = DocumentEditor.this.ivanhoeDoc;
         Workspace.instance.openLinkEditor(doc,
               DocumentEditor.this.editorPane.getSelectionStart(),
               DocumentEditor.this.editorPane.getSelectionEnd());
      }
   }
   
   /**
    * Action to insert an image at the current caret position
    */
   public class ImageAction extends javax.swing.AbstractAction 
   {
      public ImageAction() 
      {
         super("Insert Image",
            ResourceHelper.instance.getIcon("res/icons/addImg.gif"));
         putValue(Action.SHORT_DESCRIPTION,"Insert an image");
      }

      public void actionPerformed(ActionEvent ae) 
      {
         JFileChooser dlg = new JFileChooser(System.getProperty("user.dir"));
         dlg.addChoosableFileFilter(
            new DocumentFilter(".gif", "GIF Images (*.gif)"));
         dlg.addChoosableFileFilter(
            new DocumentFilter(".jpg", "JPEG Images (*.jpg)"));
               
         /**
          * The following instruction fails on Mac OS X 10.2.x,
          * which has a Java bug.  Later version of Mac OS work fine.
          */
         if (System.getProperty("mrj.version") == null)
         {
            dlg.setAcceptAllFileFilterUsed(false);
         }
         int result = dlg.showDialog(null, "Add Image");
         if (result == JFileChooser.APPROVE_OPTION)
         {
            String imageName = dlg.getSelectedFile().getName();
            discourseField.addImage(
               DocumentEditor.this.ivanhoeDoc.getInfo(), dlg.getSelectedFile());
            DocumentEditor.this.ivanhoeDoc.insertImage(GuidGenerator.generateID(),
               DocumentEditor.this.editorPane.getCaretPosition(), imageName, true);
         }
      }
    }
   
  
   private class LockBar extends JPanel
   {
       private JButton unlockButton;
       
       public LockBar(DocumentVersion documentVersion)
       {
           super(new BorderLayout());

           unlockButton = new JButton("This is a review copy of the text. Click here to edit.");
           unlockButton.setFont(IvanhoeUIConstants.TINY_BOLD_FONT);
           unlockButton.setIcon(editModeIcon);
           unlockButton.setBackground(IvanhoeUIConstants.DARK_GRAY);
           unlockButton.setForeground(IvanhoeUIConstants.WHITE);                      
           unlockButton.setHorizontalAlignment(SwingConstants.LEFT);
           
           // the border controls the margins in a JButton, need to override it
           // to get position correct.
           unlockButton.setBorder( new EmptyBorder(0,0,0,0) {               
               public Insets getBorderInsets()
               {
                    return new Insets(2,5,2,5);
               }
               public Insets getBorderInsets(Component c)
               {
                    return getBorderInsets();
               }
               public Insets getBorderInsets(Component c, Insets insets)
                {
                    return getBorderInsets();
                }               
           });           
                                 
           this.add(unlockButton, BorderLayout.CENTER);
           
       }
       
       public JButton getUnlockButton()
       {           
           return unlockButton;
       }
   }
   
	private JButton addToolbarButton(JToolBar bar, Action act)
	{
	   JButton btn = new JButton(act);
	
		btn.setText(null);
		bar.add(btn);
		btn.setSize(17,17);
		btn.setOpaque(false);
		btn.setBorder(new EmptyBorder(3,3,3,3));
        return btn;
	}
	
	private IvanhoeToolBar createToolbar( boolean readOnly )
    {
        // Create the document toolbar
        IvanhoeToolBar toolbar = new IvanhoeToolBar();
        toolbar.setBackground(IvanhoeUIConstants.DARK_GRAY);
        
        // actions
        if( readOnly == false )
        {
            addToolbarButton(toolbar, new ImageAction());
            toolbar.addSeparator();
            toolbar.addSeparator();                
        }
        
        linkButton = addToolbarButton(toolbar, new LinkAction());
        addToolbarButton(toolbar, new AnnotateAction());
        toolbar.addSeparator();
        toolbar.addSeparator();

        // only show copy action for read only documents
        if( readOnly == false )
        {
            addToolbarButton(toolbar, IvanhoeEditorKit.getCutAction(DocumentEditor.this.discourseField));
            addToolbarButton(toolbar, IvanhoeEditorKit.getCopyAction(DocumentEditor.this.discourseField));
            addToolbarButton(toolbar, IvanhoeEditorKit.getPasteAction());
        }
        else
        {
            addToolbarButton(toolbar, IvanhoeEditorKit.getCopyAction(DocumentEditor.this.discourseField));
        }
                    
        // searching            
        searchTxt = new JTextField(10);
        //searchTxt.setMaximumSize(new Dimension(120, 25));
        searchTxt.setMaximumSize(new Dimension(120, 15));
        searchTxt.addActionListener(DocumentEditor.this);
        searchTxt.setBackground(IvanhoeUIConstants.WHITE);
        searchTxt.setForeground(IvanhoeUIConstants.BLACK);
        searchTxt.setFont(IvanhoeUIConstants.TINY_FONT);
        searchTxt.setBorder(new EmptyBorder(0,5,0,0));
                        
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(searchTxt);
        addToolbarButton(toolbar, new LocalSearch());
        toolbar.setFloatable(false);

        return toolbar;
    }
	
	/* (non-Javadoc)
	 * @see edu.virginia.speclab.ivanhoe.client.game.model.metagame.IRoleListener#roleChanged()
	 */
	public void roleChanged()
	{
        DocumentVersion documentVersion = versionBox.getDocumentVersion();
	    Role role = roleManager.getRole(documentVersion.getRoleID());
	    Color titleColor = role.getFillPaint();         
	    this.setTitleColor(titleColor); 
	    repaint();
	}
/**
 * @return Returns the discourseField.
 */
public DiscourseField getDiscourseField()
{
    return discourseField;
}

    // Allows access to the correct reference of the document version while the
    // IvanhoeDocument is loading asychronously. 
    private class DocumentVersionBox
    {
        private DocumentVersion documentVersion;
        
        public DocumentVersionBox( DocumentVersion version )
        {
            documentVersion = version;
        }
        
        public DocumentVersion getDocumentVersion()
        {
            if( ivanhoeDoc == null ) return documentVersion;
            else return ivanhoeDoc.getVersion();            
        }    
    }
    
    private class LinkSelectionBar extends JPanel implements ActionListener, CaretListener
    {
        private LinkEditor linkEditor;
        private JButton linkButton;
        
        private boolean passageLink;
        
        private String documentLinkString = "Select this document as a link target.";
        private String passageLinkString = "Select this passage as a link target.";
        
        public LinkSelectionBar( LinkEditor linkEditor )
        {
            super(new BorderLayout());

            this.linkEditor = linkEditor;
            
            linkButton = new JButton();
            linkButton.setFont(IvanhoeUIConstants.TINY_BOLD_FONT);
            linkButton.setIcon(smallLinkIcon);
            linkButton.setBackground(IvanhoeUIConstants.DARK_GRAY);
            linkButton.setForeground(IvanhoeUIConstants.WHITE);                      
            linkButton.setHorizontalAlignment(SwingConstants.LEFT);
            linkButton.setText(documentLinkString);
            
            // the border controls the margins in a JButton, need to override it
            // to get position correct.
            linkButton.setBorder( new EmptyBorder(0,0,0,0) {               
                public Insets getBorderInsets()
                {
                     return new Insets(2,5,2,5);
                }
                public Insets getBorderInsets(Component c)
                {
                     return getBorderInsets();
                }
                public Insets getBorderInsets(Component c, Insets insets)
                 {
                     return getBorderInsets();
                 }               
            });           
                                  
            linkButton.addActionListener(this);
            this.add(linkButton, BorderLayout.CENTER);           
        }
        
        public JButton getLinkButton()
        {           
            return linkButton;
        }

        public void actionPerformed(ActionEvent e)
        {            
            if( passageLink )
            {
                linkEditor.addPassageLink(ivanhoeDoc, 
                                          editorPane.getSelectionStart(),  
                                          editorPane.getSelectionEnd()    );
            }
            else
            {
                linkEditor.addDocumentLink(ivanhoeDoc);
            }
        }

        public void caretUpdate(CaretEvent e)
        {
            if( isTextSelected() && !passageLink )
            {
                linkButton.setText(passageLinkString);
                passageLink = true;
            }
            else if( !isTextSelected() && passageLink )
            {
                linkButton.setText(documentLinkString);
                passageLink = false;
            }            
        }
    }

    public void enableLinkSelectionBar(LinkEditor linkEditor)
    {
        linkBar = new LinkSelectionBar(linkEditor);
        linkButton.setEnabled(false);
        barAttachPoint.add(linkBar,BorderLayout.SOUTH);
        editorPane.addCaretListener(linkBar);
    }

    public void disableLinkSelectionBar()
    {
        if( linkBar != null )
        {
            barAttachPoint.remove(linkBar);
            barAttachPoint.revalidate();
            editorPane.removeCaretListener(linkBar);
            linkBar = null;
            linkButton.setEnabled(true);            
        }        
    }   
}


