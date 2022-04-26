package com.oddlabs.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public final strictfp class KeyManager {
	private final static int KEY_SIZE = 1024;
	public final static String AGREEMENT_ALGORITHM = "DH";
	private final static String PASSWORD_ALGORITHM = "PBEWithMD5AndDES";

	public final static AlgorithmParameterSpec generateParameterSpec() throws GeneralSecurityException {
		AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance(AGREEMENT_ALGORITHM);
		paramGen.init(KEY_SIZE);
		AlgorithmParameters params = paramGen.generateParameters();
		return params.getParameterSpec(DHParameterSpec.class);
	}

	public final static KeyPair generateInitialKeyPair(AlgorithmParameterSpec param_spec) {
		try {
			KeyPairGenerator key_pair_gen = KeyPairGenerator.getInstance(AGREEMENT_ALGORITHM);
			key_pair_gen.initialize(param_spec);
			KeyPair key_pair = key_pair_gen.generateKeyPair();
			return key_pair;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public final static Cipher createCipher(int cipher_mode, KeyAgreement key_agreement, PublicKey public_key) throws InvalidKeyException, IOException, InvalidAlgorithmParameterException {
		try {
			key_agreement.doPhase(public_key, true);
			SecretKey secret_key = key_agreement.generateSecret("DESede");
			Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
			cipher.init(cipher_mode, secret_key);
			return cipher;
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public final static PrivateKey readPrivateKey(byte[] encoded_private_key, String algorithm) throws InvalidKeySpecException {
		try {
			KeyFactory key_factory = KeyFactory.getInstance(algorithm);
			KeySpec key_spec = new PKCS8EncodedKeySpec(encoded_private_key);
			return key_factory.generatePrivate(key_spec);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public final static PublicKey readPublicKey(byte[] encoded_public_key, String algorithm) throws InvalidKeySpecException {
		try {
			KeyFactory key_factory = KeyFactory.getInstance(algorithm);
			KeySpec key_spec = new X509EncodedKeySpec(encoded_public_key);
			return key_factory.generatePublic(key_spec);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public final static KeyAgreement generateAgreement(PrivateKey private_key) {
		try {
			KeyAgreement key_agreement = KeyAgreement.getInstance(AGREEMENT_ALGORITHM);
			key_agreement.init(private_key);
			return key_agreement;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public final static KeyPair generateKeyPairFromKey(PublicKey public_key) throws InvalidKeyException {
		if (!(public_key instanceof DHPublicKey))
			throw new InvalidKeyException("Not a public key");
		try {
			KeyPairGenerator key_pair_gen = KeyPairGenerator.getInstance(AGREEMENT_ALGORITHM);
			AlgorithmParameterSpec dh_param_spec = ((DHPublicKey)public_key).getParams();
			key_pair_gen.initialize(dh_param_spec);
			return key_pair_gen.generateKeyPair();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public final static Cipher createPasswordCipherFromPassword(char[] password, int mode) throws IOException, GeneralSecurityException {
		PBEKeySpec pbeKeySpec;
		PBEParameterSpec pbeParamSpec;
		SecretKeyFactory keyFac;

		// Salt
		byte[] salt = {
			(byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
			(byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
		};

		// Iteration count
		int count = 20;

		// Create PBE parameter set
		pbeParamSpec = new PBEParameterSpec(salt, count);

		// Prompt user for encryption password.
		// Collect user password as char array (using the
		// "readPasswd" method from above), and convert
		// it into a SecretKey object, using a PBE key
		// factory.
		pbeKeySpec = new PBEKeySpec(password);
		keyFac = SecretKeyFactory.getInstance(PASSWORD_ALGORITHM);
		SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

		// Create PBE Cipher
		Cipher pbeCipher = Cipher.getInstance(PASSWORD_ALGORITHM);

		// Initialize PBE Cipher with key and parameters
		pbeCipher.init(mode, pbeKey, pbeParamSpec);
		return pbeCipher;
	}

	public final static Cipher createPasswordCipher(String pass_prompt, int mode) throws IOException, GeneralSecurityException {
		return createPasswordCipherFromPassword(readPassword(pass_prompt, System.in), mode);
	}

	/**
	 * Reads user password from given input stream.
	 */
	public final static char[] readPassword(String pass_prompt, InputStream in) throws IOException {
		char[] lineBuffer;
		char[] buf;
		buf = lineBuffer = new char[128];

		int room = buf.length;
		int offset = 0;
		int c;

		System.out.print(pass_prompt);
		System.out.flush();
		loop:   while (true) {
			switch (c = in.read()) {
				case -1: 
				case '\n':
					break loop;

				case '\r':
					int c2 = in.read();
					if ((c2 != '\n') && (c2 != -1)) {
						if (!(in instanceof PushbackInputStream)) {
							in = new PushbackInputStream(in);
						}
						((PushbackInputStream)in).unread(c2);
					} else 
						break loop;

				default:
					if (--room < 0) {
						buf = new char[offset + 128];
						room = buf.length - offset - 1;
						System.arraycopy(lineBuffer, 0, buf, 0, offset);
						Arrays.fill(lineBuffer, ' ');
						lineBuffer = buf;
					}
					buf[offset++] = (char) c;
					break;
			}
		}

		if (offset == 0) {
			return null;
		}

		char[] ret = new char[offset];
		System.arraycopy(buf, 0, ret, 0, offset);
		Arrays.fill(buf, ' ');
		return ret;
	}
}
