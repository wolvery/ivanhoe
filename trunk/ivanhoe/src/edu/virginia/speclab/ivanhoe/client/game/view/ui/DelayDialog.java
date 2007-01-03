/*
 * Created on Jan 3, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;

/**
 * @author Nick
 *
 */
public class DelayDialog extends JDialog
{
    public DelayDialog(String message)
    {
       setUndecorated(true);            
       getContentPane().setLayout( new BorderLayout() );
       JPanel center = new JPanel(new BorderLayout(20,1) );
       center.setBorder( new EtchedBorder(EtchedBorder.RAISED, Color.BLUE, Color.BLACK) );
       center.add(new JLabel(message, SwingConstants.CENTER),BorderLayout.CENTER);
       center.add( new JPanel(), BorderLayout.EAST);
       center.add(new JLabel(
       ResourceHelper.instance.getIcon("res/images/ivan.gif")), BorderLayout.WEST);
       getContentPane().add(center, BorderLayout.CENTER );
       pack();
       centerWindow();
    }
    
    private void centerWindow()
    {
       Toolkit tk = Toolkit.getDefaultToolkit();
       Dimension dim = tk.getScreenSize();
       int screenHeight = dim.height;
       int screenWidth = dim.width;

       setLocation( (screenWidth - getWidth()) / 2,
               		(screenHeight - getHeight()) / 2 - 50);
    }
 }	
