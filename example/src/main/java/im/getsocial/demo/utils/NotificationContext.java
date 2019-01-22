package im.getsocial.demo.utils;

import android.support.annotation.Nullable;

public final class NotificationContext {

	private final boolean _wasClicked;
	@Nullable
	private final String _action;

	public static NotificationContext actioned(String action) {
		return new NotificationContext(true, action);
	}

	public static NotificationContext clicked(boolean wasClicked) {
		return new NotificationContext(wasClicked, null);
	}

	private NotificationContext(boolean wasClicked, @Nullable String action) {
		_wasClicked = wasClicked;
		_action = action;
	}

	public boolean wasClicked() {
		return _wasClicked;
	}

	@Nullable
	public String getAction() {
		return _action;
	}
}
