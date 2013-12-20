package com.wso2mobile.ios.mdm.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import com.wso2mobile.ios.mdm.exception.MDMException;
import com.wso2mobile.ios.mdm.util.AppConfigurations;

public class PKCSSigner {

	protected KeyStore loadKeyStore() throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, MDMException {

		KeyStore keystore = KeyStore.getInstance(AppConfigurations
				.getConfigEntry(AppConfigurations.KEYSTORE));
		InputStream is = null;
		try {
			is = new FileInputStream(
					AppConfigurations
							.getConfigEntry(AppConfigurations.PATH_KEYSTORE));
			keystore.load(
					is,
					AppConfigurations.getConfigEntry(
							AppConfigurations.KEYSTORE_PASSWORD).toCharArray());
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

	public CMSSignedDataGenerator setUpProvider(final KeyStore keystore)
			throws KeyStoreException, CertificateEncodingException,
			UnrecoverableKeyException, OperatorCreationException,
			NoSuchAlgorithmException, CMSException, MDMException {

		Security.addProvider(new BouncyCastleProvider());

		Certificate[] certchain = (Certificate[]) keystore
				.getCertificateChain(AppConfigurations
						.getConfigEntry(AppConfigurations.KEY_ALIAS_KEYSTORE));

		final List<Certificate> certlist = new ArrayList<Certificate>();

		for (int i = 0, length = certchain == null ? 0 : certchain.length; i < length; i++) {
			certlist.add(certchain[i]);
		}

		Store certstore = new JcaCertStore(certlist);

		Certificate cert = keystore.getCertificate(AppConfigurations
				.getConfigEntry(AppConfigurations.KEY_ALIAS_KEYSTORE));

		ContentSigner signer = new JcaContentSignerBuilder(
				AppConfigurations.SIGNATUREALGO)
				.setProvider(AppConfigurations.PROVIDER)
				.build((PrivateKey) (keystore.getKey(
						AppConfigurations
								.getConfigEntry(AppConfigurations.KEY_ALIAS_KEYSTORE),
						AppConfigurations.getConfigEntry(
								AppConfigurations.KEYSTORE_KEY_PASSWORD)
								.toCharArray())));

		CMSSignedDataGenerator generator = new CMSSignedDataGenerator();

		generator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
				new JcaDigestCalculatorProviderBuilder().setProvider(
						AppConfigurations.PROVIDER).build()).build(signer,
				(X509Certificate) cert));

		generator.addCertificates(certstore);

		return generator;
	}

	public byte[] signPkcs(final byte[] content,
			final CMSSignedDataGenerator generator) throws CMSException,
			IOException {

		CMSTypedData cmsdata = new CMSProcessableByteArray(content);
		CMSSignedData signeddata = generator.generate(cmsdata, true);
		return signeddata.getEncoded();
	}

	public byte[] getSignedData(byte[] input) throws MDMException {

		byte[] signedData;
		try {
			KeyStore keyStore = loadKeyStore();
			CMSSignedDataGenerator signedDataGenerator = setUpProvider(keyStore);
			signedData = signPkcs(input, signedDataGenerator);
		} catch (Exception e) {
			throw new MDMException("Error when signing data");
		}

		return signedData;
	}

}
