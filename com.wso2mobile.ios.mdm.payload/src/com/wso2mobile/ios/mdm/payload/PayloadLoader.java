package com.wso2mobile.ios.mdm.payload;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.wso2mobile.ios.mdm.exception.MDMException;
import com.wso2mobile.ios.mdm.impl.PKCSSigner;

public class PayloadLoader {

	public static final String UTF_8 = "UTF-8";
	public static final String PATH_PREFIX = "resources/";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String TRUE_TAG = "<true />";
	public static final String FALSE_TAG = "<false />";

	public String loadPayload(PayloadType payloadType,
			Map<String, Object> paramMap, boolean isProfile)
			throws MDMException {

		String plistFile = payloadType.toString();
		InputStream inputStream = PayloadLoader.class
				.getResourceAsStream(PATH_PREFIX + plistFile);
		StringWriter writer = new StringWriter();

		if (inputStream == null) {
			throw new MDMException("Error accessing file " + plistFile);
		}

		try {
			IOUtils.copy(inputStream, writer, UTF_8);
		} catch (IOException e) {
			throw new MDMException("Error accessing file " + plistFile);
		}
		String outputString = writer.toString();

		outputString = substituteRuntimeParameters(outputString, paramMap);

		System.out.println(outputString);

		if (isProfile) {
			InputStream inputStreamCommon = PayloadLoader.class
					.getResourceAsStream(PATH_PREFIX
							+ PayloadType.COMMON_PROFILE);
			StringWriter writerCommon = new StringWriter();

			if (inputStreamCommon == null) {
				throw new MDMException("Error accessing file "
						+ PayloadType.COMMON_PROFILE);
			}

			try {
				IOUtils.copy(inputStreamCommon, writerCommon, UTF_8);
			} catch (IOException e) {
				throw new MDMException("Error accessing file "
						+ PayloadType.COMMON_PROFILE);
			}
			String outputStringCommon = writerCommon.toString();
			outputStringCommon = substituteRuntimeParameters(
					outputStringCommon, paramMap);

			PKCSSigner pkcsSigner = new PKCSSigner();
			byte signedData[];
			try {
				signedData = pkcsSigner.getSignedData(outputString
						.getBytes(UTF_8));
			} catch (UnsupportedEncodingException e) {
				throw new MDMException("Unsupported encoding UTF-8");
			}

			Map<String, Object> placeHolderItems = new HashMap<String, Object>();
			placeHolderItems.put("placeholder",
					Base64.encodeBase64String(signedData));
			outputString = substituteRuntimeParameters(outputStringCommon,
					placeHolderItems);
		}

		try {
			inputStream.close();
		} catch (IOException e) {
			throw new MDMException(
					"Error closing input stream when reading file "
							+ PayloadType.COMMON_PROFILE);
		}

		return outputString;
	}

	public String substituteRuntimeParameters(String outputString,
			Map<String, Object> paramMap) {

		Iterator<String> iterator = paramMap.keySet().iterator();

		while (iterator.hasNext()) {
			String key = iterator.next();
			Object value = paramMap.get(key);

			String strValue = "";

			if (value instanceof Boolean) {
				if (((Boolean) value).booleanValue() == true) {
					strValue = TRUE_TAG;
				} else if (((Boolean) value).booleanValue() == false) {
					strValue = FALSE_TAG;
				}
			} else if (value instanceof String) {
				strValue = (String) value;
			} else if (value instanceof Integer) {
				strValue = ((Integer) value).toString();
			} else if (value instanceof Double) {
				strValue = ((Double) value).toString();
			} else {
				strValue = (String) value;
			}

			outputString = outputString.replace(String.format("${%s}", key),
					strValue);
		}

		return outputString;
	}

	public static void main(String[] args) throws IOException, MDMException {
		PayloadLoader payloadLoader = new PayloadLoader();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		String responseData = payloadLoader.loadPayload(
				PayloadType.PROFILE_LIST, paramMap, false);

	}
}
