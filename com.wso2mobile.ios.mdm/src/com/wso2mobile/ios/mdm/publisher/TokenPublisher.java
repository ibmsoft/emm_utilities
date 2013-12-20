package com.wso2mobile.ios.mdm.publisher;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.google.gson.JsonObject;
import com.wso2mobile.ios.mdm.exception.MDMException;
import com.wso2mobile.ios.mdm.plist.DeviceProperties;
import com.wso2mobile.ios.mdm.util.AppConfigurations;

public class TokenPublisher {

	public int publishiOSTokens(DeviceProperties deviceProperties)
			throws IOException, MDMException {

		String model = "";

		if (deviceProperties.getProduct() == null) {
			model = "";
		} else if (deviceProperties.getProduct().contains(
				AppConfigurations.I_PAD)) {
			model = AppConfigurations.I_PAD;
		} else if (deviceProperties.getProduct().contains(
				AppConfigurations.I_POD)) {
			model = AppConfigurations.I_POD;
		} else if (deviceProperties.getProduct().contains(
				AppConfigurations.I_PHONE)) {
			model = AppConfigurations.I_PHONE;
		} else {
			model = "";
		}

		JsonObject innerProperties = new JsonObject();
		// innerProperties.addProperty("CHALLENGE",
		// deviceProperties.getChallenge());
		innerProperties.addProperty("product", deviceProperties.getProduct());
		innerProperties.addProperty("device", "");
		innerProperties.addProperty("serial", deviceProperties.getSerial());
		innerProperties.addProperty("version", deviceProperties.getVersion());
		innerProperties.addProperty("imei", deviceProperties.getImei());
		innerProperties.addProperty("model", model);

		JsonObject outerProperties = new JsonObject();
		outerProperties.add("properties", innerProperties);
		outerProperties.addProperty("email", deviceProperties.getChallenge());
		outerProperties.addProperty("osversion", "");
		outerProperties.addProperty("vendor", "Apple");
		outerProperties.addProperty("platform", model);
		outerProperties.addProperty("udid", deviceProperties.getUdid());
		outerProperties.addProperty("regid", "");

		StringRequestEntity requestEntity = new StringRequestEntity(
				outerProperties.toString(), AppConfigurations.APPLICATION_JSON,
				AppConfigurations.UTF8);

		PostMethod postMethod = new PostMethod(
				AppConfigurations
						.getConfigEntry(AppConfigurations.DEVICE_PROPERTY_POST_URL));
		postMethod.setRequestEntity(requestEntity);

		final HttpClient httpClient = new HttpClient();
		postMethod.addRequestHeader("Content-Type",
				AppConfigurations.APPLICATION_JSON);
		httpClient.executeMethod(postMethod);
		postMethod.getResponseBodyAsStream();
		postMethod.releaseConnection();

		return httpClient.executeMethod(postMethod);

	}
}
