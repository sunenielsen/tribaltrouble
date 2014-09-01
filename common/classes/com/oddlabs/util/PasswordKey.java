package com.oddlabs.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;

public final strictfp class PasswordKey {
	public final static PrivateKey readPrivateKey(Cipher decrypt_cipher, String key_file, String algorithm) throws IOException, ClassNotFoundException, GeneralSecurityException {
		URL key_url = PasswordKey.class.getResource("/" + key_file);
		ObjectInputStream is = new ObjectInputStream(key_url.openStream());
		SealedObject sealed_key = (SealedObject)is.readObject();
		byte[] encoded_registration_key = (byte[])sealed_key.getObject(decrypt_cipher);
		return KeyManager.readPrivateKey(encoded_registration_key, algorithm);
	}
}
