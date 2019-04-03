package im.getsocial.demo.fragment;

import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.sdk.pushnotifications.ActionButton;
import im.getsocial.sdk.pushnotifications.Notification;
import im.getsocial.sdk.ui.GetSocialUi;
import im.getsocial.sdk.ui.pushnotifications.NotificationCenterViewBuilder;

import java.util.Arrays;
import java.util.List;

public class NotificationsFragment extends BaseListFragment {

	@Override
	protected List<MenuItem> createListData() {
		return Arrays.asList(
				navigationListItem("Notifications List", NotificationsListFragment.class),
				navigationListItem("Send Notification", SendNotificationsFragment.class),
				new MenuItem.Builder("Notification Center UI").withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						configureNCView();
					}
				}).build()
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

	private void configureNCView() {
		GetSocialUi.createNotificationCenterView()
				.setActionButtonClickListener(new NotificationCenterViewBuilder.ActionButtonClickListener() {
					@Override
					public boolean onActionButtonClicked(Notification notification, ActionButton actionButton) {
						_log.logInfoAndToast(String.format("Action button [%s] for notification [%s] clicked", actionButton.getId(), notification.getId()));
						return false;
					}
				})
				.setNotificationClickListener(new NotificationCenterViewBuilder.NotificationClickListener() {
					@Override
					public boolean onNotificationClicked(Notification notification) {
						_activityListener.dependencies().actionListener().handleAction(notification.getAction());
						return false;
					}
				})
				.show();
	}
}
