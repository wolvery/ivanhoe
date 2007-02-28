/*
 * Created on Jul 28, 2005
 */
package edu.virginia.speclab.ivanhoe.client.game.view.stemma;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTime;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTimeline;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.IDiscourseFieldTimeListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.document.InfoPanel;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.Role;

/**
 * @author benc
 */
public class StemmaView extends
        JPanel {
	
	public final static int VERTICAL_TICK_SPACING = 48;

    private final static float SCALE_UP_FACTOR = 4f/3f;
    private final static float SCALE_DOWN_FACTOR = 1f/SCALE_UP_FACTOR;
    private final static float MIN_SCALE = (float) Math.pow(SCALE_DOWN_FACTOR, 6);
    private final static float MAX_SCALE = (float) Math.pow(SCALE_UP_FACTOR, 3);

    private final static int INFO_PANEL_OFFSET_X = 14;
    private final static int INFO_PANEL_OFFSET_Y = 10;
    
    private final GraphLayoutManager graphLayoutManager;
    private final StemmaManager stemmaManager;
    private final AffineTransform modelView;
    private final Container logicalParent;

    private Point lastTranslatePt;
    private float scaleFactor;
    
	private DiscourseFieldTime dfTime;

	private int startPointOffset;
    
    public StemmaView( StemmaManager stemmaManager, DiscourseFieldTimeline dfTimeline, DiscourseFieldTime dfTime, Container container ) {
        super(true);
        this.stemmaManager = stemmaManager;
        this.graphLayoutManager = new GraphLayoutManager(stemmaManager);
        this.modelView = new AffineTransform();
        this.logicalParent = container;
        this.dfTime = dfTime;
        
        this.scaleFactor = 1.0f;
        
        this.setBackground(Color.BLACK);
        this.setForeground(Color.WHITE);
        
        this.stemmaManager.addTreeModelListener(new StemmaModelListener());
        
        StemmaTimeLineListener timeLineListener = new StemmaTimeLineListener(dfTime.getTick());
        dfTime.addListener(timeLineListener);
                        
        StemmaMouseDelegate mouseController = new StemmaMouseDelegate();
        this.addMouseListener(mouseController);
        this.addMouseMotionListener(mouseController);
        this.addMouseWheelListener(mouseController);
    }
    
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2d = Ivanhoe.getGraphics2D(g);                
        g2d.transform(modelView);        
        drawHorizontalRules(g2d);
        graphLayoutManager.drawGraph(g2d);
    }
    
    private void drawHorizontalRules( Graphics2D g2 ) {
    	
    	// draw a set of horizontal rules that represent time line ticks
    	g2.setColor(Color.darkGray);    	
    	for( int i=-100; i < 100; i++ ) {
    		int y = i*VERTICAL_TICK_SPACING;
    		g2.drawLine(-10000,y,10000,y);	
    	}
    	
    	// highlight the line that represents the current time
    	g2.setColor(Color.WHITE);
    	int y = (dfTime.getTick()-startPointOffset)*VERTICAL_TICK_SPACING;
    	g2.drawLine(-10000,y,10000,y);    	    	
    }

    GraphLayoutManager getGraphLayoutManager() {
        return graphLayoutManager;
    }
    
    private void scaleView(float scaleValue) {
        if (scaleValue == 0.0f) {
            throw new IllegalArgumentException("scaleValue must not be equal to zero.");
        }

        // Change scaleValue to limit the total scale to [MIN_SCALE, MAX_SCALE]
        final float valueOverFactor = scaleValue / scaleFactor;
        if (valueOverFactor < MIN_SCALE) {
            scaleValue = MIN_SCALE * scaleFactor;
        } else if (valueOverFactor > MAX_SCALE) {
            scaleValue = MAX_SCALE * scaleFactor;
        }
                
        modelView.scale(scaleValue, scaleValue);
        
        scaleFactor /= scaleValue;
    }
    
    /**
     * Zoom the view in or out in discrete steps
     * 
     * @param clicks
     *      Number of zoom levels to zoom in.  May be negative.
     */
    public void discreteZoom(int clicks) {
        float scaleValue = 1.0f;
        for (int i=0; clicks > i; ++i) {
            scaleValue *= SCALE_UP_FACTOR;
        }
        
        for (int i=0; clicks < i; --i) {
            scaleValue *= SCALE_DOWN_FACTOR;
        }
        
        scaleView(scaleValue);
        dirty();
    }
    
    /**
     * Zoom in or out smoothly.  View scaled by 2^(amount), so -1 is 1/2 zoom;
     * 2 is double. 
     * @param amount
     *      Amount to zoom in or out as specified above
     */
    public void smoothZoom(float amount) {
        float scaleValue = (float) Math.pow(2, amount);
        scaleView(scaleValue);
        dirty();
    }
    
    public void smoothPan(float dx, float dy) {
    	modelView.translate(dx, dy);
        dirty();
    }
    
    void dirty() {
        logicalParent.repaint(20);
        this.repaint(20);
    }
    
    public AbstractAction getZoomInAction() {
        return new ZoomAction(
                "zoom in",
                ResourceHelper.instance.getIcon("res/icons/zoom_in.jpg"),
                2,
                this);
    }
    
    public AbstractAction getZoomOutAction() {
        return new ZoomAction(
                "zoom out",
                ResourceHelper.instance.getIcon("res/icons/zoom_out.jpg"),
                -2,
                this);
    }

    private class StemmaMouseDelegate implements
            MouseListener,   
            MouseMotionListener,
            MouseWheelListener
    {
        public void mouseDragged(MouseEvent e) {
            Point mousePt = e.getPoint();
            
            if (lastTranslatePt != null) {
            	float dx = (float) (mousePt.getX() - lastTranslatePt.getX());
                float dy = (float) (mousePt.getY() - lastTranslatePt.getY());
                
                // Control means zoom
            	if (e.isControlDown()) {
            		smoothZoom( dy / -100.0f );
            	} else {
            		smoothPan(dx, dy); // horizontal movement only
            	}
            }
            
            lastTranslatePt = mousePt;
        }
    
        public void mouseMoved(MouseEvent e) {
            final StemmaNode highlightNode = getNodeAt(e.getPoint());
            final StemmaNode oldHighlightNode = stemmaManager.getSelectedNode();
            final InfoPanel infoPanel = Workspace.instance.getInfoPanel();
            
            if (highlightNode == null) {
                infoPanel.setVisible(false);
                infoPanel.setMsg("");
                
                logicalParent.setCursor(
                        Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));   
            } else {
                final String roleName = highlightNode.getDocumentVersion().getRoleName();
                String msgText;
                if ( Role.GAME_CREATOR_ROLE_NAME.equals(roleName) ) {
                    msgText = "<html><body><p>Initial version added at game initiation</p></body></html>";
                } else {
                    msgText = "<html><body><p>Revision created by: " + roleName + "</p></body></html>";
                }
                
                infoPanel.setLocation(StemmaView.this,e.getPoint().x+INFO_PANEL_OFFSET_X, e.getPoint().y+INFO_PANEL_OFFSET_Y);                
                infoPanel.setVisible(true);
                infoPanel.setMsg(msgText);
                
                logicalParent.setCursor(
                        Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            stemmaManager.setSelectedNode(highlightNode);    
            
            if (highlightNode != oldHighlightNode) {
                StemmaView.this.dirty();
            }
        }
        
        public void mouseEntered(MouseEvent e) { }
        public void mousePressed(MouseEvent e) { }

        public void mouseExited(MouseEvent e) {
            final StemmaNode oldHighlightNode = stemmaManager.getSelectedNode();
            if (oldHighlightNode != null) {
                stemmaManager.setSelectedNode(null);
                StemmaView.this.dirty();
            }
        }
    
        public void mouseClicked(MouseEvent e) {
            final StemmaNode activatedNode = getNodeAt(e.getPoint());
            if (activatedNode != null) {
                Workspace.instance.getInfoPanel().setVisible(false);
                Workspace.instance.getInfoPanel().setMsg("");
                
                stemmaManager.activateNode(activatedNode);
            }
        }
        
        public void mouseReleased(MouseEvent e) {
            switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                lastTranslatePt = null;
                break;
            default:
                break;
            }
        }
    
        public void mouseWheelMoved(MouseWheelEvent e) {
            discreteZoom( -(e.getWheelRotation()) );
        }
        
        private final StemmaNode getNodeAt(Point2D screenPt) {
            Point2D viewSpacePt = new Point2D.Float();
            try {
                modelView.inverseTransform(screenPt, viewSpacePt);
            } catch (NoninvertibleTransformException nte) {
                SimpleLogger.logError("ModelView transform should be invertible!", nte);
            }
            
            return graphLayoutManager.getStemmaAt(viewSpacePt);
        }
    }
    
    private class StemmaTimeLineListener implements IDiscourseFieldTimeListener {
    	
    	private int prevTick;
    	
    	public StemmaTimeLineListener( int initialTick ) {
    		discourseFieldTickChanged(initialTick);
    	}

		public void discourseFieldMoveEventChanged(MoveEvent event) {
			discourseFieldTickChanged(event.getTick());
		}

		public void discourseFieldTickChanged(int tick) {
			int delta = tick - prevTick;
			smoothPan(0, delta*-VERTICAL_TICK_SPACING);
			prevTick = tick;
		}	
    }

    private class StemmaModelListener implements TreeModelListener {
        public void treeNodesChanged(TreeModelEvent e) {
            dirty();
        }
    
        public void treeNodesInserted(TreeModelEvent e) {
            dirty();
        }
    
        public void treeNodesRemoved(TreeModelEvent e) {
            dirty();
        }
        
        public void treeStructureChanged(TreeModelEvent e) {
            dirty();
        }
    }

	public void setStartingPointOffset(int firstVersionTick) {
		this.startPointOffset = firstVersionTick;		
	}

	public int getStartPointOffset() {
		return startPointOffset;
	}
}
