/*
 * Created on Oct 14, 2003
 */

package edu.virginia.speclab.ivanhoe.client.game.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyVetoException;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.IvanhoeGame;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.CurrentMove;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.document.IvanhoeDocument;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.ReferenceResourceManager;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.Discussion;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.Journal;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.chat.Chat;
import edu.virginia.speclab.ivanhoe.client.game.view.chat.ChatNotifier;
import edu.virginia.speclab.ivanhoe.client.game.view.document.AnnotationWindow;
import edu.virginia.speclab.ivanhoe.client.game.view.document.DocumentCreator;
import edu.virginia.speclab.ivanhoe.client.game.view.document.DocumentEditor;
import edu.virginia.speclab.ivanhoe.client.game.view.document.DocumentStemmaWindow;
import edu.virginia.speclab.ivanhoe.client.game.view.document.InfoPanel;
import edu.virginia.speclab.ivanhoe.client.game.view.document.LinkEditor;
import edu.virginia.speclab.ivanhoe.client.game.view.gameaction.GameActionToolbar;
import edu.virginia.speclab.ivanhoe.client.game.view.metagame.DiscussionWindow;
import edu.virginia.speclab.ivanhoe.client.game.view.metagame.publish.MovePublishWizard;
import edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay.RolePlayWindow;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.DiscourseFieldNavigator;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.PlayerCircleFilter;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.ColorPair;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors.CustomColorsWindow;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.history.DiscourseHistoryWindow;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.time.DiscourseFieldTimeSlider;
import edu.virginia.speclab.ivanhoe.client.game.view.search.*;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.Drawer;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.HTMLViewer;
import edu.virginia.speclab.ivanhoe.client.util.PropertiesManager;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;
import edu.virginia.speclab.ivanhoe.shared.data.Role;

/**
 * @author lfoster
 * 
 * The main workspace of the game. It contains multiple documenteditors
 */
public class Workspace extends JDesktopPane
{
   private IvanhoeFrame				ivanhoeFrame;
   private IvanhoeGame 				ivanhoeGame;

   private DiscourseFieldNavigator  navigator;
   private DiscourseFieldTimeSlider timeControl;
   private GameActionToolbar        menu;
   
   private RolePlayWindow           roleWindow;
   private CustomColorsWindow 		colorsWindow;
   
   private JLabel                   frameRateLabel;  
   private Chat               		chatClient;
   private ChatNotifier             chatNoty;
   private InfoPanel                actionInfo;
   
   private Timer                    timer;
  
   private boolean historyWindowOpen, roleWindowOpen, chatWindowOpen; 
   private boolean searchWindowOpen, discussionWindowOpen;
   private boolean linkEditorWindowOpen, annotationWindowOpen;
   private boolean colorsWindowOpen;
   
   // when present, display link bar on document editors 
   private LinkEditor linkEditor;
   
   // frame rate calculation 
   private long[] previousTimes;
   private int previousIndex;
   private boolean previousFilled;
   private double frameRate;
   
   // set this flag to display frame rate
   public static final boolean DISPLAY_FRAME_RATE = false;
   
   private static final double HISTORY_WINDOW_PERCENT_WIDTH = 0.3;
   private static final double HISTORY_WINDOW_PERCENT_HEIGHT = 0.9;
   private static final double ROLE_WINDOW_PERCENT_WIDTH = 0.5;
   private static final double ROLE_WINDOW_PERCENT_HEIGHT = 0.5;
   private static final double DISCUSSION_WINDOW_PERCENT_WIDTH = 0.5;
   private static final double DISCUSSION_WINDOW_PERCENT_HEIGHT = 0.75;
   
   public static Workspace instance;
  
   public Workspace( IvanhoeFrame ivanhoeFrame )
   {
      setBackground(Color.BLACK);
    
      this.ivanhoeFrame = ivanhoeFrame;
      
      // init framerate members
      this.previousTimes = new long[128];
      this.previousTimes[0] = System.currentTimeMillis();
      this.previousIndex = 1;
      this.previousFilled = false;     
   }
   
