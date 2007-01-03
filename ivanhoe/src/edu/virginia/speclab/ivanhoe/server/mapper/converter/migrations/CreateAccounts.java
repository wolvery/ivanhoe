/*
 * Created on Jan 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.server.mapper.converter.migrations;

import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.LinkedList;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.mapper.converter.IvanhoeDataConverter;
import edu.virginia.speclab.ivanhoe.shared.Encryption;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

/**
 * @author Nick
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CreateAccounts extends IvanhoeDataConverter
{
    private void performConversion()
    {
        try 
        {
            setUp();            
            insertNames();    
            tearDown();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    private class UserAccount
    {
        public int id;
        public String userName, clearPassword;
        public String firstName, lastName;
        public String email;
        public String title;
        
        public UserAccount( int id, String userName, String firstName, String lastName, String pass, String email, String title )
        {
            this.id = id;
            this.userName = userName;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.clearPassword = pass;
            this.title = title;            
        }
    }
    
    private void insertNames()
    {
        
        //TODO programmatically update keyspace table, did it by hand this time...
        
        LinkedList accountList = new LinkedList();        
/*
 * tucker's class
 * 
 *         accountList.add( new UserAccount( 101, "Susan Anspach", "Susan","Anspach","8041","sca3d@Virginia.EDU", "Student" ));
        accountList.add( new UserAccount( 102, "Christopher Barbatti","Christopher","Barbatti", "8753", "cvb3r@Virginia.EDU", "Student" ));
        accountList.add( new UserAccount( 103, "Jason Bowman", "Jason", "Bowman", "3237", "jab3ht@Virginia.EDU", "Student" ));
        accountList.add( new UserAccount( 104, "Zachary Brown", "Zachary", "Brown", "0004", "zpb6d@Virginia.EDU", "Student" ));
        accountList.add( new UserAccount( 105, "William Daughtrey", "William", "Daughtrey", "2846", "whd6f@Virginia.EDU", "Student" ));
        accountList.add( new UserAccount( 106, "Eleanor Donlon", "Eleanor", "Donlon", "6278", "ebd4j@Virginia.EDU", "Student" ));
        accountList.add( new UserAccount( 107, "Jaime Knauf", "Jaime", "Knauf", "6626", "jnk4a@Virginia.EDU", "Student" ));
        accountList.add( new UserAccount( 108, "Anne Lee", "Anne", "Lee", "3745", "ael8z@Virginia.EDU", "Student" ));
        accountList.add( new UserAccount( 109, "Jessica Stallings", "Jessica", "Stallings", "5429", "jas5rz@Virginia.EDU", "Student" ));
        accountList.add( new UserAccount( 110, "Tucker", "Chip", "Tucker", "chip", "ht2t@virginia.edu", "Professor" ));
        accountList.add( new UserAccount( 90, "Kristen Taylor", "Kristen", "Taylor", "kristen", "ktaylor@virginia.edu", "" ));
        */
        
        accountList.add( new UserAccount( 100, "Robert Bateman", "Robert", "Bateman", "8482", "rbb3e@virginia.edu", "" ));        
        accountList.add( new UserAccount( 101, "Francis Connor", "Francis", "Connor", "2341", "fxc7z@virginia.edu", "" ));
        accountList.add( new UserAccount( 102, "Christopher Forster", "Christopher", "Forster", "3141", "csf2g@virginia.edu", "" ));
        accountList.add( new UserAccount( 103, "Joseph Gilbert", "Joseph", "Gilbert", "5979", "jfg9x@virginia.edu", "" ));
        accountList.add( new UserAccount( 104, "Irene Gomez-Castellano", "Irene", "Gomez-Castellano", "8039", "ig3v@virginia.edu", "" ));
        accountList.add( new UserAccount( 105, "John Havard", "John", "Havard", "1393", "j0h7z@virginia.edu", "" ));
        accountList.add( new UserAccount( 106, "Margaret Konkol", "Margaret", "Konkol", "0421", "m3k4f@virginia.edu", "" ));
        accountList.add( new UserAccount( 107, "Cory MacLauchlin", "Cory", "MacLauchlin", "4140", "cdm4z@virginia.edu", "" ));
        accountList.add( new UserAccount( 108, "James Myers", "James", "Myers", "6518", "jm3yg@virginia.edu", "" ));
        accountList.add( new UserAccount( 109, "Eric Rettberg", "Eric", "Rettberg", "4528", "ejr2f@virginia.edu", "" ));
        accountList.add( new UserAccount( 110, "Amy Wentworth", "Amy", "Wentworth", "4739", "asw8e@virginia.edu", "" ));
        
        for( Iterator i = accountList.iterator(); i.hasNext(); )
        {
            UserAccount userAccount = (UserAccount) i.next();
            try
            {
                insertPlayer(userAccount);
            } 
            catch (MapperException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }
    

    private void insertPlayer( UserAccount account ) throws MapperException
    {
        SimpleLogger.logInfo("Inserting accounts...");
        PreparedStatement stmt = null;
    
        try
        {
            // INSERT INTO player VALUES (17,'nick','e2e4','Nick','Laiacona','ncl2n@virginia.edu','Developer');
            
            String password = Encryption.createMD5HashCode(account.clearPassword);
            
            String command = "INSERT INTO player VALUES (?,?,?,?,?,?,?);";
            stmt = DBManager.instance.getConnection().prepareStatement(command);
            stmt.setInt(1,account.id);
            stmt.setString(2,account.userName);
            stmt.setString(3,password);
            stmt.setString(4,account.firstName);
            stmt.setString(5,account.lastName);
            stmt.setString(6,account.email);
            stmt.setString(7,account.title);
            stmt.executeUpdate();            
       }
       catch (Exception e)
       {
          throw new MapperException("Unable to insert player: "+e);
       }
       finally
       {
          DBManager.instance.close(stmt);
       }
    }
    
    public static void main( String args[] )
    {
        System.setProperty("IVANHOE_DIR","res");
        CreateAccounts converter = new CreateAccounts(); 
        converter.performConversion();
    }

}
