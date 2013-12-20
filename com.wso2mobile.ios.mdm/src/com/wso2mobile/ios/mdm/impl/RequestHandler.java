package com.wso2mobile.ios.mdm.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cms.CMSAbsentContent;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;
import org.xml.sax.SAXException;

import com.wso2mobile.ios.mdm.exception.MDMException;
import com.wso2mobile.ios.mdm.plist.DeviceProperties;
import com.wso2mobile.ios.mdm.plist.PlistExtractor;
import com.wso2mobile.ios.mdm.plist.PlistGenerator;
import com.wso2mobile.ios.mdm.publisher.TokenPublisher;
import com.wso2mobile.ios.mdm.util.AppConfigurations;

public class RequestHandler {

	public byte[] handleProfileRequest(InputStream inputStream)
			throws NoSuchAlgorithmException, InvalidKeySpecException,
			IOException, ClassNotFoundException, UnrecoverableKeyException,
			KeyStoreException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException, MDMException,
			ParserConfigurationException, SAXException {

		try {
			CMSSignedData cms = new CMSSignedData(inputStream);
			Store store = cms.getCertificates();
			SignerInformationStore signers = cms.getSignerInfos();
			Collection<?> collectionSigners = signers.getSigners();

			Iterator<?> iterator = collectionSigners.iterator();
			while (iterator.hasNext()) {
				SignerInformation signer = (SignerInformation) iterator.next();
				Collection<?> certCollection = store
						.getMatches(signer.getSID());
				Iterator<?> certIt = certCollection.iterator();
				X509CertificateHolder certHolder = (X509CertificateHolder) certIt
						.next();
				X509Certificate cert = new JcaX509CertificateConverter()
						.setProvider(AppConfigurations.PROVIDER)
						.getCertificate(certHolder);

				// verifies the signer
				if (signer.verify(new JcaSimpleSignerInfoVerifierBuilder()
						.setProvider(AppConfigurations.PROVIDER).build(cert))) {

					KeystoreReader keystoreReader = new KeystoreReader();
					X509Certificate caCertificate = (X509Certificate) keystoreReader
							.getCACertificate();

					JcaX509CertificateHolder jcaX509CertificateHolder = new JcaX509CertificateHolder(
							caCertificate);
					X500Name x500name = jcaX509CertificateHolder.getSubject();

					PlistGenerator plistGenerator = new PlistGenerator();
					PKCSSigner pkcsSigner = new PKCSSigner();
					KeyStore keyStore = pkcsSigner.loadKeyStore();
					CMSSignedDataGenerator signedDataGenerator = pkcsSigner
							.setUpProvider(keyStore);
					byte[] signedData = null;

					// extract content
					CMSProcessable cmsProcessable = cms.getSignedContent();
					String contentString = new String(
							(byte[]) cmsProcessable.getContent(),
							AppConfigurations.UTF8);
					// System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					// System.out.println(contentString);

					PlistExtractor plistExtractor = new PlistExtractor();
					DeviceProperties deviceProperties = plistExtractor
							.extractDeviceProperties(contentString);

					if (signer.getSID().getIssuer().equals(x500name)) {

						/*
						 * By this point, any previously fully enrolled clients
						 * have been redirected to the enrollment page to enroll
						 * again. Implement that.
						 */

						// System.out
						// .println("PROFILE EXECUTION 1 >>>>>>>>>>>>>>>>>>");

						// for now send the udid
						String mdmConfigurationPayload = plistGenerator
								.generateMDMConfigurationPayload(deviceProperties
										.getUdid());

						CMSTypedData msg = new CMSProcessableByteArray(
								mdmConfigurationPayload
										.getBytes(AppConfigurations.UTF8));
						CMSEnvelopedDataGenerator edGen = new CMSEnvelopedDataGenerator();
						edGen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(
								cert).setProvider(AppConfigurations.PROVIDER));
						CMSEnvelopedData envelopedData = edGen.generate(
								msg,
								new JceCMSContentEncryptorBuilder(
										CMSAlgorithm.DES_EDE3_CBC).setProvider(
										AppConfigurations.PROVIDER).build());

						// CommonUtil commonUtil = new CommonUtil();
						// String pkcs7EnvelopedData = new String(
						// commonUtil.convertDERtoPEM(
						// envelopedData.getEncoded(), "pkcs7"),
						// "UTF-8");

						String encryptedConfigurationPayload = plistGenerator
								.generateEncryptedConfigurationPayload(new String(
										Base64.encode(envelopedData
												.getEncoded()),
										AppConfigurations.UTF8));

						signedData = pkcsSigner.signPkcs(
								encryptedConfigurationPayload.getBytes(),
								signedDataGenerator);

					} else {

						// System.out
						// .println("PROFILE EXECUTION 2 >>>>>>>>>>>>>>>>>>");

						String encryptionCertificatePayload = plistGenerator
								.generateEncryptionCertificatePayload(deviceProperties
										.getChallenge());
						signedData = pkcsSigner.signPkcs(
								encryptionCertificatePayload.getBytes(),
								signedDataGenerator);

						TokenPublisher tokenPublisher = new TokenPublisher();
						int result = tokenPublisher
								.publishiOSTokens(deviceProperties);

						// IMEI, UDID, version etc. returned here. Extract those
						// information. Later stage implement a listener for
						// this.
						// System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>> "
						// + result);

					}

					return signedData;
				}
			}

		} catch (OperatorCreationException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (CMSException e) {
			e.printStackTrace();
		}

		return null;
	}

	public SCEPResponse handleGetCACert() throws MDMException {

		SCEPResponse scepResponse = new SCEPResponse();

		try {
			KeystoreReader keystoreReader = new KeystoreReader();

			byte[] caBytes = keystoreReader.getCACertificate().getEncoded();
			byte[] raBytes = keystoreReader.getRACertificate().getEncoded();

			CertificateGenerator caImpl = new CertificateGenerator();
			final List<X509Certificate> certs = caImpl.getCA_RACertificates(
					caBytes, raBytes);

			byte[] bytes = null;

			if (certs.size() == 0) {
				scepResponse.setResultCriteria(CAStatus.CA_CERT_FAILED);
				bytes = new byte[0];
			} else if (certs.size() == 1) {
				scepResponse.setResultCriteria(CAStatus.CA_CERT_RECEIVED);

				try {
					bytes = certs.get(0).getEncoded();
				} catch (CertificateEncodingException e) {
					e.printStackTrace();
				}

			} else {
				scepResponse.setResultCriteria(CAStatus.CA_RA_CERT_RECEIVED);

				CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
				JcaCertStore store = null;

				store = new JcaCertStore(certs);
				generator.addCertificates(store);
				CMSSignedData degenerateSd = generator
						.generate(new CMSAbsentContent());
				bytes = degenerateSd.getEncoded();

			}

			scepResponse.setEncodedResponse(bytes);
		} catch (Exception e) {
			throw new MDMException(
					"Error occurred when retrieving CA certificate");
		}

		return scepResponse;
	}

	public byte[] handleGetCACaps() {
		return AppConfigurations.POST_BODY_CA_CAPS.getBytes();
	}

}
