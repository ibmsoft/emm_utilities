package com.wso2mobile.ios.mdm.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import com.wso2mobile.ios.mdm.exception.MDMException;
import com.wso2mobile.ios.mdm.util.AppConfigurations;

public class KeystoreReader {

	private KeyStore loadKeyStore() throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, MDMException {

		KeyStore keystore = KeyStore.getInstance(AppConfigurations
				.getConfigEntry(AppConfigurations.MDM_KEYSTORE));

		InputStream is = null;

		try {
			is = new FileInputStream(
					AppConfigurations
							.getConfigEntry(AppConfigurations.PATH_MDM_KEYSTORE));
			keystore.load(
					is,
					AppConfigurations.getConfigEntry(
							AppConfigurations.MDM_KEYSTORE_PASSWORD)
							.toCharArray());
		} catch (FileNotFoundException e) {
			throw new MDMException("Keystore not found");
		} catch (IOException e) {
			throw new MDMException("Error reading keystore");
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				throw new MDMException("Error closing keystore input stream");
			}
		}

		return keystore;

	}

	public Certificate getCACertificate() throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, MDMException {

		KeyStore keystore = loadKeyStore();
		Certificate caCertificate = keystore.getCertificate(AppConfigurations
				.getConfigEntry(AppConfigurations.CA_CERT_ALIAS));

		if (caCertificate == null) {
			throw new MDMException("CA certificate not found in keystore");
		}

		return caCertificate;
	}

	public PrivateKey getCAPrivateKey() throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, MDMException,
			UnrecoverableKeyException {

		KeyStore keystore = loadKeyStore();
		PrivateKey caPrivateKey = (PrivateKey) (keystore.getKey(
				AppConfigurations
						.getConfigEntry(AppConfigurations.CA_CERT_ALIAS),
				AppConfigurations.getConfigEntry(
						AppConfigurations.KEYSTORE_CA_CERT_PRIV_PASSWORD)
						.toCharArray()));

		if (caPrivateKey == null) {
			throw new MDMException("CA private key not found in keystore");
		}

		return caPrivateKey;
	}

	public Certificate getRACertificate() throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, MDMException {

		KeyStore keystore = loadKeyStore();
		Certificate raCertificate = keystore.getCertificate(AppConfigurations
				.getConfigEntry(AppConfigurations.RA_CERT_ALIAS));

		if (raCertificate == null) {
			throw new MDMException("RA certificate not found in keystore");
		}

		return raCertificate;
	}

	public PrivateKey getRAPrivateKey() throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, MDMException,
			UnrecoverableKeyException {

		KeyStore keystore = loadKeyStore();
		PrivateKey raPrivateKey = (PrivateKey) (keystore.getKey(
				AppConfigurations
						.getConfigEntry(AppConfigurations.RA_CERT_ALIAS),
				AppConfigurations.getConfigEntry(
						AppConfigurations.KEYSTORE_RA_CERT_PRIV_PASSWORD)
						.toCharArray()));

		if (raPrivateKey == null) {
			throw new MDMException("RA private key not found in keystore");
		}

		return raPrivateKey;
	}
}