   /**
    * Create the user interface elements present in the workspace
    */
   public void createUI( IvanhoeGame game )
   {
       this.ivanhoeGame = game;
       
       if( DISPLAY_FRAME_RATE == true )
       {
           this.frameRateLabel = new JLabel("Framerate = XX.XX fps");
           this.frameRateLabel.setForeground(Color.white);
           this.frameRateLabel.setSize(frameRateLabel.getPreferredSize());
           add(frameRateLabel, new Integer(Integer.MIN_VALUE));
           this.frameRateLabel.setLocation(500,80);
       }
       
       // add the background drawing surface     
       this.navigator = new DiscourseFieldNavigator( game.getDiscourseField(),
               										 game.getPropertiesManager()   );
       add(navigator, new Integer(Integer.MIN_VALUE));
   
       // create the popout menu
       this.menu = new GameActionToolbar( ivanhoeGame, ivanhoeFrame );
       add(this.menu, new Integer(0), 0);
       
       // create the InfoPanel popup
       this.actionInfo = new InfoPanel("");
       actionInfo.setVisible(false);
       this.add(this.actionInfo, new Integer(1), 4);
       navigator.setInfoPanel(actionInfo);
       
       // create a blinky thing to show when chat msgs arrive
       this.chatNoty = new ChatNotifier();
       this.add(this.chatNoty, new Integer(1), 5);
       this.chatNoty.setLocation(50, getHeight() - 100);
       
       // desktop manager has a no layout manager, so we must 
       // resize the background ourselves when it changes.
       this.addComponentListener( new ComponentAdapter()
          {  
             public void componentResized(ComponentEvent arg0)
             {
                desktopResized();
             }
          });
       
       showWorkspace(false);
   }
   
   /**
    * Toggle visibility of all workspace UI elements
    * @param show
    */
   private void showWorkspace(boolean show)
   {    
      this.menu.setVisible(show);
      this.navigator.setVisible(show);
      
      if( DISPLAY_FRAME_RATE == true )
      {
          this.frameRateLabel.setVisible(show);    
      }
   }
   
   /**
    * Called from the ComponentAdapter when the desktop window is resized
    * Adjust the size of all desktop components
    */
   private void desktopResized()
   {
      PlayerCircleFilter filter = navigator.getPlayerCircleFilter();

      // put playercircle filter box on the right
      int filterX = 1 + getWidth() - filter.getWidth();
      filter.setLocation(filterX,getHeight()-100);
    
      // set the menu against the left hand side
      this.menu.setLocation(-1,-1);

      this.chatNoty.setLocation(10, getHeight() - 100);
      
      // size the navigator
      this.navigator.setSize(getSize());
   }

   /**
    * Notification that the workspace has been ticked by the timer
    */
   protected void tick()
   {
      this.navigator.render();
  
      if( DISPLAY_FRAME_RATE == true )
      {
          calculateFrameRate();
          displayFrameRate();
      }
   }
   
   private void displayFrameRate()
   {
       double displayFrameRate = Math.round(frameRate*100)/100.0;
       String frameRateText = new String("Framerate = "+displayFrameRate+" fps");
       frameRateLabel.setText(frameRateText);
   }
   
   private void calculateFrameRate()
   {
       long now = System.currentTimeMillis();
       int numberOfFrames = previousTimes.length;
       double newRate;
       
       if( previousFilled == true )
       {
           newRate = (double) numberOfFrames / 
               (double) (now - previousTimes[previousIndex]) *
               1000.0;
       }
       else
       {
           newRate = 1000.0 / 
               (now-previousTimes[numberOfFrames-1]);
       }
       frameRate = newRate;
       previousTimes[previousIndex] = now;
       previousIndex++;
       if( previousIndex >= numberOfFrames )
       {
           previousIndex = 0;
           previousFilled = true;
       }
   }
   
   /**
    * The game has started
    */
   public void startGame()
   {
      SimpleLogger.logInfo("Workspace.startGame()");
      navigator.init();
      SimpleLogger.logInfo("navigator.init()");
	  showWorkspace(true);
      SimpleLogger.logInfo("showWorkspace(true)");
      
      // start the timer
      this.timer = new Timer(1000/20, new ActionListener() 
         {
            public void actionPerformed(ActionEvent evt) 
            {
               tick();
            }
        });
      
      this.timer.start();
      SimpleLogger.logInfo("timer.start()");
      
      // init chat
      this.chatClient = new Chat(ivanhoeGame.getRoleManager());      
      
      // register chat for ticks      
      this.timer.addActionListener(this.chatNoty);
   }
  
