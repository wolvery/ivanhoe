/*
 * Created on May 5, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator;

import java.awt.*;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Date;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.CurrentAction;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DataNotFoundException;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DocumentVersionManager;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.StartingMoveEvent;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.document.DocumentEditor;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.ActionType;
import edu.virginia.speclab.ivanhoe.shared.data.Link;
import edu.virginia.speclab.ivanhoe.shared.data.LinkType;
import edu.virginia.speclab.ivanhoe.shared.data.Move;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;

/**
 *  This class manages the display and interaction with the document arcs that surround the
 *  discourse field.  
 * @author Nick Laiacona
 */
public class DiscourseFieldState
{
    private DiscourseFieldNavigator navigator;
    private LinkedList documentAreaList;
    private boolean documentAreaOrderDirty;
    private Date stateTime;
    private static int initialDocumentWeight = 1;
    
    private DocumentEditor lastEditor;
  
    private DiscourseFieldState( DiscourseFieldNavigator nav, Date stateTime )
    {
        navigator = nav;
        this.stateTime = stateTime; 
        documentAreaList = new LinkedList();
        documentAreaOrderDirty = true;
    }
    
    /**
     * Create a successor to this state which contains all of the documents 
     * found in this state. The new state is automatically associated with the 
     * DiscourseFieldNavigator of the previous state.
     * @param newTime the time corresponding to the field state
     * @return a new DiscourseFieldState
     */
    public DiscourseFieldState createNewState(Date newTime)
    {
        DiscourseFieldState newState = new DiscourseFieldState(navigator, newTime);
        
        for( Iterator i = documentAreaList.iterator(); i.hasNext(); )
        {
            DocumentArea area = (DocumentArea)i.next();
            newState.addDocument(area.getDocumentName(), area.getPublishedTime());
        }
        
        newState.updateDocumentAreas();
        
        return newState;
    }
    
    /**
     * Create a new DiscourseFieldState with no documents. Associate it with 
     * a given DiscourseFieldNavigator. 
     * @param navigator The 
     * @return A new DiscourseFieldState.
     */
    public static DiscourseFieldState createInitialState( DiscourseFieldNavigator navigator )
    {
        Date startTime =  navigator.getDiscourseFieldTimeline().getBeginTime();
        return createCleanState(navigator, startTime );
    }
    
    public static DiscourseFieldState createCleanState(DiscourseFieldNavigator navigator, Date stateTime)
    {
    	return new DiscourseFieldState(navigator, null);
    }
        
    private void addDocumentArea( DocumentArea area )
    {
        documentAreaList.add(area);
        documentAreaOrderDirty = true;
    }
    
    public void clearHighlight()
    {
    	boolean isDirty = false;
        for( int i = 0; i < documentAreaList.size(); i++ )
        {
            DocumentArea docArea = (DocumentArea) documentAreaList.get(i);
            if (docArea.highlighted)
            {
            	isDirty = true;
            	docArea.highlighted = false;
            }
        }
        
        if (isDirty)
        {
        	navigator.dirty();
        }
    }
    
    private void highlightArea( DocumentArea area )
    {
        boolean dirty = false;
        
        for( int i = 0; i < documentAreaList.size(); i++ )
        {
            DocumentArea docArea = (DocumentArea) documentAreaList.get(i);
     
            if( docArea == area && docArea.highlighted == false )
            {
                docArea.highlighted = true;
                dirty = true;
            }
            
            if( docArea != area && docArea.highlighted == true )
            {
                docArea.highlighted = false;
                dirty = true;
            }
        }
        
        if( dirty == true ) navigator.dirty();
        
    }
    
    private boolean doSelection( DocumentArea docArea )
    {
        DocumentInfo docInfo =
                navigator.getDiscourseField().getDocumentInfo(docArea.getDocumentName());
        Workspace.instance.openStemmaWindow( docInfo, navigator.getDiscourseField() );
           
        return true;
    }
    
