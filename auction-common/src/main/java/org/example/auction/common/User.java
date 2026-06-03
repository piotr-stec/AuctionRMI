package org.example.auction.common;

import java.io.Serializable;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	private String login;
	private String password;
	private String email;
	private boolean admin;

	public User() {
	}

	public User(String login, String password, String email, boolean admin) {
		this.login = login;
		this.password = password;
		this.email = email;
		this.admin = admin;
	}

	public User(String login, String password, String email) {
		this(login, password, email, false);
	}

	public String toCSV() {
		return login + "," + password + "," + email + "," + admin;
	}

	public static User fromCSV(String line) {
		String[] parts = line.split(",");
		return new User(parts[0], parts[1], parts[2], Boolean.parseBoolean(parts[3]));
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isAdmin() {
		return admin;
	} 

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	@Override
	public String toString() {
		return "User{login='" + login + "', email='" + email + "', admin=" + admin + "}";
	}
}
