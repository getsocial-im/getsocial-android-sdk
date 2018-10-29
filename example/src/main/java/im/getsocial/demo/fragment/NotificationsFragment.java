package im.getsocial.demo.fragment;

import im.getsocial.demo.adapter.MenuItem;

import java.util.Arrays;
import java.util.List;

public class NotificationsFragment extends BaseListFragment {
	@Override
	protected List<MenuItem> createListData() {
		return Arrays.asList(
				navigationListItem("Notifications List", NotificationsListFragment.class),
				navigationListItem("Send Notification", SendNotificationsFragment.class)
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
}
