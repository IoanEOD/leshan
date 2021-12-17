package org.eclipse.leshan.mtserver.demo.model;

import java.net.Inet4Address;

import org.eclipse.leshan.server.registration.Registration;

public class RegistrationRequestObject {

	private String requestType;
	private Registration registration;
	private byte[] addr;
	private String hostName;
	private int port;

	public RegistrationRequestObject(String requestType, Registration registration, byte[] addr, String hostName, int port) {
		setRequestType(requestType);
		this.registration = registration;
		this.addr = addr;
		this.hostName = hostName;
		this.port = port;
	}

	public RegistrationRequestObject(String requestType, Registration registration) {
		// Constructor to automatically extract unserializable InetAddress form registration
		final Inet4Address inetAddr = (Inet4Address) registration.getIdentity().getPeerAddress().getAddress();
		final byte[] ip = inetAddr.getAddress();
		final String hostName = inetAddr.getHostName();
		final int port = registration.getIdentity().getPeerAddress().getPort();

		setRequestType(requestType);
		this.registration = registration;
		this.addr = ip;
		this.hostName = hostName;
		this.port = port;
	}

	public String getRequestType() {
		return this.requestType;
	}

	public void setRequestType(String requestType) {
		// Control types of requests using setter
		if(requestType == "register" || requestType == "update" || requestType == "deregister") {
			this.requestType = requestType;
		}
		else {
			throw new IllegalArgumentException();
		}
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

	public RegistrationRequestObject requestType(String requestType) {
		setRequestType(requestType);
		return this;
	}

	public RegistrationRequestObject registration(Registration registration) {
		setRegistration(registration);
		return this;
	}

	public RegistrationRequestObject addr(byte[] addr) {
		setAddr(addr);
		return this;
	}

	public RegistrationRequestObject hostName(String hostName) {
		setHostName(hostName);
		return this;
	}

	public RegistrationRequestObject port(int port) {
		setPort(port);
		return this;
	}
}