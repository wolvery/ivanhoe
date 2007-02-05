/*
 * Created on Jun 28, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Stroke;
import java.util.ArrayList;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeStyleInternalFrame;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;

/**
 * @author Nick
 *
 */
public class CustomColorsWindow extends IvanhoeStyleInternalFrame implements IColorPairViewListener
{
    private boolean valid;
    private ArrayList colorPairViews;
    private JPanel currentViewPanel;
    private RoleManager roleManager;
    private final String roleName;
    
    public static final int ARC_SIZE = 100;
    public static final int BOX_SIZE = 20;
    public static final Stroke arcStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    
    public CustomColorsWindow( String roleName, ColorPair playerCircleColors, CustomColors customColors, RoleManager roleManager )
    {
    	super("color preferences");
    	this.roleManager = roleManager;
    	this.roleName = roleName;
    	        
        String options[] = { "player circle", "deletion arc", "insertion arc", 
                			 "commentary arc", "link arc" };
               
		ColorPair playerColors = new ColorPair(
		        playerCircleColors.strokeColor, playerCircleColors.fillColor);	    
        
        colorPairViews = new ArrayList(5);
        
        colorPairViews.add( CustomColors.PLAYER_CIRCLE,
                new ColorPairView( "Player Circle", playerColors, this, CustomColors.PLAYER_CIRCLE ));
        colorPairViews.add( CustomColors.DELETION_ARC, 
                new ColorPairView( "Deletion Arc", customColors.getDeletionArcColors(), this, CustomColors.DELETION_ARC));
        colorPairViews.add( CustomColors.INSERTION_ARC, 
                new ColorPairView( "Insertion Arc", customColors.getInsertionArcColors(), this, CustomColors.INSERTION_ARC));
        colorPairViews.add( CustomColors.ANNOTATION_ARC,
                new ColorPairView( "Commentary Arc", customColors.getAnnotationArcColors(), this, CustomColors.ANNOTATION_ARC));
        colorPairViews.add( CustomColors.LINK_ARC,
                new ColorPairView( "Link Arc", customColors.getLinkArcColors(), this, CustomColors.LINK_ARC));
        
        JList optionsList = new JList(options);
        optionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(optionsList);
        TitledBorder border = new TitledBorder("Color Options");
        scrollPane.setBorder(border);
        optionsList.setSelectedIndex(0);
        
        optionsList.addListSelectionListener( new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                JList list = (JList) e.getSource();
                setCurrentViewPanel( list.getSelectedIndex() );
            }
        });
        
        currentViewPanel = new JPanel();
        currentViewPanel.add((ColorPairView) colorPairViews.get(0));
        
        getContentPane().setLayout( new BorderLayout() );
        getContentPane().add(scrollPane, BorderLayout.LINE_START);
        getContentPane().add(currentViewPanel, BorderLayout.LINE_END);   
        
        setSize(getPreferredSize());
        setResizable(false);
    }
    
    private void setCurrentViewPanel( int panelIndex )
    {
        ColorPairView panel = (ColorPairView) colorPairViews.get(panelIndex);
        currentViewPanel.removeAll();
        currentViewPanel.add(panel);
        currentViewPanel.validate();
        repaint();
    }
    
    public void handleOk(ColorPairView dispatcher)
    {
       handleApply(dispatcher);
       
       valid = true;
       try
	   {
          setClosed(true); 
	   }
       catch (Exception e)
	   {
          SimpleLogger.logError("Error: "+e.getMessage());
       }
    }
    
    public void handleCancel(ColorPairView dispatcher)
    {
        valid = false;
        try
		{
        	setClosed(true);
		}
        catch (Exception e)
		{
        	SimpleLogger.logError("Error: "+e.getMessage());
		}
    }
    
    public void handleApply(ColorPairView dispatcher)
    {
        roleManager.fireColorUpdate(
                roleName,
                dispatcher.getColorPair(), dispatcher.getArcType());
    }
    
    /**
     * @return Returns the valid.
     */
    public boolean isValid()
    {
        return valid;
    }
}
