/*
 * Created on Mar 31, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author benc
 */
public class NewAccountQueryMsg extends Message
{
    public NewAccountQueryMsg()
    {
        super(MessageType.NEW_ACCOUNT_QUERY);
    }

}
