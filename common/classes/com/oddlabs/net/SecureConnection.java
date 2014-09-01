package com.oddlabs.net;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.SealedObject;

import com.oddlabs.util.KeyManager;
import com.oddlabs.event.Deterministic;

public final strictfp class SecureConnection extends AbstractConnection implements SecureConnectionInterface {
	private final ARMIInterfaceMethods interface_methods = new ARMIInterfaceMethods(SecureConnectionInterface.class);
	private final Deterministic deterministic;
	private final AbstractConnection wrapped_connection;
	private final SecureConnectionInterface secure_interface;
	private final KeyAgreement key_agreement;
	private final List event_backlog = new ArrayList();
	private Cipher decrypt_cipher;
	private Cipher encrypt_cipher;

	public SecureConnection(Deterministic deterministic, AbstractConnection wrapped_conn, AlgorithmParameterSpec param_spec) {
		this.deterministic = deterministic;
		setConnectionInterface(wrapped_conn.getConnectionInterface());
		this.wrapped_connection = wrapped_conn;
		wrapped_connection.setConnectionInterface(new ConnectionInterface() {
			public void error(AbstractConnection conn, IOException e) {
				notifyError(e);
			}
			public void connected(AbstractConnection conn) {
			}
			public final void handle(Object sender, ARMIEvent event) {
				processEvent(event);
			}
			public final void writeBufferDrained(AbstractConnection conn) {
				SecureConnection.this.writeBufferDrained();
			}
		});
		this.secure_interface = (SecureConnectionInterface)ARMIEvent.createProxy(wrapped_connection, SecureConnectionInterface.class);
		if (param_spec != null) {
			KeyPair key_pair = KeyManager.generateInitialKeyPair(param_spec);
			this.key_agreement = KeyManager.generateAgreement(key_pair.getPrivate());
			secure_interface.initAgreement(key_pair.getPublic().getEncoded());
		} else
			this.key_agreement = null;
	}

	public final AbstractConnection getWrappedConnection() {
		return wrapped_connection;
	}

	private final SealedObject encrypt(ARMIEvent event) {
		try {
			return new SealedObject(event, encrypt_cipher);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public final void initAgreement(byte[] public_key_encoded) {
		if (isConnected())
			return;
		try {
			KeyAgreement key_agreement = this.key_agreement;
			PublicKey public_key = KeyManager.readPublicKey(public_key_encoded, KeyManager.AGREEMENT_ALGORITHM);
			if (key_agreement == null) {
				KeyPair key_pair = (KeyPair)deterministic.log(KeyManager.generateKeyPairFromKey(public_key));
				key_agreement = KeyManager.generateAgreement(key_pair.getPrivate());
				secure_interface.initAgreement(key_pair.getPublic().getEncoded());
			}
			decrypt_cipher = KeyManager.createCipher(Cipher.DECRYPT_MODE, key_agreement, public_key);
			encrypt_cipher = KeyManager.createCipher(Cipher.ENCRYPT_MODE, key_agreement, public_key);
			notifyConnected();
			for (int i = 0; i < event_backlog.size(); i++) {
				ARMIEvent event = (ARMIEvent)event_backlog.get(i);
				tunnel(event);
			}
		} catch (IOException e) {
			notifyError(e);
		} catch (GeneralSecurityException e) {
			notifyError(new IOException(e.getMessage()));
		}
	}

	public final void tunnelEvent(SealedObject sealed_event) {
		try {
			if (decrypt_cipher == null)
				throw new IOException("Illegal stream state, event received before key agreement");
			ARMIEvent event = (ARMIEvent)sealed_event.getObject(decrypt_cipher);
			receiveEvent(event);
		} catch (BadPaddingException e) {
			notifyError(new IOException(e.getMessage()));
		} catch (IllegalBlockSizeException e) {
			notifyError(new IOException(e.getMessage()));
		} catch (ClassNotFoundException e) {
			notifyError(new IOException(e.getMessage()));
		} catch (IOException e) {
			notifyError(e);
		}
	}

	public final AbstractConnection getWrappedConnectionAndShutdown() {
		wrapped_connection.setConnectionInterface(getConnectionInterface());
		return wrapped_connection;
	}
	
	protected final void doClose() {
		wrapped_connection.close();
	}

	private final void tunnel(ARMIEvent event) {
		secure_interface.tunnelEvent(encrypt(event));
	}

	private void processEvent(ARMIEvent event) {
		try {
			event.execute(interface_methods, this);
		} catch (Exception e) {
			notifyError(new IOException(e.getMessage()));
		}
	}
	
	public final void handle(ARMIEvent event) {
		if (encrypt_cipher == null)
			event_backlog.add(event);
		else {
			tunnel(event);
		}
	}
}
