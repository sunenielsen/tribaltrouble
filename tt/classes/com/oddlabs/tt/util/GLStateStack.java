package com.oddlabs.tt.util;

import java.util.List;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

public final strictfp class GLStateStack {
	private static GLStateStack current;
	
	private final List state_stack = new ArrayList();

	public final static void setCurrent(GLStateStack stack) {
		current = stack;
	}
	
	public GLStateStack() {
		state_stack.add(new GLState());
	}
	
	private final GLState getCurrentState() {
		return (GLState)state_stack.get(state_stack.size() - 1);
	}

	public final static void pushState() {
		current.doPushState();
	}

	private final void doPushState() {
		try {
			state_stack.add((GLState)getCurrentState().clone());
			GL11.glPushClientAttrib(GL11.GL_ALL_CLIENT_ATTRIB_BITS);
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public final static void popState() {
		current.doPopState();
	}

	private final void doPopState() {
		GL11.glPopClientAttrib();
		state_stack.remove(state_stack.size() - 1);
	}

	public final static void switchState(int client_flags) {
		current.doSwitchState(client_flags);
	}

	private final void doSwitchState(int client_flags) {
		GLState current_state = getCurrentState();
		current_state.switchState(client_flags);
	}
}
