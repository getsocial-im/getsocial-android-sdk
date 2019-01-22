package im.getsocial.demo.dependencies.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.CompletionCallback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.pushnotifications.Notification;
import im.getsocial.sdk.pushnotifications.NotificationStatus;
import im.getsocial.sdk.pushnotifications.NotificationsCountQuery;
import im.getsocial.sdk.pushnotifications.NotificationsQuery;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class NotificationsManager {

	public interface Listener {
		void onSync();
	}

	private boolean _isLoadingMore = false;

	private final Context _context;
	private final Set<WeakReference<Listener>> _listeners = new HashSet<>();

	private int _notificationsCount;
	private List<Notification> _notifications = new ArrayList<>();

	private Set<String> _actionTypes = new HashSet<>();
	private Set<String> _filterStatus = new HashSet<>();
	private Set<String> _chosenTypes = new HashSet<>();

	public NotificationsManager(Context context) {
		_context = context;
		loadFromPrefs();
	}

	public void saveFilter(Set<String> statuses, Set<String> chosenTypes, Set<String> actionTypes) {
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

	public void addListener(Listener listener) {
		_listeners.add(new WeakReference<Listener>(listener));
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

	public void setStatus(String id, String status) {
		setStatus(Collections.singletonList(id), status);
	}

	public void loadMore() {
		if (_isLoadingMore) {
			return;
		}
		_isLoadingMore = true;
		final NotificationsQuery query = createNotificationsQuery()
				.withFilter(NotificationsQuery.Filter.OLDER, _notifications.get(_notifications.size() - 1).getId());
		GetSocial.User.getNotifications(query, new Callback<List<Notification>>() {
			@Override
			public void onSuccess(List<Notification> result) {
				_notifications.addAll(result);
				notifyListeners();
				_isLoadingMore = false;
			}

			@Override
			public void onFailure(GetSocialException exception) {
				Log.e("Notifications", "Failed to load more: " + exception);
				_isLoadingMore = false;
			}
		});
	}

	private void setStatus(final List<String> ids, final String newStatus) {
		GetSocial.User.setNotificationsStatus(ids, newStatus, new CompletionCallback() {
			@Override
			public void onSuccess() {
				updateStatuses(ids, newStatus);
			}

			@Override
			public void onFailure(GetSocialException exception) {
				Log.e("Notifications", "Failed to mark notification, exception: " + exception);
			}
		});
	}

	public void sync() {
		if (!GetSocial.isInitialized()) {
			return;
		}
		GetSocial.User.getNotificationsCount(createNotificationsCountQuery(), new Callback<Integer>() {
			@Override
			public void onSuccess(Integer result) {
				_notificationsCount = result;
				loadNotifications();
			}

			@Override
			public void onFailure(GetSocialException exception) {
				Log.e("Notifications", "Failed to load notifications count: " + exception);
				loadNotifications();
			}
		});
	}

	private void loadNotifications() {
		GetSocial.User.getNotifications(createNotificationsQuery(), new Callback<List<Notification>>() {
			@Override
			public void onSuccess(List<Notification> result) {
				_notifications = new ArrayList<>(result);
				notifyListeners();
			}

			@Override
			public void onFailure(GetSocialException exception) {
				Log.e("Notifications", "Failed to load notifications: " + exception);
				notifyListeners();
			}
		});
	}

	private NotificationsCountQuery createNotificationsCountQuery() {
		return NotificationsCountQuery.withStatuses(_filterStatus.toArray(new String[0]))
				.withActions(_actionTypes.toArray(new String[0]))
				.ofTypes(_chosenTypes.toArray(new String[0]));
	}

	private NotificationsQuery createNotificationsQuery() {
		return NotificationsQuery.withStatuses(_filterStatus.toArray(new String[0]))
				.withActions(_actionTypes.toArray(new String[0]))
				.ofTypes(_chosenTypes.toArray(new String[0]));
	}

	private void updateStatuses(List<String> ids, String newStatus) {
		for (int i = 0; i < _notifications.size(); i++) {
			final Notification current = _notifications.get(i);
			if (ids.contains(current.getId())) {
				final Notification.Builder updated = Notification.builder(current.getId())
						.withText(current.getText())
						.withTitle(current.getTitle())
						.withType(current.getType())
						.withAction(current.getAction())
						.withCreatedAt(current.getCreatedAt())
						.withImageUrl(current.getImageUrl())
						.withVideoUrl(current.getVideoUrl())
						.withStatus(newStatus)
						.addActionButtons(current.getActionButtons());

				_notifications.set(i, updated.build());
			}
		}
		notifyListeners();
	}

	private List<String> collectAllIds() {
		List<String> ids = new ArrayList<>(_notifications.size());
		for (Notification notification : _notifications) {
			ids.add(notification.getId());
		}
		return ids;
	}

	private void notifyListeners() {
		for (Iterator<WeakReference<Listener>> iterator = _listeners.iterator(); iterator.hasNext();) {
			Listener listener = iterator.next().get();
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

		_chosenTypes = sharedPreferences.getStringSet("types", Collections.<String>emptySet());
		_filterStatus = sharedPreferences.getStringSet("status", Collections.<String>emptySet());
		_actionTypes = sharedPreferences.getStringSet("actions", Collections.<String>emptySet());
	}
}
