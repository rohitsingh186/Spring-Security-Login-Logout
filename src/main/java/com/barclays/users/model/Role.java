package com.barclays.users.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.sun.istack.internal.NotNull;

@Entity
@Table(name = "roles")
public class Role {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Integer id;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "username")
	private User user;

	@NotNull
	@Column(name = "role")
	private String role;

	public Role() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
