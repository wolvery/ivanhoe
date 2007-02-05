/*
 * Created on Nov 11, 2004
 */
package edu.virginia.speclab.ivanhoe.shared;

import java.security.MessageDigest;

/**
 * @author benc
 */
public class Encryption
{
	public static void main(String[] args)
	{
		if( args.length == 1 ) 
		{
			String clearPassword = args[0];
			System.out.print("\n Password string = "+createMD5HashCode(clearPassword));
		}
		else
		{
			System.out.print("\n Usage: enter a password with no whitespace in it to see MD5 encryption");
		}
	}	
	
    /**
     * Get MD5 hashcode for a string.
     *
     * @param text Text to create the MD5 hash code for.
     * @return MD5 hash code for <I>text</I> or the input text if failed.
     */
    public static String createMD5HashCode(String text) 
    {
        String result;
                
        if (text != null)
        {
            String plain = text;
            byte bytes[] = plain.getBytes();
            result = createMD5HashCode(bytes);
        }
        else
        {
            result = "";
        }
        
        return result;
    }
    
    public static String createMD5HashCode(byte data[])
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5", "SUN");
            StringBuffer code = new StringBuffer(); //the hash code
            byte digest[] = messageDigest.digest(data); //create code
            for (int i = 0; i < digest.length; ++i) {
                code.append(Integer.toHexString(0x0100 + (digest[i] & 0x00FF)).substring(1));
            }

            return code.toString();
        }
        catch (Exception e)
        {
            SimpleLogger.logError("Error during hash creation.", e);
            return "";
        }
    }

}
