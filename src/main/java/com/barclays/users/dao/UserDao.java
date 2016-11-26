package com.barclays.users.dao;

import com.barclays.users.model.User;

public interface UserDao {

	User getUserByUserName(String username);

}