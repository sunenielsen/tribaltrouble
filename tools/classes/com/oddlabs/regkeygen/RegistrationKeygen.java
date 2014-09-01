package com.oddlabs.regkeygen;

import java.security.SecureRandom;
import java.security.PrivateKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.GeneralSecurityException;
import java.security.Signature;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import com.oddlabs.util.KeyManager;
import com.oddlabs.registration.RegServiceInterface;
import com.oddlabs.util.PasswordKey;

public final strictfp class RegistrationKeygen {
	private final static int KEY_SIZE = 2048;

	private final static KeyPair generateKeyPair() throws GeneralSecurityException {
		KeyPairGenerator keygen = KeyPairGenerator.getInstance(RegServiceInterface.KEY_ALGORITHM);
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		keygen.initialize(KEY_SIZE, random);
		return keygen.generateKeyPair();
	}
	
	public final static void main(String[] args) {
		PrivateKey private_key;
		File private_key_file = new File(args[0] + File.separator + RegServiceInterface.PRIVATE_KEY_FILE);
		File public_key_file = new File(args[0] + File.separator + RegServiceInterface.PUBLIC_KEY_FILE);
		try {
			Cipher password_cipher = KeyManager.createPasswordCipherFromPassword(args[1].toCharArray(), Cipher.ENCRYPT_MODE);
			if (args[2].equals("-generate")) {
				System.out.println("Generating a new key pair for registration keys");
				KeyPair key_pair = generateKeyPair();
				ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(public_key_file));
				System.out.println("Writing public key to " + public_key_file);
				os.writeObject(key_pair.getPublic().getEncoded());
				private_key = key_pair.getPrivate();
			} else {
				System.out.println("Changing password for private registration key");
				Cipher decrypt_cipher = KeyManager.createPasswordCipherFromPassword(args[2].toCharArray(), Cipher.DECRYPT_MODE);
				private_key = PasswordKey.readPrivateKey(decrypt_cipher, RegServiceInterface.PRIVATE_KEY_FILE, RegServiceInterface.KEY_ALGORITHM);
			}
			byte[] encoded_private_key = private_key.getEncoded();
			SealedObject sealed_key = new SealedObject(encoded_private_key, password_cipher);
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(private_key_file));
			System.out.println("Writing private key to " + private_key_file);
			os.writeObject(sealed_key);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
