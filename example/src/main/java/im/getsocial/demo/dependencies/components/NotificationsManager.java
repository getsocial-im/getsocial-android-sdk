package im.getsocial.demo.dependencies.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.CompletionCallback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.pushnotifications.Notification;
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

	private int _filterStatus = 0;
	private Set<Integer> _chosenTypes = new HashSet<>();

	public NotificationsManager(Context context) {
		_context = context;
		loadFromPrefs();
	}

	public void saveFilter(int status, Set<Integer> chosenTypes) {
		_filterStatus = status;
		_chosenTypes = new HashSet<>(chosenTypes);
		saveToPrefs();
		sync();
	}

	public int getFilterStatus() {
		return _filterStatus;
	}

	public Set<Integer> getChosenTypes() {
		return _chosenTypes;
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
		setRead(collectAllIds(), true);
	}

	public void markAsRead(String id, boolean isRead) {
		setRead(Collections.singletonList(id), isRead);
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

	private void setRead(final List<String> ids, final boolean isRead) {
		GetSocial.User.setNotificationsRead(ids, isRead, new CompletionCallback() {
			@Override
			public void onSuccess() {
				updateStatuses(ids, isRead);
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
		NotificationsCountQuery query = _filterStatus == 1
				? NotificationsCountQuery.read()
				: _filterStatus == 2 ? NotificationsCountQuery.unread() : NotificationsCountQuery.readAndUnread();

		return query.ofTypes(getTypes());
	}

	private NotificationsQuery createNotificationsQuery() {
		NotificationsQuery query = _filterStatus == 1
				? NotificationsQuery.read()
				: _filterStatus == 2 ? NotificationsQuery.unread() : NotificationsQuery.readAndUnread();

		return query.ofTypes(getTypes());
	}

	private int[] getTypes() {
		if (_chosenTypes.isEmpty()) {
			return new int[0];
		}

		int[] types = new int[_chosenTypes.size()];
		int i = 0;
		for (int type : _chosenTypes) {
			types[i++] = type;
		}
		return types;
	}

	private void updateStatuses(List<String> ids, boolean isRead) {
		for (int i = 0; i < _notifications.size(); i++) {
			final Notification current = _notifications.get(i);
			if (ids.contains(current.getId())) {
				final Notification updated = new Notification(current.getId(), isRead, current.getType(), current.getCreatedAt(),
						current.getTitle(), current.getText(), current.getActionType(), current.getActionData(), current.getImageUrl(), current.getVideoUrl());
				_notifications.set(i, updated);
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
		final Set<String> types = new HashSet<>();
		for (int type : _chosenTypes) {
			types.add(Integer.toString(type));
		}
		_context.getSharedPreferences("notifications", Context.MODE_PRIVATE)
				.edit()
				.putInt("status", _filterStatus)
				.putStringSet("types", types)
				.apply();
	}

	private void loadFromPrefs() {
		final SharedPreferences sharedPreferences = _context.getSharedPreferences("notifications", Context.MODE_PRIVATE);
		final Set<String> types = sharedPreferences.getStringSet("types", Collections.<String>emptySet());

		_filterStatus = sharedPreferences.getInt("status", 0);

		final Set<Integer> intSet = new HashSet<>();
		for (String type : types) {
			intSet.add(Integer.parseInt(type));
		}
		_chosenTypes = new HashSet<>(intSet);
	}
}
