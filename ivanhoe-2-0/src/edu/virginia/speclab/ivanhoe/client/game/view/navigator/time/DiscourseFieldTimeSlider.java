/*
 * Created on May 5, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator.time;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTime;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTimeline;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.IDiscourseFieldTimeListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.ITimeLineListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.IRoleListener;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.IDiscourseFieldNavigatorListener;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.*;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.ImageLoader;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.Role;

/**
 * 
 * @author Nick Laiacona
 */
public class DiscourseFieldTimeSlider extends JPanel implements
        ITimeLineListener, ActionListener, IRoleListener,
        IDiscourseFieldNavigatorListener, IDiscourseFieldTimeListener
{
    private DiscourseFieldTimeline timeline;
    private DiscourseFieldTime discourseFieldTime;
    private RoleManager roleManager;

    private JSlider timeTrack;
    private MarkerMap markerMap;

    private boolean playing;
    private long lastTime;
    private int currentLocation;
    
    private LinkedList listeners;

    // stuff needed for drawing time line marks.
    private ImageLoader imageLoader = new ImageLoader(null);

    private static final Icon spaceMarkerIcon = ResourceHelper.instance.getIcon("res/icons/tilde.jpg");

    public static final int MEDIUM_MARKER_THRESHOLD = 100;
    public static final int LARGE_MARKER_THRESHOLD = 500;
    public static final int TIME_BETWEEN_MOVES = 25;
    public static final int BASE_TIME_UNIT = 10; // slider ticks to
                                                    // milliseconds

    private int timeUnit;

    // Marker map is a collection of marker locations keyed off of location
    // values. Locations are
    // integer counterparts to ticks. Location values must be integers to work
    // with the JSlider
    // control.
    private class MarkerMap extends Hashtable
    {
        /**
         * Initialize this class by passing a time ordered list of MoveEvents.
         * 
         * @param timeline
         */
        public MarkerMap(LinkedList timeline)
        {
            initMap(timeline);
        }

        private void addMoveEventToLocation(int location, MoveEvent event)
        {
            Integer locationObj = new Integer(location);

            MarkerLocation markerLocation;
            if (this.contains(locationObj) == false)
            {
                markerLocation = new MarkerLocation();
                put(locationObj, markerLocation);
            } else
            {
                markerLocation = (MarkerLocation) get(locationObj);
            }

            markerLocation.addMarker(event);
        }

        /**
         * Reset the map and use the new list of move events.
         * 
         * @param mapTimeLine
         *            A time ordered list of MoveEvents.
         */
        public void initMap(LinkedList mapTimeLine)
        {
            this.clear();

            for (Iterator i = mapTimeLine.iterator(); i.hasNext();)
            {
                MoveEvent event = (MoveEvent) i.next();
                int location = event.getTick();
                addMoveEventToLocation(location, event);
            }
        }

        /**
         * Get the currently visible MoveEvent for the specified location.
         * 
         * @param location
         *            An integer location.
         * @return The requested MoveEvent
         */
        public MoveEvent getMoveEvent(int location)
        {
            Integer locationObj = new Integer(location);
            MarkerLocation markerLocation = (MarkerLocation) get(locationObj);
            return markerLocation.getVisibleMarker().getEvent();
        }

        /**
         * Select the specified MoveEvent, causing it to be visible on the time
         * line.
         * 
         * @param event
         * @return the location of the move event on the timeline.
         */
        public int selectMoveEvent(MoveEvent event)
        {
            if (event == null)
                return 0;

            Integer location = new Integer(event.getTick());
            MarkerLocation markerLocation = (MarkerLocation) get(location);
            if (markerLocation != null)
            {
                markerLocation.displayMarker(event);
            }
            return location.intValue();
        }
    }

    // MoveEvents that have a tick value that rounds to the same integer value
    // are
    // stored in a marker location and assigned a marker. Keeps track of which
    // marker is currently visible.
    private class MarkerLocation extends JLabel implements IRoleListener,
            IDiscourseFieldNavigatorListener
    {
        private LinkedList markers;

        private Marker visibleMarker;

        public MarkerLocation()
        {
            visibleMarker = null;
            markers = new LinkedList();

            // need to listen to the role manager for changes to player color
            roleManager.addRoleListener(this);

            // need to listen to the navigator for changes to the selected
            // player
            Workspace.instance.getNavigator().addListener(this);
        }

        /**
         * Add a Marker for the specified event.
         * 
         * @param event
         */
        public void addMarker(MoveEvent event)
        {
            Marker marker = new Marker(event);
            markers.add(marker);
            if (visibleMarker == null)
                displayMarker(marker);
        }

        // make the specified marker the visible marker for this location
        private void displayMarker(Marker marker)
        {
            this.setIcon(marker.getIcon());
            visibleMarker = marker;
        }

        /**
         * Make the icon associated with the specified event visible.
         * 
         * @param event
         *            The move event to make visible.
         */
        public void displayMarker(MoveEvent event)
        {
            for (Iterator i = markers.iterator(); i.hasNext();)
            {
                Marker currentMarker = (Marker) i.next();
                if (currentMarker.getEvent() == event)
                {
                    displayMarker(currentMarker);
                    return;
                }
            }
        }

        /**
         * Get the currently visible marker for this location.
         * 
         * @return Returns the selectedMarker.
         */
        public Marker getVisibleMarker()
        {
            return visibleMarker;
        }

        private void updateMarkerColor()
        {
            for (Iterator i = this.markers.iterator(); i.hasNext();)
            {
                Marker marker = (Marker) i.next();
                marker.updateMarkerColor();
            }

            displayMarker(visibleMarker);
        }

        /*
         * (non-Javadoc)
         * 
         * @see edu.virginia.speclab.ivanhoe.client.model.event.IRoleListener#roleChanged()
         */
        public void roleChanged()
        {
            updateMarkerColor();
        }

        /*
         * (non-Javadoc)
         * 
         * @see edu.virginia.speclab.ivanhoe.client.navigator.IDiscourseFieldNavigatorListener#selectedPlayerChanged()
         */
        public void selectedRoleChanged(String playerName, MoveEvent currentMove)
        {
            updateMarkerColor();
        }

        public void roleAdded(String playerName)
        {
        }

        public void roleRemoved(String playerName)
        {
        }
    }

    // Associates a Move Event with an icon on the time control time line.
    private class Marker
    {
        private MoveEvent event;

        private Color markerColor;

        private int markerType;

        private static final int EVENT_MARKER = 0;

        private static final int SPACE_MARKER = 1;

        /**
         * Construct a marker for the specified event.
         * 
         * @param event
         */
        public Marker(MoveEvent event)
        {
            this.event = event;
            this.markerColor = chooseColor();
            this.markerType = chooseMarker();
        }

        public void updateMarkerColor()
        {
            this.markerColor = chooseColor();
        }

        private Color chooseColor()
        {
            Color color;

            if (event == null || event.getMove() == null)
            {
                color = IvanhoeUIConstants.GRAY;
            } else
            {
                String selectedPlayer = Workspace.instance.getNavigator()
                        .getSelectedPlayer();
                int roleId = event.getMove().getRoleID();
                Role role = roleManager.getRole(roleId);

                // if no role is selected or the role is not found
                if (role == null || selectedPlayer == null)
                {
                    color = IvanhoeUIConstants.GRAY;
                }
                // if this is the currently selected user in the navigator
                else if (selectedPlayer.equals(role.getName()) == true)
                {
                    color = role.getFillPaint();
                }
                // otherwise
                else
                {
                    color = Color.GRAY;
                }
            }

            return color;
        }

        private int chooseMarker()
        {
            int marker;

            if (event == null)
            {
                marker = SPACE_MARKER;
            } else
            {
                marker = EVENT_MARKER;
            }

            return marker;
        }

        /**
         * @return Returns the event.
         */
        public MoveEvent getEvent()
        {
            return event;
        }

        /**
         * @return Returns the icon.
         */
        public Icon getIcon()
        {
            if (markerType == EVENT_MARKER)
            {
                // create an offscreen buffer to work in
                BufferedImage markImage = imageLoader.createImage(20, 10);
                Graphics2D g = markImage.createGraphics();

                // draw the mark
                g.setColor(markerColor);

                g.drawLine(10, 0, 10, 6);
                g.dispose();

                // simply display the image using a Swing ImageIcon
                return new ImageIcon(markImage);
            } else if (markerType == SPACE_MARKER)
            {
                return spaceMarkerIcon;
            }

            return null;
        }
    }

    public DiscourseFieldTimeSlider()
    {
        
        listeners = new LinkedList();
        
        playing = false;
        timeUnit = BASE_TIME_UNIT;

        setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        timeTrack = new JSlider();
        IvanhoeSliderUI sliderUI = new IvanhoeSliderUI(timeTrack);
        timeTrack.setUI(sliderUI);
        timeTrack.setFocusable(false);
        timeTrack.setOrientation(JSlider.HORIZONTAL);
        timeTrack.setInverted(true);

        timeTrack.setBorder(LineBorder.createGrayLineBorder());
        add(timeTrack, BorderLayout.CENTER);
        
        timeTrack.setValue(0);
        timeTrack.setOpaque(false);
             
    }

    public void init(DiscourseFieldTimeline dfTimeline,
            DiscourseFieldTime time, RoleManager rManager)
    {
        // initialize the slider bar
        timeTrack.setMinimum(0);

        this.timeline = dfTimeline;
        this.discourseFieldTime = time;
        this.roleManager = rManager;

        markerMap = new MarkerMap(dfTimeline.getTimeline());

        timeTrack.setLabelTable(markerMap);
        timeTrack.setPaintTicks(false);
        timeTrack.setPaintLabels(true);
        timeTrack.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        MoveEvent lastMove = dfTimeline.getLastMoveEvent();

        if (lastMove != null)
        {
            timeTrack.setMaximum(lastMove.getTick());
        }

        timeTrack.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event)
            {
                moveSlider();
            }
        });

        // listen for changes in the discourse field
        dfTimeline.addListener(this);

        // listen for changes to the player roles (color change)
        rManager.addRoleListener(this);

        // need to listen to the navigator for changes to the selected player
        Workspace.instance.getNavigator().addListener(this);

        moveSlider();

        // start listening for changes in the current time
        discourseFieldTime.addListener(this);
    }
    
    public void addListener( DFTimeSliderListener listener )
    {
        listeners.add(listener);
    }
    
    public void removeListener( DFTimeSliderListener listener )
    {
        listeners.remove(listener);
    }
    
    private void firePlaying()
    {
        for( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            DFTimeSliderListener listener = (DFTimeSliderListener) i.next();
            listener.playing();            
        }
    }
    
    private void fireStopped()
    {
        for( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            DFTimeSliderListener listener = (DFTimeSliderListener) i.next();
            listener.stopped();            
        }
    }

    private void moveSlider()
    {
        int newLocation = timeTrack.getValue();

        if (newLocation != this.currentLocation)
        {
            updateTime(newLocation);
        }
    }


    public void setPlay(boolean play)
    {
        playing = play;
        lastTime = 0;
        
        if( play ) firePlaying();
        else fireStopped();
    }

    public boolean isPlaying()
    {
        return playing;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.virginia.speclab.ivanhoe.client.model.event.IDiscourseFieldListener#moveAddedToHistory()
     */
    public void moveAddedToHistory(MoveEvent event)
    {
        // update the size time track
        markerMap.initMap(timeline.getTimeline());
        MoveEvent lastMove = timeline.getLastMoveEvent();
        timeTrack.setMaximum(lastMove.getTick());
        timeTrack.setLabelTable(markerMap);

        // play the new move
        setPlay(!playing);
    }

    public void updateTime(int tick)
    {
        int max = timeTrack.getMaximum();
        if (max > 0)
        {
            // if we are at the end make sure we select the last tick to avoid
            // rounding error
            if (max == tick)
            {
                MoveEvent event = timeline.getLastMoveEvent();
                updateTime(event);
            } else
            {
                discourseFieldTime.setTick(tick);
            }
        }
    }

    public void updateTime(MoveEvent event)
    {
        discourseFieldTime.setMoveEvent(event);
    }
    
    public int getMaximumSliderValue()
    {
        return this.timeTrack.getMaximum();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.virginia.speclab.ivanhoe.client.model.event.IDiscourseFieldListener#addNewDocument(edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo)
     */
    public void addNewDocument(DocumentInfo docInfo)
    {
        updateTime(timeTrack.getMaximum());
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.virginia.speclab.ivanhoe.client.model.event.IDiscourseFieldListener#removeDocument(edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo)
     */
    public void removeDocument(DocumentInfo docInfo)
    {
        updateTime(timeTrack.getMaximum());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0)
    {
        if (playing == true)
        {
            long currentTime = System.currentTimeMillis();

            if (lastTime == 0)
                lastTime = currentTime;

            long elapsedTime = currentTime - lastTime;
            int currentValue = timeTrack.getValue();

            // animate the slider
            if (elapsedTime >= timeUnit)
            {
                int numberOfUnits = (int) (elapsedTime / timeUnit);

                lastTime = currentTime;

                if ((numberOfUnits + currentValue) < timeTrack.getMaximum())
                {
                    currentValue = numberOfUnits + currentValue;
                } else
                {
                    // stop playing go to the last move
                    setPlay(!playing);
                    MoveEvent lastEvent = timeline.getLastMoveEvent();
                    updateTime(lastEvent);
                    return;
                }
            }

            // animate the player circle
            updateTime(currentValue);
        }
    }

    public void runOpeningSequence()
    {
        // go to the last move
        int max = timeTrack.getMaximum();
        updateTime(max);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.virginia.speclab.ivanhoe.client.model.event.IRoleListener#roleChanged()
     */
    public void roleChanged()
    {
        timeTrack.repaint();
    }

    public void selectedRoleChanged(String playerName, MoveEvent currentMove)
    {
        timeTrack.repaint();
    }

    public void discourseFieldTickChanged(int tick)
    {
        if (tick != this.currentLocation)
        {
            this.currentLocation = tick;
            timeTrack.setValue(this.currentLocation);
        }
    }

    public void discourseFieldMoveEventChanged(MoveEvent moveEvent)
    {
        if (moveEvent != null)
        {
            if (moveEvent.getTick() != this.currentLocation)
            {
                this.currentLocation = markerMap.selectMoveEvent(moveEvent);
                timeTrack.setValue(this.currentLocation);
            }
        }
    }

    public void roleAdded(String playerName)
    {
    }

    public void roleRemoved(String playerName)
    {
    }
}
