/*
 * Copyright 2012 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.evo.jetty.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class Crypto {
	public static void createSelfSignedCertificate(File keystore, String alias,
			String keystorePassword) {
		try {
			Provider bouncyCastleProvider = new BouncyCastleProvider();
			
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", bouncyCastleProvider);
			keyPairGenerator.initialize(1024, new SecureRandom());
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			
			// Generate self-signed certificate
			X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
			nameBuilder.addRDN(BCStyle.CN, "test");
			X500Name name = nameBuilder.build();
			
			Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
			Date notAfter = new Date(System.currentTimeMillis() + 10 * 365 * 24 * 60 * 60 * 1000);
			BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
			
			X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(name,
					serial, notBefore, notAfter, name, keyPair.getPublic());
			ContentSigner sigGen = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
					.setProvider(bouncyCastleProvider).build(keyPair.getPrivate());
			X509Certificate cert = new JcaX509CertificateConverter().setProvider(bouncyCastleProvider)
					.getCertificate(certGen.build(sigGen));
			cert.checkValidity(new Date());
			cert.verify(cert.getPublicKey());
			
			// Save to keystore
			KeyStore store = KeyStore.getInstance("JKS");
			if (keystore.exists()) {
				FileInputStream fis = new FileInputStream(keystore);
				store.load(fis, keystorePassword.toCharArray());
				fis.close();
			} else {
				store.load(null);
			}
			store.setKeyEntry(alias, keyPair.getPrivate(), keystorePassword.toCharArray(),
					new Certificate[] { cert });
			FileOutputStream fos = new FileOutputStream(keystore);
			store.store(fos, keystorePassword.toCharArray());
			fos.close();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (OperatorCreationException e) {
			throw new RuntimeException(e);
		} catch (CertificateException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		} catch (SignatureException e) {
			throw new RuntimeException(e);
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
