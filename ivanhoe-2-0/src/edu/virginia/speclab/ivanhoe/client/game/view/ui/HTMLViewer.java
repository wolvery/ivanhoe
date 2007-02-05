/*
 * Created on Nov 21, 2003
 *
 * HTMLViewer
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;

/**
 * HTMLViewer
 * @author lfoster
 *
 * Read only viewer using standard HTML toolkit. This is used
 * to view externally linked documents. It also contains a
 * button to spawn the url in the system browserr
 */
public class HTMLViewer extends IvanhoeStyleInternalFrame implements ActionListener
{
   private JEditorPane htmlPane;
   private JButton     externalView;
   private String      url;
   
   public HTMLViewer(String url)
   {
      super("External Document");
         
      setSize(Workspace.instance.getWidth()/2+50,
         Workspace.instance.getHeight()-Workspace.instance.getHeight()/3);
      
      setVisible(true);
      
      this.setFrameIcon(
         ResourceHelper.instance.getIcon("res/icons/url.gif"));
      
      this.url = url;
         
      this.htmlPane = new JEditorPane();
      this.htmlPane.setEditable(false);
      
      getContentPane().setLayout( new BorderLayout() );
      JScrollPane sp = new JScrollPane( this.htmlPane );
      sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      getContentPane().add(sp, BorderLayout.CENTER);
      
      this.externalView = new JButton("Open in system browser");
      this.externalView.addActionListener( this );
      getContentPane().add(externalView, BorderLayout.SOUTH);
      
      try
      {
         this.htmlPane.setPage(url);
      }
      catch (Exception e)
      {
         Ivanhoe.showErrorMessage("Unable to open external document " + url);
         dispose();
      }
   }
   
   public void actionPerformed(ActionEvent e)
   {
      try
      {
         String cmd[] = { Ivanhoe.getBrowser(), url};
         Runtime.getRuntime().exec(cmd);
      }
      catch (IOException e1)
      {
         SimpleLogger.logError("Unable to launch external browser: " + e1);
      }
   }
}
