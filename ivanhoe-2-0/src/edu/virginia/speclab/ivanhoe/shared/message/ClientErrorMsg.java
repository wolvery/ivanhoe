/*
 * Created on Feb 3, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.shared.message;


/**
 * @author Nick
 *
 * This message allows the client to report a message which will appear in the IvanhoeServer log on 
 * the host.
 */
public class ClientErrorMsg extends Message
{
    private String errorMessage; 
    public ClientErrorMsg(String errorMessage)
    {
       super(MessageType.CLIENT_ERROR);
       this.errorMessage = errorMessage;
    }

    /**
     * @return Returns the errorMessage.
     */
    public String getErrorMessage()
    {
        return errorMessage;
    }
}
