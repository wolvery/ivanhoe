/*
 * Created on Oct 7, 2003
 *
 */
package edu.virginia.speclab.ivanhoe.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.mail.*;
import javax.mail.internet.*;

import edu.virginia.speclab.ivanhoe.shared.*;
import edu.virginia.speclab.ivanhoe.shared.data.User;
import edu.virginia.speclab.ivanhoe.shared.message.CreateAccountMsg;
import edu.virginia.speclab.ivanhoe.shared.message.LoginMsg;
import edu.virginia.speclab.ivanhoe.shared.message.LoginResponseMsg;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.NewAccountQueryResponseMsg;
import edu.virginia.speclab.ivanhoe.shared.message.PassResetMsg;
import edu.virginia.speclab.ivanhoe.shared.message.PassResetResponseMsg;
import edu.virginia.speclab.ivanhoe.shared.message.TimeSynch;
import edu.virginia.speclab.ivanhoe.shared.message.UserLookupMsg;
import edu.virginia.speclab.ivanhoe.shared.message.UserLookupResponseMsg;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.mapper.*;

/**
 * Authenticator
 * @author lfoster
 *
 * The authenticator manages a list of unauthenticated proxies,
 * and contains the logic used to perform authentication. Upon
 * successful authentication 
 */
public class UnAuthorizedProxyManager extends Thread implements IDisconnectListener
{     
   private ServerSocket socket;
   private volatile boolean running;
   private IAuthenticationListener listener;
   private List pending;
   private String emailHost;
   private String emailFrom;
   private boolean emailEnabled;
   private boolean allowNewAccounts;
   private boolean defaultNewGamePermission, defaultNewRolePermission, defaultWritePermission;

   private Authenticator authenticator;
   
   /**
    * Constructor
    */
   public UnAuthorizedProxyManager()
   {
      super("Authenticator");
      this.pending = new ArrayList();
      this.emailEnabled = false;
      this.allowNewAccounts = false;
      this.defaultNewGamePermission = false;
      this.defaultNewRolePermission = false;
      this.defaultWritePermission = false;      
      this.authenticator = new Authenticator();
   }

   
   /**
    * Enable email notifications to be sent from the authenticator
    * @param host Email Host
    * @param fromAddr From address for emails sent
    */
   public void enableEmailNotification(String host, String fromAddr)
   {
      this.emailEnabled = true;
      this.emailHost = host;
      this.emailFrom = fromAddr;
      SimpleLogger.logInfo("Authenticator email notifications configured: " +
         this.emailHost + " from  " + this.emailFrom);
   }
   
   /**
    * Start the authenticator; begins listening for connections to authorize
    * at the specified port
    * @param port
    * @throws IOException
    */
   public void startup(int port) throws IOException
   {
      this.running = true;
      this.socket = new ServerSocket(port);
      super.start();
      SimpleLogger.logInfo("Authenticator running at port " + port);
   }
   
   /**
    * No longer accept or authorize connections
    */
   public void shutdown()
   {
      SimpleLogger.logInfo("Authenticator is shutting down...");
      this.running = false;
      try
      {
         this.socket.close();
         try
         {
            this.join(5000);
         }
         catch (InterruptedException e1){}
      }
      catch (IOException e){}
      SimpleLogger.logInfo("Authenticator is shutdown");
   }
   
   /**
    * Accept connections and add them to an unauthorized proxy list
    */
   public void run()
   {
      Socket client = null;
      while (this.running == true)
      {
         client = null;
         
         try
         {
            client = this.socket.accept();
            handleConnect(new CommEndpoint(client));
         }
         catch (IOException e)
         {
            SimpleLogger.logInfo("Socket closed");
         }
      }
   }
   
   /**
    * register a handler that will listen for and process 
    * authentication results
    * @param listener
    */
   public void registerAuthListener(IAuthenticationListener authListener)
   {
      this.listener = authListener;     
   }
   
   /**
    * Removes the auth lietener
    */
   public void unregisterAuthListener()
   {
      this.listener = null;
   }
   
   /**
    * Process a message from an unauthorized proxy. 
    * @param proxy
    * @param msg
    */
   public void handleUnauthorizedMessage(UnathenticatedProxy proxy, Message msg)
   {
      if (msg.getType().equals(MessageType.NEW_ACCOUNT_QUERY))
      {
         proxy.sendMessage(new NewAccountQueryResponseMsg(allowNewAccounts));
      }
      else if (msg.getType().equals(MessageType.LOGIN))
      {
         LoginMsg login = (LoginMsg)msg;
         try
         {
             SimpleLogger.logInfo("Logging client properties: " + login.getClientProperties().toString());
         }
         catch (NullPointerException npe)
         {
             SimpleLogger.logError("Login message received with no clientProperties");
         }
         
         performAuthentication(proxy, login.getSender(), login.getPassword());
      }
      else if (msg.getType().equals(MessageType.CREATE_ACCOUNT))
      {
         handleCreateAccount((CreateAccountMsg)msg);
         proxy.disconnect();
      }
      else if ( msg.getType().equals(MessageType.USER_LOOKUP))
      {
         List users = handleUserLookup((UserLookupMsg) msg);
         proxy.sendMessage(new UserLookupResponseMsg(users));
         proxy.disconnect();
      }
      else if ( msg.getType().equals(MessageType.PASS_RESET))
      {
         boolean resetStatus = handlePasswordReset((PassResetMsg) msg);
         User user = ((PassResetMsg) msg).getUser();

         // send message for success or failure
         proxy.sendMessage(new PassResetResponseMsg(resetStatus, user));
         proxy.disconnect();
      }
      else
      {
         SimpleLogger.logError("Got bad message from unauth connetion., kill it");
         proxy.disconnect();
      }
   }
   
   /**
    * Resets the password in the database and sends a confirmation email the user
    * @param msg A PassResetMsg containing the user object for password reset
    * @return true if password reset works
    */
   private boolean handlePasswordReset(PassResetMsg msg)
   {
      User user = msg.getUser();
      SimpleLogger.logInfo("Resetting password for " + user.getUserName());
      
      // get a random encrypted password
      GPW gpw = new GPW();
      String plainTextPass = gpw.getPassword();
      String encryptedPass = Encryption.createMD5HashCode(plainTextPass);
      
      try
      {
         // create new user object with encrypted password
         User newUser = new User(
            user.getId(),
            user.getUserName(),
            encryptedPass,
            user.getLastName(),
            user.getFirstName(),
            user.getEmail(),
            user.getAffiliation(),
            user.getNewGamePermission());
         
         // reset password in database
         UserMapper.resetPassword(newUser);
         
         // send email confirmation
         StringBuffer mailMsg = new StringBuffer();
         mailMsg.append(newUser.getFirstName());
         mailMsg.append(" ");
         mailMsg.append(newUser.getLastName()).append(", \n\n");
         mailMsg.append("Per your request, your Ivanhoe password has been reset\n");
         mailMsg.append("For the ").append(newUser.getUserName()).append(" login.\n");
         mailMsg.append("The new password is ").append(plainTextPass).append("\n\n");
         mailMsg.append("Please email ivanhoe@nines.org if you have any questions.\n");

         sendMail(newUser.getEmail(), "Ivanhoe Password Updated", mailMsg.toString() );
      } catch (MapperException e)
      {
         SimpleLogger.logError("Password reset for " + user.getUserName() + " failed.");
         return false;
      }
      
      return true;
   }
   
   /**
    * Looks up a user to see if it is found in the game system
    * @param msg A UserLookupMessage object
    */
   private List handleUserLookup(UserLookupMsg msg)
   {
      SimpleLogger.logInfo("Looking up user: " + msg.getIdText());
      List users = new ArrayList();
      String idText = msg.getIdText();
      
      // conduct search for email and id
      if (idText != null && idText.indexOf('@') != -1)
      {
         try
         {
            users = UserMapper.getByEmail(idText);
         } catch (MapperException e)
         {
            SimpleLogger.logError("Failure looking up email (" + idText + ") from database");
         }
      }
      else
      {
         try
         {
            User user = UserMapper.getByName(idText);
            if (user != null)
            {
               users.add(user);
            }
         } catch (MapperException e)
         {
            SimpleLogger.logError("Failure looking up user id (" + idText + ") from database");
         }
      }
      
      return users;
   }
   
   /**
    * Send an email notification to the spacified address
    * @param targetEmail
    * @param messageTitle
    * @param messageBody
    */
   private void sendMail(String targetEmail, String messageTitle, String messageBody)
   {
      // TODO - this method appears to be a duplicate of methods found in the Messenger
      //        class.  This is a candidate for refactoring.
      if (this.emailEnabled == false)
      {
         SimpleLogger.logInfo("SendMail failed; mail is not configured");
         return;
      }
      
      // create some properties and get the default Session
      Properties props = new Properties();
      props.put("mail.smtp.host", this.emailHost);
      Session session = Session.getInstance(props, null);

      try
      {
         // create a message
         javax.mail.Message msg = new MimeMessage(session);
         msg.setFrom(new InternetAddress(this.emailFrom));
         InternetAddress address = new InternetAddress(targetEmail);
         msg.setRecipient(javax.mail.Message.RecipientType.TO, address);
         msg.setSubject(messageTitle);
         msg.setSentDate(new Date());
         msg.setText(messageBody);
         
         // send it
         SimpleLogger.logInfo("Sending mail titled '" + messageTitle + "' to " + targetEmail);
         Transport.send(msg);
      }
      catch (MessagingException mex)
      {
         SimpleLogger.logError("Unable to send email notification: " + mex.getMessage());
      }
   }
   
   /**
    * Handle a create account request
    * @param msg
    */
   protected void handleCreateAccount(CreateAccountMsg msg)
   {
      SimpleLogger.logInfo("Authenticator handling CreateAccount request");
      boolean accountCreated = false;
      final User newUser = msg.getNewUserData();
      
      try
      {
         if (!allowNewAccounts)
         {
             throw new Exception("New account automation is disabled");
         }
          
         newUser.setNewGamePermission(defaultNewGamePermission);
         newUser.setNewRolePermission(defaultNewRolePermission);
         newUser.setWritePermission(defaultWritePermission);
         newUser.setAdmin(false);
         
         accountCreated = UserMapper.createAccount(newUser);
      }
      catch (Exception e)
      {
         SimpleLogger.logError("Unable to create new account [" + 
            msg.getNewUserData().getUserName() + 
            "]: ", e);
      }
      finally
      {
          final String mailSubject;
          final String mailMsg;
          if (accountCreated == true)
          {
              mailSubject = "Ivanhoe Account Created";
              mailMsg =
                 "Your Ivanhoe account has been successfully created.\n"
                 + "   - User Name: "+newUser.getUserName()+"\n"
                 + "   - First Name: "+newUser.getFirstName()+"\n"
                 + "   - Last Name: "+newUser.getLastName()+"\n"
                 + "   - Affiliation: "+newUser.getAffiliation()+"\n"
                 + "   - Password: "+newUser.getPassword()+"\n";
              
              SimpleLogger.logInfo("Account created");
          }
          else
          {
              mailSubject = "Problem Creating Ivanhoe Account";
              mailMsg =
                  "Ivanhoe failed in creating the following account:\n"
                  + "   - User Name: "+newUser.getUserName()+"\n"
                  + "   - First Name: "+newUser.getFirstName()+"\n"
                  + "   - Last Name: "+newUser.getLastName()+"\n"
                  + "   - Affiliation: "+newUser.getAffiliation()+"\n"
                  + "   - Password: "+newUser.getPassword()+"\n"
                  + "\nTry a different username or contact the server administrator about your problem.";
              
              SimpleLogger.logInfo("Problem encountered while creating account");
          }

          sendMail(newUser.getEmail(), mailSubject, mailMsg );
      }
   }