   /**
    * open a document that is not part of the DiscourseField and is specified by
    * a URL
    * @param docName
    */
   public void openExternalDocument(String url)
   {
      HTMLViewer viewer = new HTMLViewer(url);
      add(viewer);
      centerWindowOnWorkspace(viewer);

      //.moveToFront(viewer);
      this.setSelectedFrame(viewer);

      try
      {
         viewer.setSelected(true);
      }
      catch (PropertyVetoException e)
      {
      }
   }
   
   /**
    * Open the new document editor
    */
   public void createNewDocument()
   {
      DocumentCreator editor = new DocumentCreator(ivanhoeGame.getDiscourseField(),ivanhoeFrame);
      add(editor);
      centerWindowOnWorkspace(editor);
      
      editor.setSize(
              Math.min(this.getWidth(), editor.getWidth()),
              Math.min(this.getHeight(), editor.getHeight()));

      try
      {
         editor.setSelected(true);
      }
      catch (PropertyVetoException e) {}
   }

   /**
    * Open the specified version of a document
    * @param version Document version to open
    * @return The Editor
    */
   public DocumentEditor openEditor(DocumentVersion version)
   {
      SimpleLogger.logInfo("opening document editor for version: "+version);
      DocumentEditor editor = findEditor(version);
      if (editor == null)
      {
         editor = createDocumentEditor(version);         
      }

      return editor;
   }
   
   public DocumentStemmaWindow openStemmaWindow(DocumentInfo docInfo, DiscourseField discourseField)
   {
       SimpleLogger.logInfo("opening document stemma viewer for document: "+docInfo);
       DocumentStemmaWindow stemmaWindow = findStemmaWindow(docInfo);
       if (stemmaWindow == null)
       {
           stemmaWindow = createStemmaWindow(docInfo, discourseField);
       }
       return stemmaWindow;
   }
   
    private DocumentStemmaWindow findStemmaWindow(DocumentInfo docInfo)
    {
        JInternalFrame[] frames = this.getAllFrames();
        DocumentStemmaWindow matchFrame = null;

        for (int i = 0; i < frames.length; i++)
        {
           if (frames[i] instanceof DocumentStemmaWindow)
           {
              DocumentStemmaWindow stemmaWindow = (DocumentStemmaWindow) frames[i];
              DocumentInfo stemmaDocInfo = stemmaWindow.getStemmaManager().getDocumentInfo();
              if ( stemmaDocInfo != null && stemmaDocInfo.equals(docInfo))
              {
                 matchFrame = stemmaWindow;
                 break;
              }
           }
        }

        if (matchFrame != null)
        {
           try
           {
              if (matchFrame.isIcon())
              {
                 matchFrame.setIcon(false);
              }
              matchFrame.setSelected(true);
           }
           catch (PropertyVetoException e)
           {
              SimpleLogger.logError("Failed to deiconify window", e);
           }

           return matchFrame;
        }

        return null;
    }

/**
    * Helper method to locate a previously opened viewer for the specified document
    * @param version
    * @return
    */
   private DocumentEditor findEditor(DocumentVersion version)
   {
      JInternalFrame[] frames = this.getAllFrames();
      DocumentEditor matchFrame = null;

      for (int i = 0; i < frames.length; i++)
      {
         if (frames[i] instanceof DocumentEditor)
         {
            DocumentEditor editor = (DocumentEditor) frames[i];
            IvanhoeDocument doc = editor.getDocument();
            if ( doc != null && doc.getVersion().equals(version))
            {
               matchFrame = editor;
               break;
            }
         }
      }

      if (matchFrame != null)
      {
         try
         {
            if (matchFrame.isIcon())
            {
               matchFrame.setIcon(false);
            }
            matchFrame.setSelected(true);
         }
         catch (PropertyVetoException e)
         {
            SimpleLogger.logError("Failed to deiconify window", e);
         }

         return matchFrame;
      }

      return null;
   }

