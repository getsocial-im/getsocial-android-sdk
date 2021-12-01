package im.getsocial.demo.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.squareup.picasso.Picasso;
import im.getsocial.demo.R;
import im.getsocial.demo.dependencies.DependenciesContainer;
import im.getsocial.demo.dependencies.components.NotificationsManager;
import im.getsocial.demo.dialog.NotificationsFilterDialog;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.Notifications;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.common.PagingResult;
import im.getsocial.sdk.notifications.Notification;
import im.getsocial.sdk.notifications.NotificationButton;
import im.getsocial.sdk.notifications.NotificationContext;
import im.getsocial.sdk.notifications.NotificationStatus;
import im.getsocial.sdk.notifications.NotificationsQuery;
import im.getsocial.sdk.notifications.OnNotificationClickedListener;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

public class NotificationsSearchFragment extends BaseSearchFragment<NotificationsQuery, Notification> {

	private NotificationsManager _notificationsManager;
	private OnNotificationClickedListener _notificationHandler;
	private int _count = -1;

	@Override
	protected void inject(final DependenciesContainer dependencies) {
		_notificationsManager = dependencies.notificationsManager();
		_notificationHandler = dependencies.notificationHandler();
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		menu.add(Menu.NONE, 0x42, Menu.NONE, "Filter");
		menu.add(Menu.NONE, 0x43, Menu.NONE, "Mark all as read");
		menu.add(Menu.NONE, 0x44, Menu.NONE, "Refresh");
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final android.view.MenuItem item) {
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

	private void openFilter() {
		NotificationsFilterDialog.show(getFragmentManager());
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_notificationsManager.addListener(this::loadItems);
		_query.setVisibility(View.GONE);
		loadCount(createQuery(SearchObject.empty()), result -> {
			_count = result;
			_activityListener.invalidateUi();
		}, this::onError);
	}

	protected void loadCount(final NotificationsQuery query, final Callback<Integer> callback, final FailureCallback failureCallback) {
		Notifications.getCount(query, callback, failureCallback);
	}

	@Override
	protected BaseSearchAdapter<? extends ViewHolder> createAdapter() {
		return new BaseSearchAdapter<ViewHolder>() {
			@NonNull
			@Override
			public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
				final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notifications, parent, false);
				return new NotificationsViewHolder(view);
			}
		};
	}

	@Override
	protected void load(final PagingQuery<NotificationsQuery> query, final Callback<PagingResult<Notification>> success, final FailureCallback failure) {
		Notifications.get(query, success, failure);
	}

	@Override
	protected NotificationsQuery createQuery(final SearchObject searchObject) {
		return _notificationsManager.createNotificationsQuery();
	}

	@Override
	public String getFragmentTag() {
		return "notifications_list";
	}

	@SuppressLint("DefaultLocale")
	@Override
	public String getTitle() {
		return _count == -1 ? "Notifications" : String.format("Notifications(%d)", _count);
	}

	class NotificationsViewHolder extends ViewHolder {

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

		@BindView(R.id.customization_bgImage)
		TextView _customBgImage;

		@BindView(R.id.customization_titleColor)
		TextView _customTitleColor;

		@BindView(R.id.customization_textColor)
		TextView _customTextColor;

		NotificationsViewHolder(@NonNull final View itemView) {
			super(itemView);
		}

		@Override
		protected void bind(final View itemView) {
			ButterKnife.bind(this, itemView);
		}

		@Override
		public void invalidate() {
			String notificationTitle = TextUtils.isEmpty(_item.getTitle()) ? "Notification" : _item.getTitle();
			final String status = _item.getStatus();
			if (NotificationStatus.CONSUMED.equals(status)) {
				notificationTitle += " (CONSUMED)";
			} else if (NotificationStatus.IGNORED.equals(status)) {
				notificationTitle += " (IGNORED)";
			}
			_title.setText(notificationTitle);
			_text.setText(_item.getText());
			if (_item.getCustomization() != null) {
				_customBgImage.setText("Bg Image: " + _item.getCustomization().getBackgroundImageConfiguration());
				_customTitleColor.setText("Title color: " + _item.getCustomization().getTitleColor());
				_customTextColor.setText("Text color: " + _item.getCustomization().getTextColor());
			}
			_date.setText(DateFormat.getDateTimeInstance().format(new Date(_item.getCreatedAt() * 1000)));
			if (NotificationStatus.UNREAD.equals(status)) {
				_parent.setBackgroundColor(Color.rgb(150, 150, 150));
				_unreadIndicator.setVisibility(View.VISIBLE);
			} else {
				_parent.setBackgroundColor(Color.rgb(200, 200, 200));
				_unreadIndicator.setVisibility(View.INVISIBLE);
			}

			_image.setVisibility(View.GONE);
			_videoUrl.setVisibility(View.GONE);
			if (_item.getAttachment() != null) {
				if (_item.getAttachment().getImageUrl() != null) {
					_image.setVisibility(View.VISIBLE);
					Picasso.with(getContext()).load(_item.getAttachment().getImageUrl()).into(_image);
				}

				if (_item.getAttachment().getVideoUrl() != null) {
					_videoUrl.setVisibility(View.VISIBLE);
					_videoUrl.setText(_item.getAttachment().getVideoUrl());
				}
			}

			_notificationButtons.removeAllViews();

			if (_item.getActionButtons().isEmpty()
							|| Arrays.asList(NotificationStatus.CONSUMED, NotificationStatus.IGNORED).contains(_item.getStatus())) {
				_notificationButtons.setVisibility(View.GONE);
			} else {
				_notificationButtons.setVisibility(View.VISIBLE);
				for (final NotificationButton actionButton : _item.getActionButtons()) {
					final Button button = new Button(getContext());
					button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
					button.setText(actionButton.getTitle());
					button.setOnClickListener(view -> {
						final String actionId = actionButton.getId();
						_notificationHandler.onNotificationClicked(_item, new NotificationContext(actionId));
					});
					_notificationButtons.addView(button);
				}
			}
		}
	}
}
