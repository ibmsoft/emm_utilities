package com.wso2mobile.ios.mdm.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.bouncycastle.util.encoders.Base64;

public class CommonUtil {

	public byte[] convertDERtoPEM(byte[] bytes, String headfoot) {

		ByteArrayOutputStream pemStream = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(pemStream);

		byte[] stringBytes = Base64.encode(bytes);

		String encoded = new String(stringBytes);

		if (headfoot != null) {
			writer.print("-----BEGIN" + headfoot + "-----\n");
		}

		// write 64 chars per line till done
		int i = 0;
		while ((i + 1) * 64 < encoded.length()) {
			writer.print(encoded.substring(i * 64, (i + 1) * 64));
			writer.print("\n");
			i++;
		}
		if (encoded.length() % 64 != 0) {
			writer.print(encoded.substring(i * 64)); // write remainder
			writer.print("\n");
		}
		if (headfoot != null) {
			writer.print("-----END" + headfoot + "-----\n");
		}

		writer.flush();

		return pemStream.toByteArray();
	}
}
