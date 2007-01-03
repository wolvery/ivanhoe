/*
 * Created on Mar 15, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.gameaction;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.cache.DocumentFilter;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.IPermissionListener;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.IvanhoeFrame;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.document.DocumentInfoDialog;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.data.Role;


/**
 * @author Nathan Piazza
 */
public class AddDocAction extends AbstractAction implements IPermissionListener
{
    private DiscourseField discourseField;
    private RoleManager roleManager;
    private IvanhoeFrame ivanhoeFrame;
    private boolean enabled;
    
	public AddDocAction( DiscourseField discourseField, RoleManager roleManager, IvanhoeFrame frame)
	{
        super("add document");
        
     	this.discourseField = discourseField;
		this.roleManager = roleManager;
        this.ivanhoeFrame = frame;
		writePermissionsChanged(false);
	}
	
	public void actionPerformed(ActionEvent e)
    {

        if (enabled)
        {
            Object[] options = { "Import Existing", "Create New", "Cancel" };
            int n = JOptionPane.showOptionDialog(ivanhoeFrame,
                    "Create a new document or import an existing document?",
                    "Add Document", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            if (n == 0)
            {
                importDocument();
            }
            else if (n == 1)
            {
                createNewDocument();
            }
        }
        else
        {
            Ivanhoe.showErrorMessage(
                            "Cannot Add Document",
                            "A new document cannot be added to the discourse"
                            + " field at this time because you do not have"
                            + " permission to alter this game.");
        }

    }
   
   private void createNewDocument()
   {
      Workspace.instance.createNewDocument();
   }

	/**
     * 
     */
   private void importDocument()
   {
      JFileChooser dlg = new JFileChooser(System.getProperty("user.dir"));
	
      dlg.addChoosableFileFilter(
         new DocumentFilter(".gif", "GIF Images (*.gif)"));
      dlg.addChoosableFileFilter(
         new DocumentFilter(".jpg", "JPEG Images (*.jpg)"));
      dlg.addChoosableFileFilter(
         new DocumentFilter(".txt", "Text Documents (*.txt)"));
//      dlg.addChoosableFileFilter(
//            new DocumentFilter(".html", "HTML Documents (*.html)"));
         
      /**
       * The following instruction fails on Mac OS X 10.2.x,
       * which has a Java bug.  Later version of Mac OS work fine.
       */
      if (System.getProperty("mrj.version") == null)
      {
         dlg.setAcceptAllFileFilterUsed(false);
      }
      
      int result = dlg.showDialog(null, "Add Document");
      if (result == JFileChooser.APPROVE_OPTION)
      {
		   DocumentInfoDialog infoDlg = DocumentInfoDialog.createDialog( ivanhoeFrame, dlg.getSelectedFile().getName());
		   Workspace.instance.centerWindowOnWorkspace(infoDlg);
		   infoDlg.show();		   
		   
		   if( infoDlg.isDone() == true )
		   {
		       Role currentRole = roleManager.getCurrentRole();
		       
		       int contributorID = currentRole.getId();
		       
			   DocumentInfo info = new DocumentInfo(infoDlg.getFileName(),
	            infoDlg.getDocumentTitle(), infoDlg.getAuthor(), infoDlg.getSource(),
	            currentRole.getName(), contributorID,
	            infoDlg.getCreateDate());
	         
			   discourseField.addNewDocument(info,dlg.getSelectedFile());
		   }
		}
   }

    public void writePermissionsChanged(boolean writable)
    {
        this.enabled = writable;
        if (enabled)
        {
            putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/newpage.jpg"));
        }
        else
        {
            putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/newpage-disabled.jpg"));
        }
    }
}