   /**
    * Authenticate a proxy by performing a customizable set of rule cheks
    * @param connection
    * @param userName
    * @param password
    */
   private void performAuthentication(UnathenticatedProxy connection, 
      String userName, String password)
   {      
      SimpleLogger.logInfo("Authenticating user [" + userName + "]");
      
      // check authentication
      LoginResponseMsg resp = new LoginResponseMsg();
      final String ruleMsg = authenticator.performAuthentication(userName, password, true); 
      if (ruleMsg.length() > 0)
      {
          resp.setSuccess(false);
          resp.setReason(ruleMsg);
      }
      else
      {
          resp.setSuccess(true);
      }
            
      // handle result
      if (resp.isSuccess())
      {
         SimpleLogger.logInfo("User [" + userName + "] successfully authorized");
         
         // notify listeners before sending the response
         // -- when a client gets a response it sends messages
         // targeted to the new proxy. Notifying listener 1st makes
         // sure the proxy is fully registerd with server
         if (listener != null)
         {
            // pass the authorized connection to registered listener
            listener.userAuthorized(userName, connection );
         }
         
         // give them the time of day
         TimeSynch timeSynchMsg = new TimeSynch();
         connection.sendMessage(timeSynchMsg);
         
         //  notify sender of success
         connection.sendMessage(resp);
         connection.setEnabled(false);
         
         // remove unauth proxy
         this.pending.remove(connection);
         connection.unregisterDisconnectHandler(this);
         connection = null;
      }
      else
      {       
         SimpleLogger.logInfo("Authorization failed: " + resp.getReason());
         connection.sendMessage(resp); 
         connection.disconnect();
      }
   }

   /**
    * Implementation of the IConnectListener interface. This method
    * is called when a new socket connection is made on the server
    */
   public synchronized void handleConnect(CommEndpoint endpoint)
   {
      SimpleLogger.logInfo("Unauthorized connection registered " +
         "with Authenticator") ;
      UnathenticatedProxy proxy  = new UnathenticatedProxy(this, endpoint);
      proxy.registerDisconnectHandler(this);
      this.pending.add(  proxy );
      proxy.setEnabled(true);
   }

   /**
    * Implementation of the IDisconnectListener interface. This method
    * is called when an Unauthenticated proxy drops its connection
    */
   public void notifyDisconnect(AbstractProxy proxy)
   {
      SimpleLogger.logInfo("Authenticator removing disconnected proxy") ;
      proxy.unregisterDisconnectHandler(this);
      this.pending.remove(proxy);
   }
   
   
   /**
    * UnauthenticatedProxy
    * @author lfoster
    *
    * An extension of an abstract proxy. It awaits te arrival of
    * a login message, and requests authentication. Receipt of any
    * other message type is invalid and will drop the connection.
    */
   private static class UnathenticatedProxy extends AbstractProxy
   {     
      private UnAuthorizedProxyManager auth;
      
      public  UnathenticatedProxy (UnAuthorizedProxyManager auth, CommEndpoint endpoint)
      {
         super(endpoint);
         this.auth = auth;
      }
     
      public void receiveMessage(Message msg)
      {
         // forward all messages to the authenticator
         // after this call the proxy will be authenticated or
         // disconnected
         auth.handleUnauthorizedMessage(this, msg);
      }

      public String getID()
      {
         return "PendingConnection";
      }     
   }

   public void setAllowNewAccounts(boolean allowNewAccounts)
   {
       this.allowNewAccounts = allowNewAccounts;
   }
   
   public void setNewAccountPermissions(boolean newGame, boolean newRole, boolean write)
   {
       // admin permission can't be on by default
       this.defaultNewGamePermission = newGame;
       this.defaultNewRolePermission = newRole;
       this.defaultWritePermission = write;
   }
   
   /**
    * Generates pronounceable passwords from a random source, provided that you
    * believe that anything in the Universe is actually random.  ;)  Credit and
    * details can be found at:
    * 
    * http://www.multicians.org/thvv/gpw.html
    * @author dgran
    */
   private class GPW
   {
      final static String alphabet = "abcdefghijklmnopqrstuvwxyz";
      private String password;

      public GPW()
      {
         this.password = generate(10, 6);
      }
      
      protected String getPassword()
      {
         return this.password;
      }

      private String generate(int npw, int pwl)
      {
         GpwData data = new GpwData();
         int c1, c2, c3;
         long sum = 0;
         int nchar;
         long ranno;
         int pwnum;
         double pik;
         StringBuffer pword = null;
         Random ran = new Random(); // new random source seeded by clock

         // Pick a random starting point.
         for (pwnum = 0; pwnum < npw; pwnum++)
         {
            pword = new StringBuffer(pwl);
            pik = ran.nextDouble(); // random number [0,1]
            ranno = (long) (pik * data.getSigma()); // weight by sum of
                                                    // frequencies
            sum = 0;
            for (c1 = 0; c1 < 26; c1++)
            {
               for (c2 = 0; c2 < 26; c2++)
               {
                  for (c3 = 0; c3 < 26; c3++)
                  {
                     sum += data.get(c1, c2, c3);
                     if (sum > ranno)
                     {
                        pword.append(alphabet.charAt(c1));
                        pword.append(alphabet.charAt(c2));
                        pword.append(alphabet.charAt(c3));
                        c1 = 26; // Found start. Break all 3 loops.
                        c2 = 26;
                        c3 = 26;
                     } // if sum
                  } // for c3
               } // for c2
            } // for c1

            // Now do a random walk.
            nchar = 3;
            while (nchar < pwl)
            {
               c1 = alphabet.indexOf(pword.charAt(nchar - 2));
               c2 = alphabet.indexOf(pword.charAt(nchar - 1));
               sum = 0;
               for (c3 = 0; c3 < 26; c3++)
                  sum += data.get(c1, c2, c3);
               if (sum == 0)
               {
                  break; // exit while loop
               }
               pik = ran.nextDouble();
               ranno = (long) (pik * sum);
               sum = 0;
               for (c3 = 0; c3 < 26; c3++)
               {
                  sum += data.get(c1, c2, c3);
                  if (sum > ranno)
                  {
                     pword.append(alphabet.charAt(c3));
                     c3 = 26; // break for loop
                  } // if sum
               } // for c3
               nchar++;
            } // while nchar
//            pan.add(new Label(password.toString())); // Password generated
         } // for pwnum
         
         return pword.toString();
      } // generate()

      class GpwData
      {
         short tris[][][] = null;

         long sigma[] = null; // 125729

         GpwData()
         {
            int c1, c2, c3;
            tris = new short[26][26][26];
            sigma = new long[1];
            new GpwDataInit1().fill(this); // Break into two classes for NS 4.0
            new GpwDataInit2().fill(this); // .. its Java 1.1 barfs on methods > 65K
            for (c1 = 0; c1 < 26; c1++)
            {
               for (c2 = 0; c2 < 26; c2++)
               {
                  for (c3 = 0; c3 < 26; c3++)
                  {
                     sigma[0] += tris[c1][c2][c3];
                  } // for c3
               } // for c2
            } // for c1
         } // constructor

         void set(int x1, int x2, int x3, short v)
         {
            tris[x1][x2][x3] = v;
         } // set()

         long get(int x1, int x2, int x3) {
    return tris[x1][x2][x3];
  } // get()

  long getSigma() {
    return sigma[0];
  } // get()
   
      }
      
      class GpwDataInit1 {

