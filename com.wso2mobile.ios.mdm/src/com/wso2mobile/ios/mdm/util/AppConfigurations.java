package com.wso2mobile.ios.mdm.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.wso2mobile.ios.mdm.exception.MDMException;

public class AppConfigurations {

	public static final String PROFILE_URL = "ios-profile-url";
	public static final String CHECKIN_URL = "ios-checkin-url";
	public static final String SERVER_URL = "ios-server-url";
	public static final String ENROLL_URL = "ios-enroll-url";
	public static final String TOPIC_ID = "ios-mdm-topic-id";
	public static final String PATH_KEYSTORE = "keystore-location";
	public static final String PATH_MDM_KEYSTORE = "mdm-keystore-location";
	public static final String KEY_ALIAS_KEYSTORE = "keystore-key-alias";
	public static final String KEYSTORE_PASSWORD = "keystore-password";
	public static final String MDM_KEYSTORE_PASSWORD = "mdm-keystore-password";
	public static final String KEYSTORE_KEY_PASSWORD = "keystore-key-password";
	public static final String KEYSTORE_CA_CERT_PRIV_PASSWORD = "mdm-keystore-ca-cert-priv-password";
	public static final String KEYSTORE_RA_CERT_PRIV_PASSWORD = "mdm-keystore-ra-cert-priv-password";
	public static final String CA_CERT_ALIAS = "mdm-keystore-ca-cert-alias";
	public static final String RA_CERT_ALIAS = "mdm-keystore-ra-cert-alias";
	public static final String APNS_PUSH_CERT_PATH = "ios-apns-cert-path";
	public static final String APNS_PUSH_CERT_PASSWORD = "ios-apns-cert-password";
	public static final String SIGNATUREALGO = "SHA1withRSA";
	public static final String PROVIDER = "BC";
	public static final String KEYSTORE = "keystore-type";
	public static final String MDM_KEYSTORE = "mdm-keystore-type";
	public static final String RSA = "RSA";
	public static final String UTF8 = "UTF-8";
	public static final String X_APPLE_ASPEN_CONFIG = "application/x-apple-aspen-config";
	public static final String X509_CA_CERT = "application/x-x509-ca-cert";
	public static final String TEXT_PLAIN = "text/plain";
	public static final String APPLICATION_JSON = "application/json";
	public static final String X509_CA_RA_CERT = "application/x-x509-ca-ra-cert";
	public static final String SHA256_RSA = "SHA256WithRSAEncryption";
	public static final String X_509 = "X.509";
	public static final String POST_BODY_CA_CAPS = "POSTPKIOperation/nSHA-1/nDES3/n";
	public static final String DES_EDE = "DESede";
	public static final String PKI_MESSAGE = "application/x-pki-message";
	public static final String DEVICE_TOKEN = "devicetoken";
	public static final String I_PAD = "iPad";
	public static final String I_POD = "iPod";
	public static final String I_PHONE = "iPhone";
	public static final String DEVICE_PROPERTY_POST_URL = "ios-device-property-post-url";
	public static final String CONF_LOCATION = "conf.location";
	public static final String MDM_CONFIG_XML = "mdm-config.xml";
	public static final int RSA_KEY_LENGTH = 1024;

	private static AppConfigurations appConfigurations;
	private static String[] configEntryNames = { ENROLL_URL, PROFILE_URL,
			CHECKIN_URL, SERVER_URL, PATH_KEYSTORE, KEY_ALIAS_KEYSTORE,
			KEYSTORE_PASSWORD, KEYSTORE_KEY_PASSWORD, KEYSTORE, TOPIC_ID,
			DEVICE_PROPERTY_POST_URL, CA_CERT_ALIAS, RA_CERT_ALIAS,
			MDM_KEYSTORE, PATH_MDM_KEYSTORE, MDM_KEYSTORE_PASSWORD,
			KEYSTORE_CA_CERT_PRIV_PASSWORD, KEYSTORE_RA_CERT_PRIV_PASSWORD,
			APNS_PUSH_CERT_PATH, APNS_PUSH_CERT_PASSWORD };

	private static Map<String, String> configMap;

	private static Map<String, String> readMDMConfigurationXML()
			throws MDMException {

		String confLocation = System.getProperty(CONF_LOCATION)
				+ File.separator + MDM_CONFIG_XML;

		// System.out.println("Configuration location " + confLocation);

		if (appConfigurations == null || configMap == null) {
			appConfigurations = new AppConfigurations();
			configMap = new HashMap<String, String>();

			// read xml entries from repository/conf/mdm-config.xml

			Document document = null;
			try {
				File fXmlFile = new File(confLocation);
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder documentBuilder = documentBuilderFactory
						.newDocumentBuilder();
				document = documentBuilder.parse(fXmlFile);
			} catch (Exception e) {
				throw new MDMException("Error reading mdm-config.xml file");
			}

			for (String configEntry : configEntryNames) {

				NodeList elements = document.getElementsByTagName(configEntry);

				if (elements != null && elements.getLength() > 0) {
					configMap.put(configEntry, elements.item(0)
							.getTextContent());
				}

				// System.out.println("KEY : " + configEntry + " // VALUE : "
				// + elements.item(0).getTextContent());
			}
		}

		return configMap;
	}

	public static String getConfigEntry(final String entry) throws MDMException {

		Map<String, String> configurationMap = readMDMConfigurationXML();
		String configValue = configurationMap.get(entry);

		if (configValue == null) {
			throw new MDMException("Configuration entry not available");
		}

		return configValue.trim();
	}
}
