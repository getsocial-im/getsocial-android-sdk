package im.getsocial.demo.dependencies.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.Notifications;
import im.getsocial.sdk.notifications.Notification;
import im.getsocial.sdk.notifications.NotificationStatus;
import im.getsocial.sdk.notifications.NotificationsQuery;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class NotificationsManager {

	private final Context _context;
	private final Set<WeakReference<Listener>> _listeners = new HashSet<>();
	private final boolean _isLoadingMore = false;
	private int _notificationsCount;
	private final List<Notification> _notifications = new ArrayList<>();
	private Set<String> _actionTypes = new HashSet<>();
	private Set<String> _filterStatus = new HashSet<>();
	private Set<String> _chosenTypes = new HashSet<>();

	public NotificationsManager(final Context context) {
		_context = context;
		loadFromPrefs();
	}

	public void saveFilter(final Set<String> statuses, final Set<String> chosenTypes, final Set<String> actionTypes) {
		_filterStatus = new HashSet<>(statuses);
		_chosenTypes = new HashSet<>(chosenTypes);
		_actionTypes = actionTypes;
		saveToPrefs();
		sync();
	}

	public Set<String> getFilterStatus() {
		return _filterStatus;
	}

	public Set<String> getChosenTypes() {
		return _chosenTypes;
	}

	public Set<String> getActionTypes() {
		return _actionTypes;
	}

	public void addListener(final Listener listener) {
		_listeners.add(new WeakReference<>(listener));
	}

	public int getNewNotificationsCount() {
		return _notificationsCount;
	}

	public List<Notification> getNotifications() {
		return _notifications;
	}

	public void markAllAsRead() {
		setStatus(collectAllIds(), NotificationStatus.READ);
	}

	public void setStatus(final String id, final String status) {
		setStatus(Collections.singletonList(id), status);
	}

	private void setStatus(final List<String> ids, final String newStatus) {
		Notifications.setStatus(
						newStatus, ids,
						() -> updateStatuses(ids, newStatus),
						exception -> Log.e("Notifications", "Failed to mark notification, exception: " + exception.getMessage())
		);
	}

	public void sync() {
		if (!GetSocial.isInitialized()) {
			return;
		}
		Notifications.getCount(createNotificationsQuery(), result -> {
			_notificationsCount = result;
		}, error -> {
			Log.e("Notifications", "Failed to load notifications count: " + error);
		});
	}

	public NotificationsQuery createNotificationsQuery() {
		return NotificationsQuery.withStatuses(_filterStatus.toArray(new String[0]))
						.withActions(_actionTypes.toArray(new String[0]))
						.ofTypes(_chosenTypes.toArray(new String[0]));
	}

	private void updateStatuses(final List<String> ids, final String newStatus) {
		for (int i = 0; i < _notifications.size(); i++) {
			final Notification oldNotification = _notifications.get(i);
			if (ids.contains(oldNotification.getId())) {
				final Notification notification = new Notification(
								oldNotification.getId(), oldNotification.getActionButtons(), newStatus, oldNotification.getType(), oldNotification.getCreatedAt(),
								oldNotification.getTitle(), oldNotification.getText(), oldNotification.getAction(),
								oldNotification.getAttachment(), oldNotification.getSender(), oldNotification.getCustomization()
				);
				_notifications.set(i, notification);
			}
		}
		notifyListeners();
	}

	private List<String> collectAllIds() {
		final List<String> ids = new ArrayList<>(_notifications.size());
		for (final Notification notification : _notifications) {
			ids.add(notification.getId());
		}
		return ids;
	}

	private void notifyListeners() {
		for (final Iterator<WeakReference<Listener>> iterator = _listeners.iterator(); iterator.hasNext(); ) {
			final Listener listener = iterator.next().get();
			if (listener != null) {
				listener.onSync();
			} else {
				iterator.remove();
			}
		}
	}

	private void saveToPrefs() {
		_context.getSharedPreferences("notifications", Context.MODE_PRIVATE)
						.edit()
						.putStringSet("status", _filterStatus)
						.putStringSet("types", _chosenTypes)
						.putStringSet("actions", _actionTypes)
						.apply();
	}

	private void loadFromPrefs() {
		final SharedPreferences sharedPreferences = _context.getSharedPreferences("notifications", Context.MODE_PRIVATE);

		_chosenTypes = sharedPreferences.getStringSet("types", Collections.emptySet());
		_filterStatus = sharedPreferences.getStringSet("status", Collections.emptySet());
		_actionTypes = sharedPreferences.getStringSet("actions", Collections.emptySet());
	}

	public interface Listener {
		void onSync();
	}
}
