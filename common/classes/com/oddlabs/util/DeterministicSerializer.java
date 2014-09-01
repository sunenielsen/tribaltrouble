package com.oddlabs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import com.oddlabs.event.Deterministic;

public final strictfp class DeterministicSerializer {
	private DeterministicSerializer() {
	}

	public final static void save(Deterministic deterministic, final Object object, final File file, final DeterministicSerializerLoopbackInterface callback_loopback) {
		IOException exception;
		try {
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
			os.writeObject(object);
			exception = null;
		} catch (IOException e) {
			exception = e;
		}
		if (deterministic.log(exception != null))
			callback_loopback.failed((Exception)deterministic.log(exception));
		else
			callback_loopback.saveSucceeded();
	}

	public final static void load(Deterministic deterministic, final File file, final DeterministicSerializerLoopbackInterface callback_loopback) {
		Object object;
		Exception exception;
		try {
			ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
			object = is.readObject();
			exception = null;
		} catch (Exception e) {
			exception = e;
			object = null;
		}
		if (deterministic.log(exception != null))
			callback_loopback.failed((Exception)deterministic.log(exception));
		else
			callback_loopback.loadSucceeded(deterministic.log(object));
	}
}
