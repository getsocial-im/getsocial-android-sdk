package im.getsocial.demo.fragment;

import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.notifications.NotificationButton;
import im.getsocial.sdk.notifications.NotificationStatus;
import im.getsocial.sdk.notifications.NotificationsQuery;
import im.getsocial.sdk.ui.pushnotifications.NotificationCenterViewBuilder;

import java.util.Arrays;
import java.util.List;

public class NotificationsFragment extends BaseListFragment {

	@Override
	protected List<MenuItem> createListData() {
		return Arrays.asList(
						navigationListItem("Notifications List", NotificationsSearchFragment.class),
						navigationListItem("Send Notification", SendNotificationsFragment.class),
						MenuItem.builder("Notification Center UI with handlers").withAction(this::configureNCView).build(),
						MenuItem.builder("Notification Center UI without handlers").withAction(this::configureNCViewWithoutHandlers).build()
		);
	}

	@Override
	public String getFragmentTag() {
		return "notifications";
	}

	@Override
	public String getTitle() {
		return "Notifications";
	}

	private void configureNCViewWithoutHandlers() {
		NotificationCenterViewBuilder
						.create(NotificationsQuery.withStatuses(NotificationStatus.READ, NotificationStatus.UNREAD))
						.show();
	}

	private void configureNCView() {
		NotificationCenterViewBuilder.create(NotificationsQuery.withStatuses(NotificationStatus.READ, NotificationStatus.
						UNREAD))
						.setNotificationClickListener((notification, context) -> {
							_activityListener.dependencies().actionListener().handleAction(notification.getAction());
							if (context.getActionButtonId() == null) {
								_log.logInfoAndToast(String.format("Notification [%s] clicked", notification.getId()));
								GetSocial.handle(notification.getAction());
							} else {
								_log.logInfoAndToast(String.format("Action button [%s] for notification [%s] clicked", context.getActionButtonId(), notification.getId()));
								if (!NotificationButton.IGNORE_ACTION.equals(context.getActionButtonId())) {
									GetSocial.handle(notification.getAction());
								}
							}
						})
						.show();
	}
}
