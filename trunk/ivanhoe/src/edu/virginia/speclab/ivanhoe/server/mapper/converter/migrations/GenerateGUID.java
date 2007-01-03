/*
 * Created on Jan 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.server.mapper.converter.migrations;

import edu.virginia.speclab.ivanhoe.shared.Encryption;

/**
 * @author Nick
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GenerateGUID
{

    public static void main(String[] args)
    {
        String password = Encryption.createMD5HashCode("1234");
//        String id = GuidGenerator.generateID();
        System.out.println(password);
    }
}
