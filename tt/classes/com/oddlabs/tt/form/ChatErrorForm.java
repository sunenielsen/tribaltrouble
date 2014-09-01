package com.oddlabs.tt.form;

import java.util.ResourceBundle;

import com.oddlabs.matchmaking.MatchmakingClientInterface;
import com.oddlabs.tt.util.Utils;

public final strictfp class ChatErrorForm extends MessageForm {
	private static final String getErrorFromCode(int error_code) {
		ResourceBundle bundle = ResourceBundle.getBundle(ChatErrorForm.class.getName());
		switch (error_code) {
			case MatchmakingClientInterface.CHAT_ERROR_TOO_MANY_USERS:
				return Utils.getBundleString(bundle, "chat_error_too_many_users");
			case MatchmakingClientInterface.CHAT_ERROR_INVALID_NAME:
				return Utils.getBundleString(bundle, "chat_error_invalid_name");
			case MatchmakingClientInterface.CHAT_ERROR_NO_SUCH_NICK:
				return Utils.getBundleString(bundle, "chat_error_no_such_nick");
			default:
				throw new RuntimeException("Unknown error code: " + error_code);
		}
	}
	
	public ChatErrorForm(int error_code) {
		super(getErrorFromCode(error_code));
	}
}
