/*
 * Created on Jan 14, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * @author Nick
 *
 * This derivation of JTextField can be attached to a listener
 * to report the length of its text as it changes.
 */
public class LengthReportingTextField extends JTextField 
{
    private ILengthReportingListener listener;
    
    public LengthReportingTextField(int cols) {
        super(cols);
    }
    
    public void setLengthReportingListener( ILengthReportingListener listener )   
    {
        this.listener = listener; 
    }

    protected Document createDefaultModel() {
	      return new LengthReportingDocument();
    }

    private class LengthReportingDocument extends PlainDocument 
    {
        public void remove( int offs, int len )
        	throws BadLocationException
        {
            super.remove(offs,len);
            fireLengthChanged( getLength() );
        }
        
        public void insertString(int offs, String str, AttributeSet a) 
	          throws BadLocationException 
	    {
            super.insertString(offs, str, a);            
            fireLengthChanged( getLength() );
        }
    }
    
    private void fireLengthChanged( int length )
    {
        if( listener != null )
        {
            listener.lengthChanged(length);
        }
    }
}