package com.bbva.logview.bnet.base;

import com.jcraft.jsch.UserInfo;

public class UserSession implements UserInfo {

	private String password;
	private String passPhrase;

	public UserSession(String password, String passPhrase) {
		this.password = password;
		this.passPhrase = passPhrase;
	}

	public String getPassphrase() {
		return this.passPhrase;
	}

	public String getPassword() {
		return this.password;
	}

	public boolean promptPassphrase(String arg0) {
		return true;
	}

	public boolean promptPassword(String arg0) {
		return false;
	}

	public boolean promptYesNo(String arg0) {
		return true;
	}

	public void showMessage(String arg0) {
		System.out.println("SUserInfo.showMessage()");
	}

}
