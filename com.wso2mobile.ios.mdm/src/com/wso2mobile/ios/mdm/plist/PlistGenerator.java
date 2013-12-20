package com.wso2mobile.ios.mdm.plist;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.wso2mobile.ios.mdm.exception.MDMException;
import com.wso2mobile.ios.mdm.util.AppConfigurations;

public class PlistGenerator {

	public Map<String, Object> generateGeneralMap() {
		Map<String, Object> generalMap = new HashMap<String, Object>();
		// do not modify the payload version
		generalMap.put("PayloadVersion", 1);
		generalMap.put("PayloadUUID", UUID.randomUUID().toString());
		generalMap.put("PayloadOrganization", "WSO2Mobile");

		return generalMap;
	}

	public String generateMobileConfigurations(String challengeToken)
			throws IllegalArgumentException, UnsupportedEncodingException,
			IllegalAccessException, InvocationTargetException, MDMException {
		PropertyList plist = new PropertyList();

		Map<String, Object> itemMap = generateGeneralMap();

		// do not modify the payload type
		itemMap.put("PayloadType", "Profile Service");
		itemMap.put("PayloadIdentifier",
				"com.WSO2.mobileconfig.profile-service");
		itemMap.put("PayloadDisplayName", "WSO2Mobile Profile Service");
		itemMap.put("PayloadDescription",
				"Install this profile to enroll for secure access to WSO2Mobile Inc.");

		Map<String, Object> innnerMap = new HashMap<String, Object>();
		innnerMap
				.put("URL", AppConfigurations
						.getConfigEntry(AppConfigurations.PROFILE_URL));

		ArrayList<String> deviceAttributes = new ArrayList<String>();
		deviceAttributes.add("UDID");
		deviceAttributes.add("VERSION");
		deviceAttributes.add("SERIAL");
		deviceAttributes.add("PRODUCT");
		deviceAttributes.add("MAC_ADDRESS_EN0");
		deviceAttributes.add("DEVICE_NAME");
		deviceAttributes.add("IMEI");
		deviceAttributes.add("ICCID");
		innnerMap.put("DeviceAttributes", deviceAttributes);
		innnerMap.put("Challenge", challengeToken);

		itemMap.put("PayloadContent", innnerMap);
		String result = plist.encode(itemMap);
		// System.out.println(result);

		return result;
	}

	public String generateMDMConfigurationPayload(String challenge)
			throws IllegalArgumentException, UnsupportedEncodingException,
			IllegalAccessException, InvocationTargetException, MDMException {
		PropertyList plist = new PropertyList();

		Map<String, Object> itemMap = generateGeneralMap();
		itemMap.put("PayloadIdentifier", "com.wso2.mdm");
		itemMap.put("PayloadType", "com.apple.mdm");
		itemMap.put("PayloadDisplayName", "MDM");
		itemMap.put("PayloadDescription", "Configures MDM");
		itemMap.put("Topic",
				AppConfigurations.getConfigEntry(AppConfigurations.TOPIC_ID));
		itemMap.put("CheckInURL",
				AppConfigurations.getConfigEntry(AppConfigurations.CHECKIN_URL));
		itemMap.put("CheckOutWhenRemoved", true);
		itemMap.put("ServerURL",
				AppConfigurations.getConfigEntry(AppConfigurations.SERVER_URL));
		itemMap.put("IsRemovable", false);
		itemMap.put("Username", "");
		itemMap.put("Password", "");
		itemMap.put("PayloadRemovalDisallowed", false);
		itemMap.put("IdentityCertificateUUID",
				"45ff43d7-f0ac-40b6-94e1-ba86466b4ad3");
		itemMap.put("AccessRights", 8191);

		ArrayList<Object> compositePayload = new ArrayList<Object>();
		compositePayload.add(generateSCEPPayload(challenge, "mdm"));
		compositePayload.add(itemMap);

		String result = plist.encode(compositePayload);
		// System.out.println(result);

		return result;
	}

	public String generateEncryptedConfigurationPayload(String encryptedContent)
			throws IllegalArgumentException, UnsupportedEncodingException,
			IllegalAccessException, InvocationTargetException, MDMException {
		PropertyList plist = new PropertyList();
		// format should be 2013-09-28T00:00:00Z
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'00:00:00'Z'");
		plist.setDateFormat(dateFormat);

		Date targetDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(targetDate);
		calendar.add(Calendar.DATE, 1);

		Map<String, Object> itemMap = generateGeneralMap();
		itemMap.put("PayloadIdentifier", "com.wso2.intranet");
		itemMap.put("PayloadType", "Configuration");
		itemMap.put("PayloadDisplayName", "WSO2Mobile MDM");
		itemMap.put("PayloadDescription", "Access to the WSO2Mobile MDM");
		itemMap.put("PayloadExpirationDate", calendar.getTime());
		itemMap.put("EncryptedPayloadContent", encryptedContent.getBytes());

		String result = plist.encode(itemMap);
		// System.out.println(result);

		return result;
	}