  final short tris1[][][] = {{ /* [13][26][26] */
                     /* A A */{ 2, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 0,
                           0, 0, 3, 2, 0, 0, 0, 0, 0, 0, 0 },
                     /* A B */{ 37, 25, 2, 5, 38, 0, 0, 2, 46, 1, 0, 304, 0,
                           2, 49, 0, 0, 24, 24, 0, 19, 0, 0, 0, 14, 0 },
                     /* A C */{ 26, 1, 64, 2, 107, 0, 1, 94, 67, 0, 173, 13,
                           5, 1, 35, 1, 13, 32, 3, 114, 23, 0, 0, 0, 45, 0 },
                     /* A D */{ 35, 7, 3, 43, 116, 6, 3, 8, 75, 14, 1, 16, 25,
                           3, 44, 3, 1, 35, 20, 1, 10, 25, 9, 0, 18, 0 },
                     /* A E */{ 2, 0, 2, 1, 0, 1, 3, 0, 0, 0, 0, 10, 0, 2, 3,
                           0, 0, 12, 6, 0, 2, 0, 0, 0, 0, 0 },
                     /* A F */{ 5, 0, 0, 0, 14, 50, 2, 0, 3, 0, 2, 5, 0, 2, 7,
                           0, 0, 5, 1, 39, 1, 0, 0, 0, 1, 0 },
                     /* A G */{ 30, 1, 0, 1, 182, 0, 42, 5, 30, 0, 0, 7, 9,
                           42, 51, 3, 0, 24, 3, 0, 21, 0, 3, 0, 3, 0 },
                     /* A H */{ 12, 0, 0, 0, 20, 0, 0, 0, 3, 0, 0, 5, 4, 2,
                           13, 0, 0, 2, 0, 0, 1, 0, 0, 0, 0, 0 },
                     /* A I */{ 2, 0, 10, 26, 2, 1, 10, 0, 2, 1, 2, 87, 13,
                           144, 0, 2, 0, 93, 30, 23, 0, 3, 1, 0, 0, 0 },
                     /* A J */{ 4, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* A K */{ 11, 0, 1, 1, 98, 1, 0, 1, 15, 0, 0, 3, 0, 0,
                           5, 1, 0, 3, 0, 1, 2, 0, 3, 0, 8, 0 },
                     /* A L */{ 78, 20, 34, 45, 124, 21, 24, 5, 109, 0, 28,
                           237, 31, 3, 53, 23, 0, 7, 16, 69, 29, 26, 5, 0, 26,
                           2 },
                     /* A M */{ 70, 57, 1, 1, 98, 3, 0, 1, 68, 0, 0, 3, 38, 2,
                           43, 69, 0, 3, 14, 3, 12, 0, 2, 0, 14, 0 },
                     /* A N */{ 114, 6, 156, 359, 103, 8, 146, 12, 141, 2, 57,
                           4, 0, 89, 61, 1, 4, 1, 124, 443, 29, 6, 1, 3, 28, 9 },
                     /* A O */{ 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 3, 1, 0, 0,
                           0, 0, 3, 2, 2, 2, 0, 0, 0, 0, 0 },
                     /* A P */{ 29, 3, 0, 1, 59, 1, 0, 86, 25, 0, 1, 14, 1, 1,
                           37, 94, 0, 9, 22, 30, 8, 0, 0, 0, 9, 0 },
                     /* A Q */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 14, 0, 0, 0, 0, 0 },
                     /* A R */{ 124, 64, 101, 233, 115, 12, 47, 5, 188, 3, 61,
                           55, 68, 34, 46, 25, 6, 94, 48, 189, 5, 22, 5, 1,
                           172, 2 },
                     /* A S */{ 19, 3, 32, 0, 71, 0, 1, 81, 49, 0, 22, 3, 19,
                           2, 19, 34, 4, 0, 152, 211, 12, 0, 1, 0, 17, 1 },
                     /* A T */{ 50, 3, 41, 2, 863, 4, 0, 144, 352, 0, 5, 14,
                           6, 3, 144, 0, 0, 60, 13, 106, 57, 1, 5, 0, 8, 5 },
                     /* A U */{ 0, 5, 23, 35, 5, 5, 38, 1, 0, 1, 3, 33, 4, 23,
                           0, 4, 1, 35, 52, 56, 0, 1, 0, 7, 0, 1 },
                     /* A V */{ 35, 0, 0, 1, 108, 0, 0, 0, 49, 0, 0, 1, 0, 0,
                           19, 0, 0, 0, 0, 0, 3, 1, 0, 0, 6, 0 },
                     /* A W */{ 30, 10, 0, 4, 3, 6, 2, 2, 2, 0, 10, 13, 4, 15,
                           3, 0, 0, 6, 3, 5, 0, 0, 0, 0, 2, 0 },
                     /* A X */{ 3, 0, 0, 0, 4, 0, 0, 0, 22, 0, 0, 1, 0, 0, 7,
                           2, 0, 0, 1, 1, 0, 0, 3, 0, 3, 0 },
                     /* A Y */{ 11, 8, 1, 5, 16, 5, 1, 2, 2, 0, 0, 10, 7, 4,
                           13, 1, 0, 3, 5, 7, 3, 0, 5, 0, 0, 0 },
                     /* A Z */{ 10, 0, 0, 1, 22, 0, 0, 0, 10, 0, 0, 0, 0, 0,
                           7, 0, 0, 0, 0, 2, 2, 0, 0, 0, 4, 11 } },
               /* B A */{
                     { 0, 17, 74, 11, 1, 2, 19, 4, 8, 0, 10, 68, 7, 73, 1, 7,
                           0, 110, 54, 55, 9, 1, 3, 1, 12, 1 },
                     /* B B */{ 7, 0, 0, 0, 16, 0, 0, 0, 10, 0, 0, 24, 0, 0,
                           9, 0, 0, 2, 3, 0, 2, 0, 0, 0, 14, 0 },
                     /* B C */{ 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* B D */{ 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 3,
                           0, 0, 1, 0, 0, 3, 0, 0, 0, 0, 0 },
                     /* B E */{ 51, 1, 14, 34, 18, 11, 16, 7, 9, 0, 1, 85, 5,
                           48, 2, 2, 2, 199, 36, 41, 0, 4, 5, 1, 6, 2 },
                     /* B F */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0 },
                     /* B G */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* B H */{ 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5,
                           0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
                     /* B I */{ 34, 8, 22, 21, 8, 3, 9, 1, 0, 3, 1, 50, 7, 45,
                           16, 4, 2, 29, 22, 59, 4, 4, 0, 0, 0, 3 },
                     /* B J */{ 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
                     /* B K */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* B L */{ 57, 0, 0, 0, 519, 0, 0, 0, 35, 0, 0, 0, 0, 0,
                           47, 0, 0, 0, 0, 0, 32, 1, 0, 0, 3, 0 },
                     /* B M */{ 0, 0, 0, 0, 1, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0 },
                     /* B N */{ 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* B O */{ 62, 7, 4, 21, 3, 2, 9, 3, 8, 1, 1, 46, 8, 63,
                           58, 2, 0, 55, 15, 20, 46, 6, 17, 10, 19, 0 },
                     /* B P */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                           0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* B Q */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* B R */{ 110, 0, 0, 0, 77, 0, 0, 0, 100, 0, 0, 0, 0, 0,
                           78, 0, 0, 0, 0, 0, 28, 0, 0, 0, 10, 0 },
                     /* B S */{ 0, 0, 6, 0, 16, 0, 0, 0, 7, 0, 0, 0, 0, 0, 12,
                           0, 0, 0, 0, 27, 2, 0, 0, 0, 0, 0 },
                     /* B T */{ 1, 0, 0, 0, 3, 1, 0, 0, 0, 0, 0, 4, 0, 0, 1,
                           0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* B U */{ 0, 3, 21, 16, 3, 5, 14, 0, 12, 1, 2, 52, 7,
                           20, 2, 0, 1, 104, 44, 54, 0, 0, 0, 3, 1, 5 },
                     /* B V */{ 0, 0, 0, 0, 3, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* B W */{ 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* B X */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* B Y */{ 1, 0, 0, 0, 3, 0, 1, 2, 0, 0, 0, 4, 0, 0, 0,
                           3, 0, 6, 8, 3, 0, 0, 2, 0, 0, 2 },
                     /* B Z */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
               /* C A */{
                     { 1, 47, 17, 33, 1, 3, 4, 5, 7, 1, 3, 120, 40, 120, 1, 59,
                           1, 171, 60, 150, 19, 20, 1, 0, 5, 0 },
                     /* C B */{ 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0 },
                     /* C C */{ 23, 0, 0, 0, 22, 0, 0, 5, 13, 0, 0, 13, 0, 0,
                           26, 0, 0, 7, 0, 0, 27, 0, 0, 0, 0, 0 },
                     /* C D */{ 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* C E */{ 23, 6, 4, 17, 6, 6, 1, 2, 13, 0, 0, 50, 12,
                           109, 7, 43, 0, 76, 63, 22, 1, 0, 4, 0, 2, 1 },
                     /* C F */{ 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* C G */{ 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2,
                           0, 0, 4, 1, 0, 1, 0, 0, 0, 0, 0 },
                     /* C H */{ 165, 10, 2, 3, 176, 4, 3, 1, 141, 0, 0, 26,
                           20, 16, 102, 1, 0, 63, 8, 10, 44, 0, 13, 0, 20, 0 },
                     /* C I */{ 76, 15, 8, 33, 24, 16, 3, 0, 0, 0, 0, 38, 5,
                           45, 50, 28, 0, 29, 38, 71, 6, 8, 0, 0, 0, 0 },
                     /* C J */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* C K */{ 17, 16, 2, 3, 90, 4, 1, 7, 20, 1, 1, 45, 8, 8,
                           12, 9, 0, 3, 32, 6, 6, 0, 13, 0, 22, 0 },
                     /* C L */{ 95, 0, 0, 0, 84, 0, 0, 0, 50, 0, 0, 0, 0, 0,
                           54, 0, 0, 0, 0, 0, 34, 0, 0, 0, 3, 0 },
                     /* C M */{ 1, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
                     /* C N */{ 2, 0, 0, 0, 1, 0, 0, 0, 4, 0, 0, 0, 0, 0, 1,
                           0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
                     /* C O */{ 33, 16, 40, 22, 14, 10, 11, 12, 9, 1, 1, 101,
                           218, 421, 24, 56, 2, 129, 37, 40, 86, 22, 25, 4, 4,
                           2 },
                     /* C P */{ 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
                     /* C Q */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 13, 0, 0, 0, 0, 0 },
                     /* C R */{ 101, 0, 0, 0, 112, 0, 0, 0, 75, 0, 0, 0, 0, 0,
                           88, 0, 0, 0, 0, 1, 41, 0, 0, 0, 25, 0 },
                     /* C S */{ 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 1, 2,
                           0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0 },
                     /* C T */{ 44, 0, 0, 0, 12, 2, 0, 0, 113, 0, 0, 0, 2, 0,
                           94, 0, 0, 46, 0, 0, 42, 0, 1, 0, 3, 0 },
                     /* C U */{ 3, 12, 2, 6, 6, 6, 0, 0, 8, 0, 0, 102, 42, 10,
                           9, 15, 0, 72, 51, 41, 1, 0, 0, 0, 0, 0 },
                     /* C V */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* C W */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* C X */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* C Y */{ 5, 1, 20, 0, 0, 0, 1, 0, 0, 0, 0, 3, 0, 2, 2,
                           4, 0, 3, 2, 9, 0, 0, 0, 0, 0, 0 },
                     /* C Z */{ 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
               /* D A */{
                     { 0, 7, 16, 7, 1, 2, 13, 6, 18, 0, 3, 54, 23, 59, 0, 10,
                           0, 31, 6, 40, 8, 13, 3, 0, 32, 3 },
                     /* D B */{ 9, 0, 0, 0, 7, 0, 0, 0, 3, 0, 0, 2, 0, 0, 8,
                           0, 0, 1, 0, 0, 8, 0, 0, 0, 2, 0 },
                     /* D C */{ 5, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 3,
                           0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0 },
                     /* D D */{ 8, 0, 0, 0, 30, 0, 0, 3, 19, 0, 0, 38, 0, 0,
                           4, 0, 0, 4, 0, 0, 1, 0, 0, 0, 16, 0 },
                     /* D E */{ 34, 37, 82, 14, 17, 41, 11, 4, 5, 2, 0, 88,
                           62, 170, 14, 40, 4, 183, 99, 39, 6, 20, 16, 6, 1, 2 },
                     /* D F */{ 6, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 2, 0, 0, 5,
                           0, 0, 2, 0, 0, 4, 0, 0, 0, 0, 0 },
                     /* D G */{ 4, 0, 0, 0, 73, 0, 0, 0, 2, 0, 1, 1, 1, 0, 0,
                           0, 0, 1, 0, 0, 2, 0, 1, 0, 3, 0 },
                     /* D H */{ 8, 0, 0, 0, 9, 0, 0, 0, 4, 0, 0, 0, 0, 0, 10,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* D I */{ 100, 10, 104, 12, 33, 26, 31, 1, 1, 0, 1, 22,
                           22, 65, 57, 15, 0, 20, 138, 53, 20, 31, 1, 6, 0, 1 },
                     /* D J */{ 4, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4,
                           0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0 },
                     /* D K */{ 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* D L */{ 9, 0, 0, 0, 79, 0, 0, 0, 12, 0, 0, 0, 0, 0, 7,
                           0, 0, 0, 0, 0, 1, 0, 0, 0, 3, 0 },
                     /* D M */{ 13, 0, 0, 0, 3, 0, 0, 0, 21, 0, 0, 0, 0, 0,
                           11, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
                     /* D N */{ 7, 0, 0, 0, 9, 0, 0, 0, 3, 0, 0, 0, 0, 0, 1,
                           0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0 },
                     /* D O */{ 1, 5, 21, 10, 6, 3, 20, 1, 3, 0, 0, 30, 38,
                           54, 17, 7, 0, 39, 11, 10, 30, 5, 54, 5, 1, 3 },
                     /* D P */{ 6, 0, 0, 0, 1, 0, 0, 1, 3, 0, 0, 1, 0, 0, 7,
                           0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* D Q */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0 },
                     /* D R */{ 74, 0, 0, 0, 47, 0, 0, 0, 53, 0, 0, 0, 0, 0,
                           80, 0, 0, 0, 0, 0, 22, 0, 0, 0, 8, 0 },
                     /* D S */{ 1, 0, 3, 0, 10, 0, 0, 9, 5, 0, 1, 3, 10, 0,
                           16, 8, 0, 0, 0, 31, 1, 0, 2, 0, 0, 0 },
                     /* D T */{ 3, 0, 0, 0, 1, 0, 0, 6, 1, 0, 0, 0, 0, 0, 2,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* D U */{ 10, 7, 52, 2, 5, 3, 4, 0, 2, 0, 1, 33, 14, 15,
                           5, 11, 1, 19, 15, 8, 1, 0, 0, 0, 0, 1 },
                     /* D V */{ 3, 0, 0, 0, 13, 0, 0, 0, 7, 0, 0, 0, 0, 0, 2,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* D W */{ 19, 0, 0, 0, 10, 0, 0, 0, 19, 0, 0, 0, 0, 0,
                           8, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2, 0 },
                     /* D X */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* D Y */{ 4, 2, 1, 2, 3, 1, 2, 0, 1, 0, 1, 4, 4, 12, 0,
                           0, 0, 0, 8, 1, 0, 0, 1, 0, 0, 0 },
                     /* D Z */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 } },
               /* E A */{
                     { 0, 39, 34, 110, 0, 12, 13, 3, 0, 0, 50, 68, 38, 71, 0,
                           13, 1, 117, 80, 112, 28, 19, 7, 0, 0, 1 },
                     /* E B */{ 32, 5, 0, 0, 31, 0, 0, 0, 8, 0, 0, 6, 0, 0,
                           28, 0, 0, 32, 2, 3, 29, 0, 0, 0, 4, 0 },
                     /* E C */{ 33, 0, 9, 2, 51, 0, 0, 39, 49, 0, 47, 26, 0,
                           0, 59, 0, 0, 35, 2, 206, 42, 0, 0, 0, 2, 0 },
                     /* E D */{ 29, 7, 1, 16, 45, 5, 22, 3, 88, 0, 0, 8, 9, 4,
                           24, 2, 0, 27, 8, 4, 27, 0, 7, 0, 13, 0 },
                     /* E E */{ 2, 4, 13, 63, 1, 6, 1, 4, 10, 0, 19, 23, 13,
                           66, 1, 42, 0, 43, 9, 34, 1, 4, 6, 0, 0, 8 },
                     /* E F */{ 14, 0, 1, 2, 36, 33, 0, 0, 22, 0, 0, 15, 0, 0,
                           24, 0, 0, 14, 1, 13, 35, 0, 0, 0, 5, 0 },
                     /* E G */{ 48, 1, 0, 0, 36, 1, 15, 2, 38, 0, 0, 7, 4, 4,
                           26, 0, 0, 38, 0, 0, 19, 0, 0, 0, 4, 0 },
                     /* E H */{ 14, 0, 0, 0, 24, 0, 0, 0, 6, 0, 0, 0, 1, 0,
                           18, 0, 0, 4, 0, 0, 4, 0, 0, 0, 3, 0 },
                     /* E I */{ 8, 0, 5, 13, 2, 1, 42, 0, 1, 1, 2, 13, 7, 59,
                           1, 1, 0, 10, 25, 22, 0, 7, 0, 0, 0, 2 },
                     /* E J */{ 4, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3,
                           0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0 },
                     /* E K */{ 2, 1, 0, 1, 6, 0, 0, 0, 4, 0, 0, 0, 0, 1, 1,
                           0, 0, 0, 2, 3, 0, 0, 0, 0, 1, 0 },
                     /* E L */{ 76, 7, 6, 57, 131, 19, 7, 3, 125, 0, 4, 238,
                           22, 1, 48, 15, 0, 4, 27, 26, 17, 19, 2, 0, 7, 0 },
                     /* E M */{ 87, 53, 1, 0, 84, 0, 0, 0, 102, 0, 0, 3, 8, 8,
                           56, 64, 0, 0, 4, 0, 19, 0, 1, 0, 8, 0 },
                     /* E N */{ 78, 17, 68, 159, 128, 8, 35, 14, 96, 2, 2, 4,
                           5, 54, 57, 3, 2, 9, 127, 624, 33, 10, 8, 0, 11, 16 },
                     /* E O */{ 0, 0, 8, 10, 0, 6, 7, 1, 2, 0, 0, 23, 10, 38,
                           0, 16, 0, 14, 6, 4, 41, 3, 2, 2, 0, 1 },
                     /* E P */{ 26, 1, 1, 0, 27, 0, 0, 32, 45, 0, 0, 21, 1, 0,
                           35, 9, 0, 35, 10, 65, 13, 0, 2, 0, 3, 0 },
                     /* E Q */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 59, 0, 0, 0, 0, 0 },
                     /* E R */{ 217, 57, 66, 22, 190, 41, 70, 13, 200, 3, 14,
                           40, 134, 117, 113, 42, 2, 123, 167, 135, 23, 58, 22,
                           1, 123, 1 },
                     /* E S */{ 17, 7, 74, 6, 58, 1, 3, 25, 82, 0, 3, 6, 17,
                           5, 34, 52, 7, 0, 222, 278, 18, 2, 1, 0, 6, 0 },
                     /* E T */{ 78, 3, 19, 0, 129, 4, 0, 93, 105, 0, 1, 3, 2,
                           2, 50, 1, 0, 73, 5, 113, 17, 0, 4, 0, 32, 4 },
                     /* E U */{ 0, 4, 7, 6, 1, 0, 4, 0, 0, 0, 2, 3, 17, 4, 0,
                           15, 0, 46, 20, 18, 0, 2, 1, 0, 0, 0 },
                     /* E V */{ 29, 0, 0, 0, 121, 0, 0, 0, 56, 0, 0, 0, 0, 0,
                           26, 0, 0, 2, 1, 0, 2, 2, 0, 0, 3, 1 },
                     /* E W */{ 33, 4, 3, 4, 16, 2, 0, 5, 24, 0, 0, 3, 3, 3,
                           23, 2, 0, 3, 15, 4, 0, 0, 1, 0, 2, 0 },
                     /* E X */{ 29, 0, 43, 0, 20, 0, 0, 14, 21, 0, 0, 0, 0, 0,
                           15, 78, 1, 0, 0, 72, 12, 0, 0, 1, 2, 0 },
                     /* E Y */{ 7, 3, 1, 4, 25, 2, 0, 2, 0, 0, 1, 4, 6, 4, 4,
                           1, 0, 2, 3, 0, 0, 1, 4, 0, 0, 0 },
                     /* E Z */{ 1, 0, 0, 0, 9, 0, 0, 0, 1, 0, 0, 0, 0, 0, 4,
                           0, 0, 1, 0, 0, 1, 1, 0, 0, 2, 3 } },
               /* F A */{
                     { 1, 10, 39, 5, 2, 1, 1, 3, 18, 0, 2, 35, 10, 27, 0, 0, 0,
                           36, 13, 18, 10, 0, 2, 3, 4, 1 },
                     /* F B */{ 2, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* F C */{ 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* F D */{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* F E */{ 18, 5, 24, 6, 12, 0, 2, 0, 6, 0, 1, 25, 6, 18,
                           2, 0, 0, 114, 17, 15, 4, 2, 2, 0, 1, 0 },
                     /* F F */{ 10, 2, 0, 0, 51, 0, 0, 2, 45, 0, 0, 21, 4, 0,
                           13, 0, 0, 9, 7, 0, 7, 0, 0, 0, 8, 0 },
                     /* F G */{ 1, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* F H */{ 2, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* F I */{ 9, 9, 58, 18, 42, 7, 11, 0, 0, 0, 0, 29, 2,
                           53, 0, 0, 0, 40, 41, 18, 0, 2, 0, 10, 0, 3 },
                     /* F J */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* F K */{ 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* F L */{ 64, 0, 0, 0, 50, 0, 0, 0, 21, 0, 0, 0, 0, 0,
                           60, 0, 0, 0, 0, 0, 42, 0, 0, 0, 15, 0 },
                     /* F M */{ 6, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* F N */{ 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* F O */{ 5, 1, 8, 2, 1, 0, 7, 0, 6, 0, 0, 34, 1, 8, 32,
                           2, 0, 165, 5, 0, 25, 1, 2, 7, 1, 0 },
                     /* F P */{ 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* F Q */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* F R */{ 64, 0, 0, 0, 66, 0, 0, 0, 35, 0, 0, 0, 0, 0,
                           35, 0, 0, 0, 0, 0, 11, 0, 0, 0, 3, 0 },
                     /* F S */{ 1, 0, 0, 0, 2, 0, 0, 2, 0, 0, 1, 0, 0, 0, 1,
                           1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0 },
                     /* F T */{ 1, 1, 1, 0, 19, 0, 0, 3, 1, 0, 0, 0, 1, 0, 3,
                           0, 0, 1, 9, 0, 0, 0, 4, 0, 8, 0 },
                     /* F U */{ 0, 0, 4, 2, 1, 0, 9, 0, 0, 2, 0, 119, 7, 24,
                           0, 0, 0, 28, 31, 6, 0, 0, 0, 0, 0, 2 },
                     /* F V */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* F W */{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* F X */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* F Y */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* F Z */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
               /* G A */{
                     { 0, 20, 5, 11, 3, 2, 11, 3, 13, 0, 0, 68, 24, 60, 1, 5,
                           0, 63, 23, 68, 15, 8, 5, 0, 2, 5 },
                     /* G B */{ 4, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 5,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* G C */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* G D */{ 2, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* G E */{ 23, 3, 2, 4, 12, 1, 1, 3, 4, 0, 0, 32, 8, 141,
                           39, 4, 0, 96, 29, 33, 1, 1, 4, 0, 5, 0 },
                     /* G F */{ 0, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0,
                           0, 0, 1, 0, 0, 3, 0, 0, 0, 0, 0 },
                     /* G G */{ 8, 0, 0, 0, 20, 0, 0, 1, 60, 0, 0, 24, 0, 0,
                           3, 1, 0, 6, 4, 0, 0, 0, 0, 0, 12, 0 },
                     /* G H */{ 18, 4, 1, 1, 12, 2, 1, 1, 2, 0, 1, 4, 0, 3,
                           12, 1, 0, 1, 3, 153, 2, 0, 3, 0, 1, 0 },
                     /* G I */{ 23, 21, 16, 6, 7, 2, 9, 0, 0, 0, 0, 24, 7,
                           103, 17, 1, 0, 10, 26, 19, 3, 10, 0, 0, 0, 1 },
                     /* G J */{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* G K */{ 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* G L */{ 49, 0, 0, 0, 73, 0, 0, 0, 25, 0, 0, 0, 0, 0,
                           38, 0, 0, 0, 0, 0, 13, 0, 0, 0, 17, 0 },
                     /* G M */{ 23, 0, 0, 0, 12, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 1, 3, 0, 0, 0, 1, 0 },
                     /* G N */{ 26, 1, 0, 0, 28, 0, 0, 0, 20, 0, 0, 0, 0, 0,
                           26, 2, 0, 0, 0, 1, 7, 0, 0, 0, 0, 0 },
                     /* G O */{ 6, 4, 3, 16, 6, 1, 10, 1, 5, 0, 0, 22, 1, 49,
                           20, 3, 0, 34, 12, 23, 16, 7, 5, 0, 1, 0 },
                     /* G P */{ 0, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 2, 0, 0, 2,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* G Q */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* G R */{ 216, 0, 0, 0, 97, 0, 0, 0, 43, 0, 0, 0, 0, 0,
                           50, 0, 0, 0, 0, 0, 14, 0, 0, 0, 3, 0 },
                     /* G S */{ 2, 2, 0, 0, 0, 0, 0, 2, 2, 0, 1, 1, 0, 0, 2,
                           1, 0, 0, 0, 18, 0, 0, 1, 0, 0, 0 },
                     /* G T */{ 2, 0, 0, 0, 0, 0, 0, 8, 3, 0, 0, 0, 0, 0, 17,
                           0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* G U */{ 28, 1, 1, 0, 49, 1, 1, 0, 41, 0, 0, 26, 15,
                           24, 2, 0, 0, 14, 22, 6, 0, 0, 0, 0, 3, 1 },
                     /* G V */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* G W */{ 5, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                           0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0 },
                     /* G X */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* G Y */{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 3, 0,
                           6, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* G Z */{ 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
               /* H A */{
                     { 2, 26, 15, 20, 6, 8, 22, 3, 31, 0, 11, 90, 66, 171, 3,
                           25, 0, 142, 30, 49, 20, 11, 20, 0, 13, 8 },
                     /* H B */{ 4, 0, 0, 0, 3, 0, 0, 0, 1, 0, 0, 2, 0, 0, 12,
                           0, 0, 2, 0, 0, 4, 0, 0, 0, 1, 0 },
                     /* H C */{ 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 2,
                           0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* H D */{ 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2,
                           0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* H E */{ 123, 5, 22, 33, 37, 5, 3, 0, 27, 0, 0, 87, 65,
                           86, 17, 7, 1, 311, 57, 42, 11, 11, 14, 8, 11, 2 },
                     /* H F */{ 2, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 2,
                           0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0 },
                     /* H G */{ 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* H H */{ 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* H I */{ 22, 22, 56, 15, 23, 6, 19, 0, 0, 1, 1, 73, 20,
                           79, 17, 41, 0, 36, 53, 39, 3, 11, 0, 0, 0, 6 },
                     /* H J */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* H K */{ 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* H L */{ 5, 0, 0, 0, 11, 0, 0, 0, 8, 0, 0, 0, 0, 0, 22,
                           0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0 },
                     /* H M */{ 21, 0, 0, 0, 15, 0, 0, 0, 6, 0, 0, 0, 1, 0, 7,
                           0, 0, 0, 2, 0, 1, 0, 0, 0, 0, 0 },
                     /* H N */{ 3, 0, 0, 0, 8, 0, 0, 0, 9, 0, 0, 0, 0, 1, 3,
                           0, 0, 0, 4, 0, 2, 0, 0, 0, 0, 0 },
                     /* H O */{ 13, 18, 13, 25, 17, 5, 13, 0, 7, 1, 4, 101,
                           62, 62, 44, 29, 0, 130, 45, 33, 81, 8, 28, 0, 6, 2 },
                     /* H P */{ 3, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0, 1,
                           0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
                     /* H Q */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
                     /* H R */{ 20, 0, 0, 0, 23, 0, 0, 0, 40, 0, 0, 1, 0, 0,
                           72, 0, 0, 0, 0, 0, 13, 0, 0, 0, 3, 0 },
                     /* H S */{ 3, 0, 1, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 3,
                           0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0 },
                     /* H T */{ 3, 0, 2, 1, 21, 9, 1, 7, 5, 0, 0, 1, 4, 3, 4,
                           1, 0, 2, 7, 1, 1, 0, 3, 0, 6, 0 },
                     /* H U */{ 3, 13, 7, 6, 3, 5, 12, 1, 0, 0, 0, 7, 37, 26,
                           0, 3, 0, 37, 24, 15, 0, 0, 0, 2, 2, 1 },
                     /* H V */{ 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* H W */{ 17, 0, 0, 0, 5, 0, 0, 2, 5, 0, 0, 0, 0, 0, 9,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* H X */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* H Y */{ 5, 1, 1, 39, 1, 0, 3, 0, 1, 0, 0, 13, 9, 0, 0,
                           25, 0, 9, 29, 9, 0, 0, 0, 1, 0, 0 },
                     /* H Z */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
               /* I A */{
                     { 0, 33, 20, 8, 1, 0, 17, 5, 1, 0, 2, 169, 20, 230, 0, 3,
                           0, 30, 13, 91, 0, 1, 1, 2, 0, 1 },
                     /* I B */{ 11, 19, 0, 0, 38, 0, 0, 0, 22, 0, 0, 131, 1,
                           2, 10, 0, 0, 20, 1, 0, 23, 0, 0, 0, 2, 0 },
                     /* I C */{ 161, 0, 3, 0, 113, 0, 0, 62, 113, 0, 142, 15,
                           0, 4, 46, 0, 0, 12, 5, 53, 42, 0, 0, 0, 7, 0 },
                     /* I D */{ 51, 2, 0, 31, 232, 0, 30, 0, 46, 1, 0, 5, 1,
                           8, 10, 1, 0, 1, 10, 5, 11, 0, 7, 0, 9, 0 },
                     /* I E */{ 0, 1, 17, 6, 1, 16, 11, 1, 0, 0, 1, 52, 4, 70,
                           0, 1, 0, 66, 18, 50, 7, 17, 6, 0, 0, 2 },
                     /* I F */{ 7, 0, 0, 0, 31, 45, 0, 0, 27, 0, 0, 9, 0, 1,
                           10, 0, 0, 2, 0, 24, 10, 0, 0, 0, 71, 0 },
                     /* I G */{ 48, 0, 0, 0, 41, 0, 30, 147, 30, 0, 0, 4, 15,
                           57, 20, 1, 0, 23, 3, 1, 15, 0, 1, 0, 2, 2 },
                     /* I H */{ 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* I I */{ 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* I J */{ 3, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2,
                           0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
                     /* I K */{ 6, 0, 0, 0, 17, 0, 0, 0, 3, 0, 1, 0, 0, 0, 3,
                           0, 0, 0, 0, 1, 2, 0, 0, 0, 1, 0 },
                     /* I L */{ 60, 10, 6, 36, 106, 6, 5, 7, 90, 0, 13, 253,
                           14, 0, 24, 1, 0, 1, 10, 31, 6, 6, 5, 0, 10, 0 },
                     /* I M */{ 76, 26, 0, 0, 94, 1, 0, 1, 53, 0, 0, 1, 38, 1,
                           30, 133, 0, 1, 8, 0, 17, 0, 0, 0, 2, 0 },
                     /* I N */{ 212, 12, 143, 168, 396, 83, 435, 26, 94, 8,
                           43, 9, 6, 44, 70, 3, 10, 2, 139, 205, 35, 46, 4, 4,
                           15, 1 },
                     /* I O */{ 2, 2, 20, 10, 1, 0, 9, 0, 0, 0, 0, 28, 12,
                           604, 0, 8, 0, 25, 13, 24, 139, 3, 2, 3, 0, 1 },
                     /* I P */{ 20, 5, 0, 0, 26, 2, 0, 16, 16, 1, 0, 33, 6, 0,
                           13, 39, 0, 5, 19, 28, 5, 0, 1, 0, 1, 0 },
                     /* I Q */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 36, 0, 0, 0, 0, 0 },
                     /* I R */{ 41, 2, 39, 24, 106, 7, 9, 0, 19, 0, 11, 20,
                           24, 1, 24, 8, 0, 39, 11, 31, 3, 5, 8, 0, 10, 0 },
                     /* I S */{ 35, 5, 71, 4, 110, 4, 2, 189, 56, 1, 13, 12,
                           93, 5, 55, 33, 3, 6, 85, 271, 4, 1, 1, 0, 8, 0 },
                     /* I T */{ 136, 1, 34, 1, 184, 5, 0, 77, 158, 0, 1, 4, 6,
                           5, 70, 1, 0, 31, 2, 105, 72, 0, 1, 0, 142, 19 },
                     /* I U */{ 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 121, 1, 0,
                           0, 0, 1, 19, 0, 0, 0, 0, 0, 0, 0 },
                     /* I V */{ 57, 0, 0, 0, 292, 0, 0, 0, 37, 0, 0, 0, 0, 0,
                           12, 0, 0, 1, 0, 0, 3, 0, 0, 0, 2, 0 },
                     /* I W */{ 3, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 1,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* I X */{ 1, 0, 0, 0, 2, 1, 1, 0, 3, 0, 0, 0, 0, 0, 4,
                           0, 0, 0, 0, 9, 1, 0, 0, 0, 1, 0 },
                     /* I Y */{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* I Z */{ 9, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7,
                           0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 16 } },
               /* J A */{
                     { 0, 2, 32, 1, 1, 0, 3, 3, 2, 0, 3, 1, 8, 17, 0, 2, 0, 5,
                           2, 0, 2, 3, 2, 1, 1, 2 },
                     /* J B */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J C */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J D */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J E */{ 4, 0, 24, 1, 1, 3, 0, 1, 0, 2, 0, 2, 0, 6, 2,
                           0, 0, 11, 9, 5, 0, 0, 6, 0, 0, 0 },
                     /* J F */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J G */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J H */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J I */{ 0, 1, 0, 0, 0, 1, 4, 0, 0, 0, 0, 2, 4, 3, 0,
                           0, 0, 0, 0, 4, 0, 1, 0, 0, 0, 0 },
                     /* J J */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J K */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J L */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J M */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J N */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J O */{ 4, 2, 6, 0, 3, 0, 3, 12, 10, 0, 1, 6, 0, 5, 0,
                           0, 0, 10, 10, 1, 13, 4, 2, 0, 7, 0 },
                     /* J P */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J Q */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J R */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J S */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J T */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J U */{ 3, 3, 0, 19, 0, 0, 8, 0, 2, 2, 2, 8, 5, 24, 0,
                           1, 0, 15, 9, 5, 0, 1, 0, 2, 0, 0 },
                     /* J V */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J W */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J X */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J Y */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* J Z */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
               /* K A */{
                     { 0, 3, 0, 6, 1, 2, 8, 2, 1, 1, 1, 9, 4, 13, 2, 3, 0, 18,
                           4, 17, 2, 1, 2, 1, 5, 2 },
                     /* K B */{ 3, 0, 0, 0, 3, 0, 0, 0, 2, 0, 0, 0, 0, 0, 11,
                           0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0 },
                     /* K C */{ 2, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0,
                           0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* K D */{ 3, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2,
                           0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* K E */{ 4, 3, 0, 7, 28, 3, 3, 2, 1, 0, 0, 20, 5, 55,
                           3, 3, 0, 59, 18, 56, 2, 1, 4, 0, 27, 0 },
                     /* K F */{ 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3,
                           0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0 },
                     /* K G */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                           0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* K H */{ 9, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 8,
                           0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0 },
                     /* K I */{ 5, 2, 3, 9, 15, 1, 1, 0, 0, 0, 1, 10, 10, 87,
                           2, 4, 0, 11, 15, 13, 0, 2, 2, 0, 0, 0 },
                     /* K J */{ 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* K K */{ 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* K L */{ 15, 0, 0, 0, 46, 0, 0, 0, 13, 0, 0, 0, 0, 0,
                           3, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0 },
                     /* K M */{ 13, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* K N */{ 5, 0, 0, 0, 11, 0, 0, 0, 10, 0, 0, 0, 0, 0,
                           24, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0 },
                     /* K O */{ 1, 1, 2, 3, 2, 4, 0, 2, 1, 0, 1, 3, 1, 7, 1,
                           2, 0, 6, 2, 1, 7, 4, 5, 2, 0, 0 },
                     /* K P */{ 2, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 4, 0, 0, 5,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* K Q */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* K R */{ 10, 0, 0, 0, 3, 0, 0, 0, 3, 0, 0, 0, 0, 0, 6,
                           0, 0, 0, 0, 0, 5, 0, 0, 0, 2, 0 },
                     /* K S */{ 2, 2, 1, 0, 1, 0, 1, 9, 5, 0, 1, 0, 4, 0, 8,
                           3, 0, 0, 0, 11, 4, 0, 1, 0, 1, 0 },
                     /* K T */{ 3, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 5,
                           0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* K U */{ 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 5, 1, 1, 0,
                           8, 0, 2, 1, 1, 0, 0, 1, 0, 1, 0 },
                     /* K V */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* K W */{ 9, 0, 0, 0, 4, 0, 0, 1, 2, 0, 0, 0, 0, 0, 7,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* K X */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* K Y */{ 2, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 4, 0, 0, 2,
                           0, 0, 2, 1, 0, 1, 0, 3, 0, 0, 0 },
                     /* K Z */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
               /* L A */{
                     { 1, 46, 84, 43, 3, 2, 46, 9, 52, 0, 10, 3, 64, 242, 4,
                           23, 1, 157, 92, 210, 45, 21, 23, 9, 42, 11 },
                     /* L B */{ 12, 0, 0, 0, 17, 0, 0, 0, 3, 0, 0, 2, 0, 0,
                           13, 0, 0, 4, 0, 0, 4, 0, 0, 0, 2, 0 },
                     /* L C */{ 9, 0, 0, 0, 6, 0, 0, 12, 4, 0, 0, 1, 1, 0, 19,
                           0, 0, 2, 0, 1, 7, 0, 0, 0, 2, 0 },
                     /* L D */{ 2, 3, 2, 0, 41, 4, 0, 1, 16, 0, 0, 1, 2, 3,
                           13, 1, 0, 8, 9, 2, 3, 0, 5, 0, 3, 0 },
                     /* L E */{ 94, 25, 75, 44, 36, 13, 55, 9, 26, 1, 1, 9,
                           55, 121, 22, 22, 0, 77, 84, 115, 12, 29, 14, 30, 75,
                           1 },
                     /* L F */{ 9, 1, 0, 0, 4, 1, 1, 1, 12, 0, 0, 1, 0, 0, 7,
                           0, 0, 8, 1, 2, 8, 0, 1, 0, 0, 0 },
                     /* L G */{ 16, 0, 0, 0, 12, 0, 0, 0, 10, 0, 0, 0, 0, 0,
                           6, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* L H */{ 7, 0, 0, 0, 6, 0, 0, 0, 2, 0, 0, 0, 0, 0, 7,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* L I */{ 82, 33, 140, 26, 43, 37, 73, 0, 0, 1, 6, 11,
                           46, 238, 50, 40, 13, 5, 90, 127, 12, 36, 0, 3, 0, 7 },
                     /* L J */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* L K */{ 7, 0, 0, 0, 4, 0, 0, 3, 9, 0, 0, 2, 0, 1, 2,
                           0, 0, 0, 3, 0, 0, 0, 3, 0, 8, 0 },
                     /* L L */{ 128, 12, 2, 4, 169, 7, 2, 4, 152, 1, 0, 0, 7,
                           0, 100, 2, 0, 1, 10, 2, 41, 0, 7, 0, 53, 0 },
                     /* L M */{ 27, 0, 0, 2, 11, 0, 0, 2, 9, 0, 0, 0, 1, 0,
                           13, 0, 0, 0, 4, 0, 3, 0, 0, 0, 3, 0 },
                     /* L N */{ 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                           0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0 },
                     /* L O */{ 23, 23, 65, 15, 7, 4, 132, 3, 32, 0, 2, 7, 29,
                           69, 50, 36, 11, 74, 33, 53, 66, 16, 80, 1, 12, 1 },
                     /* L P */{ 11, 0, 0, 0, 3, 1, 0, 21, 5, 0, 0, 0, 1, 0, 6,
                           0, 0, 3, 1, 4, 0, 0, 0, 0, 1, 0 },
                     /* L Q */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* L R */{ 2, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5,
                           0, 0, 0, 0, 0, 2, 0, 0, 0, 6, 0 },
                     /* L S */{ 7, 1, 0, 0, 16, 0, 0, 8, 23, 0, 1, 0, 1, 0,
                           20, 3, 0, 0, 1, 23, 0, 0, 1, 0, 2, 0 },
                     /* L T */{ 22, 1, 0, 0, 23, 0, 0, 14, 34, 0, 0, 0, 2, 0,
                           23, 0, 0, 9, 3, 0, 8, 1, 1, 0, 18, 5 },
                     /* L U */{ 5, 17, 26, 18, 31, 5, 13, 0, 5, 2, 4, 8, 68,
                           31, 15, 5, 0, 21, 68, 56, 0, 4, 0, 13, 0, 1 },
                     /* L V */{ 19, 0, 0, 1, 46, 0, 0, 0, 9, 0, 0, 0, 0, 0, 3,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* L W */{ 8, 0, 0, 0, 2, 0, 0, 1, 2, 0, 0, 0, 0, 0, 9,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },
                     /* L X */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* L Y */{ 2, 4, 12, 2, 2, 2, 3, 7, 2, 0, 1, 3, 13, 11,
                           2, 11, 0, 2, 31, 15, 1, 0, 4, 0, 0, 0 },
                     /* L Z */{ 2, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
               /* M A */{
                     { 0, 10, 59, 34, 3, 0, 57, 7, 31, 3, 25, 104, 6, 326, 2,
                           4, 0, 144, 49, 192, 10, 2, 3, 11, 14, 7 },
                     /* M B */{ 31, 1, 0, 1, 44, 0, 0, 0, 32, 0, 0, 31, 0, 1,
                           27, 1, 0, 32, 1, 0, 21, 0, 0, 0, 0, 0 },
                     /* M C */{ 3, 1, 17, 6, 2, 2, 9, 3, 5, 0, 9, 3, 3, 4, 2,
                           1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* M D */{ 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                           0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* M E */{ 30, 6, 8, 45, 3, 2, 14, 1, 4, 0, 1, 51, 19,
                           283, 10, 4, 0, 125, 39, 128, 0, 2, 9, 3, 4, 1 },
                     /* M F */{ 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 0, 2, 0, 0, 4,
                           0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0 },
                     /* M G */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* M H */{ 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4,
                           0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
                     /* M I */{ 19, 0, 93, 54, 8, 2, 19, 0, 0, 1, 2, 76, 9,
                           194, 4, 0, 1, 21, 96, 109, 10, 0, 0, 5, 0, 1 },
                     /* M J */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* M K */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* M L */{ 1, 0, 0, 0, 3, 0, 0, 0, 6, 0, 0, 0, 0, 0, 3,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* M M */{ 40, 0, 0, 0, 46, 0, 0, 0, 33, 0, 0, 0, 0, 0,
                           32, 0, 0, 0, 0, 0, 17, 0, 0, 0, 12, 0 },
                     /* M N */{ 12, 0, 0, 0, 4, 0, 0, 0, 10, 0, 0, 0, 0, 0, 3,
                           0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0 },
                     /* M O */{ 4, 10, 13, 28, 4, 1, 14, 3, 11, 0, 6, 47, 10,
                           168, 16, 3, 0, 107, 40, 45, 56, 8, 1, 1, 1, 2 },
                     /* M P */{ 52, 3, 0, 0, 71, 1, 1, 26, 18, 0, 4, 71, 0, 0,
                           50, 0, 0, 41, 9, 43, 19, 0, 0, 0, 7, 0 },
                     /* M Q */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0 },
                     /* M R */{ 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3,
                           0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0 },
                     /* M S */{ 0, 1, 2, 1, 5, 1, 0, 2, 3, 0, 1, 0, 2, 0, 8,
                           2, 0, 0, 1, 10, 1, 0, 0, 0, 2, 0 },
                     /* M T */{ 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1,
                           0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* M U */{ 0, 0, 7, 11, 6, 3, 6, 0, 2, 0, 2, 55, 11, 29,
                           2, 1, 0, 18, 53, 30, 0, 0, 0, 0, 0, 3 },
                     /* M V */{ 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* M W */{ 2, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* M X */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                     /* M Y */{ 0, 0, 11, 0, 5, 0, 1, 0, 0, 0, 0, 1, 0, 2, 7,
                           0, 0, 7, 7, 4, 0, 0, 0, 0, 0, 0 },
                     /* M Z */{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } } };

         void fill(GpwData xx)
         {
            int c1, c2, c3;
            for (c1 = 0; c1 < 13; c1++)
            {
               for (c2 = 0; c2 < 26; c2++)
               {
                  for (c3 = 0; c3 < 26; c3++)
                  {
                     xx.set(c1, c2, c3, tris1[c1][c2][c3]);
                  } // for c3
               } // for c2
            } // for c1
         } // fill()

      } // GpwDataInit1
      
      class GpwDataInit2 {

  final  short tris2[][][] = {{ /* [13][26][26] */
/* N A */ {2,24,33,23,6,3,30,6,20,0,9,115,29,59,2,31,0,94,28,159,19,10,5,0,1,5},
/* N B */ {5,0,1,0,20,0,0,0,1,0,0,4,0,0,7,0,0,4,1,0,10,0,0,0,0,0},
/* N C */ {25,0,0,0,190,0,0,87,51,0,1,18,0,0,62,0,0,16,0,36,21,0,0,0,8,0},
/* N D */ {75,11,4,1,162,6,3,7,102,1,1,22,10,2,57,9,2,46,30,4,37,0,11,0,20,0},
/* N E */ {34,12,36,12,29,17,16,4,14,0,0,45,16,20,25,8,6,88,80,84,32,12,37,18,45,3},
/* N F */ {15,0,0,0,30,0,0,0,38,0,0,23,0,0,26,0,0,10,0,0,19,0,0,0,0,0},
/* N G */ {22,8,0,3,114,6,0,15,18,0,3,51,5,0,20,2,0,24,24,28,38,0,2,0,9,0},
/* N H */ {18,0,0,0,16,0,0,0,6,0,0,0,0,0,15,0,0,0,0,0,2,0,0,0,3,0},
/* N I */ {90,9,148,14,33,27,35,4,1,0,5,12,25,44,26,21,7,4,87,94,29,11,0,4,0,4},
/* N J */ {2,0,0,0,3,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,13,0,0,0,0,0},
/* N K */ {6,0,1,0,22,4,1,1,10,0,0,12,2,0,1,1,0,2,2,3,0,0,0,0,9,0},
/* N L */ {9,0,0,0,8,0,0,0,5,0,0,0,0,0,5,0,0,0,0,0,0,0,0,0,1,0},
/* N M */ {8,0,0,0,5,0,0,0,2,0,0,0,0,0,7,0,0,0,0,0,0,0,0,0,0,0},
/* N N */ {39,0,0,0,74,0,0,0,52,0,1,0,0,0,23,0,0,0,1,0,14,0,1,0,25,0},
/* N O */ {4,18,21,10,4,4,15,0,11,0,0,30,60,34,11,11,0,80,32,47,52,18,24,7,2,2},
/* N P */ {0,0,0,0,1,0,0,0,1,0,0,4,0,0,6,0,0,0,0,0,2,0,0,0,0,0},
/* N Q */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,22,0,0,0,0,0},
/* N R */ {3,0,1,0,1,0,0,0,6,0,0,0,0,0,6,0,0,0,0,0,3,0,0,0,6,0},
/* N S */ {26,4,23,2,73,17,3,12,96,0,5,8,13,0,60,25,0,1,3,79,39,4,4,0,5,0},
/* N T */ {143,1,1,1,175,2,2,64,209,0,0,13,3,1,65,1,0,114,3,0,32,0,2,0,21,1},
/* N U */ {12,6,16,6,11,3,6,0,5,0,1,15,35,9,6,3,0,9,25,31,1,0,0,0,0,1},
/* N V */ {15,0,0,0,43,0,0,0,20,0,0,0,0,0,17,0,0,0,0,0,4,0,0,0,1,0},
/* N W */ {12,0,0,0,3,0,0,2,4,0,0,0,0,0,6,0,0,1,0,0,0,0,0,0,0,0},
/* N X */ {0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0},
/* N Y */ {5,3,1,1,0,0,0,1,0,0,0,7,14,0,4,1,1,1,3,1,1,1,2,1,0,0},
/* N Z */ {10,0,0,0,5,0,0,0,5,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,5,0}},
/* O A */ {{1,0,20,30,0,2,5,2,0,0,9,9,8,18,0,4,1,51,13,44,1,1,0,2,0,0},
/* O B */ {17,24,2,2,28,2,0,1,32,4,0,19,0,1,16,0,0,5,26,3,8,3,1,0,2,0},
/* O C */ {50,0,28,0,38,0,0,47,26,0,129,14,0,0,33,0,0,25,0,34,20,0,0,0,8,0},
/* O D */ {17,3,3,15,59,3,13,4,47,0,1,13,2,1,22,3,0,8,11,0,21,0,8,0,35,0},
/* O E */ {0,6,1,7,0,3,0,1,6,0,1,10,3,13,1,0,1,10,15,6,2,7,0,3,1,0},
/* O F */ {7,0,0,0,4,63,0,0,10,0,0,4,1,0,6,0,0,1,0,15,4,0,0,0,1,0},
/* O G */ {34,2,0,1,44,1,22,3,15,1,0,11,3,11,7,0,0,80,1,2,18,0,1,0,83,0},
/* O H */ {10,0,0,0,8,0,0,0,6,0,0,1,5,9,5,0,0,2,0,0,0,0,0,0,1,0},
/* O I */ {3,1,12,53,1,1,2,0,0,0,1,27,0,51,0,0,0,11,39,8,0,0,0,1,0,0},
/* O J */ {1,0,0,0,5,0,0,0,1,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0},
/* O K */ {5,2,1,0,48,0,0,1,7,0,1,4,0,0,3,1,0,0,5,0,3,0,1,0,6,0},
/* O L */ {71,4,6,83,111,8,5,3,121,0,14,124,16,1,132,6,0,1,18,24,43,16,2,0,46,1},
/* O M */ {89,50,1,0,174,5,0,1,76,0,0,2,64,7,56,125,1,1,4,0,4,0,2,0,22,0},
/* O N */ {129,3,64,82,181,52,86,3,124,10,11,7,3,46,75,1,6,10,107,149,8,38,9,1,54,5},
/* O O */ {0,2,4,92,0,22,4,1,0,0,68,42,42,44,0,19,0,21,21,68,0,3,0,0,0,2},
/* O P */ {28,1,2,0,71,0,2,82,32,1,3,16,1,1,45,29,0,17,14,21,10,0,2,0,19,0},
/* O Q */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14,0,0,0,0,0},
/* O R */ {122,26,31,96,138,7,34,2,143,0,61,8,85,76,61,59,1,58,46,211,11,4,9,0,116,1},
/* O S */ {31,4,24,0,107,0,3,18,102,0,2,7,9,1,18,42,2,0,63,127,5,1,2,0,8,0},
/* O T */ {45,7,11,0,64,2,1,88,63,0,0,10,3,1,42,4,0,17,7,63,9,0,3,0,11,0},
/* O U */ {3,11,17,13,3,3,62,1,6,0,0,32,1,137,0,11,1,86,445,103,0,7,0,1,0,2},
/* O V */ {26,0,0,0,109,0,0,0,27,0,1,0,0,0,7,0,0,0,0,0,0,0,0,0,2,0},
/* O W */ {18,14,2,13,48,6,0,8,8,0,1,28,7,83,1,8,0,5,13,2,2,0,1,0,4,1},
/* O X */ {2,1,3,0,5,1,1,3,26,0,0,0,0,1,1,0,0,0,0,1,0,1,1,0,14,0},
/* O Y */ {15,1,4,6,3,1,0,0,1,0,0,3,0,1,4,1,0,1,2,1,0,0,0,0,0,0},
/* O Z */ {2,0,0,0,9,0,0,0,0,0,0,0,0,0,7,0,0,0,0,0,0,0,0,0,3,1}},
/* P A */ {{0,8,38,11,1,0,18,0,17,0,2,50,5,73,1,23,1,176,50,101,18,5,7,1,10,2},
/* P B */ {3,0,0,0,3,0,0,0,0,0,0,1,0,0,6,0,0,2,1,0,3,0,0,0,0,0},
/* P C */ {0,0,0,0,0,0,0,1,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0},
/* P D */ {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,2,0,0,0,0,0,0,0,0},
/* P E */ {51,1,62,34,19,4,8,0,3,1,2,47,2,108,4,10,0,292,22,50,3,1,8,2,2,4},
/* P F */ {0,0,0,0,1,0,0,0,2,0,0,1,0,0,0,0,0,1,0,0,3,0,0,0,0,0},
/* P G */ {2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0},
/* P H */ {56,0,0,2,88,0,0,0,76,0,0,3,0,1,97,0,0,13,1,3,5,0,0,0,79,0},
/* P I */ {21,0,74,25,33,1,19,0,0,0,6,27,3,74,12,11,2,37,27,57,3,2,0,2,0,2},
/* P J */ {1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* P K */ {0,0,0,0,2,0,0,0,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* P L */ {150,0,0,0,121,0,0,0,59,0,0,0,0,0,33,0,0,0,0,0,29,0,0,0,11,0},
/* P M */ {6,0,0,0,2,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,1,0,0,0,0,0},
/* P N */ {0,0,0,0,4,0,0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0},
/* P O */ {2,1,19,10,12,2,7,0,31,0,12,111,14,55,23,17,0,97,126,52,20,3,13,3,2,0},
/* P P */ {16,0,0,0,48,0,0,1,20,0,0,32,1,0,25,0,0,32,3,0,1,0,0,0,16,0},
/* P Q */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* P R */ {39,0,0,0,166,0,0,0,104,0,0,0,0,0,273,0,0,0,0,0,12,0,0,0,1,0},
/* P S */ {4,1,3,0,17,0,0,5,22,0,1,1,2,0,13,0,0,0,0,14,6,0,1,0,35,0},
/* P T */ {16,0,1,0,9,0,0,3,107,0,0,0,0,0,33,0,0,3,0,0,19,0,0,0,4,0},
/* P U */ {1,8,4,8,3,6,4,0,1,0,1,41,8,22,0,9,0,39,18,28,0,0,0,0,0,1},
/* P V */ {0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* P W */ {3,0,0,0,0,0,0,0,2,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0},
/* P X */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* P Y */ {1,2,0,0,0,0,3,0,1,0,1,3,0,0,1,0,0,20,0,3,0,0,1,0,0,0},
/* P Z */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}},
/* Q A */ {{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0},
/* Q B */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q C */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q D */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q E */ {0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q F */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q G */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q H */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q I */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q J */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q K */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q L */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q M */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q N */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q O */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q P */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q Q */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q R */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q S */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q T */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q U */ {110,0,0,0,100,0,0,0,128,0,0,0,0,0,13,0,0,0,0,0,0,0,0,0,3,0},
/* Q V */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q W */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q X */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q Y */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Q Z */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}},
/* R A */ {{0,72,130,95,8,35,73,14,85,3,10,121,95,313,2,119,1,26,66,277,19,45,28,2,28,13},
/* R B */ {32,0,0,0,26,0,0,0,35,0,0,4,0,0,44,0,0,3,1,0,9,0,0,0,5,0},
/* R C */ {18,0,2,0,47,0,0,86,25,0,3,11,0,0,13,0,0,1,2,7,38,0,0,0,4,0},
/* R D */ {22,5,1,0,26,1,0,4,42,0,0,4,0,2,17,1,0,5,9,4,3,0,4,0,7,0},
/* R E */ {166,26,106,99,114,52,55,20,25,4,4,60,69,143,20,72,8,11,257,119,14,56,34,7,23,2},
/* R F */ {11,0,0,0,15,1,0,0,9,0,0,7,0,0,8,0,0,4,0,0,12,0,0,0,0,0},
/* R G */ {26,0,0,0,63,0,0,5,25,0,0,11,1,0,18,0,0,2,2,0,13,0,0,0,11,0},
/* R H */ {11,0,0,0,19,0,0,0,5,0,0,0,0,0,18,0,0,0,0,0,2,0,0,0,3,0},
/* R I */ {182,54,210,87,79,38,65,1,0,1,6,49,65,166,82,61,1,0,151,141,29,44,1,6,1,10},
/* R J */ {0,0,0,0,3,0,0,0,0,0,0,0,0,0,3,0,0,0,0,0,2,0,0,0,0,0},
/* R K */ {4,2,0,1,19,0,0,3,9,0,0,6,3,2,5,3,0,1,10,2,0,0,1,0,6,0},
/* R L */ {24,2,0,4,28,0,0,0,36,0,0,0,0,0,14,1,0,0,2,1,2,0,1,0,8,0},
/* R M */ {97,1,2,0,29,2,0,3,65,0,0,2,0,0,39,1,0,0,1,1,10,0,1,0,5,0},
/* R N */ {53,5,0,0,50,4,0,3,29,0,1,0,6,0,16,1,0,0,9,5,7,0,2,0,4,0},
/* R O */ {46,40,79,40,18,22,56,4,32,5,10,76,90,167,84,127,2,14,127,74,127,42,63,17,15,3},
/* R P */ {10,0,0,0,21,0,0,33,10,0,0,5,1,0,25,0,0,12,8,8,5,0,0,0,1,0},
/* R Q */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,10,0,0,0,0,0},
/* R R */ {53,0,0,0,92,0,0,5,85,0,0,0,0,0,47,0,0,0,0,0,14,0,0,0,60,0},
/* R S */ {26,2,2,2,84,1,0,16,44,0,4,2,3,1,43,12,1,0,0,32,14,1,2,0,2,0},
/* R T */ {39,2,2,0,61,5,3,101,99,0,0,11,7,3,32,0,0,17,12,1,27,0,2,0,24,7},
/* R U */ {5,21,30,31,15,6,12,0,18,0,0,10,46,41,1,28,0,3,83,22,0,1,1,1,0,1},
/* R V */ {31,0,0,0,37,0,0,0,28,0,0,0,0,0,5,0,0,0,0,0,1,0,0,0,2,0},
/* R W */ {15,0,0,0,6,0,0,0,12,0,0,0,0,0,15,0,0,0,0,0,0,0,0,0,0,0},
/* R X */ {0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* R Y */ {5,3,3,5,3,0,1,0,0,0,0,10,11,4,12,16,0,0,9,4,0,0,2,0,0,0},
/* R Z */ {2,0,0,0,1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0}},
/* S A */ {{2,44,23,16,1,10,21,4,16,1,7,80,17,89,1,10,0,36,10,43,22,10,13,5,7,0},
/* S B */ {9,0,0,0,4,0,0,0,2,0,0,0,0,0,6,0,0,2,0,0,18,0,0,0,3,0},
/* S C */ {81,0,0,0,65,0,1,78,37,0,0,5,1,0,88,0,0,92,0,0,40,0,0,0,3,0},
/* S D */ {11,0,0,0,0,0,0,0,1,0,0,0,0,0,2,0,0,2,0,0,2,0,0,0,0,0},
/* S E */ {38,14,47,18,33,7,8,3,11,0,1,63,39,101,5,28,14,83,28,41,12,19,15,15,19,1},
/* S F */ {3,0,0,0,7,0,0,0,5,0,0,0,0,0,7,0,0,0,0,0,6,0,0,0,1,0},
/* S G */ {0,0,0,0,2,0,0,0,2,0,0,0,0,0,2,0,0,5,1,0,2,0,0,0,0,0},
/* S H */ {97,9,1,0,79,3,0,0,75,0,1,4,16,3,81,2,0,27,0,1,20,1,6,0,17,0},
/* S I */ {55,56,44,80,28,15,38,0,0,0,2,50,40,78,148,7,1,7,99,89,9,76,0,8,0,3},
/* S J */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0},
/* S K */ {9,0,0,0,24,0,0,0,35,0,0,0,2,0,3,0,0,1,0,0,5,0,0,0,23,0},
/* S L */ {42,0,0,0,35,0,0,0,29,0,0,1,0,0,29,0,0,0,0,0,13,0,0,0,2,0},
/* S M */ {57,0,0,0,30,0,0,0,31,0,0,0,0,0,25,0,0,0,0,0,14,0,0,0,2,0},
/* S N */ {21,0,0,0,12,0,0,0,12,0,0,0,0,0,19,0,0,0,0,4,6,0,0,0,2,0},
/* S O */ {6,4,26,12,6,10,4,1,8,1,0,67,65,190,8,21,0,71,0,11,34,6,3,0,3,1},
/* S P */ {63,1,0,0,116,0,0,41,82,0,0,24,0,0,69,0,0,34,1,0,16,0,0,0,3,0},
/* S Q */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,52,0,0,0,0,0},
/* S R */ {4,0,0,0,1,0,0,0,1,0,0,0,0,0,2,0,0,0,1,0,3,0,0,0,0,0},
/* S S */ {50,3,2,0,77,3,0,4,151,0,0,5,11,1,42,2,0,4,0,4,17,0,13,0,19,0},
/* S T */ {258,6,4,1,291,9,1,11,240,1,0,25,12,2,205,6,0,255,3,0,58,2,7,0,36,0},
/* S U */ {14,38,17,6,7,11,6,0,11,0,0,39,35,37,1,42,0,71,30,4,0,0,0,0,0,4},
/* S V */ {0,0,0,0,5,0,0,0,6,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0},
/* S W */ {37,0,0,0,31,0,0,0,28,0,0,0,0,0,21,0,0,2,0,0,2,0,0,0,0,0},
/* S X */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* S Y */ {0,2,32,1,1,0,1,0,0,0,1,18,19,30,0,2,0,9,5,1,0,0,0,0,0,1},
/* S Z */ {0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0}},
/* T A */ {{0,74,44,8,3,9,45,8,68,0,15,130,36,181,1,23,0,128,22,185,13,11,9,13,4,0},
/* T B */ {7,0,0,0,4,0,0,0,4,0,0,0,0,0,6,0,0,3,0,0,3,0,0,0,0,0},
/* T C */ {5,0,0,0,0,0,0,112,0,0,0,2,0,0,5,0,0,1,0,0,1,0,0,0,1,0},
/* T D */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,1,0,0,0,0,0,0,0,0},
/* T E */ {52,9,29,37,66,9,17,6,16,0,2,65,49,185,18,20,0,588,61,23,9,9,9,16,1,0},
/* T F */ {6,0,0,0,1,0,0,0,5,0,0,1,0,0,6,0,0,1,0,0,24,0,0,0,0,0},
/* T G */ {4,0,0,0,2,0,0,0,0,0,0,0,0,0,2,0,0,1,0,0,1,0,0,0,0,0},
/* T H */ {68,6,1,5,274,8,1,2,62,0,1,9,13,3,90,4,1,61,8,2,31,0,16,0,49,0},
/* T I */ {99,35,342,16,35,45,34,0,0,0,3,67,75,183,419,28,9,18,75,88,9,128,0,0,0,2},
/* T J */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* T K */ {2,0,0,0,1,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0},
/* T L */ {18,0,0,0,102,0,0,0,5,0,0,2,0,0,3,0,0,0,0,0,2,0,0,0,3,0},
/* T M */ {25,0,0,0,8,0,0,0,3,0,0,0,0,0,11,0,0,0,0,0,3,0,0,0,0,0},
/* T N */ {3,0,0,0,9,0,0,0,5,0,0,0,0,0,2,0,0,0,0,4,1,0,0,0,0,0},
/* T O */ {5,6,34,11,8,7,26,0,14,0,9,38,65,238,26,56,0,319,19,16,36,3,36,7,3,2},
/* T P */ {2,0,0,0,1,0,0,0,1,0,0,2,0,0,3,0,0,5,0,0,0,0,0,0,0,0},
/* T Q */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* T R */ {315,0,0,0,98,0,0,0,246,0,0,0,0,0,201,0,0,0,0,0,68,0,1,0,64,0},
/* T S */ {2,2,2,1,10,2,0,3,4,0,1,0,13,0,9,3,0,0,0,8,5,2,5,0,3,0},
/* T T */ {44,0,0,0,154,1,1,2,53,0,1,45,0,0,33,0,0,10,8,0,4,1,0,0,25,0},
/* T U */ {41,14,9,41,8,5,4,0,10,0,0,19,30,29,13,10,0,159,35,22,0,0,0,1,1,0},
/* T V */ {3,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* T W */ {14,0,0,0,12,0,0,1,23,0,0,0,0,0,15,0,0,0,0,0,2,0,0,1,0,0},
/* T X */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* T Y */ {2,1,2,0,0,0,1,0,1,0,0,14,2,0,0,34,0,14,3,0,0,0,2,1,0,0},
/* T Z */ {1,0,0,0,5,0,1,0,2,0,0,1,1,0,1,1,0,1,1,0,0,0,0,0,0,0}},
/* U A */ {{0,4,7,21,0,1,5,1,4,0,5,51,2,26,0,1,0,48,9,37,0,2,4,0,3,0},
/* U B */ {8,18,0,1,20,0,0,2,18,2,0,23,5,0,2,1,0,10,15,8,7,2,0,0,1,0},
/* U C */ {10,0,14,0,23,0,0,31,29,0,55,16,0,0,7,0,0,9,1,47,5,0,0,0,2,0},
/* U D */ {17,1,0,24,67,0,18,0,39,0,0,4,0,0,8,0,0,1,10,0,2,0,2,0,7,1},
/* U E */ {6,9,0,1,5,5,4,1,0,1,0,21,1,33,1,1,0,19,22,15,2,0,0,0,3,6},
/* U F */ {1,0,0,0,0,58,0,0,0,0,0,1,1,0,1,0,0,0,0,3,1,0,0,0,0,0},
/* U G */ {19,1,0,0,21,0,34,80,3,0,0,4,2,2,6,0,0,1,1,0,11,0,0,0,0,0},
/* U H */ {3,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0},
/* U I */ {3,2,14,14,6,0,1,0,0,0,0,32,0,31,1,8,0,19,44,64,1,4,0,2,0,3},
/* U J */ {1,0,0,0,0,0,0,0,2,0,0,0,0,0,1,0,0,0,0,0,3,0,0,0,0,0},
/* U K */ {1,0,0,1,12,0,0,0,3,0,1,0,0,0,1,0,0,2,0,0,0,0,0,0,0,0},
/* U L */ {136,4,11,11,46,14,7,0,35,0,10,67,5,2,23,16,0,1,24,73,16,3,1,0,5,1},
/* U M */ {22,52,3,1,51,5,0,1,32,0,0,2,28,11,8,48,1,0,8,1,6,2,0,0,0,0},
/* U N */ {21,6,73,131,25,5,46,2,55,0,33,4,2,13,4,2,0,2,15,82,1,0,2,0,5,0},
/* U O */ {0,0,0,1,0,0,0,0,3,0,0,2,0,3,0,2,0,16,3,5,29,0,0,0,2,0},
/* U P */ {4,4,1,2,31,1,1,14,10,0,1,13,1,0,8,24,0,13,13,24,2,0,2,0,2,0},
/* U Q */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0},
/* U R */ {75,27,21,17,149,8,60,1,66,2,11,17,11,55,28,15,1,51,43,43,9,15,3,0,28,1},
/* U S */ {31,5,29,2,105,0,1,53,64,0,17,3,0,1,8,12,1,0,34,115,6,0,0,0,4,0},
/* U T */ {45,1,14,1,69,0,1,55,77,0,0,8,3,3,49,0,0,13,7,51,11,0,2,0,6,2},
/* U U */ {0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* U V */ {0,0,0,0,8,0,0,0,5,0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0},
/* U W */ {2,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* U X */ {0,0,0,0,4,0,0,0,2,0,0,1,0,0,1,0,0,0,0,5,4,0,0,0,0,0},
/* U Y */ {1,0,0,0,1,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,1,1,0,0,0,0},
/* U Z */ {2,0,0,0,4,0,0,0,0,0,0,0,0,0,3,0,0,0,0,0,1,0,0,0,0,12}},
/* V A */ {{0,9,20,8,1,0,14,2,8,1,3,69,2,57,0,1,0,31,18,36,5,0,0,0,0,0},
/* V B */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V C */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V D */ {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V E */ {6,2,5,4,4,3,6,4,5,0,1,47,4,120,3,1,0,271,46,24,0,0,1,5,10,0},
/* V F */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V G */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V H */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V I */ {37,4,33,23,21,2,8,0,2,0,3,43,0,47,18,0,0,16,65,30,5,16,0,2,0,1},
/* V J */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V K */ {0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V L */ {2,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
/* V M */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V N */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V O */ {0,0,23,0,0,0,3,0,9,0,5,48,2,6,1,0,0,10,4,9,10,1,3,0,6,0},
/* V P */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V Q */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V R */ {0,0,0,0,5,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0},
/* V S */ {0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V T */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V U */ {0,0,0,0,0,0,0,0,0,0,0,13,0,0,0,0,0,2,2,0,0,0,0,0,0,0},
/* V V */ {0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0},
/* V W */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V X */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* V Y */ {0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0},
/* V Z */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0}},
/* W A */ {{1,4,7,8,0,3,12,3,18,0,8,53,5,20,0,4,0,100,27,55,1,9,1,4,71,1},
/* W B */ {6,0,0,0,7,0,0,0,1,0,0,0,0,0,10,0,0,3,0,0,1,0,0,0,0,0},
/* W C */ {3,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0},
/* W D */ {0,0,0,0,5,0,0,0,1,0,0,0,0,0,4,0,0,3,0,0,1,0,0,0,3,0},
/* W E */ {30,5,1,9,33,0,2,1,19,0,0,51,0,11,0,2,0,36,21,7,0,2,0,0,2,0},
/* W F */ {1,0,0,0,0,0,0,0,3,0,0,3,0,0,4,0,0,0,0,0,3,0,0,0,0,0},
/* W G */ {0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* W H */ {18,0,0,0,47,0,0,0,52,0,0,0,0,0,19,0,0,0,0,0,1,0,0,0,1,0},
/* W I */ {0,0,14,18,5,5,15,0,0,0,0,40,2,83,0,2,0,8,38,47,0,4,0,1,0,2},
/* W J */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* W K */ {0,0,0,0,0,0,0,0,2,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,1,0},
/* W L */ {3,0,0,0,9,0,0,0,5,0,0,0,0,0,1,0,0,0,1,1,0,0,0,0,3,0},
/* W M */ {8,0,0,0,5,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* W N */ {0,1,1,1,6,1,1,2,3,0,0,0,0,0,0,2,0,1,10,4,1,0,2,0,3,0},
/* W O */ {0,1,0,0,3,1,0,0,0,0,3,10,17,8,54,1,0,121,1,1,3,2,1,0,0,0},
/* W P */ {1,0,0,0,1,0,0,0,1,0,0,1,0,0,5,0,0,0,0,0,1,0,0,0,0,0},
/* W Q */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* W R */ {7,0,0,0,12,0,0,0,25,0,0,0,0,0,10,0,0,0,0,0,0,0,0,0,6,0},
/* W S */ {0,1,1,0,2,0,0,1,1,0,1,2,2,0,5,3,0,1,1,4,1,0,2,0,1,0},
/* W T */ {1,0,0,0,1,0,0,3,1,0,0,0,0,0,3,0,0,0,0,0,1,0,0,0,0,0},
/* W U */ {0,0,0,0,0,0,0,1,0,0,0,1,1,1,0,1,0,2,0,0,0,0,0,0,0,0},
/* W V */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* W W */ {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
/* W X */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* W Y */ {2,0,0,0,5,0,0,0,0,0,0,1,1,4,1,0,0,0,0,0,0,0,0,0,0,0},
/* W Z */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0}},
/* X A */ {{0,0,5,1,0,1,3,0,0,0,0,4,6,6,0,0,0,0,3,6,0,1,0,0,0,0},
/* X B */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0},
/* X C */ {3,0,0,0,11,0,0,3,7,0,0,7,0,0,3,0,0,5,0,0,7,0,0,0,0,0},
/* X D */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* X E */ {0,0,7,1,0,0,2,0,1,0,0,2,6,9,0,0,0,6,1,1,0,0,0,0,1,0},
/* X F */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0},
/* X G */ {0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0},
/* X H */ {7,0,0,0,0,0,0,0,4,0,0,0,0,0,4,0,0,0,0,0,2,0,0,0,0,0},
/* X I */ {8,2,12,8,4,2,2,0,0,0,0,2,11,4,8,0,0,0,9,2,0,1,1,0,0,0},
/* X J */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* X K */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* X L */ {0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* X M */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* X N */ {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* X O */ {0,0,0,1,0,0,3,0,0,0,0,1,1,10,0,1,0,6,1,5,0,0,0,0,0,0},
/* X P */ {8,0,0,0,27,0,0,0,5,0,0,18,0,0,12,0,0,7,0,0,3,0,0,0,0,0},
/* X Q */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0},
/* X R */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* X S */ {0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* X T */ {6,1,0,0,22,0,0,1,7,0,0,0,0,0,7,0,0,31,0,0,9,0,0,0,1,0},
/* X U */ {4,1,0,2,0,0,0,0,0,0,0,3,0,0,0,1,0,6,0,0,0,0,0,0,0,0},
/* X V */ {0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* X W */ {0,0,0,0,2,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0},
/* X X */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
/* X Y */ {0,0,0,0,0,0,2,0,0,0,0,6,0,0,0,0,0,2,0,0,0,0,0,0,0,0},
/* X Z */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}},
/* Y A */ {{0,0,5,5,0,0,1,1,0,0,2,11,3,29,1,4,1,20,1,3,0,0,3,0,0,0},
/* Y B */ {4,0,0,4,7,0,0,0,2,0,0,0,0,0,9,0,0,3,0,0,3,0,0,0,0,0},
/* Y C */ {4,0,0,0,18,0,0,31,4,0,0,19,0,0,12,0,0,0,0,0,0,0,0,0,0,0},
/* Y D */ {4,1,0,0,12,0,0,0,2,0,0,0,0,2,1,0,0,37,0,0,0,0,0,0,0,0},
/* Y E */ {11,3,0,1,1,1,1,0,1,0,0,13,1,6,2,1,0,19,7,6,0,1,1,0,0,0},
/* Y F */ {1,0,0,0,1,0,0,0,3,0,0,2,0,0,0,0,0,0,0,0,4,0,0,0,0,0},
/* Y G */ {0,0,0,1,2,0,0,0,2,0,0,1,3,1,8,0,0,3,0,0,1,0,0,0,2,0},
/* Y H */ {0,0,0,0,4,0,0,0,0,0,0,0,0,0,10,0,0,0,0,0,0,0,0,0,1,0},
/* Y I */ {0,0,0,1,1,0,0,0,0,0,0,0,0,9,0,2,0,0,2,0,0,0,0,0,0,0},
/* Y J */ {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Y K */ {0,0,0,0,3,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0},
/* Y L */ {15,0,0,0,22,0,0,0,13,0,1,19,0,0,11,1,0,0,2,0,3,6,0,0,0,0},
/* Y M */ {18,4,1,0,20,0,0,0,5,0,0,0,3,7,11,20,0,0,0,0,2,0,0,0,1,0},
/* Y N */ {14,0,11,3,12,0,3,1,2,0,0,0,0,3,11,0,0,0,0,6,0,0,0,2,1,0},
/* Y O */ {0,0,2,2,0,4,6,0,0,0,5,2,1,18,0,4,0,8,4,5,17,1,1,0,0,1},
/* Y P */ {2,0,0,0,24,0,0,17,5,0,0,2,0,2,21,0,0,5,7,16,3,0,0,0,1,0},
/* Y Q */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0},
/* Y R */ {15,0,0,2,6,1,0,0,21,0,0,0,0,2,29,0,0,2,0,1,4,0,0,0,1,0},
/* Y S */ {3,1,3,0,12,0,0,1,38,0,0,1,2,0,4,3,0,0,6,39,2,0,0,0,0,0},
/* Y T */ {2,0,0,0,16,0,0,16,10,0,0,0,0,0,12,0,0,0,0,2,0,0,0,0,1,0},
/* Y U */ {0,0,3,0,0,0,3,1,0,0,2,1,0,1,0,1,0,0,2,0,0,0,0,0,0,0},
/* Y V */ {1,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Y W */ {10,0,1,0,3,0,0,2,4,0,0,0,0,0,5,0,0,3,0,0,0,0,0,0,0,0},
/* Y X */ {0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Y Y */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Y Z */ {2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0}},
/* Z A */ {{1,3,2,0,0,0,5,1,1,0,1,4,1,11,0,1,0,19,0,0,0,1,0,0,0,1},
/* Z B */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z C */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z D */ {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z E */ {5,1,2,1,1,0,0,0,1,0,1,7,0,12,0,0,0,13,3,3,1,0,1,0,0,0},
/* Z F */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z G */ {0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z H */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z I */ {1,1,2,0,7,0,5,0,0,0,0,5,4,6,1,1,0,2,1,1,1,0,0,0,0,0},
/* Z J */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z K */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z L */ {0,0,0,0,16,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,2,0},
/* Z M */ {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z N */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z O */ {3,0,0,2,2,0,1,0,7,0,0,0,3,10,5,2,0,5,0,0,1,1,0,0,0,0},
/* Z P */ {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z Q */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z R */ {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
/* Z S */ {0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z T */ {0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z U */ {0,0,1,0,1,0,0,0,0,0,1,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0},
/* Z V */ {0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
/* Z W */ {0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
/* Z X */ {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z Y */ {0,1,0,0,0,0,4,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* Z Z */ {7,0,0,0,1,0,0,0,7,0,0,17,0,0,2,0,0,0,0,0,0,0,1,0,5,0}
}};

   void fill(GpwData xx) {
    int c1,c2,c3;
      for (c1=0; c1 < 13; c1++) {
   for (c2=0; c2 < 26; c2++) {
     for (c3=0; c3 < 26; c3++) {
       xx.set(c1+13, c2, c3, tris2[c1][c2][c3]);
     } // for c3
   } // for c2
      } // for c1
  } // fill()

} // GpwDataInit2


   }
}
