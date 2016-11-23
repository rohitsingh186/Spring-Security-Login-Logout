package com.barclays.users.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.sql.DataSource;

import org.springframework.security.authentication.LockedException;

import com.barclays.users.model.UserAttempts;
import com.barclays.util.CustomDateFormatter;

public class UserDetailsDaoImpl implements UserDetailsDao {

	private DataSource dataSource;
	private static final int MAX_ATTEMPTS = 3;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void updateFailAttempts(String username) {

		System.out.println("updating user fail attempts for username: " + username);

		UserAttempts userAttempts = getUserAttempts(username);

		if (userAttempts == null) {
			if (isUserExists(username)) {
				// insert new record in user attempts as no record is present
				// till now
				addUserAttempts(username);
			}
		} else {

			if (isUserExists(username)) {
				// update number of attempts
				updateUserAttempts(username);
			}

			if ((userAttempts.getAttempts() + 1) >= MAX_ATTEMPTS) {
				// Lock the user
				lockUser(username);
				// throw exception
				throw new LockedException("User Account is locked!");
			}

		}

	}

	@Override
	public void resetFailAttempts(String username) {
		
		System.out.println("reseting user fail attempts for username: " + username);
		
		String resetUserAttempts = "UPDATE USER_ATTEMPTS SET attempts = 0, lastmodified = ? WHERE username = ?";

		Connection dbConnection;
		try {
			dbConnection = dataSource.getConnection();

			try (PreparedStatement resetUserAttemptsSt = dbConnection.prepareStatement(resetUserAttempts)) {

				resetUserAttemptsSt.setDate(1, CustomDateFormatter.utilToSqlDate(new Date()));
				resetUserAttemptsSt.setString(2, username);
				resetUserAttemptsSt.executeUpdate();

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void addUserAttempts(String username) {

		String addUserAttempt = "INSERT INTO user_attempts (username, attempts, lastModified) " + "values (?, ?, ?);";

		Connection dbConnection;
		try {
			dbConnection = dataSource.getConnection();

			try (PreparedStatement addUserAttemptSt = dbConnection.prepareStatement(addUserAttempt)) {

				addUserAttemptSt.setString(1, username);
				addUserAttemptSt.setInt(2, 1);
				addUserAttemptSt.setDate(3, CustomDateFormatter.utilToSqlDate(new Date()));
				addUserAttemptSt.executeUpdate();

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void updateUserAttempts(String username) {

		String updateUserAttempt = "UPDATE user_attempts SET attempts = attempts + 1, lastmodified = ? WHERE username = ?";

		Connection dbConnection;
		try {
			dbConnection = dataSource.getConnection();

			try (PreparedStatement updatedUserAttemptSt = dbConnection.prepareStatement(updateUserAttempt)) {

				updatedUserAttemptSt.setDate(1, CustomDateFormatter.utilToSqlDate(new Date()));
				updatedUserAttemptSt.setString(2, username);
				updatedUserAttemptSt.executeUpdate();

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void lockUser(String username) {
		
		System.out.println("locking the user for username: " + username);

		String lockUser = "UPDATE users SET accountNonLocked = ? WHERE username = ?";

		Connection dbConnection;
		try {
			dbConnection = dataSource.getConnection();

			try (PreparedStatement lockUserSt = dbConnection.prepareStatement(lockUser)) {

				lockUserSt.setBoolean(1, false);
				lockUserSt.setString(2, username);
				lockUserSt.executeUpdate();

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public UserAttempts getUserAttempts(String username) {
		
		System.out.println("getting user fail attempts for username: " + username);

		UserAttempts userAttempts = null;

		try {

			Connection dbConnection = dataSource.getConnection();
			String userAttemptsSelectQuery = "SELECT * from user_attempts where username = ?";

			try (PreparedStatement userAttemptsSelectSt = dbConnection.prepareStatement(userAttemptsSelectQuery)) {

				userAttemptsSelectSt.setString(1, username);
				ResultSet rs = userAttemptsSelectSt.executeQuery();
				int id;
				String usernameDb;
				int attempts;
				Date lastModified;

				while (rs.next()) {

					id = rs.getInt("id");
					usernameDb = rs.getString("username");
					attempts = rs.getInt("attempts");
					lastModified = rs.getDate("lastModified");

					userAttempts = new UserAttempts();
					userAttempts.setId(id);
					userAttempts.setUsername(usernameDb);
					userAttempts.setAttempts(attempts);
					userAttempts.setLastModified(lastModified);

				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return userAttempts;
	}

	private boolean isUserExists(String username) {
		
		System.out.println("checking user's existance for username: " + username);

		boolean userExitsFlag = false;

		try {

			Connection dbConnection = dataSource.getConnection();
			String userExitsQuery = "SELECT count(*) as userCount from users where username = ?";

			try (PreparedStatement userExitsSt = dbConnection.prepareStatement(userExitsQuery)) {

				userExitsSt.setString(1, username);
				ResultSet rs = userExitsSt.executeQuery();
				int count;

				while (rs.next()) {

					count = rs.getInt("userCount");

					if (count > 0) {
						userExitsFlag = true;

						// Just for testing
						if (count > 1) {
							System.out.println("More than one user");
						}
					}

				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return userExitsFlag;

	}

}