	public String generateEncryptionCertificatePayload(String challenge)
			throws IllegalArgumentException, UnsupportedEncodingException,
			IllegalAccessException, InvocationTargetException, MDMException {
		PropertyList plist = new PropertyList();

		Map<String, Object> itemMap = generateGeneralMap();
		itemMap.put("PayloadIdentifier", "com.wso2.encrypted-profile-service");
		// do not modify
		itemMap.put("PayloadType", "Configuration");
		itemMap.put("PayloadDisplayName", "WSO2Mobile Profile Service Enroll");
		itemMap.put("PayloadDescription",
				"Enrolls identity for the encrypted profile service");

		ArrayList<Object> scepPayload = new ArrayList<Object>();
		scepPayload.add(generateSCEPPayload(challenge, "Profile Service"));
		itemMap.put("PayloadContent", scepPayload);

		String result = plist.encode(itemMap);
		// System.out.println(result);

		return result;
	}

	public Map<String, Object> generateSCEPPayload(String challengeToken,
			String purpose) throws MDMException {

		Map<String, Object> itemMap = new HashMap<String, Object>();
		itemMap.put("PayloadUUID", "45ff43d7-f0ac-40b6-94e1-ba86466b4ad3");
		itemMap.put("PayloadIdentifier",
				"com.WSO2Mobile.encryption-cert-request");
		itemMap.put("PayloadType", "com.apple.security.scep");
		itemMap.put("PayloadVersion", 1);
		itemMap.put("PayloadDisplayName", "WSO2Mobile Profile Service");
		itemMap.put("PayloadDescription", "Provides device encryption identity");
		itemMap.put("PayloadOrganization", "WSO2Mobile");

		Map<String, Object> innnerMap = new HashMap<String, Object>();
		innnerMap.put("URL",
				AppConfigurations.getConfigEntry(AppConfigurations.ENROLL_URL));
		// MS SCEP servers need Name property
		innnerMap.put("Name", "EnrollmentCAInstance");

		ArrayList<Object> outerList = new ArrayList<Object>();

		ArrayList<Object> outerAttribute1 = new ArrayList<Object>();
		ArrayList<String> attribute1 = new ArrayList<String>();
		attribute1.add("C");
		attribute1.add("LK");
		outerAttribute1.add(attribute1);

		ArrayList<Object> outerAttribute2 = new ArrayList<Object>();
		ArrayList<String> attribute2 = new ArrayList<String>();
		attribute2.add("L");
		attribute2.add("Colombo");
		outerAttribute2.add(attribute2);

		ArrayList<Object> outerAttribute3 = new ArrayList<Object>();
		ArrayList<String> attribute3 = new ArrayList<String>();
		attribute3.add("ST");
		attribute3.add("Western");
		outerAttribute3.add(attribute3);

		ArrayList<Object> outerAttribute4 = new ArrayList<Object>();
		ArrayList<String> attribute4 = new ArrayList<String>();
		attribute4.add("O");
		attribute4.add("WSO2");
		outerAttribute4.add(attribute4);

		ArrayList<Object> outerAttribute5 = new ArrayList<Object>();
		ArrayList<String> attribute5 = new ArrayList<String>();
		attribute5.add("OU");
		attribute5.add("Mobile");
		outerAttribute5.add(attribute5);

		ArrayList<Object> outerAttribute6 = new ArrayList<Object>();
		ArrayList<String> attribute6 = new ArrayList<String>();
		attribute6.add("CN");
		attribute6.add(String.format("%s (%s)", purpose, UUID.randomUUID()
				.toString()));
		outerAttribute6.add(attribute6);

		outerList.add(outerAttribute1);
		outerList.add(outerAttribute2);
		outerList.add(outerAttribute3);
		outerList.add(outerAttribute4);
		outerList.add(outerAttribute5);
		outerList.add(outerAttribute6);

		innnerMap.put("Subject", outerList);

		innnerMap.put("Challenge", challengeToken);
		innnerMap.put("Keysize", 1024);
		innnerMap.put("Key Type", "RSA");
		innnerMap.put("Key Usage", 5);
		// innnerMap.put("CAFingerprint", "");

		itemMap.put("PayloadContent", innnerMap);

		return itemMap;
	}

	public static void main(String[] args) throws Exception {

		PlistGenerator plistGenerator = new PlistGenerator();
		// plistGenerator.generateMobileConfigurations("test");

		PropertyList plist = new PropertyList();
		String result = plist.encode(plistGenerator.generateSCEPPayload("test",
				"test"));
		// System.out.println(result);

	}
}
