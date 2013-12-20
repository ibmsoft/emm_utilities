package com.wso2mobile.ios.apns.exception;

public class APNSException extends Exception {

	private static final long serialVersionUID = 4428561477778946321L;

	public APNSException(String errorMessage) {
		super(errorMessage);
	}
}
