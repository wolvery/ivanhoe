/*
 * Created on Aug 11, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.view.document;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.DiscourseFieldNavigator;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.time.DiscourseFieldTimeControls;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.time.DiscourseFieldTimeSlider;
import edu.virginia.speclab.ivanhoe.client.game.view.stemma.StemmaManager;
import edu.virginia.speclab.ivanhoe.client.game.view.stemma.StemmaView;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.BlackBox;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeButton;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeStyleInternalFrame;
import edu.virginia.speclab.ivanhoe.shared.blist.BArrayList;
import edu.virginia.speclab.ivanhoe.shared.blist.BList;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;

/**
 * This is the top level view window which contains the document stemma.
 * @author benc
 */
public class DocumentStemmaWindow extends IvanhoeStyleInternalFrame
{
    private final StemmaManager stemmaManager;
    private final StemmaView stemmaView;
    private final BlackBox controlBox;
    
    public DocumentStemmaWindow(DocumentInfo docInfo, DiscourseField discourseField, DiscourseFieldTimeSlider slider, DiscourseFieldNavigator navigator)
    {
        super(docInfo.getTitle()+"- Stemma View");
        
        this.stemmaManager = new StemmaManager(docInfo, discourseField, navigator);
        this.stemmaView = new StemmaView(stemmaManager, discourseField.getDiscourseFieldTimeline(), discourseField.getDiscourseFieldTime(), this);

        DiscourseFieldTimeControls timeControls = new DiscourseFieldTimeControls(slider);
        timeControls.init(discourseField.getDiscourseFieldTimeline(),discourseField.getDiscourseFieldTime());
        
        this.controlBox = new BlackBox(BoxLayout.LINE_AXIS);
        this.controlBox.add( timeControls );
        this.controlBox.add( Box.createHorizontalGlue() );
        this.controlBox.add( new IvanhoeButton( stemmaView.getZoomInAction()) );
        this.controlBox.add( new IvanhoeButton( stemmaView.getZoomOutAction()) );
        
        // create the header panel  
        JPanel headerPanel = new HeaderPanel(docInfo,true);
        
    	this.getContentPane().setLayout(new BorderLayout());
    	this.getContentPane().add( headerPanel, BorderLayout.NORTH);
    	this.getContentPane().add( controlBox, BorderLayout.SOUTH );
    	this.getContentPane().add( stemmaView, BorderLayout.CENTER );
    	
        BList docVersions = new BArrayList(
                discourseField.getDocumentVersionManager().getDocumentVersions(docInfo) );
        Collections.sort(docVersions, new Comparator() {
            public int compare(Object o1, Object o2)
            {
                final Date date1 = ((DocumentVersion) o1).getDate(); 
                final Date date2 = ((DocumentVersion) o2).getDate();
                
                return date1.compareTo(date2);
            }
        });
        
        if( !docVersions.isEmpty() ) {
            DocumentVersion firstVersion = (DocumentVersion) docVersions.get(0);
            int firstVersionTick = discourseField.getDiscourseFieldTimeline().getTick(firstVersion);
            stemmaView.setStartingPointOffset(firstVersionTick);
        }

        int windowWidth = 2*Workspace.instance.getWidth()/3;
        int windowHeight = 2*Workspace.instance.getHeight()/3; 
        
        setSize(windowWidth,windowHeight);

        // pan the stemma to the center of the window
        float startOffset = stemmaView.getStartPointOffset() * StemmaView.VERTICAL_TICK_SPACING;        
        stemmaView.smoothPan(windowWidth*.5f,(windowHeight*.5f)+startOffset);
        
        setVisible(true);
        
        for (Iterator i=docVersions.iterator(); i.hasNext(); )
        {
            DocumentVersion version = (DocumentVersion) i.next();
            stemmaManager.addDocumentVersion( version );
        }
        
        this.addInternalFrameListener( new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent e) {
                DocumentStemmaWindow.this.stemmaManager.stemmaClosing();
            }
        });
    }
    
    public StemmaManager getStemmaManager()
    {
        return this.stemmaManager;
    }
}