   // recurse through the list of windows, trying to position this window so that it isn't 
   // exactly in the same spot as any other.
   private void offsetWindow( JInternalFrame window )
   {
       JInternalFrame[] frames = this.getAllFrames();
       Point windowLocation = window.getLocation();

       for (int i = 0; i < frames.length; i++)
       {
          JInternalFrame frame = frames[i];
          
          if ( window != frame &&
               windowLocation.x == frame.getX() &&
               windowLocation.y == frame.getY()     )
          {
              // push window over and down a bit
              windowLocation.x += 15;
              windowLocation.y += 15;

              // if we reach the end of the workspace, give up and drop it near edge
              if( windowLocation.x > this.getWidth() || 
                  windowLocation.y > this.getHeight()   )
              {
	              if( windowLocation.x > this.getWidth() )
	                  windowLocation.x = this.getWidth() - 100;
	              
	              if( windowLocation.y > this.getHeight() ) 
	                  windowLocation.y = this.getHeight() - 100;
	              
	              window.setLocation(windowLocation);
	              break;
              }
              else
              {
                  // recursively look for other windows we may now be over
                  window.setLocation(windowLocation);
                  offsetWindow(window);
                  break;                 
              }
          }
       }       
   }

   /**
    * Open the latest version of the specified document in an editor
    * @param version version of the document to open
    * @return Document Editor
    */
   private DocumentEditor createDocumentEditor(DocumentVersion version)
   {
      DocumentEditor editor = new DocumentEditor(ivanhoeGame.getDiscourseField(),
              									 version,
              									 ivanhoeGame.getRoleManager() );
      editor.setInfoPanel(this.actionInfo);
      add(editor);
      this.timer.addActionListener(editor);
      centerWindowOnWorkspace(editor);
      offsetWindow(editor);
      
      // if there is a link editor open, then enable link selection bar.
      if( linkEditor != null )
      {
          editor.enableLinkSelectionBar(linkEditor);
      }

      try
      {
         editor.setSelected(true);
      }
      catch (PropertyVetoException e) { }

      return editor;
   }
   
   private DocumentStemmaWindow createStemmaWindow(DocumentInfo docInfo, DiscourseField discourseField)
   {
	   DiscourseFieldTimeSlider slider = ivanhoeFrame.getTimeSlider();
	   
       final DocumentStemmaWindow stemmaWindow =
               new DocumentStemmaWindow(docInfo, discourseField, slider, navigator);
       
       this.add(stemmaWindow);
       centerWindowOnWorkspace(stemmaWindow);
       offsetWindow(stemmaWindow);
       
       stemmaWindow.show();
       
       try
       {
          stemmaWindow.setSelected(true);
       }
       catch (PropertyVetoException e) { }
       
       return stemmaWindow;
   }
   
   /**
    * Checks if the chat window is open and not iconified
    * @return boolean true if the chat window is visible
    */
   public boolean isChatVisible()
   {
      if (this.chatClient.isVisible() && !this.chatClient.isIcon())
      {
         return true;
      }
      return false;
   }
   
   /**
    * Opens the chat window as an IvanhoeStyleInternalFrame in the workspace
    */
   public void openChatWindow()
   {
      // only open one chat window at a time
      if (chatWindowOpen == true) return;
      
      add(chatClient);
      chatClient.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
      
      if (chatClient.isIcon())
      {
         try
         {
            chatClient.setMaximum(true);
         } 
         catch (PropertyVetoException e)
         {
            e.printStackTrace();
         }
      }
      this.chatClient.setVisible(true);
      
      // set focus on the input text field
      this.chatClient.inputRequestFocus();
   }
   
   public void openHistoryWindow()
   {   
       if( historyWindowOpen == true ) return;

       DiscourseHistoryWindow historyWindow = new DiscourseHistoryWindow(navigator);
       historyWindow.addHistoryView("roles", ivanhoeGame.getPlayerHistoryTreeModel());
       historyWindow.addHistoryView("moves", ivanhoeGame.getMoveHistoryTreeModel());
       historyWindow.addHistoryView("documents", ivanhoeGame.getDocHistoryTreeModel());
       historyWindow.addHistoryView("document stemma", ivanhoeGame.getDocVersionTreeModel());
       
       add(historyWindow);
       historyWindow.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);

