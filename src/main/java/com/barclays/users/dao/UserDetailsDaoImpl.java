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

		System.out.println("updating user fail attempts");

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

		System.out.println("updated user fail attempts");

	}

	@Override
	public void resetFailAttempts(String username) {
		
		String resetUserAttempts = "UPDATE USER_ATTEMPTS SET attempts = 0, lastmodified = null WHERE username = ?";

		Connection dbConnection;
		try {
			dbConnection = dataSource.getConnection();

			try (PreparedStatement resetUserAttemptsSt = dbConnection.prepareStatement(resetUserAttempts)) {

				resetUserAttemptsSt.setString(1, username);
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

		UserAttempts userAttempts = null;

		try {

			Connection dbConnection = dataSource.getConnection();
			String userAttemptsSelectQuery = "SELECT * from user_attempts";

			try (PreparedStatement userAttemptsSelectSt = dbConnection.prepareStatement(userAttemptsSelectQuery)) {

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

		boolean userExitsFlag = false;

		try {

			Connection dbConnection = dataSource.getConnection();
			String userExitsQuery = "SELECT count(*) as userCount from users where username = " + username;
			System.out.println(userExitsQuery);

			try (PreparedStatement userExitsSt = dbConnection.prepareStatement(userExitsQuery)) {

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
