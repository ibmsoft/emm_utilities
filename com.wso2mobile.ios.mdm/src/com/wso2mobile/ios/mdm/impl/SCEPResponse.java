package com.wso2mobile.ios.mdm.impl;

public class SCEPResponse {

	private byte[] encodedResponse;
	private CAStatus resultCriteria;

	public byte[] getEncodedResponse() {
		return encodedResponse;
	}

	public void setEncodedResponse(byte[] encodedResponse) {
		this.encodedResponse = encodedResponse;
	}

	public CAStatus getResultCriteria() {
		return resultCriteria;
	}

	public void setResultCriteria(CAStatus resultCriteria) {
		this.resultCriteria = resultCriteria;
	}

}