    public boolean testHighlight( Point p )
    {
	    for( Iterator i = documentAreaList.iterator(); i.hasNext(); )
	    {
	        DocumentArea docArea = (DocumentArea) i.next();
	        if( docArea.hitTest(p) == true )
	        {
				highlightArea(docArea);
				if ( docArea.getDocumentLabelArea().contains(p) )
				{
					navigator.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				return true;
			}
		}
	    return false;
    }

    public boolean testSelection( Point p, int numClicks )
    {
        // if the last editor we attempted to open is still loading,
        // wait until it is finished before opening a new one.
        if( lastEditor != null )
        {
            if( lastEditor.isLoading() == true )
                return false;
            else
                lastEditor = null;            
        }
    	
	    
        if( documentAreaList != null )
        {
        	//We're only interested in single clicks if they're on the label
        	for ( Iterator i = documentAreaList.iterator(); i.hasNext(); )
        	{
        		DocumentArea docArea = (DocumentArea)i.next();
        		if ( docArea.getDocumentLabelArea().contains(p) )
        		{
        			return doSelection(docArea);
        		}
        	}
        	
        	// If we didn't get a click on a label, then we don't care unless
        	// it's a double click 
        	if ( numClicks < 2 )
        	{
        		return false;
        	}
        	
	        // determine if a document area was hit and if so open the 
	        // corresponding document editor.
	        for( Iterator i = documentAreaList.iterator(); i.hasNext(); )
	        {
	            DocumentArea docArea = (DocumentArea) i.next();
	            if( docArea.hitTest(p) == true )
	            {
	                return doSelection(docArea);                
	            }
	        }
        }
        
        return false;
    }
    
    /**
     * Get the offset and length of the arc corresponding to this document.
     * @param docName The title of the document to look up.
     * @return
     */
    public DocumentArc getDocumentArc( String docName )
    {
        for( Iterator i = documentAreaList.iterator(); i.hasNext(); )
        {
            DocumentArea docArea = (DocumentArea) i.next();
            if( docArea.getDocumentName().equals(docName) == true )
            {
                return docArea.getDocumentArc();
            }
        }
                
        return new DocumentArc(0,0);
    }
    
    /**
     * Paints the document areas and label text.
     * @param g The target of the painting
     * @param aspectW The aspect width as a percentage of optimal width co-nstant.
     * @param aspectH  The aspect height as a percentage of optimal height constant.
     */
    public void paint( Graphics g, double aspectW, double aspectH )
    {
    	double constrainingAspect = (aspectW < aspectH) ? aspectW : aspectH;
    	
    	if (documentAreaOrderDirty)
    	{
    		updateDocumentAreas();
    	}
    	
    	// first paint the document areas
        for( Iterator i = documentAreaList.iterator(); i.hasNext(); )
        {
            DocumentArea docArea = (DocumentArea) i.next();
            docArea.paintDocumentArea(g,constrainingAspect);
        }
        
        // then paint the document labels
        for( Iterator i = documentAreaList.iterator(); i.hasNext(); )
        {
            DocumentArea docArea = (DocumentArea) i.next();
            docArea.paintDocumentLabel(g,constrainingAspect);
        }

    }
    
    private double getMoveWeightOnDocument(String document, Move move)
    {
		DocumentVersionManager documentVersionManager = navigator.getDiscourseField().getDocumentVersionManager();
		
    	boolean thisDocTouched = false;
    	HashSet touchedDocs = new HashSet();
        
        for (Iterator i=move.getActions(); i.hasNext(); )
    	{			
    		IvanhoeAction action = (IvanhoeAction)(i.next());
		    DocumentVersion actionDocVersion = documentVersionManager.getDocumentVersion(action);
            touchedDocs.add(actionDocVersion.getDocumentTitle());
            
    		if ( document.equals(actionDocVersion.getDocumentTitle()) )
    		{
    			thisDocTouched = true;
    		} 
    		else if (action.getType().equals(ActionType.LINK_ACTION))
    		{
    			Link linkAction = (Link)(action.getContent());
    			if (linkAction.getType().equals(LinkType.INTERNAL))
    			{
                    final int dvID = linkAction.getLinkTag().getDocumentVersionID();
					DocumentVersion version = documentVersionManager.getDocumentVersion(dvID);
    				String linkedDocument = version.getDocumentTitle(); 
    				if ( linkedDocument != null )
    				{
    					if (document.equals(linkedDocument))
    					{
    						thisDocTouched = true;
    					}
    				}
				}
    		}
    		
    	}
        
 	    return (thisDocTouched ? 1.0 / touchedDocs.size() : 0.0);
    }
    
    /**
     * Add a document area that represents this document. 
     * @param documentName The title of the document
     * @param moveDate The time that the document was published
     */
    public void addDocument( String documentName, Date moveDate )
    {
    	HashSet participants = new HashSet();
    	Date documentDate;
    	double weight = 0.0;
    	boolean inProgress = false;
    	
    	DocumentInfo documentInfo = navigator.getDiscourseField().getDocumentInfo(documentName);
    	// Get the time at which this was created
    	documentDate = documentInfo.getCreateTime();
    	
    	// Set the weight for starting documents
    	if (documentInfo.isStartingDocument())
    	{
    		weight = initialDocumentWeight;
    	}
    	
    	// Determine the new document's weight and the participants in that document
    	LinkedList timeline = navigator.getDiscourseFieldTimeline().getTimeline();
    	
    	// If this is the initial state, then none of the moves will affect it.
    	if (navigator.getDocumentAreaManager().isFirstState(this))
    	{
    		timeline = new LinkedList();
    	}
    	
    	for (Iterator i=timeline.iterator(); i.hasNext(); )
    	{
    		MoveEvent me = (MoveEvent)(i.next());
    		if (me instanceof StartingMoveEvent)
    		{
    			// Doesn't have participants and only touches the initial documents
    			continue;
    		}
    		
    		Move move = me.getMove();
    		
    		if (stateTime != null && stateTime.compareTo(move.getSubmissionDate()) < 0)
    		{
    			// This move is from the future!
    			break;
    		}
    		
    		// Add the weight of this move
            double moveWeight = 0.0f;
            try 
            {
                moveWeight = getMoveWeightOnDocument(documentName, move);
            }
            catch (DataNotFoundException dnfe)
            {
                SimpleLogger.logError("Error while looking up weight of move "+move.getId()
                        +" on document ["+documentName+"]", dnfe);
            }
    		weight += moveWeight;
    		if (moveWeight > 0.0)
    		{
    			// Then this move touched this document.  Therefore, the move
    			// owner is a participant
    			participants.add(move.getRoleName());
    		}
    	}
    	
    	Move move = navigator.getDiscourseField().getCurrentMove();
    	if (move != null)
    	{
    		for (Iterator i = move.getActions(); i.hasNext();)
    		{
    			IvanhoeAction action = (IvanhoeAction)(i.next());
                
                if (action instanceof CurrentAction)
                {
                    continue;
                }
                
                DocumentVersion actionDocVersion =
                    navigator.getDiscourseField().getDocumentVersionManager().getDocumentVersion(action);
                
    			if (documentName.equals(actionDocVersion.getDocumentTitle()))
    			{
    				participants.add(move.getRoleName());
    				break;
    			}
    		}
    	}
    	
    	// In the case that none of the moves touched it, it must have been
    	// added in the current move, so we'll give it a weight of 1.0 for now
    	if (weight == 0.0)
    	{
    		weight = 1.0; // / move.getActionCount();
    	}
    	
    	// construct the document area and add it to the list
        DocumentArea docArea = new DocumentArea( navigator, documentName,
        		documentDate, moveDate, participants, weight, inProgress);
        addDocumentArea(docArea);
        updateDocumentAreas(); // TODO: this is inefficient and should only happen when a dfState is done
    }
        
    /**
     * Removes the corresponding document area from the visualization. 
     * @param documentName The title of the document.
     */
    public void removeDocument( String documentName )
    {
        // remove the document area
        for( Iterator i = documentAreaList.iterator(); i.hasNext(); )
        {
            DocumentArea docArea = (DocumentArea) i.next();
            if( docArea.getDocumentName().equals(documentName) == true )
            {
            	documentAreaList.remove(docArea);
            	break;
            }
        }
        documentAreaOrderDirty = true;
    }
    
    public void updateDocumentAreas()
    {
    	double totalWeight = 0;
    	
    	Collections.sort(documentAreaList, new DocumentAreaComparator());

        for( Iterator i = documentAreaList.iterator(); i.hasNext(); )
        {
            DocumentArea docArea = (DocumentArea) i.next();
           	totalWeight += docArea.weight;
        }
        
        int numDocAreas = documentAreaList.size();
        int availableSpace = 360 - (numDocAreas * DocumentArea.MINIMUM_ARC_LENGTH);
        
         // recalculate the document areas
        double previousEndAngle = 90.0;
        if ( documentAreaList.size() == 1)
        {
            // special case for when there's only one document area
            previousEndAngle = 180.0;
        }
        
        for( Iterator i = documentAreaList.iterator(); i.hasNext(); )
        {
            DocumentArea docArea = (DocumentArea) i.next();
            
            if( availableSpace <= 0 )
            {
                // ridiculous number of documents, give them all a sliver.
                docArea.setArcLength(-360.0 / numDocAreas );
            }
            else
            {
                // typical case, every angle gets the min length plus a portion of the available space based on weight.
                double arcLength = (availableSpace * (docArea.weight / totalWeight)) + DocumentArea.MINIMUM_ARC_LENGTH; 
                docArea.setArcLength(arcLength*-1.0);    
            }
           	
           	docArea.setAngle(previousEndAngle); 
           	previousEndAngle = docArea.getAngle() + docArea.getArcLength();
        }
        
        /* The following is here because when we update the DocumentAreas, we 
         * need to update everything that depends on them, specifically, the
         * ActionArcs and DocumentStateArcs whose sizes and positions are
         * determined by the DocumentArea.  Moving to the current tick is the
         * easiest way to do this. 
         */
        navigator.getDiscourseFieldCircle().moveToTick(navigator.getDiscourseFieldTime().getTick());
        
        documentAreaOrderDirty = false;
        navigator.dirty();
    }
    
    public static void setInitialDocumentWeight(int newWeight)
    {
    	initialDocumentWeight = newWeight;
    }
    
    public boolean isDirty()
    {
    	return documentAreaOrderDirty;
    }
    
    private class DocumentAreaComparator implements Comparator
	{
    	public int compare(Object o1, Object o2)
		{
    		int compareResult;
    		DocumentArea da1, da2;
    		da1 = (DocumentArea)o1;
    		da2 = (DocumentArea)o2;
    		
    		// publish time of null means it's in the current move
    		if (da1.getPublishedTime() == null) 
    		{
    			if (da2.getPublishedTime() == null)
    			{
    				compareResult = 0;
    			}
    			else
    			{
    				compareResult = 1;
    			}
    		}
    		else
    		{
    			if (da2.getPublishedTime() == null)
    			{
    				compareResult = -1;
    			}
    			else
    			{
    				compareResult = da1.getPublishedTime().compareTo(da2.getPublishedTime());
    			}
    		}

    		if (compareResult == 0)
    		{
    			compareResult = da1.getCreatedTime().compareTo(da2.getCreatedTime());
    		}

    		return compareResult;
		}
	}
}
