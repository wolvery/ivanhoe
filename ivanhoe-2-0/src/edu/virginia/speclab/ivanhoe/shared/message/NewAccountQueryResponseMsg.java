/*
 * Created on Mar 31, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author benc
 */
public class NewAccountQueryResponseMsg extends Message
{
    private final boolean newAccountsAllowed;
    
    public NewAccountQueryResponseMsg(boolean newAccountsAllowed)
    {
        super(MessageType.NEW_ACCOUNT_QUERY_RESPONSE);
        this.newAccountsAllowed = newAccountsAllowed;
    }

    public boolean getNewAccountsAllowed()
    {
        return newAccountsAllowed;
    }
}
