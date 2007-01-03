/*
 * Created on Jan 10, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.ReferenceResourceManager;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.IReferenceResourceListener;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.ReferenceResource;

/**
 * @author Nick
 *
 */
public class ResourcesPanel extends JPanel implements IReferenceResourceListener
{
    private ReferenceResourceManager bookmarkManager;
    private JEditorPane resourceInfo; 
    
    public ResourcesPanel( ReferenceResourceManager bookmarkManager )
    {
        this.bookmarkManager = bookmarkManager;
        bookmarkManager.addListener(this);        
        setLayout( new BorderLayout() );
        add( createResourcePane(), BorderLayout.CENTER );
        add( createButtonPanel(), BorderLayout.SOUTH );        
    }
    
    private JScrollPane createResourcePane()
    {
        resourceInfo = new JEditorPane();
        resourceInfo.setEditable(false);
        resourceInfo.setContentType("text/html");
        resourceInfo.setMargin(new Insets(10,10,10,10));
        
        WebLinkListener webLinkListener = new WebLinkListener();
        resourceInfo.addHyperlinkListener(webLinkListener);
        
        String resourceSummary = generateResourceSummary();
        resourceInfo.setText(resourceSummary);
        
        JScrollPane sp = new JScrollPane(resourceInfo);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        Border bufferBorder = new EmptyBorder(10,10,5,10);
        Border lineBorder = LineBorder.createGrayLineBorder();
        sp.setBorder(new CompoundBorder(bufferBorder,lineBorder));
        
        return sp;
    }
    
    private JPanel createButtonPanel()
    {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout( new BorderLayout() );
        buttonPanel.setBorder( new EmptyBorder(0,10,10,10) );
        
        JButton editReferenceListButton = new JButton("edit resources...");
        editReferenceListButton.setFont(IvanhoeUIConstants.SMALL_FONT);
        
        editReferenceListButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                handlEditReferenceList();
            }});
        
        buttonPanel.add(editReferenceListButton, BorderLayout.WEST );
        
        return buttonPanel;           
    }
    
    private void handlEditReferenceList()
    {
        ManageResourcesDialog dialog = new ManageResourcesDialog(this.bookmarkManager);
        dialog.show();
    }
    
    private String generateResourceSummary()
    {
       StringBuffer txt = new StringBuffer();
       txt.append("<html><body style=font-size:20 font-family: Verdana><b>Resources</b>");
       txt.append("<p style=font-size:12 font-family: Verdana>" );
       txt.append( "These resources are shared by all players in this game. You can share links here with other Ivanhoe players.<br><br>");       
       
       List bookmarks = bookmarkManager.getReferences();
       
       for( Iterator i = bookmarks.iterator(); i.hasNext(); )
       {
           ReferenceResource bookmark = (ReferenceResource) i.next();
           
           txt.append( "<b>"+bookmark.getLabel()+"</b><br>");
           txt.append( bookmark.getSummary()+"<br>");
           txt.append( "<a href=\""+bookmark.getUrl()+"\">"+bookmark.getUrl()+"</a><br>");           
           txt.append( "<br>" );
       }
       
       txt.append("</p></body></html>");
       
       return txt.toString();
    }
    
    private class WebLinkListener implements HyperlinkListener 
    {       
        public void hyperlinkUpdate(HyperlinkEvent e) 
        {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
	        {
                URL targetURL = e.getURL();
                
                try
	            {
                    BasicService bs = (BasicService) ServiceManager.lookup( "javax.jnlp.BasicService" );
	                bs.showDocument(targetURL);
	            } 
	            catch (UnavailableServiceException e1)
	            {
	                SimpleLogger.logError("Connection problem trying to open browser to page: "+targetURL.toString());
	            } 
	        }
        }
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.model.metagame.IBookMarkListener#bookmarksChanged()
     */
    public void resourceChanged()
    {
        String updatedSummary = generateResourceSummary();
        resourceInfo.setText(updatedSummary);        
    }
}
