/*
 * Created on Jan 18, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame.publish;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseFieldTimeline;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.MoveListItem;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardDialog;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.WizardStepPanel;
import edu.virginia.speclab.ivanhoe.shared.data.Move;

/**
 * @author Nick
 *
 */
public class MoveInspirationPanel extends WizardStepPanel
{    
    private DiscourseFieldTimeline timeline;
    private List moveList;
    private JEditorPane moveDescription;

    private class MoveListItemCheck extends JCheckBox
    {
        private MoveListItem item;
        
        public MoveListItemCheck( MoveListItem item )
        {
            this.item = item;
            setBackground(IvanhoeUIConstants.WHITE);
            setFont(IvanhoeUIConstants.SMALL_FONT);
            setText(item.toString());
            
            addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    handleMoveItemSelection();
                }                
            } );
        }
        
        private void handleMoveItemSelection()
        {
            item.setSelected(isSelected());   
            updateDescription(item.getDescription());
        }
        
        /**
         * @return Returns the item.
         */
        public MoveListItem getItem()
        {
            return item;
        }
    }
    
    
    public MoveInspirationPanel( WizardDialog wizard, DiscourseFieldTimeline timeline )
    {
        super(wizard);
        this.timeline = timeline;
        initMoveList();
        createUI();
    }

    private void createUI()
    {
        setLayout(new BorderLayout());        
        JPanel historyPanel = createHistoryPanel();
        add(historyPanel, BorderLayout.CENTER);

        JLabel label = new JLabel("move relations");
        label.setFont(IvanhoeUIConstants.BOLD_FONT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(label, BorderLayout.NORTH);
        
        JLabel msg = new JLabel("Select the moves you wish to relate to this move." );
        msg.setFont(IvanhoeUIConstants.SMALL_FONT);
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(msg, BorderLayout.SOUTH);
    }
    
    private JPanel createHistoryPanel()
    {        
        JPanel moveListPanel = createMoveListPanel();
        
        JScrollPane scrollPane = new JScrollPane(moveListPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        moveDescription = new JEditorPane();
        moveDescription.setEditable(false);
        moveDescription.setContentType("text/html");
        moveDescription.setMargin(new Insets(10, 10, 10, 10));
        moveDescription.setPreferredSize(new Dimension(200,200));
        moveDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
        moveDescription.setBorder( new TitledBorder("move rationale"));
        moveDescription.setBackground(this.getBackground());

        JPanel categoryListPanel = new JPanel();        
        categoryListPanel.setLayout( new BorderLayout() );
        categoryListPanel.add( scrollPane, BorderLayout.CENTER );
        categoryListPanel.add( moveDescription, BorderLayout.SOUTH );
        
        return categoryListPanel;
    }
    
    private void updateDescription( String description )
    {
        moveDescription.setText(description);
        
    }
    
    private JPanel createMoveListPanel()
    {
        JPanel moveListPanel = new JPanel();
        moveListPanel.setBackground(IvanhoeUIConstants.WHITE);
        moveListPanel.setLayout( new BoxLayout( moveListPanel, BoxLayout.Y_AXIS));
        
        for( Iterator i = moveList.iterator(); i.hasNext(); )
        {
            MoveListItem moveListItem = (MoveListItem) i.next();
            MoveListItemCheck check = new MoveListItemCheck( moveListItem );
            moveListPanel.add(check);           
        }
        
        return moveListPanel;
    }
    
    private void initMoveList()
    {
        List moveEventList = null;
        moveList = new LinkedList();
        
        if( timeline != null )
        {
            moveEventList = timeline.getTimeline();
        }

        if( moveEventList != null )
        {       
            int moveNum = 1;
            for( Iterator i = moveEventList.iterator(); i.hasNext(); )
            {
                MoveEvent moveEvent = (MoveEvent) i.next();
                Move move = moveEvent.getMove();

                if( move != null )
                {
                    MoveListItem listItem = new MoveListItem(move,moveNum++);
                    moveList.add(listItem);
                }
            }
        }       
    }
    
    public List getInspirations()
    {
        LinkedList selectedMoveList = new LinkedList();
        for( Iterator i = moveList.iterator(); i.hasNext(); )
        {
            MoveListItem moveListItem = (MoveListItem) i.next();
            
            // if the move is selected, pull out its id and add it to the list.
            if( moveListItem.isSelected() )
            {
	            Integer id = new Integer( moveListItem.getMove().getId() );
	            selectedMoveList.add(id);
            }
        }
        
        return selectedMoveList;
    }   
    
    public List getSelectedMoveItems()
    {
        LinkedList selectedMoveList = new LinkedList();
        for( Iterator i = moveList.iterator(); i.hasNext(); )
        {
            MoveListItem moveListItem = (MoveListItem) i.next();
            
            // if the move is selected, add it to the list.
            if( moveListItem.isSelected() )
            {
               selectedMoveList.add(moveListItem);
            }
        }
        
        return selectedMoveList;
    }
}
