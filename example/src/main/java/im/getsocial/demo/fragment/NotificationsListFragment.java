package im.getsocial.demo.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.getsocial.demo.R;
import im.getsocial.demo.dependencies.DependenciesContainer;
import im.getsocial.demo.dependencies.components.NotificationsManager;
import im.getsocial.demo.dialog.NotificationsFilterDialog;
import im.getsocial.demo.utils.NotificationContext;
import im.getsocial.demo.utils.NotificationHandler;
import im.getsocial.sdk.CompletionCallback;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.pushnotifications.ActionButton;
import im.getsocial.sdk.pushnotifications.Notification;
import im.getsocial.sdk.pushnotifications.NotificationStatus;

public class NotificationsListFragment extends BaseFragment implements NotificationsManager.Listener, CompletionCallback {

	private final List<Notification> _notifications = new ArrayList<>();
	private NotificationsManager _notificationsManager;
	private NotificationsAdapter _notificationsAdapter;
	private NotificationHandler _notificationHandler;

	@Override
	protected void inject(DependenciesContainer dependencies) {
		_notificationsManager = dependencies.notificationsManager();
		_notificationHandler = dependencies.notificationHandler();
	}

	@Override
	public String getFragmentTag() {
		return "notifications_list";
	}

	@Override
	public String getTitle() {
		return "Notifications";
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.fragment_notifications, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ViewContainer viewContainer = new ViewContainer(view);
		viewContainer._notificationsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				NotificationsAdapter.ViewHolder viewHolder = (NotificationsAdapter.ViewHolder) view.getTag();
				if (viewHolder != null) {
					final Notification notification = viewHolder._notification;
					final String status = notification.getStatus();
					if (NotificationStatus.CONSUMED.equals(status) || NotificationStatus.IGNORED.equals(status)) {
						return;
					}
					_notificationsManager.setStatus(notification.getId(), NotificationStatus.UNREAD.equals(status) ? NotificationStatus.READ : NotificationStatus.UNREAD);
				}
			}
		});
		viewContainer._notificationsList.setAdapter(_notificationsAdapter = new NotificationsAdapter(getContext(), _notifications));
		viewContainer._notificationsList.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount == totalItemCount - 1 && totalItemCount != 0) {
					_notificationsManager.loadMore();
				}
			}
		});
		_notificationsManager.addListener(this);
		loadNotifications();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(Menu.NONE, 0x42, Menu.NONE, "Filter");
		menu.add(Menu.NONE, 0x43, Menu.NONE, "Mark all as read");
		menu.add(Menu.NONE, 0x44, Menu.NONE, "Refresh");
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		if (item.getItemId() == 0x42) {
			openFilter();
			return true;
		} else if (item.getItemId() == 0x43) {
			_notificationsManager.markAllAsRead();
			return true;
		} else if (item.getItemId() == 0x44) {
			_notificationsManager.sync();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSync() {
		loadNotifications();
	}

	private void loadNotifications() {
		_notifications.clear();
		_notifications.addAll(_notificationsManager.getNotifications());
		_notificationsAdapter.notifyDataSetChanged();
	}

	private void openFilter() {
		NotificationsFilterDialog.show(getFragmentManager());
	}

	@Override
	public void onSuccess() {
		//
	}

	@Override
	public void onFailure(GetSocialException exception) {
		_log.logErrorAndToast(exception);
	}

	class ViewContainer {

		@BindView(R.id.notifications_list)
		ListView _notificationsList;

		public ViewContainer(View view) {
			ButterKnife.bind(this, view);
		}
	}

	class NotificationsAdapter extends ArrayAdapter<Notification> {

		NotificationsAdapter(Context context, List<Notification> objects) {
			super(context, 0, objects);
		}

		@NonNull
		@Override
		public View getView(int position, @Nullable View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_notifications, null);
				convertView.setTag(holder = new ViewHolder(convertView));
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.setNotification(getItem(position));

			return convertView;
		}

		class ViewHolder {

			@BindView(R.id.notification_title)
			TextView _title;

			@BindView(R.id.notification_text)
			TextView _text;

			@BindView(R.id.date)
			TextView _date;

			@BindView(R.id.notification_image)
			ImageView _image;

			@BindView(R.id.notification_video)
			TextView _videoUrl;

			@BindView(R.id.unread_indicator)
			View _unreadIndicator;

			@BindView(R.id.notification_buttons)
			LinearLayout _notificationButtons;

			View _parent;

			private Notification _notification;

			ViewHolder(View view) {
				ButterKnife.bind(this, view);
				_parent = view;
			}

			void setNotification(Notification notification) {
				_notification = notification;
				invalidate();
			}

			private void invalidate() {
				String notificationTitle = TextUtils.isEmpty(_notification.getTitle()) ? "Notification" : _notification.getTitle();
				if (_notification.getStatus().equals(NotificationStatus.CONSUMED)) {
					notificationTitle += " (CONSUMED)";
				} else if (_notification.getStatus().equals(NotificationStatus.IGNORED)) {
					notificationTitle += " (IGNORED)";
				}
				_title.setText(notificationTitle);
				_text.setText(_notification.getText());
				_date.setText(DateFormat.getDateTimeInstance().format(new Date(_notification.getCreatedAt() * 1000)));
				if (_notification.getStatus().equals(NotificationStatus.UNREAD)) {
					_parent.setBackgroundColor(Color.rgb(150, 150, 150));
					_unreadIndicator.setVisibility(View.VISIBLE);
				} else {
					_parent.setBackgroundColor(Color.rgb(200, 200, 200));
					_unreadIndicator.setVisibility(View.INVISIBLE);
				}

				if (_notification.getImageUrl() != null) {
					_image.setVisibility(View.VISIBLE);
					Picasso.with(getContext()).load(_notification.getImageUrl()).into(_image);
				} else {
					_image.setVisibility(View.GONE);
				}

				if (_notification.getVideoUrl() != null) {
					_videoUrl.setVisibility(View.VISIBLE);
					_videoUrl.setText(_notification.getVideoUrl());
				} else {
					_videoUrl.setVisibility(View.GONE);
				}

				_notificationButtons.removeAllViews();

				if (_notification.getActionButtons().isEmpty()
						|| Arrays.asList(NotificationStatus.CONSUMED, NotificationStatus.IGNORED).contains(_notification.getStatus())) {
					_notificationButtons.setVisibility(View.GONE);
				} else {
					_notificationButtons.setVisibility(View.VISIBLE);
					for (final ActionButton actionButton : _notification.getActionButtons()) {
						final Button button = new Button(getContext());
						button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
						button.setText(actionButton.getTitle());
						button.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								final String actionId = actionButton.getId();
								_notificationHandler.handleNotification(_notification, NotificationContext.actioned(actionId));
							}
						});
						_notificationButtons.addView(button);
					}
				}
			}
		}
	}
}
