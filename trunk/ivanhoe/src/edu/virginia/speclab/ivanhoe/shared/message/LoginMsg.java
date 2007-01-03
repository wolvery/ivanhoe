/*
 * Created on Oct 2, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import java.util.Properties;


/**
 * @author lfoster
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LoginMsg extends Message
{
    private static final long serialVersionUID = 3257003246286287153L;

    private final String password;
    private final Properties clientProperties;

    public LoginMsg(String password, Properties clientProperties)
    {
        super(MessageType.LOGIN);
        this.password = password;
        this.clientProperties = clientProperties;
        
        if (password == null || clientProperties == null)
        {
            throw new IllegalArgumentException("Attempted to construct LoginMsg with null argument");
        }
    }

    public String getPassword()
    {
        return password;
    }

    public Properties getClientProperties()
    {
        return clientProperties;
    }

    public String toString()
    {
        return super.toString() + " password [" + password + "]";
    }
}