       int w = (int)(getWidth()* HISTORY_WINDOW_PERCENT_WIDTH);
       int h = (int)(getHeight()* HISTORY_WINDOW_PERCENT_HEIGHT);
       historyWindow.setSize(w,h);       
       historyWindow.setVisible(true);      
       this.moveToFront(historyWindow);
       this.setSelectedFrame(historyWindow);   
       
       // listen for close events
       historyWindow.addInternalFrameListener(new InternalFrameAdapter()
       {
          public void internalFrameClosing(InternalFrameEvent e)
          {
              historyWindowOpen = false;
          }         
       });
       
       try
       {
           historyWindow.setSelected(true);
           historyWindowOpen = true;
       }
       catch (PropertyVetoException e)
       {
       }      
   }
   
   public void openColorsWindow()
   {
       Role currentRole = ivanhoeGame.getRoleManager().getCurrentRole();
       
       ColorPair pcColors = 
           new ColorPair(currentRole.getStrokePaint(), currentRole.getFillPaint() );
       this.openColorsWindow(currentRole.getName(), pcColors);
   }
   
   public CustomColorsWindow openColorsWindow(String roleName, ColorPair playerCircleColors)
   {   
       if( colorsWindowOpen == false ) 
       {
	       colorsWindow = 
	           new CustomColorsWindow(roleName, playerCircleColors, 
	                   navigator.getCustomColors(), navigator.getRoleManager());
	       add(colorsWindow);
	       colorsWindow.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
	
	       colorsWindow.setVisible(true);      
	       this.moveToFront(colorsWindow);
	       centerWindowOnWorkspace(colorsWindow);
	       this.setSelectedFrame(colorsWindow);   
	       
	       // listen for close events
	       colorsWindow.addInternalFrameListener(new InternalFrameAdapter()
	       {
	          public void internalFrameClosing(InternalFrameEvent e)
	          {
	              colorsWindowOpen = false;
	              colorsWindow = null;
	          }         
	       });
	       
	       colorsWindowOpen = true;
       }
       
       try
       {
           colorsWindow.setSelected(true);
       }
       catch (PropertyVetoException e)
       {
       }
       
       return colorsWindow;
   }

   
   public void openRoleWindow()
   {
       if( roleWindowOpen == true ) return;

       Journal journal = ivanhoeGame.getJournal();
       GameInfo gameInfo = ivanhoeGame.getGameInfo();
       PropertiesManager propertiesManager = ivanhoeGame.getPropertiesManager();
       RoleManager roleManager = ivanhoeGame.getRoleManager();
       ReferenceResourceManager bookmarkManager = ivanhoeGame.getBookmarkManager();
       roleWindow = new RolePlayWindow(journal,gameInfo,propertiesManager,roleManager,bookmarkManager);
       add(roleWindow);
       roleWindow.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);

       int w = (int)(getWidth()* ROLE_WINDOW_PERCENT_WIDTH);
       int h = (int)(getHeight()* ROLE_WINDOW_PERCENT_HEIGHT);
       roleWindow.setSize(w,h);       
       roleWindow.setVisible(true);      
       centerWindowOnWorkspace(roleWindow);
       this.moveToFront(roleWindow);
       this.setSelectedFrame(roleWindow);
       
       // listen for close events
       roleWindow.addInternalFrameListener(new InternalFrameAdapter()
       {
          public void internalFrameClosing(InternalFrameEvent e)
          {
              roleWindowOpen = false;
          }         
       });
       
       try
       {
           roleWindow.setSelected(true);
           roleWindowOpen = true;
       }
       catch (PropertyVetoException e)
       {
       }      
   }
   
   public void openAnnotationWindow( DocumentEditor docEditor, int start, int end )
   {
       // TODO: send the window to the front and update the selection.
       if( annotationWindowOpen == true ) return;
       
       AnnotationWindow annotationWindow = new AnnotationWindow(docEditor,start,end);
       
       add(annotationWindow);
       annotationWindow.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);

       int w = 350;
       int h = 350;
       annotationWindow.setSize(w,h);       
       annotationWindow.setVisible(true);      
       centerWindowOnWorkspace(annotationWindow);
       this.moveToFront(annotationWindow);
       this.setSelectedFrame(annotationWindow);
       
       // listen for close events
       annotationWindow.addInternalFrameListener(new InternalFrameAdapter()
       {
          public void internalFrameClosing(InternalFrameEvent e)
          {
              annotationWindowOpen = false;
          }         
       });
       
       try
       {
           annotationWindow.setSelected(true);
           annotationWindowOpen = true;
       }
       catch (PropertyVetoException e)
       {
       }             
   }
   
   public void openDiscussionWindow()
   {   
       if( discussionWindowOpen == true ) return;

       Discussion discussion = ivanhoeGame.getDiscussion();       
       DiscussionWindow discussionWindow = new DiscussionWindow(discussion, ivanhoeGame.isWritable());
       add(discussionWindow);
       discussionWindow.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);

       int w = (int)(getWidth()* DISCUSSION_WINDOW_PERCENT_WIDTH);
       int h = (int)(getHeight()* DISCUSSION_WINDOW_PERCENT_HEIGHT);
       discussionWindow.setSize(w,h);       
       discussionWindow.setVisible(true);      
       centerWindowOnWorkspace(discussionWindow);
       this.moveToFront(discussionWindow);
       this.setSelectedFrame(discussionWindow);       

       // listen for close events
       discussionWindow.addInternalFrameListener(new InternalFrameAdapter()
       {
          public void internalFrameClosing(InternalFrameEvent e)
          {
              discussionWindowOpen = false;
          }         
       });

       try
       {
           discussionWindow.setSelected(true);
           discussionWindowOpen = true;
       }
       catch (PropertyVetoException e)
       {
       }      
   }

   /**
    * Open a search results window
    */
   public void openSearchWindow()
   {
      if( searchWindowOpen == true ) return;
      
      SearchResultsWindow sr = new SearchResultsWindow( ivanhoeGame.getDiscourseField(), 
              											ivanhoeGame.getRoleManager().getCurrentRole());
      
      add(sr);
      sr.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
      centerWindowOnWorkspace(sr);

      this.moveToFront(sr);
      this.setSelectedFrame(sr);
      
      // listen for close events
      sr.addInternalFrameListener(new InternalFrameAdapter()
      {
         public void internalFrameClosing(InternalFrameEvent e)
         {
             searchWindowOpen = false;
         }         
      });

      try
      {
         sr.setSelected(true);
         searchWindowOpen = true;
      }
      catch (PropertyVetoException e)
      {
      }
   }
   
   /**
    * Open an editor for creating doc-to-doc, internal & url links
    * @param doc
    * @param start
    * @param end
    */
   public void openLinkEditor(IvanhoeDocument doc, int start, int end)
   {
      if( linkEditorWindowOpen == true ) return;
      
      DiscourseField discourseField = ivanhoeGame.getDiscourseField();
      LinkEditor editor = new LinkEditor( this, discourseField, doc, start, end );
      add(editor);
      centerWindowOnWorkspace(editor);

      // listen for close events
      editor.addInternalFrameListener(new InternalFrameAdapter()
      {
         public void internalFrameClosing(InternalFrameEvent e)
         {
             linkEditorWindowOpen = false;
         }         
      });
      
      try
      {
         editor.setSelected(true);
         linkEditorWindowOpen = true;
      }
      catch (PropertyVetoException e)
      {
      }
   }

   /**
    * make each open window into a square of the same dimensions such that all
    * open widows are visible at once
    */
   public void tileWindows()
   {
      JInternalFrame[] windows = this.getAllFrames();
      if (windows.length == 0)
      {
         return;
      }

      if (windows.length <= 2)
      {
         this.verticalStackWindows();
         return;
      }

      int numWindows = getVisibleWindowCount(windows);
      int rowSize = (int) Math.sqrt(numWindows);
      if (rowSize * rowSize < windows.length)
      {
         rowSize++;
      }

      int windowHeight = getHeight() / rowSize;
      int windowWidth = (getWidth()-Drawer.TAB_THICKNESS) / rowSize;
      int cnt = 0;
      int x, y;
      x = Drawer.TAB_THICKNESS-5; y = 0;
      for (int i = 0; i < numWindows; i++)
      {
         windows[i].setSize(windowWidth, windowHeight);
         windows[i].setLocation(x, y);
         cnt++;
         if (cnt >= rowSize)
         {
            x = Drawer.TAB_THICKNESS-5;
            y += windowHeight;
            cnt = 0;
         }
         else
         {
            x += windowWidth;
         }
      }
   }

   /**
    * Make a vertical stack of all open windows where the height of each window
    * is the height of the workspace and the widths are equal portions of the
    * total workspace width
    */
   public void verticalStackWindows()
   {
      JInternalFrame[] windows = this.getAllFrames();
      if (windows.length == 0)
         return;

      int windowWidth = (getWidth()-Drawer.TAB_THICKNESS) / getVisibleWindowCount(windows);
      int x = Drawer.TAB_THICKNESS-5;
      for (int i = 0; i < windows.length; i++)
      {
         windows[i].setSize(windowWidth, getHeight());
         windows[i].setLocation(x, 0);
         x += windowWidth;
      }
   }

   /**
    * Make a horozontal stack of all open windows where the width of each window
    * is the width of the workspace and the heights are equal portions of the
    * total workspace height
    */
   public void horizontalStackWindows()
   {
      JInternalFrame[] windows = this.getAllFrames();
      if (windows.length == 0)
         return;

      int windowHeight = getHeight() / getVisibleWindowCount(windows);
      int y = 0;
      for (int i = 0; i < windows.length; i++)
      {
         windows[i].setSize(getWidth()-Drawer.TAB_THICKNESS, windowHeight);
         windows[i].setLocation(Drawer.TAB_THICKNESS-7, y);
         y += windowHeight;
      }
   }
   
   private int getVisibleWindowCount(JInternalFrame[] windows)
   {
      int cnt = 0;
      for (int i=0; i<windows.length;i++)
      {
         if (windows[i].isVisible())
         {
            cnt++;
         }
      }
      return cnt;
   }
   
   /**
    * Close all windows in the workspace
    */
   public void closeAllWindows()
   {
      JInternalFrame[] windows = this.getAllFrames();
      for (int i = 0; i < windows.length; i++)
      {         
         windows[i].doDefaultCloseAction();
      }
   }
   
   /**
    * Release the animation timer.
    */
   public void releaseTimer()
   {
       if (this.timer != null) this.timer.stop();
   }
   
   private static void centerWindow(Container wnd, Dimension spaceDim)
   {
      final int screenHeight = spaceDim.height;
      final int screenWidth = spaceDim.width;
      final int xPos = Math.max(0, (screenWidth - wnd.getWidth()) / 2);
      final int yPos = Math.max(0, (screenHeight - wnd.getHeight()) / 2); 

      wnd.setLocation(xPos, yPos);
   }
   
   public void centerWindowOnWorkspace(Container wnd)
   {
       Dimension dim = getBounds().getSize();
       centerWindow(wnd, dim);
   }
   
   /**
    * Static method used to center an object on the screen
    * @param wnd the dialog to center
    */
   public static void centerWindow(Container wnd)
   {
      Toolkit tk = Toolkit.getDefaultToolkit();
      centerWindow(wnd, tk.getScreenSize());
   }

   /**
    * @return Returns the frameRate.
    */
   public double getFrameRate()
   {
       return frameRate;
   }
   
   /**
    * @return Returns the navigator.
    */
   public DiscourseFieldNavigator getNavigator()
   {
       return navigator;
   }
   
	/**
	 * @return Returns the timer.
	 */
	public Timer getTimer()
	{
		return timer;
	}

   /**
    * @param documentTitle
    */
   public void closeDocumentWindows(String documentTitle)
   {
      JInternalFrame[] windows = this.getAllFrames();
      for (int i = 0; i < windows.length; i++)
      {
         final JInternalFrame frame = windows[i];
         if (frame instanceof DocumentEditor )
         {
            final DocumentEditor docEditor = (DocumentEditor)frame;
            if (docEditor.getDocument().getTitle().equals(documentTitle))
            {
               docEditor.dispose();
            }
         }
         else if ( frame instanceof DocumentStemmaWindow )
         {
            final DocumentStemmaWindow stemmaWindow = (DocumentStemmaWindow) frame;
            final DocumentInfo stemmaDoc = stemmaWindow.getStemmaManager().getDocumentInfo(); 
            if ( stemmaDoc.getTitle().equals(documentTitle) )
            {
               stemmaWindow.dispose();
            }
         }
      }
   }
   
   /**
    * @param document
    *       Specific document to be closed
    */
   public void closeDocumentWindow(IvanhoeDocument document)
   {
      JInternalFrame[] windows = this.getAllFrames();
      for (int i = 0; i < windows.length; i++)
      {
         if (windows[i] instanceof DocumentEditor )
         {
            DocumentEditor docEditor = (DocumentEditor)windows[i];
            if (docEditor.getDocument().equals(document))
            {
               docEditor.dispose();
            }
         }
      }
   }
   
   /**
    * Exit Ivanhoe
    */
   public void exitGame(boolean promptForPublish)
   {
      closeAllWindows();
      
      if( ivanhoeGame != null )
      {
          // save the information in the role window
          if( roleWindowOpen && roleWindow != null )
          {
              roleWindow.save();
          }
          
	      DiscourseField discourseField = ivanhoeGame.getDiscourseField();
	      
	      // save all if DF is initialized
	      if ( discourseField != null && discourseField.isInitialized() && Ivanhoe.getProxy().isConnected())
	      {
	         CurrentMove currentMove = discourseField.getCurrentMove(); 
	         if (currentMove != null && currentMove.isStarted())
	         {
                 if (promptForPublish)
                 {
    	            int resp = JOptionPane.showConfirmDialog(ivanhoeFrame, 
    	               "<html>You have an unpublished move." +
    	               "<br>Would you like to publish it before exiting?", 
    	               "Publish Move", JOptionPane.YES_NO_CANCEL_OPTION);
    	            if (resp == JOptionPane.CANCEL_OPTION)
    	            {
    	               return;
    	            }
    	            else if (resp == JOptionPane.YES_OPTION)
    	            {                
    	                MovePublishWizard wizard = new MovePublishWizard( ivanhoeFrame,
                                                                          ivanhoeGame.getDiscourseField().getCurrentMove(),
    	                        										  ivanhoeGame.getJournal(),
    	                        										  ivanhoeGame.getCategoryManager(),
    	                        										  ivanhoeGame.getDiscourseField().getDiscourseFieldTimeline() );
    	                wizard.startWizard();
    	            }
                 }
                 
	            // save the current move
	            currentMove.save();
	         }            
	      }
      }
      
      // disconnect from the host and exit the application
      Ivanhoe.shutdown();      
   }
   
   /**
    * Returns the InfoPanel used to display popup messages in this Workspace. All view
    * elements that are children of the workspace should use this same info panel so that
    * it can be cleared properly across component boundaries. 
    * @return the action info popup
    */
   public InfoPanel getInfoPanel()
   {
      return actionInfo;
   }

	/**
	 * @param timeControl The timeControl to set.
	 */
	public void setTimeControl(DiscourseFieldTimeSlider timeControl)
	{
	    this.timeControl = timeControl;
	}
	
   /**
    * @return Returns the timeControl.
    */
   public DiscourseFieldTimeSlider getTimeControl()
   {
       return timeControl;
   }
/**
 * @return Returns the menu.
 */
public GameActionToolbar getGameActionToolbar()
{
    return menu;
}

public PropertiesManager getPropertiesManager()
{
    return ivanhoeGame.getPropertiesManager();
}

    public void enableLinkSelectionBars(LinkEditor linkEditor)
    {
        this.linkEditor = linkEditor;
        
        JInternalFrame[] frames = this.getAllFrames();
        
        for (int i = 0; i < frames.length; i++)
        {
           if (frames[i] instanceof DocumentEditor)
           {
              DocumentEditor editor = (DocumentEditor) frames[i];
              editor.enableLinkSelectionBar(linkEditor);
           }
        }       
    }
    
    public void disableLinkSelectionBars()
    {
        this.linkEditor = null;
        
        JInternalFrame[] frames = this.getAllFrames();
        
        for (int i = 0; i < frames.length; i++)
        {
           if (frames[i] instanceof DocumentEditor)
           {
              DocumentEditor editor = (DocumentEditor) frames[i];
              editor.disableLinkSelectionBar();
           }
        }       
    }

}