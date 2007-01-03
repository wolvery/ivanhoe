/*
 * Created on Jan 14, 2004
 *
 * DocumentInfoDialogDocumentInfoDialog
 */
package edu.virginia.speclab.ivanhoe.client.game.view.document;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.*;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;

/**
 * @author lfoster
 * 
 * Dialog to handle input of DocumentInfo data
 */
public class DocumentInfoDialog extends JDialog implements ActionListener
{
    private JTextField titleEdit;

    private JTextField authorEdit;

    private JTextField sourceEdit;

    private String fileName;

    private Date createDate;

    private JButton done;

    private boolean doneFlag;

    /**
     * Construct a doc info editor for an imported file
     * 
     * @param fileName
     * @return
     */
    public static DocumentInfoDialog createDialog( JFrame frame, String fileName)
    {
        return new DocumentInfoDialog(frame,fileName);
    }

    private DocumentInfoDialog( JFrame frame, String fileName)
    {
        super(frame);
        setModal(true);
        setResizable(false);

        this.fileName = fileName;
        this.createDate = Ivanhoe.getDate();

        setTitle("Document Information");
        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(createUIPanel(), BorderLayout.CENTER);
        getContentPane().add(Box.createVerticalStrut(5), BorderLayout.NORTH);
        getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.EAST);

        this.done = new JButton("Done");
        getContentPane().add(this.done, BorderLayout.SOUTH);
        this.done.addActionListener(this);

        setSize(350, 180);
        Workspace.centerWindow(this);
    }

    private JPanel createUIPanel()
    {
        JPanel ui = new JPanel(new BorderLayout(5, 5));

        // create & layout labels
        JPanel labels = new JPanel(new GridLayout(3, 1));
        labels.add(new JLabel("Title: ", SwingConstants.RIGHT));
        labels.add(new JLabel("Author: ", SwingConstants.RIGHT));
        labels.add(new JLabel("Source: ", SwingConstants.RIGHT));
        ui.add(labels, BorderLayout.WEST);

        // is this creation of a new file?
        String defaultName = DocumentInfo
                .createTitleFromFileName(this.fileName);

        // create the controls
        this.titleEdit = new JTextField(defaultName);
        this.authorEdit = new JTextField("Unknown");
        this.sourceEdit = new JTextField("Unknown");

        // layout controls
        JPanel data = new JPanel(new GridLayout(3, 1));
        data.add(this.titleEdit);
        data.add(this.authorEdit);
        data.add(this.sourceEdit);

        ui.add(new JLabel("Enter document data below:", SwingConstants.CENTER),
                BorderLayout.NORTH);
        ui.add(data, BorderLayout.CENTER);

        return ui;
    }

    public String getFileName()
    {
        String file;
        if (fileName == null) {
            file = DocumentInfo.createFileNameFromTitle(this.titleEdit
                    .getText());
            SimpleLogger.logInfo("Filename for new document:  " + file);
        } else {
            file = this.fileName;
            int pos = file.lastIndexOf('.');
            if (pos > -1) {
                file = file.substring(0, pos + 1) + "html";
            }
        }

        return file;
    }

    /**
     * @return Returns the title of this document
     */
    public String getDocumentTitle()
    {
        String docTitle = this.titleEdit.getText();
        if (docTitle.length() > 50) {
            SimpleLogger
                    .logError("Truncation insanely long title to 50 chars. Bad user!");
            docTitle = docTitle.substring(0, 50);
        }
        return docTitle;
    }

    /**
     * @return returns the creation date
     */
    public Date getCreateDate()
    {
        return this.createDate;
    }

    /**
     * @return Returns the author of the document
     */
    public String getAuthor()
    {
        return this.authorEdit.getText();
    }

    /**
     * @return Returns the provenance
     */
    public String getSource()
    {
        return this.sourceEdit.getText();
    }

    public void actionPerformed(ActionEvent e)
    {
        String title = this.titleEdit.getText();
        if (title.length() == 0) {
            Ivanhoe.showErrorMessage("You must enter a document title");
        } else {
            doneFlag = true;
            dispose();
        }
    }

    /**
     * @return Returns the doneFlag.
     */
    public boolean isDone()
    {
        return doneFlag;
    }
}