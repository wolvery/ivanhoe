package edu.virginia.speclab.ivanhoe.client.game.view.navigator.time;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTime;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTimeline;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.BlackBox;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeButton;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;

public class DiscourseFieldTimeControls extends BlackBox implements DFTimeSliderListener
{
    private DiscourseFieldTimeline timeline;
    private DiscourseFieldTime discourseFieldTime;
    
    private JButton playButton, prevButton, nextButton;

    private DiscourseFieldTimeSlider dfTimeControl;

    private static final Icon playButtonIcon = ResourceHelper.instance.getIcon("res/icons/play.jpg");
    private static final Icon pauseButtonIcon = ResourceHelper.instance.getIcon("res/icons/pause.jpg");
    private static final Icon prevButtonIcon = ResourceHelper.instance.getIcon("res/icons/back.jpg");
    private static final Icon nextButtonIcon = ResourceHelper.instance.getIcon("res/icons/forward.jpg");
    
    public DiscourseFieldTimeControls( DiscourseFieldTimeSlider dfTimeControl )
    {
        super(BoxLayout.X_AXIS);
        
        this.dfTimeControl = dfTimeControl;
        dfTimeControl.addListener(this);
        
        playButton = new IvanhoeButton("play/pause", playButtonIcon);
        prevButton = new IvanhoeButton("previous move", prevButtonIcon);
        nextButton = new IvanhoeButton("next move", nextButtonIcon);
        
        add(playButton);
        add(prevButton);
        add(nextButton);
        
        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {            	
                pressPlay();
            }
        });

        prevButton.addMouseListener( new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				if( mouseEvent.getClickCount() > 1 ) 
				{
					pressRewind();
				} 
				else 
				{
					pressPrev();
				}			
			}
        });
        
        nextButton.addMouseListener( new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				pressNext();
			}
        });
    }
    
    public void init(DiscourseFieldTimeline dfTimeline, DiscourseFieldTime time)
    {
        this.timeline = dfTimeline;
        this.discourseFieldTime = time;
    }
    
    private void pressPlay()
    {
        boolean playState = !dfTimeControl.isPlaying();
        
        dfTimeControl.setPlay(playState);        
    }
    
    private void pressPrev()
    {
        if( timeline == null || discourseFieldTime == null ) return;
        
        dfTimeControl.setPlay(false);

        MoveEvent currentEvent = discourseFieldTime.getMoveEvent();

        MoveEvent prevEvent;
        if (currentEvent != null)
        {
            prevEvent = timeline.getPreviousMoveEvent(currentEvent);
        } else
        {
            // look up the move event, which always returns the previous event
            prevEvent = discourseFieldTime.updateMoveEvent();
        }

        // if we found a previous move, select it. Otherwise the start of the
        // timeline
        if (prevEvent != null)
        {
            dfTimeControl.updateTime(prevEvent);
        } else
        {
            dfTimeControl.updateTime(0);
        }
    }
    
    private void pressRewind()
    {
        dfTimeControl.updateTime(0);
    }

    private void pressNext()
    {
        if( timeline == null || discourseFieldTime == null ) return;
        
        dfTimeControl.setPlay(false);

        MoveEvent currentEvent = discourseFieldTime.getMoveEvent();

        if (currentEvent == null)
        {
            currentEvent = discourseFieldTime.updateMoveEvent();
        }

        MoveEvent nextEvent = timeline.getNextMoveEvent(currentEvent);

        // if we found a previous move, select it. Otherwise the start of the
        // timeline
        if (nextEvent != null)
        {
            dfTimeControl.updateTime(nextEvent);
        } else
        {
            dfTimeControl.updateTime(dfTimeControl.getMaximumSliderValue());
        }
    }
    
    public void playing()
    {
        playButton.setIcon(pauseButtonIcon);
    }

    public void stopped()
    {
        playButton.setIcon(playButtonIcon);        
    }

}
