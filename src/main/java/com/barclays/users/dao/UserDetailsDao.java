package com.barclays.users.dao;

import com.barclays.users.model.UserAttempts;

public interface UserDetailsDao {
	
	public void updateFailAttempts(String username);
	
	public void resetFailAttempts(String username);
	
	public UserAttempts getUserAttempts(String username);
	
}
