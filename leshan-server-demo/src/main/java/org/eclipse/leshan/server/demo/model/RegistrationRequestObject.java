package org.eclipse.leshan.server.demo.model;

import org.eclipse.leshan.server.registration.Registration;

public class RegistrationRequestObject {

	private Registration registration;
	private byte[] addr;
	private String hostName;
	private int port;


	public RegistrationRequestObject(Registration registration, byte[] addr, String hostName, int port) {
		this.registration = registration;
		this.addr = addr;
		this.hostName = hostName;
		this.port = port;
	}
	

	public Registration getRegistration() {
		return this.registration;
	}

	public void setRegistration(Registration registration) {
		this.registration = registration;
	}

	public byte[] getAddr() {
		return this.addr;
	}

	public void setAddr(byte[] addr) {
		this.addr = addr;
	}

	public String getHostName() {
		return this.hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}