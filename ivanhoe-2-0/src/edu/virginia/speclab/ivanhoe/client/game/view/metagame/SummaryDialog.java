/*
 * Created on Aug 6, 2004
 *
 * SummaryDialog
 */
package edu.virginia.speclab.ivanhoe.client.game.view.metagame;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;

/**
 * @author lfoster
 *
 * Show an in-progress move summary
 */
public class SummaryDialog extends JDialog
{
   private JEditorPane moveSummary;

   public SummaryDialog( JFrame frame, String summaryText )
   {
      super(frame);
      
      setModal(true);  
      setTitle("Unpublished Move Summary");

      getContentPane().setLayout( new BorderLayout(5,5) );

      // create move summary display
      this.moveSummary = new JEditorPane();
      this.moveSummary.setEditable(false);
      this.moveSummary.setContentType("text/html");
      this.moveSummary.setMargin(new Insets(10,10,10,10));
      this.moveSummary.setText(summaryText);
      JScrollPane sp2 = new JScrollPane( this.moveSummary );
      sp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      sp2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      JPanel summmaryPnl = new JPanel(new BorderLayout());
      summmaryPnl.add(sp2, BorderLayout.CENTER);
      JLabel l2 = new JLabel("Summary");
      l2.setFont(IvanhoeUIConstants.BOLD_FONT);
      summmaryPnl.add(l2, BorderLayout.NORTH);
      
      JButton okBtn = new JButton("OK");
      okBtn.addActionListener( new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               dispose();
            }
         });
      okBtn.setFont(IvanhoeUIConstants.SMALL_FONT);
      
      // add all panels to dialog
      getContentPane().add(Box.createVerticalStrut(5), BorderLayout.NORTH);
      getContentPane().add(sp2, BorderLayout.CENTER);
      getContentPane().add(okBtn, BorderLayout.SOUTH);
      getContentPane().add(Box.createHorizontalStrut(5), BorderLayout.WEST);
      getContentPane().add(Box.createHorizontalStrut(5), BorderLayout.EAST);
      
      pack();
      setSize(400,400);
      setResizable(false);
      Workspace.instance.centerWindowOnWorkspace(this);
   }
}
