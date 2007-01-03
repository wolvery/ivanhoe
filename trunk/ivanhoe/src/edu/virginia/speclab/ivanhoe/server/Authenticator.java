package edu.virginia.speclab.ivanhoe.server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;

public class Authenticator {
	private List rules;

	public Authenticator() {
		this.rules = new ArrayList();

		// add basic authentication rules
		addRule(new DbCheck());
		addRule(new NameCheck());
		addRule(new PasswordCheck());
	}

	/**
	 * Add a new authentication rule
	 * 
	 * @param rule
	 */
	public void addRule(AuthRule rule) {
		if (this.rules.contains(rule) == false) {
			this.rules.add(rule);
			SimpleLogger
					.logInfo("Successfully add auth rule " + rule.getName());
		}
	}

	/**
	 * Authorization rule to check validity of DB connection
	 */
	private static final class DbCheck extends AuthRule {
		public DbCheck() {
			super("DbCheck");
		}

		public boolean executeRule(String userName, String passwd, boolean retry) {
			if (DBManager.instance.isConnected() == false) {
				setMessage("Database temporarily unavailable");
				SimpleLogger.logInfo("User [" + userName
						+ "] authorization failed; DB is unavailable");
				return false;
			}
			return true;
		}
	}

	/**
	 * Authorization rule to check validity of player name
	 */
	private static final class NameCheck extends AuthRule {
		public NameCheck() {
			super("NameCheck");
		}

		public boolean executeRule(String userName, String passwd, boolean retry)
				throws SQLException {
			boolean success = false;
			Statement statement = null;
			ResultSet rs = null;
			try {
				statement = DBManager.instance.getConnection()
						.createStatement();
				String sql = "SELECT playername FROM player";
				rs = statement.executeQuery(sql);
				while (rs.next()) {
					if (rs.getString("playername").equals(userName)) {
						success = true;
						break;
					}
				}
			} catch (SQLException e) {
				DBManager.instance.close(rs);
				DBManager.instance.close(statement);
				throw e;
			}

			DBManager.instance.close(rs);
			DBManager.instance.close(statement);

			if (success == false) {
				setMessage("Invalid user name");
				SimpleLogger.logInfo("User [" + userName
						+ "] failed authorization for incorrect name");
			}
			return success;
		}
	}

	/**
	 * Authorization rule to check validity of password
	 */
	private static final class PasswordCheck extends AuthRule {
		public PasswordCheck() {
			super("PasswordCheck");
		}

		public boolean executeRule(String userName, String passwd, boolean retry) {
			boolean success = false;
			PreparedStatement statement = null;
			String sqlCmd = "SELECT password FROM player WHERE playername=?";
			ResultSet rs = null;
			try {
				statement = DBManager.instance.getConnection()
						.prepareStatement(sqlCmd);
				statement.setString(1, userName);

				rs = statement.executeQuery();
				if (rs.first()) {
					if (rs.getString("password").equals(passwd)) {
						success = true;
					}
				}
			} catch (SQLException e) {
				SimpleLogger.logError("Error validating password: "
						+ e.getMessage());
				if (retry == true) {
					SimpleLogger.logError("Retrying...");
					return executeRule(userName, passwd, false);
				}
			} finally {
				DBManager.instance.close(rs);
				DBManager.instance.close(statement);
			}

			if (success == false) {
				setMessage("Invalid password");
				SimpleLogger.logInfo("User [" + userName
						+ "] failed authorization for incorrect password");
			}

			return success;
		}
	}

	public String performAuthentication(String userName, String password,
			boolean retry) {
		SimpleLogger.logInfo("Authenticating user [" + userName + "]");

		// iterate over rules
		try {
			for (Iterator i = rules.iterator(); i.hasNext();) {
				AuthRule rule = (AuthRule) i.next();
				if (rule.executeRule(userName, password, true) == false) {
					return rule.getMessage();
				}
			}
		} catch (SQLException sqle) {
			if (retry == true && DBManager.instance.reconnect()) {
				// Sometimes, the connection gets into a bad state without
				// being "closed", so we try reconnecting.
				return performAuthentication(userName, password, false);
			} else {
				return "Authentication database error: " + sqle;
			}
		}

		return "";
	}

}
