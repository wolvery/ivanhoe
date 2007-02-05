package edu.virginia.speclab.ivanhoe.client.game.view.document;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;

public class HeaderPanel extends JPanel
{
	private boolean readOnly;
	
	private static Icon commentaryIcon = ResourceHelper.instance.getIcon("res/icons/commentary.jpg");
	private static Icon editingIcon = ResourceHelper.instance.getIcon("res/icons/editing.jpg");
	
    public HeaderPanel(DocumentInfo info, boolean readOnly)
    {
        this.readOnly = readOnly;
        
        setLayout(new BorderLayout());

        add(createInfoPanel(info), BorderLayout.NORTH);      
    }
    
    private void addFieldLabel( JPanel panel, String line )
    {
        JLabel label = new JLabel(line, SwingConstants.LEFT);
        label.setFont(IvanhoeUIConstants.TINY_BOLD_FONT);
        label.setForeground(IvanhoeUIConstants.WHITE);
        panel.add(label);
    }
    
    private void addFieldData( JPanel panel, String line )
    {
        JLabel label = new JLabel(line, SwingConstants.LEFT);
        label.setFont(IvanhoeUIConstants.TINY_FONT);
        label.setForeground(IvanhoeUIConstants.LIGHT_GRAY);
        panel.add(label);
    }

    private JPanel createInfoPanel(DocumentInfo info)
    {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        
        JPanel labels = new JPanel(new GridLayout(3, 1));
        labels.setOpaque(false);

        addFieldLabel( labels, "Contributor: " );
        addFieldLabel( labels, "Author: ");
        addFieldLabel( labels, "Source: ");            
        pnl.add(labels, BorderLayout.WEST);

        JPanel disp = new JPanel(new GridLayout(3, 1));
        disp.setOpaque(false);

        addFieldData(disp, info.getContributor() );
        addFieldData(disp, info.getAuthor() );
        addFieldData(disp, info.getSource() );            
        pnl.add(disp, BorderLayout.CENTER);
        pnl.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        pnl.setBorder(new EmptyBorder( 0, 7, 0, 0));

        JPanel infoPnl = new JPanel(new BorderLayout());
        infoPnl.setBackground(IvanhoeUIConstants.DARKEST_GRAY);
        
        infoPnl.add(pnl, BorderLayout.CENTER);

        JLabel docIcon;
        if( readOnly )
        {
            docIcon = new JLabel(commentaryIcon);    
        }
        else
        {
            docIcon = new JLabel(editingIcon);
        }
        
        docIcon.setBackground(IvanhoeUIConstants.BLACK);
        infoPnl.add(docIcon, BorderLayout.WEST);
        infoPnl.setBorder(new EmptyBorder( 3, 3, 3, 3));
        
        return infoPnl;
    }

    
   
}
